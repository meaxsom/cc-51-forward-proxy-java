package com.gruecorner.forwardproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.InetAddress;
import java.net.URI;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProxyHandler {
	private final static Logger kLogger =	LogManager.getLogger(ProxyHandler.class.getName());

    private final static String kXForwardedForHeaderKey = "X-Forwarded-For";
    private final static String kDefaultFowardAddress = "127.0.0.1";

    public ProxyHandler() {
    }

    public void handleConnection(final InputStream inputStream, final OutputStream outputStream, InetAddress inAddress, int inPort) throws IOException {
        Optional<HttpProxyRequest> theRequest = HttpRequestDecoder.decode(inputStream);

        if (theRequest.isPresent()) {
            HttpProxyRequest theProxyRequest = theRequest.get();

            // pass along the client address and port to the request
            theProxyRequest.setAddress(inAddress);
            theProxyRequest.setPort(inPort);

            // we're ready to create an HttpClient to the intended host
            proxyRequest(theProxyRequest);
        } else
            kLogger.error("Could not decode request");

        outputStream.close();
    }
    public void proxyRequest(HttpProxyRequest inProxyRequest) {
        // we're ready to create an HttpClient to the intended host
        HttpClient theClient = HttpClient.newHttpClient();

        // make sure we have a forwarding address
        String theForwardAddress = kDefaultFowardAddress;
        InetAddress theInetAddress = inProxyRequest.getAddress();
        if (theInetAddress != null)
            theForwardAddress = theInetAddress.getHostAddress();

        HttpRequest theHttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(inProxyRequest.getRequst()))
            .header(kXForwardedForHeaderKey, theForwardAddress)
            .build();

        try {

            HttpResponse<String> theResponse = theClient.send(theHttpRequest, BodyHandlers.ofString());
            System.out.println(theResponse.body());
        } catch(Throwable theErr) {
            kLogger.error("Error making rquest", theErr);
        }
    }
}
