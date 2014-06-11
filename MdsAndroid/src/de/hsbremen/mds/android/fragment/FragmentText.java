package de.hsbremen.mds.android.fragment;

import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.hsbremen.mds.android.ingame.MainActivity;
import de.hsbremen.mds.android.ingame.SwipeAdapter;
import de.hsbremen.mds.mdsandroid.R;

@SuppressLint("ValidFragment")
public class FragmentText extends Fragment {

	private View view;
	private String message;
	private SwipeAdapter sA;
	private int style;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.fragment_text, container, false);
		
		// Style des Fragments anpassen
		MainActivity a = (MainActivity)getActivity();
		style = a.getStyleNumber();
		styleFragment(view);
		
		return view;
	}
	
	private void styleFragment(View view) {
		TextView t = (TextView)view.findViewById(R.id.text);
		TextView t2 = (TextView)view.findViewById(R.id.labelText);
		
		int styleText = 0;
		int styleLabel = 0;
		int styleLabelBgr = 0;
		
		switch(style){
			case 0:
				styleText = R.style.textColorDefaultBlue;
				styleLabel = R.style.labelDefault;
				styleLabelBgr = R.drawable.labelshape;
				break;
			case 1:
				styleText = R.style.textColorDarkBlue;
				styleLabel = R.style.labelDark;
				styleLabelBgr = R.drawable.labelshapedark;
				break;
		}
		
		t.setTextAppearance(getActivity(), styleText);
		t2.setTextAppearance(getActivity(), styleLabel);
		t2.setBackgroundResource(styleLabelBgr);
	}

	@Override
	public void onResume() {
		super.onResume();
		this.message = sA.getFragmentInformation().getText();
		TextView t = (TextView)view.findViewById(R.id.text);
		t.setText(this.message);
		showButtons();
	}
	

	private void showButtons() {
		System.out.println("Shotbuttons aufgerufen");
		LinearLayout ll = (LinearLayout)view.findViewById(R.id.buttonContainerText);
 		List<String> l = sA.getFragmentInformation().getButtons();
 		for(String s : l){
 			System.out.println("Button adden " + s);
 			Button button = new Button(getActivity());
 			ll.addView(button);
 			button.setText(s);
 			
 			switch(style){
 				case 0:
 					button.setBackgroundResource(R.drawable.buttonshape);
 					break;
 				case 1:
 					button.setBackgroundResource(R.drawable.buttonshapedark);
 					break;
 			}
 			
 			button.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					Button b = (Button)v;
 					MainActivity activity = (MainActivity) getActivity();
 					activity.updateSwipeAdapter("showText");
 					String buttonText = (String)b.getText();
 					activity.interpreterCom.buttonClicked(buttonText);
 				}
 			});
 			
 		}
 	}
	
	public void setMessage(String message){
		this.message = message;
	}

	public void setSwipeAdapter(SwipeAdapter swipeAdapter) {
		this.sA = swipeAdapter;
	}
	
}