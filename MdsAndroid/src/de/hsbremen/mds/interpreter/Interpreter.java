package de.hsbremen.mds.interpreter;

import java.io.File;
import java.security.acl.Owner;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.google.android.gms.internal.ax;

import android.util.Log;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.common.interfaces.ClientInterpreterInterface;
import de.hsbremen.mds.common.interfaces.FsmInterface;
import de.hsbremen.mds.common.interfaces.GuiInterface;
import de.hsbremen.mds.common.interfaces.InterpreterInterface;
import de.hsbremen.mds.common.interfaces.ServerInterpreterInterface;
import de.hsbremen.mds.common.valueobjects.GameResult;
import de.hsbremen.mds.common.valueobjects.MdsText;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsCondition;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsObjectContainer;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsTransition;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsAction.MdsActionIdent;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsActionExecutable;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsMiniAppAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsTextAction;
import de.hsbremen.mds.common.whiteboard.InvalidWhiteboardEntryException;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.common.whiteboard.WhiteboardUpdateObject;
import de.hsbremen.mds.parser.Parser;

/**
 * @author JW
 */
public class Interpreter implements InterpreterInterface, ClientInterpreterInterface, FsmInterface{
	
	public static final String LOGTAG = "InterpreterClient";
	public static final String CURRENT_STATE = "currentState";
	
	private ActionParser actionParser;
	private FsmManager fsmManager;
	private Whiteboard whiteboard;
	private String myId;
	private GuiInterface gui;
	private ServerInterpreterInterface serverInterpreter;
		
	public Interpreter(File json, GuiInterface guiInterface, ServerInterpreterInterface serverInterpreter, String playerId){
		Log.i(LOGTAG, "Interpreter erzeugt");
		this.gui = guiInterface;
		this.serverInterpreter = serverInterpreter;
		this.actionParser = new ActionParser();

		this.myId = playerId;
		whiteboard = new Whiteboard();
		Log.i("Mistake", "Creating Parser");
		new Parser(this,json);	
	}

	
	@Override
	public void pushParsedObjects(MdsObjectContainer objectContainer) {		
		Log.i(LOGTAG, "Geparste Objekte vom Parser bekommen");
		//this.gui.setAndroidListener(this, 5);
		// FIXME: Testausgabe der Objecte
//		for(int i = 0; i < objectContainer.getStates().size(); i++) {
//			Log.i("Mistake", "Getting Transitions of State: " + objectContainer.getStates().get(i).getName());
//			MdsTransition[] trans = objectContainer.getStates().get(i).getTransitions();
//			if(trans != null) {
//				for(int j = 0; j < trans.length; j++) {
//					if(trans[j].getTarget() != null) {
//						Log.i("Mistake", "Transition Target: " + trans[j].getTarget().getName());
//						if(trans[j].getCondition() != null) {
//							Log.i("Mistake", "Condition: " + trans[j].getCondition()[0].getName());
//							if (trans[j].getCondition()[0].getParams().get("object") != null)
//								Log.i("Mistake", "Object der Condition: " + trans[j].getCondition()[0].getParams().get("object").toString());
//							if (trans[j].getCondition()[0].getParams().get("subject") != null)
//								Log.i("Mistake", "Subject der Condition: " + trans[j].getCondition()[0].getParams().get("subject").toString());
//						}	
//					}
//				}
//			}
//		}
		Log.i("Mistake", "Creating FsmManager");
		this.fsmManager = new FsmManager(objectContainer.getStates(),this.whiteboard, this, myId);
	}
	


	@Override
	public void onButtonClick(String buttonName) {
		Log.i(LOGTAG, "onButtonClick ausgef�hrt" + buttonName);
//		if (fsmManager.getCurrentState().getName().equals("miniApp"))
//			// run actions on current state
		fsmManager.checkEvents(buttonName);
		
	}

	@Override
	public void onVideoEnded(String videoName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUserLeftGame(int playerId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPositionChanged(double longitude, double latitude) {
		if(!fsmManager.isRunning()){
			Log.e("Mistake", "Fsm not running");
			return;
		}
		Log.i(LOGTAG, "Neue Position von Android bekommen : [long:"+longitude+" ,|lat:"+latitude+"]");
		
		// tell the server
		try {
			// TODO: alle Aufrufe, bei denen auf den Player geguckt werden m�ssen aktualisiert werden
			whiteboard.setAttributeValue(Double.toString(longitude), fsmManager.getOwnGroup(), myId, "longitude");
		} catch (InvalidWhiteboardEntryException e) {
			e.printStackTrace();
		}
		List<String> keys = new Vector<String>();
		for(String key : fsmManager.getOwnGroup())
			keys.add(key);
		keys.add(""+myId);
		keys.add("longitude");
		serverInterpreter.onWhiteboardUpdate(keys, whiteboard.getAttribute(fsmManager.getOwnGroup(), myId, "longitude"));
		
		try {
			whiteboard.setAttributeValue(Double.toString(latitude), fsmManager.getOwnGroup(), myId, "latitude");
		} catch (InvalidWhiteboardEntryException e) {
			e.printStackTrace();
		}
		keys.remove(keys.size()-1);
		keys.add("latitude");
		serverInterpreter.onWhiteboardUpdate(keys, whiteboard.getAttribute(fsmManager.getOwnGroup(), myId, "latitude"));
		
		fsmManager.checkEvents(null);
		
	}

//	@Override
//	public void onWhiteboardUpdate(List<String> keys, WhiteboardEntry value) {
//		String logKeys = "";
//		for(String s : keys){
//			logKeys += ","+s;
//		}
//		Log.i(LOGTAG, "onWhiteboardUpdate [keys:"+logKeys+" values:"+value+"]");
//		try {
//			whiteboard.setAttributeValue(value, (String[])keys.toArray());
//		} catch (InvalidWhiteboardEntryException e) {
//			e.printStackTrace();
//		}
//		
//		fsmManager.checkEvents(null);
//	}

//	@Override
//	public void onFullWhiteboardUpdate(Whiteboard newWhiteboard) {
//		this.whiteboard = newWhiteboard;
//		fsmManager.initiate();
//	}

	@Override
	public void onStateChange(String setTo) {
		boolean isMiniGame = false;
		if(setTo.equals(CURRENT_STATE)) {
			Log.i(LOGTAG, "Zustand ge�ndert");
			Log.i(LOGTAG, "Last State" + ((MdsState) (whiteboard.getAttribute(fsmManager.getOwnGroup(),myId+"","lastState").value)).getName());
			MdsState current = fsmManager.getCurrentState();
			Log.i(LOGTAG, "Current State" + current.getName());
			// Actions des Current states
			
			if (current.getDoAction() != null) Log.i(LOGTAG, "Do Action des States: " + current.getDoAction().getIdent());
			for(MdsAction startAction : current.getStartAction())  {
				if (startAction != null) Log.i(LOGTAG, "Start Action des States: " + startAction.getIdent());
			}
			if (current.getEndAction() != null) Log.i(LOGTAG, "End Action des States: " + current.getEndAction().getIdent());
			
			// endAction of LastState
			MdsActionExecutable endAction = actionParser.parseAction("end", ((MdsState) (whiteboard.getAttribute(fsmManager.getOwnGroup(),myId+"","lastState").value)).getEndAction(), ((MdsState) (whiteboard.getAttribute(fsmManager.getOwnGroup(),myId+"","lastState").value)), whiteboard, fsmManager.getOwnGroup(), myId, serverInterpreter);
			Log.i(LOGTAG, "Executing End-Action");
			if(endAction != null){
				endAction.execute(gui);
				if (endAction instanceof MdsMiniAppAction) isMiniGame = true;
			}
			// startActions of CurrentState
			for(MdsAction startAction : current.getStartAction()) {
				MdsActionExecutable startActionExec = actionParser.parseAction("start", startAction, 
																		  ((MdsState) (whiteboard.getAttribute(fsmManager.getOwnGroup(),myId+"",FsmManager.LAST_STATE).value)), whiteboard, 
																		   fsmManager.getOwnGroup(), myId, serverInterpreter);
				Log.i(LOGTAG, "Executing Start-Action");
				if(startActionExec != null){
					startActionExec.execute(gui);
					if (startActionExec instanceof MdsMiniAppAction) isMiniGame = true;
				}
			}
			// do Action of CurrentState
			MdsActionExecutable doAction = actionParser.parseAction("do", ((MdsState) (whiteboard.getAttribute(fsmManager.getOwnGroup(), myId+"","currentState").value)).getDoAction(), ((MdsState) (whiteboard.getAttribute(fsmManager.getOwnGroup(),myId+"",FsmManager.LAST_STATE).value)), whiteboard, fsmManager.getOwnGroup(), myId, serverInterpreter);
			if (doAction instanceof MdsMiniAppAction) isMiniGame = true;
			
			Log.i(LOGTAG, "Executing Do-Action");
			
			if(doAction != null){
				doAction.execute(gui);
			}
			// only checkWBCond if Action is not miniGame, miniGame WBconds will be checked later
			if (!isMiniGame && !((String)whiteboard.getAttribute(fsmManager.getOwnGroup(), myId, "longitude").value).equals("null")
				&& fsmManager.getCurrentState().getTransitions() != null)
				fsmManager.checkEvents(null);;
			
			Log.i(LOGTAG, "Health des Spielers: " + whiteboard.getAttribute(fsmManager.getOwnGroup(), myId+"","health").value);
			Log.i(LOGTAG, "Inventory des Spielers: " + whiteboard.getAttribute(fsmManager.getOwnGroup(), myId+"","inventory").value);
		}
	}


	@Override
	public void onWhiteboardUpdate(List<String> keys, WhiteboardEntry entry) {
		String logKeys = "";
		for(String s : keys){
			logKeys += ","+s;
		}
		Log.i(LOGTAG, "onWhiteboardUpdate [keys:"+logKeys+" values:"+entry.value.toString()+"]");
		
		if(keys.size() == 0){
			return;
		}
		
		if(entry.getValue().equals("delete")){
			String elementKey = keys.remove(keys.size()-1);
			Log.i("Mistake", "elementKey ist: " + elementKey);
			try{
				Log.i("Mistake", "Whiteboard: " +whiteboard.toString());
				WhiteboardEntry groupEntry = whiteboard.getAttribute(keys.toArray(new String[0]));
//				if(groupEntry == null) {
//					Log.e(LOGTAG, "No Item Found to Key " + elementKey + ". Maybe it has already been removed");
//					return;
//				}
				Whiteboard group = (Whiteboard) groupEntry.value;
				group.remove(elementKey);
			}catch(ClassCastException cce){
				logKeys = "";
				for(String s : keys){
					logKeys += ","+s;
				}
				Log.e(LOGTAG, "Couldn't delete ["+elementKey+"] from 'group' ["+logKeys+"] since it's not a group");
			}
		}else{
		
			try {
				whiteboard.setAttributeValue(entry, (String[])keys.toArray(new String[0]));
			} catch (InvalidWhiteboardEntryException e) {
				e.printStackTrace();
			}
		}
		
		fsmManager.checkEvents(null);
		
		
	}


	@Override
	// wird auf jeden Fall aus dem Backpack ausgef�hrt
	public void useItem(MdsItem item, String identifier) {
		// use item
		if(identifier.equals("use")) {
			doUseItem(item);
		// remove item
		} else if(identifier.equals("remove")) {
			deleteBackpackItem(item);
		} else if (identifier.equals("drop")) {
			doDropItem(item);			
		}
		fsmManager.checkEvents(null);
				
	}


	private void doDropItem(MdsItem item) {
		Log.i(LOGTAG, "User is dropping an item");
		
		// dropAction ausf�hren
		Whiteboard wbItem = (Whiteboard)whiteboard.getAttribute(fsmManager.getOwnGroup(), ""+myId, "inventory", item.getPathKey()).value;
		Whiteboard dropAction = (Whiteboard)wbItem.get("dropAction").value;
		// jede Action ausf�hren
		for(String action : dropAction.keySet()) {
			Log.i("Mistake", "Action ist " + action);
			Whiteboard wbAction = (Whiteboard)dropAction.get(action).value;
			// get Params of Action
			HashMap<String, Object> params = new HashMap<String, Object>();
			for(String actionParam : wbAction.keySet()) {
				Log.i("Mistake", "Adding Param " + actionParam);
				params.put(actionParam, (String)wbAction.get(actionParam).value);
			}
			// create Action
			MdsAction realAction;
			if (action.equals("addToGroup")) {
				Log.i("Mistake", "DropAction ist AddToGroup");
				realAction = new MdsAction(MdsActionIdent.addToGroup, params);
			} else if (action.equals("removeFromGroup")) {
				realAction = new MdsAction(MdsActionIdent.removeFromGroup, params);
			} else if (action.equals("changeAttribute")) {
				realAction = new MdsAction(MdsActionIdent.changeAttribute, params);
			} else if (action.equals("showImage")) {
				realAction = new MdsAction(MdsActionIdent.showImage, params);
			} else if (action.equals("showMap")) {
				realAction = new MdsAction(MdsActionIdent.showMap, params);
			} else if (action.equals("showText")) {
				realAction = new MdsAction(MdsActionIdent.showText, params);
			} else if (action.equals("showVideo")) {
				realAction = new MdsAction(MdsActionIdent.showVideo, params);
			} else if (action.equals("startMiniApp")) {
				realAction = new MdsAction(MdsActionIdent.startMiniApp, params);
			} else if (action.equals("updateMap")) {
				realAction = new MdsAction(MdsActionIdent.updateMap, params);
			} else if (action.equals("useItem")) {
				realAction = new MdsAction(MdsActionIdent.useItem, params);
			} else {
				Log.e(LOGTAG, "Die DropAction konnte nicht identifiziert werden");
				return;
			}
			// parse action
			MdsActionExecutable actionExec = actionParser.parseAction("drop", realAction, fsmManager.getCurrentState(), whiteboard, fsmManager.getOwnGroup(), myId, serverInterpreter);
			// and execute if not null
			if (actionExec != null)
				actionExec.execute(gui);
		}
		
		// Testausgabe
		Log.i("Mistake", "Flags ist: " + whiteboard.get("Flags").value);
		
		// Item aus dem Backpack removen
		deleteBackpackItem(item);
	}


	private void deleteBackpackItem(MdsItem item) {
		Log.i(LOGTAG, "User is removing an item");
		// Item aus dem Backpack removen
		Whiteboard inventory = (Whiteboard)whiteboard.getAttribute(fsmManager.getOwnGroup(), ""+myId, "inventory").value;
		inventory.remove(item.getPathKey());		
		// Testausgabe
		Log.i("Mistake", "Inventory des Spielers ist: " + whiteboard.getAttribute(fsmManager.getOwnGroup(), ""+myId, "inventory").value.toString());
	}


	private void doUseItem(MdsItem item) {
		Log.i(LOGTAG, "User is using an item");
		// get Item
		WhiteboardEntry wbItem = whiteboard.getAttribute(fsmManager.getOwnGroup(), ""+myId, "inventory", item.getPathKey());
		// get useAction
		WhiteboardEntry useAction = ((Whiteboard)wbItem.value).get("useAction");
		for (String key : ((Whiteboard)useAction.value).keySet()) {
			Log.i(LOGTAG, "Executing Action Use: " + key + " of Item " + item.getImagePath());
			// get Action
			WhiteboardEntry wbAction = ((Whiteboard)useAction.value).get(key);
			
			// get Params
			HashMap<String, Object> params = new HashMap<String, Object>();
			for(String actionParam : ((Whiteboard)wbAction.value).keySet()) {
				Log.i(LOGTAG, "Adding Param " + actionParam + " to Action");
				// bei removeFrom Group muss das Inventory als Group angegeben werden
				if (key.equals("removeFromGroup") && actionParam.equals("group"))
					params.put("group", "self.inventory");
				else
					params.put(actionParam, (String)((Whiteboard)wbAction.value).get(actionParam).value);
			}

			// parse Action
			MdsActionIdent ident = null;
			if (key.equals("changeAttribute"))
				ident = MdsActionIdent.changeAttribute;
			else if (key.equals("removeFromGroup"))
				ident = MdsActionIdent.removeFromGroup;
			MdsAction action = new MdsAction(ident, params);
			// TODO: Exceptions schreiben
			if(ident == null) {
				Log.e(LOGTAG, "No Action Ident found in Action " + key + ". Returning nothing");
				return;
			}
			MdsState state = (MdsState)(whiteboard.getAttribute(fsmManager.getOwnGroup(),myId+"","lastState").value);
			MdsActionExecutable actionExecute = actionParser.parseAction("user", action, state, whiteboard, fsmManager.getOwnGroup(), myId, serverInterpreter);
			
			// execute Action if possible
			if(actionExecute != null) {
				actionExecute.execute(gui);
			}
			// Testausgabe
			Log.i("Mistake", "Inventory des Spielers ist: " + whiteboard.getAttribute(fsmManager.getOwnGroup(), ""+myId, "inventory").value.toString());
			Log.i("Mistake", "Health des Spielers ist: " + whiteboard.getAttribute(fsmManager.getOwnGroup(), ""+myId, "health").value);
		}
	}


	@Override
	public void onFullWhiteboardUpdate(List<WhiteboardUpdateObject> wb) {

		for(WhiteboardUpdateObject wuo: wb){
			String logKeys = "";
			for(String s : wuo.getKeys()){
				logKeys += ","+s;
			}
			Log.i(LOGTAG, "Ich bin ein onFullWhiteboardUpdate [keys:"+logKeys+" values:"+wuo.getValue().value.toString()+"]");
			whiteboard.setAttribute(wuo.getValue(), (String[])wuo.getKeys().toArray(new String[0]));
			
		}
		
		fsmManager.initiate();
		Log.i(LOGTAG, "Removing Dummy Text in Inventory");
		// remove dummy from inventory
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("name", "removeFromGroup");
		
		// adding param taget = inventory.dummy
		String paramString = fsmManager.getOwnGroup().get(0);
		for(int i = 1; i < fsmManager.getOwnGroup().size(); i++)
			paramString += "." + fsmManager.getOwnGroup().get(i);
		paramString += "." + myId + "." + "inventory.dummy";
		params.put("target", paramString);
		
		// adding param group = inventory
		paramString = fsmManager.getOwnGroup().get(0);
		for(int i = 1; i < fsmManager.getOwnGroup().size(); i++)
			paramString += "." + fsmManager.getOwnGroup().get(i);
		paramString += "." + myId + "." + "inventory";
		params.put("group", paramString);
		
		MdsAction action = new MdsAction(MdsActionIdent.removeFromGroup, params);
		MdsActionExecutable actionExec = actionParser.parseAction("dummyDelete", action, null, whiteboard, fsmManager.getOwnGroup(), myId, serverInterpreter);
		if (actionExec != null) {
			Log.i(LOGTAG, "Executing Action " + action.getIdent().toString());
			actionExec.execute(gui);
		}
		
	}


	@Override
	public void dropItem(MdsItem item) {
		
	}


	@Override
	public void onMinigameResult(int punkte, boolean gewonnen) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onGameResult(int points, String identifier) {
		Log.i(LOGTAG, "OnGameResult");
		boolean won = false;
		MdsState state = (MdsState)whiteboard.getAttribute(fsmManager.getOwnGroup(), ""+myId, FsmManager.CURRENT_STATE).value;
		// get all stateActions
		for(MdsAction startAction :state.getStartAction()) {
			if(actionParser.parseGameResult(whiteboard, startAction, state, points, identifier, fsmManager.getOwnGroup(), ""+myId)) {
				won = true;
				break;
			}
		}
		// do actions, if not already won
		if (!won && state.getDoAction() != null) {
			won = actionParser.parseGameResult(whiteboard, state.getDoAction(), state, points, identifier, fsmManager.getOwnGroup(), ""+myId);
		}
		// end result, if not already won
		if (!won && state.getEndAction() != null) {
			won = actionParser.parseGameResult(whiteboard, state.getEndAction(), state, points, identifier, fsmManager.getOwnGroup(), ""+myId);
		}
		String text;
		if (won) {
			text = "Super du hast das Spiel gewonnen.";
			Log.i("Mistake", "Der Spieler hat gewonnen: " + points);
		}
		else {
			text = "Du hast das Spiel leider verloren";
			Log.i("Mistake", "Der Spieler hat verloren: " + points);
		}
		List<String> buttons = new Vector<String>();
		buttons.add("back");
		MdsActionExecutable action = new MdsTextAction("showText", text, buttons);
		if (action != null)
			action.execute(gui);
		//fsmManager.checkWBCondition();
	}


}
