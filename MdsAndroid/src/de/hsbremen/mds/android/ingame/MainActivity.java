package de.hsbremen.mds.android.ingame;

import java.io.File;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import de.hsbremen.mds.android.communication.InterpreterCommunicator;
import de.hsbremen.mds.android.communication.WebServices;
import de.hsbremen.mds.android.communication.WebServicesInterface;
import de.hsbremen.mds.android.fragment.FragmentInventory;
import de.hsbremen.mds.android.fragment.FragmentMap;
import de.hsbremen.mds.android.fragment.GoogleMapFragment;
import de.hsbremen.mds.common.communication.WhiteboardHandler;
import de.hsbremen.mds.common.exception.UnknownWhiteboardTypeException;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.common.interfaces.GuiInterface;
import de.hsbremen.mds.common.interfaces.ServerInterpreterInterface;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsInfoObject;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.interpreter.Interpreter;
import de.hsbremen.mds.mdsandroid.R;

public class MainActivity extends FragmentActivity implements LocationListener,
		GuiInterface, ServerInterpreterInterface, WebServicesInterface {

	public InterpreterCommunicator interpreterCom;

	private GoogleMapFragment mapFragment;

	private ViewPager viewPager;
	public SwipeAdapter swipeAdapter;

	private CharSequence username;

	public WebServices webServ;
	
	private int style = 0;
	
	private LocationManager manager; 

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
		setOnPageChangedListener();

		FragmentManager fm = getFragmentManager();
		mapFragment = (GoogleMapFragment) fm.findFragmentById(R.id.map);

		styleFragment();
		
		// Interpreter Erstellung
		File jsonDatei = (File) extras.get("spielejson");
		
		Log.d("Menu", "JSON: " + jsonDatei.toString());

		// Hier wird der Interpreter erstellt und wir mitgegeben und als
		// Interface genutzt
		Interpreter interpreter = new Interpreter(jsonDatei, this, this,
				username.toString());

		// Initiater f¸r die Listener registrierung
		interpreterCom = new InterpreterCommunicator(interpreter, 5);
		
		try {
			interpreterCom.onWebsocketMessage(new JSONObject(extras.getString("json")));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		gpsInit();
	}
	
	private void setOnPageChangedListener() {
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				LinearLayout l = (LinearLayout) findViewById(R.id.containerMap);
				LinearLayout l2 = (LinearLayout) findViewById(R.id.containerPager);
				if(arg0 == 0){
					l.setLayoutParams(new TableLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT, 0, 4f));
					l2.setLayoutParams(new TableLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT, 0, 1f));			
				}else{
					l.setLayoutParams(new TableLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT, 0, 1f));
					l2.setLayoutParams(new TableLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT, 0, 4f));
				}	
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
	}

	private void styleFragment(){
		
		FrameLayout f = (FrameLayout)findViewById(R.id.container);
		
		switch(style){
			case 0: 
				f.setBackgroundColor(Color.parseColor("#fafafa"));
				break;
			case 1:
				f.setBackgroundColor(Color.parseColor("#6e6e6e"));
				break;
			default:
				break;
		}
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

//		LinearLayout l = (LinearLayout) findViewById(R.id.containerMap);
//		LinearLayout l2 = (LinearLayout) findViewById(R.id.containerPager);
//
//		if (item.getItemId() == R.id.toggleMap) {
//
//			if (l.getHeight() > l2.getHeight()) {
//				l.setLayoutParams(new TableLayout.LayoutParams(
//						LayoutParams.WRAP_CONTENT, 0, 1.5f));
//				l2.setLayoutParams(new TableLayout.LayoutParams(
//						LayoutParams.WRAP_CONTENT, 0, 3.5f));
//			} else {
//				l.setLayoutParams(new TableLayout.LayoutParams(
//						LayoutParams.WRAP_CONTENT, 0, 3.5f));
//				l2.setLayoutParams(new TableLayout.LayoutParams(
//						LayoutParams.WRAP_CONTENT, 0, 1.5f));
//			}
//		}
		return true;
	}

	@Override
	public void onLocationChanged(Location loc) {
		
		mapFragment.gmapsUpdate(loc);

		interpreterCom.locationChanged(loc);
	}

//	public void showProviderDisable() {
//		TextView view = (TextView) findViewById(R.id.txtGPSVal);
//		view.setText("AUS");
//		view.setBackgroundColor(Color.RED);
//
//		FragmentLocation f = (FragmentLocation) swipeAdapter
//				.getFragment("showMap");
//		f.updateLocationFields();
//	}
//
//	public void showProviderEnable() {
//		TextView view = (TextView) findViewById(R.id.txtGPSVal);
//		view.setText("AN");
//		view.setBackgroundColor(Color.GREEN);
//
//		FragmentLocation f = (FragmentLocation) swipeAdapter
//				.getFragment("showMap");
//		if(f != null){
//			f.updateLocationFields();
//		}
//
//		interpreterCom.locationChanged(location);
//	}

	@Override
	public void onProviderDisabled(String arg0) {
//		showProviderDisable();
	}

	@Override
	public void onProviderEnabled(String arg0) {
//		showProviderEnable();
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}

	@Override
	public void update() {
	}

	@Override
	public void onWhiteboardUpdate(List<String> keys, WhiteboardEntry entry) {

		// TODO: Entry Handler wandelt keys und entry in JSON um

		try {
			webServ.send(WhiteboardHandler.toJson(keys, entry));
		} catch (NotYetConnectedException e) {
			e.printStackTrace();
		} catch (UnknownWhiteboardTypeException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void showMap(ArrayList<MdsItem> items2display) {
		mapFragment.setItemLocations(items2display);
		Location l = new Location("dummie");
		mapFragment.gmapsUpdate(l);
	}

	@Override
	public void addToBackpack(MdsItem item) {
		FragmentInventory f = (FragmentInventory) swipeAdapter.getFragment("inventory");
		f.addItem(item);
	}

	public void getServerData(String type, int id) {
	}

	public ViewPager getViewPager() {
		return this.viewPager;
	}

	public void updateSwipeAdapter(String currFragment) {
		viewPager.setCurrentItem(swipeAdapter.getFragmentName("showMap"));	
		swipeAdapter.removeFragment(currFragment);
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public void onWebSocketMessage(String message) {

		try {
			final JSONObject json = new JSONObject(message);

			// TODO Abfrage ob message für Interpreter wichtig ist, oder z.B.
			// Spieler Disconnect o.Ä.
			// json.get("updatemode").equals("full");
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.d("Socket", "MainActivity: JSSSSSSSOOOOON: " + json.toString());
					interpreterCom.onWebsocketMessage(json);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public CharSequence getUsername() {
		return username;
	}

	public void setUsername(CharSequence username) {
		this.username = username;
	}

	@Override
	public void nextFragment(MdsInfoObject mds) {
		
			System.out.println("NextFragment aufgerufen mit: " + mds.getName());
			
			if(!(mds.getName().equals("showMap"))){
				swipeAdapter.setFragmentInformation(mds);
				swipeAdapter.addFragment(mds.getName());
			}
	
			viewPager.setCurrentItem(swipeAdapter.getFragmentName(mds.getName()),
					true);
	}
	
	public int getStyleNumber(){
		return this.style;
	}

	@Override
	public void removeFromBackpack(MdsItem item) {
		FragmentInventory f = (FragmentInventory) swipeAdapter.getFragment("inventory");
		f.removeItem(item);
	}

	@Override
	public void onWebserviceConnectionClosed(int code, String reason,
			boolean remote) {
	}
	
	public void gpsInit() {
	
	MainActivity activity = (MainActivity) getActivity();

	manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

	manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, // 1
																		// sec
			10, activity);
	}

	@Override
	public void onWebSocketConnected() {
		
	}
}
