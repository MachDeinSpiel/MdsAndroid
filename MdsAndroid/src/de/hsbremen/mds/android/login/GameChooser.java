package de.hsbremen.mds.android.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import de.hsbremen.mds.android.MainActivity;
import de.hsbremen.mds.android.WebSocketService.SocketService;
import de.hsbremen.mds.mdsandroid.R;

public class GameChooser extends Activity {

	CharSequence user;

	ListView list;
	String[] web = { "Bomb-Defuser", "Ich packe meinen Backpack",
			"Schubs den Roland!", "Hau den Deege", "Rangeln 2.0",
			"Irgendwo in Afrika 2", "Terror-Squad Elite Ultimate" };
	Integer[] imageId = { R.drawable.bomb, R.drawable.backpack,
			R.drawable.bremenroland, R.drawable.bomb, R.drawable.backpack,
			R.drawable.bremenroland, R.drawable.bomb };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.gamechooser);
		GameList adapter = new GameList(GameChooser.this, web, imageId);

		Bundle extras = getIntent().getExtras();
		user = (CharSequence) extras.get("username");

		Button usernameLabel = (Button) findViewById(R.id.labelUsername);

		usernameLabel.setText("Spieler: " + user);

		list = (ListView) findViewById(R.id.gamelist);
		list.setAdapter(adapter);

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// Toast.makeText(GameChooser.this,
				// "You Clicked at " + web[+position], Toast.LENGTH_SHORT)
				// .show();
				Intent myIntent = new Intent(GameChooser.this, MainActivity.class);
				myIntent.putExtra("username", user);
				myIntent.putExtra("game", web[+position]);
				myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(myIntent);

			}
		});
	}
}