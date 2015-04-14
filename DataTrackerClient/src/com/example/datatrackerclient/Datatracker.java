package com.example.datatrackerclient;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Datatracker extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_datatracker);
		
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

}