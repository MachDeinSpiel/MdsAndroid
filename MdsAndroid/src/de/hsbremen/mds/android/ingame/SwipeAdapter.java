package de.hsbremen.mds.android.ingame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;
import de.hsbremen.mds.android.fragment.FragmentGamePipe;
import de.hsbremen.mds.android.fragment.FragmentGameReaction;
import de.hsbremen.mds.android.fragment.FragmentImage;
import de.hsbremen.mds.android.fragment.FragmentInventory;
import de.hsbremen.mds.android.fragment.FragmentMap;
import de.hsbremen.mds.android.fragment.FragmentText;
import de.hsbremen.mds.android.fragment.FragmentVideo;
import de.hsbremen.mds.common.valueobjects.statemachine.MdsInfoObject;

public class SwipeAdapter extends FragmentPagerAdapter{

	private HashMap<String, Fragment> activeFragmentsList = new HashMap<String, Fragment>();
	private List<String> activeFragmentsNumbers = new ArrayList<String>();

	private HashMap<String, Fragment> fragmentsPoolList = new HashMap<String, Fragment>();
	
	private MdsInfoObject fragmentInfo;
	
	public boolean getItemPositionStandard = true;
	private int fragmentsCount = 0;
	private boolean skip = false;
	
	private FragmentManager fm;

	public SwipeAdapter(FragmentManager fm) {

		super(fm);
		this.fm = fm;
		initFragments();
	}

	private void initFragments() {	
        
		/*----AKTIV FRAGMENTS----*/
		// Init Standard Fragments
        FragmentMap locationFragment = new FragmentMap();
        activeFragmentsList.put("showMap", locationFragment);
        activeFragmentsNumbers.add("showMap");
        
        FragmentInventory inventoryFragment = new FragmentInventory();
        activeFragmentsList.put("inventory", inventoryFragment);
        activeFragmentsNumbers.add("inventory");
        
        /*----INAKTIVE FRAGMENTS----*/
        // Init Dialog Fragments
        FragmentText textFragment = new FragmentText();
        textFragment.setSwipeAdapter(this);
        fragmentsPoolList.put("showText", textFragment);
        
        FragmentImage imageFragment = new FragmentImage();
        imageFragment.setSwipeAdapter(this);
        fragmentsPoolList.put("showImage", imageFragment);

        FragmentVideo videoFragment = new FragmentVideo();
        videoFragment.setSwipeAdapter(this);
        fragmentsPoolList.put("showVideo", videoFragment);	
        
        // Init Minigames
        FragmentGameReaction reactionGameFragment = new FragmentGameReaction();
        fragmentsPoolList.put("Puzzle", reactionGameFragment);
        
//        FragmentGamePipe pipeGameFragment = new FragmentGamePipe();
//        fragmentsPoolList.put("Puzzle", pipeGameFragment);
	}
	
	public void removeFragment(String fragmentName){   
		
		// Special behaviour for getItemPosition()
		getItemPositionStandard = false;
		
        notifyDataSetChanged();
		
        activeFragmentsList.remove(fragmentName);
        activeFragmentsNumbers.remove(fragmentName);
        
		fm.beginTransaction().remove(fragmentsPoolList.get(fragmentName)).commit();
		
        notifyDataSetChanged();
        
		// Set Behaviour for getItemPosition() to standard again
        getItemPositionStandard = true;
	}	
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		Log.i("nextFragment", "Object " + object.getClass() + " zerstört");
		super.destroyItem(container, position, object);
	}
	
	public void addFragment(String fragmentName){
		
		Fragment newFragment = this.fragmentsPoolList.get(fragmentName);
		
		this.activeFragmentsList.put(fragmentName, newFragment);
		this.activeFragmentsNumbers.add(fragmentName);
		getItemPositionStandard = false;
		notifyDataSetChanged();
		getItemPositionStandard = true;
	}
	
	@Override
	public Fragment getItem(int index) {
		Log.i("nextFragment", "Fragment " + activeFragmentsNumbers.get(index) + " wurde mit getItem");
		return activeFragmentsList.get(activeFragmentsNumbers.get(index));
	}
	
	@Override
	public int getItemPosition(Object object) {
		
		if(getItemPositionStandard){
			fragmentsCount = 0;
			return super.getItemPosition(object);
		}
		
		if(fragmentsCount < 2 && !skip){
			fragmentsCount++;
			if(fragmentsCount == 2){
				fragmentsCount = 0;
				skip = true;
			}
			return super.getItemPosition(object);
		}else {
			skip = false;
//			Log.i("nextFragment", "Postion none");
			return POSITION_NONE;
		}

			
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
		return index;
	}

	public void setFragmentInformation(MdsInfoObject mds) {
		this.fragmentInfo = mds;
	}
	
	public MdsInfoObject getFragmentInformation(){
		return this.fragmentInfo;
	}
}
