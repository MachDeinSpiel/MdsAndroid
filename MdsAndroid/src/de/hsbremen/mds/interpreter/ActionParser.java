package de.hsbremen.mds.interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import android.util.Log;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.common.interfaces.GuiInterface;
import de.hsbremen.mds.common.interfaces.ServerInterpreterInterface;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsInfoObject;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsTransition;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsAction.MdsActionIdent;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsActionExecutable;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsImageAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsMapAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsTextAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsVideoAction;
import de.hsbremen.mds.common.whiteboard.InvalidWhiteboardEntryException;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;

public class ActionParser {
	
	
	public static final String ADD = "add";
	public static final String MULTIPLY = "multiply";

	/**
	 * Macht aus einer MdsAction eine ausführbare MdsActionExecutable, die man mit .execute() dann ausführen kann.
	 * @param action Action, die geparst werden soll
	 * @param state State in dem die Action ausgeführt wird
	 * @param triggerEvent	Das Event, dass diese Action auslöste
	 * @param wb	Whiteboard
	 * @param myId	Id des Spielers, der diese Action ausführt
	 * @return	Ausführbares MdsExecutableAction Objekt
	 */
	public MdsActionExecutable parseAction(String type, MdsAction action, final MdsState state, final Whiteboard wb, final String myId, final ServerInterpreterInterface sii){
		
		if(action == null){
			//Action exisitert eigentlich gar nicht? -> GTFO!
			//TODO: evtl schon im Interpreter prüfen, ob die Action null ist (also keine in der JSON-Datei spezifieziert ist)
			return null;
		}
	
		//Parameter der Action
		final HashMap<String, String> params = action.getParams();
		final HashMap<String, Object> parsedParams = new HashMap<String, Object>();
		
		// Buttons heraussuchen, falls state transitions hat
		MdsState buttonState = (MdsState) wb.getAttribute("Players",myId+"","currentState").value;
		MdsTransition[] trans = buttonState.getTransitions();
		List<String> buttons = new Vector<String>();
		if (trans != null) {
			if(type.equals("start") || type.equals("do")) {		
				// Alle Transitions durchgehen
				for(int i = 0; i < trans.length; i++) {
					if (trans[i].getEventType() == MdsTransition.EventType.uiEvent) {
						buttons.add(trans[i].getCondition().getName());
					}
				}
			}
		}
		
		//Jeden Parameter parsen/interpretieren
		for(String key : params.keySet()){
			parsedParams.put(key, parseParam(params.get(key), state, wb, myId));
		}
		
		/*	showVideo,
			showMap,
			showText,
			showImage,
			addToGroup,
			removeFromGroup,
			changeAttribute,
			useItem,
			updateMap
		 */
		
		//Je nach dem, von welchem Ident die Action ist, werden verschiedene MdsActionExecutables zurückgegeben 
		switch(action.getIdent()){
		case showVideo:
			return new MdsVideoAction((String)parsedParams.get("title"), (String)parsedParams.get("url"), (String)parsedParams.get("text"), buttons);
		case showMap:
		case updateMap:
			return new MdsActionExecutable() {
				
				@Override
				public void execute(GuiInterface guiInterface) {
					//Map anzeigen
					double lat,lon;
					try{
						lat  = Double.parseDouble((String)wb.getAttribute(Interpreter.WB_PLAYERS,""+myId, "latitude").value);
						lon  = Double.parseDouble((String)wb.getAttribute(Interpreter.WB_PLAYERS,""+myId, "longitude").value);
					}catch(Exception e){
						lat = 0;
						lon = 0;
					}
					MdsMapAction mma = new MdsMapAction("showMap", lat, lon);
					
					ArrayList<MdsItem> mapEntities = new ArrayList<MdsItem>();
					
					for(String key : ((Whiteboard)wb.getAttribute("Bombs").value).keySet()){
						//Whiteboard bomb = (Whiteboard)wb.getAttribute("Bombs",key).value;
						//mapEntities.add(new MdsItem((String)bomb.getAttribute("name").value, ""));
						Log.d(Interpreter.LOGTAG, "["+key+"] in die Liste eingefügt.");
						MdsItem item = new MdsItem(key, "");
						item.setLongitude(Double.parseDouble((String)wb.getAttribute("Bombs",key,"longitude").value));
						item.setLatitude(Double.parseDouble((String)wb.getAttribute("Bombs",key,"latitude").value));
						mapEntities.add(item);
						
					}
					for(String key : ((Whiteboard)wb.getAttribute("Medipacks").value).keySet()){
						//Whiteboard bomb = (Whiteboard)wb.getAttribute("Medipacks",key).value;
						//mapEntities.add(new MdsItem((String)bomb.getAttribute("name").value, ""));
						MdsItem item = new MdsItem(key, "");
						item.setLongitude(Double.parseDouble((String)wb.getAttribute("Medipacks",key,"longitude").value));
						item.setLatitude(Double.parseDouble((String)wb.getAttribute("Medipacks",key,"latitude").value));
						mapEntities.add(item);
					}
					//Map anzeigen
					mma.execute(guiInterface);
					//Und Bomben und Medipacks anzeigen
					//TODO: Wenn die Visibillity klar definiert wird, entsprechende Items anzeigen
					guiInterface.showMap(mapEntities);
				}
			}; 
		case showImage:
			return new MdsImageAction((String)parsedParams.get("title"),(String)parsedParams.get("url"), (String)parsedParams.get("text"), buttons);
		case showText:
			return new MdsTextAction("showText", (String)parsedParams.get("text"), buttons);
		case addToGroup:
			return new MdsActionExecutable() {
				
				@Override
				public void execute(GuiInterface guiInterface) {
					
					
				}
			};
		case removeFromGroup:
			return new MdsActionExecutable() {
				
				@Override
				public void execute(GuiInterface guiInterface) {
					
					//List<String> keysToValue = new Vector<String>(Arrays.asList(((String)parsedParams.get("group")).split("\\.")));
					Whiteboard currentWb = (Whiteboard)parsedParams.get("group");//parseActionString(wb, keysToValue, state, myId);
					String[] keys = params.get("target").split("\\.");
					WhiteboardEntry result = currentWb.remove(keys[keys.length-1]);
					
					Log.i(Interpreter.LOGTAG, "removeFromGroup: ["+params.get("target")+ "] (["+keys[keys.length-1]+"]) from group [" + params.get("group").toString()+"], is:["+result+"]");
					//TODO: server bescheid geben
					List<String> keysToValue = new Vector<String>();
					for(String s : params.get("target").split("\\."))
						keysToValue.add(s);
					try {
						sii.onWhiteboardUpdate(keysToValue, new WhiteboardEntry("remove","none"));
					} catch (InvalidWhiteboardEntryException e) {
						e.printStackTrace();
					}
					
				}
			};
		case changeAttribute:
			//TODO: tick und duration implementieren
			return new MdsActionExecutable() {
				
				@Override
				public void execute(GuiInterface guiInterface) {
					//Welches Attribut soll geändert werden?
					List<String> keysToValue = new Vector<String>(Arrays.asList(((String)parsedParams.get("attribute")).split("\\.")));
					Whiteboard currentWb = parseActionString(wb, keysToValue, state, myId);
					
					String attributeToChange;
					try{
						attributeToChange = (String)currentWb.getAttribute(keysToValue.toArray(new String[0])).value;
					}catch(NullPointerException e){
						attributeToChange = (String)parsedParams.get("attribute");
					}
					
					if(params.get("valueType").equals(ADD)){
						try{
							attributeToChange = Double.toString(Double.parseDouble(attributeToChange) + Double.parseDouble((String)parsedParams.get("value")));
						}catch(NumberFormatException nfe){
							nfe.printStackTrace();
						}
					}else if(params.get("valueType").equals(MULTIPLY)){
						try{
							attributeToChange = Double.toString(Double.parseDouble(attributeToChange) * Double.parseDouble((String)parsedParams.get("value")));
						}catch(NumberFormatException nfe){
							nfe.printStackTrace();
						}
					}
					
					try {
						currentWb.setAttributeValue(attributeToChange, keysToValue.toArray(new String[0]));
						sii.onWhiteboardUpdate(keysToValue, currentWb.getAttribute(keysToValue.toArray(new String[0])));
					} catch (InvalidWhiteboardEntryException e) {
						e.printStackTrace();
					}
					
					
				}
			};
		case useItem:
			
			//Vorbereitung: Item finden
			//List<String> keysToItem = new Vector<String>(Arrays.asList((String)parsedParams.get("target")).split("\\.")));
			// Whiteboard currentWb = (Whiteboard)parsedParams.get("target");parseActionString(wb, keysToItem, state, myId);
			//Item, dessen useAction(s) ausgeführt werden sollen
			final Whiteboard item = (Whiteboard)parsedParams.get("target");//(Whiteboard) currentWb.getAttribute(keysToItem.toArray(new String[0])).value;
			final String typeString = type;
			
			
			return new MdsActionExecutable() {
				@Override
				public void execute(GuiInterface guiInterface) {
					
					Set<String> actions = ((Whiteboard)item.getAttribute("useAction").value).keySet();
					//Alle useActions ausführen, dabei steht in action der aktuelle Name
					//TODO: so wie es jetzt ist (auch wie es geparst wird) kann von jedem Typ immer nur
					//eine Action da sein. Es können also zb. keine zwei "changeAttribute" aufrufe vorkommen
					for(String action : actions){
						MdsActionIdent actionIdent = null;
						
						//entsprechenden Enum suchen
						if(action.equals("showVideo")){
							actionIdent = MdsActionIdent.showVideo;
						}else if(action.equals("showMap")){
							actionIdent = MdsActionIdent.showMap;
						}else if(action.equals("showText")){
							actionIdent = MdsActionIdent.showText;
						}else if(action.equals("showImage")){
							actionIdent = MdsActionIdent.showImage;
						}else if(action.equals("addToGroup")){
							actionIdent = MdsActionIdent.addToGroup;
						}else if(action.equals("removeFromGroup")){
							actionIdent = MdsActionIdent.removeFromGroup;
						}else if(action.equals("changeAttribute")){
							actionIdent = MdsActionIdent.changeAttribute;
						}else if(action.equals("useItem")){
							actionIdent = MdsActionIdent.useItem;
						}else if(action.equals("updateMap")){
							actionIdent = MdsActionIdent.updateMap;
						}
						
						//Params auslesen und in action Params kopieren
						Set<String> allParamKeys = ((Whiteboard)item.getAttribute("useAction", action).value).keySet();
						HashMap<String,String> actionParams = new HashMap<String, String>();
						for(String paramName : allParamKeys){
							actionParams.put(paramName, (String) item.getAttribute("useAction", action, paramName).value);
						}
						
						//Ausführbare Action erzeugen und sie danach ausführen
						MdsActionExecutable realAction = parseAction(typeString, new MdsAction(actionIdent, actionParams), state, wb, myId, sii);
						realAction.execute(guiInterface);
					}
					
				}
			};
		default:
			return null;
		}
		
		
		
		
		
	}
	
	private Object parseParam(String param, MdsState state, Whiteboard wb, String playerId){
		
		Log.i(Interpreter.LOGTAG,"parseParam:"+param);
		//Ersetzungen gemäß der Spezisprache vorbereiten 
		HashMap<String, String> replacements = new HashMap<String, String>();
		replacements.put("self",Interpreter.WB_PLAYERS+"."+playerId);
		
		
		
		for(String toReplace : replacements.keySet()){
			//Log.i(Interpreter.LOGTAG,"parseParam replace every occurence of ["+toReplace+"]");
			param = param.replace(toReplace, replacements.get(toReplace));
		}
		Log.i(Interpreter.LOGTAG,"parseParam after replacements:"+param);
		
		//Einzelne Teile, die Punkten getrennt sind aufsplitten
		List<String> splitted = new Vector<String>(Arrays.asList(param.split("\\.")));
		
		Log.i(Interpreter.LOGTAG,"parseParam splittedParamLength:"+splitted.size());
		
		//Wenn das Schlüsselwort "Objekt" oder "Subject" vorkommt, werden dessen Attribute genutzt
		if(splitted.get(0).equals("object")){
			
			splitted.remove(0);
			String[] keys = (String[]) splitted.toArray(new String[0]);

			// TODO: erstmal nur mit einem
			List<WhiteboardEntry> objects = state.getObjects();
			if(objects == null){
				Log.e(Interpreter.LOGTAG,"Error: no objects(from trigger) in whiteboard in state "+state.getName()+ " trying currentState...");
				MdsState currentState = null;
				try{
					currentState = (MdsState)wb.getAttribute(Interpreter.WB_PLAYERS,""+playerId,FsmManager.CURRENT_STATE).value;
					objects = (currentState).getObjects();
				}catch(Exception e){
					String stateName = currentState != null ? currentState.getName() : "null";
					Log.e(Interpreter.LOGTAG,"Error while getting objects from whiteboard in state "+ stateName);
				}
				if(objects == null){
					Log.e(Interpreter.LOGTAG,"Still no luck while getting objects from whiteboard, still null");
				}
			}
			Log.i(Interpreter.LOGTAG, "parseParam: objectsSize: "+objects.size());
			Log.i(Interpreter.LOGTAG, "parseParam: object[0] "+objects.get(0).value.toString());
			Object o = ((Whiteboard)objects.get(0).value).getAttribute(keys);
			if(keys.length >0 ){
				return (String) ((Whiteboard)objects.get(0).value).getAttribute(keys).value;
			}else{
				return (Whiteboard)objects.get(0).value;
			}
		} else if (splitted.get(0).equals("subject")) {
			splitted.remove(0);
			String[] keys = (String[]) splitted.toArray(new String[0]);
			// erstmal nur mit einem
			List<WhiteboardEntry> subjects = state.getSubjects();
			return (String) ((Whiteboard)subjects.get(0).value).getAttribute(keys).value;

		} else if (splitted.get(0).equals("notValue")) {
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
	
	
	private Whiteboard parseActionString(Whiteboard root, List<String> keysToValue, MdsState state, String myId){
		//Whiteboard, in dem das zuändernde Attribut liegt
		Whiteboard currentWb = root;
		
		if(keysToValue.get(0).equals("self")){
			currentWb = (Whiteboard) root.getAttribute(Interpreter.WB_PLAYERS,myId+"").value;
			keysToValue.remove(0);
		}else if(keysToValue.get(0).equals("subject")){
			currentWb = (Whiteboard) state.getSubjects().get(0).value;
			keysToValue.remove(0);
		}else if(keysToValue.get(0).equals("object")){
			currentWb = (Whiteboard) state.getObjects().get(0).value;
			keysToValue.remove(0);
		}
		
		return currentWb;
	}
	
}
