package de.hsbremen.mds.interpreter;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import android.util.Log;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.common.interfaces.ClientInterpreterInterface;
import de.hsbremen.mds.common.interfaces.FsmInterface;
import de.hsbremen.mds.common.interfaces.GuiInterface;
import de.hsbremen.mds.common.interfaces.InterpreterInterface;
import de.hsbremen.mds.common.interfaces.ServerInterpreterInterface;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsCondition;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsObjectContainer;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsTransition;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsTransition.EventType;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsActionExecutable;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsAction.MdsActionIdent;
import de.hsbremen.mds.common.whiteboard.InvalidWhiteboardEntryException;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.common.whiteboard.WhiteboardUpdateObject;
import de.hsbremen.mds.interpreter.EventParser.Result;
import de.hsbremen.mds.parser.Parser;

/**
 * @author JW
 */
public class Interpreter implements InterpreterInterface, ClientInterpreterInterface, FsmInterface{
	
	public static final String LOGTAG = "InterpreterClient";
	public static final String WB_PLAYERS = "Players";
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
//							Log.i("Mistake", "Condition: " + trans[j].getCondition().getName());
//							if (trans[j].getCondition().getParams().get("object") != null)
//								Log.i("Mistake", "Object der Condition: " + trans[j].getCondition().getParams().get("object").toString());
//							if (trans[j].getCondition().getParams().get("subject") != null)
//								Log.i("Mistake", "Subject der Condition: " + trans[j].getCondition().getParams().get("subject").toString());
//						}	
//					}
//				}
//			}
//		}
		this.fsmManager = new FsmManager(objectContainer.getStates(),this.whiteboard, this, myId);
	}
	


	@Override
	public void onButtonClick(String buttonName) {
		Log.i(LOGTAG, "onButtonClick ausgef�hrt" + buttonName);
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
			//FSM l�uft noch nicht? Raus hier!
			return;
		}
		Log.i(LOGTAG, "Neue Position von Android bekommen : [long:"+longitude+" ,|lat:"+latitude+"]");
		try {
			whiteboard.setAttributeValue(Double.toString(longitude), WB_PLAYERS, myId, "longitude");
		} catch (InvalidWhiteboardEntryException e) {
			e.printStackTrace();
		}
		List<String> keys = new Vector<String>();
		keys.add(WB_PLAYERS);
		keys.add(""+myId);
		keys.add("longitude");
		serverInterpreter.onWhiteboardUpdate(keys, whiteboard.getAttribute(WB_PLAYERS, myId, "longitude"));
		
		try {
			whiteboard.setAttributeValue(Double.toString(latitude), WB_PLAYERS, myId, "latitude");
		} catch (InvalidWhiteboardEntryException e) {
			e.printStackTrace();
		}
		keys.remove(keys.size()-1);
		keys.add("latitude");
		serverInterpreter.onWhiteboardUpdate(keys, whiteboard.getAttribute(WB_PLAYERS, myId, "latitude"));
		
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
		if(setTo.equals(CURRENT_STATE)) {
			Log.i(LOGTAG, "Zustand ge�ndert");
			Log.i(LOGTAG, "Last State" + ((MdsState) (whiteboard.getAttribute(WB_PLAYERS,myId+"","lastState").value)).getName());
			MdsState current = (MdsState) whiteboard.getAttribute(WB_PLAYERS,myId+"","currentState").value;
			Log.i(LOGTAG, "Current State" + current.getName());
			// Actions des Current states
			
			if (current.getDoAction() != null) Log.i(LOGTAG, "Do Action des States: " + current.getDoAction().getIdent());
			if (current.getStartAction() != null) Log.i(LOGTAG, "Start Action des States: " + current.getStartAction().getIdent());
			if (current.getEndAction() != null) Log.i(LOGTAG, "End Action des States: " + current.getEndAction().getIdent());
			
			MdsActionExecutable endAction = actionParser.parseAction("end", ((MdsState) (whiteboard.getAttribute(WB_PLAYERS,myId+"","lastState").value)).getEndAction(), ((MdsState) (whiteboard.getAttribute(WB_PLAYERS,myId+"","lastState").value)), whiteboard, myId, serverInterpreter);
			MdsActionExecutable startAction = actionParser.parseAction("start", ((MdsState) (whiteboard.getAttribute(WB_PLAYERS,myId+"","currentState").value)).getStartAction(), ((MdsState) (whiteboard.getAttribute(WB_PLAYERS,myId+"",FsmManager.LAST_STATE).value)), whiteboard, myId, serverInterpreter);
			MdsActionExecutable doAction = actionParser.parseAction("do", ((MdsState) (whiteboard.getAttribute(WB_PLAYERS,myId+"","currentState").value)).getDoAction(), ((MdsState) (whiteboard.getAttribute(WB_PLAYERS,myId+"",FsmManager.LAST_STATE).value)), whiteboard, myId, serverInterpreter);
			
			Log.i(LOGTAG, "Executing Action");
			
			if(endAction != null){
				endAction.execute(gui);
			}
			if(startAction != null){
				startAction.execute(gui);
			}
			if(doAction != null){
				doAction.execute(gui);
			}
			fsmManager.checkWBCondition();
			
			Log.i(LOGTAG, "Health des Spielers: " + whiteboard.getAttribute(WB_PLAYERS, myId+"","health").value);
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
			try{
				Whiteboard group = (Whiteboard)whiteboard.getAttribute(keys.toArray(new String[0])).value;
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
	public void useItem(MdsItem item) {
		// TODO Auto-generated method stub
		
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
		WhiteboardEntry player = whiteboard.getAttribute("Players", myId);
		Log.i("Mistake", "Player Health von " + myId + " beim erzeugen ist" + ((Whiteboard)player.value).get("health").value);	
		fsmManager.initiate();
		
	}


	@Override
	public void dropItem(MdsItem item) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onMinigameResult(int punkte, boolean gewonnen) {
		// TODO Auto-generated method stub
		
	}


}
