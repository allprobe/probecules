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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import lycus.Enums.ApiAction;
import lycus.Interfaces.IDAL;

public class DAL implements IDAL {

	private static DAL dal = null;
	private static String apiUrl;
	private static File failedApiRequests;

	protected DAL() {
		apiUrl = "";
		if (Global.getApiSSL())
			apiUrl += Constants.https;
		else
			apiUrl += Constants.http;
		apiUrl += Global.getApiUrl();

		createFailsFolder();
		processOfflineRequests();
	}

	public static DAL getInstanece() {
		if (dal == null)
			dal = new DAL();
		return dal;
	}

	@Override
	public JSONObject get(ApiAction action) {
		String fullUrl = getApiUrl();
		fullUrl += "/" + action.name() + "/";
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
			conn.setRequestMethod(Constants.get);
		} catch (ProtocolException e) {
			SysLogger.Record(new Log(
					"Unable to set GET request method for URL: " + fullUrl + ", failed to communicate with API!",
					LogType.Error, e));
			return null;
		}

		String authCredentials = GeneralFunctions.Base64Encode(Global.getApiUser() + ":" + Global.getApiPass());
		conn.setRequestProperty("Authorization", "Basic " + authCredentials);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setAllowUserInteraction(false);

		try {
			String result = executeGetRequest(conn);
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				JSONObject jsonData;
				jsonData = (JSONObject) ((new JSONParser()).parse(result.toString()));
				return jsonData;
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

	@Override
	public JSONObject put(ApiAction action, JSONObject reqBody) {
		String fullUrl = getApiUrl();
		fullUrl += "/" + action.name() + "/";
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
			conn.setRequestMethod(Constants.put);
		} catch (ProtocolException e) {
			SysLogger.Record(new Log(
					"Unable to set PUT request method for URL: " + fullUrl + ", failed to communicate with API!",
					LogType.Error, e));
			return null;
		}
		String authCredentials = GeneralFunctions.Base64Encode(Global.getApiUser() + ":" + Global.getApiPass());
		conn.setRequestProperty("Authorization", "Basic " + authCredentials);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setAllowUserInteraction(false);

		try {
			String response = executePutRequest(conn, reqBody.toJSONString());
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
				return response.equals("") ? null :  (JSONObject) ((new JSONParser()).parse(response.toString()));;
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

	private void createFailsFolder() {
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

	private String getApiUrl() {
		return apiUrl;
	}

	private void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	private File getFailedApiRequests() {
		return failedApiRequests;
	}

	private void setFailedApiRequests(File failedApiRequests) {
		this.failedApiRequests = failedApiRequests;
	}

	private void processOfflineRequests() {
		File[] listOfFiles = getFailedApiRequests().listFiles();

	}

	private String executeGetRequest(HttpURLConnection conn) throws Exception {
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

	private String executePutRequest(HttpURLConnection conn, String body) throws Exception {
		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		out.write(body);
		out.close();

		InputStream inputStream = conn.getInputStream();
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

	public void createFailedRequestFile(String apiStage, String data) {
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
