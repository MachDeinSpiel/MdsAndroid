
package de.hsbremen.mds.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import valueobjects.MdsGroup;
import valueobjects.MdsMembers;
import de.hsbremen.mds.common.interfaces.InterpreterInterface;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsExhibit;


public class ParserServer {

	public ParserServer(InterpreterInterface interpreter, File jsonFile){
		JSONParser parser = new JSONParser();
		 
		try {
			
			Object obj = parser.parse(new FileReader("TourismApp_2.0_Server.json"));
			 
			JSONObject jsonObject = (JSONObject) obj;
			
			JSONArray groups = (JSONArray) jsonObject.get("groups");
			
			MdsGroup[] allMdsGroups = new MdsGroup[groups.size()];
			
			for(int i = 0; i < groups.size(); i++) {
				JSONObject groupsElement = (JSONObject) groups.get(i);
				
				String name = groupsElement.get("name").toString();
				
				JSONArray members = (JSONArray) groupsElement.get("members");
				
				MdsMembers[] membersArray = new MdsMembers[members.size()];
				
				for(int j = 0; j < members.size(); j++) {
					
					JSONObject element = (JSONObject) members.get(j);
					
					HashMap<Object, Object> membersMap = new HashMap<Object, Object>();
					
					Set<Object> keySet = element.keySet();
					
					// die param werte aus dem KeySet werden dem params HashMap übergeben
					for (Object key : keySet){
						Object value = element.get(key);
						membersMap.put(key, value);
					}
					
					membersArray[j] = new MdsMembers(membersMap);
				}
				
				allMdsGroups[i] = new MdsGroup(name, membersArray);
			}		
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

