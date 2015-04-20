package datatrackerclient.servercommunications.requests;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import datatrackerclient.servercommunications.ServerConstants;



public class LogDataRequest extends StringRequest {
	public static final String LOG_DATA_REQUEST_URL = ServerConstants.SERVER_ADDRESS + "/log_data";
	
	private String phoneNumber;
	private Date date;
	private int hour;
	private long bytes;

	public LogDataRequest(String phoneNumber, Date date, int hour, long bytes,
			Listener<String> successListener, ErrorListener errorListener) {
		super(Method.POST, LOG_DATA_REQUEST_URL, successListener, errorListener);
		this.phoneNumber = phoneNumber;
		this.date = date;
		this.hour = hour;
		this.bytes = bytes;
		setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

	}


	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		Map<String, String> params = new HashMap<String, String>();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		params.put(ServerConstants.PHONE_NUM_PARAM, phoneNumber);
		params.put(ServerConstants.DATE_PARAM, format.format(date));
		params.put(ServerConstants.HOUR_PARAM, String.valueOf(hour));
		params.put(ServerConstants.BYTES_PARAM, String.valueOf(bytes));
		return params;
	}
	
	

	public static class LogDataResponse implements Listener<String> {

		@Override
		public void onResponse(String arg0) {
			System.out.println(arg0.toString());
		}
	}



	public static class GenericErrorListener implements ErrorListener {
		@Override
		public void onErrorResponse(VolleyError error) {
			VolleyLog.d(LOG_DATA_REQUEST_URL, "Error: " + error.getMessage());
		}
	}
}
