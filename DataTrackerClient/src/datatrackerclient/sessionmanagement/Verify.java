package datatrackerclient.sessionmanagement;



import com.example.datatrackerclient.R;

import datatrackerclient.DataTracker;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class Verify extends Activity {
	
	Context context = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verify);
				
		Button login  = (Button) findViewById(R.id.button1);
		
		final EditText phone_number = (EditText) findViewById(R.id.phn_text);
		
		TelephonyManager get_number = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		
		final String mPhoneNumber = get_number.getLine1Number();
		
		phone_number.setText(String.valueOf(mPhoneNumber));
		
		login.setOnClickListener(new View.OnClickListener()
		{
			
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Intent correct_user = new Intent(getApplicationContext(), DataTracker.class);
							
				startActivity(correct_user);
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
