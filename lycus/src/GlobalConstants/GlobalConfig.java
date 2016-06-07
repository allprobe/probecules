/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GlobalConstants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

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
			if (getConfPath() == null)
				input = new FileInputStream("../config.properties");
			else
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
			syslogHost = prop.getProperty("Syslog_host");
			log4jConfigLocation = prop.getProperty("Log4j2_config_location");
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
		if (!validateGlobalVars()) {
			Logit.LogError("Global Initialization Failed!", "");
			System.err.println("Global Initialization Failed!");
			return false;
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println("Global Initialization Succeed! - " + dateFormat.format(cal.getTime()));
		Logit.LogCheck("Global Initialization Succeed!" + dateFormat.format(cal.getTime()));

		return true;
	}

	private static void setEnvironmentProperties() {
		java.security.Security.setProperty("networkaddress.cache.ttl" , "0");
		java.security.Security.setProperty("networkaddress.cache.negative.ttl","0");
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

	public static boolean validateGlobalVars() {
		if (PingerThreadCount == -1)
			return false;
		if (PorterThreadCount == -1)
			return false;
		if (WeberThreadCount == -1)
			return false;
		if (RblThreadCount == -1)
			return false;
		if (SnmpThreadCount == -1)
			return false;
		if (SnmpBatchThreadCount == -1)
			return false;
		if (ThisHostIP == null)
			return false;
		if (ThisHostToken == null)
			return false;
		if (DataCenterID == null)
			return false;
		if (ArraySeperator == null)
			return false;
		if (KeySeperator == -1)
			return false;
		if (DataCenterID == null)
			return false;
		if (Debug == null)
			return false;
		if (Development == null)
			return false;
		if (log4jConfigLocation == null)
			return false;
		// if (syslogHost==null)
		// return false;
		return true;

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

}
