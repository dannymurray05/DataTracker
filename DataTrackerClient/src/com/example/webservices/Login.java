package com.example.webservices;



//import android.support.v7.app.ActionBarActivity;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
			}
		}
		);
		 //android:layout_alignLeft="@+id/phn_text"
	

	}
}
