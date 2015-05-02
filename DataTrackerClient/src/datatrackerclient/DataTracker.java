package datatrackerclient;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.example.datatrackerclient.R;

import datatrackerclient.sessionmanagement.Login;
import datatrackerclient.sessionmanagement.SessionManager;

public class DataTracker extends Activity implements NumberPicker.OnValueChangeListener{

	private TextView tv;
    static Dialog d ;
	public static final int LOGIN_REQUEST = 0b1;
	//DatePicker getdate;
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		startActivityForResult(new Intent(this, Login.class), LOGIN_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		SessionManager.SessionStatus loginResult = SessionManager.SessionStatus.values()[resultCode];
		switch(requestCode) {
			case LOGIN_REQUEST:
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_datatracker);
		//getdate = (DatePicker) findViewById(R.id.billing_date);
		
		
		
		
		final TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
		tabHost.setup();
		
		TabSpec spec1 =  tabHost.newTabSpec("Tab1");
		spec1.setContent(R.id.tab1);
		spec1.setIndicator("Data Usage",null);
		
		TabSpec spec2 =  tabHost.newTabSpec("Tab2");
		spec2.setContent(R.id.tab2);
		spec2.setIndicator("Settings",null);
		
		tabHost.addTab(spec1);
		tabHost.addTab(spec2);
		
		
		tv = (TextView) findViewById(R.id.set_cycle);
        Button b = (Button) findViewById(R.id.get_cycle);// on click of button display the dialog
         b.setOnClickListener(new View.OnClickListener()
         {

            @Override
            public void onClick(View v) {
                 show();
            }
            });
           }
		
		
		
	//	NumberPicker np = (NumberPicker)findViewById(R.id.set_cycle);
	  //  np.setMinValue(1);
	    //np.setMaxValue(31);
	    //np.setWrapSelectorWheel(true); 
	
	    //Toast.makeText(getApplicationContext(),  "Billing Cycle is: " + np.getValue() +"",Toast.LENGTH_SHORT).show();
	    
	

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		// TODO Auto-generated method stub
		 Log.i("value is",""+newVal);
		
		}
	
	public void show()
    {

         final Dialog d = new Dialog(DataTracker.this);
         d.setTitle("Billing Cycle");
         d.setContentView(R.layout.activity_dialog);
         Button b1 = (Button) d.findViewById(R.id.set_button);
         final NumberPicker np = (NumberPicker) d.findViewById(R.id.pick_cycle);
         np.setMaxValue(31); 
         np.setMinValue(1);   
         np.setWrapSelectorWheel(true);
         np.setOnValueChangedListener(this);
        
         b1.setOnClickListener(new View.OnClickListener()
         {
          @Override
          public void onClick(View v) {
              tv.setText(String.valueOf(np.getValue())); //set the value to textview
              d.dismiss();
           }    
          });
        
       d.show();


    }
	
	
	
}