package de.hsbremen.mds.interpreter;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import android.location.Location;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsCondition;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;

public class EventParser {
	
//	// TODO: Exception no Eventtype found
//	public static boolean checkEvent(String btnName, MdsTransition trans, Whiteboard wb, int playerId) {
//		if (trans.getEvent().getName().equals("whiteboardEvent")) {
//			return checkWhiteboardEvent(trans.getCondition(), wb, playerId);
//		} else if (trans.getEvent().getName().equals("locationEvent")) {
//			return checkLocationEvent(trans.getCondition(), wb, playerId);
//		} else if (trans.getEvent().getName().equals("conditionEvent")) {
//			return checkConditionEvent(trans.getCondition(), wb, playerId);
//		} else if (trans.getEvent().getName().equals("uiEvent")) {
//			return checkUiEvent(btnName, trans.getCondition(), wb, playerId);
//		}
//		// wenn keines der Eventtypen stimmt ist wohl was schief gegangen
//		return false;
//	}
	
	public static Result checkWhiteboardEvent(MdsCondition condition,Whiteboard wb, int playerId) {
		Result result = new Result(false, null, null);
		result = checkLocationEvent(condition, wb, playerId);
		if (result.isfullfilled)
			return result;
		result = checkConditionEvent(condition, wb, playerId);
		return result;
	}

	public static Result checkLocationEvent(MdsCondition cond, Whiteboard wb, int playerId) {
		if(cond.getName().equals("nearby")) {
			if(cond.getParams().get("subject").equals("self")){
				double longitude = (Double) wb.getAttribute("players", Integer.toString(playerId), "longitude").value;
				double latitude = (Double) wb.getAttribute("players", Integer.toString(playerId), "latitude").value;
				Location playerLoc = new Location("PlayerLoc");
				playerLoc.setLatitude(latitude);
				playerLoc.setLongitude(longitude);
				int radius = Integer.parseInt(cond.getParams().get("radius"));
				// alle Items durchgehen und gucken ob genug vorhanden sind
				int quanti = Integer.parseInt(cond.getParams().get("quantifier"));
				//Unterwhiteboard (z.B. die Gruppe "exhbitis") wird anhand des parameter "target" ermittelt
				//Daf�r wird der String daf�r bei jedem Punkt geteilt, in einen Array gepackt und davon die value als Whiteboard gecastet
				Whiteboard object = (Whiteboard)wb.getAttribute(cond.getParams().get("object").split(".")).value;
				///TODO: Quantifier beachten
				List<WhiteboardEntry> objects = getEntriesNearTo(object, playerLoc, radius);
				WhiteboardEntry[] playerArray= {(WhiteboardEntry) wb.getAttribute("players",""+playerId).value}; 
				List<WhiteboardEntry> subject = Arrays.asList(playerArray);
				if (objects.size() >= quanti) {
					
					return new Result(true, subject, objects);
				}
			}else{
				//TODO: subject ist gruppe oder was anderes
			}
		}
		return new Result(false, null, null);
	}
	
	// TODO: ValueNotANumber Exception
	public static Result checkConditionEvent(MdsCondition cond, Whiteboard wb, int playerId) {
			// get value and compValue
			double value = -1;
			double compValue = -2;
			try {  
			    value = Double.parseDouble(cond.getParams().get("value"));  
			} catch(NumberFormatException nfe) {  
				try {
					// TODO: Was ist mit L�ngen / Gr��en von Gruppen?
					//Einzelne Teile, die Punkten getrennt sind aufsplitten und den Wert des Arributs in Double parsen
					value = Double.parseDouble((String) wb.getAttribute(cond.getParams().get("value").split(".")).value);
				} catch (NumberFormatException nfe2) {
					// something went wrong, Value is not a Number
				}
			} 
			try {  
			    compValue = Double.parseDouble(cond.getParams().get("compValue"));  
			} catch(NumberFormatException nfe) {  
				try {
					//Einzelne Teile, die Punkten getrennt sind aufsplitten und den Wert des Arributs in Double parsen
					value = Double.parseDouble((String) wb.getAttribute(cond.getParams().get("value").split(".")).value);
				} catch (NumberFormatException nfe2) {
					// something went wrong, Value is not a Number
				} 
			} 
			
			// get checkType
			if (cond.getParams().get("checkType").equals("==")) {
				if (value == compValue) return new Result(true, null, null);
			} else if (cond.getParams().get("checkType").equals("<")) {
				if (value < compValue) return new Result(true, null, null);
			} else if (cond.getParams().get("checkType").equals(">")) {
				if (value > compValue) return new Result(true, null, null);
			} else if (cond.getParams().get("checkType").equals("<=")) {
				if (value <= compValue) return new Result(true, null, null);
			} else if (cond.getParams().get("checkType").equals(">=")) {
				if (value >= compValue) return new Result(true, null, null);
			}
		return new Result(false, null, null);
	}
	
	/**
	 * Wir vergleichen hier den ButtonNamen, der von Android mitgegeben wird, mit dem EventNamen
	 * @param btnName
	 * @param cond
	 * @param wb
	 * @param playerId
	 * @return
	 */
	public static Result checkUiEvent(String btnName, MdsCondition cond, Whiteboard wb, int playerId) {
		//Wenn ein uiEvent gepr�ft werden soll, wird es stumpf verglichen
		return new Result((cond.getName().equals(btnName)), null, null);
	}
	
	/**
	 * Pr�ft wieviele Eintr�ge eines Whiteboards, die die Attribute Longitude und Latitude haben, innerhalb eines vordefinierten Radius von einer Position vorhanden sind
	 * @param wb Whiteboard, dessen Eintr�ge gepr�ft werden soll
	 * @param longitude
	 * @param latitude
	 * @param radius
	 * @return Liste mit Items innerhalb des Radius
	 */
	private static List<WhiteboardEntry> getEntriesNearTo(Whiteboard wb, Location playerLoc, int radius ) {
		
		List<WhiteboardEntry> result = new Vector<WhiteboardEntry>();
		
		// Alle Eintr�ge durchgehen
		for (String key :  wb.keySet()) {
			Whiteboard entry = (Whiteboard) wb.getAttribute(key).value;
			if(entry.containsKey("longitude") && entry.containsKey("latitude")) {
				// Location Object des Objects erstellen
				double longi = Double.parseDouble(""+ entry.getAttribute("longitude").value);
				double lati = Double.parseDouble(""+ entry.getAttribute("latitude").value);
				Location loc = new Location("WBLoc");
				loc.setLatitude(lati);
				loc.setLongitude(longi);
				// Distanz mit Radius vergleichen
				if(playerLoc.distanceTo(loc) <= radius) {
					result.add(wb.getAttribute(key));
				}
			}
		}
		return result;
		
	}
	
	
	static public class Result{
		public boolean isfullfilled;
		public List<WhiteboardEntry> subjects;
		public List<WhiteboardEntry> objects;
		
		public Result(boolean iff, List<WhiteboardEntry> sub, List<WhiteboardEntry> obj){
			isfullfilled = iff;
			subjects = sub;
			objects = obj;
		}
	}
	
//	private static double distanceInMeter(double p1long, double p1lat, double p2long, double p2lat){
//		//Code von https://stackoverflow.com/questions/3715521/how-can-i-calculate-the-distance-between-two-gps-points-in-java
//		double d2r = Math.PI / 180;
//		double distance = 0;
//		double dlong = (p2long - p1long) * d2r;
//		double dlat = (p2lat - p1lat) * d2r;
//		double a =
//				Math.pow(Math.sin(dlat / 2.0), 2)
//	            + Math.cos(p1lat * d2r)
//	            * Math.cos(p2lat * d2r)
//	            * Math.pow(Math.sin(dlong / 2.0), 2);
//	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//	    double d = 6367 * c;
//
//	    return d;
//	}
	
}
