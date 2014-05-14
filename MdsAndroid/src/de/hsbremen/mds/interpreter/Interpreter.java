package de.hsbremen.mds.interpreter;

import java.io.File;
import java.util.List;
import java.util.Vector;

import android.util.Log;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.common.interfaces.AndroidListener;
import de.hsbremen.mds.common.interfaces.ClientInterpreterInterface;
import de.hsbremen.mds.common.interfaces.FsmInterface;
import de.hsbremen.mds.common.interfaces.GuiInterface;
import de.hsbremen.mds.common.interfaces.InterpreterInterface;
import de.hsbremen.mds.common.interfaces.ServerInterpreterInterface;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsObjectContainer;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsActionExecutable;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.parser.Parser;

/**
 * @author JW
 */
public class Interpreter implements InterpreterInterface, AndroidListener, ClientInterpreterInterface, FsmInterface{
	private ActionParser actionParser;
	private FsmManager fsmManager;
	private Whiteboard whiteboard;
	private int myId;
	private GuiInterface gui;
	private ServerInterpreterInterface serverInterpreter;

		
	public Interpreter(File json, GuiInterface guiInterface, ServerInterpreterInterface serverInterpreter, int playerId){
		this.gui = guiInterface;
		this.serverInterpreter = serverInterpreter;

		this.myId = playerId;
		new Parser(this,json);	
		
	}

	
	@Override
	public void pushParsedObjects(MdsObjectContainer objectContainer) {		
		Log.d("Interpreter", "Geparste Objekte vo Parser bekommen");
		this.gui.setAndroidListener(this, 5);
		this.fsmManager = new FsmManager(objectContainer.getStates(),this.whiteboard, this);
	}
	


	@Override
	public void onButtonClick(String buttonName) {
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
		whiteboard.setAttributeValue(longitude, "players", Integer.toString(myId), "longitude");
		List<String> keys = new Vector<String>();
		keys.add("players");
		keys.add(""+myId);
		keys.add("longitude");
		serverInterpreter.onWhiteboardUpdate(keys, whiteboard.getAttribute("players", Integer.toString(myId), "longitude"));
		
		whiteboard.setAttributeValue(latitude, "players", Integer.toString(myId), "latitude");
		keys.remove(keys.size()-1);
		keys.add("latitude");
		serverInterpreter.onWhiteboardUpdate(keys, whiteboard.getAttribute("players", Integer.toString(myId), "latitude"));
		
		fsmManager.checkEvents(null);
		
	}

	@Override
	public void onWhiteboardUpdate(List<String> keys, WhiteboardEntry value) {
		whiteboard.setAttributeValue(value, (String[])keys.toArray());
		
		fsmManager.checkEvents(null);
	}

	@Override
	public void onFullWhiteboardUpdate(Whiteboard newWhiteboard) {
		this.whiteboard = newWhiteboard;
	}

	@Override
	public void onStateChange() {
		MdsActionExecutable endAction = actionParser.parseAction(((MdsState) (whiteboard.getAttribute("players",myId+"","lastState").value)).getEndAction(), ((MdsState) (whiteboard.getAttribute("players",myId+"","lastState").value)), whiteboard, myId);
		MdsActionExecutable startAction = actionParser.parseAction(((MdsState) (whiteboard.getAttribute("players",myId+"","currentState").value)).getStartAction(), ((MdsState) (whiteboard.getAttribute("players",myId+"","currentState").value)), whiteboard, myId);
		MdsActionExecutable doAction = actionParser.parseAction(((MdsState) (whiteboard.getAttribute("players",myId+"","currentState").value)).getDoAction(), ((MdsState) (whiteboard.getAttribute("players",myId+"","currentState").value)), whiteboard, myId);
		
		endAction.execute(gui);
		startAction.execute(gui);
		doAction.execute(gui);
	}


	@Override
	public void updateLocalWhiteboard(List<String> keys, WhiteboardEntry entry) {
		whiteboard.setAttributeValue(entry, (String[])keys.toArray());
		
		
	}


	@Override
	public void useItem(MdsItem item) {
		// TODO Auto-generated method stub
		
	}


}
