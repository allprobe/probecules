package Utils;

import org.apache.logging.log4j.*;

import GlobalConstants.GlobalConfig;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Logit {
	// static Logger log = Logger.getLogger("");
	// static Logger log = LogManager.getLogger("syslog-debug");
	private static Logger log = LogManager.getRootLogger();
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
		if (getLog().isDebugEnabled() && isDebug())
			getLog().debug(formatMessage(message));
	}

	public static void LogCheck(String message) {
		if (isCheck)
			getLog().info(formatMessage(message));
	}

	public static void LogInfo(String message) {
		// if (log.isInfoEnabled())
		// log.info(message);
		// if (log.isInfoEnabled() && isInfo)
		getLog().info(formatMessage(message));
	}

	// extraInfo - Class name + Function name
	public static void LogError(String extraInfo, String message) {
		getLog().error(formatMessage(extraInfo + " -- " + message));
	}

	// extraInfo - Class name + Function name
	public static void LogError(String extraInfo, String message, Exception e) {
		if (e == null) {
			getLog().error(formatMessage(message));
			return;
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);

		String trace = sw.toString();

		getLog().error(formatMessage(message) + " - TRACE: " + trace);

	}

	// extraInfo - Class name + Function name
	public static void LogFatal(String extraInfo, String message, Exception e) {

		if (e == null) {
			getLog().fatal(formatMessage(message));
			return;
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);

		String trace = sw.toString();

		getLog().fatal(formatMessage(message) + " - TRACE: " + trace);
	}

	public static void LogWarn(String message) {
		if (isWarn)
			getLog().warn(formatMessage(message));
	}

	public static void LogTrace(String message) {
		getLog().trace(formatMessage(message));
	}

	public static boolean isDebug() {
		return isDebug;
	}

	public static Logger getLog() {
		return log;
	}


}
