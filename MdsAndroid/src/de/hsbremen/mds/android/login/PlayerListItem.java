package de.hsbremen.mds.android.login;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.hsbremen.mds.mdsandroid.R;

public class PlayerListItem extends ArrayAdapter<String> {
	private final Activity context;
	private final String[] web;
	private final Integer[] imageId;

	public PlayerListItem(Activity context, String[] web, Integer[] imageId) {
		super(context, R.layout.gamelistitem, web);
		this.context = context;
		this.web = web;
		this.imageId = imageId;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.playerlistitem, null, true);
		TextView txtTitle = (TextView) rowView.findViewById(R.id.gameText);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.gameIcon);
		txtTitle.setText(web[position]);
		imageView.setImageResource(imageId[position]);
		
		//TextView maxplayersLabel = (TextView) view.findViewById(R.id.labelMaxPlayers);
		//maxplayersLabel.setText(maxplayers);
		
		return rowView;
	}
}