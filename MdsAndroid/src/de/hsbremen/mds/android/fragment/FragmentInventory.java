package de.hsbremen.mds.android.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;
import de.hsbremen.mds.android.ingame.CustomGrid;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.mdsandroid.R;

public class FragmentInventory extends Fragment{

	private View inventoryView;
	private ArrayList<MdsItem> itemList = new ArrayList<MdsItem>();
	private ArrayList<String> itemAsStringList = new ArrayList<String>();
	private BaseAdapter adapter;
	private GridView lv;
	
	  GridView grid;
	  List<String> buttonList = new ArrayList<String>();
	  String[] web = {
	        "Potion1nomaniacatatatat",
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
                toggleItemView(web[+ position], true); 
            }
	     });
	        
	     buttonList.add("Benutzen");
	     buttonList.add("Entfernen");
	     
	     return inventoryView;
	  }
	  
	  @Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
	}
	  
	  private void toggleItemView(String itemName, boolean itemViewVisible){
		  
			LinearLayout l = (LinearLayout) getActivity().findViewById(R.id.itemView);
			LinearLayout l2 = (LinearLayout) getActivity().findViewById(R.id.itemInfoContainer);
			TextView name = (TextView) getActivity().findViewById(R.id.itemName);
			ImageView image = (ImageView) getActivity().findViewById(R.id.imageView1);
//
//
			if (itemViewVisible) {
				l.setLayoutParams(new TableLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, 0, 0.5f));
				l.setOrientation(LinearLayout.HORIZONTAL);
				l2.setLayoutParams(new TableLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, 0, 1f));
				name.setText(itemName);
				image.setBackgroundResource(R.drawable.potion);
				showActionButtons();
			} else {
				l.setLayoutParams(new TableLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, 0, 0f));
				
				l2.setLayoutParams(new TableLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, 0, 1f));
			}
		  
	  }
	  
	  private void showActionButtons(){
		  LinearLayout bc1 = (LinearLayout)getActivity().findViewById(R.id.buttonContainer1);
		  LinearLayout bc2 = (LinearLayout)getActivity().findViewById(R.id.buttonContainer2);
		  
		  for(String b : buttonList){
			  Button btn = new Button(getActivity());
			  btn.setText(b);
			  bc1.addView(btn);
		  }
		  
		  Button btn = new Button(getActivity());
		  btn.setText("back");
		  bc2.addView(btn);
		  btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggleItemView("", false);
			}
		});
		  
		  
	  }
	
}
