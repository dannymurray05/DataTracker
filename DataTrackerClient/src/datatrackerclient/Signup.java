package datatrackerclient;



import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Response.Listener;
import com.example.datatrackerclient.R;

import datatrackerclient.servercommunications.DataTrackerRequest;
import datatrackerclient.servercommunications.ServerRequestHandler;


public class Signup extends Activity {
	
	Context context = this;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		
		
		Button login  = (Button) findViewById(R.id.register_button);
		
		final EditText phone_number = (EditText) findViewById(R.id.phone_edit);
		
		TelephonyManager get_number = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		final String mPhoneNumber = get_number.getLine1Number();
		
		phone_number.setText(String.valueOf(mPhoneNumber));
		
		login.setOnClickListener(new View.OnClickListener()
		{
				
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				///ServerRequestHandler.newUser(phone_number.getText().toString(), "password", "dannymurray05@gmail.com");

				//phone_number.getText().toString()
				
                ServerRequestHandler.registerUser(new Listener<String>() {
					@Override
					public void onResponse(String arg0) {
						Logger.getAnonymousLogger().log(Level.INFO, arg0);
					}
                }, new DataTrackerRequest.GenericErrorListener(),
                        phone_number.getText().toString(), "password", "dannymurray05@gmail.com");
			}
		}
		);
		
		
	}
}
