package de.hsbremen.mds.android.login;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

	ListView list;
	String[] gameNames;
	Integer[] gameImages;

	private WebServices webServ;

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

		Button usernameLabel = (Button) findViewById(R.id.labelUsername);

		usernameLabel.setText("Spieler: " + user);

		list = (ListView) findViewById(R.id.gamelist);
		// list.setAdapter(adapter);

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
						json.put("id", 0);
						json.put("name", user);
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
				webServ.send(json.toString());

				// Toast.makeText(GameChooser.this,
				// "You Clicked at " + web[+position], Toast.LENGTH_SHORT)
				// .show();
				Intent myIntent = new Intent(GameChooser.this,
						MainActivity.class);
				myIntent.putExtra("username", user);
				myIntent.putExtra("game", gameNames[+position]);
				myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(myIntent);

			}
		});
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
			if (json.getString("mode").equals("games")) {
				onGameUpdate(json.getJSONArray("games"));
			}
			;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void onGameUpdate(JSONArray jsonArray) throws JSONException {
		JSONObject jsonObj = null;
		this.gameImages = new Integer[jsonArray.length()];
		this.gameNames = new String[jsonArray.length()];

		Log.d("Socket", "GameChooser: OnGameUpdate");

		for (int i = 0; i < jsonArray.length(); i++) {
			jsonObj = jsonArray.getJSONObject(i);
			if (jsonObj.getInt("maxplayers") == 0) {

				gameNames[i] = jsonObj.getString("name") + " ("
						+ jsonObj.getInt("activeplayers") + ")";

			} else {
				gameNames[i] = jsonObj.getString("name") + " ("
						+ jsonObj.getInt("activeplayers") + " / "
						+ jsonObj.getInt("maxplayers") + ")";
			}
			gameImages[i] = R.drawable.bomb;
		}

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				GameList adapter = new GameList(GameChooser.this, gameNames,
						gameImages);
				list.setAdapter(adapter);
			}
		});

	}

	@Override
	public void onWebserviceConnected() {
		Log.d("Socket", "GameChooser: OnWebserviceConnected()");
		JSONObject json = new JSONObject();
		try {
			json.put("mode", "games");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		webServ.send(json.toString());

	}
}