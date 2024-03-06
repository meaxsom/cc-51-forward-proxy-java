package com.gruecorner.forwardproxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

import org.junit.Before;
import org.junit.Test;

public class HttpProxyRequestTest {
    private static final String kMinimalTestString = "GET http://example.org/ HTTP/1.1";
    private static final String kConnectTestString = "CONNECT httpbin.org:443 HTTP/1.1";

    private static final String kBadTestString = "GETsdfsdff/ HTTP/1.1";
    private static final String kHeadersTestString = "Connection: keep-alive";

    // initialize logging
    @Before
    public void setUp() throws Exception {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.DEBUG);
    }

    @Test
    public void testNullListHttpProxyRequest() {
        HttpProxyRequest theRequest = new HttpProxyRequest(null);
        assertEquals(theRequest.getMethod(), null);
        assertEquals(theRequest.getProtocol(), null);
        assertEquals(theRequest.getRequst(), null);

        Map<String, String> theHeaders = theRequest.getHeaders();
        assertNotNull(theHeaders);

        assertEquals(theHeaders.size(), 0);
    }

    @Test
    public void testInvalidListHttpProxyRequest() {
        List<String> theMinimalList = new ArrayList<String>();
        theMinimalList.add(kBadTestString);
        HttpProxyRequest theRequest = new HttpProxyRequest(theMinimalList);
        assertEquals(theRequest.getMethod(), null);
        assertEquals(theRequest.getProtocol(), null);
        assertEquals(theRequest.getRequst(), null);

        Map<String, String> theHeaders = theRequest.getHeaders();
        assertNotNull(theHeaders);

        assertEquals(theHeaders.size(), 0);
    }

    @Test
    public void testEmptyListHttpProxyRequest() {
        List<String> theEmptyList = new ArrayList<String>();
        HttpProxyRequest theRequest = new HttpProxyRequest(theEmptyList);
        assertEquals(theRequest.getMethod(), null);
        assertEquals(theRequest.getProtocol(), null);
        assertEquals(theRequest.getRequst(), null);

        Map<String, String> theHeaders = theRequest.getHeaders();
        assertNotNull(theHeaders);

        assertEquals(theHeaders.size(), 0);
    }

    @Test
    public void testMinimalListHttpProxyRequest() {
        List<String> theMinimalList = new ArrayList<String>();
        theMinimalList.add(kMinimalTestString);
        HttpProxyRequest theRequest = new HttpProxyRequest(theMinimalList);
        assertEquals(theRequest.getMethod(), "GET");
        assertEquals(theRequest.getProtocol(), "HTTP/1.1");
        assertEquals(theRequest.getRequst(), "http://example.org/");

        Map<String, String> theHeaders = theRequest.getHeaders();
        assertNotNull(theHeaders);

        assertEquals(theHeaders.size(), 0);
    }

    @Test
    public void testHeadersListHttpProxyRequest() {
        List<String> theMinimalList = new ArrayList<String>();
        theMinimalList.add(kMinimalTestString);
        theMinimalList.add(kHeadersTestString);

        HttpProxyRequest theRequest = new HttpProxyRequest(theMinimalList);
        assertEquals(theRequest.getMethod(), "GET");
        assertEquals(theRequest.getProtocol(), "HTTP/1.1");
        assertEquals(theRequest.getRequst(), "http://example.org/");

        Map<String, String> theHeaders = theRequest.getHeaders();
        assertNotNull(theHeaders);

        assertEquals(theHeaders.size(), 1);
        assertEquals(theHeaders.get("Connection"), "keep-alive");
        assertNull(theHeaders.get("Accept-Language"));
    }

    @Test
    public void testConnectHttpProxyRequest() {
        List<String> theMinimalList = new ArrayList<String>();
        theMinimalList.add(kConnectTestString);
        theMinimalList.add(kHeadersTestString);

        HttpProxyRequest theRequest = new HttpProxyRequest(theMinimalList);
        assertEquals(theRequest.getMethod(), "CONNECT");
        assertEquals(theRequest.getProtocol(), "HTTP/1.1");
        assertEquals(theRequest.getRequst(), "https://httpbin.org:443");

        Map<String, String> theHeaders = theRequest.getHeaders();
        assertNotNull(theHeaders);

        assertEquals(theHeaders.size(), 1);
        assertEquals(theHeaders.get("Connection"), "keep-alive");
        assertNull(theHeaders.get("Accept-Language"));
    }

}
