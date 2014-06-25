package de.hsbremen.mds.android.fragment;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.hsbremen.mds.mdsandroid.R;

public class FragmentMap extends Fragment{
	
    private View mapView;
    private ProgressBar healthBar;
    private int healthStatus = 75;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		
		mapView = inflater.inflate(R.layout.fragment_location, container,false);
		
		initUI();
		
		initHealthBar();
		
		return mapView;
	}
	
	private void initUI() {
		TextView healthLabel = (TextView)mapView.findViewById(R.id.healthLabel);
		healthLabel.setBackgroundResource(R.drawable.labelshape);
		
		TextView scoreLabel = (TextView)mapView.findViewById(R.id.scoreLabel);
		scoreLabel.setBackgroundResource(R.drawable.labelshape);
		
		TextView optionalLabel = (TextView)mapView.findViewById(R.id.optionalLabel);
		optionalLabel.setBackgroundResource(R.drawable.labelshape);
		
		LinearLayout healthBar = (LinearLayout)mapView.findViewById(R.id.healthBarContainer);
		healthBar.setBackgroundResource(R.drawable.labelshape);
		
		TextView score = (TextView)mapView.findViewById(R.id.scoreSummary);
		score.setBackgroundResource(R.drawable.labelshape);
		score.setTextSize(25);
		
		TextView optional = (TextView)mapView.findViewById(R.id.optional);
		optional.setBackgroundResource(R.drawable.labelshape);
		optional.setTextSize(25);
	}

	private void initHealthBar(){
		healthBar = (ProgressBar) mapView.findViewById(R.id.healthBar);
        // Start lengthy operation in a background thread

        healthBar.setProgress(healthStatus);
		
        Drawable myIcon = getResources().getDrawable(R.drawable.healthbar);
        healthBar.setProgressDrawable(myIcon);

	}
	
	public void setHealthbar(int maxValue, int value){
		double onePercent = maxValue/100;
		healthStatus = (int)(value/onePercent);
		healthBar.setProgress(healthStatus);
	}
}
