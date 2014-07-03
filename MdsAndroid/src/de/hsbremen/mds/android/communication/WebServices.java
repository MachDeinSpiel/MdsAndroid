package de.hsbremen.mds.android.communication;

import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

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

				Log.d("Socket", "WebService: ServiceConn disconnected" + actInterface.getActivity().getClass().toString());
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
				try {
					Log.d("Socket", "WebServices: Nachricht angekommen bei: "+ actInterface.getClass().toString());
					if (new JSONObject(message).has("mode"))
						actInterface.onWebSocketMessage(message);
					else
						Log.d("Socket", "WebServices: Kein Mode in JSON: "
								+ message);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();
		;
	}

	public void closeWebServices() {
		// Detach our existing connection.
		Log.d("Socket", "WebService: wird geschlossen..." + actInterface.getActivity().getClass().toString());

		actInterface
				.getActivity()
				.getBaseContext()
				.stopService(
						new Intent(actInterface.getActivity(),
								SocketService.class));

		actInterface.getActivity().getBaseContext()
				.unbindService(this.serviceConn);
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
		Log.d("Socket", "WebService: wird ungebindet..." + actInterface.getActivity().getClass().toString());
		actInterface.getActivity().getBaseContext()
				.unbindService(this.serviceConn);

	}

	public void onConnectionClosed(final int code, final String reason,
			final boolean remote) {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {

			@Override
			public void run() {
				Log.d("Socket", "WebService: onConnectionClosed: " + actInterface.getActivity().getClass().toString());
				actInterface.onWebserviceConnectionClosed(code, reason, remote);
			}
		}).start();

	}

	public void onConnectionHandshake() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Log.d("Socket", "Webservice: verbunden (Handshake)" + actInterface.getActivity().getClass().toString());
				actInterface.onWebSocketConnected();
			}
		}).start();
	}

}
