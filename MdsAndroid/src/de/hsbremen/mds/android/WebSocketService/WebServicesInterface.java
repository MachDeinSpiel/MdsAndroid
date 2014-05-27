package de.hsbremen.mds.android.WebSocketService;

import android.app.Activity;

public interface WebServicesInterface {

	public Activity getActivity();
	public void onWebSocketMessage(String message);
}
