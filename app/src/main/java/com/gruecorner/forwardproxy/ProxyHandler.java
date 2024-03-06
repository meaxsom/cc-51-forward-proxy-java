package com.gruecorner.forwardproxy;

import com.gruecorner.forwardproxy.utils.HttpReply;
import com.gruecorner.forwardproxy.utils.HttpReply.ResponseCode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProxyHandler {
	private final static Logger kLogger =	LogManager.getLogger(ProxyHandler.class.getName());

    private final static String kXForwardedForHeaderKey     = "X-Forwarded-For";
    private final static String kDefaultFowardAddress       = "127.0.0.1";
    private final static String kUserAgentHeaderKey         = "User-Agent";
    private final static String kUserAgentHeaderValue       = "cc-51-proxy";

    private static List<String> s_bannedHosts          = null;

    public ProxyHandler(List<String> inBannedHosts) {
        s_bannedHosts=inBannedHosts;
    }

    public void handleConnection(final InputStream inInputStream, final OutputStream inOutputStream, InetAddress inAddress, int inPort) throws IOException {
        Optional<HttpProxyRequest> theRequest = HttpRequestDecoder.decode(inInputStream);

        if (theRequest.isPresent()) {
            HttpProxyRequest theProxyRequest = theRequest.get();

            if (isHostAllowed(theProxyRequest)) {
                // pass along the client address and port to the request
                theProxyRequest.setInetAddress(inAddress);
                theProxyRequest.setPort(inPort);

                Optional<HttpResponse<String>> theResponseOptional = proxyRequest(theProxyRequest);
                if (theResponseOptional.isPresent()) {
                    HttpResponse<String> theResponse = theResponseOptional.get();
                    if (theResponse.statusCode() == 200)
                        handleResponse(theResponse, inOutputStream);
                    else // should try to pass back a response code that matches the original
                        handleInvalidResponse(inOutputStream, HttpReply.ResponseCode.BadRequest, "Host returned: " + theResponse.statusCode());
                } else
                    handleInvalidResponse(inOutputStream, HttpReply.ResponseCode.BadRequest, "No response from host");
            } else
                handleInvalidResponse(inOutputStream, HttpReply.ResponseCode.Forbidden, "Website not allowed: " + theProxyRequest.getRequst());
        } else {
            handleInvalidResponse(inOutputStream, HttpReply.ResponseCode.BadRequest, "Request could not be decoded...");
            kLogger.error("Could not decode request");
        }
        
        inOutputStream.close();
    }

    public Optional<HttpResponse<String>> proxyRequest(HttpProxyRequest inProxyRequest) {
        Optional<HttpResponse<String>> result = Optional.empty();

        // we're ready to create an HttpClient to the intended host
        HttpClient theClient = HttpClient.newHttpClient();

        // make sure we have a forwarding address
        String theForwardAddress = kDefaultFowardAddress;
        InetAddress theInetAddress = inProxyRequest.getInetAddress();
        if (theInetAddress != null)
            theForwardAddress = theInetAddress.getHostAddress();

        HttpRequest theHttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(inProxyRequest.getRequst()))
            .header(kXForwardedForHeaderKey, theForwardAddress)
            .header(kUserAgentHeaderKey, kUserAgentHeaderValue)
            .build();

            
        try {
            // report on the request.. should go to configured logger
            System.out.println("Request made. Target: " + inProxyRequest.getRequst() + " Client: " + inProxyRequest.getInetAddressString() + ":" + inProxyRequest.getPort());

            HttpResponse<String> theResponse = theClient.send(theHttpRequest, BodyHandlers.ofString());
            result = Optional.of(theResponse);
        } catch(Throwable theErr) {
            kLogger.error("Error making request", theErr);
        }
        
    return result;
    }

    public void handleResponse(HttpResponse<String> inResponse, OutputStream inOutputStream) throws IOException {
        if (inResponse != null) {
            // convert headers into our style if they exist
            Map<String, String> theNewHeaders = new HashMap<String, String>();
            HttpHeaders theHeaders = inResponse.headers();
            if (theHeaders != null) {
                Map<String, List<String>> theResponseHeaders = theHeaders.map();
                for (String theKey : theResponseHeaders.keySet()) {
                    List<String> theValues = theResponseHeaders.get(theKey);
                    theNewHeaders.put(theKey, theValues.get(0));
                }
            }

            HttpReply theReply = HttpReply.Builder.newInstance()
                .setResponseCode(HttpReply.ResponseCode.OK)
                .setBody(inResponse.body())
                .setHeaders(theNewHeaders).build();

            theReply.send(inOutputStream);
        } else
            handleInvalidResponse(inOutputStream, HttpReply.ResponseCode.BadRequest, "Bad Request...");
    }

    public void handleInvalidResponse(OutputStream inOutputStream, ResponseCode inResponseCode, String inMessage) throws IOException {
        HttpReply theNotFoundReply = HttpReply.Builder.newInstance()
            .setBody(inMessage)
            .setResponseCode(inResponseCode)
            .build();
        theNotFoundReply.send(inOutputStream);
    }

    private boolean isHostAllowed(HttpProxyRequest inRequest) {
        return isHostAllowed(inRequest, s_bannedHosts);
    }

    public boolean isHostAllowed(HttpProxyRequest inRequest, List<String> inBannedHosts) {
        boolean result = true;

        try
            {
            URL theUrl = inRequest.getRequst() != null ? new URL(inRequest.getRequst()) : null;
            if (inBannedHosts != null && theUrl != null) {
                // standardize for lower case compare
                String theTargetHost = theUrl.getHost().toLowerCase();

                for (String theBannedHost : inBannedHosts) {
                    if (theTargetHost.contains(theBannedHost)) {
                        result = false;
                        break;
                    }
                }
            }
        } catch (Throwable theErr) {
            kLogger.error("can't evaluate if host is allowed, banning...");
            result=false;
        }

        return result;
    }
}
