package com.csc258.datatrackerclient.sessionmanagement;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.csc258.datatrackerclient.servercommunications.ServerRequestHandler;

import datatrackerstandards.AccountRegistrationStatus;
import datatrackerstandards.AccountValidationStatus;
import datatrackerstandards.DeviceRegistrationStatus;
import datatrackerstandards.DeviceValidationStatus;


public class SessionManager {

	public static enum SessionStatus {
		LOGGED_OUT,
		LOGGED_IN,
		DEVICE_ONLY, //no login required, just a member (non-owner) of an account
		;
	}

	private static SessionManager mInstance;
	private Context mContext;
	private PropertyChangeSupport propertyChangeHandler;
	
	private SessionStatus sessionStatus = SessionStatus.LOGGED_OUT;
	private String phoneNumber = null;
	private String accountNumber = null;
	private String password = null;

	//property change strings
	public static final String SESSION_STATUS = "sessionStatus";
	public static final String DEVICE_LOGIN_ERROR = "deviceLoginError";
	public static final String ACCOUNT_LOGIN_ERROR = "accountLoginError";
	public static final String DEVICE_SIGNUP_ERROR = "deviceSignupError";
	public static final String ACCOUNT_SIGNUP_ERROR = "accountSignupError";
	public static final String DEVICE_LOGIN_SUCCESS = "deviceLoginSuccess";
	public static final String DEVICE_REGISTRATION_SUCCESS = "deviceRegistrationSuccess";

	//storage strings
	private static final String SESSION_FILE = "sessionFile";
	private static final String PHONE_NUMBER = "phoneNumber";
	private static final String ACCOUNT_NUMBER = "accountNumber";
	private static final String PASSWORD = "password";

	protected SessionManager(Context context, PropertyChangeListener listener) {
		propertyChangeHandler = new PropertyChangeSupport(this);
		propertyChangeHandler.addPropertyChangeListener(listener);
		mContext = context;
		SharedPreferences sessionFile = context.getSharedPreferences(SESSION_FILE, Context.MODE_PRIVATE);
		sessionStatus = SessionStatus.valueOf(sessionFile.getString(SESSION_STATUS, SessionStatus.LOGGED_OUT.name()));
		phoneNumber = sessionFile.getString(PHONE_NUMBER, "5555555555");
		accountNumber = sessionFile.getString(ACCOUNT_NUMBER, "5555555555");
		password = sessionFile.getString(PASSWORD, "");
	}
	
	public static SessionManager getInstance(Context context, PropertyChangeListener listener) {
		return getInstance(context, listener, null);
	}

	public static SessionManager getInstance(Context context, PropertyChangeListener listener, String property) {
		if(context == null) {
			return null;
		}
		if(mInstance == null) {
			mInstance = new SessionManager(context, listener);
		}
		else {
			if(!mInstance.getContext().equals(context)) {
				mInstance.setContext(context);
			}

			if(property == null) {
				mInstance.getPropertyChangeHandler().removePropertyChangeListener(listener);
				mInstance.getPropertyChangeHandler().addPropertyChangeListener(listener);
			}
			else {
				mInstance.getPropertyChangeHandler().removePropertyChangeListener(property, listener);
				mInstance.getPropertyChangeHandler().addPropertyChangeListener(property, listener);
			}
		}

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

	public String getDeviceNumber() {
		return phoneNumber;
	}

	public String getAccountNumber() {
		return accountNumber;
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
	
	private void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
		SharedPreferences sessionFile = mContext.getSharedPreferences(SESSION_FILE, Context.MODE_PRIVATE);
		Editor edit = sessionFile.edit();
		edit.putString(ACCOUNT_NUMBER, accountNumber);
		edit.commit();
	}

	private void setPassword(String password) {
		this.password = password;
		SharedPreferences sessionFile = mContext.getSharedPreferences(SESSION_FILE, Context.MODE_PRIVATE);
		Editor edit = sessionFile.edit();
		edit.putString(PASSWORD, password);
		edit.commit();
	}

	/**
	 * Logging in automatically. App attempts to do this at start
	 * with previous login information.
	 */
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

	/**
	 * Logging in manually (as opposed to automatically with stored info)
	 * @param phoneNumber
	 * @param password
	 */
	public void logIn(final String phoneNumber, final String password) {
		if(password == null || password.isEmpty()) {
			setPhoneNumber(phoneNumber);
			ServerRequestHandler.validDevice(new DeviceLoginListener(),
					new DeviceLoginErrorListener(), phoneNumber);
		}
		else {
			setPhoneNumber(phoneNumber);
			setAccountNumber(phoneNumber);
			setPassword(password);
			ServerRequestHandler.validAccount(new AccountLoginListener(),
					new AccountLoginErrorListener(), phoneNumber, password);
		}
	}

	//New user sign up
	public void signUp(final String phoneNumber, final String accountPhoneNumber) {
		setPhoneNumber(phoneNumber);
		setAccountNumber(accountPhoneNumber);
		ServerRequestHandler.registerDevice(new DeviceRegistrationListener(),
				new DeviceRegistrationErrorListener(), phoneNumber, accountPhoneNumber);
	}

	//New account sign up
	public void signUp(final String phoneNumber, final String password, final String email) {
		setPhoneNumber(phoneNumber);
		setAccountNumber(phoneNumber);
		setPassword(password);
		ServerRequestHandler.registerAccount(new AccountRegistrationListener(),
				new AccountRegistrationErrorListener(), phoneNumber, password, email);
	}

	public boolean logOut() {
		setSessionStatus(SessionStatus.LOGGED_OUT);
		return true;
	}

	private class DeviceLoginListener implements Listener<String> {
		@Override
		public void onResponse(String arg0) {
			if(arg0 != null) {
				String[] response = arg0.split(":");

				DeviceValidationStatus status = null;
				try {
					status = DeviceValidationStatus.valueOf(new String(response[0]));
				}
				catch(Exception e) {
					e.printStackTrace();
					return;
				}

                String accountPhoneNumber;
                if(response.length == 1 || response[1] == null) {
                	accountPhoneNumber = "";
                }
                else {
                	accountPhoneNumber = response[1];
                }
                
                setAccountNumber(accountPhoneNumber);
                propertyChangeHandler.firePropertyChange(DEVICE_LOGIN_SUCCESS, null, status);
			}
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
                DeviceValidationStatus error = null;
				try {
					error = DeviceValidationStatus.valueOf(new String(arg0.networkResponse.data));
				}
				catch(Exception e) {
					e.printStackTrace();
					return;
				}
                propertyChangeHandler.firePropertyChange(DEVICE_LOGIN_ERROR, null, error);
			}
			else {
                propertyChangeHandler.firePropertyChange(DEVICE_LOGIN_ERROR, null, DeviceValidationStatus.NO_SERVER_RESPONSE);
			}
			setSessionStatus(SessionStatus.LOGGED_OUT);
		}
	}
	
	private class AccountLoginErrorListener implements ErrorListener {
		@Override
		public void onErrorResponse(VolleyError arg0) {
			if(arg0.networkResponse != null) {
				AccountValidationStatus error;
				try {
					error = AccountValidationStatus.valueOf(new String(arg0.networkResponse.data));
				}
				catch(Exception e) {
					e.printStackTrace();
					return;
				}
				propertyChangeHandler.firePropertyChange(ACCOUNT_LOGIN_ERROR, null, error);
			}
			else {
				propertyChangeHandler.firePropertyChange(ACCOUNT_LOGIN_ERROR, null, AccountValidationStatus.NO_SERVER_RESPONSE);
			}
			setSessionStatus(SessionStatus.LOGGED_OUT);
		}
	}

	private class DeviceRegistrationListener implements Listener<String> {
		@Override
		public void onResponse(String arg0) {
			if(arg0 != null) {
				DeviceRegistrationStatus status;
				try {
					status = DeviceRegistrationStatus.valueOf(new String(arg0));
				}
				catch(Exception e) {
					e.printStackTrace();
					return;
				}
                propertyChangeHandler.firePropertyChange(DEVICE_REGISTRATION_SUCCESS, null, status);
			}
			setSessionStatus(SessionStatus.DEVICE_ONLY);
		}
	}
	
	private class AccountRegistrationListener implements Listener<String> {
		@Override
		public void onResponse(String arg0) {
			setSessionStatus(SessionStatus.LOGGED_IN);
		}
	}
	
	private class DeviceRegistrationErrorListener implements ErrorListener {
		@Override
		public void onErrorResponse(VolleyError arg0) {
			if(arg0.networkResponse != null) {
				DeviceRegistrationStatus error;
				try {
					error = DeviceRegistrationStatus.valueOf(new String(arg0.networkResponse.data));
				}
				catch(Exception e) {
					return;
				}
				propertyChangeHandler.firePropertyChange(DEVICE_SIGNUP_ERROR, null, error);
			}
			else {
				propertyChangeHandler.firePropertyChange(DEVICE_SIGNUP_ERROR, null, DeviceRegistrationStatus.NO_SERVER_RESPONSE);
			}
		}
	}
	
	private class AccountRegistrationErrorListener implements ErrorListener {
		@Override
		public void onErrorResponse(VolleyError arg0) {
			if(arg0.networkResponse != null) {
				AccountRegistrationStatus error;
				try {
					error = AccountRegistrationStatus.valueOf(new String(arg0.networkResponse.data));
				}
				catch(Exception e) {
					return;
				}
				propertyChangeHandler.firePropertyChange(ACCOUNT_SIGNUP_ERROR, null, error);
			}
			else {
				propertyChangeHandler.firePropertyChange(ACCOUNT_SIGNUP_ERROR, null, AccountRegistrationStatus.NO_SERVER_RESPONSE);
			}
		}
	}

	public Context getContext() {
		return mContext;
	}
	
	public void setContext(Context context) {
		mContext = context;
	}

	public PropertyChangeSupport getPropertyChangeHandler() {
		return propertyChangeHandler;
	}

	public void setPropertyChangeHandler(PropertyChangeSupport propertyChangeHandler) {
		this.propertyChangeHandler = propertyChangeHandler;
	}
}
