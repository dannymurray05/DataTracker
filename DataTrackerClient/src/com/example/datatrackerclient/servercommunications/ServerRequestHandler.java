package com.example.datatrackerclient.servercommunications;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerRequestHandler {
	private static Logger serverRequestLogger = Logger.getLogger("ServerRequestHandlerLogger");

	public static String serverAddress = "http://192.168.1.2:8080";
	public static String parameterIndicator = "?";
	public static String parameterDeliniator = "&";

	public static String registerUserStr = "/new_user";
	public static String phoneNumParam = "phoneNumber=";
	public static String passwordParam = "password=";
	public static String emailParam = "email=";
	public static void registerUser(String phoneNumber, String password, String email) {
		StringBuilder newUserString = new StringBuilder();
		newUserString.append(serverAddress).append(registerUserStr).append(parameterIndicator).append(phoneNumParam)
			.append(phoneNumber).append(parameterDeliniator).append(passwordParam).append(password).append(parameterDeliniator)
			.append(emailParam).append(email);
		serverRequestLogger.log(Level.INFO, newUserString.toString());
		URL newUserURL = null;
		try {
			newUserURL = new URL(newUserString.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			serverRequestLogger.log(Level.WARNING, "User registration failed due to invalid URL");
			return;
		}

		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection)newUserURL.openConnection();
			connection.connect();
		} catch (IOException e) {
			e.printStackTrace();
			serverRequestLogger.log(Level.WARNING, "User registration failed due to failure to make connection with server");
			return;
		}
		try {
			connection.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if(connection != null) {
			connection.disconnect();
		}
	}
}
