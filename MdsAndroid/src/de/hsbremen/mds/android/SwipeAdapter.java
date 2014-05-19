package de.hsbremen.mds.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.hsbremen.mds.android.fragment.FragmentBackpack;
import de.hsbremen.mds.android.fragment.FragmentImage;
import de.hsbremen.mds.android.fragment.FragmentLocation;
import de.hsbremen.mds.android.fragment.FragmentMonitoring;
import de.hsbremen.mds.android.fragment.FragmentStart;
import de.hsbremen.mds.android.fragment.FragmentText;
import de.hsbremen.mds.android.fragment.FragmentVideo;
import de.hsbremen.mds.common.guiobjects.MdsItem;

public class SwipeAdapter extends FragmentPagerAdapter{


	private HashMap<String, Fragment> fragmentList = new HashMap<String, Fragment>();
	private List<String> fragmentNumber = new ArrayList<String>();

	public SwipeAdapter(FragmentManager fm) {
		super(fm);
		initFragments();
	}

	private void initFragments() {	
        FragmentStart startFragment = new FragmentStart();
        fragmentList.put("start", startFragment);
        fragmentNumber.add("start");
        
        FragmentLocation locationFragment = new FragmentLocation();
        fragmentList.put("location", locationFragment);
        fragmentNumber.add("location");
        
        FragmentBackpack backpackFragment = new FragmentBackpack();
        backpackFragment.addItem(new MdsItem("bomb", "bomb"));
        fragmentList.put("backpack", backpackFragment);
        fragmentNumber.add("backpack");
        
        FragmentText textFragment = new FragmentText();
        textFragment.setMessage("Es wurde noch kein Ziel erreicht");
        fragmentList.put("text", textFragment);
        fragmentNumber.add("text");
        
        FragmentMonitoring monitoringFragment = new FragmentMonitoring();
        fragmentList.put("monitoring", monitoringFragment);
        fragmentNumber.add("monitoring");
        
        FragmentImage imageFragment = new FragmentImage();
        fragmentList.put("image", imageFragment);
        fragmentNumber.add("image");
        
        FragmentVideo videoFragment = new FragmentVideo();
        fragmentList.put("video", videoFragment);	
        fragmentNumber.add("video");
	}
	
	@Override
	public Fragment getItem(int index) {
		return fragmentList.get(fragmentNumber.get(index));
	}

	@Override
	public int getCount() {
		return fragmentList.size();
	}

	public Fragment getFragment(String name){
		return fragmentList.get(name);
	}
	
	public int getFragmentName(String name){
		int index = 0;
		for(String s : fragmentNumber){
			if(s.equals(name)){
				return index;
			}
			index++;
		}
		return index;
	}
}
