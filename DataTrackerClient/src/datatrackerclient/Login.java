package datatrackerclient;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
//import android.widget.Toast;
//import android.net.http.AndroidHttpClient;


import com.example.datatrackerclient.R;

import datatrackerclient.servercommunications.ServerRequestHandler;


public class Login extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		
		Button login  = (Button) findViewById(R.id.button2);
		
		login.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(), Verify.class);
				
				startActivity(intent);
			}
		}
		);
		Button verify = (Button) findViewById(R.id.button1);
		verify.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent1 = new Intent(getApplicationContext(), Verify.class);
				
				startActivity(intent1);			
			}
		});
	}
}