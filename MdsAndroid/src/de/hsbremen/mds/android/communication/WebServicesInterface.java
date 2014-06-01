package de.hsbremen.mds.android.communication;

import android.app.Activity;

public interface WebServicesInterface {

	public void onWebserviceConnected();
	public Activity getActivity();
	public void onWebSocketMessage(String message);
}
