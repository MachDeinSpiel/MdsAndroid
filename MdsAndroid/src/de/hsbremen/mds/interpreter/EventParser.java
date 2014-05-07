package de.hsbremen.mds.interpreter;

import java.util.List;
import java.util.Vector;

import de.hsbremen.mds.common.valueobjects.statemachine.MdsEvent;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;

public class EventParser {
	
	public static boolean checkEvent(MdsEvent toCheck, MdsEvent compliedEvent, Whiteboard wb, int playerId) {
		
		// alle mit Typ gameEvent
		if (toCheck.getType().equals("gameEvent")) {
			if(toCheck.getName().equals("nearby")) {
				double longitude = (Double) wb.getAttribute("players", Integer.toString(playerId), "longitude").value;
				double latitude = (Double) wb.getAttribute("players", Integer.toString(playerId), "latitude").value;
				int radius = Integer.parseInt(toCheck.getParams().get("radius"));
				// alle Items durchgehen und gucken ob genug vorhanden sind
				int quanti = Integer.parseInt(toCheck.getParams().get("quantifier"));
				//Unterwhiteboard (z.B. die Gruppe "exhbitis") wird anhand des parameter "target" ermittelt
				//Dafür wird der String dafür bei jedem Punkt geteilt, in einen Array gepackt und davon die value als Whiteboard gecastet
				Whiteboard target = (Whiteboard)wb.getAttribute(toCheck.getParams().get("target").split(".")).value;
				///TODO: Quantifier ist nicht nur eine Zahl, sondern sowas wie all, none, >5, = 3 usw
				if (getEntriesNearTo(target, longitude, latitude, radius).size() >= quanti) {
					return true;
				}
			}
		// keine richtigen Events, "nur" Conditions
		} else if (toCheck.getType().equals("condition")) {
			// get value and compValue
			double value = -1;
			double compValue = -2;
			try {  
			    value = Double.parseDouble(toCheck.getParams().get("value"));  
			} catch(NumberFormatException nfe) {  
				// TODO: was wenn da dynamische Werte drin sind? 
			} 
			try {  
			    compValue = Double.parseDouble(toCheck.getParams().get("compValue"));  
			} catch(NumberFormatException nfe) {  
				// TODO: was wenn da dynamische Werte drin sind? 
			} 
			
			// get checkType
			if (toCheck.getParams().get("checkType").equals("==")) {
				if (value == compValue) {
					return true;
				}
			} else if (toCheck.getParams().get("checkType").equals("<")) {
				if (value < compValue) {
					return true;
				}
			} else if (toCheck.getParams().get("checkType").equals(">")) {
				if (value > compValue) {
					return true;
				}
			} else if (toCheck.getParams().get("checkType").equals("<=")) {
				if (value <= compValue) {
					return true;
				}
			} else if (toCheck.getParams().get("checkType").equals(">=")) {
				if (value >= compValue) {
					return true;
				}
			}
		} else if (toCheck.getType().equals("uiEvent") && compliedEvent != null) {
			//Wenn ein uiEvent geprüft werden soll, wird es stumpf verglichen
			return (toCheck.getType().equals(compliedEvent.getType()) && toCheck.getName().equals(compliedEvent.getName()));
			
		}
		// wenn kein Event gestimmt hat
		return false;	
	}
	
	/**
	 * Prüft wieviele Einträge eines Whiteboards, die die Attribute Longitude und Latitude haben, innerhalb eines vordefinierten Radius von einer Position vorhanden sind
	 * @param wb Whiteboard, dessen Einträge geprüft werden soll
	 * @param longitude
	 * @param latitude
	 * @param radius
	 * @return Liste mit Items innerhalb des Radius
	 */
	private static List<WhiteboardEntry> getEntriesNearTo(Whiteboard wb, double longitude, double latitude, int radius ) {
		
		List<WhiteboardEntry> result = new Vector<WhiteboardEntry>();
		
		// Alle Einträge durchgehen
		for (String key :  wb.keySet()) {
			Whiteboard entry = (Whiteboard) wb.getAttribute(key).value;
			if(entry.containsKey("longitude") && entry.containsKey("latitude"));
			double longi = Double.parseDouble(""+ entry.getAttribute("longitude").value);
			double lati = Double.parseDouble(""+ entry.getAttribute("latitude").value);
			if(distanceInMeter(longitude, latitude, longi, lati) <= radius) {
				result.add(wb.getAttribute(key));
			}
		}
		return result;
		
	}
	
	
	
	private static double distanceInMeter(double p1long, double p1lat, double p2long, double p2lat){
		//Code von https://stackoverflow.com/questions/3715521/how-can-i-calculate-the-distance-between-two-gps-points-in-java
		double d2r = Math.PI / 180;
		double distance = 0;
		double dlong = (p2long - p1long) * d2r;
		double dlat = (p2lat - p1lat) * d2r;
		double a =
				Math.pow(Math.sin(dlat / 2.0), 2)
	            + Math.cos(p1lat * d2r)
	            * Math.cos(p2lat * d2r)
	            * Math.pow(Math.sin(dlong / 2.0), 2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    double d = 6367 * c;

	    return d;
	}
	
}
