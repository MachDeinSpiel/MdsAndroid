/**
 * 
 */
package de.hsbremen.mds.android;

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
		//final TextView passwordTxt = (TextView) findViewById(R.id.passwordText);
		
		View.OnClickListener loginClick = new View.OnClickListener() {
		    public void onClick(View v) {
		      
		    	String user = String.valueOf(usernameTxt.getText());
		    	//String pw = String.valueOf(passwordTxt.getText());

		    	if(user.equals("mds")){
		    		
		    		Intent myIntent = new Intent(LoginActivity.this, GameChooser.class);
		    		myIntent.putExtra("username", user);
		    		LoginActivity.this.startActivity(myIntent);
		    		
		    	} else {
		    		Toast.makeText(getApplicationContext(), "Ungültige Dateneingabe...", Toast.LENGTH_SHORT).show();;
		    	}
		    }
		  };
		  
		  loginBtn.setOnClickListener(loginClick);
		  
	}	
	
}
