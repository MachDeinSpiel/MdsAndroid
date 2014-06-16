package de.hsbremen.mds.android.login;

import de.hsbremen.mds.mdsandroid.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class KickPlayerDialogFragment extends DialogFragment {
	
	private String username;
	private int playerId;
	
	public KickPlayerDialogFragment() {
		super();
	}
	
	public void prepareDialog(String name, int id){
		username = name;
		playerId = id;
	}
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Willst du wirklich " + username + " kicken?")
               .setPositiveButton("Kick", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       KickPlayerListener activity = (KickPlayerListener) getActivity();
                       activity.onKickPlayerResult(true, playerId);
                   }
               })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       KickPlayerListener activity = (KickPlayerListener) getActivity();
                       activity.onKickPlayerResult(false, playerId);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}