package datatrackerclient.settingsmanagement;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

import com.example.datatrackerclient.R;

import datatrackerclient.DataTracker;
import datatrackerclient.sessionmanagement.SessionManager;
import datatrackerstandards.settings.AccountSetting;
import datatrackerstandards.settings.DeviceSetting;

/**
 * Displays settings specific to the account admin.
 * Additionally displays a list of all user devices associate with the account.
 * Each device is displayed using a device_settings.xml layout. 
 * @author danny
 *
 */
public class AccountSettingsDisplay extends Fragment implements PropertyChangeListener, OnSeekBarChangeListener, OnClickListener {
	
	DataTracker parent;
	SettingsManager settingsManager;
	
	//Settings widgets
	TextView phoneDisplay;
	TextView quotaDisplay;
	SeekBar quotaBar;
	TextView thresholdDisplay;
	SeekBar thresholdBar;
	NumberPicker billingCyclePicker;
	
	//device fragments map
	Map<String, DeviceSettingsDisplay> deviceDisplayMap; //device phone# to device display

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
		
		quotaBar = (SeekBar)view.findViewById(R.id.quotaSeekBar);
		thresholdBar = (SeekBar)view.findViewById(R.id.thresholdSeekBar);
		quotaBar.setMax(1 << 22); //in KB, so about 4 GB max...
		thresholdBar.setMax(100); //for precision to YYY.YY%
		quotaBar.setOnSeekBarChangeListener(this);	
		thresholdBar.setOnSeekBarChangeListener(this);	

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
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.parent = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		settingsManager = SettingsManager.getInstance(parent, this);
		deviceDisplayMap = new LinkedHashMap<String, DeviceSettingsDisplay>();
	}

	@Override
	public void onStart() {
		super.onStart();


		//this.getChildFragmentManager().beginTransaction().add(new , tag).commit();
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
		//After attempt to save changes, setting values get sent back
		//from the server. If the server failed to make the changes,
		//this will effectively revert those changes locally.
		else {
			String[] setting = property.split(":");
			if(setting.length <= 1 || !setting[0].equals("account")) {
				return;
			}
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

	public void setSettingDisplay(DeviceSetting setting) {
		switch(setting) {
			case AUTO_SHUTOFF:
				//setThresholdDisplay(Integer.class.cast(settingsManager.getAccountSetting(setting)));
				break;
			case QUOTA:
				break;
			case THRESHOLD:
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
				settingsManager.setAccountSetting(setting, quotaBar.getProgress());
				break;
			case THRESHOLD:
				settingsManager.setAccountSetting(setting, thresholdBar.getProgress());
				break;
			default:
				break;
		}
	}

	
	public void saveSetting(DeviceSetting setting) {
		//TODO: get device view id or phone number, look it up in a list (list not yet coded)
		switch(setting) {
			case AUTO_SHUTOFF:
				break;
			case QUOTA:
				break;
			case THRESHOLD:
				break;
			default:
				break;
		}
	}

	private void setBillingCycleDisplay(int start) {
		billingCyclePicker.setValue(start);
	}

	private void setQuotaDisplay(int quota) {
		quotaBar.setProgress(quota);
		quotaDisplay.setText(String.valueOf(quotaBar.getProgress()));
	}

	private void setThresholdDisplay(int threshold) {
		thresholdBar.setProgress(threshold);
		thresholdDisplay.setText(String.valueOf(thresholdBar.getProgress()));
	}

	//TODO get each device fragment to implement this and call their parent fragment if they have one
	//all the bars will be disabled for user settings so they won't call anything
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		//if from user, update all other quota seekBars so sum of subquotas equals total quota
		//otherwise ignore
		if(fromUser) {
			if(seekBar == quotaBar) {
				//TODO update quotas to make always equal to account quota total
				//Log.d("SettingsDisplay", "Updating quotas...");
				quotaDisplay.setText(String.valueOf(quotaBar.getProgress()));
			}
			else if(seekBar == thresholdBar) {
				thresholdDisplay.setText(String.valueOf(thresholdBar.getProgress()));
			}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
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
					fragTxn.remove(deviceDisplayMap.get(removedDevice)); 
					deviceDisplayMap.remove(removedDevice);
				}

				//Add new devices as device display fragments
				for(String deviceNum : devicesJSONMap.keySet()) {
					deviceDisplay = deviceDisplayMap.get(deviceNum);
					if(deviceDisplay == null) {
						//add new device display;
						deviceDisplay = new DeviceSettingsDisplay(deviceNum);
						deviceDisplayMap.put(deviceNum, deviceDisplay);
						fragTxn.add(R.id.devices, deviceDisplay);
					}
				}
				fragTxn.commitAllowingStateLoss();
			}
		});
				
		for(DeviceSettingsDisplay display : deviceDisplayMap.values()) {
			display.syncWithLocalSettings();
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
		if(v.findViewById(R.id.saveSettingsButton) == v) {
			saveSettings();
		}
		else if(v.findViewById(R.id.revertSettingsButton) == v) {
			syncWithLocalSettings();
		}
	}

}
