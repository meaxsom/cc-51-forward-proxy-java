package com.gruecorner.forwardproxy;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpRequestDecoder {
	private final static Logger kLogger =	LogManager.getLogger(HttpRequestDecoder.class.getName());

    public static Optional<HttpProxyRequest> decode(final InputStream inInputStream) {
        Optional<HttpProxyRequest> result = Optional.empty();

        Optional<List<String>> theRequestList = readMessage(inInputStream);
        if (theRequestList.isPresent())
            result = buildRequest(theRequestList.get());

        return result;
    }
    private static Optional<List<String>> readMessage(final InputStream inInputStream) {
        Optional<List<String>> result = Optional.empty();
        try {
            if (inInputStream.available() > 0) {
                // read all the data into a buffer
                final char[] theBuffer = new char[inInputStream.available()];
                final InputStreamReader theReader = new InputStreamReader(inInputStream);
                
                @SuppressWarnings("unused")
                final int theReadData = theReader.read(theBuffer);

                // put all the lines in a List of strings
                List<String> theMessages = new ArrayList<>();
                Scanner theScanner = new Scanner(new String(theBuffer));
                while (theScanner.hasNextLine()) {
                    String theLine = theScanner.nextLine();
                    theMessages.add(theLine);
                }
                theScanner.close();

            result = Optional.of(theMessages);
            }
        } catch(Throwable theErr) {
            kLogger.error("Can't parse the request", theErr);
        }

        return result;
    }

    private static Optional<HttpProxyRequest> buildRequest(List<String> inMessages) {
        Optional<HttpProxyRequest> result = Optional.empty();

        if (!inMessages.isEmpty())
            result = Optional.of(new HttpProxyRequest(inMessages));
        else
            kLogger.error("Request was empty!");
            
        return result;

    }
}
