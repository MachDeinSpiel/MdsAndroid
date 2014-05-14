package de.hsbremen.mds.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.hsbremen.mds.common.interfaces.InterpreterInterface;
import de.hsbremen.mds.common.valueobjects.MdsObject;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsCondition;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsEvent;
<<<<<<< HEAD
=======
import de.hsbremen.mds.common.valueobjects.statemachine.MdsObjectContainer;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsQuantifier;
>>>>>>> parser
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsTransition;
import de.hsbremen.mds.common.valueobjects.statemachine.actions.MdsAction;

public class Parser {

	public Parser(InterpreterInterface interpreter, File jsonFile){
		//TODO: Interpreter zwischenspeichern
		//TODO: Wenn fertig geparst wurde, MdsObjectContainer an Interpreter geben
		//		(mit interpreter.pushParsedObjects(MdsObjectContainer)
		
		JSONParser parser = new JSONParser();
		 
		try {
			
			Object obj = parser.parse(new FileReader(jsonFile));
			 
			JSONObject jsonObject = (JSONObject) obj;
			
			/* ---- aus der JSON datei lesen und in Objekte speichern ---- */
			
			JSONArray MdsAction = (JSONArray) jsonObject.get("action"); //lese des MdsAction arrays aus der JSON datei
			
			MdsAction[] allMdsActions = new MdsAction[MdsAction.size()];// array in dem all unsere MdsActions gespeichert werden
			String ident;												// temp variablen

			
			for (int i = 0; i < MdsAction.size(); i++) {	
				// aus dem JSONArray wird ein JSONObject an der stelle "i" zwischengespeichert
				JSONObject element = (JSONObject) MdsAction.get(i);
				
				// attribute werden aus dem JSONObject gelesen
				ident = (String) element.get("ident");
				// lese des defaults arrays aus der JSON datei
				HashMap<String, String> defaults = new HashMap<String, String>();
				if(element.get("defaults") != null) {
					JSONObject defaultsO = (JSONObject) element.get("defaults");
					// zwischenspeicherung der KeySet
					Set<String> keySet = defaultsO.keySet();
					// die param werte aus dem KeySet werden dem params HashMap übergeben
					for (String key : keySet){
						Object value = defaultsO.get(key);
						defaults.put(key, value.toString());
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
				else if(ident.equals("updateMap"))
					allMdsActions[i] = new MdsAction(MdsActionIdent.updateMap, defaults);

			}
			
			JSONArray MdsState = (JSONArray) jsonObject.get("states");	// lesen des MdsState arrays aus der JSON datei
			
			MdsState[] allMdsStates = new MdsState[MdsState.size()];	// array in dem all unsere MdsStates gespeichert werden
			
			// temp variablen
			int id;
			MdsAction doMdsAction = null;
			boolean startMdsState, finalMdsState;
			MdsAction startAction = null, endAction = null;
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
								
				if(element.get("doAction") instanceof JSONObject) {
					JSONObject doMdsActionObject = (JSONObject) element.get("doAction");
					
					HashMap<String, String> defaults = null;
					if(doMdsActionObject.get("params") != null) {
						JSONObject defaultsO = (JSONObject) doMdsActionObject.get("params");
						defaults = new HashMap<String, String>();
						Set<String> keySet = defaultsO.keySet();
						
						// die param werte aus dem KeySet werden dem params HashMap übergeben
						for (String key : keySet){
							Object value = defaultsO.get(key);
							defaults.put(key, value.toString());
						}
					}
					
					for(int j = 0; j < allMdsActions.length;j++) {
						System.out.println(allMdsActions[j].getIdent().toString());
						if (doMdsActionObject.get("name").toString().equals(allMdsActions[j].getIdent().toString())) {
								doMdsAction = new MdsAction(allMdsActions[j].getIdent(), allMdsActions[j].getParams());
								if(defaults != null) {
									doMdsAction.setParams(defaults);
								}
						}
					}
				}
				
				ident = element.get("startAction").toString();
				if(!ident.equals("null")) {
					JSONObject sAction = (JSONObject) element.get("startAction");
					ident = sAction.get("name").toString();
					
					HashMap<String, String> paramsHM = new HashMap<String, String>();
					if(sAction.get("params") != null) {
						JSONObject paramsO = (JSONObject) sAction.get("params");
						Set<String> keySet = paramsO.keySet();
						for (String key : keySet){
							Object value = paramsO.get(key);
							paramsHM.put(key, value.toString());
						}
					}
					for(int a = 0; a < allMdsActions.length; a++) {
						if(allMdsActions[a].getIdent().toString().equals(ident)) {
							startAction = new MdsAction(allMdsActions[a].getIdent(), paramsHM);
						}
					}
				}
				
				ident = element.get("endAction").toString();
				if(!ident.equals("null")) {
					JSONObject eAction = (JSONObject) element.get("endAction");
					ident = eAction.get("name").toString();

					HashMap<String, String> paramsHM = new HashMap<String, String>();
					if(eAction.get("params") != null) {
						JSONObject paramsO = (JSONObject) eAction.get("params");
						Set<String> keySet = paramsO.keySet();
						for (String key : keySet){
							Object value = paramsO.get(key);
							paramsHM.put(key, value.toString());
						}
					}
					for(int a = 0; a < allMdsActions.length; a++) {
						if(allMdsActions[a].getIdent().toString().equals(ident)) {
							endAction = new MdsAction(allMdsActions[a].getIdent(), paramsHM);
						}
					}
				}
				
				allMdsStates[i] = new MdsState(id, name, parentState, doMdsAction, startMdsState, finalMdsState);
				allMdsStates[i].setStartAction(startAction);
				allMdsStates[i].setEndAction(endAction);
			}
			
			for(int i = 0; i < MdsState.size(); i++) {
				MdsTransition[] allTrans = null;						// array in dem all unsere transitions gespeichert werden
				
				// aus dem JSONArray wird ein JSONObject an der stelle "i" zwischengespeichert
				JSONObject element = (JSONObject) MdsState.get(i);
				
				if(!element.get("transition").equals("null")) {
					JSONArray transition = (JSONArray) element.get("transition");	// lesen des transition arrays aus der JSON datei
					allTrans = new MdsTransition[transition.size()];	// die größe des arrays wird festgelegt
					String event, nameTransition;
					MdsState target = null;			// temp variablen
					
					for(int j = 0; j < transition.size(); j++) {
						// aus dem JSONArray wird ein JSONObject an der stelle "j" zwischengespeichert
						JSONObject transElem = (JSONObject) transition.get(j);
						HashMap<String, Object> paramsHM = new HashMap<String, Object>();
						
						JSONObject condition = (JSONObject) transElem.get("condition");
						JSONObject params = (JSONObject) condition.get("params");
						
						if(params.get("object") != null) {
							JSONObject object = (JSONObject) params.get("object");
							JSONObject quantifier = (JSONObject) object.get("quantifier");
							
							// transition/condition/params/object/quantifier
							String checkType = quantifier.get("checkType").toString();
							String value = quantifier.get("value").toString();
							MdsQuantifier quantifierObj = new MdsQuantifier(checkType, value); // checkType?
							
							// transition/condition/params/object
							name = object.get("name").toString();
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
								if(!key.equals("object") || !key.equals("subject")) {
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
						
						// transition/condition
						name = condition.get("name").toString();
						MdsCondition conditionObj = new MdsCondition(name, paramsHM);
						
						// transition
						name = transElem.get("target").toString();
						if(name != null) {
							for(int k = 0; k < allMdsStates.length; k++) {
								if(allMdsStates[k].getName().equals(name)) {
									target = allMdsStates[k];
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
						allTrans[j].setCondition(conditionObj);
						
					}
				}
				allMdsStates[i].setTransitions(allTrans); 
			}
			
			List<MdsState> states = Arrays.asList(allMdsStates);
			List<MdsAction> actions = Arrays.asList(allMdsActions);
			
			MdsObjectContainer MdsContainer = new MdsObjectContainer(actions, states);
			
//			interpreter.pushParsedObjects(MdsContainer);
		
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
}
