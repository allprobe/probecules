/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DAL;

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
import java.util.HashMap;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import GlobalConstants.Enums;
import GlobalConstants.GlobalConfig;
import Utils.GeneralFunctions;
import Utils.Logit;

public class ApiInterface {

//	private static ApiInterface apiInterface = new ApiInterface();
	private static String apiUrl;
	private static File failedApiRequests;

//	public static ApiInterface getInstanece()
//	{
//		return apiInterface;
//		
//	}
	
	public static boolean Initialize() {
		String fullUrl = "";
		if (GlobalConfig.getApiSSL())
			fullUrl += "https://";
		else
			fullUrl += "http://";
		fullUrl += GlobalConfig.getApiUrl();
		apiUrl = fullUrl;
//		createFailsFolder();
//		processOfflineRequests();
		return true;
	}

	public static Object executeRequest(Enums.ApiAction stage, String reqMethod, String reqBody) {
		String fullUrl = getApiUrl();
		fullUrl += "/" + stage.name() + "/";
		fullUrl += GlobalConfig.getDataCenterID() + "-" + GlobalConfig.getThisHostToken() + "/" + GlobalConfig.getApiAuthToken();
		URL url;
		try {
			url = new URL(fullUrl);
		} catch (MalformedURLException e) {
			Logit.LogError("ApiInterface - executeRequest()", "Unable to process URL: " + fullUrl + ", failed to communicate with API!\n" + e.getMessage());
			return null;
		}
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			Logit.LogError("ApiInterface - executeRequest()", "Unable to open connection with URL: " + fullUrl + ", failed to communicate with API!\n" + e.getMessage());
			return null;
		}

		try {
			conn.setRequestMethod(reqMethod);
		} catch (ProtocolException e) {
			Logit.LogError("ApiInterface - executeRequest()", "Unable to set " + reqMethod + " request method for URL: " + fullUrl
					+ ", failed to communicate with API!\n" + e.getMessage());
			return null;
		}
		String authCredentials = GeneralFunctions.Base64Encode(GlobalConfig.getApiUser() + ":" + GlobalConfig.getApiPass());
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
				String response=executePutRequest(conn, PUT);
				if (conn.getResponseCode() == 200)
					return response.equals("")?null:response;
			}
		} catch (ParseException pe) {
			Logit.LogError("ApiInterface - executeRequest()","Unable to parse json string from URL: " + fullUrl +  " failed to init server!");
			return null;

		} catch (Exception e) {
			Logit.LogError("ApiInterface - executeRequest()", "Failed to request URL: " + fullUrl + ", response code is different than 200 OK !");
			return null;
		} finally {
			conn.disconnect();
		}
		return null;

	}

	private static String executePutRequest(HttpURLConnection conn, String pUT) throws Exception {
		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		out.write(pUT);
		out.close();
		
		InputStream inputStream=conn.getInputStream();

		BufferedReader rd;
		StringBuilder sb = new StringBuilder();
		rd = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		}
		rd.close();

		return sb.toString();
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
				Logit.LogError("ApiInterface - createFailsFolder()", "Unable to create failed_api folder!");
			}
			Logit.LogError("ApiInterface - createFailsFolder()", "Successfully failed_api folder created.");
		} else {
			Logit.LogError("ApiInterface - createFailsFolder()", "Folder failed_api already exists, no new folder created.");
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



	public static void createFailedRequestFile(String apiStage, String data) {
		String fileName = System.currentTimeMillis() + "_" + apiStage + ".log";
		PrintWriter writer;
		try {
			writer = new PrintWriter(getFailedApiRequests().getName() + fileName);
			writer.println(data);
			writer.close();
		} catch (FileNotFoundException e) {
			Logit.LogError("ApiInterface - createFailedRequestFile()", "DATA LOST! unable to write failed api request to file!\n" + e.getMessage());
		}
	}
	
	public static void enableFileFailOver()
	{
		
	}
	
}