
package de.hsbremen.mds.interpreter;

import java.util.List;

import android.util.Log;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsCondition;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsTransition;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsTransition.EventType;
import de.hsbremen.mds.common.whiteboard.InvalidWhiteboardEntryException;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.exceptions.NoStartStateException;
import de.hsbremen.mds.interpreter.EventParser.Result;

/**
 * @author JGWNH
 */
public class FsmManager {
	
	public static final String CURRENT_STATE = "currentState";
	public static final String LAST_STATE = "lastState";
	
	private List<MdsState> states;
	private String myID;
	private Whiteboard wb;
	private Interpreter interpreter;
	private boolean isRunning = false;
	private List<String> ownGroup;
	
	public FsmManager(List<MdsState> states, Whiteboard wb, Interpreter interpreter, String id){
		this.states = states;
		this.interpreter = interpreter;
		this.wb = wb;
		this.myID = id;
		MdsState last = states.get(states.size() -1);
//		Log.i("Mistake", "Letzter State: " + last.getName() + " start Action: " + (last.getStartAction() == null ? "Keine Start Action" : last.getStartAction().getIdent()));
	}
	
	/**
	 * Legt den Startzustand fest und startet den Automaten
	 */
	public void initiate(){
		Log.i("Mistake", "Im Initiate des FsmManagers");
		if(!isRunning){
			Log.i("Mistake", "FsmManger is running");
			try{
				// set own group
				ownGroup = wb.getGroupString(myID);
				wb.setAttribute(new WhiteboardEntry("currentSt","all"), ownGroup, ""+myID, CURRENT_STATE);
				wb.setAttribute(new WhiteboardEntry("lastSt","all"), ownGroup,""+myID,LAST_STATE);
				//FIXME: fix my shit up
				wb.getAttribute(ownGroup, myID, LAST_STATE).value = new MdsState(-1, "", null , null, null, null, false, false);

				Log.i("Mistake", "Gruppe des Spielers: " + ownGroup.get(0));
				
			}catch(InvalidWhiteboardEntryException e){
				e.printStackTrace();
			}
			
			try{
				this.setState(this.getFirstState(),CURRENT_STATE);
			} catch (NoStartStateException e){
				Log.e(Interpreter.LOGTAG, "Error: No start-state found!");
			}
			isRunning = true;
		}
	}
	
	/**
	 * den Aktuellen State aus dem Whiteboard holen
	 * @return
	 */
	public MdsState getCurrentState(){
		if (ownGroup.size() > 1)
			return (MdsState)this.wb.getAttribute(ownGroup.get(0), ownGroup.get(1), myID, CURRENT_STATE).value;
		else
			return (MdsState)this.wb.getAttribute(ownGroup.get(0), myID, CURRENT_STATE).value;
	}

	/**
	 * Um einen State in das Whiteboard schreiben
	 * @return
	 */
	private void setState(MdsState current, String setTo){
		Log.i(Interpreter.LOGTAG, "FsmManager: setState("+current.getName()+", "+setTo+")");
		if(setTo.equals(CURRENT_STATE) || setTo.equals(LAST_STATE)){
			if(wb == null){
				Log.e(Interpreter.LOGTAG, "Error: Whiteboard is null while changing state");
			}
			
			wb.getAttribute(ownGroup, ""+myID, setTo).value = current;
		
			this.onstateChanged(current, setTo);
		} else {
			Log.e(Interpreter.LOGTAG, "Error: setTo doesn't equal 'currentState' or 'lastState'");
		}
	}

	private void onstateChanged(MdsState state, String setTo) {
		
		Log.i(Interpreter.LOGTAG, "onstateChanged im FsmManager ausgeführt " + state.getName());
		interpreter.onStateChange(setTo);
		
	}

	
	
	/**
	 * Ersten State raussuchen und zurückgeben / wenn nicht vorhanden: exception
	 * @return
	 */
	private MdsState getFirstState() throws NoStartStateException{
		Log.i(Interpreter.LOGTAG, "Suche nach StartZustand...");
		for(MdsState state: this.states){
			if(state.isStartState() && state.getParentState() == null){
				Log.i(Interpreter.LOGTAG, "StartZustand gefunden:"+state.getName());
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
	public void checkEvents(String buttonName){
		
		for(MdsTransition t : this.getCurrentState().getTransitions()){
			boolean result;
			boolean result2;
			Log.i(Interpreter.LOGTAG, "Checking Events on state " + this.getCurrentState().getName());
			Log.i(Interpreter.LOGTAG, "Checking Events on Transition mit Target " + t.getTarget().getName() + " Eventtype ist "+ t.getEventType().toString());
			
			switch(t.getEventType()){
			case locationEvent:
				result = EventParser.checkLocationEvent(t.getCondition()[0], wb, ownGroup, myID);
				break;
			case uiEvent:
				Log.i("Mistake", "Buttonname des Ui-Events" + buttonName);
				result = EventParser.checkUiEvent(buttonName, t.getCondition()[0], wb);
				break;
			case whiteboardEvent:
				result = EventParser.checkWhiteboardEvent(t.getCondition()[0], wb, ownGroup, myID);
				break;
			case uiLocationEvent:
				result2 = EventParser.checkUiEvent(buttonName, t.getCondition()[0], wb);
				result = EventParser.checkLocationEvent(t.getCondition()[1], wb, ownGroup, myID);
				// if both results are fullfilled set true
				result = (result && result2) ? true : false;
				// only location events can give objects back
				break;
			case locationWhiteboardEvent:
				result2 = EventParser.checkLocationEvent(t.getCondition()[0], wb, ownGroup, myID);
				result = EventParser.checkWhiteboardEvent(t.getCondition()[1], wb, ownGroup, myID);
				// if both results are fullfilled set true
				result = (result && result2) ? true : false;
				break;
			case uiWhiteboardEvent:
				result2 = EventParser.checkUiEvent(buttonName, t.getCondition()[0], wb);
				result = EventParser.checkWhiteboardEvent(t.getCondition()[1], wb, ownGroup, myID);
				// if both results are fullfilled set true
				result = (result && result2) ? true : false;
				break;
			case multiplewhiteboardEvent:
				result = true;
				// go through all conditions and set res to false if one is false
				for(MdsCondition cond : t.getCondition()) {
					if (!EventParser.checkWhiteboardEvent(cond, wb, ownGroup, myID)) {
						result = false;
						break;
					}
				}
				break;
			case locationMultiplewhiteboardEvent:
				Log.i(Interpreter.LOGTAG, "Checking locationMultiplewhiteboardEvent");
				result = true;
				
				//first one is a location event
				if (!EventParser.checkLocationEvent(t.getCondition()[0], wb, ownGroup, myID)) {
					result = false;
					break;
				}
				// go through all conditions and set res to false if one is false
				Log.i("Mistake", "Checking Conditions with Size " + t.getCondition().length);
				for(int i = 1; i < t.getCondition().length; i++) {
					Log.i("Mistake", "Checking whiteboardEvent number " + (i));
					if (!EventParser.checkWhiteboardEvent(t.getCondition()[i], wb, ownGroup, myID)) {
						result = false;
						break;
					}
				}
				break;
			default:
				Log.e(Interpreter.LOGTAG, "EventType could not be resolved in CheckEvents");
				result = false;
				break;
			}
				
			if(result){
				Log.i(Interpreter.LOGTAG, "Result is true, changing state");
				this.setState(getCurrentState(), LAST_STATE);
				this.setState(t.getTarget(), CURRENT_STATE);

				return;
			}
		}
			
		

	}
	
	/**
	 * Check WB Conditions only for game end etc.
	 * Changes state, if fullfilled
	 */
	public void checkWBCondition() {
		// check WB Cond in state
		MdsState state = (MdsState) wb.getAttribute(ownGroup ,myID+"","currentState").value;
		Log.i(Interpreter.LOGTAG, "checking Events again on " + state.getName());
		MdsTransition[] trans = state.getTransitions();
		// Only start if trans is not null
		if(trans != null) {
			Log.i(Interpreter.LOGTAG, "Found transition: " + trans.length);
			for (int i = 0; i < trans.length; i++) {
				if (trans[i].getEventType() == EventType.whiteboardEvent) {
					Log.i(Interpreter.LOGTAG, "WhiteboardEvent");
					boolean res = EventParser.checkWhiteboardEvent(trans[i].getCondition()[0], wb, ownGroup, myID);
					if(res){
						Log.i(Interpreter.LOGTAG, "Event is fullfilled");

						this.setState(getCurrentState(), LAST_STATE);
						this.setState(trans[i].getTarget(), CURRENT_STATE);

						return;
					}
				}
			}
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public List<String> getOwnGroup() {
		return ownGroup;
	}

	public void setOwnGroup(List<String> ownGroup) {
		this.ownGroup = ownGroup;
	}

}
