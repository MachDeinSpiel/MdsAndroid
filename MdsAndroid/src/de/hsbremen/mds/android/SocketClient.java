package de.hsbremen.mds.android;

import java.net.URI;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.FrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import android.widget.Toast;

public class SocketClient extends WebSocketClient {
	
	MainActivity main = null;
	
	public SocketClient(Draft d, URI uri, MainActivity main) {
		super(uri, d);
		this.main = main;
	}

	@Override
	public void onMessage(String message) {
		send(message);
		main.toastShow(message, Toast.LENGTH_LONG);
	}

	@Override
	public void onMessage(ByteBuffer blob) {
		getConnection().send(blob);
	}

	@Override
	public void onError(Exception ex) {
		System.out.println("Error: ");
		ex.printStackTrace();
	}

	@Override
	public void onOpen(ServerHandshake handshake) {
		System.out.println("Serverhandshake hat geklappt");
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		System.out.println("Closed: " + code + " " + reason);
	}

	public void onWebsocketMessageFragment(WebSocket conn, Framedata frame) {
		FrameBuilder builder = (FrameBuilder) frame;
		builder.setTransferemasked(true);
		getConnection().sendFrame(frame);
	}

}

