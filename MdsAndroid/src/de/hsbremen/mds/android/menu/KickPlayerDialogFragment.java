package de.hsbremen.mds.android.menu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class KickPlayerDialogFragment extends DialogFragment {
	
	private String username;
	
	public KickPlayerDialogFragment() {
		super();
	}
	
	public void prepareDialog(String name){
		username = name;
	}
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Willst du wirklich " + username + " kicken?")
               .setPositiveButton("Kick", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       KickPlayerListener activity = (KickPlayerListener) getActivity();
                       activity.onKickPlayerResult(true, username);
                   }
               })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       KickPlayerListener activity = (KickPlayerListener) getActivity();
                       activity.onKickPlayerResult(false, username);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}