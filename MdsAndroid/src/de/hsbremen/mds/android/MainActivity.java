package de.hsbremen.mds.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
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
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import de.hsbremen.mds.android.listener.AndroidInitiater;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.common.interfaces.GuiInterface;
import de.hsbremen.mds.common.interfaces.ServerInterpreterInterface;
import de.hsbremen.mds.common.listener.AndroidListener;
import de.hsbremen.mds.common.valueobjects.MdsImage;
import de.hsbremen.mds.common.valueobjects.MdsItem;
import de.hsbremen.mds.common.valueobjects.MdsMap;
import de.hsbremen.mds.common.valueobjects.MdsText;
import de.hsbremen.mds.common.valueobjects.MdsVideo;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.mdsandroid.R;

public class MainActivity extends FragmentActivity implements TabListener,
		LocationListener, GuiInterface , ServerInterpreterInterface{

	ActionBar actionBar;
	ViewPager viewPager;
	Location location;
	public AndroidInitiater initiater;
	LocationManager manager;
	double longitude;
	double latitude;
	MdsFragmentAdapter mfa = null;
	ActionBar.Tab tabMap = null;
	boolean initComplete = false;
	public ServerClientConnector connector;
	
	SocketClient socketClient;
	
	Thread t;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mfa = new MdsFragmentAdapter(getSupportFragmentManager());

		// Initiater für die Listener registrierung
		initiater = new AndroidInitiater();

		File jsonDatei = jsonEinlesen();

		
		// Interpreter Instanziert und sich selbst mitgegeben.

		viewPager = (ViewPager) findViewById(R.id.pager);

		viewPager.setAdapter(mfa);

		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int pos) {
				actionBar.setSelectedNavigationItem(pos);
			}
		});

		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mfa.addFragment("FragmentBackpack");
		mfa.addFragment("FragmentMap");
		mfa.addFragment("FragmentText");
		mfa.addFragment("FragmentVideo");

		addTab("Backpack");
		addTab("Map");
		addTab("Text");
		addTab("Video");

		// Hier wird der Interpreter erstellt und wir mitgegeben und als
		// Interface genutzt
		// TODO PlayerId vom Server holen (beim erstellen des Websockets)
		int playerId = 0;

		initComplete = true;

		// Serverkommunikation
		// Wichtig hier: Solange noch kein Server verfügbar ist die IP Adresse vom PC eingeben
		// auf dem der Server läuft(In der Hochschule wird das irgendwie geblockt, also kann man dort schlecht testen)
		connector = new ServerClientConnector(this, "feijnox.no-ip.org" ); // "192.168.1.5"
		
		MdsItem item = new MdsItem("ItemNummer1", "paaaaaath...");

		String jsonForServer = connector.objectToJsonString(item);
		
//		new Thread(){
//			@Override public void run() {
//				socketClient = connector.createSocket("Android");
//			}
//		}.start();
		
		t = new Thread(connector);
		t.start();

//		connector.httpGetString("/mds/appinfo");
	}

	public void redrawFragments(int number) {
		mfa.removeFragment(number);
		mfa.notifyDataSetChanged();
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	public void setTabMap() {
		actionBar.selectTab(tabMap);
	}

	public void gpsInit() {

		manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, // 1
																			// sec
				10, this);

		// showText("Hier werden ihre derzeitigen\n Koordinaten angezeigt.");

		boolean isNetworkEnabled = manager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (isNetworkEnabled) {
			showProviderEnable();
		} else {
			showProviderDisable();
		}

		updateLocationFields();

		System.out.println("GPS wurde initialisiert");
	}

	public void showProviderDisable() {
		TextView view = (TextView) findViewById(R.id.txtGPSVal);
		view.setText("AUS");
		view.setBackgroundColor(Color.RED);
		updateLocationFields();
	}

	public void showProviderEnable() {
		TextView view = (TextView) findViewById(R.id.txtGPSVal);
		view.setText("AN");
		view.setBackgroundColor(Color.GREEN);
		updateLocationFields();
		initiater.locationChanged(location);
	}

	public void updateLocationFields() {

		String latitude;
		String longitude;

		location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (location != null) {

			latitude = String.valueOf(location.getLatitude());
			longitude = String.valueOf(location.getLongitude());

		} else {

			latitude = "Kein Empfang!";
			longitude = "Kein Empfang!";

		}

		TextView latVal = (TextView) findViewById(R.id.txtLatVal);
		TextView longVal = (TextView) findViewById(R.id.txtLongVal);

		latVal.setText(latitude);
		longVal.setText(longitude);

		latVal.invalidate();
		longVal.invalidate();
	}

	@Override
	public void onLocationChanged(Location arg0) {
		updateLocationFields();
		//initiater.locationChanged(arg0);
		JSONObject json = new JSONObject();
		try {
			json.put("Latitude", arg0.getLatitude());
			json.put("Longitude", arg0.getLongitude());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//String json = "{ Longitude: " + arg0.getLongitude() + ", Latitude: " + arg0.getLatitude() + "}";
		
		connector.getSocket().send(json.toString());
		
//		t.notify();
		
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
		// mfa.addFragment("FragmentImage");
		// addTab("Image");
		viewPager.setCurrentItem(2);
		Button btn = (Button) findViewById(R.id.btnReturnImage);
		btn.setVisibility(1);
	}

	@Override
	public void nextFragment(MdsVideo mds) {
		// mfa.addFragment("FragmentVideo");
		// addTab("Video");
		viewPager.setCurrentItem(3);
		Button btn = (Button) findViewById(R.id.btnReturnVideo);
		btn.setVisibility(1);
	}

	@Override
	public void nextFragment(MdsText mds) {
		// mfa.addFragment("FragmentText");
		// addTab("Text");
		viewPager.setCurrentItem(1);
		TextView view = (TextView) findViewById(R.id.placeholderText);
		view.setText(mds.getText());
		Button btn = (Button) findViewById(R.id.btnReturnText);
		btn.setVisibility(1);
		Button btn2 = (Button) findViewById(R.id.btnShowVideo);
		btn2.setVisibility(1);
	}

	@Override
	public void nextFragment(MdsMap mds) {

		viewPager.setCurrentItem(0);

		if (initComplete) {
			TextView view = (TextView) findViewById(R.id.placeholderText);
			view.setText("Sie haben noch keine Sehenswürdigkeit erreicht");
		}
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

	public void removeTab(int site) {
		actionBar.removeTabAt(site);
	}

	/*
	 * Shows Toast for Debbuging
	 */
	public void toastShow(String text, int toastLength) {
		Toast.makeText(this, text, toastLength).show();
	}

	@Override
	public void getServerData(String type, int id) {
		String s = connector.httpGetString("/mds/" + type + "/" + id);
		if(type.equals("item")){
			addItemtoList(s);
		}else if(type.equals("player")){
			addPlayertoList(s);
		}
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
	public void onWhiteboardUpdate(List<String> keys, WhiteboardEntry value) {
		// TODO Auto-generated method stub
		
	}

}
