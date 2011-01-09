package net.isammoc.hexapod.communication;

/**
 * Common Interface for messages to send to the Hexapod.
 * 
 * @author Isammoc
 */
public interface IHexapodMessage {

	/**
	 * Return a byte array 21 length. None value of this array must be 0x00 nor
	 * 0xff.
	 * 
	 * @return a byte array 21 length.
	 */
	byte[] toByteArray();
}
