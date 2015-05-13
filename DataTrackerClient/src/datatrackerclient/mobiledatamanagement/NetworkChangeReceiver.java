package datatrackerclient.mobiledatamanagement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;


public class NetworkChangeReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		
			Log.v("Broadcast reciever","inside broadcast");
			
			ConnectivityManager cm1 = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);     
		     NetworkInfo activeNetwork = cm1.getActiveNetworkInfo();
		     {	  
		     if (null != activeNetwork)
		     {
		        if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
		        {
		        	Toast.makeText(context, "You are connected on Wifi",Toast.LENGTH_LONG).show();
		        	context.stopService(new Intent (context,MobileDataUsageTracker.class));
		        }
		        if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
		        	Toast.makeText(context,"You are now connected to mobile data", Toast.LENGTH_LONG).show();
		            context.startService(new Intent (context,MobileDataUsageTracker.class));
		     }
		     }	
			
	}
}