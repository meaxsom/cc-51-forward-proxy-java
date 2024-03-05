package com.gruecorner.forwardproxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gruecorner.forwardproxy.utils.CommandLineHelper;

class FwdProxy {
	private final static Logger kLogger =	LogManager.getLogger(FwdProxy.class.getName());
    
    private static FwdProxy s_proxyServer  = null;

    private final ServerSocket m_socket;
    private final Executor m_threadPool;
    private ProxyHandler m_proxyHandler = null;

    
    // innitialize a thread pool for handling the connection and start the server on the desired port
    public FwdProxy(int inPort, int inThreadPoolSize) throws IOException {
        m_threadPool = Executors.newFixedThreadPool(inThreadPoolSize);
        m_socket = new ServerSocket(inPort);
        kLogger.info("Started Server on Port " + inPort + " with " + inThreadPoolSize + " threads...");
    }

    public void start() throws IOException {
        m_proxyHandler = new ProxyHandler();

        // wait on a connection and then hand it off to the proxy handler
        while (true) {
            Socket theConnection = m_socket.accept();
            handleConnection(theConnection);
        }
    }

    // clean up the server
    public void stop() {
        try {
            m_socket.close();
        }
        catch (IOException theErr) {
            kLogger.error("Trouble shutting down server", theErr);
        }
    }

    private void handleConnection(Socket inClientConnection) {
        // create a runnable so each request runs in it's own thread
        Runnable theRequestRunner = () -> {
            try {
                m_proxyHandler.handleConnection(inClientConnection.getInputStream(), inClientConnection.getOutputStream(), inClientConnection.getInetAddress(), inClientConnection.getLocalPort());
            } catch(Throwable theErr) {
                kLogger.error("Some Problem", theErr);
            }
        };

        // run it!
        m_threadPool.execute(theRequestRunner);
    }

    public static void main(String inArgs[]) {

        // install shutdown hook to clean up the socket
        Runtime.getRuntime().addShutdownHook(new Thread() { 
            public void run() {
                if (s_proxyServer != null) {
                    s_proxyServer.stop();
                    kLogger.info("Shutting down server");
                }
            } 
        }); 
        
        CommandLineHelper.processArguments("FwdProxy", inArgs);
        if (CommandLineHelper.isHelp())
            System.out.println(CommandLineHelper.getHelp());
        else { // start the server
            try {
                s_proxyServer = new FwdProxy(CommandLineHelper.getPort(), CommandLineHelper.getThreadPoolSize());
                s_proxyServer.start();
            } catch (IOException theErr) {
                kLogger.error("Can't start proxy server!", theErr);
            }
        }
    }
}