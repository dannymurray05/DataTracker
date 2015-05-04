package datatrackerstandards;

public enum DeviceRegistrationStatus {
	ACCOUNT_NOT_FOUND("No account found with given phone number!", false),
	NEW_DEVICE_PENDING_VALIDATION("User created and account membership requested."
			+ "Account administrator must accept request by email.", true),
	ACCOUNT_CHANGE_PENDING_VALIDATION("Account membership change requested."
			+ "Account administrator must accept request by email.", true),
	SUCCESS("Account membership granted.", true)
	;

	public final String statusMessage;
	public final boolean success;

	DeviceRegistrationStatus(String errorMessage, boolean success) {
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