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
    private int maxHealth = 1;
    private int currentHealth = 1;
    private String scoreName = "Optional";
    private String scoreValue = "0";
    private String optionalName = "Optional";
    private String optionalValue = "0";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// Inflate the layout for this fragment
		mapView = inflater.inflate(R.layout.fragment_location, container,false);
		
		initUI();
		
		return mapView;
	}
	
	private void initUI() {
		// Set UI-Frame
		TextView healthLabel = (TextView)mapView.findViewById(R.id.healthLabel);
		healthLabel.setBackgroundResource(R.drawable.labelshape);
		
		TextView scoreLabel = (TextView)mapView.findViewById(R.id.scoreLabel);
		scoreLabel.setBackgroundResource(R.drawable.labelshape);
		
		TextView optionalLabel = (TextView)mapView.findViewById(R.id.optionalLabel);
		optionalLabel.setBackgroundResource(R.drawable.labelshape);
		
		LinearLayout healthBarCon = (LinearLayout)mapView.findViewById(R.id.healthBarContainer);
		healthBarCon.setBackgroundResource(R.drawable.labelshape);
		
		// set HealthBar
		healthBar = (ProgressBar) mapView.findViewById(R.id.healthBar);
        Drawable barStyle = getResources().getDrawable(R.drawable.healthbar);
        healthBar.setProgressDrawable(barStyle);
        setHealthbar(maxHealth, currentHealth);
		
        // set Score
		TextView score = (TextView)mapView.findViewById(R.id.scoreSummary);
		score.setBackgroundResource(R.drawable.labelshape);
		score.setTextSize(25);
		setScore(scoreName, scoreValue);
		
		// set Optional
		TextView optional = (TextView)mapView.findViewById(R.id.optional);
		optional.setBackgroundResource(R.drawable.labelshape);
		optional.setTextSize(25);
		setOptional(optionalName, optionalValue);
	}
	
	public void setHealthbar(int maxValue, int value){
		// Calculate Healtbar value
		
		maxHealth = maxValue;
		currentHealth = value;
		
		double onePercent = maxValue/100;
		int healthStatus = (int)(value/onePercent);
		
		if(healthBar != null){
			// Set Healthbar Color
			if(healthStatus >=75){
		        Drawable barStyle = getResources().getDrawable(R.drawable.healthbar);
		        healthBar.setProgressDrawable(barStyle);
			}else if(healthStatus >= 25){
		        Drawable barStyle = getResources().getDrawable(R.drawable.healthbaryellow);
		        healthBar.setProgressDrawable(barStyle);
			}else{
		        Drawable barStyle = getResources().getDrawable(R.drawable.healthbarred);
		        healthBar.setProgressDrawable(barStyle);
			}
			// Set Value of Healtbar
			healthBar.setProgress(healthStatus);
		}else{
			Log.i("fragment", "healthBar war beim Setzen null");
		}
		
		TextView healthNumber = (TextView)mapView.findViewById(R.id.healthNumber);
		String t = value + "/" + maxValue;
		healthNumber.setText(t);
	}
	
	public void setScore(String name, String value){
		this.scoreName = name;
		this.scoreValue = value;
		TextView scoreLabel = (TextView)mapView.findViewById(R.id.scoreLabel);
		TextView scoreSummary = (TextView)mapView.findViewById(R.id.scoreSummary);
		if(scoreSummary != null){
			scoreSummary.setText(scoreValue);
			scoreLabel.setText(scoreName);
		}else{
			Log.i("fragment", "Scorefeld war beim Setzen null");
		}
	}
	
	public void setOptional(String name, String value){
		this.optionalValue = value;
		TextView optional = (TextView)mapView.findViewById(R.id.optional);
		TextView optionalLabel = (TextView)mapView.findViewById(R.id.optionalLabel);
		if(optional != null){
			optional.setText(optionalValue);
			optionalLabel.setText(optionalName);
		}else{
			Log.i("fragment", "Optionalfeld war beim Setzen null");
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setHealthbar(maxHealth, currentHealth);
		setScore(scoreName, scoreValue);
		setOptional(optionalName, optionalValue);
	}
}
