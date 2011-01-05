package net.isammoc.hexapod.communication;

public class MessageFactory {

	/**
	 * Factory from a byte array.
	 * 
	 * @param bytes
	 *            byte array 21 length where all values are not 0x00 nor 0xFF.
	 * @return a new instance of {@link BasicMessage}.
	 * @throws IllegalArgumentException
	 *             If {@code bytes} is not eligible.
	 */
	public static BasicMessage newMessage(final byte[] bytes)
			throws IllegalArgumentException {
		return new BasicMessage(bytes);
	}

	/**
	 * Creates a new instance of Message from int array.
	 * 
	 * @param bytes
	 *            int array where all values are between 1 and 254 (inclusive).
	 * @return a new instance of Message
	 * @throws IllegalArgumentException
	 *             If {@code bytes} is not eligible.
	 */
	public static BasicMessage newMessage(final int[] bytes)
			throws IllegalArgumentException {
		if (bytes.length != BasicMessage.MESSAGE_LENGTH) {
			throw new IllegalArgumentException("bytes must be "
					+ BasicMessage.MESSAGE_LENGTH + " length");
		}
		final byte[] tmpMsg = new byte[BasicMessage.MESSAGE_LENGTH];
		for (int i = 0; i < BasicMessage.MESSAGE_LENGTH; i++) {
			final int b = bytes[i];
			if ((b < 1) || (b > 254)) {
				throw new IllegalArgumentException(
						"values must be byte values between 1 and 254. Found "
								+ b + " at index " + i);
			}
			tmpMsg[i] = (byte) bytes[i];
		}

		return new BasicMessage(tmpMsg);
	}
}
