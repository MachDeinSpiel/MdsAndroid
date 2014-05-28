package de.hsbremen.mds.android.ingame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import de.hsbremen.mds.android.fragment.FragmentBackpack;
import de.hsbremen.mds.android.fragment.FragmentImage;
import de.hsbremen.mds.android.fragment.FragmentLocation;
import de.hsbremen.mds.android.fragment.FragmentMinigame;
import de.hsbremen.mds.android.fragment.FragmentMonitoring;
import de.hsbremen.mds.android.fragment.FragmentStart;
import de.hsbremen.mds.android.fragment.FragmentText;
import de.hsbremen.mds.android.fragment.FragmentVideo;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsInfoObject;

public class SwipeAdapter extends FragmentPagerAdapter{

	private HashMap<String, Fragment> activeFragmentsList = new HashMap<String, Fragment>();
	private List<String> activeFragmentsNumbers = new ArrayList<String>();

	private HashMap<String, Fragment> fragmentsPoolList = new HashMap<String, Fragment>();
	
	private FragmentManager fm;
	
	private MdsInfoObject fragmentInfo;

	public SwipeAdapter(FragmentManager fm) {
		super(fm);
		this.fm = fm;
		initFragments();
	}

	private void initFragments() {	
        FragmentStart startFragment = new FragmentStart();
        activeFragmentsList.put("start", startFragment);
        activeFragmentsNumbers.add("start");
        
        FragmentLocation locationFragment = new FragmentLocation();
        activeFragmentsList.put("location", locationFragment);
        activeFragmentsNumbers.add("location");
        
        FragmentBackpack backpackFragment = new FragmentBackpack();
        backpackFragment.addItem(new MdsItem("bomb", "bomb"));
        activeFragmentsList.put("backpack", backpackFragment);
        activeFragmentsNumbers.add("backpack");
        
        FragmentMonitoring monitoringFragment = new FragmentMonitoring();
        activeFragmentsList.put("monitoring", monitoringFragment);
        activeFragmentsNumbers.add("monitoring");
        
        //INAKTIVE FRAGMENTS
        FragmentText textFragment = new FragmentText();
        textFragment.setSwipeAdapter(this);
        textFragment.setMessage("Es wurde noch kein Ziel erreicht");
        fragmentsPoolList.put("showText", textFragment);
        
        FragmentImage imageFragment = new FragmentImage();
        imageFragment.setSwipeAdapter(this);
        fragmentsPoolList.put("showImage", imageFragment);

        FragmentVideo videoFragment = new FragmentVideo();
        videoFragment.setSwipeAdapter(this);
        fragmentsPoolList.put("showVideo", videoFragment);	
        
        FragmentMinigame minigameFragment = new FragmentMinigame();
        fragmentsPoolList.put("showMinigame", minigameFragment);	
	}
	
	public void removeFragment(String fragmentName){   
		
      FragmentTransaction trans = fm.beginTransaction();
      trans.remove(activeFragmentsList.get(fragmentName));
      trans.commit();   
      fm.executePendingTransactions();
        
        activeFragmentsList.remove(fragmentName);
        activeFragmentsNumbers.remove(fragmentName);
        
        notifyDataSetChanged();
	}	
	
	public void addFragment(String fragmentName){
		
		Fragment newFragment = this.fragmentsPoolList.get(fragmentName);
		this.activeFragmentsList.put(fragmentName, newFragment);
		this.activeFragmentsNumbers.add(fragmentName);
		
		notifyDataSetChanged();
	}
	
	@Override
	public Fragment getItem(int index) {
		return activeFragmentsList.get(activeFragmentsNumbers.get(index));
	}

	@Override
	public int getCount() {
		return activeFragmentsList.size();
	}

	public Fragment getFragment(String name){
		return activeFragmentsList.get(name);
	}
	
	public int getFragmentName(String name){
		int index = 0;
		for(String s : activeFragmentsNumbers){
			if(s.equals(name)){
				return index;
			}
			index++;
		}
		System.out.println(index);
		return index;
	}

	public void setFragmentInformation(MdsInfoObject mds) {
		this.fragmentInfo = mds;
	}
	
	public MdsInfoObject getFragmentInformation(){
		return this.fragmentInfo;
	}
}
