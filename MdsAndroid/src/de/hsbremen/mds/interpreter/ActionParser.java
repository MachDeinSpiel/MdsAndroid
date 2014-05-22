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
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
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
	 * Macht aus einer MdsAction eine ausf�hrbare MdsActionExecutable, die man mit .execute() dann ausf�hren kann.
	 * @param action Action, die geparst werden soll
	 * @param triggerEvent	Das Event, dass diese Action ausl�ste
	 * @param wb	Whiteboard
	 * @param myId	Id des Spielers, der diese Action ausf�hrt
	 * @return	Ausf�hrbares MdsExecutableAction Objekt
	 */
	public MdsActionExecutable parseAction(MdsAction action, final MdsState state, final Whiteboard wb, final int myId, final ServerInterpreterInterface sii){
		
		if(action == null){
			//Action exisitert eigentlich gar nicht? -> GTFO!
			//TODO: evtl schon im Interpreter pr�fen, ob die Action null ist (also keine in der JSON-Datei spezifieziert ist)
			return null;
		}
	
		//Parameter der Action
		final HashMap<String, String> params = action.getParams();
		
		//Jeden Parameter parsen/interpretieren
		for(String key : params.keySet()){
			params.put(key, parseParam(params.remove(key), state, wb, myId));
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
		
		//Je nach dem, von welchem Ident die Action ist, werden verschiedene MdsActionExecutables zur�ckgegeben 
		switch(action.getIdent()){
		case showVideo:
			return new MdsVideoAction(params.get("title"), params.get("url"), params.get("text"));
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
					MdsMapAction mma = new MdsMapAction("Map", lat, lon);
					
					ArrayList<MdsItem> mapEntities = new ArrayList<MdsItem>();
					
					for(String key : ((Whiteboard)wb.getAttribute("Bombs").value).keySet()){
						//Whiteboard bomb = (Whiteboard)wb.getAttribute("Bombs",key).value;
						//mapEntities.add(new MdsItem((String)bomb.getAttribute("name").value, ""));
						mapEntities.add(new MdsItem(key, ""));
					}
					for(String key : ((Whiteboard)wb.getAttribute("Medipacks").value).keySet()){
						//Whiteboard bomb = (Whiteboard)wb.getAttribute("Medipacks",key).value;
						//mapEntities.add(new MdsItem((String)bomb.getAttribute("name").value, ""));
						mapEntities.add(new MdsItem(key, ""));
					}
					//Map anzeigen
					mma.execute(guiInterface);
					//Und Bomben und Medipacks anzeigen
					//TODO: Wenn die Visibillity klar definiert wird, entsprechende Items anzeigen
					guiInterface.showMap(mapEntities);
				}
			}; 
		case showImage:
			return new MdsImageAction(params.get("title"), params.get("url"), params.get("text"));
		case showText:
			return new MdsTextAction(params.get("title"), params.get("text"));
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
					
					List<String> keysToValue = new Vector<String>(Arrays.asList(params.get("group").split("\\.")));
					Whiteboard currentWb = parseActionString(wb, keysToValue, state, myId);
					currentWb.remove(params.get("target"));
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
					//Welches Attribut soll ge�ndert werden?
					List<String> keysToValue = new Vector<String>(Arrays.asList(params.get("attribute").split("\\.")));
					Whiteboard currentWb = parseActionString(wb, keysToValue, state, myId);
					
					
					String attributeToChange = (String)currentWb.getAttribute(keysToValue.toArray(new String[0])).value;
					
					if(params.get("valueType").equals(ADD)){
						try{
							attributeToChange = Double.toString(Double.parseDouble(attributeToChange) + Double.parseDouble(params.get("value")));
						}catch(NumberFormatException nfe){
							nfe.printStackTrace();
						}
					}else if(params.get("valueType").equals(MULTIPLY)){
						try{
							attributeToChange = Double.toString(Double.parseDouble(attributeToChange) * Double.parseDouble(params.get("value")));
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
			List<String> keysToItem = new Vector<String>(Arrays.asList(params.get("target").split("\\.")));
			 Whiteboard currentWb = parseActionString(wb, keysToItem, state, myId);
			//Item, dessen useAction(s) ausgef�hrt werden sollen
			final Whiteboard item = (Whiteboard) currentWb.getAttribute(keysToItem.toArray(new String[0])).value;
			
			
			return new MdsActionExecutable() {
				@Override
				public void execute(GuiInterface guiInterface) {
					
					Set<String> actions = ((Whiteboard)item.getAttribute("useAction").value).keySet();
					//Alle useActions ausf�hren, dabei steht in action der aktuelle Name
					//TODO: so wie es jetzt ist (auch wie es geparst wird) kann von jedem Typ immer nur
					//eine Action da sein. Es k�nnen also zb. keine zwei "changeAttribute" aufrufe vorkommen
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
						
						//Ausf�hrbare Action erzeugen und sie danach ausf�hren
						MdsActionExecutable realAction = parseAction(new MdsAction(actionIdent, actionParams), state, wb, myId, sii);
						realAction.execute(guiInterface);
					}
					
				}
			};
		default:
			return null;
		}
		
		
		
		
		
	}
	
	private String parseParam(String param, MdsState state, Whiteboard wb, int playerId){
		
		Log.i(Interpreter.LOGTAG,"parseParam:"+param);
		//Ersetzungen gem�� der Spezisprache vorbereiten 
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
		
		//Wenn das Schl�sselwort "Objekt" oder "Subject" vorkommt, werden dessen Attribute genutzt
		if(splitted.get(0).equals("object")){
			
			splitted.remove(0);
			String[] keys = (String[]) splitted.toArray(new String[0]);

			// TODO: erstmal nur mit einem
			List<WhiteboardEntry> objects = state.getObjects();
			if(objects == null){
				Log.e(Interpreter.LOGTAG,"Error: no objects(from trigger) in whiteboard");
			}
			return (String) ((Whiteboard)objects.get(0).value).getAttribute(keys).value;
		} else if (splitted.get(0).equals("subject")) {
			splitted.remove(0);
			String[] keys = (String[]) splitted.toArray(new String[0]);
			// erstmal nur mit einem
			List<WhiteboardEntry> subjects = state.getSubjects();
			return (String) ((Whiteboard)subjects.get(0).value).getAttribute(keys).value;

		}
		
		//Ansonsten Daten aus dem Whiteboard holen
		return (String) wb.getAttribute((String[]) splitted.toArray(new String[0])).value;
		
				
	}
	
	
	private Whiteboard parseActionString(Whiteboard root, List<String> keysToValue, MdsState state, int myId){
		//Whiteboard, in dem das zu�ndernde Attribut liegt
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
