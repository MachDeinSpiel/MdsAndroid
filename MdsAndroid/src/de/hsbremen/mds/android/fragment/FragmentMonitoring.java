package de.hsbremen.mds.android.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.hsbremen.mds.mdsandroid.R;

@SuppressLint("ValidFragment")
public class FragmentMonitoring extends Fragment {
	
	private String consoleOutput;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_monitoring, container, false);
		
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
