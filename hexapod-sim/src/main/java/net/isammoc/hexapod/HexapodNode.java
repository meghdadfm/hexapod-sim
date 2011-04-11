package net.isammoc.hexapod;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EnumMap;
import java.util.Map;

import net.isammoc.hexapod.HexapodVelocitiesHandler.DIRECTION;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class HexapodNode extends Node implements WantedAnglesAware {
	public static final String PROPERTY_MOVING = "moving";
	private static final float FLOOR_FRICTION = 10000f;
	private static final float MASS_BASE = 50f;
	private static final float MASS_SHOULDER = 5f;
	private static final float MASS_ARM = 5f;
	private static final float MASS_HAND = 10f;
	private static final float MOTOR_IMPULSE = 5f;
	private static final float MOTOR_VELOCITY = 2f;

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private final boolean moving = false;
	private final HexapodVelocitiesHandler velocities = new HexapodVelocitiesHandler();

	/** Joints of the hexapod. */
	private Map<HexapodLeg, Map<HexapodArticulation, HingeJoint>> joints;
	{
		this.joints = new EnumMap<HexapodLeg, Map<HexapodArticulation, HingeJoint>>(HexapodLeg.class);
		for (final HexapodLeg leg : HexapodLeg.values()) {
			this.joints.put(leg, new EnumMap<HexapodArticulation, HingeJoint>(HexapodArticulation.class));
		}
	}

	/** Model for joints */
	private Map<HexapodLeg, Map<HexapodArticulation, Float>> wantedAngles;
	{
		this.wantedAngles = new EnumMap<HexapodLeg, Map<HexapodArticulation, Float>>(HexapodLeg.class);
		for (final HexapodLeg leg : HexapodLeg.values()) {
			this.wantedAngles.put(leg, new EnumMap<HexapodArticulation, Float>(HexapodArticulation.class));
			for (final HexapodArticulation articulation : HexapodArticulation.values()) {
				this.wantedAngles.get(leg).put(articulation, 0f);
			}
		}
	}

	/** Angles for model equals 0. */
	private Map<HexapodLeg, Map<HexapodArticulation, Float>> zeroAngles;
	{
		this.zeroAngles = new EnumMap<HexapodLeg, Map<HexapodArticulation, Float>>(HexapodLeg.class);
		for (final HexapodLeg leg : HexapodLeg.values()) {
			this.zeroAngles.put(leg, new EnumMap<HexapodArticulation, Float>(HexapodArticulation.class));
		}
	}

	/** Hexapod shape factory. */
	private final HexapodShapeFactory shapeFactory = new HexapodShapeFactory();

	public HexapodNode() {
		super("hexapod");

		final RigidBodyControl baseControl = new RigidBodyControl(this.shapeFactory.createBaseShape(),
				MASS_BASE);
		final Node baseNode = new Node("base");
		baseNode.addControl(baseControl);
		baseNode.setName("base");
		this.attachChild(baseNode);

		this.createLeg(baseControl, HexapodLeg.LEFT_FRONT, new Vector3f(3.0f, 1f, 4.5f), +0.588f);
		this.createLeg(baseControl, HexapodLeg.LEFT_MIDDLE, new Vector3f(3.8f, 1f, 0f), FastMath.HALF_PI);
		this.createLeg(baseControl, HexapodLeg.LEFT_REAR, new Vector3f(3.0f, 1f, -4.5f), FastMath.PI - 0.588f);
		this.createLeg(baseControl, HexapodLeg.RIGHT_FRONT, new Vector3f(-3.0f, 1f, 4.5f), -0.588f);
		this.createLeg(baseControl, HexapodLeg.RIGHT_MIDDLE, new Vector3f(-3.8f, 1f, 0f), -FastMath.HALF_PI);
		this.createLeg(baseControl, HexapodLeg.RIGHT_REAR, new Vector3f(-3.0f, 1f, -4.5f),
				0.588f - FastMath.PI);

		for (final HexapodLeg leg : HexapodLeg.values()) {
			for (final HexapodArticulation articulation : HexapodArticulation.values()) {
				final HingeJoint joint = this.joints.get(leg).get(articulation);
				this.zeroAngles.get(leg).put(articulation, joint.getHingeAngle() % FastMath.TWO_PI);
			}
		}
		this.velocities.addPropertyChangeListener(PROPERTY_MOVING, new PropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				HexapodNode.this.pcs.firePropertyChange(PROPERTY_MOVING, evt.getOldValue(), evt.getNewValue());
			}
		});
	}

	/**
	 * Helper to place a node. Delete collision between hexapod parts.
	 * 
	 * @param transform
	 *            Location and orientation of the node.
	 * @param node
	 *            node to place.
	 */
	private void placeNode(final Transform transform, final RigidBodyControl node) {
		node.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
		node.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);
		node.setFriction(FLOOR_FRICTION);
		node.setPhysicsLocation(transform.getTranslation());
		node.setPhysicsRotation(transform.getRotation().toRotationMatrix());
	}

	/**
	 * Create a leg and attach it to the base.
	 * 
	 * @param base
	 *            The hexapod base node.
	 * @param leg
	 *            the enum of the leg to create
	 * @param baseTransform
	 *            transform of the base.
	 * @param pivotBase
	 *            location of the joint with the base.
	 * @param angle
	 *            start angle of the leg.
	 */
	private void createLeg(final RigidBodyControl base, final HexapodLeg leg, final Vector3f pivotBase,
			final float angle) {
		final Transform transform = new Transform(pivotBase, new Quaternion().fromAngleAxis(angle,
				Vector3f.UNIT_Y));

		final RigidBodyControl shoulderControl = this.createShoulder(leg, transform);

		final HingeJoint shoulder = new HingeJoint(base, shoulderControl, pivotBase, Vector3f.ZERO,
				Vector3f.UNIT_Y, Vector3f.UNIT_Y);
		shoulder.enableMotor(true, 0, 1);
		shoulder.setCollisionBetweenLinkedBodys(false);
		this.joints.get(leg).put(HexapodArticulation.SHOULDER, shoulder);
	}

	/**
	 * Create the shoulder for the specified leg.
	 * 
	 * @param leg
	 *            Enumeration of the shoulder to create.
	 * @param transform
	 *            Location and orientation combined.
	 * @return the node.
	 */
	private RigidBodyControl createShoulder(final HexapodLeg leg, final Transform transform) {
		final RigidBodyControl shoulderControl = new RigidBodyControl(
				this.shapeFactory.createShoulderShape(), MASS_SHOULDER);
		final Node shoulderNode = new Node("shoulder");
		shoulderNode.addControl(shoulderControl);
		this.placeNode(transform, shoulderControl);
		this.attachChild(shoulderNode);

		final RigidBodyControl armControl = this.createArm(leg,
				new Transform(new Vector3f(0, 0, 2f)).combineWithParent(transform));

		final HingeJoint elbow = new HingeJoint(shoulderControl, armControl, new Vector3f(0, 0, 1.3f),
				Vector3f.ZERO, Vector3f.UNIT_X, Vector3f.UNIT_X);
		elbow.setCollisionBetweenLinkedBodys(false);
		elbow.enableMotor(true, 0, 1);
		this.joints.get(leg).put(HexapodArticulation.ELBOW, elbow);

		return shoulderControl;
	}

	private RigidBodyControl createArm(final HexapodLeg leg, final Transform transform) {
		final RigidBodyControl armControl = new RigidBodyControl(this.shapeFactory.createArmShape(), MASS_ARM);
		final Node armNode = new Node("arm");
		armNode.addControl(armControl);
		this.placeNode(transform, armControl);
		this.attachChild(armNode);

		final RigidBodyControl handControl = this.createHand(new Transform(new Vector3f(2, 4.6f, 0))
				.combineWithParent(transform));
		final Quaternion quaternion = new Quaternion();
		quaternion.fromAngleAxis(FastMath.PI / 3, Vector3f.UNIT_X);

		final HingeJoint wrist = new HingeJoint(armControl, handControl, new Vector3f(0.25f, 4.6f, 0.25f),
				Vector3f.ZERO, Vector3f.UNIT_X, Vector3f.UNIT_X);
		wrist.enableMotor(true, 0, 1);
		wrist.setCollisionBetweenLinkedBodys(false);
		this.joints.get(leg).put(HexapodArticulation.WRIST, wrist);

		return armControl;
	}

	private RigidBodyControl createHand(final Transform transform) {
		final RigidBodyControl handControl = new RigidBodyControl(this.shapeFactory.createHandShape(),
				MASS_HAND);
		final Node handNode = new Node("hand");
		handNode.addControl(handControl);
		this.placeNode(transform, handControl);
		this.attachChild(handNode);

		return handControl;
	}

	public void simpleUpdate(final float tpf) {
		for (final HexapodLeg leg : HexapodLeg.values()) {
			for (final HexapodArticulation articulation : HexapodArticulation.values()) {
				final HingeJoint joint = this.joints.get(leg).get(articulation);
				final float current = joint.getHingeAngle() % FastMath.TWO_PI;
				float wantedAngle = (this.zeroAngles.get(leg).get(articulation) + this.wantedAngles.get(leg)
						.get(articulation)) % FastMath.TWO_PI;

				if (current - wantedAngle > FastMath.PI) {
					wantedAngle += FastMath.TWO_PI;
				}

				if (wantedAngle - current > FastMath.PI) {
					wantedAngle -= FastMath.TWO_PI;
				}

				final float diff = Math.abs((current - wantedAngle) % FastMath.TWO_PI);
				if (diff < (FastMath.PI / 100)) {
					if (current - wantedAngle < 0) {
						joint.enableMotor(true, MOTOR_VELOCITY / 10, MOTOR_IMPULSE);
					} else {
						joint.enableMotor(true, -MOTOR_VELOCITY / 10, MOTOR_IMPULSE);
					}
					this.velocities.setVelocity(leg, articulation, DIRECTION.NONE);
				} else if (diff < (FastMath.PI / 80)) {
					if (current - wantedAngle < 0) {
						joint.enableMotor(true, MOTOR_VELOCITY / 5, MOTOR_IMPULSE);
					} else {
						joint.enableMotor(true, MOTOR_VELOCITY / 5, MOTOR_IMPULSE);
					}
					this.velocities.setVelocity(leg, articulation, DIRECTION.NONE);
				} else {
					if (current - wantedAngle < 0) {
						joint.enableMotor(true, MOTOR_VELOCITY, MOTOR_IMPULSE);
						this.velocities.setVelocity(leg, articulation, DIRECTION.FORWARD);
					} else {
						joint.enableMotor(true, -MOTOR_VELOCITY, MOTOR_IMPULSE);
						this.velocities.setVelocity(leg, articulation, DIRECTION.BACKWARD);
					}
				}
			}
		}
	}

	@Override
	public void setWantedAngle(final HexapodLeg leg, final HexapodArticulation articulation, final float value) {
		this.wantedAngles.get(leg).put(articulation, value);
		final HingeJoint joint = this.joints.get(leg).get(articulation);
		final float current = joint.getHingeAngle() % FastMath.TWO_PI;
		float wantedAngle = (this.zeroAngles.get(leg).get(articulation) + this.wantedAngles.get(leg).get(
				articulation))
				% FastMath.TWO_PI;
		if (current - wantedAngle > FastMath.PI) {
			wantedAngle += FastMath.TWO_PI;
		}

		if (wantedAngle - current > FastMath.PI) {
			wantedAngle -= FastMath.TWO_PI;
		}

		if (Math.abs((current - wantedAngle) % FastMath.TWO_PI) < FastMath.PI / 90) {
			joint.enableMotor(true, 0, MOTOR_IMPULSE);
			this.velocities.setVelocity(leg, articulation, DIRECTION.NONE);
		} else {
			if (current - wantedAngle < 0) {
				joint.enableMotor(true, MOTOR_VELOCITY, MOTOR_IMPULSE);
				this.velocities.setVelocity(leg, articulation, DIRECTION.FORWARD);
			} else {
				joint.enableMotor(true, -MOTOR_VELOCITY, MOTOR_IMPULSE);
				this.velocities.setVelocity(leg, articulation, DIRECTION.BACKWARD);
			}
			joint.getBodyA().activate();
			joint.getBodyB().activate();
		}
	}

	@Override
	public void setLocalTransform(final Transform t) {
		super.setLocalTransform(t);

		for (final Spatial child : this.getChildren()) {
			final RigidBodyControl control = child.getControl(RigidBodyControl.class);
			control.setPhysicsLocation(control.getPhysicsLocation().add(t.getTranslation()));
			control.setPhysicsRotation(control.getPhysicsRotation().mult(t.getRotation()));
			child.setLocalTransform(child.getLocalTransform().combineWithParent(t));
		}
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

	public boolean hasListeners(final String propertyName) {
		return this.pcs.hasListeners(propertyName);
	}

	private static class HexapodShapeFactory {
		public CollisionShape createBaseShape() {
			final CompoundCollisionShape chassisShape = new CompoundCollisionShape();
			final CapsuleCollisionShape rf_lb = new CapsuleCollisionShape(.3f, 10.81f);
			final CapsuleCollisionShape lf_rb = new CapsuleCollisionShape(.3f, 10.81f);
			final CapsuleCollisionShape lm_rm = new CapsuleCollisionShape(.3f, 7.6f);

			final Matrix3f rotX = new Matrix3f();
			rotX.fromAngleNormalAxis(FastMath.HALF_PI, Vector3f.UNIT_X);

			final Matrix3f rotY = new Matrix3f();
			rotY.fromAngleAxis(0.588f, Vector3f.UNIT_Y);

			chassisShape.addChildShape(rf_lb, Vector3f.ZERO, rotY.mult(rotX));

			rotY.fromAngleAxis(-0.588f, Vector3f.UNIT_Y);

			chassisShape.addChildShape(lf_rb, Vector3f.ZERO, rotY.mult(rotX));

			final Matrix3f rotZ = new Matrix3f();
			rotZ.fromAngleNormalAxis(FastMath.HALF_PI, Vector3f.UNIT_Z);

			chassisShape.addChildShape(lm_rm, Vector3f.ZERO, rotZ);

			return chassisShape;
		}

		public CollisionShape createShoulderShape() {
			final CompoundCollisionShape epauleShape = new CompoundCollisionShape();
			epauleShape.addChildShape(new BoxCollisionShape(new Vector3f(0.35f, .35f, 0.65f)), new Vector3f(
					0, 0, 0.65f));
			return epauleShape;
		}

		public CollisionShape createArmShape() {
			final CompoundCollisionShape armShape = new CompoundCollisionShape();
			final BoxCollisionShape box = new BoxCollisionShape(new Vector3f(.25f, 2.3f, 1f));

			final Matrix3f rot = Matrix3f.IDENTITY;
			rot.fromAngleAxis((FastMath.PI / 9) * 1, Vector3f.UNIT_X.mult(-1));
			armShape.addChildShape(box, new Vector3f(.5f, 2.3f, .0f));
			return armShape;
		}

		public CollisionShape createHandShape() {
			final CompoundCollisionShape handShape = new CompoundCollisionShape();
			handShape.addChildShape(new CapsuleCollisionShape(.5f, 11.2f), new Vector3f(.5f, -2.9f, .5f));
			return handShape;
		}
	}
}
