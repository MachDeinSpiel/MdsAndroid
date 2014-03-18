package de.hsbremen.mds.interpreter.guiperformer;

import java.util.HashMap;

import de.hsbremen.mds.interfaces.GuiInterface;
import de.hsbremen.mds.valueobjects.MdsMap;

public class MapPerformer extends GuiPerformer {

	@Override
	public void execute(GuiInterface gui, HashMap<String, String> params) {
		MdsMap map = new MdsMap(null, null, null);
		//TODO: sinnvolle Parameter
		gui.nextFragment(map);

	}

}
