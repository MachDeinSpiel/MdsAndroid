/**
 * 
 */
package de.hsbremen.mds.android.login;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import de.hsbremen.mds.android.communication.WebServices;
import de.hsbremen.mds.android.communication.WebServicesInterface;
import de.hsbremen.mds.android.ingame.MainActivity;
import de.hsbremen.mds.mdsandroid.R;

public class GameLobby extends Activity implements WebServicesInterface,
		KickPlayerListener {

	String[] gameNames = new String[3];
	Integer[] gameImages = new Integer[3];
	Integer[] gameIds;
	CharSequence username;
	boolean isInitialPlayer;
	Button lblPlayers;
	int maxplayers;

	ListView playerList;
	PlayerListItem playerAdapter;

	private WebServices webServ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.gamelobby);

		Button startBtn = (Button) findViewById(R.id.startBtn);
		Button leaveBtn = (Button) findViewById(R.id.leaveBtn);
		Button lblGameName = (Button) findViewById(R.id.labelGameName);
		Button lblPlayerName = (Button) findViewById(R.id.labelPlayername);
		lblPlayers = (Button) findViewById(R.id.labelPlayers);
		playerList = (ListView) findViewById(R.id.playerList);

		Bundle extras = getIntent().getExtras();
		isInitialPlayer = extras.getBoolean("isInitial");


		Log.d("Socket", "GameLobby: IsInitialPlayer: " + isInitialPlayer);

		leaveBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				JSONObject json = new JSONObject();
				try {
					json.put("mode", "gamelobby");
					json.put("action", "leave");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				webServ.send(json.toString());
				
			}
		});
		
		startBtn.setEnabled(isInitialPlayer);		
		
		if (isInitialPlayer) {
			startBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					JSONObject json = new JSONObject();
					try {
						json.put("mode", "gamelobby");
						json.put("action", "start");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					webServ.send(json.toString());
				}
			});
		} else {
			startBtn.setVisibility(View.GONE);
		}

		// TODO: Später durch Swipe löschen ersetzten:
		// http://andhradroid.wordpress.com/2012/07/05/how-to-impleme
		// nt-swipe-delete-operation-in-android-like-iphone/

		playerList
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView parentView,
							View childView, int position, long id) {
						KickPlayerDialogFragment frag = new KickPlayerDialogFragment();
						frag.prepareDialog(
								playerAdapter.getPlayerName(position),
								playerAdapter.getPlayerId(position));
						FragmentManager fm = getFragmentManager();
						frag.show(fm, "fragment_kick_player");
					}

					public void onNothingSelected(AdapterView parentView) {
					}
				});

		registerForContextMenu(playerList);
		
		username = (CharSequence) extras.get("username");
		CharSequence game = (CharSequence) extras.get("game");
		int players = (Integer) extras.get("players");
		maxplayers = (Integer) extras.get("maxplayers");

		webServ = WebServices.createWebServices(this);

		lblGameName.setText(game);
		lblPlayerName.setText(username);

		try {
			JSONObject json = new JSONObject(extras.getString("json"));

			playerUpdate(json.getJSONArray("players"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Hier müssen die aktuellen Spieler und das Maximum an Spielern rein
		lblPlayers.setText(players + "/" + maxplayers);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("Socket", "GameLobby: onOptionsItemSelected()");
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
	public Activity getActivity() {
		return this;
	}

	@Override
	public void onWebSocketMessage(String message) {
		JSONObject json;
		try {
			json = new JSONObject(message);
			if (json.getString("mode").equals("gamelobby")) {
				playerUpdate(json.getJSONArray("players"));
			}
			if (json.getString("mode").equals("full")) {
				// Fullwhiteboardupdate (Spiel wurde gestartet)
				Intent intent = new Intent(GameLobby.this, MainActivity.class);
				intent.putExtra("username", username);
				intent.putExtra("json", json.toString());
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(intent);
			}
			if (json.get("mode").equals("gametemplates") || json.get("mode").equals("activegames")){
				Intent myIntent = new Intent(GameLobby.this,
						GameChooser.class);
				myIntent.putExtra("username", username);
				myIntent.putExtra("json", json.toString());
				this.startActivity(myIntent);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onSocketClientConnected() {
		// TODO Auto-generated method stub

	}

	private void playerUpdate(final JSONArray jsonArray) throws JSONException {
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
				playerImages, playerIds);

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				playerList.setAdapter(playerAdapter);

				// Hier müssen die aktuellen Spieler und das Maximum an Spielern
				// rein
				lblPlayers.setText(jsonArray.length() + "/" + maxplayers);

			}
		});

	}

	@Override
	public void onKickPlayerResult(boolean isKick, int playerId) {
		if (isKick) {
			JSONObject json = new JSONObject();
			try {
				json.put("mode", "gamelobby");
				json.put("action", "kick");
				json.put("player", playerId);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			webServ.send(json.toString());
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add("Kick");
	}
	

	@Override
	public boolean onContextItemSelected(MenuItem item){
		super.onContextItemSelected(item);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		if(item.getTitle()=="Kick" && info.position != 0){
			
			JSONObject json = null;

			try {
				json = new JSONObject();
				json.put("mode", "gamelobby");
				// TODO Später GameID einkommentieren
				// json.put("id", gameIds[position]);
				json.put("action", "kick");
				json.put("player", info.position);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			webServ.send(json.toString());
		} else if(info.position == 0){
			// TODO Toast cant kick yourself
			Toast toast = Toast.makeText(getApplicationContext(), 
					"Du kannst sich nicht selbst kicken",
					Toast.LENGTH_SHORT);
			toast.show();
		}
		
		return super.onContextItemSelected(item);
	}

	@Override
	public void onWebserviceConnectionClosed(int code, String reason,
			boolean remote) {
		// TODO Auto-generated method stub
		
	}

}
