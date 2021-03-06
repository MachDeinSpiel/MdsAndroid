package de.hsbremen.mds.android.fragment;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.VideoView;
import de.hsbremen.mds.android.ingame.MainActivity;
import de.hsbremen.mds.android.ingame.SwipeAdapter;
import de.hsbremen.mds.mdsandroid.R;

@SuppressLint("ValidFragment")
public class FragmentVideo extends Fragment {

	private String url = "http://bdmobi.in/videos/load/Hindi%203GP%20Music%20Videos/Ram%20Leela%20-%20Laal%20Ishq.3gp";
	private SwipeAdapter sA;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
	
		View view = setVideo(inflater.inflate(R.layout.fragment_video, container,
				false), url);
		
		Button btn = (Button) view.findViewById(R.id.btnReturnVideo);
		
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity) getActivity();
				Button returnBtn = (Button) activity.findViewById(R.id.btnReturnVideo);
				returnBtn.setVisibility(Button.GONE);
				activity.updateSwipeAdapter("showImage");
				activity.interpreterCom.buttonClicked("back");
			}
		});
		
		Button btn2 = (Button) view.findViewById(R.id.btnCloseAppVideo);
		
		btn2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity) getActivity();
				activity.updateSwipeAdapter("video");
			}
		});
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Button b = (Button)getActivity().findViewById(R.id.btnReturnVideo);
		b.setVisibility(1);
	}
	
	public View setVideo(View view, String url){
		
		final VideoView videoView = (VideoView) view.findViewById(R.id.placeholderVideo);
		videoView.setVideoURI(Uri.parse(url));
		videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			
			@Override
			public void onPrepared(MediaPlayer mp) {
				videoView.start();
			}
		});

		return view;
	}

	public void setSwipeAdapter(SwipeAdapter swipeAdapter) {
		this.sA = swipeAdapter;
	}
	

}
