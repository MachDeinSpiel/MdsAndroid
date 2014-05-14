
package de.hsbremen.mds.interpreter;

import java.util.List;


import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsTransition;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsActionExecutable;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.exceptions.NoStartStateException;

/**
 * @author JGWNH
 */
public class FsmManager {
	private List<MdsState> states;
	private int myID;
	private Whiteboard wb;
	private Interpreter interpreter; //TODO: interface
	
	public FsmManager(List<MdsState> states, Whiteboard wb, Interpreter interpreter){
		this.states = states;
		this.interpreter = interpreter;
		try{
			this.setState(this.getFirstState(),"currentState");
		} catch (NoStartStateException e){
			e.printStackTrace();
		}
		this.wb = wb;
	}
	
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
			wb.setAttributeValue(current, "players",Integer.toString(myID),setTo);
			this.onstateChanged(current);
		} else {
			/*
			 * TODO fehler auffangen
			 */
		}
	}


	private void onstateChanged(MdsState state) {
		
	
		interpreter.onStateChange();
		
	}

	
	
	/**
	 * Ersten State raussuchen und zur�ckgeben / wenn nicht vorhanden: exception
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
	 * hier l�uft die finit state maschine
	 */
	public void checkEvents(String buttonName){
		//TODO buttonName irgendwo anders herbekommen
		
		for(MdsTransition t : this.getCurrentState().getTransitions()){
			EventParser.Result result;
			
			switch(t.getEventType()){
			case locationEvent:
				result = EventParser.checkLocationEvent(t.getCondition(), wb, myID);
				break;
			case uiEvent:
				result = EventParser.checkUiEvent(buttonName, t.getCondition(), wb, myID);
				break;
			case whiteboardEvent:
				result = EventParser.checkWhiteboardEvent(t.getCondition(), wb, myID);
				break;
			default:
				//TODO: Fehler abfangen
				result = new EventParser.Result(false, null, null);
				break;
			}
				
			if(result.isfullfilled){
				

				// TODO: Evtl keine Liste sondern ein Whiteboard eintragen
				if(result.subjects != null)
					this.getCurrentState().setSubjects(result.subjects);
				if(result.objects != null)
					this.getCurrentState().setObjects(result.objects);
				this.setState(getCurrentState(), "lastState");
				this.setState(t.getTarget(), "currentState");

				return;
			}
		}
			
		

	}


	

}
