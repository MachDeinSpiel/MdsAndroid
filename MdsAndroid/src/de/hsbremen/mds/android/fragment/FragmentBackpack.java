package de.hsbremen.mds.android.fragment;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.hsbremen.mds.android.ingame.MainActivity;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.mdsandroid.R;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class FragmentBackpack extends Fragment {

	private ArrayList<MdsItem> itemList = new ArrayList<MdsItem>();
	private ArrayList<String> itemAsStringList = new ArrayList<String>();
	private BaseAdapter adapter;
	private ListView lv;
	private int style;
	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.fragment_backpack, container, false);
		
		updateList();
		
		// Style des Fragments anpassen
		MainActivity a = (MainActivity)getActivity();
		style = a.getStyleNumber();
		styleFragment(view);
		
		return view;
	}
	
	private void styleFragment(View view) {
		
		TextView t1 = (TextView)view.findViewById(R.id.labelBackpack);
		
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
	
	@Override
	public void onResume() {
		updateList();
		Log.i("Mistake", "Itemlist size = : " + itemList.size());
		super.onResume();
	}

	private void updateList() {
		itemAsStringList.clear();
		for (MdsItem item : itemList) {
			itemAsStringList.add(item.getName());
		}
		
		lv = (ListView) view.findViewById(R.id.itemList);
		
		lv.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {

				String currentItem = itemList.get(pos).getImagePath();
				
				int resId = getResources().getIdentifier(currentItem, "drawable", getActivity().getPackageName());
				
//				ImageView imageView = (ImageView) getActivity().findViewById(R.id.imageItem);
//				imageView.setImageResource(resId);
			}
		});
		
		Log.i("Mistake","Size der List in update ist: " + itemAsStringList.size());
		
		adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, itemAsStringList);
		lv.setAdapter(adapter);
		registerForContextMenu(lv);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add("Benutzen");
		menu.add("Entfernen");
	}
	

	@Override
	public boolean onContextItemSelected(MenuItem item){
		super.onContextItemSelected(item);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		if(item.getTitle()=="Benutzen"){
			Toast.makeText(getActivity(), itemList.get(info.position).getName()+" benutzt!", Toast.LENGTH_LONG).show();
			
			// Item benutzen an Interpreter schicken
			MainActivity activity = (MainActivity) getActivity();
			activity.interpreterCom.useItem(itemList.get(info.position), "use");
			itemList.remove(info.position);
			itemAsStringList.remove(info.position);
		}
		
		if(item.getTitle()=="Entfernen"){
			Toast.makeText(getActivity(), "Du hast "+itemList.get(info.position).getName()+" entfernt!", Toast.LENGTH_LONG).show();
			MainActivity activity = (MainActivity) getActivity();
			activity.interpreterCom.useItem(itemList.get(info.position), "remove");
			itemList.remove(info.position);
			itemAsStringList.remove(info.position);
		}
		
		adapter.notifyDataSetChanged();

//		ImageView imageView = (ImageView) getActivity().findViewById(R.id.imageItem);
//		imageView.setImageResource(R.drawable.backpack);
		
		return super.onContextItemSelected(item);
	}
	
	public void addItem(MdsItem item){
		itemList.add(item);
		updateList();
	}
	
	public void removeItem(MdsItem item){
		itemList.remove(findItemInList(item));
		updateList();
	}
	
	private int findItemInList(MdsItem item){
		int itemIndex = 0;
		for(MdsItem i : itemList){
			if(item.getPathKey().equals(i.getPathKey())){
				return itemIndex;
			}
			itemIndex++;
		}
		return itemIndex;
	}
	
}
