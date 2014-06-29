package de.hsbremen.mds.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.util.Log;
import de.hsbremen.mds.common.interfaces.InterpreterInterface;
import de.hsbremen.mds.common.valueobjects.GameResult;
import de.hsbremen.mds.common.valueobjects.MdsObject;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsCondition;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsObjectContainer;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsQuantifier;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsTransition;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsAction;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsAction.MdsActionIdent;

public class Parser {
	
	private MdsAction[] allMdsActions;
	private MdsState[] allMdsStates;

	public Parser(InterpreterInterface interpreter, File jsonFile){
		//TODO: Interpreter zwischenspeichern
		//TODO: Wenn fertig geparst wurde, MdsObjectContainer an Interpreter geben
		//		(mit interpreter.pushParsedObjects(MdsObjectContainer)
		
		JSONParser parser = new JSONParser();
		 
		try {
			
			Object obj = parser.parse(new FileReader(jsonFile));
			 
			JSONObject jsonObject = (JSONObject) obj;
			
			/* ---- aus der JSON datei lesen und in Objekte speichern ---- */
			
			// read MdsActions
			Log.i("Mistake", "Parser reading actions...");
			JSONArray MdsAction = (JSONArray) jsonObject.get("action"); //lese des MdsAction arrays aus der JSON datei
			this.allMdsActions = readActions(MdsAction);
			
			// read MdsStates
			Log.i("Mistake", "Parser reading states...");
			JSONArray MdsState = (JSONArray) jsonObject.get("states");	// lesen des MdsState arrays aus der JSON datei
			this.allMdsStates = readStates(MdsState);
			
			List<MdsState> states = Arrays.asList(this.allMdsStates);
			List<MdsAction> actions = Arrays.asList(this.allMdsActions);
			
			MdsObjectContainer MdsContainer = new MdsObjectContainer(actions, states);
			
			interpreter.pushParsedObjects(MdsContainer);
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	private MdsAction[] readActions(JSONArray MdsAction) {
		MdsAction[] allMdsActions = new MdsAction[MdsAction.size()];// array in dem all unsere MdsActions gespeichert werden
		String ident;												// temp variablen
	
		
		for (int i = 0; i < MdsAction.size(); i++) {	
			// aus dem JSONArray wird ein JSONObject an der stelle "i" zwischengespeichert
			JSONObject element = (JSONObject) MdsAction.get(i);
			
			// attribute werden aus dem JSONObject gelesen
			ident = (String) element.get("ident");
			// lese des defaults arrays aus der JSON datei
			HashMap<String, Object> defaults = new HashMap<String, Object>();
			if(element.get("defaults") != null) {
				JSONObject defaultsO = (JSONObject) element.get("defaults");
				// zwischenspeicherung der KeySet
				Set<String> keySet = defaultsO.keySet();
				// die param werte aus dem KeySet werden dem params HashMap übergeben
				for (String key : keySet){
					Object value = defaultsO.get(key);
					defaults.put(key, value);
				}
			}
			else if(element.get("params") != null) {
				JSONObject paramsO = (JSONObject) element.get("params");
				// zwischenspeicherung der KeySet
				Set<String> keySet = paramsO.keySet();
				// die param werte aus dem KeySet werden dem params HashMap übergeben
				for (String key : keySet){
					Object value = paramsO.get(key);
					defaults.put(key, value.toString());
				}
			}
			// gelesene attribute werden in das allMdsActions array gespeichert
			if(ident.equals("showVideo"))
				allMdsActions[i] = new MdsAction(MdsActionIdent.showVideo, defaults);
			else if(ident.equals("showMap"))
				allMdsActions[i] = new MdsAction(MdsActionIdent.showMap, defaults);
			else if(ident.equals("showText"))
				allMdsActions[i] = new MdsAction(MdsActionIdent.showText, defaults);
			else if(ident.equals("showImage"))
				allMdsActions[i] = new MdsAction(MdsActionIdent.showImage, defaults);
			else if(ident.equals("addToGroup"))
				allMdsActions[i] = new MdsAction(MdsActionIdent.addToGroup, defaults);
			else if(ident.equals("removeFromGroup"))
				allMdsActions[i] = new MdsAction(MdsActionIdent.removeFromGroup, defaults);
			else if(ident.equals("changeAttribute"))
				allMdsActions[i] = new MdsAction(MdsActionIdent.changeAttribute, defaults);
			else if(ident.equals("useItem"))
				allMdsActions[i] = new MdsAction(MdsActionIdent.useItem, defaults);
			else if(ident.equals("dropItem"))
				allMdsActions[i] = new MdsAction(MdsActionIdent.dropItem, defaults);
			else if(ident.equals("updateMap"))
				allMdsActions[i] = new MdsAction(MdsActionIdent.updateMap, defaults);
			else if(ident.equals("startMiniApp"))
				allMdsActions[i] = new MdsAction(MdsActionIdent.startMiniApp, defaults);
			else
				Log.e("Mistake", "Action Ident " + ident + " could not be resolved");
			
		}
		return allMdsActions;
	}

	private MdsState[] readStates(JSONArray MdsState) {
		MdsState[] allMdsStates = new MdsState[MdsState.size()];	// array in dem all unsere MdsStates gespeichert werden
		
		// temp variablen
		int id;
		boolean startMdsState, finalMdsState;
		String name;
		
		for(int i = 0; i < MdsState.size(); i++) {
			// aus dem JSONArray wird ein JSONObject an der stelle "i" zwischengespeichert
			JSONObject element = (JSONObject) MdsState.get(i);
			
			// attribute werden aus dem JSONObject gelesen
			id = Integer.parseInt(element.get("ID").toString());
			name = (String) element.get("name");
			
			MdsState parentState = null;
			if(!element.get("parentState").equals("null")) {
				// TODO: was passiert wenns nicht null ist?
			}
			
			if(element.get("startState").equals(false)) 
				startMdsState = false;
			else startMdsState = true;
			
			if(element.get("finalState").equals(false)) 
				finalMdsState = false;
			else finalMdsState = true;
			
			//Verarbeitung fehlt, da diese felder leer sind
			//JSONObject startMdsActionObject = (JSONObject) element.get("startAction");
			
			MdsAction doMdsAction = null;
			if(element.get("doAction") instanceof JSONObject) {
				JSONObject doMdsActionObject = (JSONObject) element.get("doAction");
				if(doMdsActionObject.get("name") == null) System.err.println("Do-Action in " + name + " is null");
				doMdsAction = readAction(doMdsActionObject);
			}
			
			List<MdsAction> startActions = new ArrayList<MdsAction>();
			MdsAction endAction = null;
			String ident = element.get("startAction").toString();
			if(!ident.equals("null")) {
				JSONArray sAction = (JSONArray) element.get("startAction");
				for(int j = 0; j < sAction.size(); j++) {
					JSONObject startAction = (JSONObject) sAction.get(j);
					if(startAction.get("name") == null) System.err.println("Start-Action in " + name + " is null");
					startActions.add(readAction(startAction));
				}
				
			}
			
			ident = element.get("endAction").toString();
			if(!ident.equals("null")) {
				JSONObject eAction = (JSONObject) element.get("endAction");
				if(eAction.get("name") == null) System.err.println("End-Action in " + name + " is null");
				endAction = readAction(eAction);
			}
			
			allMdsStates[i] = new MdsState(id, name, parentState, doMdsAction, startActions, endAction, startMdsState, finalMdsState);
		}
		this.allMdsStates = allMdsStates;
		for(int i = 0; i < MdsState.size(); i++) {
			MdsTransition[] allTrans = null;						// array in dem all unsere transitions gespeichert werden
			
			// aus dem JSONArray wird ein JSONObject an der stelle "i" zwischengespeichert
			JSONObject element = (JSONObject) MdsState.get(i);
			
			if(!element.get("transition").equals("null")) {
				JSONArray transition = (JSONArray) element.get("transition");	// lesen des transition arrays aus der JSON datei
				allTrans = readTransitions(transition, (String)element.get("name"));
			}
			allMdsStates[i].setTransitions(allTrans); 
		}
		return allMdsStates;
	}

	private MdsAction readAction(JSONObject MdsActionObject) {
		MdsAction mdsAction = null;
		HashMap<String, Object> defaults = null;
		if(MdsActionObject.get("params") != null) {
			JSONObject defaultsO = (JSONObject) MdsActionObject.get("params");
			defaults = new HashMap<String, Object>();
			Set<String> keySet = defaultsO.keySet();
			
			// die param werte aus dem KeySet werden dem params HashMap übergeben
			for (String key : keySet){
				if(!key.equals("result")) {
					Object value = defaultsO.get(key);
					defaults.put(key, value.toString());
				}
				else {
					JSONArray results = (JSONArray) defaultsO.get(key);
					defaults.put(key, readResults(results));
				}
			}
		}
		
		for(int j = 0; j < this.allMdsActions.length;j++) {
			Log.i("Mistake", "MdsActionObject: " + MdsActionObject.get("name").toString());
			Log.i("Mistake", "ActionIdent: " + this.allMdsActions[j].getIdent().toString());
			if (MdsActionObject.get("name").toString().equals(this.allMdsActions[j].getIdent().toString())) {
					mdsAction = new MdsAction(this.allMdsActions[j].getIdent(), this.allMdsActions[j].getParams());
					if(defaults != null) {
						mdsAction.setParams(defaults);
					}
			}
		}
		return mdsAction;
	}

	private GameResult[] readResults(JSONArray results) {
		GameResult[] gameResults = new GameResult[results.size()];
		
		for(int i = 0; i < results.size(); i++) {
			JSONObject result = (JSONObject) results.get(i);
			String attribute = null;
			String setWin = null;
			String setLoose = null;
			String addResult = null;
			double factor = 1;
			int minScore = -10;
			Set<String> keySet = result.keySet();
			
			// die param werte aus dem KeySet werden dem params HashMap übergeben
			for (String key : keySet){
				if(key.equals("attribute"))
					attribute = result.get(key).toString();
				else if(key.equals("setWin"))
					setWin = result.get(key).toString();
				else if(key.equals("setLoose"))
					setLoose = result.get(key).toString();
				else if(key.equals("addResult"))
					addResult = result.get(key).toString();
				else if(key.equals("factor"))
					factor = Double.parseDouble(result.get(key).toString());
				else if(key.equals("minScore"))
					minScore = Integer.parseInt(result.get(key).toString());
			}
			gameResults[i] = new GameResult(attribute, setWin, setLoose, addResult, factor, minScore);
		}
		return gameResults;
	}

	private MdsTransition[] readTransitions(JSONArray transition, String stateName) {
		MdsTransition[] allTrans = new MdsTransition[transition.size()];	// die größe des arrays wird festgelegt
		String event, nameTransition;
		MdsState target = null;			// temp variablen
		
		for(int j = 0; j < transition.size(); j++) {
			// aus dem JSONArray wird ein JSONObject an der stelle "j" zwischengespeichert
			JSONObject transElem = (JSONObject) transition.get(j);
			HashMap<String, Object> paramsHM = new HashMap<String, Object>();
			
			JSONArray conditions = (JSONArray) transElem.get("condition");
			MdsCondition[] conditionsArray = new MdsCondition[conditions.size()];
			for(int i = 0; i < conditions.size(); i++) {
				JSONObject condition = (JSONObject) conditions.get(i);
				JSONObject params = (JSONObject) condition.get("params");
				paramsHM = readParams(params);
				
				// transition/condition
				String name = condition.get("name").toString();
				if (name == null) System.err.println("Condition name ist null");
				conditionsArray[i] = new MdsCondition(name, paramsHM);
			}
			
			// transition
			String name = transElem.get("target").toString();
			if(name != null) {
				for(int k = 0; k < this.allMdsStates.length; k++) {
					if(this.allMdsStates[k].getName().equals(name)) {
						target = this.allMdsStates[k];
					}
				}
			}
			event = transElem.get("event").toString();
			if(event.equals("locationEvent"))
				allTrans[j] = new MdsTransition(target, MdsTransition.EventType.locationEvent);
			else if(event.equals("uiEvent"))
				allTrans[j] = new MdsTransition(target, MdsTransition.EventType.uiEvent);
			else if(event.equals("whiteboardEvent"))
				allTrans[j] = new MdsTransition(target, MdsTransition.EventType.whiteboardEvent);
			else if(event.equals("uiLocationEvent"))
				allTrans[j] = new MdsTransition(target, MdsTransition.EventType.uiLocationEvent);
			else if(event.equals("locationWhiteboardEvent"))
				allTrans[j] = new MdsTransition(target, MdsTransition.EventType.locationWhiteboardEvent);
			else if(event.equals("uiWhiteboardEvent"))
				allTrans[j] = new MdsTransition(target, MdsTransition.EventType.uiWhiteboardEvent);
			else if(event.equals("multipleWhiteboardEvent"))
				allTrans[j] = new MdsTransition(target, MdsTransition.EventType.multiplewhiteboardEvent);
			else
				System.err.println("Eventtype " + event + " could not be resolved in State " + stateName);
			allTrans[j].setConditions(conditionsArray);
		}
		return allTrans;
	}

	private HashMap<String, Object> readParams(JSONObject params) {
		HashMap<String, Object> paramsHM = new HashMap<String, Object>();
		if(params.get("object") != null) {
			JSONObject object = (JSONObject) params.get("object");
			JSONObject quantifier = (JSONObject) object.get("quantifier");
			
			// transition/condition/params/object/quantifier
			String checkType = quantifier.get("checkType").toString();
			String value = quantifier.get("value").toString();
			MdsQuantifier quantifierObj = new MdsQuantifier(checkType, value); // checkType?
			
			// transition/condition/params/object
			String name = object.get("name").toString();
			MdsObject objectObj = new MdsObject(name, quantifierObj);
			
			object = (JSONObject) params.get("subject");
			quantifier = (JSONObject) object.get("quantifier");
			
			// transition/condition/params/subject/quantifier
			checkType = quantifier.get("checkType").toString();
			value = quantifier.get("value").toString();
			quantifierObj = new MdsQuantifier(checkType, value); // checkType?
			
			// transition/condition/params/subject
			name = object.get("name").toString();
			MdsObject subjectObj = new MdsObject(name, quantifierObj);
			
			// transition/condition/params
			paramsHM.put("object", objectObj);
			paramsHM.put("subject", subjectObj);
			
			Set<String> keySet = params.keySet();
			for (String key : keySet){
				if(!(key.equals("object") || key.equals("subject"))) {
					Object values = params.get(key);
					paramsHM.put(key, values);
				}
			}
		}
		else {
			Set<String> keySet = params.keySet();
			for (String key : keySet){
				Object values = params.get(key);
				paramsHM.put(key, values);
			}
		}
		return paramsHM;
	}
}
