package com.gruecorner.forwardproxy;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.junit.Before;
import org.junit.Test;

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

        ProxyHandler theHandler = new ProxyHandler();
        theHandler.proxyRequest(theRequest);
    }
}
