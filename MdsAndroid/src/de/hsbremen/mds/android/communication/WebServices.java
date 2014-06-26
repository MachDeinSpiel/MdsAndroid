package de.hsbremen.mds.android.communication;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class WebServices {

	public WebServicesInterface actInterface;

	// SocketService (beinhaltet Websocket)
	private ServiceConnection serviceConn;
	private SocketService socketService;
	private Vector<String> messageBuffer = new Vector<String>();

	/**
	 * Bind Service: SocketService to MainActivity
	 */
	private void doBindSocketService(WebServicesInterface act) {

		actInterface = act;

		Intent intent = new Intent(actInterface.getActivity().getBaseContext(),
				SocketService.class);

		serviceConn = new ServiceConnection() {
			public void onServiceConnected(ComponentName className,
					IBinder service) {
				// This is called when the connection with the service has been
				// established.
				Log.d("Socket",
						"WebServices: Connection zum Service hergestellt");
				socketService = ((SocketService.MyBinder) service)
						.getService(WebServices.this);
				socketService.setActivity(WebServices.this.actInterface
						.getActivity());

				if (messageBuffer.size() > 0) {
					for (String s : messageBuffer)
						socketService.send(s);
					messageBuffer.clear();
				}
			}

			public void onServiceDisconnected(ComponentName className) {
				// This is called when the connection with the service has been
				// unexpectedly disconnected
				socketService = null;
			}
		};
		// Activity an Service binden
		actInterface.getActivity().getBaseContext()
				.bindService(intent, serviceConn, Context.BIND_AUTO_CREATE);

	}

	public static WebServices createWebServices(WebServicesInterface act) {
		WebServices w = new WebServices();
		w.doBindSocketService(act);
		return w;
	}



	public void send(final String s) {
		if (socketService != null) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					WebServices.this.socketService.send(s);
				}
			};
			thread.start();
		} else {
			Log.d("Socket", "WebServices: SocketService ist null");
			messageBuffer.add(s);
		}
	}

	public void onMessage(final String message) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				actInterface.onWebSocketMessage(message);
			}
		}).start();
		;
	}

	public void closeWebServices() {
		// Detach our existing connection.
		actInterface.getActivity().unbindService(serviceConn);
	}

	public void onSocketConnected() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Log.d("Socket", "WebServices onSocketConnected");
				socketService.connectToServer();
				actInterface.onWebSocketConnected();
				if (messageBuffer.size() > 0) {
					for (String s : messageBuffer)
						socketService.send(s);
					messageBuffer.clear();
				}
			}
		}).start();

	}

	public void unbindService() {
		// Null um ihn zu unbinden vom service
		socketService.setWebService(null);
		// TODO Service richtig unbinden:
		// socketService.unbindService(serviceConn);
	}

	public void onConnectionClosed(final int code, final String reason,
			final boolean remote) {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {

			@Override
			public void run() {
				actInterface.onWebserviceConnectionClosed(code, reason, remote);
			}
		}).start();

	}

	public void onConnectionHandshake() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				actInterface.onWebSocketConnected();
			}
		}).start();
	}

}
