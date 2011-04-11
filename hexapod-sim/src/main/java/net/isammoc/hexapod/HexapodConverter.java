package net.isammoc.hexapod;

import net.isammoc.hexapod.communication.LegMessage;

import com.jme3.math.FastMath;

public class HexapodConverter {
	private static final float STEP = FastMath.PI / 256;
	private final WantedAnglesAware hexapod;

	public HexapodConverter(final WantedAnglesAware hexapod) {
		this.hexapod = hexapod;
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
		final int realValue;
		switch (leg) {
			case RIGHT_FRONT:
			case RIGHT_MIDDLE:
			case RIGHT_REAR:
				realValue = value;
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
				break;
			case LEFT_FRONT:
			case LEFT_MIDDLE:
			case LEFT_REAR:
			default:
				realValue = 254 - value;
				switch (articulation) {
					case SHOULDER:
						var = 127 * STEP + FastMath.PI;
						sens = 1;
						break;
					case ELBOW:
						var = 226 * STEP - FastMath.HALF_PI;
						sens = -1;
						break;
					case WRIST:
					default:
						var = -71 * STEP;
						sens = 1;
						break;
				}
		}
		this.hexapod.setWantedAngle(leg, articulation, sens * realValue * STEP + var);
	}
}
