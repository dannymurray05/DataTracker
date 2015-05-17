package datatrackerstandards;

public enum DataError {
	INVALID_DATA("Invalid data for log request."),
	INVALID_HOUR("Hour out of range for log request."),
	HOUR_ALREADY_LOGGED("Hour of data already logged."),
	INVALID_BYTE_VALUE("Invalid byte amount."),
	INVALID_DATE_FORMAT("Invalid date format."),
	;

	public final String errorMessage;

	DataError(String message) {
		errorMessage = message;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
}