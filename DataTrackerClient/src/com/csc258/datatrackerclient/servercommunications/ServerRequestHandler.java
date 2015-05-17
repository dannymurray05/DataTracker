package com.csc258.datatrackerclient.servercommunications;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import datatrackerstandards.RequestType;

public class ServerRequestHandler {
	//private static Logger serverRequestLogger = Logger.getLogger("ServerRequestHandlerLogger");
    public static final String TAG = ServerRequestHandler.class.getSimpleName();
	
	public static void makeRequest(RequestType requestType,
			Listener<String> listener, ErrorListener errorListener, String... params) {
		DataTrackerRequest request = new DataTrackerRequest(requestType,
				listener, errorListener, params);
		NetworkController.getInstance().addToRequestQueue(request, request.getURL());
	}
			
	public static void registerAccount(Listener<String> listener, ErrorListener errorListener,
			String phoneNumber, String password, String email) {
		makeRequest(RequestType.REGISTER_ACCOUNT, listener, errorListener,
				phoneNumber, password, email);
	}
	
	public static void validateEmail(Listener<String> listener, ErrorListener errorListener,
			String phoneNumber, String code) {
		makeRequest(RequestType.VALIDATE_EMAIL, listener, errorListener, phoneNumber, code);
	}
	
	public static void registerDevice(Listener<String> listener, ErrorListener errorListener,
			String phoneNumber, String accountPhoneNumber) {
		makeRequest(RequestType.REGISTER_DEVICE, listener, errorListener,
				phoneNumber, accountPhoneNumber);
	}
	
	public static void validateDevice(Listener<String> listener, ErrorListener errorListener,
			String phoneNumber, String accountPhoneNumber, String code) {
		makeRequest(RequestType.VALIDATE_DEVICE, listener, errorListener, phoneNumber,
				accountPhoneNumber, code);
	}
	
	public static void removeDevice(Listener<String> listener, ErrorListener errorListener,
			String phoneNumber, String accountPhoneNumber, String password) {
		makeRequest(RequestType.REMOVE_DEVICE, listener, errorListener, phoneNumber,
				accountPhoneNumber, password);
	}

	public static void logData(Listener<String> listener, ErrorListener errorListener,
			String phoneNumber, Date date, int hour, long bytes) {
		makeRequest(RequestType.LOG_DATA, listener, errorListener,
				phoneNumber, formatDate(date), String.valueOf(hour), String.valueOf(bytes));
	}

	public static void requestDeviceData(Listener<String> listener, ErrorListener errorListener,
			String phoneNumber, Date beginDate, Date endDate) {
		makeRequest(RequestType.REQUEST_DEVICE_DATA, listener, errorListener, phoneNumber,
				formatDate(beginDate), formatDate(endDate));
	}
	
	public static void requestAccountData(Listener<String> listener, ErrorListener errorListener,
			String phoneNumber, Date beginDate, Date endDate) {
		makeRequest(RequestType.REQUEST_ACCOUNT_DATA, listener, errorListener, phoneNumber,
				formatDate(beginDate), formatDate(endDate));
	}

	public static void requestDeviceSettings(Listener<String> listener, ErrorListener errorListener,
			String phoneNumber) {
		makeRequest(RequestType.REQUEST_DEVICE_SETTINGS, listener, errorListener, phoneNumber);
	}

	public static void requestAccountSettings(Listener<String> listener, ErrorListener errorListener,
			String phoneNumber) {
		makeRequest(RequestType.REQUEST_ACCOUNT_SETTINGS, listener, errorListener,
				phoneNumber);
	}

	public static void updateDeviceSetting(Listener<String> listener, ErrorListener errorListener,
			String phoneNumber, String setting, String value) {
		makeRequest(RequestType.UPDATE_DEVICE_SETTING, listener, errorListener,
				phoneNumber, setting, value);
	}

	public static void updateAccountSetting(Listener<String> listener, ErrorListener errorListener,
			String phoneNumber, String password, String setting, String value) {
		makeRequest(RequestType.UPDATE_ACCOUNT_SETTING, listener, errorListener,
				phoneNumber, password, setting, value);
	}

	public static void validDevice(Listener<String> listener, ErrorListener errorListener,
			String phoneNumber) {
		makeRequest(RequestType.VALID_DEVICE, listener, errorListener, phoneNumber);
	}

	public static void validAccount(Listener<String> listener, ErrorListener errorListener,
			String phoneNumber, String password) {
		makeRequest(RequestType.VALID_ACCOUNT, listener, errorListener,
				phoneNumber, password);
	}

	public static String formatDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		return format.format(date);
	}
}
