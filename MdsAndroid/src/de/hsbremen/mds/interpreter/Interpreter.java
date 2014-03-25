package de.hsbremen.mds.interpreter;

import java.io.File;

import android.util.Log;
import de.hsbremen.mds.common.interfaces.FsmInterface;
import de.hsbremen.mds.common.interfaces.GuiInterface;
import de.hsbremen.mds.common.interfaces.InterpreterInterface;
import de.hsbremen.mds.common.listener.AndroidListener;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsEvent;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsObjectContainer;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.parser.Parser;
/**
 * @author JW
 */
public class Interpreter implements InterpreterInterface, FsmInterface, AndroidListener{
	private ActionParser actionParser;
	private EventParser eventParser;
	private FsmManager fsmManager;
	
	private Whiteboard whiteboard;
	
	private GuiInterface gui;
		
	public Interpreter(File json, GuiInterface guiInterface){
		this.gui = guiInterface;
		this.whiteboard = gui.getData();
		new Parser(this,json);	
	}

	@Override
	public void pushParsedObjects(MdsObjectContainer objectContainer) {		
		Log.d("Interpreter", "Geparste Objekte vo Parser bekommen");
		//this.actionParser = new ActionParser(objectContainer.getPlayer(), objectContainer.getExhibits(), objectContainer.getItems());
		this.eventParser = new EventParser();
		fsmManager = new FsmManager(objectContainer.getStates(),eventParser, whiteboard);
		// Listener
		fsmManager.addStateChangedListener(this);
		this.gui.setAndroidListener(this, 5);
		
		this.onDataSet();
	}
	
	private void onDataSet(){
		//Starten
		Log.d("Interpreter", "Let fsmManager checking Events...");
		fsmManager.checkEvents();
		
	}

	@Override
	public void onStateChange(MdsState next, MdsState current, MdsEvent e) {
		// TODO Actions ausführen, dann nach neuen erfüllten Transitions gucken
		
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
	public void onUserLeftGame(String deineMudda) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPositionChanged(double longitude, double laditude) {
		// TODO Auto-generated method stub
		
	}
}
