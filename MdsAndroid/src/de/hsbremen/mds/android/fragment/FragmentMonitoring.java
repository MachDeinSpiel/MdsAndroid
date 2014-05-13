package de.hsbremen.mds.android.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.hsbremen.mds.mdsandroid.R;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class FragmentMonitoring extends Fragment {
	
	String consoleOutput;
	View view;
	
	public FragmentMonitoring() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.fragment_monitoring, container, false);
		
		TextView monitoringConsole = (TextView)view.findViewById(R.id.monitoringConsole);
		monitoringConsole.setMovementMethod(new ScrollingMovementMethod());
		monitoringConsole.setText(consoleOutput);
		
		return view;

	}
	
	public void addConsoleEntry(String entry){
		consoleOutput += entry;
		consoleOutput += "\n";
	}
}
