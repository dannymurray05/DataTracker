package datatrackerclient.sessionmanagement;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import datatrackerclient.servercommunications.ServerRequestHandler;
import datatrackerstandards.DataTrackerConstants.AccountValidationError;
import datatrackerstandards.DataTrackerConstants.DeviceValidationError;


public class SessionManager {

	public static enum SessionStatus {
		LOGGED_IN,
		LOGGED_OUT,
		DEVICE_ONLY, //no login required, just a member (non-owner) of an account
		;
	}

	private static SessionManager mInstance;
	private static Context mContext;
	private static PropertyChangeSupport propertyChangeHandler;
	
	private SessionStatus sessionStatus = SessionStatus.LOGGED_OUT;
	private String phoneNumber = null;
	private String password = null;
	
	public static final String SESSION_STATUS = "sessionStatus";
	public static final String DEVICE_LOGIN_ERROR = "deviceLoginError";
	public static final String ACCOUNT_LOGIN_ERROR = "accountLoginError";
	public static final String DEVICE_SIGNUP_ERROR = "deviceSignupError";
	public static final String ACCOUNT_SIGNUP_ERROR = "accountSignupError";
	
	private static final String SESSION_FILE = "sessionFile";
	private static final String PHONE_NUMBER = "phoneNumber";
	private static final String PASSWORD = "password";

	protected SessionManager(Context context, PropertyChangeListener listener) {
		propertyChangeHandler = new PropertyChangeSupport(this);
		propertyChangeHandler.addPropertyChangeListener(listener);
		mContext = context;
		SharedPreferences sessionFile = context.getSharedPreferences(SESSION_FILE, Context.MODE_PRIVATE);
		sessionStatus = SessionStatus.valueOf(sessionFile.getString(SESSION_STATUS, SessionStatus.LOGGED_OUT.name()));
		phoneNumber = sessionFile.getString(PHONE_NUMBER, "5555555555");
		password = sessionFile.getString(PASSWORD, "password");
	}
	
	public static SessionManager getInstance(Context context, PropertyChangeListener listener) {
		if(mInstance == null) {
			mInstance = new SessionManager(context, listener);
		}
		else if(!mContext.equals(context)) {
			mContext = context;
			propertyChangeHandler.addPropertyChangeListener(listener);
		}
		
		propertyChangeHandler.removePropertyChangeListener(
				propertyChangeHandler.getPropertyChangeListeners()[0]);
		propertyChangeHandler.addPropertyChangeListener(listener);
		
		return mInstance;
	}

	public boolean isLoggedIn() {
		return sessionStatus.equals(SessionStatus.LOGGED_IN);
	}

	public boolean isLoggedOut() {
		return sessionStatus.equals(SessionStatus.LOGGED_OUT);
	}

	public boolean isDeviceOnly() {
		return sessionStatus.equals(SessionStatus.DEVICE_ONLY);
	}

	public SessionStatus getSessionStatus() {
		return sessionStatus;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getPassword() {
		return password;
	}

	private void setSessionStatus(SessionStatus status) {
		sessionStatus = status;
		SharedPreferences sessionFile = mContext.getSharedPreferences(SESSION_FILE, Context.MODE_PRIVATE);
		Editor edit = sessionFile.edit();
		edit.putString(SESSION_STATUS, status.name());
		edit.commit();
		propertyChangeHandler.firePropertyChange(SESSION_STATUS, null, sessionStatus);
	}
	
	private void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		SharedPreferences sessionFile = mContext.getSharedPreferences(SESSION_FILE, Context.MODE_PRIVATE);
		Editor edit = sessionFile.edit();
		edit.putString(PHONE_NUMBER, phoneNumber);
		edit.commit();
	}

	private void setPassword(String password) {
		this.password = password;
		SharedPreferences sessionFile = mContext.getSharedPreferences(SESSION_FILE, Context.MODE_PRIVATE);
		Editor edit = sessionFile.edit();
		edit.putString(PASSWORD, password);
		edit.commit();
	}

	public void logIn() {
		if(sessionStatus.equals(SessionStatus.LOGGED_IN)) {
			ServerRequestHandler.validAccount(new AccountLoginListener(),
					new AccountLoginErrorListener(), phoneNumber, password);	
		}
		else if(sessionStatus.equals(SessionStatus.DEVICE_ONLY)) {
			ServerRequestHandler.validDevice(new DeviceLoginListener(),
					new DeviceLoginErrorListener(), phoneNumber);	
		}
		else {
			setSessionStatus(SessionStatus.LOGGED_OUT);
		}
	}

	public void logIn(final String phoneNumber, final String password) {
		if(password == null || password.isEmpty()) {
			setPhoneNumber(phoneNumber);
			ServerRequestHandler.validDevice(new DeviceLoginListener(),
					new DeviceLoginErrorListener(), phoneNumber);
		}
		else {
			setPhoneNumber(phoneNumber);
			setPassword(password);
			ServerRequestHandler.validAccount(new AccountLoginListener(),
					new AccountLoginErrorListener(), phoneNumber, password);

		}
	}

	public void signUp(final String phoneNumber, final String accountPhoneNumber,
			final String password, final String email) {
		if(password == null || password.isEmpty()) {
			setPhoneNumber(phoneNumber);
			ServerRequestHandler.registerDevice(new DeviceLoginListener(),
					new DeviceSignupErrorListener(), phoneNumber, accountPhoneNumber);
		}
		else {
			setPhoneNumber(phoneNumber);
			setPassword(password);
			ServerRequestHandler.registerAccount(new AccountLoginListener(),
					new AccountSignupErrorListener(), phoneNumber, password, email);
		}
	}

	public boolean logOut() {
		setSessionStatus(SessionStatus.LOGGED_OUT);
		return true;
	}


	private class DeviceLoginListener implements Listener<String> {
		@Override
		public void onResponse(String arg0) {
			setSessionStatus(SessionStatus.DEVICE_ONLY);
		}
	}
	
	private class AccountLoginListener implements Listener<String> {
		@Override
		public void onResponse(String arg0) {
			setSessionStatus(SessionStatus.LOGGED_IN);
		}
	}
	
	private class DeviceLoginErrorListener implements ErrorListener {
		@Override
		public void onErrorResponse(VolleyError arg0) {
			if(arg0.networkResponse != null) {
                DeviceValidationError error = DeviceValidationError.valueOf(new String(arg0.networkResponse.data));
                propertyChangeHandler.firePropertyChange(DEVICE_LOGIN_ERROR, null, error);
			}
			else {
                propertyChangeHandler.firePropertyChange(DEVICE_LOGIN_ERROR, null, DeviceValidationError.NO_SERVER_RESPONSE);
			}
		}
	}
	
	private class AccountLoginErrorListener implements ErrorListener {
		@Override
		public void onErrorResponse(VolleyError arg0) {
			if(arg0.networkResponse != null) {
				AccountValidationError error = AccountValidationError.valueOf(String.valueOf(arg0.networkResponse.data));
				propertyChangeHandler.firePropertyChange(ACCOUNT_LOGIN_ERROR, null, error);
			}
			else {
				propertyChangeHandler.firePropertyChange(ACCOUNT_LOGIN_ERROR, null, AccountValidationError.NO_SERVER_RESPONSE);
			}

		}
	}
	
	private class DeviceSignupErrorListener implements ErrorListener {
		@Override
		public void onErrorResponse(VolleyError arg0) {
			if(arg0.networkResponse != null) {
				DeviceValidationError error = DeviceValidationError.valueOf(new String(arg0.networkResponse.data));
				propertyChangeHandler.firePropertyChange(DEVICE_SIGNUP_ERROR, null, error);
			}
			else {
				propertyChangeHandler.firePropertyChange(DEVICE_SIGNUP_ERROR, null, DeviceValidationError.NO_SERVER_RESPONSE);
			}
		}
	}
	
	private class AccountSignupErrorListener implements ErrorListener {
		@Override
		public void onErrorResponse(VolleyError arg0) {
			if(arg0.networkResponse != null) {
				AccountValidationError error = AccountValidationError.valueOf(String.valueOf(arg0.networkResponse.data));
				propertyChangeHandler.firePropertyChange(ACCOUNT_SIGNUP_ERROR, null, error);
			}
			else {
				propertyChangeHandler.firePropertyChange(ACCOUNT_SIGNUP_ERROR, null, AccountValidationError.NO_SERVER_RESPONSE);
			}
		}
	}
}
