package de.hsbremen.mds.android.login;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import de.hsbremen.mds.android.communication.WebServices;
import de.hsbremen.mds.android.communication.WebServicesInterface;
import de.hsbremen.mds.android.ingame.MainActivity;
import de.hsbremen.mds.mdsandroid.R;

public class GameChooser extends Activity implements WebServicesInterface {

	CharSequence user;

	String msg;

	ListView activeGamesList;
	ListView gametemplatesList;

	GameListItem activeGamesAdapter;
	GameListItem gametemplatesAdapter;

	private WebServices webServ;
	private SwipeRefreshLayout swipeLayout;

	private Intent lobbyIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.gamechooser);
		// GameList adapter = new GameList(GameChooser.this, gameNames,
		// gameImages);

		// Daten aus voriger Activity holen
		Bundle extras = getIntent().getExtras();
		user = (CharSequence) extras.get("username");

		webServ = WebServices.createWebServices(this);

		Button usernameLabel = (Button) findViewById(R.id.labelGameName);

		usernameLabel.setText("Spieler: " + user);

		activeGamesList = (ListView) findViewById(R.id.gameList);

		gametemplatesList = (ListView) findViewById(R.id.newGameList);

		activeGamesList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						JSONObject json = null;
						try {
							json = new JSONObject();
							json.put("mode", "join");
							json.put("id", id);
							json.put("name", user);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						webServ.send(json.toString());
						

						lobbyIntent = new Intent(GameChooser.this, GameLobby.class);
						lobbyIntent.putExtra("isInitial", false);
						lobbyIntent.putExtra("username", user);
						lobbyIntent.putExtra("game", activeGamesAdapter.getName(position));
						lobbyIntent.putExtra("maxplayers", activeGamesAdapter.getMaxplayers(position));
						lobbyIntent.putExtra("players", activeGamesAdapter.getPlayers(position));
						lobbyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						
						// TODO Sollte eigentlich ohne anfrage vom server gesendet werden
						// LobbyListe anfordern:
						json = null;

						try {
							json = new JSONObject();
							json.put("mode", "gamelobby");
							// TODO Sp�ter GameID einkommentieren
							// json.put("id", gameIds[position]);
							json.put("action", "players");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						webServ.send(json.toString());
					}
				});

		gametemplatesList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						JSONObject json = null;

						try {
							json = new JSONObject();
							json.put("mode", "create");
							// TODO Sp�ter GameID einkommentieren
							// json.put("id", gameIds[position]);
							json.put("id", id);
							json.put("name", user);
							// TODO Maxplayers muss noch dynamisch sein
							json.put("maxplayers", 2);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						webServ.send(json.toString());

						Log.d("Socket", "GameChooser: OnItemClick position: " + position);
						
						lobbyIntent = new Intent(GameChooser.this, GameLobby.class);
						lobbyIntent.putExtra("isInitial", true);
						lobbyIntent.putExtra("username", user);
						lobbyIntent.putExtra("game", gametemplatesAdapter.getName(position));
						lobbyIntent.putExtra("maxplayers", gametemplatesAdapter.getMaxplayers(position));
						lobbyIntent.putExtra("players", gametemplatesAdapter.getPlayers(position));
						lobbyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						
						// TODO Sollte eigentlich ohne anfrage vom server gesendet werden
						// LobbyListe anfordern:
						json = null;

						try {
							json = new JSONObject();
							json.put("mode", "gamelobby");
							// TODO Sp�ter GameID einkommentieren
							// json.put("id", gameIds[position]);
							json.put("action", "players");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						webServ.send(json.toString());

					}
				});

		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(new OnRefreshListener() {

			public void onRefresh() {
				new android.os.Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						swipeLayout.setRefreshing(false);
						// onWebSocketMessage(msg);
					}
				}, 5000);
			}
		});

		swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_blue_bright,
				android.R.color.holo_orange_light,
				android.R.color.holo_orange_light);
		
		String json = extras.getString("json");
		if(json != null){
			onWebSocketMessage(json);
		}
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public void onWebSocketMessage(String message) {

		msg = message;

		JSONObject json;
		try {
			json = new JSONObject(message);
			if (json.getString("mode").equals("gametemplates")) {
				onGameTemplatesUpdate(json.getJSONArray("games"));
			} else if(json.getString("mode").equals("activegames")){
				onActiveGamesUpdate(json.getJSONArray("games"));
			} else if(json.getString("mode").equals("gamelobby")){
				lobbyIntent.putExtra("json", json.toString());
				webServ.unbindService();
				getApplicationContext().startActivity(lobbyIntent);
			} // TODO Eigentlich soll hier nur die Lobby erstellt werden 
			else if(json.getString("mode").equals("full")){
				Intent intent = new Intent(GameChooser.this, MainActivity.class);
				intent.putExtra("username", user);
				intent.putExtra("json", json.toString());
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(intent);
			}
			;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void onActiveGamesUpdate(JSONArray jsonArray) throws JSONException {
		JSONObject jsonObj = null;

		String[] gameNames;
		Integer[] gameImages;
		Integer[] gameIds;
		Integer[] players;
		Integer[] maxplayers;
		
		gameNames = new String[jsonArray.length()];
		gameImages = new Integer[jsonArray.length()];
		gameIds = new Integer[jsonArray.length()];
		players = new Integer[jsonArray.length()];
		maxplayers = new Integer[jsonArray.length()];

		Log.d("Socket", "GameChooser: OnGameUpdate");

		for (int i = 0; i < jsonArray.length(); i++) {
			jsonObj = jsonArray.getJSONObject(i);
			if (jsonObj.getInt("maxplayers") == 0) {

				gameNames[i] = jsonObj.getString("name");

			} else {
				gameNames[i] = jsonObj.getString("name");
				maxplayers[i] = jsonObj.getInt("maxplayers");
			}
			
			players[i] = jsonObj.getInt("activeplayers");
			gameIds[i] = jsonObj.getInt("id");
			gameImages[i] = R.drawable.bomb;

		}
		
		activeGamesAdapter = new GameListItem(this, gameNames, gameImages, maxplayers, players, gameIds);

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				activeGamesList.setAdapter(activeGamesAdapter);
			}
		});
		
	}

	private void onGameTemplatesUpdate(JSONArray jsonArray) throws JSONException {
		JSONObject jsonObj = null;

		String[] gameNames;
		Integer[] gameImages;
		Integer[] gameIds;
		Integer[] players;
		Integer[] maxplayers;
		
		gameNames = new String[jsonArray.length()];
		gameImages = new Integer[jsonArray.length()];
		gameIds = new Integer[jsonArray.length()];
		players = new Integer[jsonArray.length()];
		maxplayers = new Integer[jsonArray.length()];

		Log.d("Socket", "GameChooser: OnGameUpdate");

		for (int i = 0; i < jsonArray.length(); i++) {
			jsonObj = jsonArray.getJSONObject(i);
			if (jsonObj.getInt("maxplayers") == 0) {

				gameNames[i] = jsonObj.getString("name");

			} else {
				gameNames[i] = jsonObj.getString("name");
				maxplayers[i] = jsonObj.getInt("maxplayers");
			}
			
			gameIds[i] = jsonObj.getInt("id");

			gameImages[i] = R.drawable.bomb;

		}
		
		gametemplatesAdapter = new GameListItem(this, gameNames, gameImages, maxplayers, players, gameIds);

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				gametemplatesList.setAdapter(gametemplatesAdapter);
			}
		});

	}
	
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.d("Socket", "GameChooser: onResume()");
		super.onResume();
		getNewGameLists();
		
	}
	

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
	}

	@Override
	public void recreate() {
		// TODO Auto-generated method stub
		Log.d("Socket", "GameChooser: recreate()");
		super.recreate();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onSocketClientConnected() {
		Log.d("Socket", "GameChooser: OnWebserviceConnected()");
		getNewGameLists();
	}
	
	private void getNewGameLists(){
		JSONObject json = new JSONObject();
		try {
			json.put("mode", "gametemplates");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		webServ.send(json.toString());

		json = new JSONObject();
		try {
			json.put("mode", "activegames");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		webServ.send(json.toString());
	}

//	@Override
	public void onWebserviceConnectionClosed(int code, String reason,
			boolean remote) {
		// TODO Auto-generated method stub
		
	}
}