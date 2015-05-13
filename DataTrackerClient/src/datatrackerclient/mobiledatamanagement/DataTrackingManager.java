package datatrackerclient.mobiledatamanagement;

import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;

import datatrackerclient.servercommunications.ServerRequestHandler;
import datatrackerstandards.DataTrackerConstants;
import datatrackerstandards.RequestType;

public class DataTrackingManager extends Service implements Runnable {

	Handler handler = new Handler();

	MobileDataUsageTracker dataTracker;
	
	Set<String> backlogData;
	
	public static final long DATA_COLLECTION_RATE = 3600000; //once an hour
	//public static final long DATA_COLLECTION_RATE = 5000; //once every 5 seconds

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

		
		handler.postDelayed(this, DATA_COLLECTION_RATE);
		return START_STICKY;
	}
	
	@Override
	public void run() {
		SharedPreferences backlogDataFile = getSharedPreferences(BACKLOG_FILE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = backlogDataFile.edit();

		//gather new data and store it
		Random r = new Random(); //TODO REMOVE!!!

		Calendar currentTime = Calendar.getInstance();
		currentTime.setTimeInMillis(r.nextLong()); //DEBUG TODO REMOVE!!!!!!
		backlogData.add(DataTrackerConstants.dateToString(currentTime.getTime())
				+ DELIMITER + String.valueOf(r.nextInt(24))//currentTime.get(Calendar.HOUR_OF_DAY) //TODO UNDO!!
				+ DELIMITER + String.valueOf(dataTracker.calculateData()));
		editor.putStringSet(BACKLOG_DATA, backlogData);
		//finalize
		editor.commit();
		
		clearBacklogData(editor);
		handler.postDelayed(this, DATA_COLLECTION_RATE);
	}

	private void clearBacklogData(SharedPreferences.Editor editor) {
		for(String dataEntry : backlogData) {
			pushDataToCloud(dataEntry);
		}
	}

	private void pushDataToCloud(final String dataKey) {
		TelephonyManager phoneManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		//date:hour:dataUsage
		String[] data = dataKey.split(DELIMITER);
		ServerRequestHandler.logData(
				new DataPushListener(dataKey),
				new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						VolleyLog.d("Data with key (%s) not pushed to server. "
								+ "Stored in backlog for future push attempts.", dataKey);
					}
				},
				phoneManager.getLine1Number(), DataTrackerConstants.stringToDate(
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
			synchronized(backlogData) {
				SharedPreferences backlogDataFile = getSharedPreferences(BACKLOG_FILE_NAME, MODE_PRIVATE);
				SharedPreferences.Editor editor = backlogDataFile.edit();

				backlogData.remove(dataKey);
				editor.putStringSet(BACKLOG_DATA, backlogData);
				
				editor.commit();
			}
		}
	}
}
