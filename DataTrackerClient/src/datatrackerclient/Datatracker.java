package datatrackerclient;

import com.example.datatrackerclient.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

public class Datatracker extends Activity {

	
	//DatePicker getdate;
	
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