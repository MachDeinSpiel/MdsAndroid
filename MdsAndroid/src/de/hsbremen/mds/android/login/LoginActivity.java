/**
 * 
 */
package de.hsbremen.mds.android.login;

import java.nio.channels.NotYetConnectedException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import de.hsbremen.mds.android.communication.SocketClient;
import de.hsbremen.mds.android.communication.SocketService;
import de.hsbremen.mds.android.communication.WebServicesInterface;
import de.hsbremen.mds.mdsandroid.R;

public class LoginActivity extends Activity implements WebServicesInterface {

	private CharSequence user;
	public SocketClient loginSocket;

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

		setContentView(R.layout.login);

		Button loginBtn = (Button) findViewById(R.id.loginBtn);
		final TextView usernameTxt = (TextView) findViewById(R.id.usernameText);

		user = usernameTxt.getText();

		View.OnClickListener loginClick = new View.OnClickListener() {
			public void onClick(View v) {

				try {
					SocketService.createSocketService(getBaseContext(), user);

					Intent myIntent = new Intent(LoginActivity.this,
							GameChooser.class);
					myIntent.putExtra("username", user);
					LoginActivity.this.startActivity(myIntent);

				} catch (NotYetConnectedException ex) {
					Toast toast = Toast.makeText(getApplicationContext(), 
							"Server konnte nicht erreicht werden",
							Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		};

		loginBtn.setOnClickListener(loginClick);

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
			if (json.get("mode").equals("gamelist")) {

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
