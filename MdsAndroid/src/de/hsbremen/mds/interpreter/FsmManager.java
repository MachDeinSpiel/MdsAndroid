package de.hsbremen.mds.interpreter;

import java.util.List;
import java.util.Vector;

import de.hsbremen.mds.common.interfaces.FsmInterface;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsAction;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsEvent;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.exceptions.NoStartStateException;

/**
 * @author JW
 */
public class FsmManager{
	private List<MdsState> states;
	private List<FsmInterface> listeners = new Vector<FsmInterface>();
	private EventParser = new EventParser();
	private MdsState currentState;
	
	
	public FsmManager(List<MdsState> states){
		this.states = states;
		try{
			this.currentState = this.getFirstState();
		} catch (NoStartStateException e){
			e.printStackTrace();
		}
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


}
