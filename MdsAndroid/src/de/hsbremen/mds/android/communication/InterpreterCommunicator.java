package de.hsbremen.mds.android.communication;

import java.util.List;

import org.json.JSONObject;

import android.location.Location;
import android.util.Log;
import de.hsbremen.mds.common.communication.WhiteboardHandler;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.common.interfaces.ClientInterpreterInterface;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.common.whiteboard.WhiteboardUpdateObject;

public class InterpreterCommunicator {

	double positionIntervall;
	Location oldLocation;
	ClientInterpreterInterface interpreter;

	// TODO Listener ersetzen durch AndroidInterpreterVermittler
	public InterpreterCommunicator(ClientInterpreterInterface interpreter,
			double intervall) {
		positionIntervall = intervall;
		oldLocation = null;
		this.interpreter = interpreter;
	}

	public void locationChanged(Location loc) {
		if (interpreter != null && loc != null) {
			if (oldLocation == null
					|| loc.distanceTo(oldLocation) >= positionIntervall) {
				Log.d("Android", "Neue Position");
				// Callback mit aktueller Location
				interpreter.onPositionChanged(loc.getLongitude(),
						loc.getLatitude());
				oldLocation = loc;
			}
		}
	}

	public void buttonClicked(String name) {
		if (interpreter != null) {
			// Callback mit Event Informationen
			interpreter.onButtonClick(name);
		}
	}

	public void updateLocalWhiteboard(List<String> keys, WhiteboardEntry entry) {
		if (interpreter != null) {
			// Callback mit Event Informationen
			interpreter.onWhiteboardUpdate(keys, entry);
		}
	}

	public void fullUpdateLocalWhiteboard(List<WhiteboardUpdateObject> list) {
		if (interpreter != null) {
			// Callback mit Event Informationen
			Log.d("Socket", "WhiteboardUpdateObjectlistenlänge: " + list.size());
			interpreter.onFullWhiteboardUpdate(list);
		}
	}

	public void useItem(MdsItem item, String identifier) {
		this.interpreter.useItem(item, identifier);
	}

	public void dropItem(MdsItem item) {
		this.interpreter.dropItem(item);
	}

	public void minigameResult(int punkte, boolean gewonnen) {
		interpreter.onMinigameResult(punkte, gewonnen);
	}

	public void onWebsocketMessage(JSONObject json) {
		// TODO ENTRY HANDLER Soll Whiteboard Handler heissen und JSONObject
		// annehmen
		List<WhiteboardUpdateObject> wObj = WhiteboardHandler.toObject(json
				.toString());

		if (wObj.size() == 1)
			updateLocalWhiteboard(wObj.get(0).getKeys(), wObj.get(0).getValue());
		else
			fullUpdateLocalWhiteboard(wObj);
	}
	
	public void onGameResult(boolean hasWon, String identifier){
		this.interpreter.onGameResult(hasWon, identifier);
	}

}
