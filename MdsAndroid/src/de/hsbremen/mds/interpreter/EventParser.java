package de.hsbremen.mds.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.location.Location;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsEvent;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsItem;

public class EventParser {
	
	public static boolean checkEvent(MdsEvent event, HashMap<String, MdsItem> items, Location loc) {
		
		// alle mit Typ gameEvent
		if (event.getType().equals("gameEvent")) {
			if(event.getName().equals("nearby")) {
				int radius = Integer.parseInt(event.getParams().get("radius"));
				// alle Items durchgehen und gucken ob genug vorhanden sind
				int quanti = Integer.parseInt(event.getParams().get("quantifier"));
				if (checkLocationItems(items, loc, radius).size() >= quanti) {
					return true;
				}
			}
		// keine richtigen Events, "nur" Conditions
		} else if (event.getType().equals("condition")) {
			// get value and compValue
			double value = -1;
			double compValue = -2;
			try {  
			    value = Double.parseDouble(event.getParams().get("value"));  
			} catch(NumberFormatException nfe) {  
				// TODO: was wenn da dynamische Werte drin sind? 
			} 
			try {  
			    compValue = Double.parseDouble(event.getParams().get("compValue"));  
			} catch(NumberFormatException nfe) {  
				// TODO: was wenn da dynamische Werte drin sind? 
			} 
			
			// get checkType
			if (event.getParams().get("checkType").equals("==")) {
				if (value == compValue) {
					return true;
				}
			} else if (event.getParams().get("checkType").equals("<")) {
				if (value < compValue) {
					return true;
				}
			} else if (event.getParams().get("checkType").equals(">")) {
				if (value > compValue) {
					return true;
				}
			} else if (event.getParams().get("checkType").equals("<=")) {
				if (value <= compValue) {
					return true;
				}
			} else if (event.getParams().get("checkType").equals(">=")) {
				if (value >= compValue) {
					return true;
				}
			}
		}
		// wenn kein Event gestimmt hat
		return false;	
	}
	
	/**
	 * Prüft wieviele Items innerhalb eines vordefinierten Radius von einer Position vorhanden sind
	 * @param items
	 * @param loc
	 * @param radius
	 * @return Liste mit Items innerhalb des Radius
	 */
	private static List<MdsItem> checkLocationItems(HashMap<String, MdsItem> items, Location loc, int radius ) {
		
		List<MdsItem> result = new ArrayList<MdsItem>();
		
		// Alle Items durchgehen
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
