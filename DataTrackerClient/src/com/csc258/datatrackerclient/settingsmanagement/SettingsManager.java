package com.csc258.datatrackerclient.settingsmanagement;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.csc258.datatrackerclient.servercommunications.ServerRequestHandler;
import com.csc258.datatrackerclient.sessionmanagement.SessionManager;

import datatrackerstandards.settings.AccountSetting;
import datatrackerstandards.settings.DeviceSetting;

public class SettingsManager implements PropertyChangeListener {

	private static SettingsManager mInstance;
	private SessionManager session;
	private Context mContext;
	private PropertyChangeSupport propertyChangeHandler;

	private static final String SETTINGS_RESPONSE = "accountSettingsResponse";
	public static final String SETTINGS = "accountSettings";
	public static final String DEVICE_SETTINGS = "deviceSettings";
	
	private JSONObject accountJSON = new JSONObject();
	private Map<String, JSONObject> devicesMap = new LinkedHashMap<String, JSONObject>();
	
	/**
	 * Gets shared preferences files, unique to each user.
	 * Within that file, each user has all their settings
	 * Admins will also have a set of associated devices.
	 * @param context
	 * @param listener
	 */
	protected SettingsManager(Context context, PropertyChangeListener listener) {
		propertyChangeHandler = new PropertyChangeSupport(this);
		propertyChangeHandler.addPropertyChangeListener(SETTINGS_RESPONSE, this);
		propertyChangeHandler.addPropertyChangeListener(listener);
		mContext = context;
		session = SessionManager.getInstance(mContext, null);
	}

	public static SettingsManager getInstance(Context context, PropertyChangeListener listener) {
		return getInstance(context, listener, null);
	}
	
	public static SettingsManager getInstance(Context context, PropertyChangeListener listener, String property) {
		if(context == null) {
			return null;
		}
		if(mInstance == null) {
			mInstance = new SettingsManager(context, listener);
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

	public void addListener(PropertyChangeListener listener) {
		addListener(listener, null);
	}

	public void addListener(PropertyChangeListener listener, String property) {
		if(property == null) {
			propertyChangeHandler.addPropertyChangeListener(listener);
		}
		else {
			propertyChangeHandler.addPropertyChangeListener(property, listener);
		}
	}

	public void removeListener(PropertyChangeListener listener) {
		removeListener(listener, null);
	}

	public void removeListener(PropertyChangeListener listener, String property) {
		if(property == null) {
			propertyChangeHandler.removePropertyChangeListener(listener);
		}
		else {
			propertyChangeHandler.removePropertyChangeListener(property, listener);
		}
	}

	public void syncSettings() {
		ServerRequestHandler.requestAccountSettings(new Listener<String>() {
			@Override
			public void onResponse(String arg0) {
				Log.d("Settings", "Success getting account settings!");
				Log.d("Settings", arg0);
				JSONObject accountJSON = null;
				try {
					accountJSON = new JSONObject(arg0);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Log.d("Settings", accountJSON == null ? "" : accountJSON.toString());

				propertyChangeHandler.firePropertyChange(SETTINGS_RESPONSE, null, accountJSON);
			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				Log.d("Settings", "Error getting account settings!");
			}
		}, session.getAccountNumber());
		
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName = event.getPropertyName();
		Object property = event.getNewValue();
		JSONArray devicesArray;
		if(propertyName.equals(SETTINGS_RESPONSE)) {
			accountJSON = (JSONObject) property;
			syncAccountSettings(accountJSON);

			try {
				devicesArray = accountJSON.getJSONArray("devices");
			} catch (JSONException e1) {
				e1.printStackTrace();
				return;
			}
			
			JSONObject device = null;
			devicesMap.clear();
			for(int i = 0; i < devicesArray.length(); i++) {
				try {
					device = devicesArray.getJSONObject(i);
					syncDeviceSettings(device.getString("phoneNumber"), device);
					devicesMap.put(device.getString("phoneNumber"), device);
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}
			propertyChangeHandler.firePropertyChange(SETTINGS, null, null);
		}
	}

	protected void syncAccountSettings(JSONObject accountJSON) {
		SharedPreferences settingsFile = mContext.getSharedPreferences(
				getAccountSettingsFile(), Context.MODE_PRIVATE);
		Editor editor = settingsFile.edit();
		for(AccountSetting setting : AccountSetting.values()) {
			Object value = null;
			try {
				value = accountJSON.get(setting.getSettingField().getName());
			} catch (JSONException e) {
				e.printStackTrace();
				continue;
			}
			setLocalAccountSetting(editor, setting, value);
		}
		editor.commit();
	}

	protected void syncDeviceSettings(String phoneNumber, JSONObject deviceJSON) {
		SharedPreferences settingsFile = mContext.getSharedPreferences(
				getDeviceSettingsFile(phoneNumber), Context.MODE_PRIVATE);
		Editor editor = settingsFile.edit();
		for(DeviceSetting setting : DeviceSetting.values()) {
			Object value = null;
			try {
				value = deviceJSON.get(setting.getSettingField().getName());
			} catch (JSONException e) {
				e.printStackTrace();
				continue;
			}
			setLocalDeviceSetting(editor, setting, value);
		}
		editor.commit();	
	}

	public Object getAccountSetting(AccountSetting setting) {
		SharedPreferences settingsFile = mContext.getSharedPreferences(
				getAccountSettingsFile(), Context.MODE_PRIVATE);
		Object settingValue = null;
		switch(setting.getType()) {
            case BOOLEAN:
            	settingValue = settingsFile.getBoolean(setting.name(), (Boolean)setting.getDefaultValue());
                break;
            case INT:
            	settingValue = settingsFile.getInt(setting.name(), (Integer)setting.getDefaultValue());
                break;
            case LONG:
            	settingValue = settingsFile.getLong(setting.name(), (Long)setting.getDefaultValue());
                break;
            case STRING:
            	settingValue = settingsFile.getString(setting.name(), (String)setting.getDefaultValue());
                break;
            default:
                break;
		}
		return settingValue;
	}
	
	public void setAccountSetting(AccountSetting setting, Object value) {
		//listeners of the request fire property changes for settings UI to respond to
		Object oldValue = getAccountSetting(setting);
		ServerRequestHandler.updateAccountSetting(new AccountSettingsListener(setting, oldValue, value),
				new AccountSettingsErrorListener(setting, oldValue),
				session.getAccountNumber(), session.getPassword(),
				setting.name(), setting.getType().getClazz().cast(value).toString());
	}

	public void setLocalAccountSetting(AccountSetting setting, Object value) {
		setLocalAccountSetting(setting, null, value);
	}

	public void setLocalAccountSetting(AccountSetting setting, Object oldValue, Object value) {
		if(!oldValue.equals(value)) {
			SharedPreferences settingsFile = mContext.getSharedPreferences(
					getAccountSettingsFile(), Context.MODE_PRIVATE);
			Editor editor = settingsFile.edit();
			setLocalAccountSetting(editor, setting, value);
			editor.commit();
		}
		propertyChangeHandler.firePropertyChange("account:" + setting.name(), oldValue, value);
	}
	
	public void setLocalAccountSetting(Editor editor, AccountSetting setting, Object value) {
		switch(setting.getType()) {
            case BOOLEAN:
            	editor.putBoolean(setting.name(), (boolean)value);
                break;
            case INT:
            	editor.putInt(setting.name(), ((Number)value).intValue());
                break;
            case LONG:
            	editor.putLong(setting.name(), ((Number)value).longValue()); 
                break;
            case STRING:
            	editor.putString(setting.name(), value.toString());
                break;
            default:
                break;
		}
	}
	
	public Object getDeviceSetting(String phoneNumber, DeviceSetting setting) {
		SharedPreferences settingsFile = mContext.getSharedPreferences(
				getDeviceSettingsFile(phoneNumber), Context.MODE_PRIVATE);
		Object settingValue = null;
		switch(setting.getType()) {
            case BOOLEAN:
            	settingValue = settingsFile.getBoolean(setting.name(), (Boolean)setting.getDefaultValue());
                break;
            case INT:
            	settingValue = settingsFile.getInt(setting.name(), (Integer)setting.getDefaultValue());
                break;
            case LONG:
            	settingValue = settingsFile.getLong(setting.name(), (Long)setting.getDefaultValue());
                break;
            case STRING:
            	settingValue = settingsFile.getString(setting.name(), (String)setting.getDefaultValue());
                break;
            default:
                break;
		}
		return settingValue;
	}
	
	public void setDeviceSetting(String phoneNumber, DeviceSetting setting, Object value) {
		//listeners of the request fire property changes for settings UI to respond to
		Object oldValue = getDeviceSetting(phoneNumber, setting);
		ServerRequestHandler.updateDeviceSetting(new DeviceSettingsListener(
				phoneNumber, setting, oldValue, value),
				new DeviceSettingsErrorListener(phoneNumber, setting, oldValue), phoneNumber,
				setting.name(), setting.getType().getClazz().cast(value).toString());
	}

	public void setLocalDeviceSetting(String phoneNumber, DeviceSetting setting, Object value) {
		setLocalDeviceSetting(phoneNumber, setting, null, value);
	}

	public void setLocalDeviceSetting(String phoneNumber,
			DeviceSetting setting, Object oldValue, Object value) {
		SharedPreferences settingsFile = mContext.getSharedPreferences(
				getDeviceSettingsFile(phoneNumber), Context.MODE_PRIVATE);
		Editor editor = settingsFile.edit();
		setLocalDeviceSetting(editor, setting, value);
		editor.commit();
		//fire *device specific* property change for this setting
		propertyChangeHandler.firePropertyChange(phoneNumber + ":" + setting.name(), oldValue, value);
	}
	
	public void setLocalDeviceSetting(Editor editor, DeviceSetting setting, Object value) {
		switch(setting.getType()) {
            case BOOLEAN:
            	editor.putBoolean(setting.name(), (Boolean)value);
                break;
            case INT:
            	editor.putInt(setting.name(), (Integer)value);
                break;
            case LONG:
            	editor.putLong(setting.name(), (Long)value);
                break;
            case STRING:
            	editor.putString(setting.name(), (String)value);
                break;
            default:
                break;
		}
	}

	public void removeDevice(String deviceNum) {
		ServerRequestHandler.removeDevice(
				new Listener<String>() {
					@Override
					public void onResponse(String arg0) {
						Log.d("SettingsManager", "Device was removed! Syncing settings...");
						syncSettings();
					}
				},
				new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.d("SettingsManager", "Server did not remove device!");
					}
				},
				deviceNum, session.getAccountNumber(), session.getPassword());
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

	private String getAccountSettingsFile() {
		return SETTINGS + session.getAccountNumber();
	}
	
	private String getDeviceSettingsFile(String phoneNumber) {
		return DEVICE_SETTINGS + phoneNumber;
	}

	public Map<String, JSONObject> getDevices() {
		return devicesMap;
	}

	private class AccountSettingsListener implements Listener<String> {
		AccountSetting setting;
		Object oldValue;
		Object newValue;

		public AccountSettingsListener(AccountSetting setting,
				Object oldValue, Object newValue) {
			this.setting = setting;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		@Override
		public void onResponse(String response) {
			setLocalAccountSetting(setting, oldValue, newValue);
		}
	}

	private class AccountSettingsErrorListener implements ErrorListener {
		AccountSetting setting;
		Object oldValue;
		
		public AccountSettingsErrorListener(AccountSetting setting, Object oldValue) {
			this.setting = setting;
			this.oldValue = oldValue;
		}

		@Override
		public void onErrorResponse(VolleyError response) {
			setLocalAccountSetting(setting, oldValue, oldValue);
		}
	}

	private class DeviceSettingsListener implements Listener<String> {
		String phoneNumber;
		DeviceSetting setting;
		Object oldValue;
		Object newValue;

		public DeviceSettingsListener(String phoneNumber, DeviceSetting setting,
				Object oldValue, Object newValue) {
			this.setting = setting;
			this.oldValue = oldValue;
			this.newValue = newValue;
			this.phoneNumber = phoneNumber;
		}

		@Override
		public void onResponse(String response) {
			setLocalDeviceSetting(phoneNumber, setting, oldValue, newValue);
		}
	}

	private class DeviceSettingsErrorListener implements ErrorListener {
		String phoneNumber;
		DeviceSetting setting;
		Object oldValue;

		public DeviceSettingsErrorListener(String phoneNumber, DeviceSetting setting, Object oldValue) {
			this.phoneNumber = phoneNumber;
			this.setting = setting;
			this.oldValue = oldValue;
		}

		@Override
		public void onErrorResponse(VolleyError response) {
			setLocalDeviceSetting(phoneNumber, setting, oldValue, oldValue);
		}
	}	
}
