package DAL;

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
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import GlobalConstants.Constants;
import GlobalConstants.GlobalConfig;
import GlobalConstants.Enums.ApiAction;
import Interfaces.IDAL;
import Utils.GeneralFunctions;
import Utils.Logit;

public class DAL implements IDAL {

	private static DAL dal = null;
	private static String apiUrl;
	private static ConcurrentLinkedQueue<ApiRequest> failedRequests;

	protected DAL() {
		apiUrl = "";
		if (GlobalConfig.getApiSSL())
			apiUrl += Constants.https_prefix;
		else
			apiUrl += Constants.http_prefix;
		apiUrl += GlobalConfig.getApiUrl();
		failedRequests = new ConcurrentLinkedQueue<ApiRequest>();
	}

	public static DAL getInstanece() {
		if (dal == null)
			dal = new DAL();
		return dal;
	}

	public JSONObject executeRequest(ApiRequest request) {
		executeFailedRequests();
		switch (request.getType()) {
		case Constants.get:
			return get(request.getAction());
		case Constants.put:
			return put(request.getAction(), request.getRequestBody(), false);
		}
		return null;
	}

	@Override
	public JSONObject get(ApiAction action) {

		String fullUrl = getApiUrl();
		fullUrl += "/" + action.name() + "/";
		fullUrl += GlobalConfig.getDataCenterID() + "-" + GlobalConfig.getThisHostToken() + "/"
				+ GlobalConfig.getApiAuthToken();

		URL url;
		try {
			url = new URL(fullUrl);
		} catch (MalformedURLException e) {
			Logit.LogFatal("DAL - get", "Unable to process URL: " + fullUrl + ", failed to communicate with API!", e);
			return null;
		}

		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			Logit.LogFatal("DAL - get",
					"Unable to open connection with URL: " + fullUrl + ", failed to communicate with API!", e);
			addGetFailedRequest(action);
			return null;
		}

		try {
			conn.setRequestMethod(Constants.get);
		} catch (ProtocolException e) {
			Logit.LogFatal("DAL - get",
					"Unable to set GET request method for URL: " + fullUrl + ", failed to communicate with API!", e);
			addGetFailedRequest(action);

			return null;
		}

		String authCredentials = GeneralFunctions
				.Base64Encode(GlobalConfig.getApiUser() + ":" + GlobalConfig.getApiPass());
		conn.setRequestProperty("Authorization", "Basic " + authCredentials);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setAllowUserInteraction(false);

		String response;

		try {
			response = executeGetRequest(conn);
		} catch (Exception e) {
			Logit.LogFatal("DAL - get", "Failed to request URL: " + fullUrl + ", E: " + e.getMessage(), e);
			addGetFailedRequest(action);

			return null;
		} finally {
			conn.disconnect();
		}

		try {
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
				return response.equals("") ? null : (JSONObject) ((new JSONParser()).parse(response));
		} catch (IOException ioe) {
			Logit.LogFatal("DAL - get", "Unable to retrieve response code from response for url: " + fullUrl
					+ ", response is: " + response + ", E: " + ioe.getMessage(), ioe);
			addGetFailedRequest(action);

			return null;

		} catch (ParseException pe) {
			Logit.LogFatal("DAL - get", "Unable to parse json string from URL: " + fullUrl + ", JSON string is: "
					+ response + ", E: " + pe.getMessage(), pe);
			addGetFailedRequest(action);

			return null;

		}
		addGetFailedRequest(action);
		return null;
	}

	@Override
	public JSONObject put(ApiAction action, JSONObject reqBody, boolean isBase64Decode) {
		String fullUrl = getApiUrl();
		fullUrl += "/" + action.name() + "/";
		fullUrl += GlobalConfig.getDataCenterID() + "-" + GlobalConfig.getThisHostToken() + "/"
				+ GlobalConfig.getApiAuthToken();

		URL url;
		try {
			url = new URL(fullUrl);
		} catch (MalformedURLException e) {

			Logit.LogFatal("DAL - put", "Unable to process URL: " + fullUrl + ", failed to communicate with API!", e);
			addPutFailedRequest(action, reqBody);
			return null;
		}
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			Logit.LogFatal("DAL - put",
					"Unable to open connection with URL: " + fullUrl + ", failed to communicate with API!", e);
			addPutFailedRequest(action, reqBody);

			return null;
		}

		try {
			conn.setRequestMethod(Constants.put);
		} catch (ProtocolException e) {
			Logit.LogError("DAL - put",
					"Unable to set PUT request method for URL: " + fullUrl + ", failed to communicate with API!");
			addPutFailedRequest(action, reqBody);

			return null;
		}
		String authCredentials = GeneralFunctions
				.Base64Encode(GlobalConfig.getApiUser() + ":" + GlobalConfig.getApiPass());
		conn.setRequestProperty("Authorization", "Basic " + authCredentials);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setAllowUserInteraction(false);

		String response;

		try {
			if (isBase64Decode) {
				String base64String = GeneralFunctions.Base64Encode(reqBody.toJSONString());
				response = executePutRequest(conn, base64String);
			} else
				response = executePutRequest(conn, reqBody.toJSONString());
		} catch (Exception e) {
			Logit.LogFatal("DAL - put", "Failed to request URL: " + fullUrl + ", E: " + e.getMessage(), e);
			addPutFailedRequest(action, reqBody);

			return null;

		} finally {
			conn.disconnect();
		}
		try {
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
				return response.equals("") ? null : (JSONObject) ((new JSONParser()).parse(response.toString()));
		} catch (IOException ioe) {
			Logit.LogFatal("DAL - put", "Unable to retrieve response code from response for url: " + fullUrl
					+ ", response is: " + response + ", E: " + ioe.getMessage(), ioe);
			addPutFailedRequest(action, reqBody);

			return null;

		} catch (ParseException pe) {
			Logit.LogFatal("DAL - put", "Unable to parse json string from URL: " + fullUrl + ", JSON string is: "
					+ response + ", E: " + pe.getMessage(), pe);
			addPutFailedRequest(action, reqBody);

			return null;
		}
		addPutFailedRequest(action, reqBody);

		return null;
	}

	private String getApiUrl() {
		return apiUrl;
	}

	private void executeFailedRequests() {
		while (true) {
			if (failedRequests.isEmpty())
				return;
			ApiRequest failedRequest = failedRequests.peek();
			switch (failedRequest.getType()) {
			case Constants.get:
				if (get(failedRequest.getAction()) != null)
					failedRequests.remove();
				break;
			case Constants.put:
				if (put(failedRequest.getAction(), failedRequest.getRequestBody(), false) != null)
					failedRequests.remove();
			}
		}

	}

	private void addPutFailedRequest(ApiAction action, JSONObject requestBody) {
		// failedRequests.add(new ApiRequest(action, requestBody));
	}

	private void addGetFailedRequest(ApiAction action) {
		// failedRequests.add(new ApiRequest(action));
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
