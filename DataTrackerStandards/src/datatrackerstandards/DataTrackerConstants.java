package datatrackerstandards;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataTrackerConstants {
	public static final String SERVER_ADDRESS = "http://98.255.169.237:8080";
	public static final String PHONE_NUM_PARAM = "phoneNumber";
	public static final String ACCOUNT_PHONE_NUM_PARAM = "accountPhoneNumber";
	public static final String PASSWORD_PARAM = "password";
	public static final String EMAIL_PARAM = "email";
	public static final String CODE_PARAM = "code";
	public static final String DATE_PARAM = "date";
	public static final String HOUR_PARAM = "hour";
	public static final String BYTES_PARAM = "bytes";
	public static final String SETTING_PARAM = "setting";
	public static final String VALUE_PARAM = "value";
	public static final String DATE_BEGIN_PARAM = "dateBegin";
	public static final String DATE_END_PARAM = "dateEnd";

	public static enum DeviceValidationError {
		DEVICE_NOT_FOUND,
		PENDING_ACCOUNT_VALIDATION,
		;
	}

	public static enum AccountValidationError {
		ACCOUNT_NOT_FOUND,
		INCORRECT_PHONE_NUMBER_OR_PASSWORD,
		;
	}
	
    public static String dateToString(Date date) {
    	if(date == null) {
    		return null;
    	}
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    	String dateStr = formatter.format(date);
		return dateStr;
    }   
    
    public static Date stringToDate(String dateStr) {
    	if(dateStr == null) {
    		return null;
    	}
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    	Date date = null;
		try {
			date = formatter.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return date;
		}
		
		return date;
    }
}
