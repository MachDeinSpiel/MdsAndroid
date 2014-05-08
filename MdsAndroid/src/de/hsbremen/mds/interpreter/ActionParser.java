package de.hsbremen.mds.interpreter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import de.hsbremen.mds.common.valueobjects.statemachine.MdsEvent;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsActionExecutable;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsImageAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsMapAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsTextAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsVideoAction;
import de.hsbremen.mds.common.whiteboard.Whiteboard;

public class ActionParser {

	/**
	 * Macht aus einer MdsAction eine ausführbare MdsActionExecutable, die man mit .execute() dann ausführen kann.
	 * @param action Action, die geparst werden soll
	 * @param triggerEvent	Das Event, dass diese Action auslöste
	 * @param wb	Whiteboard
	 * @param myId	Id des Spielers, der diese Action ausführt
	 * @return	Ausführbares MdsExecutableAction Objekt
	 */
	public MdsActionExecutable parseAction(MdsAction action,MdsEvent triggerEvent, Whiteboard wb, int myId){
		
		//Parameter der Action
		HashMap<String, String> params = action.getParams();
		
		//Jeden Parameter parsen/interpretieren
		for(String key : params.keySet()){
			params.put(key, parseParam(params.remove(key), triggerEvent, wb, myId));
		}
		
		//Je nach dem, von welchem Ident die Action ist, werden verschiedene MdsActionExecutables zurückgegeben 
		switch(action.getIdent()){
		case showVideo:
			return new MdsVideoAction(params.get("title"), params.get("url"), params.get("text"));
		case showMap:
			return new MdsMapAction("Map", Double.parseDouble(params.get("longitude")), Double.parseDouble(params.get("latitude")));
		case showImage:
			return new MdsImageAction(params.get("title"), params.get("url"), params.get("text"));
		case showText:
			return new MdsTextAction(params.get("title"), params.get("text"));
		case addToGroup:
			//TODO;
		default:
			return null;
		}
		
		
		
		
		
	}
	
	private String parseParam(String param, MdsEvent event, Whiteboard wb, int playerId){
		
		
		//Ersetzungen gemäß der Spezisprache vorbereiten 
		HashMap<String, String> replacements = new HashMap<String, String>();
		replacements.put("self","players."+playerId);
			
		for(String toReplace : replacements.keySet()){
			param.replace(toReplace, replacements.get(toReplace));
		}
		
		//Einzelne Teile, die Punkten getrennt sind aufsplitten
		List<String> splitted = Arrays.asList(param.split("."));
		
		//Keine weiteren Teile (param enthielt keinen Punkt)? fertig!
		if(splitted.size() == 1){
			return splitted.get(0);
		}
		
		//Wenn das Schlüsselwort "Trigger" vorkommt, werden dessen Attribute genutzt
		if(splitted.get(0).equals("trigger")){
			splitted.remove(0);
			String[] keys = (String[]) splitted.toArray();
			//TODO: Trigger mit Object ersetzen
			//return (String) event.getTrigger().getAttributes().getAttribute(keys).value;
			return null;
		}
		
		//Ansonsten Daten aus dem Whiteboard holen
		return (String) wb.getAttribute((String[]) splitted.toArray()).value;
		
				
	}
	
}
