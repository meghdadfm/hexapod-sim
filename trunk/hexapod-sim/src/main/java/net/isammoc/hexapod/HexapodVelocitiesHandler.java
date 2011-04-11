package net.isammoc.hexapod;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EnumMap;
import java.util.Map;

public class HexapodVelocitiesHandler {
	private static final float MOTOR_VELOCITY = 1f;
	public static final String PROPERTY_MOVING = "moving";
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public enum DIRECTION {
		FORWARD,
		BACKWARD,
		NONE
	}

	/** Model for joints */
	private Map<HexapodLeg, Map<HexapodArticulation, Float>> velocities;
	{
		this.velocities = new EnumMap<HexapodLeg, Map<HexapodArticulation, Float>>(HexapodLeg.class);
		for (final HexapodLeg leg : HexapodLeg.values()) {
			this.velocities.put(leg, new EnumMap<HexapodArticulation, Float>(HexapodArticulation.class));
			for (final HexapodArticulation articulation : HexapodArticulation.values()) {
				this.velocities.get(leg).put(articulation, 0f);
			}
		}
	}

	public void setVelocity(final HexapodLeg leg, final HexapodArticulation articulation,
			final DIRECTION direction) {
		final boolean old = this.isMoving();
		final float value;
		switch (direction) {
			case FORWARD:
				value = MOTOR_VELOCITY;
				break;
			case BACKWARD:
				value = MOTOR_VELOCITY;
				break;
			case NONE:
			default:
				value = 0f;
		}
		this.velocities.get(leg).put(articulation, value);
		this.pcs.firePropertyChange(PROPERTY_MOVING, old, this.isMoving());
	}

	public float getVelocityValue(final HexapodLeg leg, final HexapodArticulation articulation) {
		return this.velocities.get(leg).get(articulation);
	}

	public boolean isMoving() {
		boolean moving = false;
		ext: for (final HexapodLeg leg : HexapodLeg.values()) {
			for (final HexapodArticulation articulation : HexapodArticulation.values()) {
				if (this.velocities.get(leg).get(articulation) != 0f) {
					moving = true;
					break ext;
				}
			}
		}
		return moving;
	}

	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(propertyName, listener);
	}
}
