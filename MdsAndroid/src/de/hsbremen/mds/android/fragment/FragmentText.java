package de.hsbremen.mds.android.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import de.hsbremen.mds.android.MainActivity;
import de.hsbremen.mds.mdsandroid.R;

@SuppressLint("ValidFragment")
public class FragmentText extends Fragment {

	private boolean actionButton;
	private View view;
	private String message;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.fragment_text, container, false);
		
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
				activity.interpreterCom.buttonClicked("back");
			}
		});
		
		Button btn2 = (Button) view.findViewById(R.id.btnCloseAppText);
		
		btn2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity) getActivity();
				activity.updateSwipeAdapter("text");
			}
		});
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		TextView t = (TextView)view.findViewById(R.id.placeholderText);
		t.setText(this.message);
	}
	
	public void setMessage(String message){
		this.message = message;
	}

	public void setActionbutton(boolean b) {
		actionButton = b;
	}
	
}