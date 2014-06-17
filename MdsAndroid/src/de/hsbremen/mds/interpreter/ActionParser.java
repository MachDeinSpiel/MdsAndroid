package de.hsbremen.mds.interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import android.nfc.Tag;
import android.util.Log;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.common.interfaces.GuiInterface;
import de.hsbremen.mds.common.interfaces.ServerInterpreterInterface;
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
		Log.i(Interpreter.LOGTAG, "Parse Action: " + action.getIdent() + " des States: " + buttonState.getName());
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
					
					
					Log.i(Interpreter.LOGTAG, "Changing Map Entities: Interface is: " + guiInterface.toString());
					changeMapEntities(guiInterface, wb);
					//Map anzeigen
					mma.execute(guiInterface);
					//Und Bomben und Medipacks anzeigen
					//TODO: Wenn die Visibillity klar definiert wird, entsprechende Items anzeigen
					
				}
			}; 
		case showImage:
			return new MdsImageAction((String)parsedParams.get("title"),(String)parsedParams.get("url"), (String)parsedParams.get("text"), buttons);
		case showText:
			Log.i(Interpreter.LOGTAG, "Returning Text Action");
			return new MdsTextAction("showText", (String)parsedParams.get("text"), buttons);
		case addToGroup:
			return new MdsActionExecutable() {
				
				@Override
				public void execute(GuiInterface guiInterface) {
					
					Log.i(Interpreter.LOGTAG, "AddToGroup wird ausgeführt");
					// get target
					WhiteboardEntry target = (WhiteboardEntry) parsedParams.get("target");
					Log.i(Interpreter.LOGTAG, "Target found");
					//String[] keys = { "Players", "1", "object"};
					
					// create copy of object
					if(target == null) Log.e(Interpreter.LOGTAG, "Target Null");
					//Log.i(Interpreter.LOGTAG, "Keys sind" + keys[0] + keys[1] + ((keys[2] != null) ? keys[2] : "kein dritter key"));
					WhiteboardEntry copy;
					try {
						copy = new WhiteboardEntry(target.value, target.visibility);
						// fill new Element into Whiteboard
						// get group
						Whiteboard group = (Whiteboard) parsedParams.get("group");
						String[] groupKeys = params.get("group").split("\\.");
						
						// immer gruppe + name für server
						List<String> keysToValue = new Vector<String>();
						// Keys der keysToValue hinzufügen
						for(String key : groupKeys)
							keysToValue.add(key);
						keysToValue.add("dummy");
						
						// delete dummy if there
						if ((group).get("dummy") != null) {
							Log.i(Interpreter.LOGTAG, "Deleting dummy in Inventory");
							(group).remove("dummy");
							// tell the server
							sii.onWhiteboardUpdate(keysToValue, new WhiteboardEntry("delete","none"));
						} else {
							Log.i(Interpreter.LOGTAG, "No Dummy found in Inventory");
						}
						// füge item der Gruppe hinzu
						group.put((String)((Whiteboard)target.value).get("pathKey").value, copy);
						Log.i(Interpreter.LOGTAG, "addToGroup: ["+params.get("target")+ "] (["+(String)((Whiteboard)target.value).get("pathKey").value+"]) to group [" 
								+ parsedParams.get("group").toString()+ " + " + (String)((Whiteboard)target.value).get("pathKey").value +"]");
						
						// tell the server
						sii.onWhiteboardUpdate(keysToValue, copy);
						
						// wenn die Gruppe Inventory war, füge dem Backpack hinzu
						if(groupKeys[groupKeys.length-1].equals("inventory")) {
							Log.i(Interpreter.LOGTAG, "Adding Item to Backpack");
							// create item
							Log.i("Mistake", "Title: " +((Whiteboard)target.value).get("title").value.toString());
							Log.i("Mistake", "Image: " + ((Whiteboard)target.value).get("imagePath").value.toString());
							Log.i("Mistake", "pathKey: " + ((Whiteboard)target.value).get("pathKey").value.toString());
							MdsItem item = new MdsItem(((Whiteboard)target.value).get("title").value.toString(), ((Whiteboard)target.value).get("imagePath").value.toString(), 
													   ((Whiteboard)target.value).get("pathKey").value.toString());
							// and tell server to add in backpack
							guiInterface.addToBackpack(item);
						}
						// update Map view
						changeMapEntities(guiInterface, wb);

					} catch (InvalidWhiteboardEntryException e1) {
						Log.e(Interpreter.LOGTAG, "Could not create Copy-WBEntry");
						e1.printStackTrace();
					}
					
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
					
					Log.i(Interpreter.LOGTAG, "removeFromGroup: ["+params.get("target")+ "] (["+keys[keys.length-1]+"]) from group [" + params.get("group").toString()+"], is:["+result.value.toString()+"]");
					// server bescheid geben
					List<String> keysToValue = new Vector<String>();
					for(String s : params.get("target").split("\\."))
						keysToValue.add(s);
					try {
						sii.onWhiteboardUpdate(keysToValue, new WhiteboardEntry("delete","none"));
					} catch (InvalidWhiteboardEntryException e) {
						e.printStackTrace();
					}
					changeMapEntities(guiInterface, wb);
					
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
			
			Log.i("Mistake", "Executing Use Item");
			//Vorbereitung: Item finden
			//List<String> keysToItem = new Vector<String>(Arrays.asList((String)parsedParams.get("target")).split("\\.")));
			// Whiteboard currentWb = (Whiteboard)parsedParams.get("target");parseActionString(wb, keysToItem, state, myId);
			//Item, dessen useAction(s) ausgeführt werden sollen
			Log.i("Mistake", parsedParams.toString());
			final Whiteboard item = (Whiteboard)((WhiteboardEntry)parsedParams.get("target")).value;//(Whiteboard) currentWb.getAttribute(keysToItem.toArray(new String[0])).value;
			Log.i("Mistake", "Received target");
			final String typeString = type;
			
			
			return new MdsActionExecutable() {
				@Override
				public void execute(GuiInterface guiInterface) {
					
					Set<String> actions = ((Whiteboard)item.getAttribute("useAction").value).keySet();
					Log.i("Mistake", "Executing UseAction des item");
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
						Log.i("Mistake", "Executing real Action");
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
		
		Object tmpObj = wb;
		// Wenn das Schlüsselwort "self vorkommt"
		if(splitted.size() > 2 && splitted.get(0).equals("Players") &&  splitted.get(1).equals(""+playerId) && splitted.get(2).equals("object")) {
			Log.i(Interpreter.LOGTAG, "Parse Object of Self");
			// auf Whiteboard des Spielers setzen und "Players" + ID löschen
			splitted.remove(0);
			splitted.remove(0);
			tmpObj = (Whiteboard) ((Whiteboard)tmpObj).getAttribute(Interpreter.WB_PLAYERS, ""+playerId).value;
		}
		if(splitted.size() > 0 && splitted.get(0).equals(FsmManager.CURRENT_STATE)) {
			Log.i(Interpreter.LOGTAG, "Parse Current");
			splitted.remove(0);
			tmpObj = (MdsState) ((Whiteboard)tmpObj).getAttribute(Interpreter.WB_PLAYERS,""+playerId, FsmManager.CURRENT_STATE).value;
			
		} else if (splitted.size() > 0 &&  splitted.get(0).equals(FsmManager.LAST_STATE)) {
			Log.i(Interpreter.LOGTAG, "Parse Last");
			splitted.remove(0);
			tmpObj = (MdsState) ((Whiteboard)tmpObj).getAttribute(Interpreter.WB_PLAYERS,""+playerId, FsmManager.LAST_STATE).value;
		}
		//Wenn das Schlüsselwort "Objekt" oder "Subject" vorkommt, werden dessen Attribute genutzt
		if(splitted.size() > 0 && splitted.get(0).equals("object")){
			
			splitted.remove(0);
			String[] keys = (String[]) splitted.toArray(new String[0]);

			Log.i("Mistake", "Object found...");
			// TODO: erstmal nur mit einem
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
				Log.e(Interpreter.LOGTAG,"Error: no objects(from trigger) in whiteboard in state "+state.getName()+ " trying currentState...");
				Whiteboard currentState = null;
				try{
					currentState = (Whiteboard)wb.getAttribute(Interpreter.WB_PLAYERS,""+playerId,FsmManager.CURRENT_STATE).value;
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
			Object o = ((Whiteboard)objects.get(""+0).value).getAttribute(keys);
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
	
	// TODO: Darf natürlich nicht hart gecoded sein
	private void changeMapEntities(GuiInterface guiInterface, Whiteboard wb) {
		
		ArrayList<MdsItem> mapEntities = new ArrayList<MdsItem>();
		
		for(String key : ((Whiteboard)wb.getAttribute("Bombs").value).keySet()){
			//Whiteboard bomb = (Whiteboard)wb.getAttribute("Bombs",key).value;
			//mapEntities.add(new MdsItem((String)bomb.getAttribute("name").value, ""));
			Log.d(Interpreter.LOGTAG, "["+key+"] in die Liste eingefügt.");
			// get visibility of item
			String vis = ((Whiteboard) wb.getAttribute("Bombs").value).get(key).visibility;
			MdsItem item = new MdsItem(key, "", key);
//			if(vis == "mine" || vis == "all") {
				item.setLongitude(Double.parseDouble((String)wb.getAttribute("Bombs",key,"longitude").value));
				item.setLatitude(Double.parseDouble((String)wb.getAttribute("Bombs",key,"latitude").value));
				mapEntities.add(item);
//			}
			
		}
		for(String key : ((Whiteboard)wb.getAttribute("Medipacks").value).keySet()){
			//Whiteboard bomb = (Whiteboard)wb.getAttribute("Medipacks",key).value;
			//mapEntities.add(new MdsItem((String)bomb.getAttribute("name").value, ""));
			// get visibility of item
			String vis = ((Whiteboard) wb.getAttribute("Medipacks").value).get(key).visibility;
			MdsItem item = new MdsItem(key, "", key);
			if(vis == "mine" || vis == "all") {
				item.setLongitude(Double.parseDouble((String)wb.getAttribute("Medipacks",key,"longitude").value));
				item.setLatitude(Double.parseDouble((String)wb.getAttribute("Medipacks",key,"latitude").value));
				mapEntities.add(item);
			}
		}
		Log.i(Interpreter.LOGTAG, "Size of Entities ist: " + mapEntities.size() + ", GuiInterface: " + guiInterface);
		guiInterface.showMap(mapEntities);	
	}
	
}
