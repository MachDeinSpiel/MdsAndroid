package de.hsbremen.mds.android.communication;

import java.net.URI;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;

import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class SocketService extends Service {

	// Interaktion mit Apps:
	private final IBinder mBinder = new MyBinder();
	// Websocket:
	private SocketClient socketClient;
	private WebServices webServices;
	private ArrayList<String> bufferedMessages = new ArrayList<String>();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO do something useful
		Log.d("Socket", "Service: Gestartet");
		connectToServer();
		return startId;
	}

	public void connectToServer() {

		Log.d("Socket", "SocketService: Connect to Server");
		// Serverkommunikation
		Draft d = new Draft_17();

		String PROTOKOLL_WS = "ws://";
		// String serverIp = "195.37.176.178";
		// String PORT_WS = ":1387";
		String serverIp = "192.168.2.110";
		String PORT_WS = ":8887";

		String serverlocation = PROTOKOLL_WS + serverIp + PORT_WS;

		URI uri = URI.create(serverlocation + "/runCase?case=" + 1 + "&agent="
				+ "Android");
		socketClient = new SocketClient(d, uri, this);

		Log.d("Socket", "SocketService: Socketclient wurde gestartet...");
		Thread t = new Thread(socketClient);
		t.start();

	}

	public static void createSocketService(Context context) {
		Intent intent = new Intent(context, SocketService.class);
		context.startService(intent);
	}

	public void setWebService(WebServices web) {
		webServices = web;
	}

	public class MyBinder extends Binder {
		SocketService getService(WebServices service) {
			Log.d("Socket", "SocketService: MyBinder: getService");
			SocketService.this.webServices = service;

			if (bufferedMessages.size() > 0) {
				for (int i = 0; i < bufferedMessages.size(); i++) {
					webServices.onMessage(bufferedMessages.get(i));
					
					Log.d("Socket",
							"SocketService: Buffered Message an Websocket gesendet");
				}
			}
			bufferedMessages.clear();

			return SocketService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		webServices = null;
		return super.onUnbind(intent);
	}

	public void send(String s) throws NotYetConnectedException {
		try {
			Log.d("Socket", "SocketService: Wird gesendet: " + s);
			socketClient.getConnection().send(s);
		} catch (NullPointerException ex) {
			Log.d("Socket",
					"SocketService: Noch keine Connection" + ex.getMessage());
			throw new NotYetConnectedException();
		}
	}

	@Override
	public void onDestroy() {
		Log.d("Socket", "SocketService: onDestroy()");
		socketClient.close();
		super.onDestroy();
	}

	public void setActivity(Activity activity) {

	}

	public void onMessage(String message) {
		if (this.webServices != null)
			webServices.onMessage(message);
		else
			bufferedMessages.add(message);
	}

	public void onConnectionClosed(int code, String reason, boolean remote) {
		// TODO Auto-generated method stub
		webServices.onConnectionClosed(code, reason, remote);
	}

	public void onConnnectionHandshake() {
		webServices.onConnectionHandshake();
	}

}
