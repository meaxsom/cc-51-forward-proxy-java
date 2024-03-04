package com.gruecorner.forwardproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProxyHandler {
	private final static Logger kLogger =	LogManager.getLogger(ProxyHandler.class.getName());

    public ProxyHandler() {
    }

    public void handleConnection(final InputStream inputStream, final OutputStream outputStream, InetAddress inAddress, int inPort) throws IOException {
        Optional<HttpProxyRequest> theRequest = HttpRequestDecoder.decode(inputStream);

        if (theRequest.isPresent()) {
            HttpProxyRequest theProxyRequest = theRequest.get();

            // pass along the client address and port to the request
            theProxyRequest.setAddress(inAddress);
            theProxyRequest.setPort(inPort);
            System.out.println(theProxyRequest.getAddress().getHostAddress());

            Map<String, String> theHeaders = theProxyRequest.getHeaders();
            for (String theKey : theHeaders.keySet())
                System.out.println(theKey + ": " + theHeaders.get(theKey));
        } else
            kLogger.error("Could not decode request");

        outputStream.close();
    }
}
