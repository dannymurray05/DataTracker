package datatrackerstandards;

public enum AccountValidationStatus {
	ACCOUNT_NOT_FOUND("No account with the given phone number found!", false),
	INCORRECT_PHONE_NUMBER_OR_PASSWORD("Invalid phone number or password!", false),
	NO_SERVER_RESPONSE("No response from the server!", false),
	VALIDATED("Account validated!", true),
	;

	public final String statusMessage;
	public final boolean success;

	AccountValidationStatus(String errorMessage, boolean success) {
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