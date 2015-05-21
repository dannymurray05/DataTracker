package com.csc258.datatrackerclient.mobiledatamanagement;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.csc258.datatrackerclient.mobiledatamanagement.NetworkChangeReceiver.NetworkChangeInterface;
import com.csc258.datatrackerclient.servercommunications.ServerRequestHandler;
import com.csc258.datatrackerclient.sessionmanagement.SessionManager;
import com.csc258.datatrackerclient.settingsmanagement.SettingsManager;

import datatrackerstandards.settings.AccountSetting;
import datatrackerstandards.settings.DeviceSetting;

public class DataUsageManager extends Service implements Runnable, PropertyChangeListener, ServiceConnection {
	public static final String TAG = "DataUsageManager";
	private Handler mHandler = new Handler();
	private DataUsageBinder mDataUsageBinder = new DataUsageBinder();

	boolean timerStarted = false;
	private SettingsUpdateTimer updateTimer;
	
	//listen for session changes so the proper device data is being calculated
	private SessionManager session;
	//listen for settings changes so data is updated to reflect latest changes
	private SettingsManager settings;
	//listen for network state changes and control network
	//to make sure mobile data usage isn't overused
	private NetworkChangeInterface networkChangeInterface;

	private String devicePhoneNumber = "";
	private long accountUsage = 0;
	private Map<String, Long> deviceDataUsageMap = new HashMap<String, Long>();
	private Calendar cycleBegin = Calendar.getInstance();
	private Calendar cycleEnd = Calendar.getInstance();
	private int billingCyclePeriod = 0;
	private int accountQuota = 0;
	private int accountThreshold = 0;
	private boolean deviceAutoShutOff;
	private int deviceQuota = 0;
	private int deviceThreshold = 0;

	private PropertyChangeSupport propertyChangeHandler = new PropertyChangeSupport(this);
	
	//public static int USAGE_CALCULATION_RATE = 1800000; //every half hour
	public static int USAGE_CALCULATION_RATE = 30000;
	
	public static int UPDATE_DELAY = 3000; //every 3 seconds
	
	//private static String ACCOUNT_DATA_FILE = "accountDataStorageFile";
	//private static String USER_DATA_FILE = "userDataStorageFile";
	
	public static final String DATA_USAGE_RECALCULATED = "dataUsageRecalculated";
	public static final String DEVICE_QUOTA_REACHED = "deviceQuotaReached";
	

	public static long NOTIFICATION_RATE = 600000; //notify every ten minutes (For demo)
	private long lastNotification = 0;
	
	@Override
	public void onCreate() {
		super.onCreate();
		session = SessionManager.getInstance(this, this, SessionManager.SESSION_STATUS);

		settings = SettingsManager.getInstance(this, null);
		settings.addListener(this, "account:" + AccountSetting.QUOTA.name());
		settings.addListener(this, "account:" + AccountSetting.THRESHOLD.name());
		settings.addListener(this, "account:" + AccountSetting.BILLING_CYCLE.name());
		
		updateTimer = new SettingsUpdateTimer(UPDATE_DELAY, 1000);
		
		//SharedPreferences accountDataFile = getSharedPreferences(ACCOUNT_DATA_FILE, MODE_PRIVATE);

		/*settings.addListener(this, "device:" + DeviceSetting.AUTO_SHUTOFF.name());
		settings.addListener(this, "device:" + DeviceSetting.QUOTA.name());
		settings.addListener(this, "device:" + DeviceSetting.THRESHOLD.name());*/
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mDataUsageBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		//bind to service to get notifications and grab data as needed
		Intent networkChangeReceiverIntent = new Intent(this, NetworkChangeReceiver.class);
		this.bindService(networkChangeReceiverIntent, this, Context.BIND_AUTO_CREATE);

        mHandler.postDelayed(this, 0);
        return START_STICKY;
	}
	

	private void turnOffData() {
		Log.d(TAG, "THRESHOLD REACHED! TURNING OFF MOBILE DATA USAGE!!!");
		
		networkChangeInterface.setMobileDataEnabled(false);
	}

	@Override
	public void run() {
		gatherData();

        mHandler.postDelayed(this, USAGE_CALCULATION_RATE);
	}
	
	private void gatherData() {
		updateSettings(); //make sure the latest settings are being used
		if(session.isLoggedOut() || !calculateBillingPeriod()) {
			return;
		}

		//account admin and account user will see same type of data
		//-account overall usage
		//-individual device usage
		//-no other device's specific usage will be displayed.
		ServerRequestHandler.requestAccountData(new DataListener(), new DataErrorListener(),
				session.getDeviceNumber(), cycleBegin.getTime(), cycleEnd.getTime());
	
	}

	private boolean calculateBillingPeriod() {
		if(billingCyclePeriod == 0) {
			mHandler.postDelayed(this, USAGE_CALCULATION_RATE);
			return false;
		}

		Calendar currentTime = Calendar.getInstance();
		cycleBegin = Calendar.getInstance();
		cycleEnd = Calendar.getInstance();
		cycleBegin.set(Calendar.DAY_OF_MONTH, billingCyclePeriod);
		cycleEnd.set(Calendar.DAY_OF_MONTH, billingCyclePeriod);
		if(currentTime.get(Calendar.DAY_OF_MONTH) < billingCyclePeriod) {
			cycleBegin.set(Calendar.MONTH, currentTime.get(Calendar.MONTH) - 1);
			cycleEnd.set(Calendar.MONTH, currentTime.get(Calendar.MONTH));
		}
		else {
			cycleBegin.set(Calendar.MONTH, currentTime.get(Calendar.MONTH));
			cycleEnd.set(Calendar.MONTH, currentTime.get(Calendar.MONTH) + 1);
		}	

		return true;
	}

	private void calculateDataUsage(JSONArray usageHistory) {

		//TODO Calculate usage by adding up all hourly records
		extractDataHistory(usageHistory);
		calculateTotalDataUsage();
		

		propertyChangeHandler.firePropertyChange(DATA_USAGE_RECALCULATED, null, accountUsage);
		
		//TODO If device is over quota and auto turn-off is on for this device, turn off mobile data.
		String devicePhoneNumber = session.getDeviceNumber();
		Long deviceUsage = deviceDataUsageMap.get(devicePhoneNumber);
		int deviceUsageKB = deviceUsage == null ? 0 : (int)(deviceUsage / 1000l);
		
		if(deviceUsageKB >= deviceThreshold) {
			long currentTime = System.currentTimeMillis();
			session = SessionManager.getInstance(this, this, SessionManager.SESSION_STATUS);
			if((session.isLoggedIn() || session.isDeviceOnly())
					&& currentTime - lastNotification > NOTIFICATION_RATE) {
				notifyOverThreshold();
				lastNotification = currentTime;
			}	
		}
		
		//check if over quota
		if(deviceUsageKB >= deviceQuota && deviceAutoShutOff) {
			//if over quota and control over data usage is given, shut off data usage
			turnOffData();
			propertyChangeHandler.firePropertyChange(DEVICE_QUOTA_REACHED, null, deviceUsageKB);
		}
	}
	
	private void notifyOverThreshold() {
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        //.setSmallIcon(null)
		        .setContentTitle("My notification")
		        .setContentText("Hello World!");
		NotificationManager mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, mBuilder.build());
	}

	private void calculateTotalDataUsage() {
		accountUsage = 0;
		for(Long deviceUsage : deviceDataUsageMap.values()) {
			accountUsage += deviceUsage;
		}
	}
	
	private void extractDataHistory(JSONArray usageHistory) {
		JSONObject record;
		JSONArray usageData;
		String phoneNumber;
		int hourValue;
		deviceDataUsageMap.clear();
		for(int i = 0; i < usageHistory.length(); i++) {
			try {
				record = usageHistory.getJSONObject(i);
				phoneNumber = record.getString("phoneNumber");
				usageData = record.getJSONArray("usageData");

				if(deviceDataUsageMap.get(phoneNumber) == null) {
					deviceDataUsageMap.put(phoneNumber, 0l);
				}
				long sum = 0;
				for(int j = 0; j < usageData.length(); j++) {
					hourValue = usageData.getInt(j);
					if(hourValue != -1)
						sum += hourValue;
				}
				deviceDataUsageMap.put(phoneNumber,
						deviceDataUsageMap.get(phoneNumber) + sum);
			}
			catch(JSONException e) {
				e.printStackTrace();
			}
		}
		System.out.println(deviceDataUsageMap.toString());
	}
	
	private void updateSettings() {
		billingCyclePeriod = (int)settings.getAccountSetting(AccountSetting.BILLING_CYCLE);
		accountQuota = (int)settings.getAccountSetting(AccountSetting.QUOTA);
		accountThreshold = (int)settings.getAccountSetting(AccountSetting.THRESHOLD);
		deviceQuota = (int)settings.getDeviceSetting(session.getDeviceNumber(), DeviceSetting.QUOTA);
		deviceThreshold = (int)settings.getDeviceSetting(session.getDeviceNumber(), DeviceSetting.THRESHOLD);
	}

	private class DataListener implements Listener<String> {

		@Override
		public void onResponse(String arg0) {
			if(arg0 != null && arg0.isEmpty() == false) {
				JSONArray deviceHistory;
				try {
					deviceHistory = new JSONArray(arg0);
				} catch (JSONException e) {
					e.printStackTrace();
					Log.d(TAG, "Invalid data retrieved from server!");
					return;
				}

				Log.d(TAG, "DEVICE HISTORY: " + deviceHistory.toString());
				
				calculateDataUsage(deviceHistory);
			}
		}
	}
	
	private class DataErrorListener implements ErrorListener {

		@Override
		public void onErrorResponse(VolleyError arg0) {
			if(arg0 != null) {
				Log.d(TAG, arg0.getMessage() == null ? "" : arg0.getMessage());
				Log.d(TAG, "Failed to retrieve data from server!");
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName = event.getPropertyName();
		if(propertyName.equals(SessionManager.SESSION_STATUS)) {
			String oldNumber = devicePhoneNumber;
			devicePhoneNumber = session.getDeviceNumber();

			for(DeviceSetting setting : DeviceSetting.values()) {
				settings.removeListener(this, oldNumber + ":" + setting.name());
				settings.addListener(this, devicePhoneNumber + ":" + setting.name());
			}
			
			restartUpdateCountdown();
			Log.d(TAG, "TESTING");
			//gatherData();
		}
		else {
			String[] settingInfo = propertyName.split(":");
			if(settingInfo.length < 2) {
				return;
			}
			String type = settingInfo[0];
			String settingName = settingInfo[1];
			
			//all the settings are updated at once when save button is clicked...
			//so really only need to listen on one
			if(type.equals("account")) {
				AccountSetting setting = AccountSetting.valueOf(settingName);
				switch(setting) {
					case BILLING_CYCLE:
						billingCyclePeriod = (int)event.getNewValue();
						restartUpdateCountdown();
						break;
					case QUOTA:
						accountQuota = (int)event.getNewValue();
						restartUpdateCountdown();
						break;
					case THRESHOLD:
						accountThreshold = (int)event.getNewValue();
						restartUpdateCountdown();
						break;
					default:
						break;
				
				}
			}
			else if(type.equals(devicePhoneNumber)) {
				DeviceSetting setting = DeviceSetting.valueOf(settingName);
				switch(setting) {
					case AUTO_SHUTOFF:
						deviceAutoShutOff = (boolean)event.getNewValue();
						restartUpdateCountdown();
						break;
					case QUOTA:
						deviceQuota = (int)event.getNewValue();
						restartUpdateCountdown();
						break;
					case THRESHOLD:
						deviceThreshold = (int)event.getNewValue();
						restartUpdateCountdown();
						break;
					default:
						break;
				}
			}
		}
	}

	public class DataUsageBinder extends Binder {

		DataUsageManager getManagerService(PropertyChangeListener listener) {
			return getDUMService(listener, new String[0]);
		}

		DataUsageManager getDUMService(PropertyChangeListener listener, String... property) {
			if(property == null) {
				propertyChangeHandler.removePropertyChangeListener(listener);
				propertyChangeHandler.addPropertyChangeListener(listener);
			}
			else {
				for(int i = 0; i < property.length; i++) {
					propertyChangeHandler.removePropertyChangeListener(property[i], listener);
					propertyChangeHandler.addPropertyChangeListener(property[i], listener);
				}
			}
			return DataUsageManager.this;
		}
	}

	public class SettingsUpdateTimer extends CountDownTimer {

		public SettingsUpdateTimer(long millisInFuture, long millisUntilFinished) {
			super(millisInFuture, millisUntilFinished);
		}

		@Override
		public void onFinish() {
			timerStarted = false;
			gatherData();
		}

		@Override
		public void onTick(long millisUntilFinished) {
		}
	}

	public void restartUpdateCountdown() {
		if(timerStarted) {
			updateTimer.cancel();
		}
		
		updateTimer.start();
		timerStarted = true;
	}

	//getters
	public long getAccountDataUsage() {
		return accountUsage;
	}

	public Long getDeviceDataUsage() {
		Long usage = deviceDataUsageMap.get(devicePhoneNumber);
		return usage == null ? 0l : usage;
	}

	public Map<String, Long> getDeviceDataUsageMap() {
		return deviceDataUsageMap;
	}

	public Date getCycleBeginDate() {
		return cycleBegin.getTime();
	}

	public Date getCycleEndDate() {
		return cycleBegin.getTime();
	}

	public int getBillingCyclePeriod() {
		return billingCyclePeriod;
	}
	
	public int getAccountQuota() {
		return accountQuota;
	}
	
	public int getAccountThreshold() {
		return accountThreshold;
	}
	
	public int getDeviceQuota() {
		return deviceQuota;
	}
	
	public int getDeviceThreshold() {
		return deviceThreshold;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		networkChangeInterface = (NetworkChangeInterface)service;
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		networkChangeInterface = null;
	}
}
