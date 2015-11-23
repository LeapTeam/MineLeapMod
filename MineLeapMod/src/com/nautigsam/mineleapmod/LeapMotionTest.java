/*

	Self-contained file used for testing LeapMotion without running Minecraft

	To run you need to have all binaries in the same directory:

	> javac -classpath ./LeapJava.jar LeapMotionTest.java && java -classpath "./LeapJava.jar:." LeapMotionTest

 */

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.HandList;
import com.leapmotion.leap.Hand;

// temp
import java.io.IOException;

public class LeapMotionTest {

	private static float LEFT_ROLL_THRESHOLD = 0.25f;
	private static float LEFT_ROLL_INIT = 0.75f;
	private static float LEFT_PITCH_THRESHOLD = 0.25f;
	private static float LEFT_PITCH_INIT = 0.35f;

	// NOTE: not sure if these should be different
	private static float RIGHT_ROLL_THRESHOLD = 0.25f;
	private static float RIGHT_ROLL_INIT = -0.55f;
	private static float RIGHT_PITCH_THRESHOLD = 0.25f;
	private static float RIGHT_PITCH_INIT = 0.35f;

	// hit and release values
	private static float RIGHT_GRAB_TOLERANCE = 0.0f;
	private static int positive = 10;
	private static int hits = 0;
	private static int releases = 0;

	// last delta movement of axis
	public static float deltaX;
	public static float deltaY;

	// last virtual mouse position
	public static int x = 0;
	public static int y = 0;

	// values that Minecraft expects when reading the actual mouse
	public static int mcX = 0;
	public static int mcY = 0;

	private static long lastAxisReading = 0;
	private static long guiPollTimeout = 30;
	private static long gamePollTimeout = 10;
	public static long last0Reading = 0;
	public static long lastNon0Reading = 0;

	private static long lastFrameID = 0;
	private static Controller controller;

	public static void onFrame(Controller controller) {
		Frame frame = controller.frame();

		for (Hand hand : frame.hands()) {

			float grabStrength = hand.grabStrength();
			// 'debouncing' the hits and releases gestures
			if (grabStrength + RIGHT_GRAB_TOLERANCE >= 1.0f) {
				if (hits < positive) hits += 1; // prevent overflowing
			} else {
				hits = 0;
			}
			if (grabStrength - RIGHT_GRAB_TOLERANCE <= 0.0f) {
				if (releases < positive) releases += 1; // prevent overflowing
			} else {
				releases = 0;
			}

			if (hits >= positive) {
				System.out.println("HIT!");
			} else if (releases >= positive) {
				System.out.println("RELEASE!");
			}

			// Left Hand controls movement
			// Right Hand controls camera
			// Angles:
			// pitch (angle x-axis) (fingers up/down)
			// yaw (angle y-axis) (sideways rotation)
			// roll (angle z-axis) (thumbs up/down):
			float pitch = hand.direction().pitch();
			float yaw = hand.direction().yaw();
			float roll = hand.palmNormal().roll();

			if (hand.isLeft()) {
				if (roll > LEFT_ROLL_INIT + LEFT_ROLL_THRESHOLD) {
					System.out.println("LEFT!");
				} else if (roll < LEFT_ROLL_INIT - LEFT_ROLL_THRESHOLD) {
					System.out.println("RIGHT!");
				}
				if (pitch > LEFT_PITCH_INIT + LEFT_PITCH_THRESHOLD) {
					System.out.println("BACKWARD!");
				} else if (pitch < LEFT_PITCH_INIT - LEFT_PITCH_THRESHOLD) {
					System.out.println("FORWARD!");
				}
			} else if (hand.isRight()) {
				if (roll > RIGHT_ROLL_INIT + RIGHT_ROLL_THRESHOLD) {
					System.out.println("CAMERA LEFT!");
				} else if (roll < RIGHT_ROLL_INIT - RIGHT_ROLL_THRESHOLD) {
					System.out.println("CAMERA RIGHT!");
				}
				if (pitch > RIGHT_PITCH_INIT + RIGHT_PITCH_THRESHOLD) {
					System.out.println("CAMERA UP!");
				} else if (pitch < RIGHT_PITCH_INIT - RIGHT_PITCH_THRESHOLD) {
					System.out.println("CAMERA DOWN!");
				}
			}

		}


	}

	public static Frame nextFrame() {
		Frame frame = controller.frame();

		// NOTE: should this be done?
	    // if( frame.id() == lastFrameID ) return;
	    // lastFrameID = frame.id();

	    return frame;
	}

	public static void main(String[] args) {
		Controller controller = new Controller();

		for (;;) {
			try {
				onFrame(controller);
				Thread.sleep(30);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
