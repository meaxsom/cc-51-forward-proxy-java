package com.gruecorner.forwardproxy.utils;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StreamClient extends Thread {
	private final static Logger kLogger =	LogManager.getLogger(StreamClient.class.getName());

    private final static int    kBufferSize = 1024;
    private final static String kDefaulStreamName = "streamer";

    private String m_name;
    private InputStream m_InputStream;
    private OutputStream m_OutputStream;

    public StreamClient(String inName, InputStream inInputStream, OutputStream inOutputStream) {
        super(inName);
        m_name = inName != null ? inName : kDefaulStreamName;
        m_InputStream = inInputStream;
        m_OutputStream = inOutputStream;
    }

    public void run() {
        byte[] theBuffer = new byte[kBufferSize];

        int theSize = 0;
        try {
            while ((theSize = m_InputStream.read(theBuffer)) != -1) {
                m_OutputStream.write(theBuffer, 0, theSize);
            }
            kLogger.debug(m_name + ": flushing");
            
            m_OutputStream.flush();            
        } catch (Throwable theErr) {
            kLogger.error("Streaming problem on: "+ m_name, theErr);
        }
    }
}
