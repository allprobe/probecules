package Utils;

import org.apache.log4j.Logger;

import GlobalConstants.GlobalConfig;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.LogManager;

public class Logit {
	// static Logger log = Logger.getLogger("");
	// static Logger log = LogManager.getLogger("syslog-debug");
	static Logger log = LogManager.getRootLogger();
	private static boolean isDebug = true;
	static boolean isInfo = false;
	static boolean isWarn = false;
	static boolean isCheck = true;

	// public Logit(String className)
	// {
	// log = Logger.getLogger(className);
	// }
	private static String formatMessage(String message) {
		return GlobalConfig.getDataCenterID() + "-" + GlobalConfig.getThisHostToken() + ": " + message;
	}

	public static void LogDebug(String message) {
		if (log.isDebugEnabled() && isDebug())
			log.debug(formatMessage(message));
	}

	public static void LogCheck(String message) {
		if (isCheck)
			log.info(formatMessage(message));
	}

	public static void LogInfo(String message) {
		// if (log.isInfoEnabled())
		// log.info(message);
//		if (log.isInfoEnabled() && isInfo)
			log.info(formatMessage(message));
	}

	// extraInfo - Class name + Function name
	public static void LogError(String extraInfo, String message) {
		log.error(formatMessage(extraInfo + " -- " + message));
	}

	// extraInfo - Class name + Function name
	public static void LogError(String extraInfo, String message, Exception e) {
		if (e == null) {
			log.error(formatMessage(message));
			return;
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);

		String trace = sw.toString();

		log.error(formatMessage(message) + " - TRACE: " + trace);

	}

	// extraInfo - Class name + Function name
	public static void LogFatal(String extraInfo, String message, Exception e) {

		if (e == null) {
			log.fatal(formatMessage(message));
			return;
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);

		String trace = sw.toString();

		log.fatal(formatMessage(message) + " - TRACE: " + trace);
	}

	public static void LogWarn(String message) {
		if (isWarn)
			log.warn(formatMessage(message));
	}

	public static void LogTrace(String message) {
		log.trace(formatMessage(message));
	}

	public static boolean isDebug() {
		return isDebug;
	}

}
