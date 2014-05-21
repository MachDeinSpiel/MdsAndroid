package de.hsbremen.mds.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;
import de.hsbremen.mds.android.fragment.FragmentBackpack;
import de.hsbremen.mds.android.fragment.FragmentLocation;
import de.hsbremen.mds.android.fragment.FragmentMonitoring;
import de.hsbremen.mds.android.fragment.FragmentText;
import de.hsbremen.mds.android.fragment.GoogleMapFragment;
import de.hsbremen.mds.android.listener.AndroidInitiater;
import de.hsbremen.mds.common.communication.EntryHandler;
import de.hsbremen.mds.common.exception.UnknownWhiteboardTypeException;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.common.interfaces.AndroidListener;
import de.hsbremen.mds.common.interfaces.GuiInterface;
import de.hsbremen.mds.common.interfaces.ServerInterpreterInterface;
import de.hsbremen.mds.common.valueobjects.MdsImage;
import de.hsbremen.mds.common.valueobjects.MdsMap;
import de.hsbremen.mds.common.valueobjects.MdsText;
import de.hsbremen.mds.common.valueobjects.MdsVideo;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.interpreter.Interpreter;
import de.hsbremen.mds.mdsandroid.R;

public class MainActivity extends FragmentActivity implements LocationListener, 
					GuiInterface , ServerInterpreterInterface{

	private Location location;
	public AndroidInitiater initiater;
	
	private Interpreter interpreter;
	private SocketClient socketClient;
	
	private GoogleMapFragment mapFragment;
	
	private ViewPager viewPager;
	public SwipeAdapter swipeAdapter;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ActionBar actionbar = getActionBar();
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		viewPager=(ViewPager) findViewById(R.id.pager);
		swipeAdapter = new SwipeAdapter(getSupportFragmentManager());
		viewPager.setAdapter(swipeAdapter);
		
		// Initiater f¸r die Listener registrierung
		initiater = new AndroidInitiater();
			
		FragmentManager fm = getFragmentManager();
		mapFragment = (GoogleMapFragment) fm.findFragmentById(R.id.map);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		LinearLayout l = (LinearLayout)findViewById(R.id.containerMap);
		LinearLayout l2 = (LinearLayout)findViewById(R.id.containerPager);
		
		
		if(item.getItemId() == R.id.toggleMap){
			
			if(l.getHeight() > l2.getHeight()){
				l.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 0, 1.5f));
				l2.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 0, 3.5f));
			}else{
				l.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 0, 3.5f));
				l2.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 0, 1.5f));
			}
		}
		return true;
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
		socketClient = new SocketClient(d, uri, this);

		Thread t = new Thread(socketClient);
		t.start();
		
		File jsonDatei = jsonEinlesen();
		
		// Hier wird der Interpreter erstellt und wir mitgegeben und als Interface genutzt
		// TODO PlayerId vom Server holen (beim erstellen des Websockets)
		int playerId = 0;
		interpreter = new Interpreter(jsonDatei, this, this, playerId);
		// TODO Listener wird vorrübergehend hier erstellt
		initiater.setListener(interpreter, 5);
	}

	@Override
	public void onLocationChanged(Location loc) {

		mapFragment.gmapsUpdate(loc);
		
		FragmentLocation f = (FragmentLocation)swipeAdapter.getFragment("location");
		f.updateLocationFields();
		
		initiater.locationChanged(loc);
	}
	
	public void showProviderDisable() {
		TextView view = (TextView) findViewById(R.id.txtGPSVal);
		view.setText("AUS");
		view.setBackgroundColor(Color.RED);
		
		FragmentLocation f = (FragmentLocation)swipeAdapter.getFragment("location");
		f.updateLocationFields();
	}
	
	public void showProviderEnable() {
		TextView view = (TextView) findViewById(R.id.txtGPSVal);
		view.setText("AN");
		view.setBackgroundColor(Color.GREEN);
		
		FragmentLocation f = (FragmentLocation)swipeAdapter.getFragment("location");
		f.updateLocationFields();
		
		initiater.locationChanged(location);
	}
	
	@Override
	public void onProviderDisabled(String arg0) {
		showProviderDisable();
	}

	@Override
	public void onProviderEnabled(String arg0) {
		showProviderEnable();
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}

	@Override
	public void setAndroidListener(AndroidListener listener,
			double positionsIntervall) {
		initiater.setListener(listener, positionsIntervall);
	}

	@Override
	public void nextFragment(MdsImage mds) {
		viewPager.setCurrentItem(swipeAdapter.getFragmentName("image"), true);
		
		Button btn = (Button) findViewById(R.id.btnReturnImage);
		btn.setVisibility(1);
	}

	@Override
	public void nextFragment(MdsVideo mds) {
		
		viewPager.setCurrentItem(swipeAdapter.getFragmentName("video"), true);
       
		Button btn = (Button) findViewById(R.id.btnReturnVideo);
		btn.setVisibility(1);
	}

	@Override
	public void nextFragment(MdsText mds) {	      
		FragmentText f = (FragmentText)swipeAdapter.getFragment("text");
        f.setMessage(mds.getText());
        f.setActionbutton(true);
        
		viewPager.setCurrentItem(swipeAdapter.getFragmentName("text"), true);
	}

	@Override
	public void nextFragment(MdsMap mds) {
		viewPager.setCurrentItem(swipeAdapter.getFragmentName("location"), true);
	}

/*	
	@Override
	public void nextFragment(MdsInfoObject mdsInfo) {
		// Transaction
		int index = 0; //???
		Fragment f = fragmentList.get(index);
		// f.setInfo(mdsInfo);
	}
*/
	private File jsonEinlesen() {

		ThreadPolicy tp = ThreadPolicy.LAX;
		StrictMode.setThreadPolicy(tp);

		InputStream is = null;

		// InputStream is =
		// getInputStreamFromUrl("http://195.37.176.178:1388/MDSS-0.1/api/appinfo/2.xml");
		// InputStream is =
		// getInputStreamFromUrl("http://195.37.176.178:1388/MDSS-0.1/api/appinfo/3");

		// Temporäre Datei anlegen
		File json = null;
		try {
			json = File.createTempFile("TourismApp", ".json");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Zum Testen bitte drin lassen!!
		// Assetmanager um auf den Assetordner zuzugreifen(Json ist da drin)

		AssetManager am = getAssets();

		// Inputstream zum einlesen der Json
		try {
			is = am.open("test.json");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			// Inputstream zum einlesen der Json
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			// Json wird zeilenweise eingelesn uns in das File json geschrieben
			FileWriter writer = new FileWriter(json, true);

			String t = "";

			while ((t = br.readLine()) != null) {
				System.out.println(t);
				writer.write(t);
			}

			writer.flush();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Überprüfung, ob es geklappt hat
		if (json.exists()) {
			System.out.println("Geklappt");
			System.out.println(json.length());
		} else {
			System.out.println("Nicht geklappt");
		}

		return json;

	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onWhiteboardUpdate(List<String> keys, WhiteboardEntry entry) {
		
		//TODO: Entry Handler wandelt keys und entry in JSON um

		try {
			socketClient.send(EntryHandler.toJson(keys, entry));
		} catch (NotYetConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownWhiteboardTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void consoleEntry(String message) {
		FragmentMonitoring f = (FragmentMonitoring)swipeAdapter.getFragment("monitoring");
		f.addConsoleEntry(message);
	}

	@Override
	public void showMap(ArrayList<MdsItem> items2display) {
		// TODO Anzeigen von MdsItems auf der GMaps Karte
	}

	@Override
	public void addToBackpack(MdsItem item) {
		FragmentBackpack f = (FragmentBackpack)swipeAdapter.getFragment("backpack");
        f.addItem(item);
	}

	public void getServerData(String type, int id) {
	}
}
