package de.hsbremen.mds.android.fragment;

import java.util.List;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;
import de.hsbremen.mds.android.ingame.MainActivity;
import de.hsbremen.mds.android.ingame.SwipeAdapter;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsInfoObject;
import de.hsbremen.mds.mdsandroid.R;

@SuppressLint("ValidFragment")
public class FragmentVideo extends Fragment {

	private SwipeAdapter sA;
	private View videoView;
	private VideoView video;
	private MdsInfoObject mds;
	private int style = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
	
		videoView = inflater.inflate(R.layout.fragment_video, container, false);
		
		return videoView;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		updateVideoView();
	}
	
	private void showButtons() {
		LinearLayout ll = (LinearLayout)videoView.findViewById(R.id.buttonContainerVideo);
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
 					activity.updateSwipeAdapter("showVideo");
 					String buttonText = (String)b.getText();
 					activity.interpreterCom.buttonClicked(buttonText);
 				}
 			});
 		}
 	}
	
	public void updateVideoView(){
		mds = sA.getFragmentInformation();
		// Set Video
		setVideo(mds.getUrl());
		// Set Text
		TextView text = (TextView)videoView.findViewById(R.id.textVideo);
		text.setText(mds.getText());
		// Set Buttons
		showButtons();
	}
	
	public void setVideo(String url){
		
	    Uri uri=Uri.parse(url);
	    video  =(VideoView)videoView.findViewById(R.id.placeholderVideo);
	    video.setVideoURI(uri);
	    video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				MainActivity mA = (MainActivity)getActivity();
				FragmentVideo f = (FragmentVideo)mA.swipeAdapter.getFragment("showVideo");
				VideoView v = f.getVideo();		
				v.start();
			}
		});
	}

	public void setSwipeAdapter(SwipeAdapter swipeAdapter) {
		this.sA = swipeAdapter;
	}
	
	public VideoView getVideo(){
		return this.video;
	}
	

}
