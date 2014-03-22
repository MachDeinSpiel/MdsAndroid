package de.hsbremen.mds.interpreter;

import java.util.List;
import java.util.Vector;

import android.location.Location;
import de.hsbremen.mds.android.whiteboard.Whiteboard;
import de.hsbremen.mds.common.interfaces.FsmInterface;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsEvent;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsTransition;
import de.hsbremen.mds.exceptions.NoStartStateException;

/**
 * @author JW
 */
public class FsmManager implements FsmInterface{
	private List<MdsState> states;
	private Location pos;
	// TODO wofür ist denn das?
	private List<FsmInterface> listeners = new Vector<FsmInterface>();
	private EventParser eParser = new EventParser();
	private MdsState currentState;
	private Whiteboard wb;
	
	
	public FsmManager(List<MdsState> states, EventParser eParser, Whiteboard wb){
		this.states = states;
		try{
			this.currentState = this.getFirstState();
		} catch (NoStartStateException e){
			e.printStackTrace();
		}
		this.eParser = eParser;
		this.wb = wb;
	}
		
	public void addLister(FsmInterface toAdd){
		this.listeners.add(toAdd);
	}
	
	/**
	 * Ersten State raussuchen und zurückgeben / wenn nicht vorhanden: exception
	 * @return
	 */
	private MdsState getFirstState() throws NoStartStateException{
		for(MdsState state: this.states){
			if(state.isStartState() && state.getParentState() == null){
				return state;
			}
		}
		/*
		 * Wenn null returned wird muss noch eine exception geworfen werden
		 * eg. FsmNoStatestateExcepption
		 */
		throw new NoStartStateException();
	}
	
	/**
	 * hier läuft die finit state maschine
	 */
	private MdsState checkEvent(MdsEvent e){
		
		//TODO: die transitionen des Currentstate mit dem Eventparser auf erfüllung (.)(.) prüfen dann in den nächsten state wechseln und returnen 
		MdsTransition trans = eParser.checkEvents(wb.itemList.items, pos, currentState.getTransitions());
		return null;
	}
	
	
	/**
	 * Benachrichtigt alle Listerns 
	 * 
	 * @param next der nächste state
	 * @param current der alte state
	 * @param e das event
	 */
	private void notifyListeners(MdsState next, MdsState current, MdsEvent e){
		for(FsmInterface f:this.listeners){
			f.onStateChange(next,current,e);
		}
	}


	
	public void onEvent(MdsEvent e) {
		// TODO checkevent aufrufen 
		
	}

	@Override
	public void onStateChange(MdsState next, MdsState current, MdsEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void addStateChangedListener(Interpreter interpreter) {
		// TODO Auto-generated method stub
		
	}

	public void checkEvents() {
		// TODO Auto-generated method stub
		
	}


}
