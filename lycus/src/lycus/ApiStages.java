/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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

public class ApiStages {

	private static String apiUrl;

	public static boolean Initialize() {
		String fullUrl = "";
		if (Global.getApiSSL())
			fullUrl += "https://";
		else
			fullUrl += "http://";
		fullUrl += Global.getApiUrl();
		apiUrl = fullUrl;
		return true;
	}

	public static String getApiUrl() {
		return apiUrl;
	}

	public static void setApiUrl(String apiUrl) {
		ApiStages.apiUrl = apiUrl;
	}

	public static JSONObject InitServer() {
		String fullUrl = getApiUrl();
		fullUrl += "/InitServer/";
		fullUrl += Global.getDataCenterID() + "-" + Global.getThisHostToken() + "/" + Global.getApiAuthToken();
		URL url;
		try {
			url = new URL(fullUrl);
		} catch (MalformedURLException e) {
			SysLogger.Record(
					new Log("Unable to process URL: " + fullUrl + ", failed to init server!", LogType.Error, e));
			return null;
		}
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			SysLogger.Record(
					new Log("Unable to connect URL: " + fullUrl + ", failed to init server!", LogType.Error, e));
			return null;
		}
		try {
			conn.setRequestMethod("GET");
		} catch (ProtocolException e) {
			SysLogger
					.Record(new Log("Unable to set GET request method for URL: " + fullUrl + ", failed to init server!",
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
				SysLogger.Record(
						new Log("Unable to connect URL: " + fullUrl + ", failed to init server!", LogType.Error));
				return null;
			}
		} catch (IOException e) {
			SysLogger.Record(new Log("Unable to connect URL: " + fullUrl + ", failed to init server!", LogType.Error));
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
			SysLogger.Record(new Log("Unable to connect URL: " + fullUrl + ", failed to init server!", LogType.Error));
			return null;
		} finally {
			conn.disconnect();
		}
		JSONObject fullData;
		try {
			fullData = (JSONObject) ((new JSONParser()).parse(sb.toString()));
		} catch (ParseException e) {
			SysLogger.Record(new Log("Unable to parse json string from URL: " + fullUrl + ", failed to init server!",
					LogType.Error));
			return null;
		}

		return fullData;
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
	
	public static String retrieveExistingRollups() {
		String fullUrl = getApiUrl();
		fullUrl += "/GetServerMemoryDump/";
		fullUrl += Global.getDataCenterID() + "-" + Global.getThisHostToken() + "/" + Global.getApiAuthToken();
		URL url;
		String fullData;
		try {
			url = new URL(fullUrl);
		} catch (MalformedURLException e) {
			SysLogger.Record(new Log("Unable to process URL: " + fullUrl + ", failed to retrieve existing rollups!",
					LogType.Error, e));
			return null;
		}
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			SysLogger.Record(new Log("Unable to connect URL: " + fullUrl + ", failed to retrieve existing rollups!",
					LogType.Error, e));
			return null;
		}
		try {
			conn.setRequestMethod("GET");
		} catch (ProtocolException e) {
			SysLogger.Record(new Log(
					"Unable to set GET request method for URL: " + fullUrl + ", failed to retrieve existing rollups!",
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
				SysLogger.Record(new Log("Unable to connect URL: " + fullUrl + ", failed to retrieve existing rollups!",
						LogType.Error));
				return null;
			}
		} catch (IOException e) {
			SysLogger.Record(new Log("Unable to connect URL: " + fullUrl + ", failed to retrieve existing rollups!",
					LogType.Error));
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
			SysLogger.Record(new Log("Unable to connect URL: " + fullUrl + ", failed to retrieve existing rollups!",
					LogType.Error));
			return null;
		} finally {
			conn.disconnect();
		}
		if (sb.toString().equals("0\n")) {
			fullData = "not_exists";
			return fullData;
		}
		fullData = sb.toString();
		return fullData.substring(1, fullData.length() - 1);
	}

	public static boolean insertExistingRollups(String rollups) {
		String fullUrl = getApiUrl();
		fullUrl += "/FlushServerMemory/";
		fullUrl += Global.getDataCenterID() + "-" + Global.getThisHostToken() + "/" + Global.getApiAuthToken();
		URL url;
		try {
			url = new URL(fullUrl);
		} catch (MalformedURLException e) {
			SysLogger.Record(
					new Log("Unable to process URL: " + fullUrl + ", failed to flush rollups!", LogType.Error, e));
			return false;
		}
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			SysLogger.Record(
					new Log("Unable to connect URL: " + fullUrl + ", failed to flush rollups!", LogType.Error, e));
			return false;
		}
		try {
			conn.setRequestMethod("PUT");
		} catch (ProtocolException e) {
			SysLogger.Record(
					new Log("Unable to set PUT request method for URL: " + fullUrl + ", failed to flush rollups!",
							LogType.Error, e));
			return false;
		}
		String authCredentials = GeneralFunctions.Base64Encode(Global.getApiUser() + ":" + Global.getApiPass());
		conn.setRequestProperty("Authorization", "Basic " + authCredentials);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setAllowUserInteraction(false);

		try {
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());

			out.write("{\"last_rollups\":\"" + rollups + "\"}");
			out.close();
			conn.getInputStream();
		} catch (IOException e) {
			SysLogger.Record(
					new Log("Unable to send existing rollups for URL: " + fullUrl + ", failed to flush rollups!",
							LogType.Error, e));
			return false;
		}
		return true;
	}

	public static boolean insertDatapointsBatches(String jsonresults) {
		SysLogger.Record(new Log("ApiStages.insertDatapointsBatches: " + jsonresults, LogType.Debug));
		String results = GeneralFunctions.Base64Encode(jsonresults);
		String fullUrl = getApiUrl();
		fullUrl += "/insertDatapointsBatches/";
		fullUrl += Global.getDataCenterID() + "-" + Global.getThisHostToken() + "/" + Global.getApiAuthToken();
		URL url;
		try {
			url = new URL(fullUrl);
		} catch (MalformedURLException e) {
			SysLogger.Record(
					new Log("Unable to process URL: " + fullUrl + ", failed to insert results!", LogType.Error, e));
			return false;
		}
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			SysLogger.Record(
					new Log("Unable to connect URL: " + fullUrl + ", failed to insert results!", LogType.Error, e));
			return false;
		}
		try {
			conn.setRequestMethod("PUT");
		} catch (ProtocolException e) {
			SysLogger.Record(
					new Log("Unable to set PUT request method for URL: " + fullUrl + ", failed to insert results!",
							LogType.Error, e));
			return false;
		}
		String authCredentials = GeneralFunctions.Base64Encode(Global.getApiUser() + ":" + Global.getApiPass());
		conn.setRequestProperty("Authorization", "Basic " + authCredentials);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setAllowUserInteraction(false);

		try {
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
			out.write("{\"results\" : \"" + results + "\"}");
			out.close();
			if (GeneralFunctions.convertStreamToString(conn.getInputStream()).equals("1"))
				return true;
			return false;
		} catch (IOException e) {
			SysLogger.Record(
					new Log("Unable to send existing results for URL: " + fullUrl + ", failed to insert results!",
							LogType.Error, e));
			return false;
		}
	}

	public static boolean putEvents(String events) {
		SysLogger.Record(new Log("ApiStages.putEvents: " + events, LogType.Debug));
		String fullUrl = getApiUrl();
		fullUrl += "/PutEvents/";
		fullUrl += Global.getDataCenterID() + "-" + Global.getThisHostToken() + "/" + Global.getApiAuthToken();
		URL url;
		try {
			url = new URL(fullUrl);
		} catch (MalformedURLException e) {
			SysLogger.Record(
					new Log("Unable to process URL: " + fullUrl + ", failed to send events!", LogType.Error, e));
			return false;
		}
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			SysLogger.Record(
					new Log("Unable to connect URL: " + fullUrl + ", failed to send events!", LogType.Error, e));
			return false;
		}
		try {
			conn.setRequestMethod("PUT");
		} catch (ProtocolException e) {
			SysLogger
					.Record(new Log("Unable to set PUT request method for URL: " + fullUrl + ", failed to send events!",
							LogType.Error, e));
			return false;
		}
		String authCredentials = GeneralFunctions.Base64Encode(Global.getApiUser() + ":" + Global.getApiPass());
		conn.setRequestProperty("Authorization", "Basic " + authCredentials);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setAllowUserInteraction(false);

		try {
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
			String test = "{\"events\" : \"" + events + "\"}";
			out.write(test);
			out.close();
			if (GeneralFunctions.convertStreamToString(conn.getInputStream()).equals("1"))
				return true;
			return false;
		} catch (IOException e) {
			SysLogger.Record(new Log("Unable to send events for URL: " + fullUrl + ", failed to send events!",
					LogType.Error, e));
			return false;
		}
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
			SysLogger.Record(new Log("Unable to connect URL: " + fullUrl + ", failed to retrieve live events!",
					LogType.Error));
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
			SysLogger.Record(new Log("Unable to connect URL: " + fullUrl + ", failed to retrieve live events!",
					LogType.Error));
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

}