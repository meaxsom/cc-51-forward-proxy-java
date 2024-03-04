package com.gruecorner.forwardproxy;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;

public class HttpProxyRequest {
	private final static Logger kLogger =	LogManager.getLogger(HttpProxyRequest.class.getName());

    private String m_method = null;
    private String m_requst = null;
    private String m_protocol = null;

    private int m_port = 0;
    private InetAddress m_address = null;

    private Map<String, String> m_headers = new HashMap<String, String>();

    public HttpProxyRequest(List<String> inRequest) {
        int theRequestSize = inRequest.size();

        if (theRequestSize > 0) {
            String[] theLineParts = inRequest.get(0).split("\\s+");
            if (theLineParts.length == 3) {
                m_method = theLineParts[0].trim();
                m_requst = theLineParts[1].trim();
                m_protocol = theLineParts[2].trim();
            
                if (theRequestSize > 1) {
                    for (String theLine : inRequest.subList(1, inRequest.size())) {
                        String[] theHeaderLine = theLine.split("\\:\\s+");
                        if (theHeaderLine.length == 2)
                            m_headers.put(theHeaderLine[0], theHeaderLine[1]);
                    }
                } else
                    kLogger.info("Seem's odd there are no headers");
            } else
                kLogger.error("Request is invalid");
        } else
            kLogger.error("Request is empty!");
    }

    public String getMethod() {
        return m_method;
    }

    public void setMethod(String inMethod) {
        m_method = inMethod;
    }

    public String getRequst() {
        return m_requst;
    }

    public void setRequst(String inRequest) {
        m_requst = inRequest;
    }

    public String getProtocol() {
        return m_protocol;
    }

    public void setProtocol(String inProtocol) {
        m_protocol = inProtocol;
    }

    public Map<String, String> getHeaders() {
        return m_headers;
    }

    public void setHeaders(Map<String, String> inHeaders)  {
        m_headers = inHeaders;
    }

    public int getPort() {
        return m_port;
    }

    public void setPort(int inPort) {
        m_port = inPort;
    }

    public InetAddress getAddress() {
        return m_address;
   }

    public void setAddress(InetAddress inAddress) {
        m_address = inAddress;
    }
}
