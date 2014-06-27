package de.hsbremen.mds.android.fragment;

import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.hsbremen.mds.android.ingame.ImageLoader;
import de.hsbremen.mds.android.ingame.MainActivity;
import de.hsbremen.mds.android.ingame.SwipeAdapter;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsInfoObject;
import de.hsbremen.mds.mdsandroid.R;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
@SuppressLint("ValidFragment")
public class FragmentImage extends Fragment {

	private SwipeAdapter sA;
	private MdsInfoObject mds;
	private View imageView;
	private ImageLoader imageLoader;
	private int style = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		imageLoader = new ImageLoader();
		
		imageView = inflater.inflate(R.layout.fragment_image, container, false);
		
        return imageView;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		updateImageView();
		super.onViewCreated(view, savedInstanceState);
	}
	
	public void updateImageView(){
		mds = sA.getFragmentInformation();
		// Set Image
		ImageView img = (ImageView)imageView.findViewById(R.id.picImageFragment);
		img.setImageBitmap(imageLoader.getBitmapFromURL(mds.getUrl()));
		// Set Text
		TextView text = (TextView)imageView.findViewById(R.id.textImageFragment);
		text.setText(mds.getText());
		// Set Buttons
		showButtons();
	}
	
	private void showButtons() {
		LinearLayout ll = (LinearLayout)imageView.findViewById(R.id.buttonContainerImage);
 		List<String> l = sA.getFragmentInformation().getButtons();
 		for(String s : l){
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
 					activity.updateSwipeAdapter("showImage");
 					String buttonText = (String)b.getText();
 					activity.interpreterCom.buttonClicked(buttonText);
 				}
 			});
 		}
 	}

	public void setSwipeAdapter(SwipeAdapter swipeAdapter) {
		this.sA = swipeAdapter;
	}
	
}
