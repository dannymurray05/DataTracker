package datatrackerstandards;

import java.util.Arrays;
import java.util.List;

public enum RequestType {
	REGISTER_ACCOUNT("/register_account", DataTrackerConstants.PHONE_NUM_PARAM,
			DataTrackerConstants.PASSWORD_PARAM, DataTrackerConstants.EMAIL_PARAM),
	VALIDATE_EMAIL("/validate_email", DataTrackerConstants.PHONE_NUM_PARAM,
			DataTrackerConstants.CODE_PARAM),
	REGISTER_DEVICE("/register_device", DataTrackerConstants.PHONE_NUM_PARAM,
			DataTrackerConstants.ACCOUNT_PHONE_NUM_PARAM),
	VALIDATE_DEVICE("/validate_device", DataTrackerConstants.PHONE_NUM_PARAM,
			DataTrackerConstants.ACCOUNT_PHONE_NUM_PARAM, DataTrackerConstants.CODE_PARAM),
	REMOVE_DEVICE("/remove_device", DataTrackerConstants.PHONE_NUM_PARAM,
			DataTrackerConstants.ACCOUNT_PHONE_NUM_PARAM, DataTrackerConstants.PASSWORD_PARAM),
	LOG_DATA("/log_data", DataTrackerConstants.PHONE_NUM_PARAM, DataTrackerConstants.DATE_PARAM,
			DataTrackerConstants.HOUR_PARAM, DataTrackerConstants.BYTES_PARAM),
	REQUEST_DEVICE_DATA("/request_device_data", DataTrackerConstants.PHONE_NUM_PARAM,
			DataTrackerConstants.BEGIN_DATE_PARAM, DataTrackerConstants.END_DATE_PARAM),
	REQUEST_ACCOUNT_DATA("/request_account_data", DataTrackerConstants.PHONE_NUM_PARAM,
			DataTrackerConstants.BEGIN_DATE_PARAM, DataTrackerConstants.END_DATE_PARAM),
	REQUEST_DEVICE_SETTINGS("/request_device_settings", DataTrackerConstants.PHONE_NUM_PARAM),
	REQUEST_ACCOUNT_SETTINGS("/request_account_settings", DataTrackerConstants.PHONE_NUM_PARAM),
	UPDATE_DEVICE_SETTING("/update_device_setting", DataTrackerConstants.PHONE_NUM_PARAM,
			DataTrackerConstants.SETTING_PARAM, DataTrackerConstants.VALUE_PARAM),
	UPDATE_ACCOUNT_SETTING("/update_account_setting", DataTrackerConstants.PHONE_NUM_PARAM,
			DataTrackerConstants.PASSWORD_PARAM, DataTrackerConstants.SETTING_PARAM, DataTrackerConstants.VALUE_PARAM),
	VALID_DEVICE("/valid_device", DataTrackerConstants.PHONE_NUM_PARAM),
	VALID_ACCOUNT("/valid_account", DataTrackerConstants.PHONE_NUM_PARAM, DataTrackerConstants.PASSWORD_PARAM),
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

	RequestType(String mapping, String... params) {
		this.mapping = mapping;
		this.method = 1; //POST
		paramKeys = Arrays.asList(params);
	}
}