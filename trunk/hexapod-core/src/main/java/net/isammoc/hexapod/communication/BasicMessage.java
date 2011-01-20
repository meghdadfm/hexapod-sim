package net.isammoc.hexapod.communication;

import java.util.Arrays;

/**
 * Basic message to send to the Hexapod.
 * 
 * @author Isammoc
 */
public class BasicMessage implements IHexapodMessage {
	/** Messages are 21 length. */
	public static final int MESSAGE_LENGTH = 21;

	/** Current message. */
	private final byte[] message;

	/**
	 * Check that specified {@code b} is not forbidden values (0x00, 0xff).
	 * 
	 * @param b
	 *            The byte to check.
	 * @throws IllegalArgumentException
	 *             In case of incorrect byte provided.
	 */
	protected static void checkByte(final byte b)
			throws IllegalArgumentException {
		if ((b == 0x00) || (b == (byte) 0xff)) {
			throw new IllegalArgumentException(String.format("value must NOT be 0x00 or 0xff but got %x", b));
		}
	}

	/**
	 * Default constructor.
	 */
	public BasicMessage() {
		this.message = new byte[21];
		Arrays.fill(this.message, Byte.MAX_VALUE);
	}

	/**
	 * Constructor from an existing bytes
	 * 
	 * @param bytes
	 * @throws IllegalArgumentException
	 */
	protected BasicMessage(final byte[] bytes)
			throws IllegalArgumentException {
		if (bytes.length != MESSAGE_LENGTH) {
			throw new IllegalArgumentException("bytes must be " + MESSAGE_LENGTH + " length");
		}
		for (int i = 0; i < MESSAGE_LENGTH; i++) {
			checkByte(bytes[i]);
		}
		this.message = Arrays.copyOf(bytes, MESSAGE_LENGTH);
	}

	/**
	 * Transform the actual instance to a byte array. Used to send the message.
	 * 
	 * @return the byte array value corresponding.
	 */
	@Override
	public byte[] toByteArray() {
		return Arrays.copyOf(this.message, this.message.length);
	}

	public void setByte(final int index, final byte value) {
		if ((index < 0) || (index >= MESSAGE_LENGTH)) {
			throw new IndexOutOfBoundsException("index must be comprised between 0 and "
					+ (MESSAGE_LENGTH - 1) + " (inclusive)");
		}
		checkByte(value);

		this.message[index] = value;
	}

	public byte getByte(final int index) {
		return this.message[index];
	}

	public void setUnsignedByte(final int index, final int value)
			throws IllegalArgumentException, IndexOutOfBoundsException {
		this.message[index] = HexapodBasicInterface.unsignedByteToByte(value);
	}

	public int getUnsignedByte(final int index) {
		return HexapodBasicInterface.byteToUnsignedByte(this.message[index]);
	}

}
