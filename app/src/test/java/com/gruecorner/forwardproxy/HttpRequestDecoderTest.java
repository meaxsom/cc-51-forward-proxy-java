package com.gruecorner.forwardproxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

import org.junit.Before;
import org.junit.Test;

public class HttpRequestDecoderTest {
    private static final String kMinimalTestString = "GET / HTTP/1.1";
    private static final String kHeadersTestString = "GET / HTTP/1.1\nConnection: keep-alive";
    
    @Before
    public void setUp() throws Exception {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.DEBUG);
    }

    @Test
    public void testEmptyInputHttpRequestDecoder() {
        Optional<HttpProxyRequest> theRequest = HttpRequestDecoder.decode(null);
        assertFalse(theRequest.isPresent());
    }

    @Test
    public void testMinimalInputHttpRequestDecoder() {
        InputStream theInputStream = new ByteArrayInputStream(kMinimalTestString.getBytes());

        Optional<HttpProxyRequest> theOptionalRequest = HttpRequestDecoder.decode(theInputStream);
        assertTrue(theOptionalRequest.isPresent());

        HttpProxyRequest theHttpProxyRequest = theOptionalRequest.get();
        assertEquals(theHttpProxyRequest.getMethod(), "GET");
        assertEquals(theHttpProxyRequest.getProtocol(), "HTTP/1.1");
        assertEquals(theHttpProxyRequest.getRequst(), "/");

        Map<String, String> theHeaders = theHttpProxyRequest.getHeaders();
        assertNotNull(theHeaders);

        assertEquals(theHeaders.size(), 0);
    }

    @Test
    public void testHeadersInputHttpRequestDecoder() {
        InputStream theInputStream = new ByteArrayInputStream(kHeadersTestString.getBytes());

        Optional<HttpProxyRequest> theOptionalRequest = HttpRequestDecoder.decode(theInputStream);
        assertTrue(theOptionalRequest.isPresent());

        HttpProxyRequest theHttpProxyRequest = theOptionalRequest.get();
        assertEquals(theHttpProxyRequest.getMethod(), "GET");
        assertEquals(theHttpProxyRequest.getProtocol(), "HTTP/1.1");
        assertEquals(theHttpProxyRequest.getRequst(), "/");

        Map<String, String> theHeaders = theHttpProxyRequest.getHeaders();
        assertNotNull(theHeaders);

        assertEquals(theHeaders.size(), 1);
        assertEquals(theHeaders.get("Connection"), "keep-alive");
        assertNull(theHeaders.get("Accept-Language"));

    }
}
