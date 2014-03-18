package de.hsbremen.mds.valueobjects.statemachine;

import java.util.HashMap;

public class MdsTrigger {
	
	private HashMap<String, MdsAttribute> attributes;
	
	public MdsTrigger() {
		attributes = new HashMap<String, MdsAttribute>();
	}
	

	public void setAttribute(MdsAttribute attribute, String name) {
		this.attributes.put(name, attribute);
	}
	
	public MdsAttribute getAttribute(String name){
		return this.attributes.get(name);
	}
	

}
