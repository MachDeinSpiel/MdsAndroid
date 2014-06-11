/**
 * 
 */
package de.hsbremen.mds.android.login;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import de.hsbremen.mds.android.communication.WebServices;
import de.hsbremen.mds.android.communication.WebServicesInterface;
import de.hsbremen.mds.mdsandroid.R;

public class GameLobby extends Activity implements WebServicesInterface{

	String[] gameNames = new String[3];
	Integer[] gameImages = new Integer[3];
	Integer[] gameIds;
	
	private WebServices webServ;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub

		getMenuInflater().inflate(R.menu.login, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.helpItem:
			Toast.makeText(this, "Hilfe...", Toast.LENGTH_SHORT).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.gamelobby);

		Button startBtn = (Button) findViewById(R.id.startBtn);
		Button lblGameName = (Button) findViewById(R.id.labelGameName);
		Button lblPlayerName = (Button) findViewById(R.id.labelPlayername);
		Button lblPlayers = (Button) findViewById(R.id.labelPlayers);
		ListView playerList = (ListView) findViewById(R.id.playerList);
		
//		startBtn.setEnabled(false);
		
		Bundle extras = getIntent().getExtras();
		CharSequence user = (CharSequence) extras.get("username");
		CharSequence game = (CharSequence) extras.get("game");
		int players =  (Integer) extras.get("players");
		int maxplayers = (Integer) extras.get("maxplayers");
		//CharSequence id = (CharSequence) extras.get("id");
		//CharSequence maxplayer = (CharSequence) extras.get("maxplayers");
		
		webServ = WebServices.createWebServices(this);

		lblGameName.setText(game);
		lblPlayerName.setText(user);
//		lblPlayers.setText(players+"/"+maxplayers);
		lblPlayers.setText("3/5");
		
		gameNames[0] = "Mario";
		gameNames[1] = "Diddy";
		gameNames[2] = user.toString();
				
		gameImages [0] = R.drawable.marioavatar;
		gameImages [1] = R.drawable.diddyavatar;
		gameImages [2] = R.drawable.player;
		
		PlayerListItem adapter = new PlayerListItem(GameLobby.this, gameNames,
				gameImages);
		playerList.setAdapter(adapter);
		
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public void onWebSocketMessage(String message) {
		JSONObject json;
		try {
			json = new JSONObject(message);
			if(json.get("mode").equals("gamelobby")){
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void onWebserviceConnected() {
		// TODO Auto-generated method stub
		
	}

}
