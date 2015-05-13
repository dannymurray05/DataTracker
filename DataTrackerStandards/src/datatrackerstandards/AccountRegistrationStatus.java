package datatrackerstandards;


public enum AccountRegistrationStatus {
	INVALID_NUMBER("Invalid phone number: Phone number must be 9 digits (including area code).", false),
	INVALID_PASSWORD("Invalid password: The password must be at least 8 characters long and include at least one non-alphanumeric character.", false),
	INVALID_EMAIL("Invalid email: Email address must be a valid email address.", false),
	ACCOUNT_ALREADY_EXISTS("Account already exists: The phone number given is already in use.", false),
	EMAIL_ALREADY_EXISTS("Email already exists: The email given is already in use.", false),
	NO_SERVER_RESPONSE("No response from server!", false),
	SUCCESS("Account successfully created.", true),
	;

	public final String statusMessage;
	public final boolean success;

	AccountRegistrationStatus(String errorMessage, boolean success) {
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
