package com.gruecorner.forwardproxy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.junit.Before;
import org.junit.Test;

import com.gruecorner.forwardproxy.utils.HttpReply;

public class ProxyHandlerTest {
    private final static String kMinimalProxyTestString = "GET http://httpbin.org/ip HTTP/1.1";

    // initialize logging
    @Before
    public void setUp() throws Exception {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.DEBUG);
    }

    @Test
    public void testSimpleProxyRequest() {
        // build up a proxy test request
        List<String> theMinimalList = new ArrayList<String>();
        theMinimalList.add(kMinimalProxyTestString);
        HttpProxyRequest theRequest = new HttpProxyRequest(theMinimalList);

        ProxyHandler theHandler = new ProxyHandler(null, null);
        theHandler.proxyRequest(theRequest);

        // TODO: Fix the remainder of this test
    }

    @Test
    public void testHandleInvalidResponse() {
        ByteArrayOutputStream theStream = new ByteArrayOutputStream();

        ProxyHandler theHandler = new ProxyHandler(null, null);
        try {
            List<String> theMinimalList = new ArrayList<String>();
            theMinimalList.add(kMinimalProxyTestString);
            HttpProxyRequest theRequest = new HttpProxyRequest(theMinimalList);

            theHandler.handleInvalidResponse(theRequest, theStream, HttpReply.ResponseCode.BadRequest, "Bad Stuff Happend!");
            theStream.close();

            String theOutput = new String(theStream.toByteArray());
            assertNotNull(theOutput);
            assertTrue(theOutput.length() > 0);
            assertTrue(theOutput.contains("Bad Request"));

        } catch (IOException theErr) {
            theErr.printStackTrace(System.err);
            fail();
        }
    }

    @Test
    public void testHandlerIsBanned() {
        List<String> theMinimalList = new ArrayList<String>();
        theMinimalList.add(kMinimalProxyTestString);
        HttpProxyRequest theRequest = new HttpProxyRequest(theMinimalList);


        List<String> theBannedHosts = new ArrayList<String>();
        theBannedHosts.add("facebook.com");

        List<String> theBannedWords = new ArrayList<String>();
        theBannedWords.add("facebook");

        ProxyHandler theHandler = new ProxyHandler(theBannedHosts, theBannedWords);
        boolean isAllowed = theHandler.isHostAllowed(theRequest, theBannedHosts, theBannedWords);
        assertTrue(isAllowed);

        theRequest.setRequst("http://WWW.faceBooK.coM");
        isAllowed = theHandler.isHostAllowed(theRequest, theBannedHosts, theBannedWords);
        assertFalse(isAllowed);

        isAllowed = theHandler.isAllowed(theRequest.getRequst(), theBannedWords);
        assertFalse(isAllowed);
    }
}
