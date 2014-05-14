package de.hsbremen.mds.interpreter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.location.Location;
import de.hsbremen.mds.common.valueobjects.MdsObject;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsCondition;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsQuantifier;
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
		
		// ------------------ Werte holen ---------------//
		// get Params
		HashMap<String, Object> params = cond.getParams();
		// get Object
		MdsObject object = (MdsObject) params.get("object");
		// get Subject
		MdsObject subject = (MdsObject) params.get("subject");
		// get Object Quantifier
		MdsQuantifier objQuanti = object.getQuantifier();
		// get Subject Quantifier
		MdsQuantifier subQuanti = subject.getQuantifier();
		// get Radius
		int radius = Integer.parseInt((String) params.get("radius"));
		// get Quantivalue
		int quanti = Integer.parseInt(objQuanti.getValue());
		// get QuanticheckType
		String checkType = objQuanti.getChecktype();
		
		// -------------- alle Verschiedenen Location Events -----------//
		if(cond.getName().equals("nearby")) {
			// wenn das Subject "self" ist, im Einzelspieler immer
			if(subject.getName().equals("self")){
				// Locationobjekt des Spielers erzeuegen
				double longitude = (Double) wb.getAttribute("players", Integer.toString(playerId), "longitude").value;
				double latitude = (Double) wb.getAttribute("players", Integer.toString(playerId), "latitude").value;
				Location playerLoc = new Location("PlayerLoc");
				playerLoc.setLatitude(latitude);
				playerLoc.setLongitude(longitude);

				//Unterwhiteboard (z.B. die Gruppe "exhbitis") wird anhand des parameter "target" ermittelt
				//Dafür wird der String dafür bei jedem Punkt geteilt, in einen Array gepackt und davon die value als Whiteboard gecastet

				Whiteboard realObject = (Whiteboard)wb.getAttribute(object.getName().split("\\.")).value;

				List<WhiteboardEntry> objects = getEntriesNearTo(realObject, playerLoc, radius);
				
				// CheckType des Quantifiers identifizieren
				if (checkType.equals(MdsCondition.EQUALS)) {
					if (objects.size() == quanti) return new Result(true, null, null);
				} else if (checkType.equals(MdsQuantifier.LOWER)) {
					if (objects.size() < quanti) return new Result(true, null, null);
				} else if (checkType.equals(MdsQuantifier.HIGHER)) {
					if (objects.size() > quanti) return new Result(true, null, null);
				} else if (checkType.equals(MdsQuantifier.LOWEQUALS)) {
					if (objects.size() <= quanti) return new Result(true, null, null);
				} else if (checkType.equals(MdsQuantifier.HIGHEQUALS)) {
					if (objects.size() >= quanti) return new Result(true, null, null);
				} else if (checkType.equals(MdsQuantifier.EXISTS)) {
					if (objects.size() >= 1) return new Result(true, null, null);
				} else if (checkType.equals(MdsQuantifier.ALL)) {
					if (objects.size() == realObject.entrySet().size()) return new Result(true, null, null);
				} 
				//TODO : Error if quantifier is invalid
			}else{
				// TODO: Hier muss noch viel überlegt werden bei Multiplayer
				//Unterwhiteboard (z.B. die Gruppe "exhbitis") wird anhand des parameter "target" ermittelt
				//Dafür wird der String dafür bei jedem Punkt geteilt, in einen Array gepackt und davon die value als Whiteboard gecastet

				Whiteboard realSubject = (Whiteboard)wb.getAttribute(object.getName().split("\\.")).value;
				//Unterwhiteboard (z.B. die Gruppe "exhbitis") wird anhand des parameter "target" ermittelt
				//Dafür wird der String dafür bei jedem Punkt geteilt, in einen Array gepackt und davon die value als Whiteboard gecastet
				Whiteboard realObject = (Whiteboard)wb.getAttribute(((String)cond.getParams().get("object")).split("\\.")).value;

				// Location des Objekts
				Location someLoc = new Location("somLoc");
				// FIXME:
				// Hier evtl eine Liste von Locations?
				List<WhiteboardEntry> objects = getEntriesNearTo(realObject, someLoc, radius);
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
			    value = Double.parseDouble((String) cond.getParams().get("value"));  
			} catch(NumberFormatException nfe) {  
				try {
					//Einzelne Teile, die Punkten getrennt sind aufsplitten und den Wert des Arributs in Double parsen

					String[] paramsSplitted = ((String)cond.getParams().get("value")).split("\\.");

					if(paramsSplitted[paramsSplitted.length-1].equals("length")){
						//Wenn die Länge abgefragt werden soll
						//Entferne "length" aus den Parametern
						List<String> temp = Arrays.asList(paramsSplitted);
						temp.remove(paramsSplitted.length-1);
						//Navigiere zum WhiteboardEintrag, caste ihn als Whiteboard (von der wir die Länge haben wollen)
						//Dann holen wir mit entrySet() alle Einträge und mit size() dann schließlich die Länge
						value = ((Whiteboard)wb.getAttribute((String[]) temp.toArray()).value).entrySet().size();
					}else{
						value = Double.parseDouble((String) wb.getAttribute(paramsSplitted).value);
					}
				} catch (NumberFormatException nfe2) {
					// something went wrong, Value is not a Number
				}
			} 
			try {  
			    compValue = Double.parseDouble((String) cond.getParams().get("compValue"));  
			} catch(NumberFormatException nfe) {  
				try {
					//Einzelne Teile, die Punkten getrennt sind aufsplitten und den Wert des Arributs in Double parsen

					String[] paramsSplitted = ((String) cond.getParams().get("value")).split("\\.");

					if(paramsSplitted[paramsSplitted.length-1].equals("length")){
						//Wenn die Länge abgefragt werden soll
						//Entferne "length" aus den Parametern
						List<String> temp = Arrays.asList(paramsSplitted);
						temp.remove(paramsSplitted.length-1);
						//Navigiere zum WhiteboardEintrag, caste ihn als Whiteboard (von der wir die Länge haben wollen)
						//Dann holen wir mit entrySet() alle Einträge und mit size() dann schließlich die Länge
						value = ((Whiteboard)wb.getAttribute((String[]) temp.toArray()).value).entrySet().size();
					}else{
						value = Double.parseDouble((String) wb.getAttribute(paramsSplitted).value);
					}
				} catch (NumberFormatException nfe2) {
					// something went wrong, Value is not a Number
				} 
			} 
			
				
			
				// Doch nicht, ist nur bei Location Events wenn ich mich nicht irre
			// get checkType
			if (cond.getParams().get("checkType").equals(MdsCondition.EQUALS)) {
				if (value == compValue) return new Result(true, null, null);
			} else if (cond.getParams().get("checkType").equals(MdsCondition.LOWER)) {
				if (value < compValue) return new Result(true, null, null);
			} else if (cond.getParams().get("checkType").equals(MdsCondition.HIGHER)) {
				if (value > compValue) return new Result(true, null, null);
			} else if (cond.getParams().get("checkType").equals(MdsCondition.LOWEQUALS)) {
				if (value <= compValue) return new Result(true, null, null);
			} else if (cond.getParams().get("checkType").equals(MdsCondition.HIGHEQUALS)) {
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
		//Wenn ein uiEvent geprüft werden soll, wird es stumpf verglichen
		return new Result((cond.getName().equals(btnName)), null, null);
	}
	
	/**
	 * Prüft wieviele Einträge eines Whiteboards, die die Attribute Longitude und Latitude haben, innerhalb eines vordefinierten Radius von einer Position vorhanden sind
	 * @param wb Whiteboard, dessen Einträge geprüft werden soll
	 * @param longitude
	 * @param latitude
	 * @param radius
	 * @return Liste mit Items innerhalb des Radius
	 */
	private static List<WhiteboardEntry> getEntriesNearTo(Whiteboard wb, Location playerLoc, int radius ) {
		
		List<WhiteboardEntry> result = new Vector<WhiteboardEntry>();
		
		// Alle Einträge durchgehen
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
