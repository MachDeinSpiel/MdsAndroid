/**
 * 
 */
package de.hsbremen.mds.android.login;

import java.net.URI;

import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import de.hsbremen.mds.mdsandroid.R;

/**
 * @author flexfit
 */

public class LoginActivity extends Activity {

	private CharSequence user;
	private LoginSocket loginSocket;
	
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
		
		connectToServer();
		
		Button loginBtn = (Button) findViewById(R.id.loginBtn);
		final TextView usernameTxt = (TextView) findViewById(R.id.usernameText);
		
		user = usernameTxt.getText();
		
		View.OnClickListener loginClick = new View.OnClickListener() {
		    public void onClick(View v) {
		      
		    	loginSocket.send(user.toString());
		    	System.out.println(user);
		    		
		    		Intent myIntent = new Intent(LoginActivity.this, GameChooser.class);
		    		myIntent.putExtra("username", user);
		    		LoginActivity.this.startActivity(myIntent);
		    		
		    	}
		    //}
		    };
		  
		  loginBtn.setOnClickListener(loginClick);
		  
	}	
	
public void connectToServer(){
		
		// Serverkommunikation
		Draft d = new Draft_17();

		String clientname = "AndroidClient";
		String serverIp = "feijnox.no-ip.org";
		String PROTOKOLL_WS = "ws://";
		String PORT_WS = ":8000";
		
		String serverlocation = PROTOKOLL_WS + serverIp + PORT_WS;
		
		URI uri = URI.create(serverlocation + "/runCase?case=" + 1 + "&agent="
				+ clientname);
		loginSocket = new LoginSocket(d, uri, this);
		
		Thread t = new Thread(loginSocket);
		t.start();
		
		loginSocket.send("HALLO");

	}
}
