package net.isammoc.hexapod;

import java.util.EnumMap;
import java.util.Map;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.joints.PhysicsHingeJoint;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.JmeContext.Type;

public class HexapodJME extends SimpleApplication {
	private BulletAppState bulletAppState;

	private Map<HexapodLeg, Map<HexapodArticulation, PhysicsHingeJoint>> articulations;

	@Override
	public void simpleInitApp() {
		// Initialisation de l'espace physique
		this.bulletAppState = new BulletAppState();
		this.stateManager.attach(this.bulletAppState);
		// this.bulletAppState.getPhysicsSpace().setGravity(
		// new Vector3f(0, -9.81f, 0));

		// Position de la caméra
		this.cam.setLocation(this.cam.getLocation().add(
				new Vector3f(10, 10, 10)));
		this.cam.setDirection(Vector3f.UNIT_Y.mult(-10)
				.add(this.cam.getLocation().mult(-1)).normalize());
		this.flyCam.setMoveSpeed(50);

		this.articulations = new EnumMap<HexapodLeg, Map<HexapodArticulation, PhysicsHingeJoint>>(
				HexapodLeg.class);
		for (final HexapodLeg leg : HexapodLeg.values()) {
			this.articulations.put(leg,
					new EnumMap<HexapodArticulation, PhysicsHingeJoint>(
							HexapodArticulation.class));
		}

		this.initFloor();

		this.creerHexapod(new Vector3f(0, 10, 0));

		this.bulletAppState.getPhysicsSpace().setAccuracy(0.005f);
	}

	/** Make a solid floor and add it to the scene. */
	public void initFloor() {

		final Material floor_mat = new Material(this.assetManager,
				"Common/MatDefs/Misc/SimpleTextured.j3md");
		// final TextureKey key3 = new
		// TextureKey("Textures/Terrain/Pond/Pond.png");
		// key3.setGenerateMips(true);
		// final Texture tex3 = this.assetManager.loadTexture(key3);
		// tex3.setWrap(WrapMode.Repeat);
		// floor_mat.setTexture("m_ColorMap", tex3);
		final Box floorBox = new Box(Vector3f.ZERO, 100f, 0.1f, 50f);
		floorBox.scaleTextureCoordinates(new Vector2f(30, 60));
		final Geometry floor = new Geometry("floor", floorBox);
		floor.setMaterial(floor_mat);
		floor.setShadowMode(ShadowMode.Receive);
		final PhysicsNode floorNode = new PhysicsNode(floor,
				new BoxCollisionShape(new Vector3f(100f, 0.1f, 50f)), 0);
		floorNode.setLocalTranslation(0, -0.1f, 0);
		this.rootNode.attachChild(floorNode);
		this.bulletAppState.getPhysicsSpace().add(floorNode);
	}

	@Override
	public void simpleUpdate(final float tpf) {
	}

	public static void main(final String[] args) {
		final HexapodJME hexapod = new HexapodJME();
		hexapod.start(Type.Display);
	}

	private PhysicsNode creerHexapod(final Vector3f location) {
		final PhysicsNode chassis = new PhysicsNode(new BoxCollisionShape(
				new Vector3f(3.0f, .35f, 4.5f)), 0);
		chassis.attachDebugShape(this.assetManager);
		chassis.setLocalTranslation(location);
		this.rootNode.attachChild(chassis);
		this.bulletAppState.getPhysicsSpace().add(chassis);

		this.creerPatte(chassis, new Transform(location.add(3.0f, .7f, 4.5f)),
				HexapodLeg.RIGHT_FRONT);

		final Transform transform = new Transform(
				location.add(-3.0f, .7f, 4.5f), new Quaternion().fromAngleAxis(
						FastMath.PI / 3, Vector3f.UNIT_Y));
		this.creerPatte(chassis, transform, HexapodLeg.LEFT_FRONT);

		return chassis;
	}

	private void creerPatte(final PhysicsNode chassis,
			final Transform transform, final HexapodLeg leg) {
		final PhysicsNode epaule = this.creerEpaule(leg, transform);

		final PhysicsHingeJoint shoulder = new PhysicsHingeJoint(chassis,
				epaule, chassis.worldToLocal(transform.getTranslation(), null),
				Vector3f.ZERO, Vector3f.UNIT_Y, Vector3f.UNIT_Y.mult(-1));
		shoulder.enableMotor(true, 0, 50);
		shoulder.setCollisionBetweenLinkedBodys(false);
		this.bulletAppState.getPhysicsSpace().add(shoulder);
		this.articulations.get(leg).put(HexapodArticulation.SHOULDER, shoulder);
	}

	private PhysicsNode creerEpaule(final HexapodLeg leg,
			final Transform transform) {
		final CompoundCollisionShape epauleShape = new CompoundCollisionShape();
		epauleShape.addChildShape(new BoxCollisionShape(new Vector3f(0.35f,
				.35f, 0.65f)), new Vector3f(0, 0, 0.65f));
		final PhysicsNode epaule = new PhysicsNode(epauleShape, 10);
		epaule.attachDebugShape(this.assetManager);

		epaule.setLocalTransform(transform);
		this.rootNode.attachChild(epaule);
		this.bulletAppState.getPhysicsSpace().add(epaule);

		final PhysicsNode bras = this.creerBras(leg, new Transform(
				new Vector3f(0, 0, 1.3f)).combineWithParent(transform));

		final PhysicsHingeJoint elbow = new PhysicsHingeJoint(epaule, bras,
				new Vector3f(0, 0, 1.3f), Vector3f.ZERO, Vector3f.UNIT_X,
				Vector3f.UNIT_X.mult(-1));
		elbow.setCollisionBetweenLinkedBodys(false);
		elbow.enableMotor(true, 0, 5);
		this.bulletAppState.getPhysicsSpace().add(elbow);
		this.articulations.get(leg).put(HexapodArticulation.ELBOW, elbow);

		return epaule;
	}

	private PhysicsNode creerBras(final HexapodLeg leg,
			final Transform transform) {
		final CompoundCollisionShape brasShape = new CompoundCollisionShape();
		final BoxCollisionShape box = new BoxCollisionShape(new Vector3f(.25f,
				2.3f, 1f));

		final Matrix3f rot = Matrix3f.IDENTITY;
		rot.fromAngleAxis((FastMath.PI / 9) * 1, Vector3f.UNIT_X.mult(-1));
		brasShape.addChildShape(box, new Vector3f(.5f, 2.3f, .0f));

		final PhysicsNode avantBras = new PhysicsNode(brasShape, 10);
		avantBras.setLocalTransform(transform);
		avantBras.attachDebugShape(this.assetManager);
		this.rootNode.attachChild(avantBras);
		this.bulletAppState.getPhysicsSpace().add(avantBras);

		final PhysicsNode main = this.creerMain(new Transform(new Vector3f(2,
				4.6f, 0)).combineWithParent(transform));
		final Quaternion quaternion = new Quaternion();
		quaternion.fromAngleAxis(FastMath.PI / 3, Vector3f.UNIT_X);

		final Vector3f pivotAvantBras = new Vector3f(0.25f, 4.6f, 0.25f);

		final PhysicsHingeJoint wrist = new PhysicsHingeJoint(avantBras, main,
				pivotAvantBras, Vector3f.ZERO, Vector3f.UNIT_X,
				Vector3f.UNIT_X.mult(-1));
		wrist.enableMotor(true, 0, 2);
		wrist.setCollisionBetweenLinkedBodys(false);
		this.bulletAppState.getPhysicsSpace().add(wrist);
		this.articulations.get(leg).put(HexapodArticulation.WRIST, wrist);

		return avantBras;
	}

	private PhysicsNode creerMain(final Transform transform) {

		final CompoundCollisionShape mainShape = new CompoundCollisionShape();
		mainShape.addChildShape(new CapsuleCollisionShape(.5f, 11.2f),
				new Vector3f(.5f, -2.9f, .5f));
		final PhysicsNode main = new PhysicsNode(mainShape, 10);

		main.setLocalTransform(transform);
		main.attachDebugShape(this.assetManager);
		this.rootNode.attachChild(main);
		this.bulletAppState.getPhysicsSpace().add(main);

		return main;
	}
}
