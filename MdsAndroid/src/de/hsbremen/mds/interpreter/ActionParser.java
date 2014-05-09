package de.hsbremen.mds.interpreter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsActionExecutable;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsImageAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsMapAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsTextAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsVideoAction;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;

public class ActionParser {

	/**
	 * Macht aus einer MdsAction eine ausführbare MdsActionExecutable, die man mit .execute() dann ausführen kann.
	 * @param action Action, die geparst werden soll
	 * @param triggerEvent	Das Event, dass diese Action auslöste
	 * @param wb	Whiteboard
	 * @param myId	Id des Spielers, der diese Action ausführt
	 * @return	Ausführbares MdsExecutableAction Objekt
	 */
	public MdsActionExecutable parseAction(MdsState state, Whiteboard wb, int myId){
		
		// Action des States holen
		MdsAction action = state.getDoAction();
		//Parameter der Action
		HashMap<String, String> params = action.getParams();
		
		//Jeden Parameter parsen/interpretieren
		for(String key : params.keySet()){
			params.put(key, parseParam(params.remove(key), state, wb, myId));
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
	
	private String parseParam(String param, MdsState state, Whiteboard wb, int playerId){
		
		
		//Ersetzungen gemäß der Spezisprache vorbereiten 
		HashMap<String, String> replacements = new HashMap<String, String>();
		replacements.put("self","players."+playerId);
			
		for(String toReplace : replacements.keySet()){
			param.replace(toReplace, replacements.get(toReplace));
		}
		
		//Einzelne Teile, die Punkten getrennt sind aufsplitten
		List<String> splitted = Arrays.asList(param.split("."));
		
		//Wenn das Schlüsselwort "Objekt" oder "Subject" vorkommt, werden dessen Attribute genutzt
		if(splitted.get(0).equals("object")){
			splitted.remove(0);
			String[] keys = (String[]) splitted.toArray();
			// TODO: erstmal nur mit einem
			List<WhiteboardEntry> objects = state.getObjects();
			return (String) ((Whiteboard)objects.get(0).value).getAttribute(keys).value;
		} else if (splitted.get(0).equals("subject")) {
			splitted.remove(0);
			String[] keys = (String[]) splitted.toArray();
			// erstmal nur mit einem
			List<WhiteboardEntry> subjects = state.getSubjects();
			return (String) ((Whiteboard)subjects.get(0).value).getAttribute(keys).value;
		}
		
		//Ansonsten Daten aus dem Whiteboard holen
		return (String) wb.getAttribute((String[]) splitted.toArray()).value;
		
				
	}
	
}
