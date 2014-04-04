package de.hsbremen.mds.interpreter;

import java.io.File;
import java.util.List;
import java.util.Vector;

import android.util.Log;
import de.hsbremen.mds.common.interfaces.ClientInterpreterInterface;
import de.hsbremen.mds.common.interfaces.GuiInterface;
import de.hsbremen.mds.common.interfaces.InterpreterInterface;
import de.hsbremen.mds.common.interfaces.ServerInterpreterInterface;
import de.hsbremen.mds.common.listener.AndroidListener;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsObjectContainer;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.parser.Parser;
/**
 * @author JW
 */
public class Interpreter implements InterpreterInterface, AndroidListener, ClientInterpreterInterface{
	private ActionParser actionParser;
	
	private Whiteboard whiteboard;
	private int myId;
	private GuiInterface gui;
	private ServerInterpreterInterface serverInterpreter;
		
	public Interpreter(File json, GuiInterface guiInterface, int playerId){
		this.gui = guiInterface;
		//TODO: getData 
		this.myId = playerId;
		new Parser(this,json);	
	}

	private void setPlayerAttributeValue(int playerId, String key, Object value){
		((Whiteboard)((Whiteboard) whiteboard.get("players").value).get(playerId).value).get(key).value = value;
	}
	
	private WhiteboardEntry getPlayerAttribute(int playerId, String key){
		return ((Whiteboard)((Whiteboard) whiteboard.get("players").value).get(playerId).value).get(key);
	}
	
	@Override
	public void pushParsedObjects(MdsObjectContainer objectContainer) {		
		Log.d("Interpreter", "Geparste Objekte vo Parser bekommen");
		this.gui.setAndroidListener(this, 5);
		
		this.onDataSet();
	}
	
	private void onDataSet(){
		//Starten
		Log.d("Interpreter", "Let fsmManager checking Events...");
		
	}

	@Override
	public void onButtonClick(String buttonName) {
		// TODO Auto-generated method stub
		
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
		setPlayerAttributeValue(myId, "longitude", longitude);
		List<String> keys = new Vector<String>();
		keys.add("players");
		keys.add(""+myId);
		keys.add("longitude");
		serverInterpreter.onWhiteboardUpdate(keys, new WhiteboardEntry(longitude, getPlayerAttribute(myId, "longitude").visibility));
		
		setPlayerAttributeValue(myId, "latitude", latitude);
		keys.remove(keys.size()-1);
		keys.add("latitude");
		serverInterpreter.onWhiteboardUpdate(keys, new WhiteboardEntry(latitude, getPlayerAttribute(myId, "latitude").visibility));
		
		
	}

	@Override
	public void onWhiteboardUpdate(List<String> keys, WhiteboardEntry value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFullWhiteboardUpdate(Whiteboard newWhiteboard) {
		this.whiteboard = newWhiteboard;
		
	}
}
