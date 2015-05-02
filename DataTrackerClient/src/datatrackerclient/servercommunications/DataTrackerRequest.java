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

import datatrackerstandards.DataTrackerConstants;
import datatrackerstandards.RequestType;

public class DataTrackerRequest extends StringRequest {

	private RequestType requestType;
	private List<String> requestParams;

	public DataTrackerRequest(RequestType requestType, Listener<String> listener, ErrorListener errorListener, String... params) {
		super(requestType.getMethod(), DataTrackerConstants.SERVER_ADDRESS + requestType.getMapping(), listener, errorListener);
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
		return DataTrackerConstants.SERVER_ADDRESS + requestType.getMapping(); 
	}

	
	public static class GenericErrorListener implements ErrorListener {
		@Override
		public void onErrorResponse(VolleyError error) {
			Logger.getAnonymousLogger().log(Level.WARNING, "Error: " + error.getMessage());
		}
	}
}
