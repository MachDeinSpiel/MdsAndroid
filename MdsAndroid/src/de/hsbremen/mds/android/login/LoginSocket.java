package de.hsbremen.mds.android.login;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import android.util.Log;

public class LoginSocket extends WebSocketClient {
	
	@Override
	public void send(String text) throws NotYetConnectedException {
		// TODO Auto-generated method stub
		super.send(text);
	}

	LoginActivity loginActivity = null;	
	
	public LoginSocket(Draft d, URI uri, LoginActivity main) {
		super(uri, d);
		this.loginActivity = main;
	}

	@Override
	public void onMessage(String message) {
		Log.d("Na", "Message vom Server: " + message);
	}

	@Override
	public void onMessage(ByteBuffer blob) {
		Log.d("Na", "Message ByteBuffer");
//		getConnection().send(blob);
//			main.consoleEntry(blob.toString());
	}

	@Override
	public void onError(Exception ex) {
		Log.d("Na", "Error: " + ex.getMessage());
	}

	@Override
	public void onOpen(ServerHandshake handshake) {
		Log.d("Na", "onOpen, Verbindung zum Server steht!");
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		Log.d("Na", "Closed: " + code + " " + reason);
	}

}

