/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.jar.Attributes.Name;

import org.snmp4j.smi.OID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ApiInterface {

	private static String apiUrl;
	private static File failedApiRequests;

	public static boolean Initialize() {
		String fullUrl = "";
		if (Global.getApiSSL())
			fullUrl += "https://";
		else
			fullUrl += "http://";
		fullUrl += Global.getApiUrl();
		apiUrl = fullUrl;
		createFailsFolder();
		processOfflineRequests();
		return true;
	}

	public static Object executeRequest(ApiStages stage, String reqMethod, String reqBody) {
		String fullUrl = getApiUrl();
		fullUrl += "/" + stage.name() + "/";
		fullUrl += Global.getDataCenterID() + "-" + Global.getThisHostToken() + "/" + Global.getApiAuthToken();
		URL url;
		try {
			url = new URL(fullUrl);
		} catch (MalformedURLException e) {
			SysLogger.Record(new Log("Unable to process URL: " + fullUrl + ", failed to communicate with API!",
					LogType.Error, e));
			return null;
		}
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			SysLogger.Record(
					new Log("Unable to open connection with URL: " + fullUrl + ", failed to communicate with API!",
							LogType.Error, e));
			return null;
		}

		try {
			conn.setRequestMethod(reqMethod);
		} catch (ProtocolException e) {
			SysLogger.Record(new Log("Unable to set " + reqMethod + " request method for URL: " + fullUrl
					+ ", failed to communicate with API!", LogType.Error, e));
			return null;
		}
		String authCredentials = GeneralFunctions.Base64Encode(Global.getApiUser() + ":" + Global.getApiPass());
		conn.setRequestProperty("Authorization", "Basic " + authCredentials);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setAllowUserInteraction(false);

		String GET = null;
		String PUT = null;

		try {
			switch (reqMethod) {
			case "GET":
				GET = executeGetRequest(conn);
				if (conn.getResponseCode() == 200) {
					JSONObject jsonData;
					jsonData = (JSONObject) ((new JSONParser()).parse(GET.toString()));
					return jsonData;
					// return GET;
				}
			case "PUT":
				PUT = reqBody;
				boolean isOK=executePutRequest(conn, PUT);
				if (conn.getResponseCode() == 200)
					return isOK?true:null;
			}
		} catch (ParseException pe) {
			SysLogger.Record(new Log("Unable to parse json string from URL: " + fullUrl + ", failed to init server!",
					LogType.Error));
			return null;

		} catch (Exception e) {
			SysLogger.Record(new Log("Failed to request URL: " + fullUrl + ", response code is different than 200 OK !",
					LogType.Error));
			return null;
		} finally {
			conn.disconnect();
		}
		return null;

	}

	private static boolean executePutRequest(HttpURLConnection conn, String pUT) throws Exception {
		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		out.write(pUT);
		out.close();
		
		InputStream inputStream=conn.getInputStream();
		if (GeneralFunctions.convertStreamToString(inputStream).equals("1"))
			return true;
		return false;
	}

	private static String executeGetRequest(HttpURLConnection conn) throws Exception {
		BufferedReader rd;
		StringBuilder sb = new StringBuilder();
		rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		}
		rd.close();
		return sb.toString();
	}

	private static void createFailsFolder() {
		setFailedApiRequests(new File("failed_api"));
		File mainFolder = getFailedApiRequests();
		// if the directory does not exist, create it
		if (!mainFolder.exists()) {
			try {
				mainFolder.mkdir();
			} catch (SecurityException se) {
				SysLogger.Record(new Log("Unable to create failed_api folder!", LogType.Error));
			}
			SysLogger.Record(new Log("Successfully failed_api folder created.", LogType.Info));
		} else {
			SysLogger.Record(new Log("Folder failed_api already exists, no new folder created.", LogType.Info));
		}
	}

	private static void processOfflineRequests() {
		File[] listOfFiles = getFailedApiRequests().listFiles();

	}

	public static String getApiUrl() {
		return apiUrl;
	}

	public static void setApiUrl(String apiUrl) {
		ApiInterface.apiUrl = apiUrl;
	}

	public static File getFailedApiRequests() {
		return failedApiRequests;
	}

	public static void setFailedApiRequests(File failedApiRequests) {
		ApiInterface.failedApiRequests = failedApiRequests;
	}


	public static HashMap<String, UUID> getInitRPs(Object longIds) {
		// key:templateId@hostId@probeId , value:userId
		HashMap<String, UUID> ExtendedProbeID_UserID = new HashMap<String, UUID>();
		JSONArray allRps = (JSONArray) longIds;

		for (int i = 0; i < allRps.size(); i++) {
			JSONObject rp = (JSONObject) allRps.get(i);
			ExtendedProbeID_UserID.put((String) rp.get("long_id"), UUID.fromString((String) rp.get("user_id")));
		}

		return ExtendedProbeID_UserID;

	}

	public static HashMap<UUID, HashMap<UUID, SnmpTemplate>> getInitSnmpTemplates(Object snmpTemplates) {
		HashMap<UUID, HashMap<UUID, SnmpTemplate>> snmpTemps = new HashMap<UUID, HashMap<UUID, SnmpTemplate>>();
		JSONArray allSnmpTemplates = (JSONArray) snmpTemplates;

		for (int i = 0; i < allSnmpTemplates.size(); i++) {
			JSONObject snmpTempJson = (JSONObject) allSnmpTemplates.get(i);
			UUID id = UUID.fromString((String) snmpTempJson.get("snmp_template_id"));
			String name = (String) snmpTempJson.get("snmp_tmp_name");
			int version = Integer.parseInt((String) snmpTempJson.get("snmp_tmp_version"));
			String commName = (String) snmpTempJson.get("snmp_tmp_community");
			String sec = (String) snmpTempJson.get("snmp_tmp_sec");
			String authMethod = (String) snmpTempJson.get("snmp_tmp_auth_method");
			String authCredentials = GeneralFunctions.Base64Decode((String) snmpTempJson.get("snmp_tmp_auth_password"));
			String cryptMethod = (String) snmpTempJson.get("snmp_tmp_crypt_method");
			String cryptCredentials = GeneralFunctions
					.Base64Decode((String) snmpTempJson.get("snmp_tmp_crypt_password"));
			int timeout = Integer.parseInt((String) snmpTempJson.get("snmp_tmp_timeout"));
			int port = Integer.parseInt((String) snmpTempJson.get("snmp_tmp_snmp_port"));
			SnmpTemplate snmpTemp;
			if (version <= 2)
				snmpTemp = new SnmpTemplate(id, name, commName, version, port, timeout, true);
			else
				snmpTemp = new SnmpTemplate(id, name, commName, version, port, timeout, true);

			if (snmpTemps.containsKey(UUID.randomUUID()))
				snmpTemps.get(UUID.randomUUID()).put(snmpTemp.getSnmpTemplateId(), snmpTemp);
			else {
				HashMap<UUID, SnmpTemplate> userSnmpTemps = new HashMap<UUID, SnmpTemplate>();
				userSnmpTemps.put(snmpTemp.getSnmpTemplateId(), snmpTemp);
				snmpTemps.put(UUID.randomUUID(), userSnmpTemps);
			}
		}
		return snmpTemps;
	}


	public static String retrieveExistingEvents() {
		String fullUrl = getApiUrl();
		fullUrl += "/GetServerLiveEvents/";
		fullUrl += Global.getDataCenterID() + "-" + Global.getThisHostToken() + "/" + Global.getApiAuthToken();
		URL url;
		String fullData;
		try {
			url = new URL(fullUrl);
		} catch (MalformedURLException e) {
			SysLogger.Record(new Log("Unable to process URL: " + fullUrl + ", failed to retrieve live events!",
					LogType.Error, e));
			return null;
		}
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			SysLogger.Record(new Log("Unable to connect URL: " + fullUrl + ", failed to retrieve live events!",
					LogType.Error, e));
			return null;
		}
		try {
			conn.setRequestMethod("GET");
		} catch (ProtocolException e) {
			SysLogger.Record(new Log(
					"Unable to set GET request method for URL: " + fullUrl + ", failed to retrieve live events!",
					LogType.Error, e));
			return null;
		}
		String authCredentials = GeneralFunctions.Base64Encode(Global.getApiUser() + ":" + Global.getApiPass());
		conn.setRequestProperty("Authorization", "Basic " + authCredentials);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setAllowUserInteraction(false);

		try {
			if (conn.getResponseCode() != 200) {
				SysLogger.Record(new Log("Unable to connect URL: " + fullUrl + ", failed to retrieve live events!",
						LogType.Error));
				return null;
			}
		} catch (IOException e) {
			SysLogger.Record(
					new Log("Unable to connect URL: " + fullUrl + ", failed to retrieve live events!", LogType.Error));
			return null;
		}
		BufferedReader rd;
		StringBuilder sb = new StringBuilder();
		try {
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			rd.close();
		} catch (IOException e) {
			SysLogger.Record(
					new Log("Unable to connect URL: " + fullUrl + ", failed to retrieve live events!", LogType.Error));
			return null;
		} finally {
			conn.disconnect();
		}
		if (sb.toString().equals("0\n")) {
			fullData = "not_exists";
			return fullData;
		}
		fullData = sb.toString();
		return fullData;
	}

	public static void createFailedRequestFile(String apiStage, String data) {
		String fileName = System.currentTimeMillis() + "_" + apiStage + ".log";
		PrintWriter writer;
		try {
			writer = new PrintWriter(getFailedApiRequests().getName() + fileName);
			writer.println(data);
			writer.close();
		} catch (FileNotFoundException e) {
			SysLogger.Record(new Log("DATA LOST! unable to write failed api request to file!", LogType.Error, e));
		}

	}
}