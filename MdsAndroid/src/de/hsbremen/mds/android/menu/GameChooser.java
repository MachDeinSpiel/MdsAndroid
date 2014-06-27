package de.hsbremen.mds.android.menu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
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
							e.printStackTrace();
						}

						webServ.send(json.toString());

						lobbyIntent = new Intent(GameChooser.this,
								GameLobby.class);
						lobbyIntent.putExtra("isInitial", false);
						lobbyIntent.putExtra("username", user);
						lobbyIntent.putExtra("game",
								activeGamesAdapter.getName(position));
						lobbyIntent.putExtra("maxplayers",
								activeGamesAdapter.getMaxplayers(position));
						lobbyIntent.putExtra("players",
								activeGamesAdapter.getPlayers(position));
						File f = GameChooser.this
								.jsonEinlesen(activeGamesAdapter
										.getClienturl(position));
						lobbyIntent.putExtra("spielejson", f);
						lobbyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//						// TODO Sollte eigentlich ohne anfrage vom server
//						// gesendet werden
//						// LobbyListe anfordern:
//						json = null;
//
//						try {
//							json = new JSONObject();
//							json.put("mode", "gamelobby");
//							// TODO Später GameID einkommentieren
//							// json.put("id", gameIds[position]);
//							json.put("action", "players");
//						} catch (JSONException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//
//						webServ.send(json.toString());
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
							// TODO Später GameID einkommentieren
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

						Log.d("Socket", "GameChooser: OnItemClick position: "
								+ position);

						lobbyIntent = new Intent(GameChooser.this,
								GameLobby.class);
						lobbyIntent.putExtra("isInitial", true);
						lobbyIntent.putExtra("username", user);
						lobbyIntent.putExtra("game",
								gametemplatesAdapter.getName(position));
						lobbyIntent.putExtra("maxplayers",
								gametemplatesAdapter.getMaxplayers(position));
						lobbyIntent.putExtra("players",
								gametemplatesAdapter.getPlayers(position));
						File f = GameChooser.this
								.jsonEinlesen(gametemplatesAdapter
										.getClienturl(position));
						lobbyIntent.putExtra("spielejson", f);
						lobbyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

						// TODO Sollte eigentlich ohne anfrage vom server
						// gesendet werden
						// LobbyListe anfordern:
						json = null;

						try {
							json = new JSONObject();
							json.put("mode", "gamelobby");
							// TODO Später GameID einkommentieren
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
			
			@Override
			public void onRefresh() {
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						getNewGameLists();
					}
				}).start();
				
			}
		});

		swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_blue_bright,
				android.R.color.holo_orange_light,
				android.R.color.holo_orange_light);

		String json = extras.getString("json");
		if (json != null) {
			onWebSocketMessage(json);
		}
	}

	private File jsonEinlesen(String url) {

		ThreadPolicy tp = ThreadPolicy.LAX;
		StrictMode.setThreadPolicy(tp);

		InputStream is = null;
		is = getInputStreamFromUrl(url);

		Log.d("Menu", "InputStream: " + is.toString());

		// InputStream is =
		// getInputStreamFromUrl("http://195.37.176.178:1388/MDSS-0.1/api/appinfo/2.xml");
		// InputStream is =
		// getInputStreamFromUrl("http://195.37.176.178:1388/MDSS-0.1/api/appinfo/3");

		// Temporäre Datei anlegen
		File json = null;
		try {
			json = File.createTempFile("GameJson", ".json");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Zum Testen bitte drin lassen!!
		// Assetmanager um auf den Assetordner zuzugreifen(Json ist da drin)

		// AssetManager am = getAssets();
		//
		// // Inputstream zum einlesen der Json
		// try {
		// is = am.open("test.json");
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }

		try {
			// Inputstream zum einlesen der Json
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			// Json wird zeilenweise eingelesn uns in das File json geschrieben
			FileWriter writer = new FileWriter(json, true);

			String t = "";

			while ((t = br.readLine()) != null) {
				writer.write(t);
			}

			writer.flush();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Überprüfung, ob es geklappt hat
		if (json.exists()) {
			Log.d("Menu", "JSON hat geklappt! Size: " + json.length() );
		} else {
			Log.d("Menu", "Json hat nicht geklappt :(");
		}

		return json;

	}

	public static InputStream getInputStreamFromUrl(String url) {
		InputStream content = null;
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(new HttpGet(url));
			content = response.getEntity().getContent();
		} catch (Exception e) {
			Log.e("Menu", "Network exception", e);
		}
		return content;
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public void onWebSocketMessage(String message) {

		msg = message;

		try {
			final JSONObject json = new JSONObject(message);
			swipeLayout.setRefreshing(false);
			if (json.getString("mode").equals("gametemplates")) {
				onGameTemplatesUpdate(json.getJSONArray("games"));
			} else if (json.getString("mode").equals("activegames")) {
				onActiveGamesUpdate(json.getJSONArray("games"));
			} else if (json.getString("mode").equals("gamelobby")) {
				lobbyIntent.putExtra("json", json.toString());
				webServ.unbindService();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						getApplicationContext().startActivity(lobbyIntent);
					}
				});
			} // TODO Eigentlich soll hier nur die Lobby erstellt werden
			else if (json.getString("mode").equals("full")) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Intent intent = new Intent(GameChooser.this,
								MainActivity.class);
						intent.putExtra("username", user);
						intent.putExtra("json", json.toString());
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						getApplicationContext().startActivity(intent);
					}
				});
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
		String[] gamePlayerNames;
		Integer[] gameImages;
		Integer[] gameIds;
		Integer[] players;
		Integer[] maxplayers;
		String[] clientUrl;

		gameNames = new String[jsonArray.length()];
		gamePlayerNames = new String[jsonArray.length()];
		gameImages = new Integer[jsonArray.length()];
		gameIds = new Integer[jsonArray.length()];
		players = new Integer[jsonArray.length()];
		maxplayers = new Integer[jsonArray.length()];
		clientUrl = new String[jsonArray.length()];

		Log.d("Socket", "GameChooser: OnGameUpdate");

		for (int i = 0; i < jsonArray.length(); i++) {
			jsonObj = jsonArray.getJSONObject(i);
			if (jsonObj.getInt("maxplayers") != 0) {

				maxplayers[i] = jsonObj.getInt("maxplayers");
			}

			gameNames[i] = jsonObj.getString("name");
			gamePlayerNames[i] = "Players: " + jsonObj.getString("players");

			players[i] = jsonObj.getInt("activeplayers");
			gameIds[i] = jsonObj.getInt("id");
			gameImages[i] = R.drawable.bomb;
			clientUrl[i] = jsonObj.getString("clienturl");

		}

		activeGamesAdapter = new GameListItem(this, gameNames, gamePlayerNames,
				gameImages, maxplayers, players, gameIds, clientUrl);

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				activeGamesList.setAdapter(activeGamesAdapter);
			}
		});

	}

	private void onGameTemplatesUpdate(JSONArray jsonArray)
			throws JSONException {
		JSONObject jsonObj = null;

		String[] gameNames;
		String[] gameSubinformation;
		Integer[] gameImages;
		Integer[] gameIds;
		Integer[] players;
		Integer[] maxplayers;
		String[] clientUrl;

		gameNames = new String[jsonArray.length()];
		gameSubinformation = new String[jsonArray.length()];
		gameImages = new Integer[jsonArray.length()];
		gameIds = new Integer[jsonArray.length()];
		players = new Integer[jsonArray.length()];
		maxplayers = new Integer[jsonArray.length()];
		clientUrl = new String[jsonArray.length()];

		Log.d("Socket", "GameChooser: OnGameUpdate");

		for (int i = 0; i < jsonArray.length(); i++) {
			jsonObj = jsonArray.getJSONObject(i);
			if (jsonObj.getInt("maxplayers") != 0) {
				maxplayers[i] = jsonObj.getInt("maxplayers");
			}

			gameNames[i] = jsonObj.getString("name");
			gameIds[i] = jsonObj.getInt("id");
			gameSubinformation[i] = "Author: " + jsonObj.getString("author");

			gameImages[i] = R.drawable.bomb;
			clientUrl[i] = jsonObj.getString("clienturl");

		}

		gametemplatesAdapter = new GameListItem(this, gameNames,
				gameSubinformation, gameImages, maxplayers, players, gameIds,
				clientUrl);

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				gametemplatesList.setAdapter(gametemplatesAdapter);
			}
		});

	}

	@Override
	protected void onResume() {
		Log.d("Socket", "GameChooser: onResume()");
		super.onResume();
		// getNewGameLists();
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
	public void onWebSocketConnected() {
		Log.d("Socket", "GameChooser: OnWebserviceConnected()");
		getNewGameLists();
	}

	private void getNewGameLists() {
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

	// @Override
	public void onWebserviceConnectionClosed(int code, String reason,
			boolean remote) {
		// TODO Auto-generated method stub

	}
}