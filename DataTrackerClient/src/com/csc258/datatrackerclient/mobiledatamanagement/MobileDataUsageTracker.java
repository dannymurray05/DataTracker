package com.csc258.datatrackerclient.mobiledatamanagement;

import android.net.TrafficStats;

public class MobileDataUsageTracker
{
	private long initialReceivedBytes = 0;
	private long intialTransmittedBytes = 0;
	private long prevBytes = 0;
	private long totalBytes = 0;
	private long currentBytes = 0;

	public MobileDataUsageTracker() {       
		initialReceivedBytes = TrafficStats.getMobileRxBytes();
		intialTransmittedBytes = TrafficStats.getMobileTxBytes(); 
		//mHandler.postDelayed(calculate_data, 10000);
	}
	
	
	
	public long calculateData() {
		long rxBytes = TrafficStats.getMobileRxBytes() - initialReceivedBytes;
		long txBytes = TrafficStats.getMobileTxBytes() - intialTransmittedBytes;
			
		prevBytes = totalBytes;
		totalBytes = rxBytes + txBytes;
		currentBytes = totalBytes - prevBytes;

		return currentBytes;
	}

	/*
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {       
		//Toast.makeText(getBaseContext(), "Checking Service", Toast.LENGTH_LONG).show();
		received_bytes = TrafficStats.getMobileRxBytes();
		transmitted_bytes = TrafficStats.getMobileTxBytes(); 
		mHandler.postDelayed(calculate_data, 10000);
			
		return START_STICKY;
	}
	
	private final Runnable calculate_data = new Runnable() {
		public void run() {
			//long rxBytes=0, currentBytes=0;
			//Date date = Calendar.getInstance().getTime();
				
			long rxBytes = TrafficStats.getMobileRxBytes()- received_bytes;
			long txBytes = TrafficStats.getMobileTxBytes()- transmitted_bytes;
				
			//String dateString = Integer.toString(date);
			///Toast.makeText(getApplicationContext(), dateString, Toast.LENGTH_LONG).show();
			prevBytes=totalBytes;
				
			totalBytes = rxBytes + txBytes;
			
			//prevBytes=rxBytes;
			currentBytes = totalBytes - prevBytes;
			//prevBytes=rxBytes;
			//rxBytes=rxBytes/1024;
			String usage_data = Long.toString(currentBytes);
			Toast.makeText(getApplicationContext(), usage_data, Toast.LENGTH_LONG).show();
			
			mHandler.postDelayed(calculate_data, 10000);
		}
	};
	*/
}