package de.hsbremen.mds.android.login;

import java.util.logging.Handler;

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
import android.widget.Toast;
import de.hsbremen.mds.android.communication.WebServices;
import de.hsbremen.mds.android.communication.WebServicesInterface;
import de.hsbremen.mds.android.ingame.MainActivity;
import de.hsbremen.mds.mdsandroid.R;

public class GameChooser extends Activity implements WebServicesInterface {

	CharSequence user;

	String msg;
	
	ListView list;
	ListView listNewGames;
	String[] gameNames;
	Integer[] gameImages;
	Integer[] gameIds;
	Integer[] players;
	Integer[] maxplayers;

	private WebServices webServ;
	private SwipeRefreshLayout swipeLayout;

	
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


		list = (ListView) findViewById(R.id.gameList);

		listNewGames = (ListView) findViewById(R.id.newGameList);
		// list.setAdapter(adapter);

		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				Toast toast = Toast.makeText(getApplicationContext(),
						gameNames[+position], Toast.LENGTH_SHORT);
				toast.show();

				return false;
			}
		});

		listNewGames.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				Toast toast = Toast.makeText(getApplicationContext(),
						gameNames[+position], Toast.LENGTH_SHORT);
				toast.show();

				return false;
			}
		});
		
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				JSONObject json = null;

				switch (position) {
				case 0:
					try {
						json = new JSONObject();
						json.put("mode", "create");
						// TODO Später GameID einkommentieren
						// json.put("id", gameIds[position]);
						json.put("id", 0);
						json.put("name", user);
						json.put("maxplayers", 3);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case 1:
					try {
						json = new JSONObject();
						json.put("mode", "join");
						json.put("id", 0);
						json.put("name", user);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
				//
				// try {
				// json = new JSONObject();
				// json.put("mode", "create");
				// // TODO Später GameID einkommentieren
				// //json.put("id", gameIds[position]);
				// json.put("id", 0);
				// json.put("name", user);
				// json.put("maxplayers", 3);
				// } catch (JSONException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }

				webServ.send(json.toString());
				webServ.unbindService();
				Log.d("Socket", "GameChooser Service ungebindet");

				Intent myIntent = new Intent(GameChooser.this,
						GameLobby.class);
				myIntent.putExtra("username", user);
				myIntent.putExtra("game", gameNames[+position]);
				myIntent.putExtra("maxplayers", maxplayers[+position]);
				myIntent.putExtra("players", players[+position]);
				myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(myIntent);

			}
		});

		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(new OnRefreshListener() {
			
			public void onRefresh() {
				new android.os.Handler().postDelayed(new Runnable() {
			        @Override public void run() {
			            swipeLayout.setRefreshing(false);
			            //onWebSocketMessage(msg);
			        }
			    }, 5000);
			}
		});
		
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_blue_bright,
				android.R.color.holo_orange_light,
				android.R.color.holo_orange_light);
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
				onGameUpdate(json.getJSONArray("games"));
			};
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void onGameUpdate(JSONArray jsonArray) throws JSONException {
		JSONObject jsonObj = null;
		this.gameNames = new String[jsonArray.length()];
		this.gameImages = new Integer[jsonArray.length()];
		this.gameIds = new Integer[jsonArray.length()];
		this.players = new Integer[jsonArray.length()];
		this.maxplayers = new Integer[jsonArray.length()];

		Log.d("Socket", "GameChooser: OnGameUpdate");

		for (int i = 0; i < jsonArray.length(); i++) {
			jsonObj = jsonArray.getJSONObject(i);
			if (jsonObj.getInt("maxplayers") == 0) {

				gameNames[i] = jsonObj.getString("name");
				
				players[i] = jsonObj.getInt("activeplayers");
				
			} else {
				gameNames[i] = jsonObj.getString("name");
				maxplayers[i] = jsonObj.getInt("maxplayers");
				}
			
			
			switch (i) {
			case 0: gameImages[i] = R.drawable.bomb;
				break;
			case 1: gameImages[i] = R.drawable.diddyavatar;
				break;
			case 2: gameImages[i] = R.drawable.ic_launcher;
				break;
			case 3: gameImages[i] = R.drawable.marioavatar;
				break;
			default:
				break;
			}
			
			//gameImages[i] = R.drawable.bomb;
			gameIds[i] = jsonObj.getInt("id");
		}

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				GameListItem adapter = new GameListItem(GameChooser.this,
						gameNames, gameImages, maxplayers , players);
				list.setAdapter(adapter);
				listNewGames.setAdapter(adapter);
			}
		});

	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onWebserviceConnected() {
		Log.d("Socket", "GameChooser: OnWebserviceConnected()");
		JSONObject json = new JSONObject();
		try {
			json.put("mode", "gametemplates");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		webServ.send(json.toString());

	}
}