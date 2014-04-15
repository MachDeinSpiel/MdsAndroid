package de.hsbremen.mds.android;

import java.net.URI;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.FrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import android.util.Log;

public class SocketClient extends WebSocketClient{
	
	MainActivity main = null;
	
	public SocketClient(Draft d, URI uri, MainActivity main) {
		super(uri, d);
		this.main = main;
	}
	
	@Override
	public void onMessage(String message) {

		Log.d("Na", "Message vom Server: " + message);
		
		// TODO: Hier muss dem Clientinterpreter bescheid gesagt werden, dass es Änderungen auf dem Server gab
		// activity.initiater.serverUpdate(message);
	}

	@Override
	public void onMessage(ByteBuffer blob) {
		Log.d("Na", "Message ByteBuffer");
		getConnection().send(blob);
	}

	@Override
	public void onError(Exception ex) {
		Log.d("Na", "Error: " + ex.getMessage());
	}

	@Override
	public void onOpen(ServerHandshake handshake) {
		Log.d("Na", "onOpen, Serverhandshake");
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		Log.d("Na", "Closed: " + code + " " + reason);
	}

	public void onWebsocketMessageFragment(WebSocket conn, Framedata frame) {
		Log.d("Na", "onWebsocketMessageFragment");
		FrameBuilder builder = (FrameBuilder) frame;
		builder.setTransferemasked(true);
		getConnection().sendFrame(frame);
	}

}

