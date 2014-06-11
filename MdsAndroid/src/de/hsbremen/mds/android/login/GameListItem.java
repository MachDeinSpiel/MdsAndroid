package de.hsbremen.mds.android.login;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.hsbremen.mds.mdsandroid.R;

public class GameListItem extends ArrayAdapter<String> {
	private final Activity context;
	private final String[] name;
	private final Integer[] imageId;
	private final Integer[] maxplayers;
	private final Integer[] activePlayers;
	private final Integer[] id;

	public GameListItem(Activity context, String[] name, Integer[] imageId,
			Integer[] maxPlayers, Integer[] players, Integer[] id) {
		super(context, R.layout.gamelistitem, name);
		this.context = context;
		this.name = name;
		this.imageId = imageId;
		this.maxplayers = maxPlayers;
		this.activePlayers = players;
		this.id = id;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.gamelistitem, null, true);
		TextView txtTitle = (TextView) rowView.findViewById(R.id.gameText);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.gameIcon);
		txtTitle.setText(name[position]);
		imageView.setImageResource(imageId[position]);

		TextView maxPlayers = (TextView) rowView
				.findViewById(R.id.labelMaxPlayers);

			if(this.activePlayers[position] == null){
				this.activePlayers[position] = 0;
			}
		
			maxPlayers.setText(this.activePlayers[position] + " / "
					+ this.maxplayers[position]);

		// TextView maxplayersLabel = (TextView)
		// view.findViewById(R.id.labelMaxPlayers);
		// maxplayersLabel.setText(maxplayers);

		return rowView;
	}

	public Integer getImageId(int pos) {
		return imageId[pos];
	}

	public String getName(int pos) {
		return name[pos];
	}

	public Integer getMaxplayers(int pos) {
		return maxplayers[pos];
	}

	public Integer getPlayers(int pos) {
		return activePlayers[pos];
	}

	public Integer getId(int pos) {
		return id[pos];
	}
	
}