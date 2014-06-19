/**
 * 
 */
package de.hsbremen.mds.android.login;

import java.nio.channels.NotYetConnectedException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import de.hsbremen.mds.android.communication.SocketClient;
import de.hsbremen.mds.android.communication.SocketService;
import de.hsbremen.mds.android.communication.WebServices;
import de.hsbremen.mds.android.communication.WebServicesInterface;
import de.hsbremen.mds.common.communication.LoginSecurity;
import de.hsbremen.mds.mdsandroid.R;

public class LoginActivity extends Activity implements WebServicesInterface {

	private CharSequence userName;
	public SocketClient loginSocket;
	protected ProgressDialog progress;
	private String userPassword;
	private WebServices webService;

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
		Button registerBtn = (Button) findViewById(R.id.registerBtn);
		final TextView usernameTxt = (TextView) findViewById(R.id.usernameText);
		final TextView userpasswordTxt = (TextView) findViewById(R.id.passwordText);

		View.OnClickListener registerClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				LoginActivity.this.registerPlayer();
			}
		};

		View.OnClickListener loginClick = new View.OnClickListener() {

			public void onClick(View v) {

				try {

					LoginActivity.this.startLoadingScreen();
					
					SocketService.createSocketService(getBaseContext());
					LoginActivity.this.webService = WebServices
							.createWebServices(LoginActivity.this);

					userName = usernameTxt.getText();
					userPassword = LoginSecurity.md5(userpasswordTxt.getText()
							.toString());

					System.out.println("Socket: UserPW: "
							+ LoginActivity.this.userPassword);

				} catch (NotYetConnectedException ex) {
					Toast toast = Toast.makeText(getApplicationContext(),
							"Server konnte nicht erreicht werden",
							Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		};

		loginBtn.setOnClickListener(loginClick);
		registerBtn.setOnClickListener(registerClick);

	}

	protected void startLoadingScreen() {
		progress = new ProgressDialog(LoginActivity.this);
		progress.setMessage("Connecting to Server...");
		progress.show();
	}

	protected void stopLoadingScreen() {
		progress.dismiss();
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	private void registerPlayer() {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://mds.apertures.de/accounts/register.php"));
		startActivity(browserIntent);
	}

	@Override
	public void onWebSocketMessage(String message) {

		try {
			final JSONObject json = new JSONObject(message);
			if (json.get("mode").equals("gametemplates")) {

				
				this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						LoginActivity.this.stopLoadingScreen();

						Intent myIntent = new Intent(LoginActivity.this,
								GameChooser.class);
						myIntent.putExtra("username", userName);
						myIntent.putExtra("json", json.toString());

						LoginActivity.this.startActivity(myIntent);
					}
				});
			}
			if(json.getString("mode").equals("error")){
				this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						stopLoadingScreen();
						Toast toast;
						try {
							toast = Toast.makeText(getApplicationContext(),
									json.getString("message"),
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

	@Override
	public void onSocketClientConnected() {
		try {
			JSONObject json = new JSONObject();
			json.put("mode", "login");
			json.put("username", userName);
			json.put("password", userPassword);

			webService.send(json.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotYetConnectedException ex) {
			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					progress.dismiss();
					Toast toast = Toast.makeText(getApplicationContext(),
							"Server konnte nicht erreicht werden",
							Toast.LENGTH_LONG);
					toast.show();

				}
			});
		}

	}

	@Override
	public void onWebserviceConnectionClosed(int code, String reason,
			boolean remote) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				LoginActivity.this.stopLoadingScreen();

				Toast toast = Toast.makeText(getApplicationContext(),
						"Server konnte nicht erreicht werden",
						Toast.LENGTH_LONG);
				toast.show();
			}
		});

	}

}
