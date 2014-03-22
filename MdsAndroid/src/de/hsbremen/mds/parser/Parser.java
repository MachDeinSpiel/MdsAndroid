package de.hsbremen.mds.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.hsbremen.mds.common.interfaces.InterpreterInterface;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsAction;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsEvent;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsExhibit;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsItem;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsObjectContainer;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsPlayer;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsState;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsTransition;

public class Parser {

	public Parser(InterpreterInterface interpreter, File jsonFile){
		//TODO: braucht ihr �erbaupt ein File-Object? Wenn nicht einfach Typ �ndern, wir
		//		(Interpretergruppe) �ndern dann unseren Aufruf
		//TODO: Interpreter zwischenspeichern
		//TODO: Wenn fertig geparst wurde, MdsObjectContainer an Interpreter geben
		//		(mit interpreter.pushParsedObjects(MdsObjectContainer)
		
		JSONParser parser = new JSONParser();
		 
		try {
			
			Object obj = parser.parse(new FileReader("TourismApp_2.0_Client.json"));
	 
			JSONObject jsonObject = (JSONObject) obj;
			
			/* ---- aus der JSON datei lesen und in Objekte speichern ---- */
			
			JSONArray MdsAction = (JSONArray) jsonObject.get("action"); //lese des MdsAction arrays aus der JSON datei
			
			MdsAction[] allMdsActions = new MdsAction[MdsAction.size()];        // array in dem all unsere MdsActions gespeichert werden
			String ident;											// temp variablen

			
			for (int i = 0; i < MdsAction.size(); i++){	
				HashMap<String, String> defaults = new HashMap<String, String>();						// temp variablen
				// aus dem JSONArray wird ein JSONObject an der stelle "i" zwischengespeichert
				JSONObject element = (JSONObject) MdsAction.get(i);
				
				// attribute werden aus dem JSONObject gelesen
				ident = (String) element.get("ident");
				// lese des defaults arrays aus der JSON datei
				JSONObject defaultso = (JSONObject) element.get("defaults");
				// zwischenspeicherung der KeySet
				Set<String> keySet = defaultso.keySet();
				// die param werte aus dem KeySet werden dem params HashMap �bergeben
				for (String key : keySet){
					
					Object value = defaultso.get(key);
					defaults.put(key, value.toString());
					
				}
				// gelesene attribute werden in das allMdsActions array gespeichert
				allMdsActions [i] = new MdsAction(ident, defaults);
				//Die HashMap wird geleert
				//params.clear();
				
			}
			
			// lesen des MdsPlayer objects
			JSONObject MdsPlayer = (JSONObject) jsonObject.get("player");
			
			//Temp variablen
			String name = MdsPlayer.get("name").toString();
			
			
	
			double longitude = Double.parseDouble(MdsPlayer.get("longitude").toString());
			double latitude = Double.parseDouble(MdsPlayer.get("latitude").toString());
			
			// erzeugen des MdsPlayers
			MdsPlayer player = new MdsPlayer(name, longitude, latitude);
			
			MdsExhibit[] allMdsExhibits = null;
			
			JSONArray MdsState = (JSONArray) jsonObject.get("state");	// lesen des MdsState arrays aus der JSON datei
			
			MdsState[] allMdsStates = new MdsState[MdsState.size()];				// array in dem all unsere MdsStates gespeichert werden
			int id;												// temp variablen
			MdsAction doMdsAction = null;
			boolean startMdsState = true, finalMdsState = true;
			String startAction = null, endAction = null;
			
			for(int i = 0; i < MdsState.size(); i++) {
				// aus dem JSONArray wird ein JSONObject an der stelle "i" zwischengespeichert
				JSONObject element = (JSONObject) MdsState.get(i);
				
				// attribute werden aus dem JSONObject gelesen
				id = Integer.parseInt(element.get("ID").toString());
				name = (String) element.get("name");
				
				String parentState = element.get("parentState").toString();
				
				if(element.get("startState").equals(false)) 
					startMdsState = false;
				else startMdsState = true;
				
				if(element.get("finalState").equals(false)) 
					finalMdsState = false;
				else finalMdsState = true;
				
				
				
				//Verarbeitung fehlt, da diese felder leer sind
				//JSONObject startMdsActionObject = (JSONObject) element.get("startAction");
				startAction = element.get("startAction").toString();
				
				//Verarbeitung fehlt, da diese felder leer sind
				//JSONObject endMdsActionObject = (JSONObject) element.get("endAction");
				endAction = element.get("endAction").toString();
				
				JSONObject doMdsActionObject = (JSONObject) element.get("doAction");
				HashMap<String, String> defaults = null;
				
				if(doMdsActionObject.get("params") != null) {
					JSONObject defaultsO = (JSONObject) doMdsActionObject.get("params");
					defaults = new HashMap<String, String>();
					Set<String> keySet = defaultsO.keySet();
					
					// die param werte aus dem KeySet werden dem params HashMap �bergeben
					for (String key : keySet){
						Object value = defaultsO.get(key);
						defaults.put(key, value.toString());
					}
				}
				
				for(int j = 0; j < allMdsActions.length;j++) {
					if (doMdsActionObject.get("name").toString().equals(allMdsActions[j].getIdent())) {
							doMdsAction = new MdsAction(allMdsActions[j].getIdent(), allMdsActions[j].getParams());
							if(defaults != null) {
								doMdsAction.setParams(defaults);
							}
					}
				}
				// TODO: Parentstate (im Moment null) muss noch ersetzt werden
				allMdsStates[i] = new MdsState(id, name, null, doMdsAction, startMdsState, finalMdsState);
			}
			
			for(int i = 0; i < MdsState.size(); i++) {
				MdsTransition[] allTrans = null;						// array in dem all unsere transitions gespeichert werden
				
				// aus dem JSONArray wird ein JSONObject an der stelle "i" zwischengespeichert
				JSONObject element = (JSONObject) MdsState.get(i);
				
				if(allMdsStates[i].getFinalState() != true) {							// wenn finalMdsState nicht true --> dann lie� die transitions
					
					JSONArray transition = (JSONArray) element.get("transition");	// lesen des transition arrays aus der JSON datei
					allTrans = new MdsTransition[transition.size()];	// die gr��e des arrays wird festgelegt
					String type, nameTransition;
					MdsState target = null;			// temp variablen
					
					for(int j = 0; j < transition.size(); j++) {
						// aus dem JSONArray wird ein JSONObject an der stelle "j" zwischengespeichert
						JSONObject transElem = (JSONObject) transition.get(j);
						
						// attribute werden aus dem JSONObject gelesen
						name = transElem.get("target").toString();
						System.out.println(name);
						if(name != null) {
							for(int k = 0; k < allMdsStates.length; k++) {
								if(allMdsStates[k].getName().equals(name)) {
									target = allMdsStates[k];
								}
							}
							
							JSONObject eventObject = (JSONObject) transElem.get("event");
							MdsEvent event;
							HashMap<String, String> paramsEvent = new HashMap<String, String>();	
							
							type = eventObject.get("type").toString();
							nameTransition = eventObject.get("name").toString();
							
							// lese des param arrays aus der JSON datei
							if( eventObject.get("params") != null){
								JSONObject paramsEventO = (JSONObject) eventObject.get("params");
								// zwischenspeicherung der KeySet
								Set<String> keySet = paramsEventO.keySet();
								for (String key : keySet){
									
									Object value = paramsEventO.get(key);
									paramsEvent.put(key, value.toString());
									
								}
							}
							event = new MdsEvent(type, nameTransition, paramsEvent);						
							
							// gelesene attribute werden in das allTrans array gespeichert
							allTrans[j] = new MdsTransition(target, event);
						}
					}
				}
				allMdsStates[i].setTransitions(allTrans); 
			}
			
			
			 /*---- ausgabe der Objekte ---- 
			
			for(int i = 0; i < allMdsActions.length; i++) {
				System.out.println("-------- Action (" + i + ") --------");
				//Ausgabe der Items
				System.out.println("ident: " + allMdsActions[i].getIdent());
				System.out.println("params: " + allMdsActions[i].getParams().toString());
			}
			
			System.out.println("-------- Player --------");
			System.out.println("name: " + player.getName());
			System.out.println("position: x: " + player.getLongtitude() + " y: " + player.getLatitude());
						
			for(int i = 0; i < allMdsStates.length; i++) {
				System.out.println("-------- State (" + i + ") --------");
				System.out.println("Zustand: " + allMdsStates[i].getName());
				System.out.println("parentState: " + allMdsStates[i].getParentState());
				System.out.println("Startzustand: " + allMdsStates[i].isStartState());
				System.out.println("Endzustand: " + allMdsStates[i].getFinalState());
				System.out.println("Do-Aktion: " + allMdsStates[i].getDoAction().getIdent() + " - " + allMdsStates[i].getDoAction().getParams());
				System.out.println("start-Aktion: " + startAction);
				System.out.println("end-Aktion: " + endAction);
				
				
				if(!allMdsStates[i].getFinalState()) {
					// zur besseren lesbarkeit wird das transitions array zwischengespeichert
					MdsTransition[] trans = allMdsStates[i].getTransitions();
					
					for(int j = 0; j < trans.length; j++) {
						System.out.println("Ziel: " + trans[j].getTarget().getName());
						System.out.println("-------- Event --------");
						System.out.println("Type: " + trans[j].getEvent().getType());
						System.out.println("Name: " + trans[j].getEvent().getName());
						System.out.println("params: " + trans[j].getEvent().getParams().toString());

					}
					
				}
			}
			*/
			
			List<MdsState> states = Arrays.asList(allMdsStates);
			List<MdsAction> actions = Arrays.asList(allMdsActions);
			List<MdsExhibit> exhibits = Arrays.asList(allMdsExhibits);
			
			// TODO: Hier wird noch null f�r allMdsItems gemacht, �ndern!
			
			MdsObjectContainer MdsContainer = new MdsObjectContainer(actions, player, exhibits, null, states);
			
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
}
