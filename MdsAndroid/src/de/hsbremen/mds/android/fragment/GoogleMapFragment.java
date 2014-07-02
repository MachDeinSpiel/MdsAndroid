package de.hsbremen.mds.android.fragment;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import de.hsbremen.mds.android.ingame.ImageLoader;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.mdsandroid.R;

public class GoogleMapFragment extends MapFragment {

	private GoogleMap map;
	private Location lastLocation;
	private ArrayList<MdsItem> itemLocations = null;
	private Location playerlocation;
	private ImageLoader imageLoader;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initGMaps();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		imageLoader = new ImageLoader();
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable("LAST_LOCATION", lastLocation);
		super.onSaveInstanceState(outState);
	}
	
	private void initGMaps(){
  
		  map = getMap();
		  // map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		  // map.setMapType(GoogleMap.MAP_TYPE_NONE);
		  map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		  // map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		  // map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		  
		  LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		  Criteria criteria = new Criteria();
		  criteria.setAccuracy(Criteria.ACCURACY_FINE);
		  String provider = lm.getBestProvider(criteria, true);
	}
	

	public void gmapsUpdate(Location loc){
		
		if(map != null){
			map.clear();
			
			if(itemLocations != null){
				for(MdsItem i : itemLocations){
					MarkerOptions mpi = new MarkerOptions();
					mpi.position(new LatLng(i.getLatitude(), i.getLongitude()));
					mpi.title(i.getName());
					
					Bitmap icon = imageLoader.getBitmapFromURL(i.getImagePath());

					if(icon == null){
						icon = BitmapFactory.decodeResource(getResources(), R.drawable.unknown);
					}
					
					// Scale big images so normsize
					int height = icon.getHeight();
					int width = icon.getWidth();
					int weight = height/width;
					Bitmap resized =Bitmap.createScaledBitmap(icon, 100,100*weight, false);
					
					mpi.icon(BitmapDescriptorFactory.fromBitmap(resized));
					map.addMarker(mpi);
				}
			}
	
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(
			new LatLng(loc.getLatitude(), loc.getLongitude()), 16));
			
			if(!loc.getProvider().equals("dummie")){
				playerlocation = loc;
			}
		
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(
			new LatLng(playerlocation.getLatitude(), playerlocation.getLongitude()), 16));
		}
	}
	
	public void setItemLocations(ArrayList<MdsItem> itemLocations){
		this.itemLocations = itemLocations;
	}
	
	public Location getLastLocation(){
		return this.lastLocation;
	}
}
