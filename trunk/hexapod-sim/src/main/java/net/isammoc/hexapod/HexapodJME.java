package net.isammoc.hexapod;

import java.util.EnumMap;
import java.util.Map;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.joints.PhysicsHingeJoint;
import com.jme3.bullet.nodes.PhysicsNode;
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
	private BulletAppState bulletAppState;
	private final Node hexapod = new Node();
	private static float MASS_BASE = 1f;
	private static float MASS_SHOULDER = 1f;
	private static float MASS_ARM = 1f;
	private static float MASS_HAND = 1f;
	private static float MOTOR_IMPUSLE = .1f;
	private static float MOTOR_VELOCITY = 3f;

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
		final PhysicsNode baseNode = new PhysicsNode(this.shapeFactory.createBaseShape(), MASS_BASE);
		this.placeNode(transform, baseNode);
		baseNode.setName("base");
		this.hexapod.attachChild(baseNode);
		this.hexapod.setName("hexapod");

		this.createLeg(baseNode, HexapodLeg.LEFT_FRONT, transform, new Vector3f(3.0f, 1f, 4.5f), +0.588f);
		this.createLeg(baseNode, HexapodLeg.LEFT_MIDDLE, transform, new Vector3f(3.8f, 1f, 0f),
				FastMath.HALF_PI);
		this.createLeg(baseNode, HexapodLeg.LEFT_REAR, transform, new Vector3f(3.0f, 1f, -4.5f),
				FastMath.PI - 0.588f);
		this.createLeg(baseNode, HexapodLeg.RIGHT_FRONT, transform, new Vector3f(-3.0f, 1f, 4.5f), -0.588f);
		this.createLeg(baseNode, HexapodLeg.RIGHT_MIDDLE, transform, new Vector3f(-3.8f, 1f, 0f),
				-FastMath.HALF_PI);
		this.createLeg(baseNode, HexapodLeg.RIGHT_REAR, transform, new Vector3f(-3.0f, 1f, -4.5f),
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
	private void placeNode(final Transform transform, final PhysicsNode node) {
		node.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
		node.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);
		node.attachDebugShape(this.assetManager);
		node.setLocalTransform(transform);
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
	private void createLeg(final PhysicsNode base, final HexapodLeg leg, final Transform baseTransform,
			final Vector3f pivotBase, final float angle) {
		final Transform transform = new Transform(pivotBase, new Quaternion().fromAngleAxis(angle,
				Vector3f.UNIT_Y)).combineWithParent(baseTransform);

		final PhysicsNode shoulderNode = this.createShoulder(leg, transform);

		final PhysicsHingeJoint shoulder = new PhysicsHingeJoint(base.getRigidBody(),
				shoulderNode.getRigidBody(), pivotBase, Vector3f.ZERO, Vector3f.UNIT_Y, Vector3f.UNIT_Y);
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
	private PhysicsNode createShoulder(final HexapodLeg leg, final Transform transform) {
		final PhysicsNode shoulderNode = new PhysicsNode(this.shapeFactory.createShoulderShape(),
				MASS_SHOULDER);
		this.placeNode(transform, shoulderNode);
		this.hexapod.attachChild(shoulderNode);

		final PhysicsNode armNode = this.createArm(leg,
				new Transform(new Vector3f(0, 0, 2f)).combineWithParent(transform));

		final PhysicsHingeJoint elbow = new PhysicsHingeJoint(shoulderNode.getRigidBody(),
				armNode.getRigidBody(), new Vector3f(0, 0, 1.3f), Vector3f.ZERO, Vector3f.UNIT_X,
				Vector3f.UNIT_X);
		elbow.setCollisionBetweenLinkedBodys(false);
		elbow.enableMotor(true, 0, 1);
		this.joints.get(leg).put(HexapodArticulation.ELBOW, elbow);

		return shoulderNode;
	}

	private PhysicsNode createArm(final HexapodLeg leg, final Transform transform) {
		final PhysicsNode armNode = new PhysicsNode(this.shapeFactory.createArmShape(), MASS_ARM);
		this.placeNode(transform, armNode);
		this.hexapod.attachChild(armNode);

		final PhysicsNode handNode = this.createHand(new Transform(new Vector3f(2, 4.6f, 0))
				.combineWithParent(transform));
		final Quaternion quaternion = new Quaternion();
		quaternion.fromAngleAxis(FastMath.PI / 3, Vector3f.UNIT_X);

		final PhysicsHingeJoint wrist = new PhysicsHingeJoint(armNode.getRigidBody(),
				handNode.getRigidBody(), new Vector3f(0.25f, 4.6f, 0.25f), Vector3f.ZERO, Vector3f.UNIT_X,
				Vector3f.UNIT_X);
		wrist.enableMotor(true, 0, 1);
		wrist.setCollisionBetweenLinkedBodys(false);
		this.joints.get(leg).put(HexapodArticulation.WRIST, wrist);

		return armNode;
	}

	private PhysicsNode createHand(final Transform transform) {
		final PhysicsNode handNode = new PhysicsNode(this.shapeFactory.createHandShape(), MASS_HAND);
		this.placeNode(transform, handNode);
		this.hexapod.attachChild(handNode);

		return handNode;
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
		final PhysicsNode floorNode = new PhysicsNode(floor, new BoxCollisionShape(new Vector3f(100f, 0.1f,
				50f)), 0);
		floorNode.setLocalTranslation(0, -0.1f, 0);
		floorNode.setName("floor");
		this.rootNode.attachChild(floorNode);
		this.bulletAppState.getPhysicsSpace().add(floorNode);
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
					joint.enableMotor(true, 0, MOTOR_IMPUSLE);
				} else {
					if (current - wantedAngle < 0) {
						joint.enableMotor(true, MOTOR_VELOCITY, MOTOR_IMPUSLE);
					} else {
						joint.enableMotor(true, -MOTOR_VELOCITY, MOTOR_IMPUSLE);
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
