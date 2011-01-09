package net.isammoc.hexapod;

import java.util.Map;

import net.isammoc.hexapod.communication.LegMessage;

import com.jme3.math.FastMath;

public class HexapodConverter {
	private static final float STEP = FastMath.PI / 256;
	private final Map<HexapodLeg, Map<HexapodArticulation, Float>> wantedAngles;

	public HexapodConverter(final Map<HexapodLeg, Map<HexapodArticulation, Float>> wantedAngles) {
		this.wantedAngles = wantedAngles;
	}

	public void setMessage(final LegMessage msg) {
		for (final HexapodLeg leg : HexapodLeg.values()) {
			for (final HexapodArticulation articulation : HexapodArticulation.values()) {
				this.setValue(leg, articulation, msg.getUnsignedByte(leg, articulation));
			}
		}
	}

	public void setValue(final HexapodLeg leg, final HexapodArticulation articulation, final int value) {
		final float var;
		final float sens;
		switch (articulation) {
			case SHOULDER:
				var = 127 * STEP;
				sens = -1;
				break;
			case ELBOW:
				var = 226 * STEP - FastMath.HALF_PI;
				sens = -1;
				break;
			case WRIST:
				var = -71 * STEP;
				sens = 1;
				break;
			default:
				var = 0;
				sens = 1;
		}
		this.wantedAngles.get(leg).put(articulation, sens * value * STEP + var);
	}
}
