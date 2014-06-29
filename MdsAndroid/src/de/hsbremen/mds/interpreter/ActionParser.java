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
import de.hsbremen.mds.common.valueobjects.GameResult;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsTransition;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsAction.MdsActionIdent;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsActionExecutable;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsImageAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsMapAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsMiniAppAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsTextAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsVideoAction;
import de.hsbremen.mds.common.whiteboard.InvalidWhiteboardEntryException;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;

public class ActionParser {
	
	
	public static final String ADD = "add";
	public static final String MULTIPLY = "multiply";
	public static final String SET = "set";

	/**
	 * Macht aus einer MdsAction eine ausführbare MdsActionExecutable, die man mit .execute() dann ausführen kann.
	 * @param action Action, die geparst werden soll
	 * @param state State in dem die Action ausgeführt wird
	 * @param triggerEvent	Das Event, dass diese Action auslöste
	 * @param wb	Whiteboard
	 * @param myId	Id des Spielers, der diese Action ausführt
	 * @return	Ausführbares MdsExecutableAction Objekt
	 */
	public MdsActionExecutable parseAction(String type, MdsAction action, final MdsState state, final Whiteboard wb, final List<String> myGroup, final String myId, final ServerInterpreterInterface sii){
		
		if(action == null){
			//Action exisitert eigentlich gar nicht? -> GTFO!
			return null;
		}
	
		//Parameter der Action
		final HashMap<String, Object> params = action.getParams();
		final HashMap<String, Object> parsedParams = new HashMap<String, Object>();
		
		// Buttons heraussuchen, falls state transitions hat
		List<String> buttons = new Vector<String>();
		if (state != null) {
			MdsState buttonState = (MdsState) wb.getAttribute(myGroup, myId + "","currentState").value;
			Log.i(Interpreter.LOGTAG, "Parse Action: " + action.getIdent() + " des States: " + buttonState.getName());
			MdsTransition[] trans = buttonState.getTransitions();
			if (trans != null && (type.equals("start") || type.equals("do"))) {		
				// Alle Transitions durchgehen
				for(int i = 0; i < trans.length; i++) {
					if (trans[i].getEventType() == MdsTransition.EventType.uiEvent) {
						buttons.add(trans[i].getCondition()[0].getName());
					}
				}
			}
		}
		
		//Jeden Parameter parsen/interpretieren
		for(String key : params.keySet()){
			if (params.get(key) instanceof String)
				parsedParams.put(key, EventParser.parseParam((String)params.get(key), state, wb, myGroup, myId));
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
			// not called atm
		case startMiniApp:
			Log.i(Interpreter.LOGTAG, "Executing Minigame " + parsedParams.get("type"));
			return new MdsMiniAppAction((String)parsedParams.get("type"), null, buttons);			
		case updateMap:
			return new MdsActionExecutable() {
				
				@Override
				public void execute(GuiInterface guiInterface) {
					//Map anzeigen
					double lat,lon;
					try{
						lat  = Double.parseDouble((String)wb.getAttribute(myGroup,""+myId, "latitude").value);
						lon  = Double.parseDouble((String)wb.getAttribute(myGroup,""+myId, "longitude").value);
					}catch(Exception e){
						lat = 0;
						lon = 0;
					}
					MdsMapAction mma = new MdsMapAction("showMap", lat, lon);
					
					
					Log.i(Interpreter.LOGTAG, "Changing Map Entities: Interface is: " + guiInterface.toString());
					//Und Bomben und Medipacks anzeigen
					changeMapEntities(guiInterface, wb, myGroup);
					//Map anzeigen
					mma.execute(guiInterface);
					
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
					Log.i("Mistake", "Target Value is " + target.value);
					Log.i(Interpreter.LOGTAG, "Target found");
					
					// create copy of object
					if(target == null) Log.e(Interpreter.LOGTAG, "Target bei AddtoGroup Null");
					//Log.i(Interpreter.LOGTAG, "Keys sind" + keys[0] + keys[1] + ((keys[2] != null) ? keys[2] : "kein dritter key"));
					WhiteboardEntry copy;
					try {
						copy = new WhiteboardEntry(target.value, target.visibility);
						// fill new Element into Whiteboard
						// get group
						Log.i(Interpreter.LOGTAG, "Getting group in AddToGroup");
						Whiteboard group = (Whiteboard) ((WhiteboardEntry)parsedParams.get("group")).value;
						String[] groupKeys = ((String)params.get("group")).split("\\.");
						
						// immer gruppe + name für server
						List<String> keysToValue = new Vector<String>();
						// Keys der keysToValue hinzufügen
						for(String key : groupKeys)
							keysToValue.add(key);

						// füge item der Gruppe hinzu
						group.put((String)((Whiteboard)target.value).get("pathKey").value, copy);
						Log.i(Interpreter.LOGTAG, "addToGroup: ["+params.get("target")+ "] (["+(String)((Whiteboard)target.value).get("pathKey").value+"]) to group [" 
								+ ((WhiteboardEntry)parsedParams.get("group")).value.toString()+ " + " + (String)((Whiteboard)target.value).get("pathKey").value +"]");
						
						// wenn die Gruppe Inventory war, füge dem Backpack hinzu
						if(groupKeys[groupKeys.length-1].equals("inventory")) {
							((Whiteboard)copy.value).get("visibility").value = "none";
							Log.i(Interpreter.LOGTAG, "Adding Item to Backpack");
							// create item
							Log.i("Mistake", "Title: " +((Whiteboard)target.value).get("title").value.toString());
							Log.i("Mistake", "Image: " + ((Whiteboard)target.value).get("imagePath").value.toString());
							Log.i("Mistake", "pathKey: " + ((Whiteboard)target.value).get("pathKey").value.toString());
							// Look if item has drop action
							boolean isDroppable = false;
							if (((Whiteboard)target.value).get("dropAction") != null)
								isDroppable = true;
							MdsItem item = new MdsItem(((Whiteboard)target.value).get("title").value.toString(), ((Whiteboard)target.value).get("imagePath").value.toString(), 
													   ((Whiteboard)target.value).get("pathKey").value.toString(), isDroppable);
							// and tell android to add in backpack
							guiInterface.addToBackpack(item);
						}
						
						// tell the server
						sii.onWhiteboardUpdate(keysToValue, copy);
						
						// update Map view
						changeMapEntities(guiInterface, wb, myGroup);

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
					Whiteboard currentWb = (Whiteboard)((WhiteboardEntry)parsedParams.get("group")).value;//parseActionString(wb, keysToValue, state, myId);
					Log.i("Mistake", "Group is: " + currentWb.toString());
					String[] keys = ((String)params.get("target")).split("\\.");
					WhiteboardEntry result;
					try {
						// only gives result when path is directly written (e.g. Bombs.Bomb1)
						result = currentWb.remove(keys[keys.length-1]);
						Log.i("Mistake", "Target-Key: " + keys[keys.length-1]);
						Log.i("Mistake", "result is: " + result.toString());
						Log.i("Mistake", "result is: " + result.value.toString());
						
						// aus dem Backpack removen wenn inventory
						Log.i("Mistake", "Dies sollte die Gruppe sein: " + keys[keys.length-2]);
						if(keys[keys.length-2].equals("inventory") && !keys[keys.length-1].equals("dummy")) {
							Log.i(Interpreter.LOGTAG, "Removing Item from Backpack");
							guiInterface.removeFromBackpack(keys[keys.length-1]);
						}
						
						Log.i(Interpreter.LOGTAG, "removeFromGroup: ["+params.get("target")+ "] (["+keys[keys.length-1]+"]) from group [" + params.get("group").toString()+"], is:["+result.value.toString()+"]");
						// server bescheid geben
						List<String> keysToValue = new Vector<String>();
						for(String s : ((String)params.get("target")).split("\\."))
							keysToValue.add(s);
						try {
							sii.onWhiteboardUpdate(keysToValue, new WhiteboardEntry("delete","none"));
						} catch (InvalidWhiteboardEntryException e) {
							e.printStackTrace();
						}
						changeMapEntities(guiInterface, wb, myGroup);
					} catch (NullPointerException npe) {
						// otherwise we use the parsed value
						Log.i(Interpreter.LOGTAG, "Removing an Object");
						
						String key = (String) parsedParams.get("target");
						Log.i("Mistake", "Parsed Key: " + key);
						Log.i("Mistake", "Group is: " + currentWb.toString());
						result = currentWb.remove(key);
						Log.i("Mistake", "result is: " + result.toString());
						Log.i("Mistake", "result is: " + result.value.toString());
						
						Log.i(Interpreter.LOGTAG, "removeFromGroup: ["+params.get("target")+ "] (["+key+"]) from group [" + params.get("group").toString()+"], is:["+result.value.toString()+"]");
						// server bescheid geben
						List<String> keysToValue = new Vector<String>();
						for(String s : ((String)params.get("target")).split("\\."))
							keysToValue.add(s);
						try {
							sii.onWhiteboardUpdate(keysToValue, new WhiteboardEntry("delete","none"));
						} catch (InvalidWhiteboardEntryException e) {
							e.printStackTrace();
						}
						changeMapEntities(guiInterface, wb, myGroup);
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
					Whiteboard currentWb = parseActionString(wb, keysToValue, myGroup,state, myId);
					
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
					}else if(params.get("valueType").equals(SET)){
						Log.i(Interpreter.LOGTAG, "Setting value " + keysToValue.get(keysToValue.size()-1) + " to " + parsedParams.get("value"));
						attributeToChange = (String)parsedParams.get("value");
					}
					
					try {
						Log.i("Mistake", "Setting the value for real");
						currentWb.setAttributeValue(attributeToChange, keysToValue.toArray(new String[0]));
						Log.i("Mistake", "Telling the Server");
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
						HashMap<String,Object> actionParams = new HashMap<String, Object>();
						for(String paramName : allParamKeys){
							actionParams.put(paramName, (String) item.getAttribute("useAction", action, paramName).value);
						}
						
						//Ausführbare Action erzeugen und sie danach ausführen
						Log.i("Mistake", "Executing real Action");
						MdsActionExecutable realAction = parseAction(typeString, new MdsAction(actionIdent, actionParams), state, wb, myGroup, myId, sii);
						realAction.execute(guiInterface);
					}
					
				}
			};
		default:
			return null;
		}
		
		
		
		
		
	}
		
	public boolean parseGameResult(Whiteboard whiteboard, MdsAction action, MdsState state, int points, String identifier, List<String> playerGroup, String myId) {
		Log.i(Interpreter.LOGTAG, "Parsing Game Result");
		// actions des aktuellen States nach identifier durchgucken
		Log.i("Mistake", "Player Whiteboard ist: " + whiteboard.getAttribute(playerGroup, ""+myId).value.toString());
		HashMap<String, Object> actionParams = action.getParams();
		// search in Params for identifier
		for(String key : actionParams.keySet()) {
			if(key.equals("type") && actionParams.get(key).equals(identifier)) {
				// get Result Object
				GameResult[] results = (GameResult[]) actionParams.get("result");
				// get Key to the attribute that has 2 be changed
				
				for(GameResult result : results) {
					String[] attributes = ((String)EventParser.parseParam(result.attribute, state, whiteboard, playerGroup, myId)).split("\\.");
					int minPoints = result.minScore;
					if (minPoints <= points) {
						if(result.setWin != null) {
							whiteboard.getAttribute(attributes).value = result.setWin;
						}
						if(result.addResult != null) {
							double value = Double.parseDouble((String)whiteboard.getAttribute(attributes).value);
							value += points;
							whiteboard.getAttribute(attributes).value = value;
						}
						return true;
					} else {
						if(result.setLoose != null)
							whiteboard.getAttribute(attributes).value = result.setLoose;
						if(result.addResult != null) {
							double value = Double.parseDouble((String)whiteboard.getAttribute(attributes).value);
							value -= points;
							whiteboard.getAttribute(attributes).value = value;
						}
						return false;
					}
				}
			}
		}
		return false;
		// no key = identifier found
	}
	
	
	private Whiteboard parseActionString(Whiteboard root, List<String> keysToValue, List<String> playerGroup, MdsState state, String myId){
		//Whiteboard, in dem das zuändernde Attribut liegt
		Whiteboard currentWb = root;
		
		if(keysToValue.get(0).equals("self")){
			currentWb = (Whiteboard) root.getAttribute(playerGroup, myId+"").value;
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
	
	private void changeMapEntities(GuiInterface guiInterface, Whiteboard wb, List<String> playerGroup) {
		
		//TODO: add more visibilities
		ArrayList<MdsItem> mapEntities = getEntriesAsItem(wb, wb, playerGroup, "", "all", "ownGroup");
		Log.i(Interpreter.LOGTAG, "Size of Entities ist: " + mapEntities.size() + ", GuiInterface: " + guiInterface);
		guiInterface.showMap(mapEntities);	
	}
	
	/**
	 * Geht rekursiv alle Entries eines Whiteboards durch und prüft, ob dieses das Attribut "visibility"
	 * enthält. Ist das der Fall, wird ein MdsItem angelegt und dies in die Liste eingefügt.
	 * @param wb Whiteboard, bei dem begonnen werden soll zu prüfen
	 * @param pathKey Key, unterdem dieses Whiteboard im Whiteboard dadrüber zu finden ist (root ist "")
	 * @param visibility Array von Sichtbarkeiten, die ein Whiteboard haben kann, um in die Liste zu kommen
	 * @return ArrayList von MdsItems, welche aus Whiteboards erzeugt wurden, die die verlangte Visibility haben
	 */
	private ArrayList<MdsItem> getEntriesAsItem(Whiteboard root, Whiteboard wb, List<String> playerGroup,String pathKey, String... visibility ){
		ArrayList<MdsItem> items = new ArrayList<MdsItem>();
		
		//Mich (Whiteboard wb) hinzufügen?
		boolean addMe = false;
		if(wb.containsKey("visibility")){
			for(String v : visibility){
				if(((String)wb.getAttribute("visibility").value).equals(v)){
					// if key equals all add directly
					if(v.equals("all")) {
						addMe = true;
						break;
					// else look if key equals ownGroup.title	
					} else {
						Log.i("Mistake", "Group is: " + v);
						Log.i("Mistkae", "OnwGroup Title ist: " + root.getAttribute(playerGroup, "title").value);
						if (v.equals((String)root.getAttribute(playerGroup, "title").value)) {
							addMe = true;
							break;
						}
						
					}
				}
			}
		}
		if(addMe && pathKey != "object"){
			Log.d(Interpreter.LOGTAG, "EntriesAsItem: adding to List:"+pathKey);
			//mdsItem erzeugen
			String title = "NoTitleAvailable";
			String imgPath = "NoImageAvailable";
			if(wb.containsKey("iconName")){
				title = (String)wb.getAttribute("iconName").value;
			}
			if(wb.containsKey("imagePath")){
				imgPath = (String)wb.getAttribute("imagePath").value;
			}
			MdsItem item = new MdsItem(title, imgPath, pathKey, false);
			if(wb.containsKey("latitude") &&  wb.containsKey("longitude")){
				try {
					//item.setName(pathKey);
					item.setLatitude(Double.parseDouble((String)wb.getAttribute("latitude").value));
					item.setLongitude(Double.parseDouble((String)wb.getAttribute("longitude").value));
				} catch (NumberFormatException e1) {
					Log.e(Interpreter.LOGTAG, "No Position set. Player might be at the nordpol");
				}
			}
			items.add(item);
		}
		
		//Rekursiv alle Whiteboards in diesem Whiteboard prüfen
		for(String key : wb.keySet()){
			if(wb.getAttribute(key).value instanceof Whiteboard){
				items.addAll(getEntriesAsItem(root, (Whiteboard)wb.getAttribute(key).value, playerGroup, key, visibility));
			}
		}
		
		return items;
	}
	
}
