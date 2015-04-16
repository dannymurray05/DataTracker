package datatrackerclient.servercommunications.requests;

import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import datatrackerclient.servercommunications.ServerConstants;



public class NewUserRequest extends StringRequest {
	public static final String NEW_USER_REQUEST_URL = ServerConstants.SERVER_ADDRESS + "/new_user";
	
	private String phoneNumber;
	private String password;
	private String email;

	public NewUserRequest(String phoneNumber, String password, String email,
			Listener<String> successListener, ErrorListener errorListener) {
		super(Method.POST, NEW_USER_REQUEST_URL, successListener, errorListener);
		this.phoneNumber = phoneNumber;
		this.password = password;
		this.email = email;
		setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

	}


	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		Map<String, String> params = new HashMap<String, String>();
		params.put(ServerConstants.PHONE_NUM_PARAM, phoneNumber);
		params.put(ServerConstants.PASSWORD_PARAM, password);
		params.put(ServerConstants.EMAIL_PARAM, email);
		return params;
	}
	
	

	public static class NewUserResponse implements Listener<String> {

		@Override
		public void onResponse(String arg0) {
			System.out.println(arg0.toString());
		}
	}



	public static class GenericErrorListener implements ErrorListener {
		@Override
		public void onErrorResponse(VolleyError error) {
			VolleyLog.d(NEW_USER_REQUEST_URL, "Error: " + error.getMessage());
		}
	}
}
