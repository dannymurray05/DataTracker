package datatrackerclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.example.datatrackerclient.R;

import datatrackerclient.sessionmanagement.Login;
import datatrackerclient.sessionmanagement.SessionManager;

public class DataTracker extends Activity {

	
	public static final int LOGIN_REQUEST = 0b1;
	//DatePicker getdate;
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		startActivityForResult(new Intent(this, Login.class), LOGIN_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		SessionManager.SessionStatus loginResult = SessionManager.SessionStatus.values()[resultCode];
		switch(requestCode) {
			case LOGIN_REQUEST:
				switch(loginResult) {
                    case DEVICE_ONLY:
                        break;
                    case LOGGED_IN:
                        break;
                    case LOGGED_OUT:
                        break;
                    default:
                        break;
				}
				break;
			default:
				break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_datatracker);
		//getdate = (DatePicker) findViewById(R.id.billing_date);
		
		final TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
		tabHost.setup();
		
		TabSpec spec1 =  tabHost.newTabSpec("Tab1");
		spec1.setContent(R.id.tab1);
		spec1.setIndicator("Data Usage",null);
		
		TabSpec spec2 =  tabHost.newTabSpec("Tab2");
		spec2.setContent(R.id.tab2);
		spec2.setIndicator("Settings",null);
		
		tabHost.addTab(spec1);
		tabHost.addTab(spec2);
	}

/*	public void SetCycle(View v)
	{
		int day = getdate.getDayOfMonth();
		int month = getdate.getMonth() + 1;
		int year = getdate.getYear();
		Toast.makeText(getBaseContext(), "Date:"+day+"/"+month+"/"+year, Toast.LENGTH_LONG).show();
	} */
	
}