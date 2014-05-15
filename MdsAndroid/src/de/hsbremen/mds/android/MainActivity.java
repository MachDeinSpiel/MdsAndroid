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
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import de.hsbremen.mds.android.fragment.FragmentBackpack;
import de.hsbremen.mds.android.fragment.FragmentMap;
import de.hsbremen.mds.android.fragment.FragmentMonitoring;
import de.hsbremen.mds.android.fragment.FragmentStart;
import de.hsbremen.mds.android.fragment.FragmentText;
import de.hsbremen.mds.android.fragment.FragmentVideo;
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

public class MainActivity extends Activity implements TabListener,
		LocationListener, GuiInterface , ServerInterpreterInterface{

	ActionBar actionBar;
	Location location;
	public AndroidInitiater initiater;
	LocationManager manager;
	double longitude;
	double latitude;
	boolean initComplete = false;
//	public ServerClientConnector connector;
	
	ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
	FragmentManager fm;

	int tabCount = 1;
	
	Interpreter interpreter;
	SocketClient socketClient;
	Thread socketThread;
	
	LocationManager lm;
	GoogleMap map;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initiater für die Listener registrierung
		initiater = new AndroidInitiater();
		
		// Initialisiern von GoogleMaps
		initGMaps();

		// Initiallisierung der verfügbaren Fragments
        initFragments();
		
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		addTab("Left");
		addTab("Right");	
		
	}
	
	private void initGMaps(){
		
		  lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		  lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		  
		  map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
		    .getMap();
		  // map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		  // map.setMapType(GoogleMap.MAP_TYPE_NONE);
		  map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		  // map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		  // map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
	}
	
	private void initFragments() {
		fm = getFragmentManager();
		
		FragmentTransaction transaction = fm.beginTransaction();
        FragmentStart startFragment = new FragmentStart();
        fragmentList.add(startFragment);
        
        transaction.add(R.id.content, startFragment);
        transaction.commit();
        
        FragmentMap mapFragment = new FragmentMap();
        fragmentList.add(mapFragment);
        
        FragmentText textFragment = new FragmentText();
        fragmentList.add(textFragment);
        
        FragmentBackpack backpackFragment = new FragmentBackpack();
        backpackFragment.addItem(new MdsItem("Bomb","bomb"));
        backpackFragment.addItem(new MdsItem("Bomb","bomb"));
        backpackFragment.addItem(new MdsItem("Bomb","bomb"));
        backpackFragment.addItem(new MdsItem("Bomb","bomb"));
        fragmentList.add(backpackFragment);
        
        FragmentMonitoring monitoringFragment = new FragmentMonitoring();
        fragmentList.add(monitoringFragment);
        
        FragmentVideo videoFragment = new FragmentVideo();
        fragmentList.add(videoFragment);
	}

	public void connectToServer(){
		
		// Serverkommunikation
		Draft d = new Draft_17();

		String clientname = "AndroidClient";
		String serverIp = "feijnox.no-ip.org";
		String PROTOKOLL_HTTP = "http://";
		String PORT_HTTP = ":8080";
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
	}

	public void showProviderDisable() {
		TextView view = (TextView) findViewById(R.id.txtGPSVal);
		view.setText("AUS");
		view.setBackgroundColor(Color.RED);
		
		FragmentMap f = (FragmentMap)fragmentList.get(1);
		f.updateLocationFields();
	}

	public void showProviderEnable() {
		TextView view = (TextView) findViewById(R.id.txtGPSVal);
		view.setText("AN");
		view.setBackgroundColor(Color.GREEN);
		
		FragmentMap f = (FragmentMap)fragmentList.get(1);
		f.updateLocationFields();
		
		initiater.locationChanged(location);
	}

	@Override
	public void onLocationChanged(Location loc) {

		gmapsUpdate(loc);
		
		FragmentMap f = (FragmentMap)fragmentList.get(1);
		f.updateLocationFields();
		
		initiater.locationChanged(loc);
	}
	
	private void gmapsUpdate(Location loc){
		map.clear();

		MarkerOptions mp = new MarkerOptions();

		mp.position(new LatLng(loc.getLatitude(), loc.getLongitude()));

		mp.title("Player Position");

		map.addMarker(mp);

		map.animateCamera(CameraUpdateFactory.newLatLngZoom(
		new LatLng(loc.getLatitude(), loc.getLongitude()), 16));
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
		FragmentTransaction transaction = fm.beginTransaction();
	    transaction.replace(R.id.content, fragmentList.get(4));
	    transaction.addToBackStack(null);
        transaction.commit();
        
		Button btn = (Button) findViewById(R.id.btnReturnImage);
		btn.setVisibility(1);
	}

	@Override
	public void nextFragment(MdsVideo mds) {
		FragmentTransaction transaction = fm.beginTransaction();
	    transaction.replace(R.id.content, fragmentList.get(5));
	    transaction.addToBackStack(null);
        transaction.commit();
        
		// TODO: Hier muss noch eine Transaction hin
		Button btn = (Button) findViewById(R.id.btnReturnVideo);
		btn.setVisibility(1);
	}

	@Override
	public void nextFragment(MdsText mds) {
		FragmentTransaction transaction = fm.beginTransaction();
	    transaction.replace(R.id.content, fragmentList.get(2));
	    transaction.addToBackStack(null);
        transaction.commit();
	      
        FragmentText t = (FragmentText)fragmentList.get(2);
        t.setMessage(mds.getText());
        t.setActionbutton(true);
	}

	@Override
	public void nextFragment(MdsMap mds) {
		FragmentTransaction transaction = fm.beginTransaction();
	    transaction.replace(R.id.content, fragmentList.get(1));
	    transaction.addToBackStack(null);
        transaction.commit();
	}

	private File jsonEinlesen() {

		ThreadPolicy tp = ThreadPolicy.LAX;
		StrictMode.setThreadPolicy(tp);

		InputStream is = null;

		// InputStream is =
		// getInputStreamFromUrl("http://195.37.176.178:1388/MDSS-0.1/api/appinfo/2.xml");
		// InputStream is =
		// getInputStreamFromUrl("http://195.37.176.178:1388/MDSS-0.1/api/appinfo/3");

		// TemporŠre Datei anlegen
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

		// †berprŸfung, ob es geklappt hat
		if (json.exists()) {
			System.out.println("Geklappt");
			System.out.println(json.length());
		} else {
			System.out.println("Nicht geklappt");
		}

		return json;

	}

	public void addTab(String name) {
		ActionBar.Tab tabImage = actionBar.newTab();
		tabImage.setText(name);
		tabImage.setTabListener(this);
		actionBar.addTab(tabImage);
	}

	/*
	 * Shows Toast for Debbuging
	 */
	public void toastShow(String text, int toastLength) {
		Toast.makeText(this, text, toastLength).show();
	}

	private void addItemtoList(String s) {
		// TODO Müssen noch erstellt werden
		
	}

	private void addPlayertoList(String s) {
		// TODO Müssen noch erstellt werden
		
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

	@Override
	public void onTabReselected(Tab tab, android.app.FragmentTransaction arg1) {
		changeFragment(tab.getPosition());
	}

	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction arg1) {
		changeFragment(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab index, android.app.FragmentTransaction arg1) {
		
	}
	
	private void changeFragment(int index){
		if((tabCount > 0 && index == 0) || (tabCount < fragmentList.size()-1 && index == 1)){
			FragmentTransaction transaction = fm.beginTransaction();
			
			int i = (index == 0 )? -1 : 1;
			
			tabCount += i;
		    transaction.replace(R.id.content, fragmentList.get(tabCount));
		    transaction.addToBackStack(null);
	        transaction.commit();
		}
	}
	
	public FragmentManager getFm() {
		return fm;
	}
	
	public ArrayList<Fragment> getFragmentList(){
		return fragmentList;
	}

	public void consoleEntry(String message) {
		FragmentMonitoring f = (FragmentMonitoring)fragmentList.get(4);
		f.addConsoleEntry(message);
	}
	
	public LocationManager getLocationManager() {
		return this.lm;
    }

	@Override
	public void showMap(ArrayList<MdsItem> items2display) {
		// TODO Anzeigen von MdsItems auf der GMaps Karte
		
	}

	@Override
	public void addToBackpack(MdsItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getServerData(String type, int id) {
		// TODO Auto-generated method stub
	}
}
