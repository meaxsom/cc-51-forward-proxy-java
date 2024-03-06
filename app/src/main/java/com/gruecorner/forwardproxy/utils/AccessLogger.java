package com.gruecorner.forwardproxy.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gruecorner.forwardproxy.HttpProxyRequest;
import com.gruecorner.forwardproxy.utils.HttpReply.ResponseCode;

public class AccessLogger {
	private final static Logger kLogger =	LogManager.getLogger(AccessLogger.class.getName());

    // log any old string to the access log location
    public static final void log(String inLogMessage) {
        kLogger.info(inLogMessage);
    }

    // formulate a log message based on the client IP/Port and the response code
    public static final void log(HttpProxyRequest inRequest, ResponseCode inResponseCode) {
        String theAddress = inRequest != null ? inRequest.getInetAddressString() + ":" + inRequest.getPort() : "No Client Address Found";
        log(theAddress + " " + inResponseCode.code + " " + inResponseCode.label);
    }
}
