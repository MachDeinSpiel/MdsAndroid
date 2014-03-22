package de.hsbremen.mds.interpreter;

import java.util.List;
import java.util.Vector;

import de.hsbremen.mds.common.interfaces.FsmInterface;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsAction;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsEvent;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.interpreter.exceptions.NoStartStateExcetpion;

/**
 * @author JW
 */
public class FsmManager implements FsmInterface{
	private List<MdsState> states;
	private List<FsmInterface> listeners = new Vector<FsmInterface>();
	private EventParser = new EventParser();
	private MdsState currentState;
	
	
	public FsmManager(List<MdsState> states){
		this.states = states;
		try{
			this.currentState = this.getFirstState();
		} catch (NoStartStateExcetpion e){
			e.printStackTrace();
		}
		
	}

	
	/**
	 * Ersten State raussuchen und zurückgeben / wenn nicht vorhanden: exception
	 * @return
	 */
	private MdsState getFirstState() throws NoStartStateExcetpion{
		for(MdsState state: this.states){
			if(state.isStartState() && state.getParentState() == null){
				return state;
			}
		}
		/*
		 * Wenn null returned wird muss noch eine exception geworfen werden
		 * eg. FsmNoStatestateExcepption
		 */
		throw new NoStartStateExcetpion();
	}
	
	/**
	 * hier läuft die finit state maschine
	 */
	private void fsm(){
		
	}
	
	
	
	private void changeState(){
		
	}


	@Override
	public void onEvent(MdsEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public MdsAction getAction() {
		// TODO Auto-generated method stub
		
		return null;
	}
}
