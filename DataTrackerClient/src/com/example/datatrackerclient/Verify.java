package com.example.datatrackerclient;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.content.Intent;
import android.widget.Toast;
import android.net.http.AndroidHttpClient;


public class Verify extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verify);
		
		
		Button login  = (Button) findViewById(R.id.button1);
		
		login.setOnClickListener(new View.OnClickListener()
		{
			
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
						
			}
		}
		);
		
		RadioButton signup = (RadioButton) findViewById(R.id.radioButton2);
		
		signup.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent_signup = new Intent(getApplicationContext(), Signup.class);
				
				startActivity(intent_signup);
			}
		});
		
	}
}