
package de.hsbremen.mds.interpreter;

import java.util.List;
import java.util.Vector;

import android.location.Location;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.interfaces.FsmInterface;
import de.hsbremen.mds.common.interfaces.InterpreterInterface;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsEvent;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsTransition;
import de.hsbremen.mds.exceptions.NoStartStateException;

/**
 * @author JGWNH
 */
public class FsmManager {
	private List<MdsState> states;
	private List<FsmInterface> listeners = new Vector<FsmInterface>();
	private int myID;
	private Whiteboard wb;
	/**
	 * den Aktuellen State aus dem Whiteboard holen
	 * @return
	 */
	private MdsState getCurrentState(){
		return (MdsState)this.wb.getAttribute("player" ,Integer.toString(myID), "currentState").value;
	}
	
	/**
	 * Um einen State in das Whiteboard schreiben
	 * @return
	 */
	private void setState(MdsState current, String setTo){
		if(setTo.equals("currentState") || setTo.equals("lastState")){
			wb.setAttributeValue(current, "player",Integer.toString(myID),setTo);
			this.notifyListeners();
		} else {
			/*
			 * TODO fehler auffangen
			 */
		}
	}
	
	
	public FsmManager(List<MdsState> states, Whiteboard wb){
		this.states = states;
		try{
			this.setState(this.getFirstState(),"currentState");
		} catch (NoStartStateException e){
			e.printStackTrace();
		}
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
		 * eg. FsmNoStartStateExcepption
		 */
		throw new NoStartStateException();
	}
	
	/**
	 * hier läuft die finit state maschine
	 */
	public void checkEvents(MdsEvent complied){
		for(MdsTransition t : this.getCurrentState().getTransitions()){
			if(EventParser.checkEvent(t.getEvent(), complied, this.wb, this.myID)){
				this.setState(t.getTarget(), "currentState");
			}
		}
	}
	
	
	/**
	 * Benachrichtigt alle Listener
	 */
	private void notifyListeners(){
		for(FsmInterface f:this.listeners){
			f.onStateChange();
		}
	}


	

}
