package net.isammoc.hexapod;

public class HexapodException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HexapodException(String message) {
		super(message);
	}

	public HexapodException(String message, Throwable cause) {
		super(message, cause);
	}

}
