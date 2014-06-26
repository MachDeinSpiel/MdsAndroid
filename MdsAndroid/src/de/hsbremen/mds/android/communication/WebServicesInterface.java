package de.hsbremen.mds.android.communication;

import android.app.Activity;

public interface WebServicesInterface {

	public void onWebSocketConnected();
	public Activity getActivity();
	public void onWebSocketMessage(String message);
	public void onWebserviceConnectionClosed(int code, String reason,
			boolean remote);
}
