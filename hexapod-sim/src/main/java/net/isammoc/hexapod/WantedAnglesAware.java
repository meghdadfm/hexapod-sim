package net.isammoc.hexapod;

public interface WantedAnglesAware {
	void setWantedAngle(final HexapodLeg leg, final HexapodArticulation articulation, final float value);
}
