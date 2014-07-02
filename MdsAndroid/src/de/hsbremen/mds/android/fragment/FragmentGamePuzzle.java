package de.hsbremen.mds.android.fragment;

import java.util.Random;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import de.hsbremen.mds.android.ingame.MainActivity;
import de.hsbremen.mds.mdsandroid.R;

public class FragmentGamePuzzle extends Fragment{
	
	int[][] pos;
	int[] puzzles = { 1, 2, 3, 4, 5, 6, 7, -1, 8 };
	//{ 1, 3, 7, 5, 8, -1, 1, 4, 6 };

	GridLayout grid;
	boolean started = false;
	int attempts = 5;
	private View gameView;
	boolean gameEnded = false;
	
	private int[] arrayMix(int[] zahlen) { 
        int tmp; 
        int rand; 
        Random r = new Random(); 
        for (int i = 0; i < zahlen.length; i++) { 
            rand = r.nextInt(zahlen.length); 
            tmp = zahlen[i]; 
            zahlen[i] = zahlen[rand]; 
            zahlen[rand] = tmp; 
        } 
        return zahlen; 
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameView = inflater.inflate(R.layout.fragment_puzzlegame, container, false);
		
		resetGame();

		return gameView;
	}

	public void initPuzzles() {

		

		if(!gameEnded){
		
		grid.removeAllViews();

		for (int i = 0; i < 9; i++) {
			grid = (GridLayout) gameView.findViewById(R.id.grid);
			grid.addView(new Button(getActivity()));
		}

		for (int i = 0; i < 9; i++) {
			grid = (GridLayout) gameView.findViewById(R.id.grid);
			Button b = (Button) grid.getChildAt(i);

			if (puzzles[i] != -1) {
				b.setText(String.valueOf(puzzles[i]));

				b.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						moveTile((Button) v);
						attempts--;
						TextView attemptsText = (TextView) gameView.findViewById(R.id.txtAttempts);
						attemptsText.setText("Attempts: " + attempts);
					}
				});
			}
		}
		}
		
		if (attempts == 1) {
			returnGameResult(0);
			return;
		}

		for (int i = 0; i < puzzles.length; i++) {
			Button b = (Button) grid.getChildAt(i);

			String a = String.valueOf(i + 1);
			String bu = (String)b.getText();
			
			if (bu.equals(a)) {

			} else {
				break;
			}

			if (i == 7) {
				gameEnded = true;
				returnGameResult(attempts);
			}

		}
		
	}

	public void moveTile(Button v) {

		boolean ok = false;
		// Position in Array
		int emptyTilePos = searchForEmptyTile();
		int currentTile = locateTilePosition(v);

		switch (emptyTilePos) {
		case 8:
			if (currentTile == 5 || currentTile == 7)
				ok = true;
			break;
		case 7:
			if (currentTile == 6 || currentTile == 4 || currentTile == 8)
				ok = true;
			break;
		case 6:
			if (currentTile == 3 || currentTile == 7)
				ok = true;
			break;
		case 5:
			if (currentTile == 2 || currentTile == 4 || currentTile == 8)
				ok = true;
			break;
		case 4:
			if (currentTile == 1 || currentTile == 3 || currentTile == 5 || currentTile == 7)
				ok = true;
			break;
		case 3:
			if (currentTile == 0 || currentTile == 4 || currentTile == 6)
				ok = true;
			break;
		case 2:
			if (currentTile == 1 || currentTile == 5)
				ok = true;
			break;
		case 1:
			if (currentTile == 0 || currentTile == 2 || currentTile == 4)
				ok = true;
			break;
		case 0:
			if (currentTile == 1 || currentTile == 3)
				ok = true;
			break;

		default:
			break;
		}

		if (ok) {
			int CurrentValue = puzzles[currentTile];
			puzzles[emptyTilePos] = CurrentValue;
			puzzles[currentTile] = -1;

		}

		if(!gameEnded){
			initPuzzles();
		}
	}

	public int locateTilePosition(Button v) {

		int pos = 0;

		for (int i = 0; i < puzzles.length; i++) {
			if (Integer.parseInt((String) v.getText()) == puzzles[i]) {
				pos = i;
				break;
			}
		}
		return pos;

	}

	public int searchForEmptyTile() {
		int pos = 0;

		for (int i = 0; i < 9; i++) {

			if (puzzles[i] == -1) {
				pos = i;
				break;
			}
		}
		return pos;
	}
	
	private void returnGameResult(int attempts){
		MainActivity mA = (MainActivity)getActivity();
 		
		//Prepare MdsInfoObject for nextFragment
		mA.updateSwipeAdapter("Puzzle");
 		 
		int returnValue = calculateScore(attempts);
	 	 
		mA.interpreterCom.onGameResult(returnValue, "Puzzle");
	}
	
	private int calculateScore(int attempts){
		
		if(attempts > 25){
			return 10;
		}else if(attempts > 20){
			return 7;
		}else if(attempts > 15){
			return 4;
		}else if(attempts > 10){
			return 3;
		}else if(attempts > 5){
			return 2;
		}else if(attempts >= 1){
			return 1;
		}else if(attempts == 0){
			return 0;
		}
		
		return 0;
	}
	
	private void resetGame(){
	int[] newPuzzle = {1,2,3,4,5,6,7,-1,8};
		puzzles = newPuzzle;
		gameEnded = false;
		attempts = 5;
		TextView attemptsText = (TextView) gameView.findViewById(R.id.txtAttempts);
		attemptsText.setText("Attempts: " + attempts);
		//puzzles = arrayMix(puzzles);
		grid = (GridLayout) gameView.findViewById(R.id.grid);
		initPuzzles();
	}
}
