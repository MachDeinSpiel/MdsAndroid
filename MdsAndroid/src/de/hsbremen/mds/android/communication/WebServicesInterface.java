package de.hsbremen.mds.android.communication;

import android.app.Activity;

public interface WebServicesInterface {

	public Activity getActivity();
	public void onWebSocketMessage(String message);
}
