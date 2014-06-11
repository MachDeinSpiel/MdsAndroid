/**
 * 
 */
package de.hsbremen.mds.android.login;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
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
	
	ListView playerList;
	PlayerListItem playerAdapter;
	
	private WebServices webServ;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.gamelobby);

		Button startBtn = (Button) findViewById(R.id.startBtn);
		Button lblGameName = (Button) findViewById(R.id.labelGameName);
		Button lblPlayerName = (Button) findViewById(R.id.labelPlayername);
		Button lblPlayers = (Button) findViewById(R.id.labelPlayers);
		playerList = (ListView) findViewById(R.id.playerList);
		
		Bundle extras = getIntent().getExtras();
		boolean isInitial = extras.getBoolean("isInitail");
		
		if(isInitial){
			startBtn.setEnabled(false);
		}
		
		CharSequence user = (CharSequence) extras.get("username");
		CharSequence game = (CharSequence) extras.get("game");
		int players =  (Integer) extras.get("players");
		int maxplayers = (Integer) extras.get("maxplayers");

		webServ = WebServices.createWebServices(this);

		lblGameName.setText(game);
		lblPlayerName.setText(user);
		
		//Hier mŸssen die aktuellen Spieler und das Maximum an Spielern rein
		lblPlayers.setText(players + "/" +maxplayers);
		
		//DUMMYS -> bzw. eingelogger!
		gameNames[1] = user.toString();
		gameImages[0] = R.drawable.marioavatar;
		
		//gameImages [1] = R.drawable.diddyavatar;
		//gameImages [2] = R.drawable.player;
		
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
					onPlayerUpdate(json.getJSONArray("players"));
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
	
	private void onPlayerUpdate(JSONArray jsonArray) throws JSONException {
		JSONObject jsonObj = null;

		String[] playerNames;
		Integer[] playerImages;
		Integer[] playerIds;
		
		playerNames = new String[jsonArray.length()];
		playerImages = new Integer[jsonArray.length()];
		playerIds = new Integer[jsonArray.length()];

		Log.d("Socket", "GameLobby: onPlayerUpdate");

		for (int i = 0; i < jsonArray.length(); i++) {
			jsonObj = jsonArray.getJSONObject(i);
			
			playerNames[i] = jsonObj.getString("name");
			playerIds[i] = jsonObj.getInt("id");
			playerImages[i] = R.drawable.player;

		}
		
		playerAdapter = new PlayerListItem(GameLobby.this, playerNames,
				playerImages);
		playerList.setAdapter(playerAdapter);
		
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				playerList.setAdapter(playerAdapter);
			}
		});
		
	}

}
