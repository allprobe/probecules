package lycus.DAL;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import lycus.Utils.GeneralFunctions;
import lycus.Utils.Logit;
import lycus.GlobalConstants.Constants;
import lycus.GlobalConstants.GlobalConfig;
import lycus.GlobalConstants.Enums.ApiAction;
import lycus.Interfaces.IDAL;

public class DAL implements IDAL {

	private static DAL dal = null;
	private static String apiUrl;
	private static File failedApiRequests;

	protected DAL() {
		apiUrl = "";
		if (GlobalConfig.getApiSSL())
			apiUrl += Constants.https_prefix;
		else
			apiUrl += Constants.http_prefix;
		apiUrl += GlobalConfig.getApiUrl();
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
		fullUrl += GlobalConfig.getDataCenterID() + "-" + GlobalConfig.getThisHostToken() + "/" + GlobalConfig.getApiAuthToken();

		URL url;
		try {
			url = new URL(fullUrl);
		} catch (MalformedURLException e) {
			Logit.LogFatal("DAL - get","Unable to process URL: " + fullUrl + ", failed to communicate with API!");
			return null;
		}

		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			Logit.LogFatal("DAL - get","Unable to open connection with URL: " + fullUrl + ", failed to communicate with API!");
			return null;
		}

		try {
			conn.setRequestMethod(Constants.get);
		} catch (ProtocolException e) {
			Logit.LogFatal("DAL - get","Unable to set GET request method for URL: " + fullUrl + ", failed to communicate with API!");
			return null;
		}

		String authCredentials = GeneralFunctions.Base64Encode(GlobalConfig.getApiUser() + ":" + GlobalConfig.getApiPass());
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
			Logit.LogFatal("DAL - get","Unable to parse json string from URL: " + fullUrl + ", failed to init server!");
			return null;

		} catch (Exception e) {
			Logit.LogFatal("DAL - get","Failed to request URL: " + fullUrl + ", response code is different than 200 OK !");
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
		fullUrl += GlobalConfig.getDataCenterID() + "-" + GlobalConfig.getThisHostToken() + "/" + GlobalConfig.getApiAuthToken();

		URL url;
		try {
			url = new URL(fullUrl);
		} catch (MalformedURLException e) {
			
			Logit.LogError("DAL - put", "Unable to process URL: " + fullUrl + ", failed to communicate with API!");
			return null;
		}
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			Logit.LogError("DAL - put","Unable to open connection with URL: " + fullUrl + ", failed to communicate with API!");
			return null;
		}

		try {
			conn.setRequestMethod(Constants.put);
		} catch (ProtocolException e) {
			Logit.LogError("DAL - put", "Unable to set PUT request method for URL: " + fullUrl + ", failed to communicate with API!");
			return null;
		}
		String authCredentials = GeneralFunctions.Base64Encode(GlobalConfig.getApiUser() + ":" + GlobalConfig.getApiPass());
		conn.setRequestProperty("Authorization", "Basic " + authCredentials);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setAllowUserInteraction(false);

		try {
			String response = executePutRequest(conn, reqBody.toJSONString());
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
				return response.equals("") ? null :  (JSONObject) ((new JSONParser()).parse(response.toString()));;
		} catch (ParseException pe) {
			Logit.LogError("DAL - put", "Unable to parse json string from URL: " + fullUrl + ", failed to init server!");
			return null;

		} catch (Exception e) {
			Logit.LogError("DAL - put", "Failed to request URL: " + fullUrl + ", response code is different than 200 OK !");
			return null;
		} finally {
			conn.disconnect();
		}
		return null;
	}

	private String getApiUrl() {
		return apiUrl;
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
}
