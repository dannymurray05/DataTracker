package com.csc258.datatrackerclient.mobiledatamanagement;

import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class NetworkChangeReceiver extends BroadcastReceiver
{
	private PropertyChangeSupport propertyChangeHandler = new PropertyChangeSupport(this);
	public static final String MOBILE_DATA_ENABLED = "mobileDataEnabled";
	public static final String MOBILE_DATA_DISABLED = "mobileDataDisabled";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("Broadcast reciever","inside broadcast");

		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);     
		NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

		if (null != activeNetwork) {
			if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
				propertyChangeHandler.firePropertyChange(MOBILE_DATA_DISABLED, true, false);
				//Toast.makeText(context, "You are connected on Wifi",Toast.LENGTH_LONG).show();
				//context.stopService(new Intent (context,MobileDataUsageTracker.class));
			}
			if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
				propertyChangeHandler.firePropertyChange(MOBILE_DATA_ENABLED, false, true);
				//Toast.makeText(context,"You are now connected to mobile data", Toast.LENGTH_LONG).show();
				//context.startService(new Intent (context,MobileDataUsageTracker.class));
			}
		}	
	}
}