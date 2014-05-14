package de.hsbremen.mds.interpreter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import de.hsbremen.mds.common.interfaces.GuiInterface;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsAction.MdsActionIdent;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsActionExecutable;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsImageAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsMapAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsTextAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsVideoAction;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;

public class ActionParser {
	
	
	public static final String ADD = "add";
	public static final String MULTIPLY = "multiply";

	/**
	 * Macht aus einer MdsAction eine ausführbare MdsActionExecutable, die man mit .execute() dann ausführen kann.
	 * @param action Action, die geparst werden soll
	 * @param triggerEvent	Das Event, dass diese Action auslöste
	 * @param wb	Whiteboard
	 * @param myId	Id des Spielers, der diese Action ausführt
	 * @return	Ausführbares MdsExecutableAction Objekt
	 */
	public MdsActionExecutable parseAction(MdsAction action, final MdsState state, final Whiteboard wb, final int myId){
		
	
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
		
		//Je nach dem, von welchem Ident die Action ist, werden verschiedene MdsActionExecutables zurückgegeben 
		switch(action.getIdent()){
		case showVideo:
			return new MdsVideoAction(params.get("title"), params.get("url"), params.get("text"));
		case showMap:
		case updateMap:
			return new MdsMapAction("Map", Double.parseDouble(params.get("longitude")), Double.parseDouble(params.get("latitude")));
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
					
					List<String> keysToValue = Arrays.asList(params.get("group").split("\\."));
					Whiteboard currentWb = parseActionString(wb, keysToValue, state, myId);
					currentWb.remove(params.get("target"));
					
				}
			};
		case changeAttribute:
			//TODO: tick und duration implementieren
			return new MdsActionExecutable() {
				
				@Override
				public void execute(GuiInterface guiInterface) {
					//Welches Attribut soll geändert werden?
					List<String> keysToValue = Arrays.asList(params.get("attribute").split("\\."));
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
					
					currentWb.setAttributeValue(attributeToChange, keysToValue.toArray(new String[0]));
					
					
				}
			};
		case useItem:
			
			//Vorbereitung: Item finden
			List<String> keysToItem = Arrays.asList(params.get("target").split("\\."));
			 Whiteboard currentWb = parseActionString(wb, keysToItem, state, myId);
			//Item, dessen useAction(s) ausgeführt werden sollen
			final Whiteboard item = (Whiteboard) currentWb.getAttribute(keysToItem.toArray(new String[0])).value;
			
			
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
						MdsActionExecutable realAction = parseAction(new MdsAction(actionIdent, actionParams), state, wb, myId);
						realAction.execute(guiInterface);
					}
					
				}
			};
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
		List<String> splitted = Arrays.asList(param.split("\\."));
		
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
	
	
	private Whiteboard parseActionString(Whiteboard root, List<String> keysToValue, MdsState state, int myId){
		//Whiteboard, in dem das zuändernde Attribut liegt
		Whiteboard currentWb = root;
		
		if(keysToValue.get(0).equals("self")){
			currentWb = (Whiteboard) root.getAttribute("players",myId+"").value;
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
