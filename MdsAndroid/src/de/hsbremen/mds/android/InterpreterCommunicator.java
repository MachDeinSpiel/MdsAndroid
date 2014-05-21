package de.hsbremen.mds.android;

import java.util.List;

import android.location.Location;
import android.util.Log;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.common.interfaces.ClientInterpreterInterface;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.common.whiteboard.WhiteboardUpdateObject;

public class InterpreterCommunicator {

	double positionIntervall;
	Location oldLocation;
    ClientInterpreterInterface interpreter;
    
    // TODO Listener ersetzen durch AndroidInterpreterVermittler
    public InterpreterCommunicator(ClientInterpreterInterface interpreter, double intervall){
    	positionIntervall = intervall;
    	oldLocation = null;
    	this.interpreter = interpreter;
    }

    public void locationChanged(Location loc) {
    	if(interpreter != null && loc != null) {
	    	if(oldLocation == null || loc.distanceTo(oldLocation)>= positionIntervall) {
	            Log.d("Android", "Neue Position");
	    		// Callback mit aktueller Location
	    		interpreter.onPositionChanged(loc.getLongitude(), loc.getLatitude());
	    		oldLocation = loc;
	    	}
    	}
    }

    public void buttonClicked(String name) {
    	if(interpreter != null) {
	        // Callback mit Event Informationen
	        interpreter.onButtonClick(name);
	    }
    }
    
    public void updateLocalWhiteboard(List<String> keys, WhiteboardEntry entry){
    	if(interpreter != null) {
	        // Callback mit Event Informationen
	        interpreter.onWhiteboardUpdate(keys, entry);
	    }
    }
    
    public void fullUpdateLocalWhiteboard(List<WhiteboardUpdateObject> list){
    	if(interpreter != null) {
	        // Callback mit Event Informationen
	        interpreter.onFullWhiteboardUpdate(list);
	    }
    }
    
    public void useItem(MdsItem item){
    	this.interpreter.useItem(item);
    }
    
    public void dropItem(MdsItem item){
    	
    	this.interpreter.dropItem(item);
    }
    
    
}
