package de.hsbremen.mds.common.communication;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;

public class EntryHandler {

	List<String> keys = null;
	WhiteboardEntry entry;
	JSONObject json;
	
	public EntryHandler(String message) throws JSONException{
		json = new JSONObject(message);
		convertMessage();
	}
	
	public List<String> getKeys() {
		return keys;
	}

	public WhiteboardEntry getEntry() {
		return entry;
	}

	private void convertMessage(){
		try {
			String path = json.getString("path");
			String value = json.getString("entry");
			String visibility = json.getString("visibility");
			
			String[] parts = path.split(".");
			for(String s : parts){
				keys.add(s);
			}
			
			entry = new WhiteboardEntry(value, visibility);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
}
