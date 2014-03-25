package de.hsbremen.mds.parser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.hsbremen.mds.common.valueobjects.statemachine.MdsExhibit;


public class ParserServer {

	public ParserServer(InterpreterInterface interpreter, File jsonFile){
		JSONParser parser = new JSONParser();
		 
		try {
			
			Object obj = parser.parse(new FileReader("TourismApp_2.0_Server.json"));
			 
			JSONObject jsonObject = (JSONObject) obj;
		
			JSONArray groups = (JSONArray) jsonObject.get("groups");
			
			MdsExhibit[] allMdsExhibits = null;
			
			for(int i = 0; i < groups.size(); i++) {
				JSONObject groupsElement = (JSONObject) groups.get(i);
				
				String name = groupsElement.get("name").toString();
				
				if(name.equals("exhibits")) {
					JSONArray MdsExhibits = (JSONArray) groupsElement.get("members");
					
					allMdsExhibits = new MdsExhibit[MdsExhibits.size()];
					String url, text;
					
					for(int j = 0; j < MdsExhibits.size(); j++) {
						
						JSONObject element = (JSONObject) MdsExhibits.get(j);
						
						name = element.get("name").toString();
						url = element.get("url").toString();
						text = element.get("text").toString();
						
						double longitude = Double.parseDouble(element.get("longitude").toString());
						double latitude = Double.parseDouble(element.get("latitude").toString());
						
						int movable = Integer.parseInt(element.get("moveableStatus").toString());
						
						
						allMdsExhibits[j] = new MdsExhibit(name,url,text,longitude,latitude,movable);
					}
				}
				//else if(name.equals("items"))  <-- für später
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

