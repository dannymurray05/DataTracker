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
import android.widget.ViewSwitcher;

import com.example.datatrackerclient.R;

public class Signup extends Activity implements PropertyChangeListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);

		Button register = (Button) findViewById(R.id.register_button);
		RadioButton accountRegistration = (RadioButton) findViewById(R.id.accountRegistration);
		RadioButton memberRegistration = (RadioButton) findViewById(R.id.memberRegistration);
		final ViewSwitcher signUpOptionSwitcher = (ViewSwitcher) findViewById(R.id.signUpOptionSwitcher);

		final EditText phoneEdit = (EditText) findViewById(R.id.phoneEdit);
		final EditText passwordEdit = (EditText) findViewById(R.id.passwordEdit);
		final EditText emailEdit = (EditText) findViewById(R.id.emailEdit);
		final EditText accountPhoneEdit = (EditText) findViewById(R.id.accountPhoneEdit);
		
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		final String phoneNumber = telephonyManager.getLine1Number();
		phoneEdit.setText(String.valueOf(phoneNumber));
		
		register.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SessionManager.getInstance(getApplicationContext(), Signup.this).signUp(
						phoneEdit.getText().toString(), accountPhoneEdit.getText().toString(),
                		passwordEdit.getText().toString(), emailEdit.getText().toString());
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
		// TODO Auto-generated method stub
		
	}
}
