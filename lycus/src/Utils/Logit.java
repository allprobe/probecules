package Utils;

import org.apache.log4j.Logger;

import GlobalConstants.GlobalConfig;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.LogManager;

public class Logit {
	// static Logger log = Logger.getLogger("");
//	static Logger log = LogManager.getLogger("syslog-debug");
	static Logger log = LogManager.getRootLogger();
	static boolean isDebug = false;
	static boolean isInfo = false;
	static boolean isWarn = false;
	static boolean isCheck = true;
	// public Logit(String className)
	// {
	// log = Logger.getLogger(className);
	// }

	public static void LogDebug(String message) {
		if (log.isDebugEnabled() && isDebug)
			log.debug(message);
	}

	public static void LogCheck(String message) {
		if (isCheck)
			log.info(message);
	}

	public static void LogInfo(String message) {
		log.info(message);
	}

	// extraInfo - Class name + Function name
	public static void LogError(String extraInfo, String message) {
		log.error(
				GlobalConstants.GlobalConfig.getDataCenterID() + "-" + GlobalConfig.getThisHostToken() + " " + message);
	}

	// extraInfo - Class name + Function name
	public static void LogError(String extraInfo, String message, Exception e) {
		if (e == null) {
			log.error(GlobalConstants.GlobalConfig.getDataCenterID() + "-" + GlobalConfig.getThisHostToken() + " "
					+ message);
			return;
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);

		String trace = sw.toString();

		log.error(GlobalConstants.GlobalConfig.getDataCenterID() + "-" + GlobalConfig.getThisHostToken() + " " + message
				+ " - TRACE: " + trace);

	}

	// extraInfo - Class name + Function name
	public static void LogFatal(String extraInfo, String message, Exception e) {

		if (e == null) {
			log.fatal(message);
			return;
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);

		String trace = sw.toString();

		log.fatal(message + " - TRACE: " + trace);
	}

	public static void LogWarn(String message) {
		if (isWarn)
			log.warn(message);
	}

	public static void LogTrace(String message) {
		log.trace(message);
	}
}
