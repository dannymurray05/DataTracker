package datatrackerclient.mobiledatamanagement;

import java.util.Calendar;

import datatrackerclient.servercommunications.ServerRequestHandler;
import datatrackerclient.sessionmanagement.SessionManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;

public class DataUsageManager extends Service implements Runnable {

	Handler handler = new Handler();
	
	public static int USAGE_CALCULATION_RATE = 1800000; //every half hour
	
	private static String ACCOUNT_DATA_FILE = "accountDataStorageFile";
	private static String USER_DATA_FILE = "userDataStorageFile";

	int billingCyclePeriod = 0;
	boolean accountAdmin = false;

	@Override
	public void onCreate() {
		super.onCreate();
		
		SharedPreferences accountDataFile = getSharedPreferences(ACCOUNT_DATA_FILE, MODE_PRIVATE);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

        handler.postDelayed(this, 0);
        return START_STICKY;
	}

	@Override
	public void run() {
		if(billingCyclePeriod == 0) {
			handler.postDelayed(this, USAGE_CALCULATION_RATE);
			return;
		}

		SessionManager session = SessionManager.getInstance(this, null);

		Calendar currentTime = Calendar.getInstance();
		/*Calendar cycleBegin = currentTime.get
		
		if(session.isLoggedIn()) {
			ServerRequestHandler.requestAccountData(listener, errorListener,
					session.getPhoneNumber(), session.getPassword(), beginDate, endDate);
		}
		else if(session.isDeviceOnly()){
			ServerRequestHandler.requestDeviceData(listener, errorListener, session.getPhoneNumber(),
					beginDate, endDate);
			
		}*/

        handler.postDelayed(this, USAGE_CALCULATION_RATE);
	}


	public void setBillingCyclePeriod(int billingCyclePeriod) {
		this.billingCyclePeriod = billingCyclePeriod;
	}
}
