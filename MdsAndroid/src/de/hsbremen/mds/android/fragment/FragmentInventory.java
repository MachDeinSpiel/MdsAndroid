package de.hsbremen.mds.android.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.hsbremen.mds.android.ingame.CustomGrid;
import de.hsbremen.mds.android.ingame.ImageLoader;
import de.hsbremen.mds.android.ingame.MainActivity;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.mdsandroid.R;

public class FragmentInventory extends Fragment{

	private View inventoryView;
	private ArrayList<MdsItem> itemList = new ArrayList<MdsItem>();
	private CustomGrid adapter;
	private int itemPosition;
	private int style;
	
	private GridView grid;
	private List<String> buttonList = new ArrayList<String>();
	private ImageLoader imageLoader;
	  
	  @Override
	  public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) {
	    
		 inventoryView = inflater.inflate(R.layout.fragment_inventorynew, container, false);
		 
		 imageLoader = new ImageLoader();
		 
	     updateItemlist();
	     
	     // Clear buttonList
	     buttonList.clear();
	     buttonList.add("use");
	     buttonList.add("drop");
	     buttonList.add("remove");
	     
		// Style des Fragments anpassen
		MainActivity a = (MainActivity)getActivity();
		style = a.getStyleNumber();
		styleFragment(inventoryView);
		
	     
	     return inventoryView;
	  }	  
	  
	  private void updateItemlist() {
		
		adapter = new CustomGrid(getActivity(), itemList);
		grid=(GridView)inventoryView.findViewById(R.id.grid);
	    grid.setAdapter(adapter);
	    grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view,
                                   int position, long id) {                    	
               showItemDialog(position); 
           }
	     });
	}

	private void showItemDialog(int position){
			// custom dialog
			final Dialog dialog = new Dialog(getActivity());
			dialog.setContentView(R.layout.custom);
			dialog.setTitle(itemList.get(position).getName());
		 
			// set the custom dialog components - text, image and button
			TextView text = (TextView) dialog.findViewById(R.id.text);
			text.setText("Item: " + itemList.get(position).getName() + "\n" + " Hier sind folgende Aktionen möglich:");
			ImageView image = (ImageView) dialog.findViewById(R.id.image);
			Bitmap icon = imageLoader.getBitmapFromURL(itemList.get(position).getImagePath());
			image.setImageBitmap(icon);
			LinearLayout buttonContainer = (LinearLayout)dialog.findViewById(R.id.buttonContainer);
			
			// Clear buttoncontainer
			buttonContainer.removeAllViews();
			
			for(String s : buttonList){
				
				Button b = new Button(getActivity());
				b.setText(s);
				b.setBackgroundResource(R.drawable.buttonshape_dialog);
				b.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						MainActivity activity = (MainActivity) getActivity();
						Button b = (Button)v;
						System.out.println("buttontext : " + b.getText().toString());
						activity.interpreterCom.useItem(itemList.get(itemPosition), b.getText().toString());
						itemList.remove(itemPosition);
						updateItemlist();
						dialog.dismiss();
					}
				});
				
				buttonContainer.addView(b);
			}	
		 
			Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonBack);
			dialogButton.setBackgroundResource(R.drawable.buttonshape);
			// if button is clicked, close the custom dialog
			dialogButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		 
			dialog.show();
	}
	
	
	private void styleFragment(View view) {
		
		TextView t1 = (TextView)view.findViewById(R.id.labelInventory);
		
		int styleText = 0;
		int styleLabel = 0;
		int styleLabelBgr = 0;
		
		switch(style){
			case 0:
				styleText = R.style.textColorDefaultBlue;
				styleLabel = R.style.labelDefault;
				styleLabelBgr = R.drawable.labelshape;
				break;
			case 1:
				styleText = R.style.textColorDarkBlue;
				styleLabel = R.style.labelDark;
				styleLabelBgr = R.drawable.labelshapedark;
				break;
		}
		
		t1.setTextAppearance(getActivity(), styleLabel);
		t1.setBackgroundResource(styleLabelBgr);
	}
	
	public void addItem(MdsItem item){
		itemList.add(item);
		updateItemlist();
	}
	
	public void removeItem(String itemPathKey){
		itemList.remove(findItemInList(itemPathKey));
		updateItemlist();
	}
	
	private int findItemInList(String itemPathKey){
		int itemIndex = 0;
		for(MdsItem i : itemList){
			if(itemPathKey.equals(i.getPathKey())){
				return itemIndex;
			}
			itemIndex++;
		}
		return itemIndex;
	}
}
