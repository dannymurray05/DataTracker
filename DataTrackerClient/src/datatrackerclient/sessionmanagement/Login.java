package datatrackerclient.sessionmanagement;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
//import android.widget.Toast;
//import android.net.http.AndroidHttpClient;
import android.widget.EditText;
import android.widget.Toast;

import com.example.datatrackerclient.R;

import datatrackerclient.sessionmanagement.SessionManager.SessionStatus;
import datatrackerstandards.DataTrackerConstants.AccountValidationError;
import datatrackerstandards.DataTrackerConstants.DeviceValidationError;


public class Login extends Activity implements PropertyChangeListener {
	public static final int SIGN_UP_REQUEST = 0b1;

	@Override
	protected void onStart() {
		super.onStart();
		SessionManager.getInstance(getApplicationContext(), this).logIn();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		Button login  = (Button) findViewById(R.id.enterButton);
		Button signup = (Button) findViewById(R.id.registerButton);
		final EditText phoneNumberField = (EditText) findViewById(R.id.phn_text);
		final EditText passwordField = (EditText) findViewById(R.id.password);
		
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		final String phoneNumber = telephonyManager.getLine1Number();
		
		phoneNumberField.setText(String.valueOf(phoneNumber));
		//phoneNumberField.setEnabled(false);
		
		login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("Login", "phoneNumber: " + phoneNumberField.getText().toString());
				SessionManager.getInstance(getApplicationContext(), Login.this).logIn(
						phoneNumberField.getText().toString(), passwordField.getText().toString());
			}
		});
		
		
		signup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent signupIntent = new Intent(getApplicationContext(), Signup.class);
				startActivityForResult(signupIntent, SIGN_UP_REQUEST);
			}
		});

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		SessionManager.SessionStatus loginResult = SessionManager.SessionStatus.values()[resultCode];
		switch(requestCode) {
			case SIGN_UP_REQUEST:
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
	public void propertyChange(PropertyChangeEvent event) {
		Object newValue = event.getNewValue();
		if(event.getPropertyName().equals("sessionStatus")) {
			SessionStatus status = (SessionStatus) event.getNewValue();
			if(status.equals(SessionStatus.LOGGED_IN)) {
				setResult(status.ordinal());
				finish();
			}
			else if(status.equals(SessionStatus.DEVICE_ONLY)) {
				setResult(status.ordinal());
				finish();
			}
			else if(status.equals(SessionStatus.LOGGED_OUT)) {
				//wait until success
			}
		}
		else if(event.getPropertyName().equals(SessionManager.DEVICE_LOGIN_ERROR)) {
			DeviceValidationError error = (DeviceValidationError) newValue;
			switch(error) {
                case DEVICE_NOT_FOUND:
                    Toast.makeText(getApplicationContext(), "Phone number not found!", Toast.LENGTH_SHORT).show();
                    break;
                case PENDING_ACCOUNT_VALIDATION:
                    Toast.makeText(getApplicationContext(), "Not yet accepted for requested account", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "No response from server.", Toast.LENGTH_SHORT).show();
                    break;
			}
		}
		else if(event.getPropertyName().equals(SessionManager.ACCOUNT_LOGIN_ERROR)) {
			AccountValidationError error = (AccountValidationError) newValue;
			switch(error) {
                case ACCOUNT_NOT_FOUND:
                    Toast.makeText(getApplicationContext(), "Account not found!", Toast.LENGTH_SHORT).show();
                    break;
                case INCORRECT_PHONE_NUMBER_OR_PASSWORD:
                    Toast.makeText(getApplicationContext(), "Incorrect phone number or password", Toast.LENGTH_SHORT).show();;
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "No response from server.", Toast.LENGTH_SHORT).show();
                    break;
            }
		}
		else if(event.getPropertyName().equals(SessionManager.DEVICE_SIGNUP_ERROR)) {
			
		}
		else if(event.getPropertyName().equals(SessionManager.ACCOUNT_SIGNUP_ERROR)) {
			
		}
	}
	
	/*@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		//start tracking data
		//startService(new Intent (this, DataTrackingManager.class)); //TODO put this in DataTracker.java
		
		
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
	}*/
}
