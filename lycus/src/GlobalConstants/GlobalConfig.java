/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GlobalConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.util.Loader;

import Utils.Logit;

/**
 *
 * @author ran
 */
public class GlobalConfig {

	private static String confPath = null;

	private static String dateFormat = "yyyy-MM-dd HH:mm:ss";
	private static String ThisHostname = null;
	private static String ThisHostIP = null;
	private static String ThisHostToken = null;
	private static String DataCenterID = null;
	private static String apiUser = null;
	private static String apiPass = null;
	private static String apiUrl = null;
	private static Boolean apiSSL = null;
	private static String apiAuthToken = null;
	private static int PingerThreadCount = 50;
	private static int PorterThreadCount = 50;
	private static int WeberThreadCount = 50;
	private static int RblThreadCount = 50;
	private static int SnmpThreadCount = 50;
	private static int SnmpBatchThreadCount = 50;
	private static int BandwidthThreadCount = 50;
	private static int DiskhreadCount = 50;
	private static int TracerouteThreadCount = 50;
	private static int SqlThreadCount = 50;
	private static Boolean Debug = null;
	private static Boolean Development = null;
	private static String syslogHost = null;
	private static String log4jConfigLocation = null;

	private static Character ArraySeperator = null;
	private static Character KeySeperator = null;
	private static String HostSnmpOK = "1.3.6.1.2.1.1.5.0";
	private static int maxSnmpResponseInBytes = 100;

	public static String getDateFormat() {
		return dateFormat;
	}

	public static int getPingerThreadCount() {
		return PingerThreadCount;
	}

	public static int getPorterThreadCount() {
		return PorterThreadCount;
	}

	public static int getWeberThreadCount() {
		return WeberThreadCount;
	}

	public static int getRblThreadCount() {
		return RblThreadCount;
	}

	public static int getSnmpThreadCount() {
		return SnmpThreadCount;
	}

	public static int getSnmpBatchThreadCount() {
		return SnmpBatchThreadCount;
	}

	public static int getSqlThreadCount() {
		return SqlThreadCount;
	}

	public static String getThisHostIP() {
		return ThisHostIP;
	}

	public static String getThisHostname() {
		return ThisHostname;
	}

	public static void setThisHostname(String thisHostname) {
		ThisHostname = thisHostname;
	}

	public static String getThisHostToken() {
		return ThisHostToken;
	}

	public static String getDataCenterID() {
		return DataCenterID;
	}

	public static Boolean getDebug() {
		return Debug;
	}

	public static Boolean getDevelopment() {
		return Development;
	}

	public static String getSyslogHost() {
		return syslogHost;
	}

	public static void setSyslogHost(String syslogHost) {
		GlobalConfig.syslogHost = syslogHost;
	}

	public static int getMaxSnmpResponseInBytes() {
		return maxSnmpResponseInBytes;
	}

	public static void setMaxSnmpResponseInBytes(int maxSnmpResponseInBytes) {
		GlobalConfig.maxSnmpResponseInBytes = maxSnmpResponseInBytes;
	}

	public static String getHostSnmpOK() {
		return HostSnmpOK;
	}

	public static void setHostSnmpOK(String hostSnmpOK) {
		HostSnmpOK = hostSnmpOK;
	}

	public static String getApiUrl() {
		return apiUrl;
	}

	public static void setApiUrl(String apiUrl) {
		GlobalConfig.apiUrl = apiUrl;
	}

	public static Boolean getApiSSL() {
		return apiSSL;
	}

	public static void setApiSSL(Boolean apiSSL) {
		GlobalConfig.apiSSL = apiSSL;
	}

	public static String getApiUser() {
		return apiUser;
	}

	public static void setApiUser(String apiUser) {
		GlobalConfig.apiUser = apiUser;
	}

	public static String getApiPass() {
		return apiPass;
	}

	public static void setApiPass(String apiPass) {
		GlobalConfig.apiPass = apiPass;
	}

	public static String getConfPath() {
		return confPath;
	}

	public static void setConfPath(String confPath) {
		GlobalConfig.confPath = confPath;
	}

	public static String getApiAuthToken() {
		return apiAuthToken;
	}

	public static void setApiAuthToken(String apiAuthToken) {
		GlobalConfig.apiAuthToken = apiAuthToken;
	}

	public static boolean Initialize() {

		setEnvironmentProperties();

		Properties prop = new Properties();
		InputStream input = null;
		try {
			if (getConfPath() == null) {
				File configFile = new File("config.properties");
				if (configFile.exists() && !configFile.isDirectory()) {
					System.out.println("Loading config: " + configFile.getAbsolutePath());
					input = new FileInputStream(configFile);
				} else {
					Logit.LogError("No Config Found!", "");
					System.err.println("No Config Found!");
					return false;
				}
			} else
				input = new FileInputStream(getConfPath());
			prop.load(input);
			ThisHostIP = prop.getProperty("Server_IP");
			ThisHostname = prop.getProperty("Hostname");
			ThisHostToken = prop.getProperty("Server_token");
			DataCenterID = prop.getProperty("DataCenter_id");
			setApiUser(prop.getProperty("API_user"));
			setApiPass(prop.getProperty("API_pass"));
			apiUrl = prop.getProperty("API_url");
			apiSSL = Boolean.valueOf(prop.getProperty("API_ssl")).booleanValue();
			apiAuthToken = prop.getProperty("API_auth_token");
			PingerThreadCount = Integer.parseInt(prop.getProperty("Pinger_thread_count"));
			PorterThreadCount = Integer.parseInt(prop.getProperty("Porter_thread_count"));
			WeberThreadCount = Integer.parseInt(prop.getProperty("Weber_thread_count"));
			SnmpBatchThreadCount = Integer.parseInt(prop.getProperty("SnmpBatch_thread_count"));
			SnmpThreadCount = Integer.parseInt(prop.getProperty("Snmp_thread_count"));
			RblThreadCount = Integer.parseInt(prop.getProperty("RBL_thread_count"));
			Debug = Boolean.valueOf(prop.getProperty("Debug")).booleanValue();
			Development = Boolean.valueOf(prop.getProperty("Development")).booleanValue();
//			syslogHost = prop.getProperty("Syslog_host");
//			log4jConfigLocation = prop.getProperty("Log4j2_config");
//			File log4jConfigFile;
//			if (log4jConfigLocation == null || log4jConfigLocation == "") {
//				log4jConfigFile = new File("..", "log4j.xml");
//				System.out.println("Log4j file was not configured in config file, searching for: "
//						+ log4jConfigFile.getAbsolutePath());
//			} else {
//				log4jConfigFile = new File(log4jConfigLocation);
//				System.out.println(
//						"Log4j file configured in config file, searching for: " + log4jConfigFile.getAbsolutePath());
//
//			}
//			if (log4jConfigFile.exists())
//				System.out.println("Loading Log4j config file: " + log4jConfigFile.getAbsolutePath());
//			else
//				System.out.println("Loading default Log4j config file.");
//			System.setProperty("log4j.configuration", log4jConfigFile.getAbsolutePath());
//			DOMConfigurator.configure(log4jConfigFile.getAbsolutePath());
//			File log4jFile =log4jConfigFile ;
//			  if (log4jFile.exists()) {
//				  ((org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false)).reconfigure();
//				  PropertyConfigurator.configureAndWatch("./"+log4jFile.toString(), 10 * 1000); //every 10 secs
//				  ((org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false)).reconfigure();
//			    Object log = LoggerFactory.getLogger(Server.class);
//			  }
//			PropertyConfigurator.configureAndWatch("log4j.properties");
//		    String prop2 = System.getProperty("log4j.configuration");
//		    if (prop2 == null) prop2          = "log4j.properties";
//		    URL log4jConfig =org.ayache.log4j.helpers.getResource(prop2);
//		    if (log4jConfIG.GETPROTOCOL().EQUALSIGNORECASE("FILE")) {
//		        PROPERTYCONFIGURATOR.CONFIGUREANDWATCH(LOG4JCONFIG.GETFILE().SUBSTRING(1), 10000);
//		    }
//		    ELSE {
//		        // CANNOT MONITOR IF FILE CHANGED BECAUSE URL IS NOT A FILE
//		    }
//			LogManager.getRootLogger().info("test");
			ArraySeperator = prop.getProperty("Array_seperator").charAt(0);
			KeySeperator = prop.getProperty("Key_seperator").charAt(0);
		} catch (FileNotFoundException e) {
			Logit.LogError("No Config Found!", "");
			System.err.println("No Config Found!");
			return false;
		} catch (IOException e) {
			Logit.LogError("Error Loading Config File!(IO)", "");
			System.err.println("Error Loading Config File!(IO)");
			return false;
		} catch (Exception e) {
			Logit.LogError("Error Loading Config File!(config is not valid)", "");
			System.err.println("Error Loading Config File!(config is not valid)");
			return false;
		}
		String missingConfigs = validateGlobalVars();
		if (missingConfigs == "" ? false : true) {
			Logit.LogError("Global Initialization Failed!", "");
			System.err.println("Global Initialization Failed! at config: " + missingConfigs);
			return false;
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// Calendar cal = Calendar.getInstance();
		// Logit.LogCheck("Global Initialization Succeed!" +
		// dateFormat.format(cal.getTime()));
		Logit.LogCheck("Global Initialization Succeed! SRV: " + DataCenterID + "-" + ThisHostToken);

		return true;
	}

	private static void setEnvironmentProperties() {
		java.security.Security.setProperty("networkaddress.cache.ttl", "0");
		java.security.Security.setProperty("networkaddress.cache.negative.ttl", "0");
	}

	public static String ToString() {
		StringBuilder s = new StringBuilder();
		s.append("This Host IP:").append(ThisHostIP).append(";\n");
		s.append("This Host Token:").append(ThisHostToken).append(";\n");
		s.append("DataCenter ID:").append(DataCenterID).append(";\n");
		s.append("API User:").append(getApiUser()).append(";\n");
		s.append("API Password:").append(getApiPass()).append(";\n");
		s.append("Pinger Threads Count:").append(PingerThreadCount).append(";\n");
		s.append("Porter Threads Count:").append(PorterThreadCount).append(";\n");
		s.append("Weber Threads Count:").append(WeberThreadCount).append(";\n");
		s.append("SnmpBatch Threads Count:").append(SnmpBatchThreadCount).append(";\n");
		s.append("Snmp Threads Count:").append(SnmpThreadCount).append(";\n");
		s.append("RBL Threads Count:").append(RblThreadCount).append(";\n");
		s.append("Debug Mode:").append(Debug).append(";\n");
		s.append("Development Mode:").append(Development).append(";");
		s.append("Array Seperator:").append(ArraySeperator).append(";\n");
		s.append("Key Seperator:").append(KeySeperator).append(";\n");
		return s.toString();
	}

	// returns missing configuration name or null if otherwise
	public static String validateGlobalVars() {
		if (PingerThreadCount == -1)
			return "PingerThreadCount";
		if (PorterThreadCount == -1)
			return "PorterThreadCount";
		if (WeberThreadCount == -1)
			return "WeberThreadCount";
		if (RblThreadCount == -1)
			return "RblThreadCount";
		if (SnmpThreadCount == -1)
			return "SnmpThreadCount";
		if (SnmpBatchThreadCount == -1)
			return "SnmpBatchThreadCount";
		if (ThisHostIP == null)
			return "ThisHostIP";
		if (ThisHostToken == null)
			return "ThisHostToken";
		if (DataCenterID == null)
			return "DataCenterID";
		if (ArraySeperator == null)
			return "ArraySeperator";
		if (KeySeperator == -1)
			return "KeySeperator";
		if (DataCenterID == null)
			return "DataCenterID";
		if (Debug == null)
			return "Debug";
		if (Development == null)
			return "Development";
//		if (log4jConfigLocation == null)
//			return "log4jConfigLocation";
		// if (syslogHost==null)
		// return false;
		return "";

	}

	public static int getBandwidthThreadCount() {
		return BandwidthThreadCount;
	}

	public static void setBandwidthThreadCount(int bandwidthThreadCount) {
		BandwidthThreadCount = bandwidthThreadCount;
	}

	public static int getDiskhreadCount() {
		return DiskhreadCount;
	}

	public static void setDiskhreadCount(int diskhreadCount) {
		DiskhreadCount = diskhreadCount;
	}

	public static int getTracerouteThreadCount() {
		return TracerouteThreadCount;
	}

}
