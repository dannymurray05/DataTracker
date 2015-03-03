package com.example.datatrackerclient;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
//import android.net.http.AndroidHttpClient;


public class Login extends Activity {
	

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		
		Button login  = (Button) findViewById(R.id.button1);
		
		login.setOnClickListener(new View.OnClickListener()
		{
			
			public void onClick(View v)
			{
				Toast.makeText(Login.this, "Invalid Username and Password",Toast.LENGTH_LONG).show();
				
				//HttpClient client1= new DefaultHttpClient();
			}
		}
		);
		

	}
}