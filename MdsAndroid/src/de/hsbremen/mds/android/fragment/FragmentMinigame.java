package de.hsbremen.mds.android.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import de.hsbremen.mds.android.ingame.MainActivity;
import de.hsbremen.mds.mdsandroid.R;

public class FragmentMinigame extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment

		View view = inflater.inflate(R.layout.fragment_minigame, container,
				false);

		final EditText points = (EditText) view.findViewById(R.id.intPoints);
		
		Button btnG = (Button) view.findViewById(R.id.btnGewonnen);

		btnG.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int punkte;
				try{
					punkte = Integer.parseInt(points.getText().toString());
				} catch(NumberFormatException e){
					punkte = 0;
				}
					
				MainActivity activity = (MainActivity) getActivity();
				activity.interpreterCom.minigameResult(punkte, true);
			}
		});

		Button btnV = (Button) view.findViewById(R.id.btnVerloren);

		btnV.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int punkte;
				try{
					punkte = Integer.parseInt(points.getText().toString());
				} catch(NumberFormatException e){
					punkte = 0;
				}
				
				MainActivity activity = (MainActivity) getActivity();
				activity.interpreterCom.minigameResult(punkte, false);
			}
		});

		return view;
	}
}
