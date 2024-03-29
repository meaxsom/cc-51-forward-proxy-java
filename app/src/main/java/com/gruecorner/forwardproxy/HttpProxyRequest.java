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
        if (inRequest != null)
            {
            int theRequestSize = inRequest.size();

            if (theRequestSize > 0) {
                String[] theLineParts = inRequest.get(0).split("\\s+");
                if (theLineParts.length == 3) {
                        setMethod(theLineParts[0].trim());
                        setRequst(theLineParts[1].trim());
                        setProtocol(theLineParts[2].trim());
                
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
        } else
            kLogger.error("Request is null!");
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
        String theRequest = inRequest;
        kLogger.debug("Original Request: " + theRequest);

        // adjust the request to contain a protocol if one is not there
        if (inRequest != null) {
            if (!inRequest.startsWith("http://") && !inRequest.startsWith("https://")) {
                if (inRequest.contains("443"))
                    theRequest = "https://" + inRequest;
                else
                    theRequest = "http://" + inRequest;
            }

        kLogger.debug("Adjusted Request: " + theRequest);
        m_requst = theRequest;
        }
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

    public InetAddress getInetAddress() {
        return m_address;
   }

   public String getInetAddressString() {
    String result = "";
    if (m_address != null)
        result = m_address.getHostAddress();
    return result;
    }

    public void setInetAddress(InetAddress inAddress) {
        m_address = inAddress;
    }
}
