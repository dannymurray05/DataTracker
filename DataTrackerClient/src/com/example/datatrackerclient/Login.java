package com.example.datatrackerclient;


import com.example.datatrackerclient.servercommunications.ServerRequestHandler;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.net.http.AndroidHttpClient;


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
				Runnable registerUser = new Runnable() {
					public void run() {
						ServerRequestHandler.registerUser("5303004290", "password", "dannymurray05@gmail.com");
					}
				};
				Thread registerUserThread = new Thread(registerUser);
				registerUserThread.start();
			}
		});
	}
}
