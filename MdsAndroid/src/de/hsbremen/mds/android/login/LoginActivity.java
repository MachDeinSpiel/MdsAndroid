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
import android.os.Message;
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

		getMenuInflater().inflate(R.menu.login, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);

		Button loginBtn = (Button) findViewById(R.id.loginBtn);
		Button registerBtn = (Button) findViewById(R.id.registerBtn);
		final TextView usernameTxt = (TextView) findViewById(R.id.usernameText);
		final TextView userpasswordTxt = (TextView) findViewById(R.id.passwordText);
		usernameTxt.setText("Julian");
		userpasswordTxt.setText("julian");

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

	protected void stopLoadingScreen(final String message, final boolean success) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				progress.setTitle(message);
				progress.setIcon(R.drawable.bomb);
				progress.setIconAttribute(RESULT_OK);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				progress.dismiss();
				
			}
		}).start();
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	private void registerPlayer() {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://195.37.176.178:1380/accounts/register.php"));
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
						LoginActivity.this
								.stopLoadingScreen("Login successfull", true);

						Intent myIntent = new Intent(LoginActivity.this,
								GameChooser.class);
						myIntent.putExtra("username", userName);
						myIntent.putExtra("json", json.toString());

						LoginActivity.this.startActivity(myIntent);
					}
				});
			}
			if (json.getString("mode").equals("error")) {
				this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						try {
							stopLoadingScreen(json.getString("message"), false);

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				});
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onWebSocketConnected() {
		try {
			JSONObject json = new JSONObject();
			json.put("mode", "login");
			json.put("username", userName);
			json.put("password", userPassword);

			webService.send(json.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (NotYetConnectedException ex) {
			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					stopLoadingScreen("Server konnte nicht erreicht werden", false);

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
				LoginActivity.this
						.stopLoadingScreen("Server konnte nicht erreicht werden", false);

			}
		});

	}

}
