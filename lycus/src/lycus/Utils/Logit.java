package lycus.Utils;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;


public class Logit {
	static Logger log = Logger.getLogger("");
//	static Logger log = LogManager.getLogger("");

//	public Logit(String className)
//	{
//		log = Logger.getLogger(className);
//	}

	public static void LogDebug(String message) {
		if (log.isDebugEnabled())
			log.debug(message);
	}

	public static void LogInfo(String message) {
		if (log.isInfoEnabled())
			log.info(message);
	}
	
	// extraInfo - Class name + Function name
	public static void LogError(String extraInfo, String message) {
		log.error(message);
	}
	
	// extraInfo - Class name + Function name
	public static void LogFatal(String extraInfo, String message) {
		log.fatal(message);
	}
	
	public static void LogWarn(String message) {
		log.warn(message);
	}
	
	public static void LogTrace(String message) {
		log.trace(message);
	}
}
