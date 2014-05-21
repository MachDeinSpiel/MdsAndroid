package de.hsbremen.mds.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import de.hsbremen.mds.mdsandroid.R;

public class GameChooser extends Activity {
	ListView list;
	String[] web = { "Bomb-Defuser", "Ich packe meinen Backpack","Schubs den Roland!" };
	Integer[] imageId = { R.drawable.bomb, R.drawable.backpack ,R.drawable.bremenroland, };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gamechooser);
		GameList adapter = new GameList(GameChooser.this, web, imageId);
		
		Bundle extras = getIntent().getExtras();
		String user = extras.getString("username");
		
		Button usernameLabel = (Button) findViewById(R.id.labelUsername);
		
		
		
		usernameLabel.setText(user);
		
		list = (ListView) findViewById(R.id.gamelist);
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText(GameChooser.this,
						"You Clicked at " + web[+position], Toast.LENGTH_SHORT)
						.show();
			}
		});
	}
}