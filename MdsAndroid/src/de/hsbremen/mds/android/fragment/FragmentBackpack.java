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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import de.hsbremen.mds.android.MainActivity;
import de.hsbremen.mds.android.ServerClientConnector;
import de.hsbremen.mds.common.valueobjects.MdsItem;
import de.hsbremen.mds.mdsandroid.R;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class FragmentBackpack extends Fragment {

	ArrayList<MdsItem> itemList = new ArrayList<MdsItem>();
	ArrayList<String> itemAsStringList = new ArrayList<String>();
	BaseAdapter adapter;
	ListView lv;
	
	public FragmentBackpack() {
	}
	
	public FragmentBackpack(ArrayList<MdsItem> itemList) {
		this.itemList=itemList;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// TODO Hier gecleared
		itemList.clear();
		itemList.add(new MdsItem("Rotten-Kiwi","rottenkiwi"));
		itemList.add(new MdsItem("M4A1","m4a1"));
		itemList.add(new MdsItem("Burlap-Sack","burlapsack"));
		itemList.add(new MdsItem("Fire-Axe","fireaxe"));
		itemList.add(new MdsItem("Banana","banana"));
		
		final View view = inflater.inflate(R.layout.fragment_backpack, container,
				false);
		
		// TODO Hier gecleared
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
				
				ImageView imageView = (ImageView) getActivity().findViewById(R.id.imageItem);
				imageView.setImageResource(resId);
			}
			
		});
		
		adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, itemAsStringList);
		lv.setAdapter(adapter);
		registerForContextMenu(lv);
		
		return view;
	}
	
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);

		menu.add("Benutzen");
		menu.add("Ablegen");
		menu.add("Essen");
	}
	

	@Override
	public boolean onContextItemSelected(MenuItem item){
		super.onContextItemSelected(item);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		if(item.getTitle()=="Benutzen"){
			Toast.makeText(getActivity(), itemList.get(info.position).getName()+" benutzt!", Toast.LENGTH_LONG).show();
			itemList.remove(info.position);
			itemAsStringList.remove(info.position);
			
			String jsonItem = "{Name:Apple}";
			ServerClientConnector c = ((MainActivity)getActivity()).connector;
			c.httpPost("item",jsonItem, "/mds/item");
			
			String jsonPlayer = "{Name:Julian}";
			c.httpPost("player",jsonPlayer, "/mds/player");
		}
		
		if(item.getTitle()=="Ablegen"){
			Toast.makeText(getActivity(), "Du hast "+itemList.get(info.position).getName()+" abgelegt!", Toast.LENGTH_LONG).show();
			itemList.remove(info.position);
			itemAsStringList.remove(info.position);
			
			ServerClientConnector c = ((MainActivity)getActivity()).connector;
			String s = c.httpGetString("/mds/item/0");
			Log.d("Na", s);
			
			String s1 = c.httpGetString("/mds/item/1");
			Log.d("Na", s1);
			
			String s2 = c.httpGetString("/mds/item/2");
			Log.d("Na", s2);
			
			String s3 = c.httpGetString("/mds/item/3");
			Log.d("Na", s3);
			
			String s4 = c.httpGetString("/mds/item/4");
			Log.d("Na", s4);
			
			String s5 = c.httpGetString("/mds/item/5");
			Log.d("Na", s5);
			
			String s6 = c.httpGetString("/mds/item/6");
			Log.d("Na", s6);
			
			String s7 = c.httpGetString("/mds/item/7");
			Log.d("Na", s7);
			
			String s8 = c.httpGetString("/mds/player/0");
			Log.d("Na", s8);
			
			String s9 = c.httpGetString("/mds/player/1");
			Log.d("Na", s9);
			
			String s10 = c.httpGetString("/mds/player/2");
			Log.d("Na", s10);
			
			String s11 = c.httpGetString("/mds/player/3");
			Log.d("Na", s11);
		}

		if(item.getTitle()=="Essen"){
			Toast.makeText(getActivity(), "Du hast "+itemList.get(info.position).getName()+" weggeschmatzt!", Toast.LENGTH_LONG).show();
			itemList.remove(info.position);
			itemAsStringList.remove(info.position);
		}
		
		adapter.notifyDataSetChanged();

		ImageView imageView = (ImageView) getActivity().findViewById(R.id.imageItem);
		imageView.setImageResource(R.drawable.backpack);
		
		return super.onContextItemSelected(item);
	}
	
}
