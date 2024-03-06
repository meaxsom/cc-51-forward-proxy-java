package com.gruecorner.forwardproxy.utils;

import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

public class CommandLineHelper {
	private final static Logger kLogger =	LogManager.getLogger(CommandLineHelper.class.getName());

    private static String	kDefaultOptions			= 	":hp:l:t:b:";
	private static int		kDefaultPort			=	8989;
	private static int		kDefaultThreadPoolSize	=	100;

	private final static String kHelpString = "-l {log4j-config} -  path to log4j config file\n"
		+ "-p {port} optional forward proxy port numberm default is " + kDefaultPort + "\n"
		+ "-t {thread-count} optional number of threads to use; defaults to " + kDefaultThreadPoolSize + "\n"
		+ "-b {banned-hosts} optional text file of banned hosts, 1 per line"
		+ "-h prints this help.. all other options are ignored and program terminates\n";


	private static int			s_port 				= kDefaultPort;
	private static int			s_threadPoolSize	= kDefaultThreadPoolSize;
	private static String		s_log4JConf			= null;
	private static String		s_bannedHostsFile	= null;
	private static boolean		s_help 				= false;
	private static Set<String>	s_bannedHosts		= new HashSet<String>();

	public static void processArguments(String inProcessName, String inArgs[]) {
		CommandLineHelper.processArguments(inProcessName, inArgs, kDefaultOptions);
	}
	public static void processArguments(String inProcessName, String inArgs[], String inOptions) {
		Getopt theOpts = new Getopt(inProcessName, inArgs, inOptions);

		int theOption;
		while ((theOption = theOpts.getopt()) != -1) {
			switch (theOption) {
				case 'b':
					setBannedHostsFile(theOpts.getOptarg());
					break;

				case 'h':
					setHelp(true);
					break;

				case 'l':
					setLog4JConf(theOpts.getOptarg());
					break;

				case 'p':
					setPort(theOpts.getOptarg());
					break;

				case 't':
					setThreadPoolSize(theOpts.getOptarg());
					break;
            }
        }

		// process logger here so we can use the logging system ASAP
		if (s_log4JConf != null) {
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
		s_port = inPort;
	}

	public static void setPort(String inPort) {
		try {
			CommandLineHelper.setPort(Integer.parseInt(inPort));
		} catch (Throwable theErr) {
			kLogger.error("Can't set port to: " + inPort + ". Using default of " + kDefaultPort, theErr);
			s_port = kDefaultPort;
		}
	}

	public static String getLog4JConf() {
		return s_log4JConf;
	}

	public static void setLog4JConf(String inLog4JConf) {
		s_log4JConf = inLog4JConf;
	}

	public static String getHelp() {
		return kHelpString;
	}

	public static void setHelp(boolean inHelp) {
		s_help = inHelp;
	}
		
	public static boolean isHelp() {
		return s_help;
	}

	public static int getThreadPoolSize() {
		return s_threadPoolSize;
	}

	public static void setThreadPoolSize(int inThreadPoolSize) {
		CommandLineHelper.s_threadPoolSize = inThreadPoolSize;
	}

	public static void setThreadPoolSize(String inThreadPoolSize) {
		try {
			setThreadPoolSize(Integer.valueOf(inThreadPoolSize));
		} catch (Throwable theErr) {
			kLogger.error("Can't set pool size to: " + inThreadPoolSize + ". Using default of " + kDefaultThreadPoolSize, theErr);
			setThreadPoolSize(kDefaultThreadPoolSize);
		}
	}
	public static Set<String> getBannedHostsFile() {
		return s_bannedHosts;
	}

	public static void setBannedHostsFile(String inBannedHostsFile) {
		s_bannedHostsFile = inBannedHostsFile;
	}

	public static Set<String> getBannedHosts() {
		return s_bannedHosts;
	}

	public static void configureBannedHosts(String inBannedHostFile) {
		File theFile = new File(inBannedHostFile);
		if (theFile.exists()) {
			try {
				BufferedReader theReader = new BufferedReader(new FileReader(theFile));

				String theLine = theReader.readLine();
				while (theLine != null) {
					s_bannedHosts.add(theLine);
					theLine = theReader.readLine();
				}
				theReader.close();

			} catch (Throwable theErr) {
				kLogger.error("Can't read banned host file", theErr);
			}
		} else
			kLogger.error(theFile.getAbsolutePath() + " does not exist. No site banning will be enforced");
	}
}
