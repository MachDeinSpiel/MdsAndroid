/**
 * 
 */
package de.hsbremen.mds.android.menu;

import java.io.File;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
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
	Vector<Button> btnTeams;
	Vector<ListView> listTeams;
	int maxplayers;

	PlayerListItem playerAdapter;

	private ProgressDialog progressDial;
	private WebServices webServ;
	private File spielejson;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    //Remove title bar
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);

	    //Remove notification bar
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.gamelobby);

		Button startBtn = (Button) findViewById(R.id.startBtn);
		Button leaveBtn = (Button) findViewById(R.id.leaveBtn);
		Button lblGameName = (Button) findViewById(R.id.labelGameName);
		lblPlayers = (Button) findViewById(R.id.labelPlayers);

		Bundle extras = getIntent().getExtras();
		isInitialPlayer = extras.getBoolean("isInitial");
		spielejson = (File) extras.get("spielejson");

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
					progressDial = ProgressDialog.show(getActivity(), "Game Lobby", "Starting Game...");
					
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

		username = (CharSequence) extras.get("username");
		CharSequence game = (CharSequence) extras.get("game");
		int players = (Integer) extras.get("players");
		maxplayers = (Integer) extras.get("maxplayers");

		webServ = WebServices.createWebServices(this);

		lblGameName.setText(game);
		// lblPlayerName.setText(username);

		try {
			JSONObject json = new JSONObject(extras.getString("json"));
			initLists(json);
			teamUpdate(json);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
	
	protected void onDestroy() {
		if(progressDial != null)
			progressDial.dismiss();
		webServ.unbindService();
		super.onDestroy();
	}


	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public void onWebSocketMessage(final String message) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				try {
					final JSONObject json = new JSONObject(message);
					if (json.getString("mode").equals("gamelobby")) {
						teamUpdate(json);
						Log.d("Menu", "TeamUpdate");
					}
					if (json.getString("mode").equals("full")) {

						if(progressDial == null)
							progressDial = ProgressDialog.show(getActivity(), "Game Lobby", "Starting Game...");
						// Fullwhiteboardupdate (Spiel wurde gestartet)
						Intent intent = new Intent(GameLobby.this,
								MainActivity.class);
						intent.putExtra("username", username);
						intent.putExtra("json", json.toString());
						intent.putExtra("spielejson", spielejson);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						getApplicationContext().startActivity(intent);						
						finish();
					}
					if (json.get("mode").equals("gametemplates")
							|| json.get("mode").equals("activegames")) {
						Intent myIntent = new Intent(GameLobby.this,
								GameChooser.class);
						myIntent.putExtra("username", username);
						myIntent.putExtra("json", json.toString());
						myIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
						GameLobby.this.startActivity(myIntent);
						finish();
					}
					if (json.getString("mode").equals("error")) {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Toast toast;
								try {
									toast = Toast.makeText(
											getApplicationContext(), json.getString("message"),
											Toast.LENGTH_LONG);
									toast.show();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						});
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

	}

	@Override
	public void onWebSocketConnected() {
		// TODO Auto-generated method stub

	}

	private void initLists(JSONObject json) {

		try {
			JSONArray arr;
			if (!json.getBoolean("isteamgame")) {
				arr = new JSONArray();
				arr.put(json.getJSONArray("players"));
			} else {
				arr = json.getJSONArray("players");

			}
			int teamAnz = arr.length();
			Log.d("Menu", "teamanzahl: " + teamAnz);
			TableRow t;
			// Löschen der Nicht benötigten Views
			switch (teamAnz) {
			case 1:
			case 2:
				t = (TableRow) findViewById(R.id.tablerowTeam2);
				((ViewManager) t.getParent()).removeView(t);
			case 3:
			case 4:
				t = (TableRow) findViewById(R.id.tablerowTeam3);
				((ViewManager) t.getParent()).removeView(t);
			case 5:
			case 6:
				t = (TableRow) findViewById(R.id.tablerowTeam4);
				((ViewManager) t.getParent()).removeView(t);
			}

			Log.d("Menu", "teamanz: " + teamAnz);

			if (teamAnz % 2 != 0) {
				// Listen von Spielern getten
				LinearLayout l = ((LinearLayout) findViewById(getResources()
						.getIdentifier("layoutTeam" + (Integer) (teamAnz + 1),
								"id", getPackageName())));
				Log.d("Menu", "Delete: " + "layoutTeam" + (teamAnz + 1));
				((ViewManager) l.getParent()).removeView(l);
			}

			listTeams = new Vector<ListView>();
			btnTeams = new Vector<Button>();
			for (int i = 1; i <= teamAnz; i++) {

				// Listen von Spielern getten
				listTeams
						.add((ListView) findViewById(getResources()
								.getIdentifier("playerList" + i, "id",
										getPackageName())));

				// listTeams.add(i, (ListView) findViewById(R.id.playerList1));

				listTeams.get(i - 1).toString();

				if (isInitialPlayer) {

					listTeams.get(i - 1).setOnItemSelectedListener(
							new AdapterView.OnItemSelectedListener() {
								public void onItemSelected(
										AdapterView parentView, View childView,
										int position, long id) {
									KickPlayerDialogFragment frag = new KickPlayerDialogFragment();
									frag.prepareDialog(playerAdapter
											.getPlayerName(position));
									FragmentManager fm = getFragmentManager();
									frag.show(fm, "fragment_kick_player");
								}

								public void onNothingSelected(
										AdapterView parentView) {
								}
							});

					registerForContextMenu(listTeams.get(i - 1));
				}

				this.btnTeams.add((Button) findViewById(getResources()
						.getIdentifier("labelTeamname" + i, "id",
								getPackageName())));

				// Bei Teams = 1 gibts nur 1 Team -> dh. Keine Teams
				if (teamAnz > 1) {
					btnTeams.get(i - 1).setOnClickListener(
							new OnClickListener() {

								@Override
								public void onClick(View v) {
									changeTeam(v.getId());
								}
							});
				}

			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void changeTeam(int btnId) {

		for (Button b : btnTeams) {
			if (btnId == b.getId()) {
				JSONObject json = new JSONObject();
				try {
					json.put("mode", "gamelobby");
					json.put("action", "changeteam");
					json.put("team", b.getText().toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				webServ.send(json.toString());
			}
		}
	}

	private void teamUpdate(JSONObject json) throws JSONException {
		JSONArray jsonArray = json.getJSONArray("players");
		JSONObject team;
		int numberPlayers = 0;
		if (json.getBoolean("isteamgame")) {
			for (int i = 0; i < jsonArray.length(); i++) {
				team = (JSONObject) jsonArray.get(i);

				btnTeams.get(i).setText(team.getString("name"));
				// btnTeams.get(i).setText(
				// team.getString("name") + " ("
				// + team.getJSONArray("players").length() + ")");
				playerUpdate(team.getJSONArray("players"), listTeams.get(i));
				numberPlayers += team.getJSONArray("players").length();
				Log.d("Menu",
						"Gamelobby: Teamupdate wurde ausgeführt. ArrayLength: "
								+ jsonArray.length());
			}
		} else {
			btnTeams.get(0).setText(username);
			playerUpdate(jsonArray, listTeams.get(0));
			numberPlayers = jsonArray.length();
		}

		// Hier müssen die aktuellen Spieler und das Maximum an Spielern rein
		lblPlayers.setText(numberPlayers + "/" + maxplayers);
	}

	private void playerUpdate(JSONArray jsonArray, ListView playerList)
			throws JSONException {

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
			playerImages[i] = R.drawable.player;

		}

		playerAdapter = new PlayerListItem(GameLobby.this, playerNames,
				playerImages);

		playerList.setAdapter(playerAdapter);

		// Hier müssen die aktuellen Spieler und das Maximum an Spielern
		// rein
		lblPlayers.setText(jsonArray.length() + "/" + maxplayers);

	}

	@Override
	public void onKickPlayerResult(boolean isKick, String playername) {
		if (isKick) {
			JSONObject json = new JSONObject();
			try {
				json.put("mode", "gamelobby");
				json.put("action", "kick");
				json.put("player", playername);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			webServ.send(json.toString());
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add("Kick");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		if (item.getTitle() == "Kick" && info.position != 0) {

			JSONObject json = null;

			try {
				json = new JSONObject();
				json.put("mode", "gamelobby");
				json.put("action", "kick");
				json.put("player", info.position);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			webServ.send(json.toString());
		} else if (info.position == 0) {
			// TODO Toast cant kick yourself
			Toast toast = Toast.makeText(getApplicationContext(),
					"Du kannst dich nicht selbst kicken", Toast.LENGTH_SHORT);
			toast.show();
		}

		return super.onContextItemSelected(item);
	}

	// @Override
	public void onWebserviceConnectionClosed(int code, String reason,
			boolean remote) {

	}

}
