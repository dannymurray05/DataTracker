package com.example.datatrackerclient.servercommunications;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerRequestHandler {
	private static Logger serverRequestLogger = Logger.getLogger("ServerRequestHandlerLogger");

	public static String serverAddress = "http://192.168.1.2:8080";
	public static String parameterIndicator = "?";
	public static String parameterDeliniator = "&";
	public static String equalsStr = "=";

	public static String phoneNumParam = "phoneNumber";
	public static String passwordParam = "password";
	public static String emailParam = "email";

	public static String newUserStr = "/new_user";
	public static JSONObject newUser(String phoneNumber, String password, String email) {
		return makeRequest(createRequestMapping(newUserStr, phoneNumParam, phoneNumber, passwordParam, password, emailParam, email));
	}

	public static String dateParam = "date";
	public static String hourParam = "hour";
	public static String bytesParam = "bytes";
	public static String logDataStr = "/log_data";
	public static JSONObject logData(String phoneNumber, Date date, int hour, long bytes) {
		return makeRequest(createRequestMapping(logDataStr, phoneNumParam, phoneNumber, dateParam,
				date.toString(), hourParam, String.valueOf(hour), bytesParam, String.valueOf(bytes)));
	}
	
	public static String dateBeginParam = "dateBegin";
	public static String dateEndParam = "dateEnd";
	public static String requestDataStr = "/request_data";
	public static JSONObject requestData(String phoneNumber, Date dateBegin, Date dateEnd) {
		return makeRequest(createRequestMapping(requestDataStr, phoneNumParam, phoneNumber, dateBeginParam,
				dateBegin.toString(), dateEndParam, dateEnd.toString()));
	}

	/**
	 * 
	 * @param requestType The type of request being made (e.g. new_user or log_data)
	 * @param params A set of parameter assignments (e.g. password, this_is_a_password, name, this_is_a_name)
	 * @return Returns the string needed by a URL to map to the correct server response.
	 */
	public static String createRequestMapping(String requestType, String... params) {
		StringBuilder request = new StringBuilder();
		request.append(serverAddress).append(requestType).append(parameterIndicator);
		for(int i = 0; i < params.length; i += 2) {
			request.append(params[i]);
			request.append(equalsStr);
			request.append(params[i + 1]);
			request.append(parameterDeliniator);
		}
		return request.toString();
	}
	
	public static JSONObject makeRequest(String request) {
		serverRequestLogger.log(Level.INFO, "Request: " + request.toString());
		URL requestURL = null;
		try {
			requestURL = new URL(request.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			serverRequestLogger.log(Level.WARNING, "Request failed due to invalid URL");
			return null;
		}

		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection)requestURL.openConnection();
			connection.connect();
		} catch (IOException e) {
			e.printStackTrace();
			serverRequestLogger.log(Level.WARNING, "Request failed due to failure to make connection with server");
			return null;
		}
		
		InputStream jsonStringStream = null;
		try {
			jsonStringStream = connection.getInputStream();
		} catch (IOException e) {
			serverRequestLogger.log(Level.WARNING, "Request failed due to failure to retrieve input stream");
			e.printStackTrace();
			return null;
		}

		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(jsonStringStream, writer);
		} catch (IOException e) {
			serverRequestLogger.log(Level.WARNING, "Request failed due to failure to convert response to string");
			e.printStackTrace();
			return null;
		}
		
		JSONObject jsonResponse = null;
		try {
			jsonResponse = new JSONObject(writer.toString());
		} catch (JSONException e) {
			serverRequestLogger.log(Level.WARNING, "Request failed due to failure to convert response to JSON");
			e.printStackTrace();
			return null;
		}
		
		if(connection != null) {
			connection.disconnect();
		}
		
		return jsonResponse;
	}
}
