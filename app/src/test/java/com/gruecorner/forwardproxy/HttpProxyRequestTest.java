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
    // initialize logging
    @Before
    public void setUp() throws Exception {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.DEBUG);
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
        theMinimalList.add("GET / HTTP/1.1");
        HttpProxyRequest theRequest = new HttpProxyRequest(theMinimalList);
        assertEquals(theRequest.getMethod(), "GET");
        assertEquals(theRequest.getProtocol(), "HTTP/1.1");
        assertEquals(theRequest.getRequst(), "/");

        Map<String, String> theHeaders = theRequest.getHeaders();
        assertNotNull(theHeaders);

        assertEquals(theHeaders.size(), 0);
    }

    @Test
    public void testHeadersListHttpProxyRequest() {
        List<String> theMinimalList = new ArrayList<String>();
        theMinimalList.add("GET / HTTP/1.1");
        theMinimalList.add("Connection: keep-alive");

        HttpProxyRequest theRequest = new HttpProxyRequest(theMinimalList);
        assertEquals(theRequest.getMethod(), "GET");
        assertEquals(theRequest.getProtocol(), "HTTP/1.1");
        assertEquals(theRequest.getRequst(), "/");

        Map<String, String> theHeaders = theRequest.getHeaders();
        assertNotNull(theHeaders);

        assertEquals(theHeaders.size(), 1);
        assertEquals(theHeaders.get("Connection"), "keep-alive");
        assertNull(theHeaders.get("Accept-Language"));
    }
}
