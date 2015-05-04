package datatrackerstandards;

public enum DeviceValidationStatus {
	DEVICE_NOT_FOUND("No user with the given phone number found!", false),
	NO_SERVER_RESPONSE("No response from server!", false),
	PENDING_ACCOUNT_VALIDATION("Device exists, but account membership still pending.", true),
	VALIDATED("User validated!", true),
	;

	public final String statusMessage;
	public final boolean success;

	DeviceValidationStatus(String errorMessage, boolean success) {
		this.statusMessage = errorMessage;
		this.success = success;
	}
	
	public String getStatusMessage() {
		return statusMessage;
	}
	
	public boolean getSuccess() {
		return success;
	}	
}