package de.hsbremen.mds.interpreter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.location.Location;
import android.util.Log;
import de.hsbremen.mds.common.valueobjects.MdsObject;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsCondition;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsQuantifier;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;

public class EventParser {
	
	public static Result checkWhiteboardEvent(MdsCondition condition,Whiteboard wb, List<String> playerGroup, String playerId) {
		Result result = new Result(false, null, null);

		result = checkConditionEvent(condition, wb, playerGroup, playerId);
		return result;
	}

	public static Result checkLocationEvent(MdsCondition cond, Whiteboard wb, List<String> playerGroup, String playerId) {
		
		// ------------------ Werte holen ---------------//
		// get Params
		HashMap<String, Object> params = cond.getParams();
		// get Object
		Log.i("Mistake", cond.getName());
		Log.i("Mistake", params.get("object").toString());
		MdsObject object = (MdsObject) params.get("object");
		// get Subject
		MdsObject subject = (MdsObject) params.get("subject");
		Log.i("Mistake", "Received object and subject of Event");
		// get Object Quantifier
		MdsQuantifier objQuanti = object.getQuantifier();
		// get Subject Quantifier
		MdsQuantifier subQuanti = subject.getQuantifier();
		Log.i("Mistake", "Received quantifiers");
		// get Radius
		int radius;
		try{
			radius = (Integer)params.get("radius");
		}catch(ClassCastException cce){
			try{
				radius = Integer.parseInt(params.get("radius").toString());
			}catch(Exception e){
				//TODO: bessere alternative
				radius = 20;
			}
		}
		// get Quantivalue
		int quanti = Integer.parseInt(objQuanti.getValue());
		// get QuanticheckType
		String checkType = objQuanti.getChecktype();
		
		Log.i("Mistake", "Checking different Conditions");
		// -------------- alle Verschiedenen Location Events -----------//
		if(cond.getName().equals("nearby")) {
			Log.i("Mistake", "Condition is nearby");
			// wenn das Subject "self" ist, im Einzelspieler immer
			if(subject.getName().equals("self")){
				Log.i("Mistake", "Subject is self");
				// Locationobjekt des Spielers erzeuegen
				if (playerGroup.size() > 1) Log.i("Mistake", "PlayerWb von Spieler " + playerId + "ist:" + wb.getAttribute(playerGroup.get(0), playerGroup.get(1), playerId).value.toString());
				else Log.i("Mistake", "PlayerWb von Spieler " + playerId + "ist:" + wb.getAttribute(playerGroup.get(0), playerId).value.toString());
				try {
					double longitude;
					double latitude;
					if (playerGroup.size() > 1) {
						longitude = Double.parseDouble((String) wb.getAttribute(playerGroup.get(0), playerGroup.get(1), playerId, "longitude").value);
						latitude = Double.parseDouble((String) wb.getAttribute(playerGroup.get(0), playerGroup.get(1), playerId, "latitude").value);
					} else {
						longitude = Double.parseDouble((String) wb.getAttribute(playerGroup.get(0), playerId, "longitude").value);
						latitude = Double.parseDouble((String) wb.getAttribute(playerGroup.get(0), playerId, "latitude").value);
					}
					Location playerLoc = new Location("PlayerLoc");
					playerLoc.setLatitude(latitude);
					playerLoc.setLongitude(longitude);
					Log.i("Mistake", "Received player Loc");

					//Unterwhiteboard (z.B. die Gruppe "exhbitis") wird anhand des parameter "target" ermittelt
					//Dafür wird der String dafür bei jedem Punkt geteilt, in einen Array gepackt und davon die value als Whiteboard gecastet

					Log.i("Mistake", "Getting the real Object from WB");
					Whiteboard realObject = (Whiteboard)wb.getAttribute(object.getName().split("\\.")).value;

					Log.i("Mistake", "Getting all Objects resulting from Nearby");
					List<WhiteboardEntry> objects = getEntriesNearTo(realObject, playerLoc, radius);
					
					Result result = null;
					// CheckType des Quantifiers identifizieren
					// TODO: vlt gar kein Result mehr gebraucht? Einfach boolean
					if (checkType.equals(MdsCondition.EQUALS)) {
						if (objects.size() == quanti) result = new Result(true, null, null);
					} else if (checkType.equals(MdsQuantifier.LOWER)) {
						if (objects.size() < quanti) result = new Result(true, null, null);
					} else if (checkType.equals(MdsQuantifier.HIGHER)) {
						if (objects.size() > quanti) result = new Result(true, null, null);
					} else if (checkType.equals(MdsQuantifier.LOWEQUALS)) {
						if (objects.size() <= quanti) result = new Result(true, null, null);
					} else if (checkType.equals(MdsQuantifier.HIGHEQUALS)) {
						if (objects.size() >= quanti) result = new Result(true, null, null);
					} else if (checkType.equals(MdsQuantifier.EXISTS)) {
						if (objects.size() >= 1) result = new Result(true, null, null);
					} else if (checkType.equals(MdsQuantifier.ALL)) {
						if (objects.size() == realObject.entrySet().size()) result = new Result(true, null, null);
					} 
					if(result != null){
						MdsState currentState; 
						if(playerGroup.size() > 1)
							currentState = (MdsState)wb.getAttribute(playerGroup.get(0), playerGroup.get(1), ""+playerId,FsmManager.CURRENT_STATE).value;
						else
							currentState = (MdsState)wb.getAttribute(playerGroup.get(0), ""+playerId,FsmManager.CURRENT_STATE).value;
						// glaube ich nicht mehr genutzt
						currentState.setObjects(objects);
						Log.i("Mistake", "Object Size ist " + objects.size());
						// WB im State und beim Player speichern
						// Erstmal nur ein Object
						WhiteboardEntry target;
						if(playerGroup.size() > 1) {
							wb.setAttribute(objects.get(0), playerGroup.get(0), playerGroup.get(1), ""+playerId, "object");
							target = wb.getAttribute(playerGroup.get(0), playerGroup.get(1),""+playerId, "object");
						} else {
							wb.setAttribute(objects.get(0), playerGroup.get(0), ""+playerId, "object");
							target = wb.getAttribute(playerGroup.get(0), ""+playerId, "object");
						}
						Log.i("Mistake", "Adding Object to" + playerGroup + playerId + " object" + target.value);
						return result;
					} else {
						Log.e(Interpreter.LOGTAG, "Error in Eventparser, CheckLocationEvent, result is null");
					}
				} catch (NumberFormatException e1) {
					Log.e(Interpreter.LOGTAG, "Position Attribute could not be parsed to double");
					return new Result(false, null, null);
				}
				
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

				List<WhiteboardEntry> objects = getEntriesNearTo(realObject, someLoc, radius);
				
				MdsState currentState;
				if (playerGroup.size() > 1)
					currentState = (MdsState)wb.getAttribute(playerGroup.get(0), playerGroup.get(1), ""+playerId,FsmManager.CURRENT_STATE).value;
				else 
					currentState = (MdsState)wb.getAttribute(playerGroup.get(0), ""+playerId,FsmManager.CURRENT_STATE).value;
				currentState.setObjects(objects);
			} 
		}else if(cond.getName().equals("!nearby")) {
			// wenn das Subject "self" ist, im Einzelspieler immer
			if(subject.getName().equals("self")){
				// Locationobjekt des Spielers erzeuegen
				double longitude;
				double latitude;
				if (playerGroup.size() > 1) {
					longitude = Double.parseDouble((String) wb.getAttribute(playerGroup.get(0), playerGroup.get(1), playerId, "longitude").value);
					latitude = Double.parseDouble((String) wb.getAttribute(playerGroup.get(0), playerGroup.get(1), playerId, "latitude").value);
				} else {
					longitude = Double.parseDouble((String) wb.getAttribute(playerGroup.get(0), playerId, "longitude").value);
					latitude = Double.parseDouble((String) wb.getAttribute(playerGroup.get(0), playerId, "latitude").value);
				}
				Location playerLoc = new Location("PlayerLoc");
				playerLoc.setLatitude(latitude);
				playerLoc.setLongitude(longitude);

				//Unterwhiteboard (z.B. die Gruppe "exhbitis") wird anhand des parameter "target" ermittelt
				//Dafür wird der String dafür bei jedem Punkt geteilt, in einen Array gepackt und davon die value als Whiteboard gecastet

				Whiteboard realObject = (Whiteboard)wb.getAttribute(object.getName().split("\\.")).value;

				List<WhiteboardEntry> objects = getEntriesNearTo(realObject, playerLoc, radius);
				
				Result result = null;
				// CheckType des Quantifiers identifizieren
				if (checkType.equals(MdsCondition.EQUALS)) {
					if (objects.size() != quanti) result = new Result(true, null, null);
				} else if (checkType.equals(MdsQuantifier.LOWER)) {
					if (objects.size() >= quanti) result = new Result(true, null, null);
				} else if (checkType.equals(MdsQuantifier.HIGHER)) {
					if (objects.size() <= quanti) result = new Result(true, null, null);
				} else if (checkType.equals(MdsQuantifier.LOWEQUALS)) {
					if (objects.size() > quanti) result = new Result(true, null, null);
				} else if (checkType.equals(MdsQuantifier.HIGHEQUALS)) {
					if (objects.size() < quanti) result = new Result(true, null, null);
				} else if (checkType.equals(MdsQuantifier.EXISTS)) {
					if (objects.size() < 1) result = new Result(true, null, null);
				} else if (checkType.equals(MdsQuantifier.ALL)) {
					if (objects.size() < realObject.entrySet().size()) result = new Result(true, null, null);
				} 
				if(result != null){
					MdsState currentState; 
					if(playerGroup.size() > 1)
						currentState = (MdsState)wb.getAttribute(playerGroup.get(0), playerGroup.get(1), ""+playerId,FsmManager.CURRENT_STATE).value;
					else
						currentState = (MdsState)wb.getAttribute(playerGroup.get(0), ""+playerId,FsmManager.CURRENT_STATE).value;
					currentState.setObjects(objects);
					Log.i("Mistake", "Object Size ist " + objects.size());
					// WB im State und beim Player speichern
					// Erstmal nur ein Object
					WhiteboardEntry target;
					if(playerGroup.size() > 1) {
						wb.setAttribute(objects.get(0), playerGroup.get(0), playerGroup.get(1), ""+playerId, "object");
						target = wb.getAttribute(playerGroup.get(0), playerGroup.get(1),""+playerId, "object");
					} else {
						wb.setAttribute(objects.get(0), playerGroup.get(0), ""+playerId, "object");
						target = wb.getAttribute(playerGroup.get(0), ""+playerId, "object");
					}
					Log.i("Mistake", "Adding Object to" + playerGroup + playerId + " object" + target.value);
					return result;
				} else {
					Log.e(Interpreter.LOGTAG, "Error in Eventparser, CheckLocationEvent, result is null");
				}
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
				// Hier evtl eine Liste von Locations?
				List<WhiteboardEntry> objects = getEntriesNearTo(realObject, someLoc, radius);
				
				MdsState currentState;
				if (playerGroup.size() > 1)
					currentState = (MdsState)wb.getAttribute(playerGroup.get(0), playerGroup.get(1), ""+playerId,FsmManager.CURRENT_STATE).value;
				else 
					currentState = (MdsState)wb.getAttribute(playerGroup.get(0), ""+playerId,FsmManager.CURRENT_STATE).value;
				currentState.setObjects(objects);
			}
		}
		return new Result(false, null, null);
	}
	
	public static Result checkConditionEvent(MdsCondition cond, Whiteboard wb, List<String> playerGroup, String playerId) {
		Log.i(Interpreter.LOGTAG, "CheckCondition: " + cond.getName());
		
		// get value and compValue
		double value = -1;
		String sValue = null;
		double compValue = -2;
		String sCompValue = null;
		
		try {  
		    value = (Double)cond.getParams().get("value");  
		} catch(ClassCastException cce){
			try{
				value = Double.parseDouble(cond.getParams().get("value").toString());
			}catch(NumberFormatException nfe) {  
				try {
					//Einzelne Teile, die Punkten getrennt sind aufsplitten und den Wert des Arributs in Double parsen
					Log.i("Mistake", "ConditionName ist: " + cond.getName());
					Log.i(Interpreter.LOGTAG,"checkCondition: value ist kein Double, versuche zu splitten. params: "+(String)cond.getParams().get("value"));
					String paramString = (String)cond.getParams().get("value");
					if (playerGroup.size() > 1)
						paramString = paramString.replaceAll("self", playerGroup.get(0) + "." + playerGroup.get(1) + "."+playerId);
					else 
						paramString = paramString.replaceAll("self", playerGroup.get(0) + "."+playerId);
					String[] paramsSplitted = (paramString).split("\\.");

					if(paramsSplitted[paramsSplitted.length-1].equals("length")){
						//Wenn die Länge abgefragt werden soll
						//Entferne "length" aus den Parametern
						List<String> temp = new Vector<String>(Arrays.asList(paramsSplitted));
						temp.remove(paramsSplitted.length-1);
						//Navigiere zum WhiteboardEintrag, caste ihn als Whiteboard (von der wir die Länge haben wollen)
						//Dann holen wir mit entrySet() alle Einträge und mit size() dann schließlich die Länge
						value = ((Whiteboard)wb.getAttribute((String[]) temp.toArray(new String[0])).value).entrySet().size();
					}else{
						MdsState current;
						if (playerGroup.size() > 1)
							current = (MdsState)wb.getAttribute(playerGroup.get(0), playerGroup.get(1), ""+playerId, FsmManager.CURRENT_STATE).value;
						else 
							current = (MdsState)wb.getAttribute(playerGroup.get(0), ""+playerId, FsmManager.CURRENT_STATE).value;
						sValue = (String) parseParam(cond.getParams().get("value").toString(), current, wb, playerGroup, playerId);
						Log.i("Mistake", "SValue ist: " + sValue);
						Log.i("Mistake", "WB ist: " + wb);
						value = Double.parseDouble(sValue);
						Log.i("Mistake", "Value ist: " + value);
					}
				} catch (NumberFormatException nfe2) {
					// something went wrong, Value is not a Number
				}
			}
		} 
	
		if (cond.getName().equals("condition") || cond.getName().equals("compare")) {
			try {  
			    compValue = (Double)cond.getParams().get("compValue");  
			} catch(ClassCastException cce){
				try{
					compValue = Double.parseDouble(cond.getParams().get("compValue").toString());
				}catch(NumberFormatException nfe) {  
					try {
						//Einzelne Teile, die Punkten getrennt sind aufsplitten und den Wert des Arributs in Double parsen

						String[] paramsSplitted = ((String) cond.getParams().get("value")).split("\\.");

						if(paramsSplitted[paramsSplitted.length-1].equals("length")){
							//Wenn die Länge abgefragt werden soll
							//Entferne "length" aus den Parametern
							List<String> temp = new Vector<String>(Arrays.asList(paramsSplitted));
							temp.remove(paramsSplitted.length-1);
							//Navigiere zum WhiteboardEintrag, caste ihn als Whiteboard (von der wir die Länge haben wollen)
							//Dann holen wir mit entrySet() alle Einträge und mit size() dann schließlich die Länge
							compValue = ((Whiteboard)wb.getAttribute((String[]) temp.toArray(new String[0])).value).entrySet().size();
						}else{
							MdsState current;
							if (playerGroup.size() > 1)
								current = (MdsState)wb.getAttribute(playerGroup.get(0), playerGroup.get(1), ""+playerId, FsmManager.CURRENT_STATE).value;
							else 
								current = (MdsState)wb.getAttribute(playerGroup.get(0), ""+playerId, FsmManager.CURRENT_STATE).value;
							sCompValue = (String) parseParam(cond.getParams().get("compValue").toString(), current, wb, playerGroup, playerId);
							Log.i("Mistake", "SCompValue ist: " + sCompValue);
							Log.i("Mistake", "WB ist: " + wb);
							compValue = Double.parseDouble(sCompValue);
							Log.i("Mistake", "Value ist: " + value);
						}
					} catch (NumberFormatException nfe2) {
						// something went wrong, Value is not a Number
					} 
				} 
			}
			
			// get checkType
			Log.i("Mistake", cond.getParams().toString());
			if(sValue != null || sCompValue != null) {
				if (sValue.equals(sCompValue)) return new Result(true, null, null);
			} else if (cond.getParams().get("checkType").equals(MdsCondition.EQUALS)) {
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
	public static Result checkUiEvent(String btnName, MdsCondition cond, Whiteboard wb) {
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
	
	// TODO: Hier muss es irgendwie möglich sein, dass ownGroup.enemy vernünftig geparst werden können, sodass das gewollte Team zurück gegeben wird
	/**
	 * Parst einen Action String so, dass er für den weiteren Prozess verwendbar ist
	 * @param param - param to be parsed
	 * @param state - current State the machine is in
	 * @param wb - the whiteboard the elements are in
	 * @param playerGroup - Keys to the group of the player
	 * @param playerId - ID of the player
	 * @return - Whiteboard, WhiteboardEntry or parsed string
	 */
	public static Object parseParam(String param, MdsState state, Whiteboard wb, List<String> playerGroup, String playerId){
		
		Log.i(Interpreter.LOGTAG,"parseParam:"+param);
		//Ersetzungen gemäß der Spezisprache vorbereiten 
		HashMap<String, String> replacements = new HashMap<String, String>();
		if (playerGroup.size() > 1)
			replacements.put("self", playerGroup.get(0) + "." + playerGroup.get(1) +"."+playerId);
		else 
			replacements.put("self", playerGroup.get(0) +"."+playerId);
		// TODO: Own Group auflösen
		if (playerGroup.size() > 1)
			replacements.put("ownGroup", playerGroup.get(0) + "." + playerGroup.get(1) + "." + playerId+"");
		else
			replacements.put("ownGroup", playerGroup.get(0) + "." + playerId+"");
		
		
		
		for(String toReplace : replacements.keySet()){
			//Log.i(Interpreter.LOGTAG,"parseParam replace every occurence of ["+toReplace+"]");
			param = param.replace(toReplace, replacements.get(toReplace));
		}
		Log.i(Interpreter.LOGTAG,"parseParam after replacements:"+param);
		
		//Einzelne Teile, die Punkten getrennt sind aufsplitten
		List<String> splitted = new Vector<String>(Arrays.asList(param.split("\\.")));
		
		Log.i(Interpreter.LOGTAG,"parseParam splittedParamLength:"+splitted.size());
		
		Object tmpObj = wb;
		// Wenn das Schlüsselwort "self vorkommt"
		if(splitted.size() > 2 && splitted.get(0).equals("Players") &&  splitted.get(1).equals(""+playerId) && splitted.get(2).equals("object")) {
			Log.i(Interpreter.LOGTAG, "Parse Object of Self");
			// auf Whiteboard des Spielers setzen und "Players" + ID löschen
			splitted.remove(0);
			splitted.remove(0);
			if (playerGroup.size() > 1)
				tmpObj = (Whiteboard) ((Whiteboard)tmpObj).getAttribute(playerGroup.get(0), playerGroup.get(1), ""+playerId).value;
			else
				tmpObj = (Whiteboard) ((Whiteboard)tmpObj).getAttribute(playerGroup.get(0), ""+playerId).value; 
		}
		// wenn object in current state
		if(splitted.size() > 0 && splitted.get(0).equals(FsmManager.CURRENT_STATE)) {
			Log.i(Interpreter.LOGTAG, "Parse Current");
			splitted.remove(0);
			if(playerGroup.size() > 1)
				tmpObj = (MdsState) ((Whiteboard)tmpObj).getAttribute(playerGroup.get(0), playerGroup.get(1), ""+playerId, FsmManager.CURRENT_STATE).value;
			else
				tmpObj = (MdsState) ((Whiteboard)tmpObj).getAttribute(playerGroup.get(0), ""+playerId, FsmManager.CURRENT_STATE).value;
		// wenn object in last state	
		} else if (splitted.size() > 0 &&  splitted.get(0).equals(FsmManager.LAST_STATE)) {
			Log.i(Interpreter.LOGTAG, "Parse Last");
			splitted.remove(0);
			if(playerGroup.size() > 1)
				tmpObj = (MdsState) ((Whiteboard)tmpObj).getAttribute(playerGroup.get(0), playerGroup.get(1), ""+playerId, FsmManager.LAST_STATE).value;
			else
				tmpObj = (MdsState) ((Whiteboard)tmpObj).getAttribute(playerGroup.get(0), ""+playerId, FsmManager.LAST_STATE).value;
		}
		//Wenn das Schlüsselwort "Objekt" oder "Subject" vorkommt, werden dessen Attribute genutzt
		if(splitted.size() > 0 && splitted.get(0).equals("object")){
			
			splitted.remove(0);
			String[] keys = (String[]) splitted.toArray(new String[0]);

			Log.i("Mistake", "Object found...");
			// erstmal nur mit einem
			Whiteboard objects = new Whiteboard();
			// Wenn das tmpObj ein Whiteboard ist einfach auf Objects setzen
			if (tmpObj instanceof Whiteboard) {
				Log.i(Interpreter.LOGTAG, "Type: Whiteboard");
				WhiteboardEntry wbe = ((Whiteboard)tmpObj).getAttribute("object");
				objects.put(""+0, wbe);
				if (objects == null) Log.i(Interpreter.LOGTAG, "Objects ist null");
				Log.i(Interpreter.LOGTAG, "Size der Objects: " + objects.size());
			// sonst die Objects des States einfügen
			} else if (tmpObj instanceof MdsState) {
				Log.i(Interpreter.LOGTAG, "Type: State");
				List<WhiteboardEntry> objectList = ((MdsState)tmpObj).getObjects();
				for(int i = 0; i < objectList.size(); i++) {
					objects.put(""+i, objectList.get(0));
				}
			}
			
			if(objects.keySet().isEmpty()){
				// probably empty code
				Log.e(Interpreter.LOGTAG,"Error: no objects(from trigger) in whiteboard in state "+state.getName()+ " trying currentState...");
				Whiteboard currentState = null;
				try{
					if(playerGroup.size() > 1)
						currentState = (Whiteboard)wb.getAttribute(playerGroup.get(0), playerGroup.get(1), ""+playerId,FsmManager.CURRENT_STATE).value;
					else
						currentState = (Whiteboard)wb.getAttribute(playerGroup.get(0), ""+playerId,FsmManager.CURRENT_STATE).value;
					Object o = ((Whiteboard) ((Whiteboard)currentState.get("objects").value).get(0).value).getAttribute(keys);
				}catch(Exception e){
					Log.e(Interpreter.LOGTAG,"Error while getting objects from whiteboard in state ");
				}
				if(objects.keySet().isEmpty()){
					Log.e(Interpreter.LOGTAG,"Still no luck while getting objects from whiteboard, still null");
				}
			}
			Log.i(Interpreter.LOGTAG, "parseParam: objectsSize: "+objects.size());
			Log.i(Interpreter.LOGTAG, "parseParam: object[0] "+objects.get(""+0).toString());
			Log.i(Interpreter.LOGTAG, "parseParam: object[0] "+objects.get(""+0).value.toString());
			if(keys.length >0 ){
				return (String) ((Whiteboard)objects.get(""+0).value).getAttribute(keys).value;
			}else{
				return (WhiteboardEntry)objects.get(""+0);
			}
		} else if (splitted.size() > 0 && splitted.get(0).equals("subject")) {
			splitted.remove(0);
			String[] keys = (String[]) splitted.toArray(new String[0]);
			// erstmal nur mit einem
			List<WhiteboardEntry> subjects = state.getSubjects();
			return (String) ((Whiteboard)subjects.get(0).value).getAttribute(keys).value;

		} else if (splitted.size() > 0 && splitted.get(0).equals("notValue")) {
			splitted.remove(0);
			// String wieder zusammen setzen
			StringBuffer buffer = new StringBuffer();
			for(String s : splitted) {                                                  
				buffer.append(s + ".");
			}
			String result = buffer.toString();
			return buffer.toString();
		}
		
		//Ansonsten Daten aus dem Whiteboard holen
		try{
			Object o = wb.getAttribute((String[]) splitted.toArray(new String[0])).value;
			return  wb.getAttribute((String[]) splitted.toArray(new String[0])).value;
		}catch(NullPointerException e){
			Log.d(Interpreter.LOGTAG,"Could not parse param ["+param+"], returning itself");
			return param;
		}
		
				
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
	
}
