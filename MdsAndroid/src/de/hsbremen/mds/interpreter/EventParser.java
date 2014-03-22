package de.hsbremen.mds.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.location.Location;
import de.hsbremen.mds.common.valueobjects.MdsObject;
import de.hsbremen.mds.common.valueobjects.MdsValueObject;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsItem;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsTransition;

public class EventParser {
	
	public MdsTransition checkEvents(HashMap<String, MdsItem> items, Location loc, MdsTransition[] mdsTransitions) {
		
		for(MdsTransition t : mdsTransitions) {
			// alle mit Typ gameEvent
			if (t.getEvent().getType().equals("gameEvent")) {
				if(t.getEvent().getName().equals("nearby")) {
					int radius = Integer.parseInt(t.getEvent().getParams().get("radius"));
					// alle Items durchgehen und gucken ob genug vorhanden sind
					int quanti = Integer.parseInt(t.getEvent().getParams().get("quantifier"));
					if (checkLocationItems(items, loc, radius).size() >= quanti) {
						return t;
					}
				}
			} else if (t.getEvent().getType().equals("condition")) {
				// get value and compValue
				double value = -1;
				double compValue = -2;
				try {  
				    value = Double.parseDouble(t.getEvent().getParams().get("value"));  
				} catch(NumberFormatException nfe) {  
					// TODO: was wenn da dynamische Werte drin sind? 
				} 
				try {  
				    compValue = Double.parseDouble(t.getEvent().getParams().get("compValue"));  
				} catch(NumberFormatException nfe) {  
					// TODO: was wenn da dynamische Werte drin sind? 
				} 
				
				// get checkType
				if (t.getEvent().getParams().get("checkType").equals("==")) {
					if (value == compValue) {
						return t;
					}
				} else if (t.getEvent().getParams().get("checkType").equals("<")) {
					if (value < compValue) {
						return t;
					}
				} else if (t.getEvent().getParams().get("checkType").equals(">")) {
					if (value > compValue) {
						return t;
					}
				} else if (t.getEvent().getParams().get("checkType").equals("<=")) {
					if (value <= compValue) {
						return t;
					}
				} else if (t.getEvent().getParams().get("checkType").equals(">=")) {
					if (value >= compValue) {
						return t;
					}
				}
			}
		}
		// wenn kein Event gestimmt hat
		return null;	
	}
	
	private List<MdsItem> checkLocationItems(HashMap<String, MdsItem> items, Location loc, int radius ) {
		
		List<MdsItem> result = new ArrayList<MdsItem>();
		
		for (String key : items.keySet()) {
			Location itemPos = new Location("ItemPos");
			itemPos.setLatitude(items.get(key).getLatitude());
			itemPos.setLatitude(items.get(key).getLongitude());
			if(itemPos.distanceTo(loc) <= radius) {
				result.add(items.get(key));
			}
		}
		return result;
		
	}
	
}
