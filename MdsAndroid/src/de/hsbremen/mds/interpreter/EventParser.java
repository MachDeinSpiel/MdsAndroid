package de.hsbremen.mds.interpreter;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsItem;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsTransition;

public class EventParser {
	
	public MdsTransition checkEvents(List<MdsItem> items, Location loc, List<MdsTransition> transitions) {
		
		for(MdsTransition t : transitions) {
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
	
	private List<MdsItem> checkLocationItems(List<MdsItem> items, Location loc, int radius ) {
		
		List<MdsItem> result = new ArrayList<MdsItem>();
		
		for (MdsItem i : items) {
			Location itemPos = new Location("ItemPos");
			itemPos.setLatitude(i.getLatitude());
			itemPos.setLatitude(i.getLongitude());
			if(itemPos.distanceTo(loc) <= radius) {
				result.add(i);
			}
		}
		return result;
		
	}
	
}
