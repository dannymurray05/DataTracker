package datatrackerclient.sessionmanagement;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.datatrackerclient.R;

import datatrackerclient.sessionmanagement.SessionManager.SessionStatus;
import datatrackerstandards.AccountRegistrationStatus;
import datatrackerstandards.DeviceRegistrationStatus;

public class Signup extends Activity implements PropertyChangeListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);

		Button register = (Button) findViewById(R.id.register_button);
		final RadioButton accountRegistration = (RadioButton) findViewById(R.id.accountRegistration);
		final RadioButton memberRegistration = (RadioButton) findViewById(R.id.memberRegistration);
		final ViewSwitcher signUpOptionSwitcher = (ViewSwitcher) findViewById(R.id.signUpOptionSwitcher);

		final EditText phoneEdit = (EditText) findViewById(R.id.phoneEdit);
		final EditText passwordEdit = (EditText) findViewById(R.id.passwordEdit);
		final EditText emailEdit = (EditText) findViewById(R.id.emailEdit);
		final EditText accountPhoneEdit = (EditText) findViewById(R.id.accountPhoneEdit);
		
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		final String phoneNumber = telephonyManager.getLine1Number();
		phoneEdit.setText(phoneNumber);
		
		//debug
		passwordEdit.setText("password");
		emailEdit.setText("dannymurray05@gmail.com");
		accountPhoneEdit.setText("15303204790");
		
		//end debug
		register.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean allFieldsChecked = false;
				SessionManager sessionManager =
						SessionManager.getInstance(getApplicationContext(), Signup.this);
				if(accountRegistration.isChecked() && phoneEdit.getText().length() != 0
						&& passwordEdit.getText().length() != 0
						&& emailEdit.getText().length() != 0) {
					allFieldsChecked = true;
					sessionManager.signUp(phoneEdit.getText().toString(),
                		passwordEdit.getText().toString(), emailEdit.getText().toString());
				}
				else if(memberRegistration.isChecked() && phoneEdit.getText().length() != 0
						&& accountPhoneEdit.getText().length() != 0) {
					allFieldsChecked = true;
					sessionManager.signUp(phoneEdit.getText().toString(),
						accountPhoneEdit.getText().toString());
				}

				if(!allFieldsChecked) {
					Toast.makeText(Signup.this, "All fields required!", Toast.LENGTH_SHORT).show();
				}
			}
		});

		accountRegistration.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				signUpOptionSwitcher.showNext();
			}
		});

		memberRegistration.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				signUpOptionSwitcher.showNext();
			}
		});
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		Object newValue = event.getNewValue();
		if(event.getPropertyName().equals(SessionManager.SESSION_STATUS)) {
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
		else if(event.getPropertyName().equals(SessionManager.DEVICE_REGISTRATION_SUCCESS)) {
			DeviceRegistrationStatus status = (DeviceRegistrationStatus) newValue;
			Toast.makeText(getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
		}
		else if(event.getPropertyName().equals(SessionManager.DEVICE_SIGNUP_ERROR)) {
			DeviceRegistrationStatus error = (DeviceRegistrationStatus) newValue;
			Toast.makeText(getApplicationContext(), error.getStatusMessage(), Toast.LENGTH_SHORT).show();	
		}
		else if(event.getPropertyName().equals(SessionManager.ACCOUNT_SIGNUP_ERROR)) {
			AccountRegistrationStatus error = (AccountRegistrationStatus) newValue;
			Toast.makeText(getApplicationContext(), error.getStatusMessage(), Toast.LENGTH_SHORT).show();		
		}
	}
}
