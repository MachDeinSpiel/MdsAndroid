package de.hsbremen.mds.android.communication;

import java.net.URI;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.FrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import android.util.Log;

public class SocketClient extends WebSocketClient {

	private SocketService service;

	public SocketClient(Draft d, URI uri, SocketService socketService) {
		super(uri, d);
		Log.d("Socket", "SocketClient: Gestartet");
		this.service = socketService;
	}

	@Override
	public void onMessage(String message) {
		Log.d("Socket", "SocketClient: OnMessage: " + message);
		service.onMessage(message);
	}

	@Override
	public void onMessage(ByteBuffer blob) {
		Log.d("Socket", "Message ByteBuffer");
		getConnection().send(blob);
	}

	@Override
	public void onError(Exception ex) {
		Log.d("Socket", "Error: [" + ex.getMessage() + "] ["
				+ ex.getStackTrace().toString() + "]");
		ex.printStackTrace();
		if (ex instanceof ClassCastException) {
			Log.d("Socket", "Error Details:[ ClasCastException]");
		}
		
		ex.printStackTrace();
	}

	@Override
	public void onOpen(ServerHandshake handshake) {
		Log.d("Socket", "SocketClient: OnSocketClientOpen");
		service.onConnnectionHandshake();
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		Log.d("Socket", "SocketClient: Closed: " + code + " " + reason);
		service.onConnectionClosed(code, reason, remote);
	}

	public void onWebsocketMessageFragment(WebSocket conn, Framedata frame) {
		Log.d("Socket", "SocketClient: onWebsocketMessageFragment");
		
		FrameBuilder builder = (FrameBuilder) frame;
		builder.setTransferemasked(true);
		getConnection().sendFrame(frame);
	}

}
