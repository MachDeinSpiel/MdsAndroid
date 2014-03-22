package de.hsbremen.mds.interpreter;

import java.io.File;
import java.util.List;

import de.hsbremen.mds.android.MainActivity;
import de.hsbremen.mds.common.interfaces.FsmInterface;
import de.hsbremen.mds.common.interfaces.InterpreterInterface;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsAction;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsItem;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsObjectContainer;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.parser.Parser;
/**
 * @author JW
 */
public class Interpreter implements InterpreterInterface{
	private List<MdsAction> actions;
	private List<MdsState> states;
	private List<MdsItem> items;
	
	private FsmInterface fsmmgr;
		
	public Interpreter(File json, MainActivity android){
		new Parser(this,json);
	}

	@Override
	public void pushParsedObjects(MdsObjectContainer objectContainer) {
		// TODO Objekte speichern
		actions = objectContainer.getActions();
		states = objectContainer.getStates();
		items = objectContainer.getItems();
		
		this.onDataSet();
	}
	
	private void onDataSet(){
		/*
		 * Erstellen des FsmManagers
		 */
		fsmmgr = new FsmManager(this.states);
		
	}
}
