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
import datatrackerstandards.AccountValidationStatus;
import datatrackerstandards.DeviceValidationStatus;


public class Login extends Activity implements PropertyChangeListener {
	public static final int SIGN_UP_REQUEST = 0b1;

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
		
		passwordField.setText("password");


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

		//attempt to log in with stored session values
		SessionManager.getInstance(getApplicationContext(), this).logIn();
	}

	private void sessionStatusChange(SessionManager.SessionStatus status) {
		switch(status) {
            case DEVICE_ONLY:
            case LOGGED_IN:
                setResult(status.ordinal());
                finish();
                break;
            case LOGGED_OUT:
                //wait until success
                break;
            default:
                break;
        }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		SessionManager.SessionStatus signUpResult = SessionManager.SessionStatus.values()[resultCode];
		switch(requestCode) {
			case SIGN_UP_REQUEST:
				sessionStatusChange(signUpResult);
				break;
			default:
				break;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		Object newValue = event.getNewValue();
		if(event.getPropertyName().equals(SessionManager.SESSION_STATUS)) {
			SessionStatus status = (SessionStatus) event.getNewValue();
			sessionStatusChange(status);
		}
		else if(event.getPropertyName().equals(SessionManager.DEVICE_LOGIN_ERROR)) {
			DeviceValidationStatus error = (DeviceValidationStatus) newValue;
			Toast.makeText(getApplicationContext(), error.getStatusMessage(), Toast.LENGTH_SHORT).show();
		}
		else if(event.getPropertyName().equals(SessionManager.DEVICE_LOGIN_SUCCESS)) {
			DeviceValidationStatus status = (DeviceValidationStatus) newValue;
			Toast.makeText(getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
		}
		else if(event.getPropertyName().equals(SessionManager.ACCOUNT_LOGIN_ERROR)) {
			AccountValidationStatus error = (AccountValidationStatus) newValue;
			Toast.makeText(getApplicationContext(), error.getStatusMessage(), Toast.LENGTH_SHORT).show();
		}
	}
}
