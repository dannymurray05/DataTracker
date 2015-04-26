package datatrackerclient.sessionmanagement;


public class SessionManager {

	public static enum SessionStatus {
		LOGGED_IN,
		LOGGED_OUT,
		DEVICE_ONLY, //no login required, attached to user account
		;
	}
}
