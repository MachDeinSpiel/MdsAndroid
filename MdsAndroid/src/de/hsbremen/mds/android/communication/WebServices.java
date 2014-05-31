package de.hsbremen.mds.android.communication;

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
				socketService = ((SocketService.MyBinder) service).getService(WebServices.this);
				Log.d("Socket", "ServiceConnection muss vor TESTNACHRICHT");
				socketService.setActivity(WebServices.this.actInterface.getActivity());
				WebServices.this.onSocketConnected();
			}

			public void onServiceDisconnected(ComponentName className) {
				// This is called when the connection with the service has been
				// unexpectedly disconnected
				socketService = null;
			}
		};

		// Activity an Service binden
		actInterface.getActivity().getBaseContext().bindService(intent, serviceConn,
				Context.BIND_AUTO_CREATE);

	}

	public static WebServices createWebServices(WebServicesInterface act) {
		WebServices w = new WebServices();
		w.doBindSocketService(act);
		return w;
	}

	public void send(String s) {
		if (socketService != null)
			socketService.send(s);
		else
			Log.d("Socket", "WebServices: SocketService ist null");
	}

	public void onMessage(String message){
		actInterface.onWebSocketMessage(message);
	}
	
	public void closeWebServices() {
		// Detach our existing connection.
		actInterface.getActivity().unbindService(serviceConn);
	}
	
	public void onSocketConnected(){
		actInterface.onWebserviceConnected();
	}
}
