package de.hsbremen.mds.interpreter;

import java.util.List;
import java.util.Vector;

import de.hsbremen.mds.common.interfaces.FsmInterface;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
/**
 * @author JW
 */
public class FsmManager {
	private List<MdsState> states;
	private List<FsmInterface> listeners = new Vector<FsmInterface>();
	
	public FsmManager(List<MdsState> states){
		this.states = states;
		this.fsm();
	}
	/**
	 * 
	 * @param toAdd Der hinzuzufügende Listener
	 */
	public void addFsmListener(FsmInterface toAdd){
		this.listeners.add(toAdd);
	}
	
	
	private MdsState getFirstState(){
		for(MdsState state: this.states){
			if(state.isStartState() && state.getParentState() == null){
				return state;
			}
		}
		/*
		 * Wenn null returned wird muss noch eine exception geworfen werden
		 * eg. FsmNoStatestateExcepption
		 */
		return null;
	}
	
	private void fsm(){
		this.getFirstState().getStartAction();
	}
	
	
	
	private void changeState(){
		
	}
}
