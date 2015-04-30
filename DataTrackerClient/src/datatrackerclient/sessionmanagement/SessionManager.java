package datatrackerclient.sessionmanagement;


public class SessionManager {

	public static enum SessionStatus {
		LOGGED_IN,
		LOGGED_OUT,
		DEVICE_ONLY, //no login required, just a member (non-owner) of an account
		;
	}
}
