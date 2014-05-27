package de.hsbremen.mds.android.WebSocketService;

import java.net.URI;

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

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO do something useful
		Log.d("Socket", "Service: Gestartet");
		connectToServer(intent.getExtras().getCharSequence("username"));

		return startId;
	}

	private void connectToServer(CharSequence user) {

		Log.d("Socket", "SocketService: Connect to Server");
		// Serverkommunikation
		Draft d = new Draft_17();

		String serverIp = "192.168.2.110";
		String PROTOKOLL_WS = "ws://";
		String PORT_WS = ":8887";

		String serverlocation = PROTOKOLL_WS + serverIp + PORT_WS;

		URI uri = URI.create(serverlocation + "/runCase?case=" + 1 + "&agent="
				+ user);
		socketClient = new SocketClient(d, uri, this);

		Thread t = new Thread(socketClient);
		t.start();

	}

	public static void createSocketService(Context context, CharSequence user) {
		Intent intent = new Intent(context, SocketService.class);
		intent.putExtra("username", user);
		context.startService(intent);
	}

	public class MyBinder extends Binder {
		SocketService getService(WebServices service) {
			Log.d("Socket", "MyBinder: getService");
			SocketService.this.webServices = service;
			return SocketService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void send(String s) {
		try {
			Log.d("Socket", "SocketService: Wird gesendet: " + s);
			socketClient.getConnection().send(s);
		} catch (NullPointerException ex) {
			Log.d("Socket", "SocketService: Noch keine Connection" + ex.getMessage());
		}
	}

	@Override
	public void onDestroy() {
		Log.d("Socket", "SocketService: onDestroy()");
		socketClient.close();
		super.onDestroy();
	}

	public void setActivity(Activity activity) {
		// TODO Auto-generated method stub

	}

	public void onMessage(String message) {
		if (this.webServices != null)
			webServices.onMessage(message);
	}

}
