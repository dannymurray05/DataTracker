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
	private Context bindingContext;
	private boolean mobileDataEnabled = false;

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
				mobileDataEnabled = false;
				propertyChangeHandler.firePropertyChange(MOBILE_DATA_DISABLED, true, mobileDataEnabled);
				//Toast.makeText(context, "You are connected on Wifi",Toast.LENGTH_LONG).show();
				//context.stopService(new Intent (context,MobileDataUsageTracker.class));
			}
			if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
				mobileDataEnabled = true;
				propertyChangeHandler.firePropertyChange(MOBILE_DATA_ENABLED, false, mobileDataEnabled);
				//Toast.makeText(context,"You are now connected to mobile data", Toast.LENGTH_LONG).show();
				//context.startService(new Intent (context,MobileDataUsageTracker.class));
			}
		}	
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setMobileDataEnabled(boolean enabled) {
		boolean success = true;
		try {
			final ConnectivityManager conman = (ConnectivityManager) bindingContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			final Class conmanClass = Class.forName(conman.getClass().getName());
			final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
			iConnectivityManagerField.setAccessible(true);
			final Object iConnectivityManager = iConnectivityManagerField.get(conman);
			final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
			final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
			setMobileDataEnabledMethod.setAccessible(true);

			setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
		}
		catch(Exception e) {
			success = false;
			e.printStackTrace();
		}

		if(success) {
			mobileDataEnabled = enabled;
			Toast.makeText(bindingContext, "Mobile data turned " + (enabled ? "on" : "off") + "!", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(bindingContext, "Failed to turn " + (enabled ? "on" : "off") + " mobile data!", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public IBinder peekService(Context myContext, Intent service) {
		bindingContext = myContext;
		return new NetworkChangeInterface();
	}

	public class NetworkChangeInterface extends Binder {
		public boolean isMobileDataEnabled() {
			return mobileDataEnabled;
		}

		public void setMobileDataEnabled(boolean enabled) {
			NetworkChangeReceiver.this.setMobileDataEnabled(enabled);
		}
	}
}