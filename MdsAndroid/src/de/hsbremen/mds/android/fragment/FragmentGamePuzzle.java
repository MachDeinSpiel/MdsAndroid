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
import android.widget.Toast;
import de.hsbremen.mds.mdsandroid.R;

public class FragmentGamePuzzle extends Fragment{
	
	int[][] pos;
	int[] puzzles = { 2, 3, 7, 5, 1, 9, 11, 4, 6, 8, 10, -1 };

	GridLayout grid;
	boolean started = false;
	int attempts = 100;
	private View gameView;

	
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
		
		puzzles = arrayMix(puzzles);
		grid = (GridLayout) gameView.findViewById(R.id.grid);
		initPuzzles();

		return gameView;
	}

	public void initPuzzles() {

		if (attempts == 1) {
			Toast.makeText(getActivity(), "LOSE!", Toast.LENGTH_SHORT)
					.show();
		}

		for (int i = 0; i < puzzles.length; i++) {
			Button b = (Button) grid.getChildAt(i);

			if (b.getText().equals(String.valueOf(i + 1))) {

			} else {
				break;
			}

			if (i == 10) {
				Toast.makeText(getActivity(), "WON!",
						Toast.LENGTH_SHORT).show();
			}

		}

		grid.removeAllViews();

		for (int i = 0; i < 12; i++) {
			grid = (GridLayout) gameView.findViewById(R.id.grid);
			grid.addView(new Button(getActivity()));
		}

		for (int i = 0; i < 12; i++) {
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

			} else {

			}

		}
	}

	public void moveTile(Button v) {

		boolean ok = false;
		// Position in Array
		int emptyTilePos = searchForEmptyTile();
		int currentTile = locateTilePosition(v);

		switch (emptyTilePos) {
		case 11:
			if (currentTile == 7 || currentTile == 10)
				ok = true;
			break;
		case 10:
			if (currentTile == 11 || currentTile == 9 || currentTile == 6)
				ok = true;
			break;
		case 9:
			if (currentTile == 8 || currentTile == 10 || currentTile == 5)
				ok = true;
			break;
		case 8:
			if (currentTile == 4 || currentTile == 9)
				ok = true;
			break;
		case 7:
			if (currentTile == 6 || currentTile == 3 || currentTile == 11)
				ok = true;
			break;
		case 6:
			if (currentTile == 2 || currentTile == 7 || currentTile == 10
					|| currentTile == 5)
				ok = true;
			break;
		case 5:
			if (currentTile == 4 || currentTile == 1 || currentTile == 6
					|| currentTile == 9)
				ok = true;
			break;
		case 4:
			if (currentTile == 0 || currentTile == 8 || currentTile == 5)
				ok = true;
			break;
		case 3:
			if (currentTile == 2 || currentTile == 7)
				ok = true;
			break;
		case 2:
			if (currentTile == 3 || currentTile == 1 || currentTile == 6)
				ok = true;
			break;
		case 1:
			if (currentTile == 0 || currentTile == 2 || currentTile == 5)
				ok = true;
			break;
		case 0:
			if (currentTile == 1 || currentTile == 4)
				ok = true;
			break;

		default:
			break;
		}

		if (ok) {
			int CurrentValue = puzzles[currentTile];
			puzzles[emptyTilePos] = CurrentValue;
			puzzles[currentTile] = -1;

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

		for (int i = 0; i < 12; i++) {

			if (puzzles[i] == -1) {
				pos = i;
				break;
			}
		}
		return pos;
	}
}
