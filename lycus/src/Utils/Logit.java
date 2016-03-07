package Utils;

import org.apache.log4j.Logger;

public class Logit {
	static Logger log = Logger.getLogger(Logit.class.getName());

	public static void LogDebug(String message) {
		log.debug("Hello this is a debug message");
	}

	public static void LogInfo(String message) {
		log.info("Hello this is an info message");
	}
	
	public static void LogError(String message) {
		log.error("Hello this is an info message");
	}
}
