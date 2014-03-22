package de.hsbremen.mds.interpreter.exceptions;

public class NoStartStateExcetpion extends Exception {
	
	public NoStartStateExcetpion(){
		super("kein startstate gefunden");
	}
}
