package net.isammoc.hexapod;

import java.util.EnumMap;
import java.util.Map;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.PhysicsRigidBodyControl;
import com.jme3.bullet.joints.PhysicsHingeJoint;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.system.JmeContext.Type;

public class HexapodJME extends SimpleApplication {
	private static final float FLOOR_FRICTION = 10000f;
	private static final float MASS_BASE = 100f;
	private static final float MASS_SHOULDER = 10f;
	private static final float MASS_ARM = 10f;
	private static final float MASS_HAND = 10f;
	private static final float MOTOR_IMPULSE = 1f;
	private static final float MOTOR_VELOCITY = 1f;

	private BulletAppState bulletAppState;
	private final Node hexapod = new Node();

	private static boolean PHYSICS_ACTIVE = true;

	/** Joints of the hexapod. */
	private Map<HexapodLeg, Map<HexapodArticulation, PhysicsHingeJoint>> joints;
	{
		this.joints = new EnumMap<HexapodLeg, Map<HexapodArticulation, PhysicsHingeJoint>>(HexapodLeg.class);
		for (final HexapodLeg leg : HexapodLeg.values()) {
			this.joints.put(leg, new EnumMap<HexapodArticulation, PhysicsHingeJoint>(
					HexapodArticulation.class));
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

	@Override
	public void simpleInitApp() {
		// Init physics space
		this.bulletAppState = new BulletAppState();
		this.stateManager.attach(this.bulletAppState);
		this.bulletAppState.setActive(PHYSICS_ACTIVE);
		this.bulletAppState.getPhysicsSpace().setAccuracy(0.005f);

		// Camera position
		this.cam.setLocation(this.cam.getLocation().add(new Vector3f(10, 10, 10)));
		this.cam.setDirection(Vector3f.UNIT_Y.mult(-10).add(this.cam.getLocation().mult(-1)).normalize());
		this.flyCam.setMoveSpeed(50);

		this.initFloor();

		this.createHexapod(new Transform(new Vector3f(0, 10, 0)));

		// Add hexapod to the world
		this.rootNode.attachChild(this.hexapod);
		this.bulletAppState.getPhysicsSpace().addAll(this.hexapod);
		for (final HexapodLeg leg : HexapodLeg.values()) {
			for (final HexapodArticulation articulation : HexapodArticulation.values()) {
				final PhysicsHingeJoint joint = this.joints.get(leg).get(articulation);
				this.zeroAngles.get(leg).put(articulation, joint.getHingeAngle() % FastMath.TWO_PI);
				this.bulletAppState.getPhysicsSpace().add(joint);
			}
		}
	}

	/**
	 * Create an hexapod to the specified position and orientation.
	 * 
	 * @param transform
	 *            The combined position and orientation.
	 */
	private void createHexapod(final Transform transform) {

		final Node baseNode = new Node("base");
		final PhysicsRigidBodyControl baseControl = new PhysicsRigidBodyControl(
				this.shapeFactory.createBaseShape(), MASS_BASE);
		baseNode.addControl(baseControl);
		this.placeNode(transform, baseControl);
		baseNode.setName("base");
		this.hexapod.attachChild(baseNode);
		this.hexapod.setName("hexapod");

		this.createLeg(baseControl, HexapodLeg.LEFT_FRONT, transform, new Vector3f(3.0f, 1f, 4.5f), +0.588f);
		this.createLeg(baseControl, HexapodLeg.LEFT_MIDDLE, transform, new Vector3f(3.8f, 1f, 0f),
				FastMath.HALF_PI);
		this.createLeg(baseControl, HexapodLeg.LEFT_REAR, transform, new Vector3f(3.0f, 1f, -4.5f),
				FastMath.PI - 0.588f);
		this.createLeg(baseControl, HexapodLeg.RIGHT_FRONT, transform, new Vector3f(-3.0f, 1f, 4.5f), -0.588f);
		this.createLeg(baseControl, HexapodLeg.RIGHT_MIDDLE, transform, new Vector3f(-3.8f, 1f, 0f),
				-FastMath.HALF_PI);
		this.createLeg(baseControl, HexapodLeg.RIGHT_REAR, transform, new Vector3f(-3.0f, 1f, -4.5f),
				0.588f - FastMath.PI);
	}

	/**
	 * Helper to place a node. Delete collision between hexapod parts.
	 * 
	 * @param transform
	 *            Location and orientation of the node.
	 * @param node
	 *            node to place.
	 */
	private void placeNode(final Transform transform, final PhysicsRigidBodyControl node) {
		node.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
		node.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);
		node.attachDebugShape(this.assetManager);
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
	private void createLeg(final PhysicsRigidBodyControl base, final HexapodLeg leg,
			final Transform baseTransform, final Vector3f pivotBase, final float angle) {
		final Transform transform = new Transform(pivotBase, new Quaternion().fromAngleAxis(angle,
				Vector3f.UNIT_Y)).combineWithParent(baseTransform);

		final PhysicsRigidBodyControl shoulderControl = this.createShoulder(leg, transform);

		final PhysicsHingeJoint shoulder = new PhysicsHingeJoint(base, shoulderControl, pivotBase,
				Vector3f.ZERO, Vector3f.UNIT_Y, Vector3f.UNIT_Y);
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
	private PhysicsRigidBodyControl createShoulder(final HexapodLeg leg, final Transform transform) {
		final PhysicsRigidBodyControl shoulderControl = new PhysicsRigidBodyControl(
				this.shapeFactory.createShoulderShape(), MASS_SHOULDER);
		final Node shoulderNode = new Node("shoulder");
		shoulderNode.addControl(shoulderControl);
		this.placeNode(transform, shoulderControl);
		this.hexapod.attachChild(shoulderNode);

		final PhysicsRigidBodyControl armControl = this.createArm(leg,
				new Transform(new Vector3f(0, 0, 2f)).combineWithParent(transform));

		final PhysicsHingeJoint elbow = new PhysicsHingeJoint(shoulderControl, armControl, new Vector3f(0, 0,
				1.3f), Vector3f.ZERO, Vector3f.UNIT_X, Vector3f.UNIT_X);
		elbow.setCollisionBetweenLinkedBodys(false);
		elbow.enableMotor(true, 0, 1);
		this.joints.get(leg).put(HexapodArticulation.ELBOW, elbow);

		return shoulderControl;
	}

	private PhysicsRigidBodyControl createArm(final HexapodLeg leg, final Transform transform) {
		final PhysicsRigidBodyControl armControl = new PhysicsRigidBodyControl(
				this.shapeFactory.createArmShape(), MASS_ARM);
		final Node armNode = new Node("arm");
		armNode.addControl(armControl);
		this.placeNode(transform, armControl);
		this.hexapod.attachChild(armNode);

		final PhysicsRigidBodyControl handControl = this.createHand(new Transform(new Vector3f(2, 4.6f, 0))
				.combineWithParent(transform));
		final Quaternion quaternion = new Quaternion();
		quaternion.fromAngleAxis(FastMath.PI / 3, Vector3f.UNIT_X);

		final PhysicsHingeJoint wrist = new PhysicsHingeJoint(armControl, handControl, new Vector3f(0.25f,
				4.6f, 0.25f), Vector3f.ZERO, Vector3f.UNIT_X, Vector3f.UNIT_X);
		wrist.enableMotor(true, 0, 1);
		wrist.setCollisionBetweenLinkedBodys(false);
		this.joints.get(leg).put(HexapodArticulation.WRIST, wrist);

		return armControl;
	}

	private PhysicsRigidBodyControl createHand(final Transform transform) {
		final PhysicsRigidBodyControl handControl = new PhysicsRigidBodyControl(
				this.shapeFactory.createHandShape(), MASS_HAND);
		final Node handNode = new Node("hand");
		handNode.addControl(handControl);
		this.placeNode(transform, handControl);
		this.hexapod.attachChild(handNode);

		return handControl;
	}

	/** Make a solid floor and add it to the scene. */
	public void initFloor() {

		final Material floor_mat = new Material(this.assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
		floor_mat.setColor("m_Color", ColorRGBA.Gray);
		final Box floorBox = new Box(Vector3f.ZERO, 100f, 0.1f, 50f);
		floorBox.scaleTextureCoordinates(new Vector2f(30, 60));
		final Geometry floor = new Geometry("floor", floorBox);
		floor.setMaterial(floor_mat);
		floor.setShadowMode(ShadowMode.Receive);
		final PhysicsRigidBodyControl floorControl = new PhysicsRigidBodyControl(new BoxCollisionShape(
				new Vector3f(100f, 0.1f, 50f)), 0);
		floor.addControl(floorControl);
		floorControl.setPhysicsLocation(new Vector3f(0, -0.1f, 0));
		floorControl.setFriction(FLOOR_FRICTION);
		this.rootNode.attachChild(floor);
		this.bulletAppState.getPhysicsSpace().add(floorControl);
	}

	public static void main(final String[] args) {
		final HexapodJME hexapod = new HexapodJME();
		hexapod.start(Type.Display);
	}

	@Override
	public void simpleUpdate(final float tpf) {
		for (final HexapodLeg leg : HexapodLeg.values()) {
			for (final HexapodArticulation articulation : HexapodArticulation.values()) {
				final PhysicsHingeJoint joint = this.joints.get(leg).get(articulation);
				final float current = joint.getHingeAngle() % FastMath.TWO_PI;
				float wantedAngle = ((this.zeroAngles.get(leg).get(articulation) % FastMath.TWO_PI) + this.wantedAngles
						.get(leg).get(articulation)) % FastMath.TWO_PI;

				if (current - wantedAngle > FastMath.PI) {
					wantedAngle += FastMath.TWO_PI;
				}

				if (wantedAngle - current > FastMath.PI) {
					wantedAngle -= FastMath.TWO_PI;
				}

				if (Math.abs((current - wantedAngle) % FastMath.TWO_PI) < FastMath.PI / 90) {
					joint.enableMotor(true, 0, MOTOR_IMPULSE);
				} else {
					if (current - wantedAngle < 0) {
						joint.enableMotor(true, MOTOR_VELOCITY, MOTOR_IMPULSE);
					} else {
						joint.enableMotor(true, -MOTOR_VELOCITY, MOTOR_IMPULSE);
					}
					joint.getBodyA().activate();
					joint.getBodyB().activate();
				}
			}
		}
	}

	public Map<HexapodLeg, Map<HexapodArticulation, Float>> getModel() {
		return this.wantedAngles;
	}
}
