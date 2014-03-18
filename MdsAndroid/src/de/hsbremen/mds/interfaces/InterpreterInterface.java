package de.hsbremen.mds.interfaces;

import de.hsbremen.mds.valueobjects.statemachine.MdsObjectContainer;

public interface InterpreterInterface {

	void pushParsedObjects(MdsObjectContainer objectContainer);
	
}
