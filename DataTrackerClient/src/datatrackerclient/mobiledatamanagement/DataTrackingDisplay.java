package datatrackerclient.mobiledatamanagement;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.example.datatrackerclient.R;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DataTrackingDisplay extends Fragment implements PropertyChangeListener  {
	Activity parent;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.account_settings, container, true);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.parent = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// TODO Auto-generated method stub
		
	}
}
