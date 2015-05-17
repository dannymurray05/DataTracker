package com.csc258.datatrackerclient.mobiledatamanagement;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.csc258.datatrackerclient.R;
import com.csc258.datatrackerclient.mobiledatamanagement.DataUsageManager.DataUsageBinder;

public class DataUsageDisplay extends Fragment implements PropertyChangeListener, ServiceConnection  {
	private Activity parent;
	private DataUsageManager dataUsageManager;
	
	private TextView accountDataUsageText;
	private ProgressBar accountDataUsageBar;
	private TextView deviceDataUsageText;
	private ProgressBar deviceDataUsageBar;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.datatracking_display, container, false);
		
		accountDataUsageText = (TextView)view.findViewById(R.id.accountDataUsageDisplay);
		accountDataUsageBar = (ProgressBar)view.findViewById(R.id.accountDataUsageBar);
		deviceDataUsageText = (TextView)view.findViewById(R.id.deviceDataUsageDisplay);
		deviceDataUsageBar = (ProgressBar)view.findViewById(R.id.deviceDataUsageBar);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.parent = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		
		//bind to service to get notifications and grab data as needed
		Intent dataUsageIntent = new Intent(parent, DataUsageManager.class);
		parent.bindService(dataUsageIntent, this, Context.BIND_AUTO_CREATE);

	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getPropertyName();
		if(property.equals(DataUsageManager.DATA_USAGE_RECALCULATED)) {
			//set account usage bar
			int accountUsage = (int)(dataUsageManager.getAccountDataUsage() / 1000);
			int accountQuota = dataUsageManager.getAccountQuota();
			int accountThreshold = dataUsageManager.getAccountThreshold();
			setAccountDataUsageQuota(accountQuota);
			setAccountDataUsageDisplay(accountUsage);
			setAccountDataUsageThreshold(accountThreshold);
			
			//set device usage bar
			int deviceUsage = (int)(dataUsageManager.getDeviceDataUsage() / 1000);
			int deviceQuota = dataUsageManager.getDeviceQuota();
			int deviceThreshold = dataUsageManager.getDeviceThreshold();
			setDeviceDataUsageQuota(deviceQuota);
			setDeviceDataUsageDisplay(deviceUsage);
			setDeviceDataUsageThreshold(deviceThreshold);
		}
		else if(property.equals(DataUsageManager.DEVICE_QUOTA_REACHED)) {
			
		}
	}

	public void setAccountDataUsageDisplay(int dataUsage) {
		accountDataUsageBar.setProgress(dataUsage);
		accountDataUsageText.setText(String.valueOf(dataUsage));
	}
	
	public void setAccountDataUsageQuota(int dataUsageMax) {
		accountDataUsageBar.setMax(dataUsageMax);
	}
	
	public void setAccountDataUsageThreshold(int threshold) {
		accountDataUsageBar.setSecondaryProgress(
				(threshold * accountDataUsageBar.getProgress()) / 100);
		//TODO how to set threshold line color to red??
		//dataUsageBar.setSecondaryProgressTintList();
	}
	
	public void setDeviceDataUsageDisplay(int dataUsage) {
		deviceDataUsageBar.setProgress(dataUsage);
		deviceDataUsageText.setText(String.valueOf(dataUsage));
	}
	
	public void setDeviceDataUsageQuota(int dataUsageMax) {
		deviceDataUsageBar.setMax(dataUsageMax);
	}
	
	public void setDeviceDataUsageThreshold(int threshold) {
		deviceDataUsageBar.setSecondaryProgress(
				(threshold * deviceDataUsageBar.getProgress()) / 100);
		//TODO how to set threshold line color to red??
		//dataUsageBar.setSecondaryProgressTintList();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder dataUsageBinder) {
		dataUsageManager = ((DataUsageBinder)dataUsageBinder).getDUMService(this,
				DataUsageManager.DATA_USAGE_RECALCULATED, DataUsageManager.DEVICE_QUOTA_REACHED);
		dataUsageManager.restartUpdateCountdown();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		dataUsageManager = null;
	}
}
