package de.hsbremen.mds.android.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import de.hsbremen.mds.android.ingame.MainActivity;
import de.hsbremen.mds.android.ingame.SwipeAdapter;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsInfoObject;
import de.hsbremen.mds.mdsandroid.R;

public class FragmentGameReaction extends Fragment{
	
	private static final int MINSCORE = 2;

	private List<Button> buttonList = new ArrayList<Button>();
	private List<Integer> shotList = new ArrayList<Integer>();
	private TextView timer;
	private int score;
	private TextView scoreView;
	private SwipeAdapter sA;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View gameView = inflater.inflate(R.layout.fragment_reactiongame, container, false);
		
		return gameView;
	}
	
	@Override
	
	public void onViewCreated(View view, Bundle savedInstanceState) {

		super.onViewCreated(view, savedInstanceState);
		
		getButtons();
		
		startCountdownTimer(3000);
	}

	private void startCountdownTimer(int time) {
		
		timer = (TextView)getActivity().findViewById(R.id.timer);
		
		 new CountDownTimer(time, 1000) {

		     public void onTick(long millisUntilFinished) {
		    	 long remainingTime = millisUntilFinished / 1000;
		         timer.setText("Start in " + remainingTime + "s");
		     }

		     public void onFinish() {
		    	 startGameTimer(10000);
		    	 gameRound();
		     }
		  }.start();
		
	}
	
	private void startGameTimer(int time) {
		
		timer = (TextView)getActivity().findViewById(R.id.timer);
		
		 new CountDownTimer(time, 1000) {

		     public void onTick(long millisUntilFinished) {
		    	 long remainingTime = millisUntilFinished / 1000;
		         timer.setText("Hit every Field " + remainingTime);
		         if(remainingTime <= 5){
		        	 timer.setBackgroundColor(Color.RED);
		         }
		     }

		     public void onFinish() {
		         timer.setText("Finished");
		 	     for(Button lb : buttonList){
		 			lb.setOnClickListener(null);
		 			lb.setBackgroundResource(R.drawable.reactiongamebutton);       
		 	     }
		 		 timer.setBackgroundColor(Color.GREEN);  
		 		 
		 		 MainActivity mA = (MainActivity)getActivity();
		 		 
		 		mA.updateSwipeAdapter("Puzzle");
		 		
		 		 //Prepare MdsInfoObject for nextFragment
		 		 MdsInfoObject iO = sA.getFragmentInformation();
		 		 iO.setName("showText");
		 		 List<String> buttonList = new ArrayList<String>();
		 		 buttonList.add("back");
		 		 iO.setButtons(buttonList);
		 		 
		 		 if(score >= MINSCORE){
			 		 mA.setMiniGameResult(true, "Puzzle");
			 		 iO.setText("Du hast gewonnen");
		 		 }else{
		 			 mA.setMiniGameResult(false, "Puzzle");
			 		 iO.setText("Du hast verloren");
		 		 }
		 		 
		 		 // Call nextFragment to show the gameResult
		 		 mA.nextFragment(iO);
		     }
		  }.start();
		
	}

	private void getButtons() {
		
		Button btn1 = (Button)getActivity().findViewById(R.id.button1);
		buttonList.add(btn1);
		Button btn2 = (Button)getActivity().findViewById(R.id.button2);
		buttonList.add(btn2);
		Button btn3 = (Button)getActivity().findViewById(R.id.button3);
		buttonList.add(btn3);
		Button btn4 = (Button)getActivity().findViewById(R.id.button4);
		buttonList.add(btn4);
		Button btn5 = (Button)getActivity().findViewById(R.id.button5);
		buttonList.add(btn5);
		Button btn6 = (Button)getActivity().findViewById(R.id.button6);
		buttonList.add(btn6);
		Button btn7 = (Button)getActivity().findViewById(R.id.button7);
		buttonList.add(btn7);
		Button btn8 = (Button)getActivity().findViewById(R.id.button8);
		buttonList.add(btn8);
		Button btn9 = (Button)getActivity().findViewById(R.id.button9);
		buttonList.add(btn9);
		Button btn10 = (Button)getActivity().findViewById(R.id.button10);
		buttonList.add(btn10);
		Button btn11 = (Button)getActivity().findViewById(R.id.button11);
		buttonList.add(btn11);
		Button btn12 = (Button)getActivity().findViewById(R.id.button12);
		buttonList.add(btn12);	
		Button btn13 = (Button)getActivity().findViewById(R.id.button13);
		buttonList.add(btn13);
		Button btn14 = (Button)getActivity().findViewById(R.id.button14);
		buttonList.add(btn14);
		Button btn15 = (Button)getActivity().findViewById(R.id.button15);
		buttonList.add(btn15);
		Button btn16 = (Button)getActivity().findViewById(R.id.button16);
		buttonList.add(btn16);
	}

	private void gameRound() {
		
	    Random randomGenerator = new Random();
	    scoreView = (TextView)getActivity().findViewById(R.id.Score);
	    
	    // Create 3 new Randombuttons
	    do{
	    	int buttonNumber = randomGenerator.nextInt(buttonList.size());
	    	
	    	if(!buttonInList(buttonNumber)){
	    		shotList.add(buttonNumber);
	    	}
	    	
	    }while(shotList.size() < 4);
	    
	    for(int lb : shotList){
		    buttonList.get(lb).setBackgroundResource(R.drawable.shot);
			buttonList.get(lb).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					v.setBackgroundResource(R.drawable.reactiongamebutton);
					v.setOnClickListener(null);
					shotList.remove(shotList.size()-1);
					
					if(shotList.size() == 0){
						score++;
						scoreView.setText("Score: " + score);
						gameRound();
					}
				}
			});
	    }
	}

	private boolean buttonInList(int newNumber) {
		
		boolean inside = false;
		
		for(int b : shotList){
			if(b == newNumber){
				inside = true;
			}
		}
		return inside;
	}

	public void setSwipeAdapter(SwipeAdapter swipeAdapter) {
		this.sA = swipeAdapter;
	}
	
}
