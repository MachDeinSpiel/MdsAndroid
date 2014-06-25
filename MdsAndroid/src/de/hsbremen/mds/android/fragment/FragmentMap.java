package de.hsbremen.mds.android.fragment;


import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import de.hsbremen.mds.android.ingame.MainActivity;
import de.hsbremen.mds.mdsandroid.R;

public class FragmentMap extends Fragment{
	
	private LocationManager manager; 
    private Location location;
    private View mapView;
    private int style;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		
		mapView = inflater.inflate(R.layout.fragment_location, container,false);
		
		// Style des Fragments anpassen
		MainActivity a = (MainActivity)getActivity();
		style = a.getStyleNumber();
		styleFragment(mapView);
		
		return mapView;
	}
	
	private void styleFragment(View view) {
		
		int styleText = 0;
		int styleLabel = 0;
		int styleButton = 0;
		int styleLabelBgr = 0;
		
		switch(style){
			case 0:
				styleText = R.style.textColorDefaultBlue;
				styleLabel = R.style.labelDefault;
				styleButton = R.drawable.buttonshape;
				styleLabelBgr = R.drawable.labelshape;
				break;
			case 1:
				styleText = R.style.textColorDarkBlue;
				styleLabel = R.style.labelDark;
				styleButton = R.drawable.buttonshapedark;
				styleLabelBgr = R.drawable.labelshapedark;
				break;
		}
	}

//	public void gpsInit() {
//		
//		MainActivity activity = (MainActivity) getActivity();
//
//		manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//
//		manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, // 1
//																			// sec
//				10, activity);
//
//		boolean isNetworkEnabled = manager
//				.isProviderEnabled(LocationManager.GPS_PROVIDER);
//
//		if (isNetworkEnabled) {
//			activity.showProviderEnable();
//		} else {
//			activity.showProviderDisable();
//		}
//
//		updateLocationFields();
//	}
	
//	public void updateLocationFields() {
//
//		String latitude;
//		String longitude;
//
//		if(manager != null){
//			location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//	
//			System.out.println("Location gesetzt");
//			
//			if (location != null) {
//	
//				latitude = String.valueOf(location.getLatitude());
//				longitude = String.valueOf(location.getLongitude());
//	
//			} else {
//	
//				latitude = "Kein Empfang!";
//				longitude = "Kein Empfang!";
//	
//			}
//	
//			TextView latVal = (TextView) mapView.findViewById(R.id.txtLatVal);
//			TextView longVal = (TextView) mapView.findViewById(R.id.txtLongVal);
//	
//			latVal.setText(latitude);
//			longVal.setText(longitude);
//	
//			latVal.invalidate();
//			longVal.invalidate();
//		}
//	}
}
