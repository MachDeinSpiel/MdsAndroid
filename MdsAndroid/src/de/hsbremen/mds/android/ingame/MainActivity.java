package de.hsbremen.mds.android.ingame;

import java.io.File;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
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
		
		//Remove title bar
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);

	    //Remove notification bar
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_main);

		Bundle extras = getIntent().getExtras();
		username = extras.getCharSequence("username");

		webServ = WebServices.createWebServices(this);

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

		// Initiater für die Listener registrierung
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
	public void onLocationChanged(Location loc) {
		
		mapFragment.gmapsUpdate(loc);

		interpreterCom.locationChanged(loc);
	}

	@Override
	public void onProviderDisabled(String arg0) {
	}

	@Override
	public void onProviderEnabled(String arg0) {
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

			// TODO Abfrage ob message fŸr Interpreter wichtig ist, oder z.B.
			// Spieler Disconnect o.€.
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
		if(!(mds.getName().equals("showMap")) && !(mds.getName().equals("backToMap"))){
		

			
			if(!(mds.getName().equals("showMap"))){
				swipeAdapter.setFragmentInformation(mds);
				swipeAdapter.addFragment(mds.getName());
			}
			viewPager.setCurrentItem(swipeAdapter.getFragmentName(mds.getName()), true);
		}
		
		if((mds.getName().equals("backToMap"))){
			if(swipeAdapter.getCount() == 3){
				updateSwipeAdapter(swipeAdapter.getLastFragmentName());
			}
		}
	}
	
	public int getStyleNumber(){
		return this.style;
	}

	@Override
	public void removeFromBackpack(String itemPathKey) {
		FragmentInventory f = (FragmentInventory) swipeAdapter.getFragment("inventory");
		f.removeItem(itemPathKey);
	}

	@Override
	public void onWebserviceConnectionClosed(int code, String reason,
			boolean remote) {
	}
	
	public void gpsInit() {
	
		MainActivity activity = (MainActivity) getActivity();
	
		manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
	
		manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10, activity);
	}

	@Override
	public void onWebSocketConnected() {
		
	}

	@Override
	public void setPlayerData(HashMap<String, Object> dataMap) {
		FragmentMap f = (FragmentMap)swipeAdapter.getFragment("showMap");
		
		validateDataMap(dataMap);
		Log.i("image", "Size von Hashmap: " + dataMap.size());
		
		int iterator = 0;
		
		for(String key : dataMap.keySet()){
			if(iterator < 3){
				if(key.equals("health")){
					f.setHealthbar(((int[])dataMap.get(key))[1], ((int[])dataMap.get(key))[0]);
				}
				else if(iterator == 2){
					f.setOptional(key, ((String)dataMap.get(key)));
				}else{
					f.setScore(key, ((String)dataMap.get(key)));
				}
				iterator++;
			}
		}
	}

	private void validateDataMap(HashMap<String, Object> dataMap) {

		LinearLayout healthlayout = (LinearLayout)findViewById(R.id.healthBarCont);
		LinearLayout scoreLayout = (LinearLayout)findViewById(R.id.scoreContainer);
		LinearLayout optionalLayout = (LinearLayout)findViewById(R.id.optionalContainer);
		
		if(healthlayout != null || scoreLayout != null || optionalLayout != null) {
			if(dataMap.size() >= 3 && dataMap.containsKey("health")){
				Log.i("image", "1");
				healthlayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
				scoreLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
				optionalLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
			}else if(dataMap.size() == 2 && dataMap.containsKey("health")){
				Log.i("image", "2");
				healthlayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
				scoreLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
				optionalLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0f));
			}else if(dataMap.size() == 1 && dataMap.containsKey("health")){
				Log.i("image", "3");
				healthlayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
				scoreLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0f));
				optionalLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0f));
			}else if(dataMap.size() == 1 && !dataMap.containsKey("health")){
				Log.i("image", "4");
				healthlayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0f));
				scoreLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
				optionalLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0f));
			}else if(dataMap.size() == 2 && !dataMap.containsKey("health")){
				Log.i("image", "5");
				healthlayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0f));
				scoreLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
				optionalLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
			}else if(dataMap.size() >= 3 && !dataMap.containsKey("health")){
				Log.i("image", "6");
				healthlayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0f));
				scoreLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
				optionalLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
			}
		}
	}

	
	
	
	@Override
	public void endGame() {
		showEndDialog();
	}
	
	private void showEndDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Game ended")
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   finish();
                   }
               })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   finish();
                   }
               }).show();
	}
	
	
}
