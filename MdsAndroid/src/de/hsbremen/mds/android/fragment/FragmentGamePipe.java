package de.hsbremen.mds.android.fragment;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import de.hsbremen.mds.android.ingame.MainActivity;
import de.hsbremen.mds.mdsandroid.R;

public class FragmentGamePipe extends Fragment{
	
	private List<ImageView> pipeList = new ArrayList<ImageView>();
	private boolean gameEnded = false;
	private int gameNumber;
	private TextView end;
	private View gameView;
	private TextView timer;
	
	float[] currentGame;
	int[] currentGameDraws;
	
	// SPIEL1
	float[] game1 = { 270f, 90f, 90f, 0f, 90f, 0f, 0f, 270f, 180f };
	int[] game1Draws = { R.drawable.rd, R.drawable.g, R.drawable.rd,
			R.drawable.rd, R.drawable.rd, R.drawable.g, R.drawable.g,
			R.drawable.rd, R.drawable.rd };

	// SPIEL2
	float[] game2 = { 0f, 0f, 90f, 270f, 180f, 0f, 0f, 90f, 90f };
	int[] game2Draws = { R.drawable.g, R.drawable.rd, R.drawable.rd,
			R.drawable.rd, R.drawable.rd, R.drawable.g, R.drawable.rd,
			R.drawable.g, R.drawable.rd };

	// SPIEL3
	float[] game3 = { 0f, 0f, 90f, 0f, 0f, 0f, 270f, 180f, 0f };
	int[] game3Draws = { R.drawable.g, R.drawable.rd, R.drawable.rd,
			R.drawable.g, R.drawable.g, R.drawable.g, R.drawable.rd,
			R.drawable.rd, R.drawable.g };

	@Override
	public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) 
	{
		
		gameView = inflater.inflate(R.layout.fragment_pipegame, container, false);

		startCountdownTimer(3000);
		
		return gameView;
		
	}
	
	private void startCountdownTimer(int time) {
		
		resetGame();
			
		timer = (TextView)gameView.findViewById(R.id.count);
		
		 new CountDownTimer(time, 1000) {

		     public void onTick(long millisUntilFinished) {
		    	 long remainingTime = millisUntilFinished / 1000;
		         timer.setText("Start in " + remainingTime + "s");
		     }

		     public void onFinish() {
		    	 startGameTimer(20000);
		     }
		  }.start();
	}
	
	private void startGameTimer(int time) {
		
		timer = (TextView)gameView.findViewById(R.id.count);
		
		 new CountDownTimer(time, 1000) {

		     public void onTick(long millisUntilFinished) {
		    	 long remainingTime = millisUntilFinished / 1000;
		         timer.setText("Time remaining: " + remainingTime);
		         
		         checkPipes(remainingTime);
		     }

		     public void onFinish() {
		 		 
		 		 MainActivity mA = (MainActivity)getActivity();
		 		
		 		 if(!gameEnded){
			 		 //Prepare MdsInfoObject for nextFragment
				 	 mA.updateSwipeAdapter("Puzzle");
				 	 
				 	 mA.interpreterCom.onGameResult(0, "Puzzle");
		 		 }


		     }
		  }.start();
		
	}

	public void checkPipes(long remainingTime) {

		if (currentGame == null && currentGameDraws == null) {
			if (gameNumber == 1) {
				currentGame = game1;
				currentGameDraws = game1Draws;
				end = (TextView) gameView.findViewById(R.id.targetLeft);
				end.setTextColor(Color.WHITE);

			} else if (gameNumber == 2) {
				currentGame = game2;
				currentGameDraws = game2Draws;
				end = (TextView) gameView.findViewById(R.id.targetLeft);
				end.setTextColor(Color.WHITE);
			} else {
				currentGame = game3;
				currentGameDraws = game3Draws;
				end = (TextView) gameView.findViewById(R.id.targetRight);
				end.setTextColor(Color.WHITE);
			}
		}

		for (int i = 0; i < 9; i++) {

			if (currentGameDraws[i] == R.drawable.g) {
				
				if (pipeList.get(i).getRotation() == currentGame[i] || pipeList.get(i).getRotation() == currentGame[i]+180) {

				} else {
					break;
				}
			
			} else {

				if (pipeList.get(i).getRotation() == currentGame[i]) {

				} else {
					break;
				}

			}
			
			if (i == 8 && !gameEnded ) {
				gameEnded = true;
				returnGameResult(remainingTime);
			}
			
		}
	}

	public void placePipes(int gameNumber) {
		switch (gameNumber) {
		case 1:

			for (int i = 1; i < 10; i++) {
				int resID = getResources().getIdentifier("pipe" + i, "id",
						getActivity().getPackageName());

				ImageView currentImage = (ImageView) gameView.findViewById(resID);
				currentImage.setImageResource(game1Draws[i - 1]);

				currentImage.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (v.getRotation() == 270f) {
							v.setRotation(0f);
						} else {
							v.setRotation(v.getRotation() + 90);
						}
					}
				});
				pipeList.add(currentImage);
			}

			break;
		case 2:

			for (int i = 1; i < 10; i++) {
				int resID = getResources().getIdentifier("pipe" + i, "id",
						getActivity().getPackageName());
				
				ImageView currentImage = (ImageView) gameView.findViewById(resID);
				currentImage.setImageResource(game2Draws[i - 1]);

				currentImage.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (v.getRotation() == 270f) {
							v.setRotation(0f);
						} else {
							v.setRotation(v.getRotation() + 90);
						}
					}
				});
				pipeList.add(currentImage);
			}

			break;
		case 3:

			for (int i = 1; i < 10; i++) {
				int resID = getResources().getIdentifier("pipe" + i, "id",
						getActivity().getPackageName());

				ImageView currentImage = (ImageView) gameView.findViewById(resID);
				currentImage.setImageResource(game3Draws[i - 1]);

				currentImage.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (v.getRotation() == 270f) {
							v.setRotation(0f);
						} else {
							v.setRotation(v.getRotation() + 90);
						}
					}
				});
				pipeList.add(currentImage);
			}

			break;

		default:
			break;
		}
	}
	
	private void returnGameResult(long remainingTime){
		MainActivity mA = (MainActivity)getActivity();
 		
		//Prepare MdsInfoObject for nextFragment
		mA.updateSwipeAdapter("Puzzle");
 		 
		int returnValue = calculateScore(remainingTime);
	 	 
		mA.interpreterCom.onGameResult(returnValue, "Puzzle");
	}
	
	private int calculateScore(long remainingTime){
		
		if(remainingTime > 25){
			return 10;
		}else if(remainingTime > 20){
			return 7;
		}else if(remainingTime > 15){
			return 4;
		}else if(remainingTime > 10){
			return 3;
		}else if(remainingTime > 5){
			return 2;
		}else if(remainingTime >= 0){
			return 1;
		}
		
		return 0;
	}
	
	private void resetGame(){
		gameNumber = (int) (Math.random() * 3 + 1);
		gameEnded = false;
		pipeList.clear();
		placePipes(gameNumber);
		
		if (gameNumber == 1) {
			currentGame = game1;
			currentGameDraws = game1Draws;
			end = (TextView) gameView.findViewById(R.id.targetLeft);
			end.setTextColor(Color.WHITE);

		} else if (gameNumber == 2) {
			currentGame = game2;
			currentGameDraws = game2Draws;
			end = (TextView) gameView.findViewById(R.id.targetLeft);
			end.setTextColor(Color.WHITE);
		} else {
			currentGame = game3;
			currentGameDraws = game3Draws;
			end = (TextView) gameView.findViewById(R.id.targetRight);
			end.setTextColor(Color.WHITE);
		}
		
	}
}
