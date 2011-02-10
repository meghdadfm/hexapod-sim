package net.isammoc.hexapod;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Transform;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.JmeContext.Type;

public class HexapodJME extends SimpleApplication {
	private static final float FLOOR_FRICTION = 10000f;

	private BulletAppState bulletAppState;
	private final HexapodNode hexapod = new HexapodNode();
	private static boolean PHYSICS_ACTIVE = true;

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

		this.hexapod.setLocalTransform(new Transform(new Vector3f(0, 20, 0)));

		// Add hexapod to the world
		this.rootNode.attachChild(this.hexapod);
		this.bulletAppState.getPhysicsSpace().addAll(this.hexapod);

		// this.hexapod.getControl(RigidBodyControl.class).attachDebugShape(this.assetManager);
		for (final Spatial child : this.hexapod.getChildren()) {
			child.getControl(RigidBodyControl.class).attachDebugShape(this.assetManager);
		}
	}

	@Override
	public void simpleUpdate(final float tpf) {
		super.simpleUpdate(tpf);
		this.hexapod.simpleUpdate(tpf);
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
		final RigidBodyControl floorControl = new RigidBodyControl(new BoxCollisionShape(new Vector3f(100f,
				0.1f, 50f)), 0);
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

	public HexapodNode getHexapod() {
		return this.hexapod;
	}
}
