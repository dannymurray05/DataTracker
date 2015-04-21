package datatrackerclient.servercommunications;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

public class DataTrackerRequest extends StringRequest {


	public static enum RequestType {
		REGISTER_USER("/register_user", Method.POST, ServerConstants.PHONE_NUM_PARAM,
				ServerConstants.PASSWORD_PARAM, ServerConstants.EMAIL_PARAM),
		VALIDATE_EMAIL("/validate_email", Method.POST, ServerConstants.PHONE_NUM_PARAM,
				ServerConstants.CODE_PARAM),
		REGISTER_DEVICE("/register_device", Method.POST, ServerConstants.PHONE_NUM_PARAM,
				ServerConstants.USER_PHONE_NUM_PARAM),
		VALIDATE_DEVICE("/validate_device", Method.POST, ServerConstants.PHONE_NUM_PARAM,
				ServerConstants.USER_PHONE_NUM_PARAM, ServerConstants.CODE_PARAM),
		LOG_DATA("/log_data", Method.POST, ServerConstants.PHONE_NUM_PARAM, ServerConstants.DATE_PARAM,
				ServerConstants.HOUR_PARAM, ServerConstants.BYTES_PARAM),
		REQUEST_DEVICE_DATA("/request_device_data", Method.GET, ServerConstants.PHONE_NUM_PARAM,
				ServerConstants.DATE_BEGIN_PARAM, ServerConstants.DATE_END_PARAM),
		REQUEST_USER_DATA("/request_user_data", Method.GET, ServerConstants.PHONE_NUM_PARAM,
				ServerConstants.PASSWORD_PARAM, ServerConstants.DATE_BEGIN_PARAM, ServerConstants.DATE_END_PARAM),
		REQUEST_DEVICE_SETTINGS("/request_device_settings", Method.GET, ServerConstants.PHONE_NUM_PARAM),
		REQUEST_USER_SETTINGS("/request_user_settings", Method.GET, ServerConstants.PHONE_NUM_PARAM,
				ServerConstants.PASSWORD_PARAM),
		UPDATE_DEVICE_SETTING("/update_device_settings", Method.POST, ServerConstants.PHONE_NUM_PARAM,
				ServerConstants.SETTING_PARAM, ServerConstants.VALUE_PARAM),
		UPDATE_USER_SETTING("/update_user_settings", Method.POST, ServerConstants.PHONE_NUM_PARAM,
				ServerConstants.PASSWORD_PARAM, ServerConstants.SETTING_PARAM, ServerConstants.VALUE_PARAM),
		;

		private String mapping;
		private int method;
		private List<String> paramKeys;
		
		public String getMapping() {
			return mapping;
		}
		
		public int getMethod() {
			return method;
		}

		public List<String> getParamKeys() {
			return paramKeys;
		}

		RequestType(String mapping, int method, String... params) {
			this.mapping = mapping;
			this.method = method;
			paramKeys = Arrays.asList(params);
		}
	}

	private RequestType requestType;
	private List<String> requestParams;

	public DataTrackerRequest(RequestType requestType, Listener<String> listener, ErrorListener errorListener, String... params) {
		super(requestType.getMethod(), ServerConstants.SERVER_ADDRESS + requestType.getMapping(), listener, errorListener);
		if(params.length != requestType.getParamKeys().size()) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Parameter count given does not equals required amount for request type: " + requestType.name());
			System.exit(1);
		}
		else {
			requestParams = Arrays.asList(params);
		}
		
		this.requestType = requestType;
		
		setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		Map<String, String> params = new HashMap<String, String>();
		List<String> paramKeys = requestType.getParamKeys();
		for(int i = 0; i < paramKeys.size(); i++) {
			params.put(paramKeys.get(i), requestParams.get(i));
		}
		return params;
	}

	public String getURL() {
		return ServerConstants.SERVER_ADDRESS + requestType.getMapping(); 
	}

	
	public static class GenericErrorListener implements ErrorListener {
		@Override
		public void onErrorResponse(VolleyError error) {
			Logger.getAnonymousLogger().log(Level.WARNING, "Error: " + error.getMessage());
		}
	}
}
