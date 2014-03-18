package de.hsbremen.mds.interpreter.guiperformer;

import java.util.HashMap;

import de.hsbremen.mds.interfaces.GuiInterface;
import de.hsbremen.mds.valueobjects.MdsVideo;

public class VideoPerformer extends GuiPerformer {

	@Override
	public void execute(GuiInterface gui, HashMap<String, String> params) {
		MdsVideo vid = new MdsVideo(params.get("name"), params.get("url"), params.get("text"));
		gui.nextFragment(vid);

	}

}
