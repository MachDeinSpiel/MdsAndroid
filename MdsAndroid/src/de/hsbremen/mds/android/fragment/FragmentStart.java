package de.hsbremen.mds.android.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import de.hsbremen.mds.android.MainActivity;
import de.hsbremen.mds.common.valueobjects.MdsImage;
import de.hsbremen.mds.common.valueobjects.MdsMap;
import de.hsbremen.mds.mdsandroid.R;

public class FragmentStart extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		
		View view = inflater.inflate(R.layout.fragment_start, container, false);
		
		Button btn = (Button) view.findViewById(R.id.btnServerConnect);
		
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity)getActivity();
				activity.connectToServer();
//				activity.nextFragment(new MdsMap("Map", "URL", "Text"));
				activity.nextFragment(new MdsImage("", "", ""));
			}
		});
	
		return view;
	}
}
