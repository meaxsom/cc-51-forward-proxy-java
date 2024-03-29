package com.gruecorner.forwardproxy;

import com.gruecorner.forwardproxy.utils.AccessLogger;
import com.gruecorner.forwardproxy.utils.HttpReply;
import com.gruecorner.forwardproxy.utils.StreamClient;
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
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
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
    private final static String kUserAgentHeaderValue       = "cc-51-proxy/0.5.0";

    private final static String kProxyAgentHeaderKey         = "Proxy-Agent";
    private final static String kHttpMethodConnect          = "CONNECT";

    private static List<String> s_bannedHosts          = null;
    private static List<String> s_bannedWords          = null;

    public ProxyHandler(List<String> inBannedHosts, List<String> inBannedWords) {
        s_bannedHosts=inBannedHosts;
        s_bannedWords = inBannedWords;
    }

    public void handleConnection(final InputStream inInputStream, final OutputStream inOutputStream, InetAddress inAddress, int inPort) throws IOException {
        Optional<HttpProxyRequest> theRequest = HttpRequestDecoder.decode(inInputStream);

        if (theRequest.isPresent()) {
            HttpProxyRequest theProxyRequest = theRequest.get();

            if (isHostAllowed(theProxyRequest)) {
                // pass along the client address and port to the request
                theProxyRequest.setInetAddress(inAddress);
                theProxyRequest.setPort(inPort);

                // decide if this is a connect request for a tunnel or something more normal
                if (kHttpMethodConnect.equalsIgnoreCase(theProxyRequest.getMethod())) {
                    tunnelRequest(theProxyRequest, inInputStream, inOutputStream);
                }
                else { // normal stuff
                    Optional<HttpResponse<String>> theResponseOptional = proxyRequest(theProxyRequest);
                    if (theResponseOptional.isPresent()) {
                        HttpResponse<String> theResponse = theResponseOptional.get();
                        if (theResponse.statusCode() == 200) {
                            // check for banned words in the response body
                            if (isWordsAllowed(theResponse.body()))
                                handleResponse(theProxyRequest, theResponse, inOutputStream);
                            else
                                handleInvalidResponse(theProxyRequest, inOutputStream, HttpReply.ResponseCode.Forbidden, "Website content not allowed.");
                        }
                        else // should try to pass back a response code that matches the original
                            handleInvalidResponse(theProxyRequest, inOutputStream, HttpReply.ResponseCode.BadRequest, "Host returned: " + theResponse.statusCode());
                    } else {
                       handleInvalidResponse(theProxyRequest, inOutputStream, HttpReply.ResponseCode.BadRequest, "No response from host");
                    }
                }
            } else
                handleInvalidResponse(theProxyRequest, inOutputStream, HttpReply.ResponseCode.Forbidden, "Website not allowed: " + theProxyRequest.getRequst());
        } else {
            handleInvalidResponse(null, inOutputStream, HttpReply.ResponseCode.BadRequest, "Request could not be decoded...");
            kLogger.error("Could not decode request");
        }
        
        inOutputStream.close();
    }

    public void tunnelRequest(HttpProxyRequest inProxyRequest, InputStream inInputStream, OutputStream inOutputStream) {
        // build the reply to the client for establising the tunnel
        HttpReply theReply = HttpReply.Builder.newInstance()
        .setResponseCode(HttpReply.ResponseCode.ProxyConnectionOK)
        .addHeader(kProxyAgentHeaderKey, kUserAgentHeaderValue)
        .build();

        try {
            try {
                URL theUrl = new URL(inProxyRequest.getRequst());
                
                // get the kind of socket we'll need - it'll do the right thing based on the protocol in the host
                Socket theTunnel = new Socket(theUrl.getHost(), theUrl.getPort());

                // create different streamers.. one for each direction
                StreamClient theServerStream = new StreamClient("client->server", inInputStream, theTunnel.getOutputStream());
                StreamClient theClientStream = new StreamClient("server->client", theTunnel.getInputStream(), inOutputStream);
 
                // keep the list of streamers so we can check their "join" status later
                List<StreamClient> theClients = new ArrayList<>();
                theClients.add(theServerStream);
                theClients.add(theClientStream);

                // start the streams, send the notification to start and wait for the threads to join
                theServerStream.start();
                theClientStream.start();

                // reply to the client that we're ready with the tunnel
                theReply.send(inOutputStream);

                // wait for all clients to finish
                for (StreamClient theClient : theClients)
                    theClient.join();

                // close the tunnel socket
                theTunnel.close();

            } catch(Throwable theErr) {
                kLogger.error("Error with tunnel", theErr);
            }
        } catch (Throwable theErr) {
            kLogger.error("Error establishing proxy tunnel", theErr);
        }
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
            AccessLogger.log("Client: " + inProxyRequest.getInetAddressString() + " Request URL: " + inProxyRequest.getRequst());

            HttpResponse<String> theResponse = theClient.send(theHttpRequest, BodyHandlers.ofString());
            result = Optional.of(theResponse);
        } catch(Throwable theErr) {
            kLogger.error("Error making request", theErr);
        }

    return result;
    }

    public void handleResponse(HttpProxyRequest inRequest, HttpResponse<String> inResponse, OutputStream inOutputStream) throws IOException {
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
            AccessLogger.log(inRequest, HttpReply.ResponseCode.OK);
        } else
            handleInvalidResponse(inRequest, inOutputStream, HttpReply.ResponseCode.BadRequest, "Bad Request...");
    }

    // write back an error to the client
    public void handleInvalidResponse(HttpProxyRequest inRequest, OutputStream inOutputStream, ResponseCode inResponseCode, String inMessage) throws IOException {
        HttpReply theNotFoundReply = HttpReply.Builder.newInstance()
            .setBody(inMessage)
            .setResponseCode(inResponseCode)
            .build();
        theNotFoundReply.send(inOutputStream);
        AccessLogger.log(inRequest, inResponseCode);
    }

    private boolean isHostAllowed(HttpProxyRequest inRequest) {
        return isHostAllowed(inRequest, s_bannedHosts, s_bannedWords);
    }

    // check both host list AND word list
    public boolean isHostAllowed(HttpProxyRequest inRequest, List<String> inBannedHosts, List<String> inBannedWords) {
        boolean result = true;

        try {
            URL theUrl = inRequest.getRequst() != null ? new URL(inRequest.getRequst()) : null;
            if (theUrl != null)
                result = isAllowed(theUrl.getHost(), inBannedHosts) && isAllowed(theUrl.getHost(), inBannedWords);

        } catch(Throwable theErr) {
            kLogger.error("can't evaluate if host " + inRequest.getRequst() + " is allowed, banning...", theErr);
            result=false;
        }
        return result;
    }

    private boolean isWordsAllowed(String inData) {
        return isAllowed(inData, s_bannedWords);
    }

    public boolean isAllowed(String inData, List<String> inBannedList) {
        boolean result = true;

        if (inBannedList != null && inData != null) {
            // standardize for lower case compare
            String theData = inData.toLowerCase();
            for (String theBannedData : inBannedList) {
                if (theData.contains(theBannedData)) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }


}
