package com.gruecorner.forwardproxy.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

public class HttpReply {
    private final static String kContentLengthHeaderKeyStd = "Content-Length";
    private final static String kContentLengthHeaderKeyAlt = "content-length";

    private static final String[] kRestrictedHeadersNames = new String[] {kContentLengthHeaderKeyStd, kContentLengthHeaderKeyAlt};
    private static final Set<String> kRestrictedHeaders = new HashSet<>(Arrays.asList(kRestrictedHeadersNames));

    private ResponseCode m_responseCode = ResponseCode.OK;
    private Protocol m_protocol = Protocol.HTTP11;
    private String m_body = "";
    private Map<String, String> m_headers = new HashMap<String, String>();


    public HttpReply(Builder inBuilder) {
        this.m_responseCode = inBuilder.m_responseCode;
        this.m_protocol = inBuilder.m_protocol;

        // assign headers and add in our content length
        m_headers = inBuilder.m_headers;

        // only apply body and content-length header if this isn't a tunnel  
        if (this.m_responseCode != ResponseCode.ProxyConnectionOK) {
            this.m_body = inBuilder.m_body == null ? "" : new String(inBuilder.m_body.getBytes(), StandardCharsets.UTF_8);
            m_headers.put(kContentLengthHeaderKeyStd, String.valueOf(m_body.length()));
        }
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
            // filter out any restricted headers
            if (!kRestrictedHeaders.contains(inKey))
                m_headers.put(inKey, inValue);
            return this;
        }

        public Builder setHeaders(Map<String, String> inHeaders) {
            // if we use putAll, we'll miss the filtering
            for (String theKey : inHeaders.keySet())
                addHeader(theKey, inHeaders.get(theKey));
        
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
        ProxyConnectionOK(200, "Connection established"),
        BadRequest(400, "Bad Request"),
        Forbidden(403, "Forbidden"),
        NotFound(404, "Not Found");
        
        public final int code;
        public final String label;

        private ResponseCode(int inCode, String inLabel) {
            this.code = inCode;
            this.label = inLabel;
        }
    }
}
