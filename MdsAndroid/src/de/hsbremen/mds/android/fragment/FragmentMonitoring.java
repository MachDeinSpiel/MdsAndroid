package de.hsbremen.mds.android.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.hsbremen.mds.mdsandroid.R;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class FragmentMonitoring extends Fragment {
	
	String output = "";
	View view;
	
	public FragmentMonitoring() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.fragment_monitoring, container, false);

	}	
}
