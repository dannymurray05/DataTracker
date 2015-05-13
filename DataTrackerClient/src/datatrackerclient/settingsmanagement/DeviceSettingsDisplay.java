package datatrackerclient.settingsmanagement;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.datatrackerclient.R;

import datatrackerstandards.settings.DeviceSetting;

public class DeviceSettingsDisplay extends Fragment implements PropertyChangeListener, OnSeekBarChangeListener, OnClickListener  {
	Activity parent;
	SettingsManager settingsManager;
	
	String devicePhoneNumber;
	
	//Settings widgets
	TextView phoneDisplay;
	TextView quotaDisplay;
	SeekBar quotaBar;
	TextView thresholdDisplay;
	SeekBar thresholdBar;
	CheckBox autoTurnOffSelection;
	
	Button removeDeviceButton;
	
	public DeviceSettingsDisplay(String phoneNumber) {
		this.devicePhoneNumber = phoneNumber;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.device_settings, container, false);

		phoneDisplay = (TextView)view.findViewById(R.id.devicePhoneNumber);
		quotaDisplay = (TextView)view.findViewById(R.id.quotaDisplay);
		thresholdDisplay = (TextView)view.findViewById(R.id.thresholdDisplay);
		phoneDisplay.setText(devicePhoneNumber);
		
		quotaBar = (SeekBar)view.findViewById(R.id.quotaSeekBar);
		thresholdBar = (SeekBar)view.findViewById(R.id.thresholdSeekBar);
		quotaBar.setOnSeekBarChangeListener(this);	
		thresholdBar.setOnSeekBarChangeListener(this);
		quotaBar.setMax(1 << 22); //in KB, so about 4 GB max...
		thresholdBar.setMax(100); //for precision to YYY.YY%
		
		autoTurnOffSelection = (CheckBox)view.findViewById(R.id.autoTurnOff);

		removeDeviceButton = (Button)view.findViewById(R.id.removeDeviceButton);
		removeDeviceButton.setOnClickListener(this);

		syncWithLocalSettings();

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.parent = activity;
		settingsManager = SettingsManager.getInstance(parent, this);
	}	

	@Override
	public void onDetach() {
		super.onDetach();
		this.parent = null;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
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
			if(setting.length <= 1 || !setting[0].equals("device")) {
				return;
			}
			DeviceSetting changedSetting = null;
			try {
				changedSetting = DeviceSetting.valueOf(setting[1]);
			}
			catch(Exception e) {
			}
			
			if(changedSetting != null) {
				setSettingDisplay(changedSetting);
			}
		}
	}
	
	public void setSettingDisplay(DeviceSetting setting) {
		switch(setting) {
			case AUTO_SHUTOFF:
				setAutoTurnOff(Boolean.class.cast(settingsManager.getDeviceSetting(devicePhoneNumber, setting)));
				break;
			case QUOTA:
				setQuotaDisplay(Integer.class.cast(settingsManager.getDeviceSetting(devicePhoneNumber, setting)));
				break;
			case THRESHOLD:
				setThresholdDisplay(Integer.class.cast(settingsManager.getDeviceSetting(devicePhoneNumber, setting)));
				break;
			default:
				break;
		}
	}

	public void saveSetting(DeviceSetting setting) {
		switch(setting) {
			case AUTO_SHUTOFF:
				settingsManager.setDeviceSetting(devicePhoneNumber, setting, autoTurnOffSelection.isChecked());	
				break;
			case QUOTA:
				settingsManager.setDeviceSetting(devicePhoneNumber, setting, quotaBar.getProgress());	
				break;
			case THRESHOLD:
				settingsManager.setDeviceSetting(devicePhoneNumber, setting, thresholdBar.getProgress());
				break;
			default:
				break;
		}
	}

	private void setQuotaDisplay(int quota) {
		quotaBar.setProgress(quota);
		quotaDisplay.setText(String.valueOf(quotaBar.getProgress()));
	}

	private void setThresholdDisplay(int threshold) {
		thresholdBar.setProgress(threshold);
		thresholdDisplay.setText(String.valueOf(thresholdBar.getProgress()));
	}
	
	private void setAutoTurnOff(boolean autoTurnOff) {
		this.autoTurnOffSelection.setChecked(autoTurnOff);
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
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}

	public void syncWithLocalSettings() {
		if(settingsManager == null) {
			return;
		}

		for(DeviceSetting setting : DeviceSetting.values()) {
			setSettingDisplay(setting);
		}
	}
	
	public void saveSettings() {
		for(DeviceSetting setting : DeviceSetting.values()) {
			saveSetting(setting);
		}
	}

	@Override
	public void onClick(View v) {
		if(v.equals(this.removeDeviceButton)) {
			settingsManager.removeDevice(devicePhoneNumber);
		}
	}
}
