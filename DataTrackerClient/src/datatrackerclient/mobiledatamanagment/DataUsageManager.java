package datatrackerclient.mobiledatamanagment;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class DataUsageManager extends Service {

	Handler handler = new Handler();

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

}
