package datatrackerclient;



import com.example.datatrackerclient.R;

import datatrackerclient.servercommunications.ServerRequestHandler;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class Signup extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		
		
		Button login  = (Button) findViewById(R.id.button1);
		
		final EditText phone_number = (EditText) findViewById(R.id.phn_text);
		
		login.setOnClickListener(new View.OnClickListener()
		{
			
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				///ServerRequestHandler.newUser(phone_number.getText().toString(), "password", "dannymurray05@gmail.com");
				
				Runnable registerUser = new Runnable() {
					public void run() {
						ServerRequestHandler.newUser(phone_number.getText().toString(), "password", "dannymurray05@gmail.com");
					}
				}; 
				Thread registerUserThread = new Thread(registerUser);
				registerUserThread.start();
				
	
			}
		}
		);
		
		
	}
}
