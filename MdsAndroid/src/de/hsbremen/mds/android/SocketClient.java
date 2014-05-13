package de.hsbremen.mds.android;

import java.net.URI;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.FrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;

import android.util.Log;
import de.hsbremen.mds.common.communication.EntryHandler;
import de.hsbremen.mds.common.whiteboard.WhiterboardUpdateObject;

public class SocketClient extends WebSocketClient {
	
	MainActivity main = null;	
	
	public SocketClient(Draft d, URI uri, MainActivity main) {
		super(uri, d);
		this.main = main;
	}

	@Override
	public void onMessage(String message) {
		
		WhiterboardUpdateObject wObj = EntryHandler.toObject(message);
		
		main.initiater.updateLocalWhiteboard(wObj.getKeys(), wObj.getValue());

		Log.d("Na", "Message vom Server: " + message);
		main.consoleEntry(message);
	}

	@Override
	public void onMessage(ByteBuffer blob) {
		Log.d("Na", "Message ByteBuffer");
		getConnection().send(blob);
			main.consoleEntry(blob.toString());
	}

	@Override
	public void onError(Exception ex) {
		Log.d("Na", "Error: " + ex.getMessage());
		main.consoleEntry("Error: " + ex.getMessage());
	}

	@Override
	public void onOpen(ServerHandshake handshake) {
		Log.d("Na", "onOpen, Serverhandshake");
		main.consoleEntry("onOpen, Serverhandshake");
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		Log.d("Na", "Closed: " + code + " " + reason);
		main.consoleEntry("Closed: " + code + " " + reason);
	}

	public void onWebsocketMessageFragment(WebSocket conn, Framedata frame) {
		Log.d("Na", "onWebsocketMessageFragment");
		main.consoleEntry("onWebsocketMessageFragment");
		FrameBuilder builder = (FrameBuilder) frame;
		builder.setTransferemasked(true);
		getConnection().sendFrame(frame);
	}

}

