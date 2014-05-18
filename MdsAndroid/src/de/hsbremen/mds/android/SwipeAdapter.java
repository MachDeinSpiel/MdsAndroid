package de.hsbremen.mds.android;

import java.util.ArrayList;

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

	private ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
	
	public SwipeAdapter(FragmentManager fm) {
		super(fm);
		initFragments();
	}

	private void initFragments() {	
        FragmentStart startFragment = new FragmentStart();
        fragmentList.add(startFragment);
        
        FragmentLocation mapFragment = new FragmentLocation();
        fragmentList.add(mapFragment);
        
        FragmentBackpack backpackFragment = new FragmentBackpack();
        backpackFragment.addItem(new MdsItem("bomb", "bomb"));
        fragmentList.add(backpackFragment);
        
        FragmentText textFragment = new FragmentText();
        fragmentList.add(textFragment);
        
        FragmentMonitoring monitoringFragment = new FragmentMonitoring();
        fragmentList.add(monitoringFragment);
        
        FragmentImage imageFragment = new FragmentImage();
        fragmentList.add(imageFragment);
        
        FragmentVideo videoFragment = new FragmentVideo();
        fragmentList.add(videoFragment);		
	}
	

	@Override
	public Fragment getItem(int index) {		
		return fragmentList.get(index);
	}

	@Override
	public int getCount() {
		return fragmentList.size();
	}

	public Fragment getFragment(int index){
		return fragmentList.get(index);
	}
}
