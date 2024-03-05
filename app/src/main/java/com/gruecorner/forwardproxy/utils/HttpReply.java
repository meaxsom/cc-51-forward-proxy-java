package com.gruecorner.forwardproxy.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpReply {
    private ResponseCode m_responseCode = ResponseCode.OK;
    private Protocol m_protocol = Protocol.HTTP11;
    private String m_body = "";
    private Map<String, String> m_headers = new HashMap<String, String>();

    public HttpReply(Builder inBuilder) {
        this.m_responseCode = inBuilder.m_responseCode;
        this.m_protocol = inBuilder.m_protocol;

        String theBody = inBuilder.m_body;
        if (theBody != null)
            this.m_body = new String(inBuilder.m_body.getBytes(), StandardCharsets.UTF_8);
        else
            this.m_body = "";

        m_headers = inBuilder.m_headers;
    }

    public void send(OutputStream inOutputStream) throws IOException{
        BufferedWriter theWriter = new BufferedWriter(new OutputStreamWriter(inOutputStream));
        // send the initial response
        theWriter.write(m_protocol.label + " " + m_responseCode.code + " " + m_responseCode.label + "\n");

        // write all the headers
        for (String theKey : m_headers.keySet())
            theWriter.write(theKey + ": " + m_headers.get(theKey) + "\n");
        
        // signal that we're at the end of the front matter and to expect the body
        theWriter.write("\n");

        // write the body
        theWriter.write(m_body);
        theWriter.flush();
    }
    
    public static class Builder {
        private ResponseCode m_responseCode = ResponseCode.OK;
        private Protocol m_protocol = Protocol.HTTP11;
        private String m_body = "";
        private Map<String, String> m_headers = new HashMap<String, String>();

        public static Builder newInstance() {
            return new Builder();
        }

        private Builder() {
        }

        public Builder setResponseCode(ResponseCode inResponseCode) {
            m_responseCode = inResponseCode;
            return this;
        }

        public Builder setProtocol(Protocol inProtocol) {
            m_protocol = inProtocol;
            return this;
        }

        public Builder setBody(String inBody) {
            m_body = inBody;
            return this;
        }

        public Builder addHeader(String inKey, String inValue) {
            m_headers.put(inKey, inValue);
            return this;
        }

        public Builder setHeaders(Map<String, String> inHeaders) {
            if (inHeaders != null)
                 m_headers.putAll(inHeaders);
            return this;
        }

        public HttpReply build() {
            return new HttpReply(this);
        }
    }

    public enum Protocol {
        HTTP10("HTTP/1.0"),
        HTTP11("HTTP/1.1"),
        HTTP20("HTTP/2.0");
    
        public final String label;
    
        private Protocol(String inLabel) {
            this.label = inLabel;
        }
    }
    public enum ResponseCode {
        OK(200, "OK"),
        BadRequest(400, "Bad Request"),
        NotFound(404, "Not Found");
        
        public final int code;
        public final String label;

        private ResponseCode(int inCode, String inLabel) {
            this.code = inCode;
            this.label = inLabel;
        }
    }
}
