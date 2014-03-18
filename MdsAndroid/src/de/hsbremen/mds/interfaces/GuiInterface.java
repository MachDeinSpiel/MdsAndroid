package de.hsbremen.mds.interfaces;

import de.hsbremen.mds.listener.AndroidListener;
import de.hsbremen.mds.valueobjects.MdsImage;
import de.hsbremen.mds.valueobjects.MdsMap;
import de.hsbremen.mds.valueobjects.MdsText;
import de.hsbremen.mds.valueobjects.MdsVideo;

public interface GuiInterface {
        void setAndroidListener(AndroidListener listener, double positionsIntervall);
        void nextFragment(MdsImage mds);
        void nextFragment(MdsVideo mds);
        void nextFragment(MdsText mds);
        void nextFragment(MdsMap mds);
        
        void update();
}