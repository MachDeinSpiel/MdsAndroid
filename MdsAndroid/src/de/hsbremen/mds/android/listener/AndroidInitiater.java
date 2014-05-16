package de.hsbremen.mds.android.listener;

import java.util.List;

import android.location.Location;
import android.util.Log;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.common.interfaces.AndroidListener;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;

public class AndroidInitiater {

	double positionIntervall;
	Location oldLocation;
    AndroidListener listener;
    
    // TODO Listener ersetzen durch AndroidInterpreterVermittler
    public AndroidInitiater(){
    	positionIntervall = 0;
    	oldLocation = null;
    	listener = null;
    }
    
    public void setListener(AndroidListener toAdd, double intervall) {
        this.listener = toAdd;
        this.positionIntervall = intervall;
        oldLocation = null;
        Log.d("Android", "Listener hinzugef�gt");
    }

    public void locationChanged(Location loc) {
    	 Log.d("Android", "LocationChanged");
    	if(listener != null) {
	    	//if(oldLocation == null || loc.distanceTo(oldLocation)>= positionIntervall) {
	            Log.d("Android", "Neue Position");
	    		// Callback mit aktueller Location
	    		listener.onPositionChanged(loc.getLongitude(), loc.getLatitude());
	    		oldLocation = loc;
	    	//}
    	}
    }

    public void buttonClicked(String name) {
    	if(listener != null) {
	        // Callback mit Event Informationen
	        listener.onButtonClick(name);
	    }
    }
    
    public void updateLocalWhiteboard(List<String> keys, WhiteboardEntry entry){
    	if(listener != null) {
	        // Callback mit Event Informationen
	        listener.updateLocalWhiteboard(keys, entry);
	    }
    }
    
    public void useItem(MdsItem item){
    	this.listener.useItem(item);
    }
    
    public void dropItem(MdsItem item){
    	// TODO: Dropitem noch einf�gen
//    	this.listener.dropItem(item);
    }
    
    
}
