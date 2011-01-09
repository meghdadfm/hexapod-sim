package net.isammoc.hexapod;

import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;

public class HexapodShapeFactory {
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
		epauleShape.addChildShape(new BoxCollisionShape(new Vector3f(0.35f, .35f, 0.65f)), new Vector3f(0, 0,
				0.65f));
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
