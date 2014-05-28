package de.hsbremen.mds.android.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import de.hsbremen.mds.android.ingame.MainActivity;
import de.hsbremen.mds.android.ingame.SwipeAdapter;
import de.hsbremen.mds.mdsandroid.R;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
@SuppressLint("ValidFragment")
public class FragmentImage extends Fragment {

	private String imagePath = "";
	private SwipeAdapter sA;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = setImage(inflater.inflate(R.layout.fragment_image, container, false) , this.imagePath);
		
		Button btn = (Button) view.findViewById(R.id.btnReturnImage);
		
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity) getActivity();
				Button returnBtn = (Button) activity.findViewById(R.id.btnReturnImage);
				returnBtn.setVisibility(Button.GONE);
				activity.updateSwipeAdapter("showImage");
				activity.interpreterCom.buttonClicked("back");
			}
		});
		
		Button btn2 = (Button) view.findViewById(R.id.btnCloseAppImage);
		
		btn2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity) getActivity();
				activity.updateSwipeAdapter("image");
			}
		});
		
        return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// TODO: hier kann der View bearbeitet werden(sA.getFragmentInformation())
		Button b = (Button)getActivity().findViewById(R.id.btnReturnImage);
		b.setVisibility(1);
	}
	
	public View setImage(View view, String url){

        ImageView imageView = (ImageView) view.findViewById(R.id.placeholderImage);
        imageView.setImageResource(R.drawable.bremenroland);
		
		return view;
	}

	public void setSwipeAdapter(SwipeAdapter swipeAdapter) {
		// TODO Auto-generated method stub
		this.sA = swipeAdapter;
	}
	
}
