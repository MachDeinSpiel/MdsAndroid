package de.hsbremen.mds.android.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import de.hsbremen.mds.android.MainActivity;
import de.hsbremen.mds.mdsandroid.R;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
@SuppressLint("ValidFragment")
public class FragmentText extends Fragment {

	String text = "";
	boolean actionButton;
	
	public FragmentText() {
		// Required empty public constructor
		this.text = "Sie haben noch kein Ziel erreicht";
	}
	
	public FragmentText(String txtString) {
		this.text = txtString;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = setText(inflater.inflate(R.layout.fragment_text, container,
				false), this.text);
		
		Button btn = (Button) view.findViewById(R.id.btnReturnText);
		
		if(actionButton){
			btn.setVisibility(1);
		}
		
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity) getActivity();
				Button returnBtn = (Button) activity.findViewById(R.id.btnReturnText);
				returnBtn.setVisibility(Button.GONE);
				activity.initiater.buttonClicked("useItem");
			}
		});
		
		Button btn2 = (Button) view.findViewById(R.id.btnCloseAppText);
		
		btn2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity) getActivity();
				activity.initiater.buttonClicked("endGame");
			}
		});
		
		Button btn3 = (Button) view.findViewById(R.id.btnShowVideo);
		
		btn3.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity) getActivity();
				
				Button returnBtn = (Button) activity.findViewById(R.id.btnReturnText);
				returnBtn.setVisibility(Button.INVISIBLE);
				Button showVidBtn = (Button) activity.findViewById(R.id.btnShowVideo);
				showVidBtn.setVisibility(Button.INVISIBLE);		
				
				activity.initiater.buttonClicked("showVideo");
			}
		});
		
		return view;
	}
	
	public View setText(View view, String text){
		TextView txtView = (TextView) view.findViewById(R.id.placeholderText);
		txtView.setText(text);
		return view;
	}
	
	public void setMessage(String message){
		this.text = message;
	}

	public void setActionbutton(boolean b) {
		actionButton = b;
	}
	
}