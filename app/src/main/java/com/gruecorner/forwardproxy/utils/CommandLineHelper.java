package com.gruecorner.forwardproxy.utils;

import gnu.getopt.Getopt;

import java.io.File;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

public class CommandLineHelper {
	private final static Logger kLogger =	LogManager.getLogger(CommandLineHelper.class.getName());

	private final static String kHelpString = "-l {log4j-config} -  path to log4j config file\n"
		+ "-p {port} forward proxy port number\n"
		+ "-h - print help.. all other options are ignored and program terminates\n";

    private static String	kDefaultOptions	= 	":hp:l:";
	private static int		kDefaultPort	=	8989;

	private static int		s_port 		= kDefaultPort;
	private static String	s_log4JConf	= null;
	private static boolean	s_help 		= false;

	public static void processArguments(String inProcessName, String inArgs[]) {
		CommandLineHelper.processArguments(inProcessName, inArgs, kDefaultOptions);
	}
	public static void processArguments(String inProcessName, String inArgs[], String inOptions) {
		Getopt theOpts = new Getopt(inProcessName, inArgs, inOptions);

		int theOption;
		while ((theOption = theOpts.getopt()) != -1) {
			switch (theOption) {
				case 'p':
					setPort(theOpts.getOptarg());
					break;
			
				case 'h':
					setHelp(true);
					break;

				case 'l':
					setLog4JConf(theOpts.getOptarg());
					break;
            }
        }

		if (s_log4JConf != null) {
			// replaces DOMConfigurator.configure(s_log4JConf);
			File file = new File(s_log4JConf);
			LoggerContext context = (LoggerContext) LogManager.getContext(false);
			context.setConfigLocation(file.toURI());
			
			kLogger.info("Log4J Configured from: " + s_log4JConf);
		}
		else {
		    Configurator.initialize(new DefaultConfiguration());
		    Configurator.setRootLevel(Level.DEBUG);
		}
    }
	public static int getPort() {
		return s_port;
	}

	public static void setPort(int inPort) {
		CommandLineHelper.s_port = inPort;
	}

	public static void setPort(String inPort) {
		try {
			CommandLineHelper.setPort(Integer.parseInt(inPort));
		} catch (Throwable theErr) {
			CommandLineHelper.s_port = kDefaultPort;
		}
	}

	public static String getLog4JConf() {
		return s_log4JConf;
	}

	public static void setLog4JConf(String inLog4JConf) {
		CommandLineHelper.s_log4JConf = inLog4JConf;
	}

	public static String getHelp()
		{
		return kHelpString;
		}

	public static void setHelp(boolean inHelp)
		{
		CommandLineHelper.s_help = inHelp;
		}
		
	public static boolean isHelp()
		{
		return s_help;
		}

}
