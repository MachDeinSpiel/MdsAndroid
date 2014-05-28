package de.hsbremen.mds.android.ingame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
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
import de.hsbremen.mds.android.communication.InterpreterCommunicator;
import de.hsbremen.mds.android.communication.WebServices;
import de.hsbremen.mds.android.communication.WebServicesInterface;
import de.hsbremen.mds.android.fragment.FragmentBackpack;
import de.hsbremen.mds.android.fragment.FragmentLocation;
import de.hsbremen.mds.android.fragment.FragmentMonitoring;
import de.hsbremen.mds.android.fragment.FragmentText;
import de.hsbremen.mds.android.fragment.GoogleMapFragment;
import de.hsbremen.mds.common.communication.WhiteboardHandler;
import de.hsbremen.mds.common.exception.UnknownWhiteboardTypeException;
import de.hsbremen.mds.common.guiobjects.MdsItem;
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
		GuiInterface, ServerInterpreterInterface, WebServicesInterface {

	private Location location;
	public InterpreterCommunicator interpreterCom;

	private GoogleMapFragment mapFragment;

	private ViewPager viewPager;
	public SwipeAdapter swipeAdapter;

	private CharSequence username;

	public WebServices webServ;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Bundle extras = getIntent().getExtras();
		username = extras.getCharSequence("username");

		webServ = WebServices.createWebServices(this);

		ActionBar actionbar = getActionBar();
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		viewPager = (ViewPager) findViewById(R.id.pager);
		swipeAdapter = new SwipeAdapter(getSupportFragmentManager());
		viewPager.setAdapter(swipeAdapter);

		FragmentManager fm = getFragmentManager();
		mapFragment = (GoogleMapFragment) fm.findFragmentById(R.id.map);

		// Interpreter Erstellung
		File jsonDatei = jsonEinlesen();

		// Hier wird der Interpreter erstellt und wir mitgegeben und als
		// Interface genutzt
		// TODO PlayerId vom Server holen (beim erstellen des Websockets)
		int playerId = 0;
		Interpreter interpreter = new Interpreter(jsonDatei, this, this,
				username.toString());

		// Initiater f¸r die Listener registrierung
		interpreterCom = new InterpreterCommunicator(interpreter, 5);

	}

	@Override
	protected void onDestroy() {
		webServ.closeWebServices();
		super.onDestroy();
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

		LinearLayout l = (LinearLayout) findViewById(R.id.containerMap);
		LinearLayout l2 = (LinearLayout) findViewById(R.id.containerPager);

		if (item.getItemId() == R.id.toggleMap) {

			if (l.getHeight() > l2.getHeight()) {
				l.setLayoutParams(new TableLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, 0, 1.5f));
				l2.setLayoutParams(new TableLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, 0, 3.5f));
			} else {
				l.setLayoutParams(new TableLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, 0, 3.5f));
				l2.setLayoutParams(new TableLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, 0, 1.5f));
			}
		}
		return true;
	}


	@Override
	public void onLocationChanged(Location loc) {

		mapFragment.gmapsUpdate(loc);

		FragmentLocation f = (FragmentLocation) swipeAdapter
				.getFragment("location");
		f.updateLocationFields();

		interpreterCom.locationChanged(loc);
	}

	public void showProviderDisable() {
		TextView view = (TextView) findViewById(R.id.txtGPSVal);
		view.setText("AUS");
		view.setBackgroundColor(Color.RED);

		FragmentLocation f = (FragmentLocation) swipeAdapter
				.getFragment("location");
		f.updateLocationFields();
	}

	public void showProviderEnable() {
		TextView view = (TextView) findViewById(R.id.txtGPSVal);
		view.setText("AN");
		view.setBackgroundColor(Color.GREEN);

		FragmentLocation f = (FragmentLocation) swipeAdapter
				.getFragment("location");
		f.updateLocationFields();

		interpreterCom.locationChanged(location);
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
	public void nextFragment(MdsImage mds) {
		swipeAdapter.addFragment("image");
		viewPager.setCurrentItem(swipeAdapter.getFragmentName("image"), true);

		Button btn = (Button) findViewById(R.id.btnReturnImage);
		btn.setVisibility(1);
	}

	@Override
	public void nextFragment(MdsVideo mds) {
		
		swipeAdapter.addFragment("video");
		viewPager.setCurrentItem(swipeAdapter.getFragmentName("video"), true);

		Button btn = (Button) findViewById(R.id.btnReturnVideo);
		btn.setVisibility(1);
	}

	@Override
	public void nextFragment(MdsText mds) {	
		
		swipeAdapter.addFragment("text");
		FragmentText f = (FragmentText)swipeAdapter.getFragment("text");
        f.setMessage(mds.getText());
        f.setActionbutton(true);
		viewPager.setCurrentItem(swipeAdapter.getFragmentName("text"), true);

	}

	@Override
	public void nextFragment(MdsMap mds) {
		viewPager
				.setCurrentItem(swipeAdapter.getFragmentName("location"), true);
	}

	/*
	 * @Override public void nextFragment(MdsInfoObject mdsInfo) { //
	 * Transaction int index = 0; //??? Fragment f = fragmentList.get(index); //
	 * f.setInfo(mdsInfo); }
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

		// TODO: Entry Handler wandelt keys und entry in JSON um
		
		try {
			webServ.send(WhiteboardHandler.toJson(keys, entry));
		} catch (NotYetConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownWhiteboardTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void consoleEntry(String message) {
		FragmentMonitoring f = (FragmentMonitoring) swipeAdapter
				.getFragment("monitoring");
		f.addConsoleEntry(message);
	}

	@Override
	public void showMap(ArrayList<MdsItem> items2display) {
		System.out.println("ShowMap aufgerufen");
		for(MdsItem i : items2display){
			System.out.println("Item: " + i.getName());
			System.out.println("Location: " + i.getLatitude() + i.getLongitude());
		} 
		mapFragment.setItemLocations(items2display);
		mapFragment.gmapsUpdate(mapFragment.getLastLocation());
	}

	@Override
	public void addToBackpack(MdsItem item) {
		FragmentBackpack f = (FragmentBackpack) swipeAdapter
				.getFragment("backpack");
		f.addItem(item);
	}

	public void getServerData(String type, int id) {
	}

	public ViewPager getViewPager(){
		return this.viewPager;
	}
	
	public void updateSwipeAdapter(String currFragment) {
		viewPager.setCurrentItem(1);
		swipeAdapter.removeFragment(currFragment);
		swipeAdapter.notifyDataSetChanged();
	}

	@Override
	public Activity getActivity() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public void onWebSocketMessage(String message) {
		
		JSONObject json = null;
		try {
			json = new JSONObject(message);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//TODO Abfrage ob message für Interpreter wichtig ist, oder z.B. Spieler Disconnect o.Ä.
		//json.get("updatemode").equals("full");
		
		interpreterCom.onWebsocketMessage(json);
	}

	public CharSequence getUsername() {
		return username;
	}

	public void setUsername(CharSequence username) {
		this.username = username;
	}
}
