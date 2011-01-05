package net.isammoc.hexapod.communication;

import net.isammoc.hexapod.HexapodArticulation;
import net.isammoc.hexapod.HexapodLeg;
import net.isammoc.hexapod.HexapodServo;

public class LegMessage extends ServoMessage {
	/**
	 * Set the byte corresponding to the specified {@code leg} and
	 * {@code articulation}
	 * 
	 * @param leg
	 * @param articulation
	 * @param value
	 *            a byte not equals to neither 0x00 nor 0xFF
	 * @throws IllegalArgumentException
	 *             If {@code value} is either 0x00 or 0xFF.
	 * @throws NullPointerException
	 *             If either {@code leg} or {@code articulation} is
	 *             <code>null</code>.
	 */
	public void setByte(final HexapodLeg leg,
			final HexapodArticulation articulation, final byte value)
			throws IllegalArgumentException {
		if (leg == null) {
			throw new NullPointerException("leg must not be null");
		}
		if (articulation == null) {
			throw new NullPointerException("articulation must not be null");
		}
		this.setByte(HexapodServo.fromLegArticulation(leg, articulation)
				.ordinal(), value);
	}

	public void setUnsignedByte(final HexapodLeg leg,
			final HexapodArticulation articulation, final int value) {
		this.setUnsignedByte(HexapodServo
				.fromLegArticulation(leg, articulation).ordinal(), value);
	}

	public void setBytes(final HexapodLeg leg, final byte shoulder,
			final byte elbow, final byte wrist) {
		setByte(leg, HexapodArticulation.SHOULDER, shoulder);
		setByte(leg, HexapodArticulation.ELBOW, elbow);
		setByte(leg, HexapodArticulation.WRIST, wrist);
	}

	public void setUnsignedBytes(final HexapodLeg leg, final int shoulder,
			final int elbow, final int wrist) {
		setUnsignedByte(leg, HexapodArticulation.SHOULDER, shoulder);
		setUnsignedByte(leg, HexapodArticulation.ELBOW, elbow);
		setUnsignedByte(leg, HexapodArticulation.WRIST, wrist);
	}
}
