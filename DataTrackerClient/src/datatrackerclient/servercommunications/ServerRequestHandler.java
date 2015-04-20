package datatrackerclient.servercommunications;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import com.android.volley.toolbox.StringRequest;

import datatrackerclient.servercommunications.requests.LogDataRequest;
import datatrackerclient.servercommunications.requests.NewUserRequest;

public class ServerRequestHandler {
	//private static Logger serverRequestLogger = Logger.getLogger("ServerRequestHandlerLogger");
    public static final String TAG = ServerRequestHandler.class
            .getSimpleName();
	
	public static void newUser(String phoneNumber, String password, String email) {
		StringRequest request = new NewUserRequest(phoneNumber, password, email,
				new NewUserRequest.NewUserResponse(), new NewUserRequest.GenericErrorListener());
		NetworkController.getInstance().addToRequestQueue(request, NewUserRequest.NEW_USER_REQUEST_URL);
		logData(phoneNumber, Calendar.getInstance().getTime(), 1, 2048);
	}

	public static void logData(String phoneNumber, Date date, int hour, long bytes) {
		StringRequest request = new LogDataRequest(phoneNumber, date, hour, bytes,
				new LogDataRequest.LogDataResponse(), new LogDataRequest.GenericErrorListener());
		NetworkController.getInstance().addToRequestQueue(request, LogDataRequest.LOG_DATA_REQUEST_URL);
	}
	
	/*
	public static JSONObject requestData(String phoneNumber, Date dateBegin, Date dateEnd) {
		return makeRequest(createRequestMapping(requestDataStr, phoneNumParam, phoneNumber, dateBeginParam,
				dateBegin.toString(), dateEndParam, dateEnd.toString()));
	}*/
}
