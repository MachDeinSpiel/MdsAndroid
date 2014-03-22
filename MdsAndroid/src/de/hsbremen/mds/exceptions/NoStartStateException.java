package de.hsbremen.mds.exceptions;

public class NoStartStateException extends Exception {
	
	public NoStartStateException(){
		super("kein startstate gefunden");
	}
}
