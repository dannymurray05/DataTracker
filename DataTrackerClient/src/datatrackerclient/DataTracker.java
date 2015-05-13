package datatrackerclient;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.example.datatrackerclient.R;

import datatrackerclient.sessionmanagement.Login;
import datatrackerclient.sessionmanagement.SessionManager;
import datatrackerclient.settingsmanagement.AccountSettingsDisplay;
import datatrackerclient.settingsmanagement.SettingsManager;
import datatrackerstandards.settings.AccountSetting;

public class DataTracker extends FragmentActivity implements NumberPicker.OnValueChangeListener, OnClickListener{

	private TextView tv;
    static Dialog d ;
	public static final int LOGIN_REQUEST = 0b1;
	//DatePicker getdate;
	
	//public static final long SETTINGS_SYNC_MAX_RATE = 1800000; //30 minutes
	public static final long SETTINGS_SYNC_MAX_RATE = 30000; //30 minutes
	public static final long DATA_SYNC_MAX_RATE = 300000; //5 minutes
	private long lastSettingsSync = 0;
	private long lastDataSync = 0;
	
	@Override
	protected void onStart() {
		super.onStart();
		startActivityForResult(new Intent(this, Login.class), LOGIN_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		SessionManager.SessionStatus loginResult = SessionManager.SessionStatus.values()[resultCode];
		SettingsManager settings = null;
		switch(requestCode) {
			case LOGIN_REQUEST:
				switch(loginResult) {
                    case DEVICE_ONLY:
                    	//set up user settings
                    	break;
                    case LOGGED_IN:
                    	//set up account settings
                    	break;
                    case LOGGED_OUT:
                    	//should this ever be allowed to happen?
                    	return;
                    default:
                        break;
				}
				settings = SettingsManager.getInstance(this, null);
            	//dataUsage = DataUsageManager.getInstance(this, null);

				long currentTime = System.currentTimeMillis();
				
				if(settings != null && currentTime - lastSettingsSync > SETTINGS_SYNC_MAX_RATE) {
					//update settings
					settings.syncSettings();
					lastSettingsSync = currentTime;
				}
				/*if(dataUsage != null && currentTime - lastDataSync > DATA_SYNC_MAX_RATE) {
					dataUsage.syncData();
					lastDatSync = currentTime;
				}
				*/
				break;
			default:
				break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FragmentManager fragmentManager = getFragmentManager();

		setContentView(R.layout.activity_datatracker);
		//getdate = (DatePicker) findViewById(R.id.billing_date);

		final TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
		tabHost.setup();
		
		TabSpec spec1 =  tabHost.newTabSpec("DataManagment");
		spec1.setContent(R.id.tab1);
		spec1.setIndicator("Data Usage",null);

		FragmentTransaction acctSettingsFragTxn = fragmentManager.beginTransaction();
		AccountSettingsDisplay accountSettings = new AccountSettingsDisplay();
		acctSettingsFragTxn.add(R.id.settings_view, accountSettings);
		acctSettingsFragTxn.commit();
		
		TabSpec spec2 =  tabHost.newTabSpec("Settings");
		spec2.setContent(R.id.settings_scroller);
		spec2.setIndicator("Settings",null);
		
		tabHost.addTab(spec1);
		tabHost.addTab(spec2);


		/*
		tv = (TextView) findViewById(R.id.set_cycle);
        Button b = (Button) findViewById(R.id.get_cycle);// on click of button display the dialog
         b.setOnClickListener(new View.OnClickListener()
         {

            @Override
            public void onClick(View v) {
                 show();
            }
            });
		*/
	}
		
		
		
	//	NumberPicker np = (NumberPicker)findViewById(R.id.set_cycle);
	  //  np.setMinValue(1);
	    //np.setMaxValue(31);
	    //np.setWrapSelectorWheel(true); 
	
	    //Toast.makeText(getApplicationContext(),  "Billing Cycle is: " + np.getValue() +"",Toast.LENGTH_SHORT).show();
	    
	

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		// TODO Auto-generated method stub
		 Log.i("value is",""+newVal);
		
		}
	
	public void show()
    {
         final Dialog d = new Dialog(DataTracker.this);
         d.setTitle("Billing Cycle");
         d.setContentView(R.layout.activity_dialog);
         Button b1 = (Button) d.findViewById(R.id.set_button);
         final NumberPicker np = (NumberPicker) d.findViewById(R.id.pick_cycle);
         np.setMaxValue(31); 
         np.setMinValue(1);   
         np.setWrapSelectorWheel(true);
         np.setOnValueChangedListener(this);
        
         b1.setOnClickListener(new View.OnClickListener()
         {
          @Override
          public void onClick(View v) {
              tv.setText(String.valueOf(np.getValue())); //set the value to textview
              d.dismiss();
           }    
          });
        
       d.show();


    }

	@Override
	public void onClick(View v) {
		 if(v.findViewById(R.id.logoutButton) == v) {
			SessionManager.getInstance(this, null).logOut();
			startActivityForResult(new Intent(this, Login.class), LOGIN_REQUEST);
		}
	}
}