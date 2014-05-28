package de.hsbremen.mds.android.fragment;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import de.hsbremen.mds.android.MainActivity;
import de.hsbremen.mds.common.valueobjects.MdsImage;
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
				
				MainActivity activity = (MainActivity) getActivity();
				JSONObject mes = null;
				try {
					mes = new JSONObject();
					mes.put("mode", "join");
					mes.put("id", 0);
					mes.put("name", activity.getUsername().toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				activity.webServ.send(mes.toString());

				// activity.connectToServer();
				// activity.nextFragment(new MdsMap("Map", "URL", "Text"));
				// activity.nextFragment(new MdsImage("", "", ""));
				activity.getViewPager().setCurrentItem(1);
			}
		});

		return view;
	}
}
