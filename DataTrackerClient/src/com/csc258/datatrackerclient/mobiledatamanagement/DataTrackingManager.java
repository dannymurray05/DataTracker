package com.csc258.datatrackerclient.mobiledatamanagement;

import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.csc258.datatrackerclient.servercommunications.ServerRequestHandler;
import com.csc258.datatrackerclient.sessionmanagement.SessionManager;

import datatrackerstandards.DataError;
import datatrackerstandards.DataTrackerConstants;

public class DataTrackingManager extends Service implements Runnable {
	public static final String TAG = "DataTrackingManager";

	private Handler handler = new Handler();
	private SessionManager session;

	private MobileDataUsageTracker dataTracker;
	
	private Set<String> backlogData;
	
	//public static final long DATA_COLLECTION_RATE = 3600000; //once an hour
	public static final long DATA_COLLECTION_RATE = 30000; //once every 30 seconds

	//for previously tracked data counts, but for whatever reason,
	//those counts were unable to be sent to the server 
	private static final String BACKLOG_FILE_NAME = "BacklogDataFile";
	private static final String BACKLOG_DATA = "BacklogData";
	private static final String DELIMITER = ":";
	
	@Override
	public void onCreate() {
		super.onCreate();
		dataTracker = new MobileDataUsageTracker();

		SharedPreferences backlogDataFile = getSharedPreferences(BACKLOG_FILE_NAME, MODE_PRIVATE);
		backlogData = backlogDataFile.getStringSet(BACKLOG_DATA, new LinkedHashSet<String>());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		session = SessionManager.getInstance(this, null);
		handler.postDelayed(this, DATA_COLLECTION_RATE);
		return START_STICKY;
	}
	
	private int hour = -1;
	private int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
	private int month = Calendar.getInstance().get(Calendar.MONTH);
	@Override
	public void run() {
		SharedPreferences backlogDataFile = getSharedPreferences(BACKLOG_FILE_NAME, MODE_PRIVATE);

		//gather new data and store it
		//Random r = new Random(); //TODO REMOVE!!!

		Calendar currentTime = Calendar.getInstance();
		//debug//////////////TODO
		//currentTime.setTimeInMillis(r.nextLong()); //DEBUG TODO REMOVE!!!!!!
		hour++;
		if(hour > 23) {
			hour = 0;
			day++;
			if(day > currentTime.getActualMaximum(Calendar.DAY_OF_MONTH)) {
				day = 1;
				++month;
			}
		}
		currentTime.set(Calendar.DAY_OF_MONTH, day);
		currentTime.set(Calendar.MONTH, month);
		//end debug///////////TODO
		synchronized(backlogData) { 
			backlogData.add(DataTrackerConstants.dateToString(currentTime.getTime())
					+ DELIMITER + String.valueOf(hour)//currentTime.get(Calendar.HOUR_OF_DAY) //TODO UNDO!!
					+ DELIMITER + String.valueOf(dataTracker.calculateData()));
			SharedPreferences.Editor editor = backlogDataFile.edit();
			editor.putStringSet(BACKLOG_DATA, backlogData);
			//finalize
			editor.commit();
		}
		
		clearBacklogData();
		handler.postDelayed(this, DATA_COLLECTION_RATE);
	}

	private void clearBacklogData() {
		for(String dataEntry : backlogData) {
			pushDataToCloud(dataEntry);
		}
	}

	private void pushDataToCloud(final String dataKey) {
		//date:hour:dataUsage
		String[] data = dataKey.split(DELIMITER);
		ServerRequestHandler.logData(
				new DataPushListener(dataKey), new DataPushErrorListener(dataKey),
				session.getDeviceNumber(), DataTrackerConstants.stringToDate(
						data[0]), Integer.valueOf(data[1]), Long.valueOf(data[2]));
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private class DataPushListener implements Listener<String> {
		String dataKey;

		public DataPushListener(String dataKey) {
			this.dataKey = dataKey;
		}

		@Override
		public void onResponse(String arg0) {
			DataError error = null;
			if(arg0 != null && arg0.isEmpty() == false) {
				try {
					error = DataError.valueOf(new String(arg0));
				}
				catch(Exception e) {
				}
			}
			if(error != null) {
				if(error.equals(DataError.HOUR_ALREADY_LOGGED)) {
					Log.d(TAG, error.getErrorMessage());
				}
				else {
					return;
				}
			}
			synchronized(backlogData) {
				SharedPreferences backlogDataFile = getSharedPreferences(BACKLOG_FILE_NAME, MODE_PRIVATE);
				SharedPreferences.Editor editor = backlogDataFile.edit();

				backlogData.remove(dataKey);
				editor.putStringSet(BACKLOG_DATA, backlogData);
				
				editor.commit();
			}
		}
	}

	private class DataPushErrorListener implements ErrorListener {
		String dataKey;

		public DataPushErrorListener(String dataKey) {
			this.dataKey = dataKey;
		}
		
		@Override
		public void onErrorResponse(VolleyError arg0) {
			VolleyLog.d("Data with key (%s) not pushed to server. "
				+ "Stored in backlog for future push attempts.", dataKey);
		}
	}
}
