
package de.hsbremen.mds.interpreter;

import java.util.List;




import android.util.Log;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsTransition;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsActionExecutable;
import de.hsbremen.mds.common.whiteboard.InvalidWhiteboardEntryException;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.exceptions.NoStartStateException;

/**
 * @author JGWNH
 */
public class FsmManager {
	
	public static final String CURRENT_STATE = "currentState";
	public static final String LAST_STATE = "lastState";
	
	private List<MdsState> states;
	private String myID;
	private Whiteboard wb;
	private Interpreter interpreter; //TODO: interface
	private boolean isRunning = false;
	
	public FsmManager(List<MdsState> states, Whiteboard wb, Interpreter interpreter, String id){
		this.states = states;
		this.interpreter = interpreter;
		this.wb = wb;
		this.myID = id;
	}
	
	/**
	 * Legt den Startzustand fest und startet den Automaten
	 */
	public void initiate(){
		if(!isRunning){
			try{
				wb.setAttribute(new WhiteboardEntry("currentSt","all"), Interpreter.WB_PLAYERS,""+myID,CURRENT_STATE);
				wb.setAttribute(new WhiteboardEntry("lastSt","all"), Interpreter.WB_PLAYERS,""+myID,LAST_STATE);
				//TODO: fix my shit up
				wb.getAttribute(Interpreter.WB_PLAYERS,myID,LAST_STATE).value = new MdsState(-1, "", null, null, false, false);
				
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
	private MdsState getCurrentState(){
		return (MdsState)this.wb.getAttribute(Interpreter.WB_PLAYERS ,myID, CURRENT_STATE).value;
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
			
			//TODO: Sneaky methode zum setzen des States enternen, WhiteboardEntry akzeptiert ja eigentlich
			//nur Strings und Whiteboards, hier wird (falls noch nicht da) erst ein leerer string gesetzt
			//und dann mit einem MdsState �berschrieben (muhahahah *evil face*)
			
//			if(wb.getAttribute(Interpreter.WB_PLAYERS,myID,setTo) == null){
//				try {
//					wb.setAttribute(new WhiteboardEntry("", "all"), Interpreter.WB_PLAYERS,myID,setTo);
//				} catch (InvalidWhiteboardEntryException e) {
//					e.printStackTrace();
//				}
//			}
			wb.getAttribute(Interpreter.WB_PLAYERS,myID,setTo).value = current;
			
		
			this.onstateChanged(current, setTo);
		} else {
			/*
			 * TODO fehler auffangen
			 */
			Log.e(Interpreter.LOGTAG, "Error: setTo doesn't equal 'currentState' or 'lastState'");
		}
	}


	private void onstateChanged(MdsState state, String setTo) {
		
		Log.i(Interpreter.LOGTAG, "onstateChanged im FsmManager ausgef�hrt " + state.getName());
		interpreter.onStateChange(setTo);
		
	}

	
	
	/**
	 * Ersten State raussuchen und zur�ckgeben / wenn nicht vorhanden: exception
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
				this.setState(getCurrentState(), LAST_STATE);
				this.setState(t.getTarget(), CURRENT_STATE);

				return;
			}
		}
			
		

	}

	public boolean isRunning() {
		return isRunning;
	}


	

}
