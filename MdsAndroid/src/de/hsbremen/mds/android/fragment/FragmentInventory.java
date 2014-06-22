package de.hsbremen.mds.android.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.hsbremen.mds.android.ingame.CustomGrid;
import de.hsbremen.mds.android.ingame.MainActivity;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.mdsandroid.R;

public class FragmentInventory extends Fragment{

	private View inventoryView;
	private ArrayList<MdsItem> itemList = new ArrayList<MdsItem>();
	private ArrayList<String> itemAsStringList = new ArrayList<String>();
	private BaseAdapter adapter;
	private GridView lv;
	private int itemPosition;
	
	  GridView grid;
	  List<String> buttonList = new ArrayList<String>();
	  String[] web = {
	        "Potion1",
	        "Potion2",
		    "Mana1",
		    "Mana2",
		    "Potion3",
		    "Potion4",
		    "Mana3",
		    "Mana4",
		    "Mana5",
		    "Mana6",
		    "Potion5",
		    "Potion6",
		    "Potion7",
		    "Potion8",
		    "Mana7",
		    "Potion1",
		    "Potion2",
		    "Mana1",
		    "Mana2",
		    "Potion3",
		    "Potion4",
		    "Mana3",
		    "Mana4",
		    "Mana5",
		    "Mana6",
		    "Potion5",
		    "Potion6",
		    "Potion7",
		    "Potion8",
		    "Mana7"
	  } ;
	  int[] imageId = {
	      R.drawable.red,
	      R.drawable.red,
	      R.drawable.potion,
	      R.drawable.potion,
	      R.drawable.red,
	      R.drawable.red,
	      R.drawable.potion,
	      R.drawable.potion,
	      R.drawable.potion,
	      R.drawable.potion,
	      R.drawable.red,
	      R.drawable.red,
	      R.drawable.red,
	      R.drawable.red,
	      R.drawable.potion,
	      R.drawable.red,
	      R.drawable.red,
	      R.drawable.potion,
	      R.drawable.potion,
	      R.drawable.red,
	      R.drawable.red,
	      R.drawable.potion,
	      R.drawable.potion,
	      R.drawable.potion,
	      R.drawable.potion,
	      R.drawable.red,
	      R.drawable.red,
	      R.drawable.red,
	      R.drawable.red,
	      R.drawable.potion
	  };
	  
	  @Override
	  public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) {
	    
		  
		 inventoryView = inflater.inflate(R.layout.fragment_inventorynew, container, false);
		
		 CustomGrid adapter = new CustomGrid(getActivity(), web, imageId);
		 grid=(GridView)inventoryView.findViewById(R.id.grid);
	     grid.setAdapter(adapter);
	     grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {                    	
                showItemDialog(position); 
            }
	     });
	        
	     buttonList.add("use");
	     buttonList.add("drop");
	     buttonList.add("remove");
	     
	     return inventoryView;
	  }	  
	  
	  private void showItemDialog(int position){
			// custom dialog
			final Dialog dialog = new Dialog(getActivity());
			dialog.setContentView(R.layout.custom);
			dialog.setTitle(web[+ position]);
		 
					// set the custom dialog components - text, image and button
			TextView text = (TextView) dialog.findViewById(R.id.text);
			text.setText("Dieses Item ist ein wirklich praktisches Item");
			ImageView image = (ImageView) dialog.findViewById(R.id.image);
			image.setImageResource(imageId[+ position]);
			
			LinearLayout buttonContainer = (LinearLayout)dialog.findViewById(R.id.buttonContainer);
			
			for(String s : buttonList){
				Button b = new Button(getActivity());
				b.setText(s);
				b.setBackgroundColor(Color.WHITE);
				b.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						MainActivity activity = (MainActivity) getActivity();
						activity.interpreterCom.useItem(itemList.get(itemPosition), "use");
						
					}
				});
				
				//b.setLayoutParams(new ViewGroup.LayoutParams(150, 60));
//				LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
//				but_action.setLayoutParams(lp);
				buttonContainer.addView(b);
			}	
		 
			Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonBack);
			// if button is clicked, close the custom dialog
			dialogButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		 
			dialog.show();
		  }
}
