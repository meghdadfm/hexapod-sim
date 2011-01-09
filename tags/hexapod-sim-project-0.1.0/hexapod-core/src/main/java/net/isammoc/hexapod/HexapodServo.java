package net.isammoc.hexapod;

import static net.isammoc.hexapod.HexapodArticulation.ELBOW;
import static net.isammoc.hexapod.HexapodArticulation.SHOULDER;
import static net.isammoc.hexapod.HexapodArticulation.WRIST;
import static net.isammoc.hexapod.HexapodLeg.LEFT_FRONT;
import static net.isammoc.hexapod.HexapodLeg.LEFT_MIDDLE;
import static net.isammoc.hexapod.HexapodLeg.LEFT_REAR;
import static net.isammoc.hexapod.HexapodLeg.RIGHT_FRONT;
import static net.isammoc.hexapod.HexapodLeg.RIGHT_MIDDLE;
import static net.isammoc.hexapod.HexapodLeg.RIGHT_REAR;

import java.util.EnumMap;
import java.util.Map;

public enum HexapodServo {
	S1(RIGHT_FRONT, WRIST),
	S2(RIGHT_FRONT, ELBOW),
	S3(RIGHT_FRONT, SHOULDER),
	S4(RIGHT_MIDDLE, WRIST),
	S5(RIGHT_MIDDLE, ELBOW),
	S6(RIGHT_MIDDLE, SHOULDER),
	S7(RIGHT_REAR, WRIST),
	S8(RIGHT_REAR, ELBOW),
	S9(RIGHT_REAR, SHOULDER),
	S10(LEFT_REAR, WRIST),
	S11(LEFT_REAR, ELBOW),
	S12(LEFT_REAR, SHOULDER),
	S13(LEFT_MIDDLE, WRIST),
	S14(LEFT_MIDDLE, ELBOW),
	S15(LEFT_MIDDLE, SHOULDER),
	S16(LEFT_FRONT, WRIST),
	S17(LEFT_FRONT, ELBOW),
	S18(LEFT_FRONT, SHOULDER),
	S19(null, null),
	S20(null, null),
	S21(null, null);

	private final HexapodLeg leg;
	private final HexapodArticulation articulation;
	private static final Map<HexapodLeg,Map<HexapodArticulation,HexapodServo>> dictionnary;
	static {
		dictionnary = new EnumMap<HexapodLeg, Map<HexapodArticulation,HexapodServo>>(HexapodLeg.class);
		for(HexapodLeg leg: HexapodLeg.values()){
			dictionnary.put(leg, new EnumMap<HexapodArticulation, HexapodServo>(HexapodArticulation.class));
		}
		
		for(HexapodServo servo : HexapodServo.values()){
			if(servo.leg != null && servo.articulation != null) {
				dictionnary.get(servo.leg).put(servo.articulation, servo);
			}
		}
	}

	private HexapodServo(HexapodLeg leg, HexapodArticulation articulation) {
		this.leg = leg;
		this.articulation = articulation;
	}
	
	public static HexapodServo fromLegArticulation(HexapodLeg leg, HexapodArticulation articulation){
		if(leg == null){
			throw new IllegalArgumentException("leg must not be null");
		}
		if(articulation == null) {
			throw new IllegalArgumentException("articulation must not be null");
		}
		return dictionnary.get(leg).get(articulation);
	}
}