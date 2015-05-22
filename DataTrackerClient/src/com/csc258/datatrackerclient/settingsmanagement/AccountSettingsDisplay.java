package com.csc258.datatrackerclient.settingsmanagement;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.csc258.datatrackerclient.DataTracker;
import com.csc258.datatrackerclient.R;
import com.csc258.datatrackerclient.sessionmanagement.SessionManager;

import datatrackerstandards.settings.AccountSetting;

/**
 * Displays settings specific to the account admin.
 * Additionally displays a list of all user devices associate with the account.
 * Each device is displayed using a device_settings.xml layout. 
 * @author danny
 *
 */
public class AccountSettingsDisplay extends Fragment implements PropertyChangeListener, OnSeekBarChangeListener, OnClickListener {
	
	private DataTracker parent;
	private SettingsManager settingsManager;
	private SessionManager sessionManager;
	
	private String accountPhoneNumber;
	
	//Settings widgets
	private TextView phoneDisplay;
	private TextView quotaDisplay;
	private SeekBar accountQuotaBar;
	private TextView thresholdDisplay;
	private SeekBar accountThresholdBar;
	private NumberPicker billingCyclePicker;
	
	//device fragments map
	private Map<String, DeviceSettingsDisplay> deviceDisplayMap; //device phone# to device display

	private int oldQuota = 0;
	public static final int MAX_QUOTA = 1 << 18; //4MB - for testing!

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.account_settings, container, false);

		Button save = (Button)view.findViewById(R.id.saveSettingsButton);
		Button revert = (Button)view.findViewById(R.id.revertSettingsButton);
		Button logout = (Button)view.findViewById(R.id.logoutButton);
		save.setOnClickListener(this);
		revert.setOnClickListener(this);
		logout.setOnClickListener(parent);
		
		phoneDisplay = (TextView)view.findViewById(R.id.accountPhoneNumber);
		quotaDisplay = (TextView)view.findViewById(R.id.quotaDisplay);
		thresholdDisplay = (TextView)view.findViewById(R.id.thresholdDisplay);
		phoneDisplay.setText(SessionManager.getInstance(parent, null).getAccountNumber());
		
		accountQuotaBar = (SeekBar)view.findViewById(R.id.quotaSeekBar);
		accountThresholdBar = (SeekBar)view.findViewById(R.id.thresholdSeekBar);
		accountQuotaBar.setMax(MAX_QUOTA); //in KB, so about 4 GB max...
		accountThresholdBar.setMax(100); //for percentage 0-100%
		accountQuotaBar.setOnSeekBarChangeListener(this);	
		accountThresholdBar.setOnSeekBarChangeListener(this);	

		billingCyclePicker = (NumberPicker)view.findViewById(R.id.billingCyclePicker);
		billingCyclePicker.setMinValue(1);
		billingCyclePicker.setMaxValue(28);
		billingCyclePicker.setWrapSelectorWheel(true);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.parent = (DataTracker)activity;

		settingsManager = SettingsManager.getInstance(parent, this, SettingsManager.SETTINGS);
		for(AccountSetting setting : AccountSetting.values()) {
			settingsManager.addListener(this, "account:" + setting.name());
		}

		sessionManager = SessionManager.getInstance(parent, this, SessionManager.SESSION_STATUS);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.parent = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		deviceDisplayMap = new LinkedHashMap<String, DeviceSettingsDisplay>();
	}

	@Override
	public void onStart() {
		super.onStart();
		syncWithLocalSettings();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getPropertyName();

		/*syncs all settings with local settings
		 * - done after SettingsManager syncs with server */
		if(property.equals(SettingsManager.SETTINGS)) {
			syncWithLocalSettings();
			return;
		}
		else if(property.equals(SessionManager.SESSION_STATUS)) {
			setAccountNumber(sessionManager.getAccountNumber());
		}
		//Manage multipart properties (delimited parts by ":")
		else {
			String[] setting = property.split(":");
			if(setting.length <= 1) {
				return;
			}

			//After attempt to save changes, setting values get sent back
			//from the server. If the server failed to make the changes,
			//this will effectively revert those changes locally.
			if(setting[0].equals("account")) {
				AccountSetting changedSetting = null;
				try {
					changedSetting = AccountSetting.valueOf(setting[1]);
				}
				catch(Exception e) {
				}
				
				if(changedSetting != null) {
					setSettingDisplay(changedSetting);
				}
			}
			
			else if(setting[0].equals(DeviceSettingsDisplay.QUOTA_BAR)) {
				normalizeQuotas(setting[1], (int)event.getOldValue(), (int)event.getNewValue(), false);
			}
		}
	}

	public void normalizeQuotas(String deviceNum, int oldValue, int newValue, boolean accountQuotaChanged) {
		normalizeQuotas(deviceNum, oldValue - newValue, accountQuotaChanged);
	}

	/* For total quota, change is dispersed among all account devices and follows the change
	 * For a sub quota, change is counteracted in the opposite direction for all other sub quotas*/
	public void normalizeQuotas(String deviceNum, int change, boolean accountQuotaChanged) {
		if(deviceDisplayMap.size() == 0)
			return;
		if(accountQuotaChanged) {
			change = -change;
		}

		int transferrableDevices = deviceDisplayMap.size();
		
		transferrableDevices -= accountQuotaChanged ? 0 : 1;
		Set<DeviceSettingsDisplay> emptyQuotas = new HashSet<DeviceSettingsDisplay>();
		final int totalQuota = accountQuotaBar.getProgress(); //TODO ????? wtf is this here for? shouldn't it be max, not progress?
		int changeToSpare = 0;

		/* Loop until all change is equalized to other bars
		 * Splits change between all non-user changed bars
		 * Until all change is gone. As change runs out of some bars
		 * more change happens to others.
		 */
		while(change != 0) {
			if(transferrableDevices == 0) { //dump all the rest of change on single device
				//either last device to have room for change or ONLY device in account
				for(DeviceSettingsDisplay deviceDisplay : deviceDisplayMap.values()) {
					if(emptyQuotas.contains(deviceDisplay)) {
						continue;
					}
					deviceDisplay.addToQuotaDisplay(change);
					break;
				}
				break;
			}
			int changePart = change / transferrableDevices;
			int changeLeftover = change - (changePart * transferrableDevices);

			if(accountQuotaChanged) {
				accountQuotaBar.setProgress(accountQuotaBar.getProgress() + changeLeftover);
				change -= changeLeftover;
			}

			for(DeviceSettingsDisplay deviceDisplay : deviceDisplayMap.values()) {
				if(emptyQuotas.contains(deviceDisplay)) {
					continue;
				}
				if(!deviceDisplay.getDevicePhoneNumber().equals(deviceNum)) {
					changeToSpare = deviceDisplay.getQuotaDisplay();
					changeToSpare = change > 0 ? totalQuota - changeToSpare : changeToSpare;
					if(changeToSpare < changePart) {
						deviceDisplay.addToQuotaDisplay(changeToSpare);
						change -= changeToSpare;
						--transferrableDevices;
						emptyQuotas.add(deviceDisplay);
					}
					else {
						deviceDisplay.addToQuotaDisplay(changePart);
						change -= changePart;
					}
				}
				else {
					deviceDisplay.addToQuotaDisplay(changeLeftover);
					change -= changeLeftover;
				}
			}
		}
	}

	public void setSettingDisplay(AccountSetting setting) {
		switch(setting) {
			case BILLING_CYCLE:
				setBillingCycleDisplay(Integer.class.cast((settingsManager.getAccountSetting(setting))));
				break;
			case QUOTA:
				setQuotaDisplay(Integer.class.cast(settingsManager.getAccountSetting(setting)));
				break;
			case THRESHOLD:
				setThresholdDisplay(Integer.class.cast(settingsManager.getAccountSetting(setting)));
				break;
			default:
				break;
		}
	}


	public void saveSetting(AccountSetting setting) {
		switch(setting) {
			case BILLING_CYCLE:
				settingsManager.setAccountSetting(setting, billingCyclePicker.getValue());
				break;
			case QUOTA:
				settingsManager.setAccountSetting(setting, accountQuotaBar.getProgress());
				break;
			case THRESHOLD:
				settingsManager.setAccountSetting(setting, accountThresholdBar.getProgress());
				break;
			default:
				break;
		}
	}

	private void setAccountNumber(String accountNum) {
		this.accountPhoneNumber = accountNum;
		phoneDisplay.setText(accountPhoneNumber);
	}

	private void setBillingCycleDisplay(int start) {
		billingCyclePicker.setValue(start);
	}

	private void setQuotaDisplay(int quota) {
		/*
		//need to know if quota increased/decreased to determine what order to
		//do balancing and setting of max values - if max is set first when
		//quota decreased, sub quotas could be cut off and come out of balance
		int oldValue = quota;
		if(quotaDisplay.getText().length() != 0) {
			oldValue = Integer.valueOf(quotaDisplay.getText().toString());
		}
		boolean quotaIncreased = oldValue < quota;
		quotaDisplay.setText(String.valueOf(accountQuotaBar.getProgress()));
		*/
		
		/*if(!quotaIncreased) {
			normalizeQuotas("", oldValue, quota, true);
		}
		for(DeviceSettingsDisplay display : deviceDisplayMap.values()) {
			display.setMaxQuota(quota);
		}
		if(quotaIncreased) {
			normalizeQuotas("", oldValue, quota, true);
		}*/

		accountQuotaBar.setProgress(quota);
		quotaDisplay.setText(String.valueOf(accountQuotaBar.getProgress()));
		//set large enough that nothing could go wrong
		for(DeviceSettingsDisplay display : deviceDisplayMap.values()) {
			display.setMaxQuota(AccountSettingsDisplay.MAX_QUOTA);
		}

		normalizeQuotas("", oldQuota, quota, true);

		//now resize max values down, now that it's safe
		for(DeviceSettingsDisplay display : deviceDisplayMap.values()) {
			display.setMaxQuota(quota);
		}
	}

	private void setThresholdDisplay(int threshold) {
		accountThresholdBar.setProgress(threshold);
		thresholdDisplay.setText(String.valueOf(accountThresholdBar.getProgress()));
	}

	//TODO get each device fragment to implement their own progress bar change listener
	//and call their parent fragment if they have one.
	//all the bars will be disabled for user login settings so they won't call anything
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		//if from user, update all other quota seekBars so sum of subquotas equals total quota
		//otherwise ignore
		if(fromUser) {
			if(seekBar == accountQuotaBar) {
				setQuotaDisplay(progress);
				oldQuota = accountQuotaBar.getProgress();
			}
			else if(seekBar == accountThresholdBar) {
				thresholdDisplay.setText(String.valueOf(accountThresholdBar.getProgress()));
			}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		if(seekBar == accountQuotaBar) {
			oldQuota = accountQuotaBar.getProgress();
		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}

	public void syncWithLocalSettings() {
		for(AccountSetting setting : AccountSetting.values()) {
			setSettingDisplay(setting);
		}

		//run display fragment management in a separate thread
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				Map<String, JSONObject> devicesJSONMap = settingsManager.getDevices();
				List<String> removedDevices = new LinkedList<String>();
				JSONObject device = null;
				DeviceSettingsDisplay deviceDisplay = null;

				FragmentManager fragmentManager = getFragmentManager();
				if(fragmentManager == null) {
					return;
				}
				FragmentTransaction fragTxn = fragmentManager.beginTransaction();

				//remove devices no longer part of account
				for(String deviceNum : deviceDisplayMap.keySet()) {
					device = devicesJSONMap.get(deviceNum);
					if(device == null) {
						removedDevices.add(deviceNum);
					}
				}
				for(String removedDevice : removedDevices) {
					DeviceSettingsDisplay display = deviceDisplayMap.get(removedDevice);
					fragTxn.remove(display);
					display.removePropertyChangeListener(AccountSettingsDisplay.this);
					deviceDisplayMap.remove(removedDevice);
				}

				//Add new devices as device display fragments
				for(String deviceNum : devicesJSONMap.keySet()) {
					deviceDisplay = deviceDisplayMap.get(deviceNum);
					if(deviceDisplay == null) {
						//add new device display;
						deviceDisplay = new DeviceSettingsDisplay(deviceNum);
						deviceDisplayMap.put(deviceNum, deviceDisplay);
						deviceDisplay.addPropertyChangeListener(AccountSettingsDisplay.this);
						fragTxn.add(R.id.devices, deviceDisplay);
					}
				}
				fragTxn.commitAllowingStateLoss();
			}
		});
		
		for(DeviceSettingsDisplay settingsDisplay : deviceDisplayMap.values()) {
			settingsDisplay.syncWithLocalSettings();
		}
	}
	
	public void saveSettings() {
		for(AccountSetting setting : AccountSetting.values()) {
			saveSetting(setting);
		}

		//save all device settings
		for(DeviceSettingsDisplay deviceDisplay : deviceDisplayMap.values())  {
			deviceDisplay.saveSettings();
		}
	}

	@Override
	public void onClick(View v) {
		if(v.findViewById(R.id.saveSettingsButton) == v
				&& sessionManager.getAccountNumber().equals(sessionManager.getDeviceNumber())
				&& sessionManager.getPassword().isEmpty() == false) {
			saveSettings();
		}
		else if(v.findViewById(R.id.revertSettingsButton) == v) {
			syncWithLocalSettings();
		}
	}
}
