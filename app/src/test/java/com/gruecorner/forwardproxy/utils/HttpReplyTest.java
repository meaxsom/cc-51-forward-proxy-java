package com.gruecorner.forwardproxy.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.junit.Before;
import org.junit.Test;

import com.gruecorner.forwardproxy.utils.HttpReply.Protocol;
import com.gruecorner.forwardproxy.utils.HttpReply.ResponseCode;

public class HttpReplyTest {
    // initialize logging
    @Before
    public void setUp() throws Exception {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.DEBUG);
    }

    @Test
    public void testHttpReplySend() {
        HttpReply theReply = HttpReply.Builder.newInstance()
            .setProtocol(Protocol.HTTP11)
            .setResponseCode(ResponseCode.OK)
            .setBody("Hello World")
            .addHeader("Content-Type", "text/plain")
            .build();

        ByteArrayOutputStream theStream = new ByteArrayOutputStream();

        try {
            theReply.send(theStream);
            theStream.close();

            String theOutput = new String(theStream.toByteArray());
            assertNotNull(theOutput);
            assertTrue(theOutput.length() > 0);
            assertTrue(theOutput.contains("Hello World"));
        } catch (IOException e) {
            e.printStackTrace(System.err);
            fail();
        }   
    }

    @Test
    public void testHttpReplyContentLength() {
        HttpReply theReply = HttpReply.Builder.newInstance()
            .setProtocol(Protocol.HTTP11)
            .setResponseCode(ResponseCode.OK)
            .setBody("Hello World")
            .addHeader("Content-Type", "text/plain")
            .build();

        ByteArrayOutputStream theStream = new ByteArrayOutputStream();

        try {
            theReply.send(theStream);
            theStream.close();

            String theOutput = new String(theStream.toByteArray());
            assertNotNull(theOutput);
            assertTrue(theOutput.length() > 0);
            assertTrue(theOutput.contains("Hello World"));
            assertTrue(theOutput.contains("Content-Length"));
        } catch (IOException e) {
            e.printStackTrace(System.err);
            fail();
        }
    }
    @Test
    public void testHttpReplyContentLengthFilter() {
        HttpReply theReply = HttpReply.Builder.newInstance()
            .setProtocol(Protocol.HTTP11)
            .setResponseCode(ResponseCode.OK)
            .setBody("Hello World")
            .addHeader("Content-Type", "text/plain")
            .addHeader("content-length", String.valueOf(0)) // input lower case
            .addHeader("Content-Length", String.valueOf(30000)) // input upper case w/crazy value
            .build();

        ByteArrayOutputStream theStream = new ByteArrayOutputStream();

        try {
            theReply.send(theStream);
            theStream.close();

            String theOutput = new String(theStream.toByteArray());
            assertNotNull(theOutput);
            assertTrue(theOutput.length() > 0);
            assertTrue(theOutput.contains("Hello World"));

            // should have a content lenght but not 3K
            assertTrue(theOutput.contains("Content-Length"));   
            assertTrue(!theOutput.contains("30000"));
        } catch (IOException e) {
            e.printStackTrace(System.err);
            fail();
        }
    }
}   