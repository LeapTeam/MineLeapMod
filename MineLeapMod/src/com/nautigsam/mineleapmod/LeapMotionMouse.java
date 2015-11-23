package com.nautigsam.mineleapmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.HandList;
import com.leapmotion.leap.Hand;

// temp
import java.io.IOException;

import com.nautigsam.mineleapmod.lwjglVirtualInput.VirtualMouse;
import com.nautigsam.mineleapmod.helpers.LogHelper;
import com.nautigsam.mineleapmod.ControllerSettings;

public class LeapMotionMouse {

	private static Minecraft mc = Minecraft.getMinecraft();

	private static float LEFT_ROLL_THRESHOLD = 0.25f;
	private static float LEFT_ROLL_INIT = 0.75f;
	private static float LEFT_PITCH_THRESHOLD = 0.25f;
	private static float LEFT_PITCH_INIT = 0.35f;

	// NOTE: not sure if these should be different
	private static float RIGHT_ROLL_THRESHOLD = 0.25f;
	private static float RIGHT_ROLL_INIT = -0.55f;
	private static float RIGHT_PITCH_THRESHOLD = 0.25f;
	private static float RIGHT_PITCH_INIT = 0.35f;

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

	public void onFrame(Controller controller) {
		Frame frame = controller.frame();

		System.out.println(".");
		for (Hand hand : frame.hands()) {
			// Left Hand controls movement
			// Right Hand controls camera
			// Angles:
			// pitch (angle x-axis) (fingers up/down)
			// yaw (angle y-axis) (sideways rotation)
			// roll (angle z-axis) (thumbs up/down):
			if (hand.isLeft()) {
				float pitch = hand.direction().pitch();
				float yaw = hand.direction().yaw();
				float roll = hand.palmNormal().roll();

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
				float pitch = hand.direction().pitch();
				float yaw = hand.direction().yaw();
				float roll = hand.palmNormal().roll();
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
		LeapMotionMouse listener = new LeapMotionMouse();
		controller = new Controller();

		// Keep this process running until Enter is pressed
		System.out.println("Press Enter to quit...");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static int getmcX() {
		return mcX;
	}

	public static int getmcY() {
		return mcY;
	}

	public static int getX() {
		return x;
	}

	public static int getY() {
		return y;
	}

	public static void leftButtonDown() {
		if (!VirtualMouse.isButtonDown(0))
		{
			VirtualMouse.setXY(mcX, mcY);
			VirtualMouse.holdMouseButton(0, true);
		}
	}

	public static void leftButtonUp() {
		if (VirtualMouse.isButtonDown(0))
		{
			VirtualMouse.releaseMouseButton(0, true);
		}
	}

	public static boolean isLeftButtonDown() {
		return VirtualMouse.isButtonDown(0);
	}

	public static void rightButtonDown() {
		if (!VirtualMouse.isButtonDown(1))
		{
			VirtualMouse.setXY(mcX, mcY);
			VirtualMouse.holdMouseButton(1, true);
		}
	}

	public static void rightButtonUp() {
		if (VirtualMouse.isButtonDown(1))
		{
			VirtualMouse.releaseMouseButton(1, true);
		}
	}

	public static boolean isRightButtonDown() {
		return VirtualMouse.isButtonDown(1);
	}

	public static void UnpressButtons() {
		for (int i = 0; i < 2; i++)
		{
			if (VirtualMouse.isButtonDown(i))
				VirtualMouse.releaseMouseButton(i, true);
		}
	}

	public static boolean pollNeeded(boolean inGui) {
		if (Minecraft.getSystemTime() - lastAxisReading < (inGui ? guiPollTimeout : gamePollTimeout))
			return false;
		return true;
	}

	public static void pollAxis() {
		pollAxis(mc.currentScreen != null);
	}

	// pollAxis() will update the x and y position of the
	// 'mouse' with respect to right hand's movements.
	public static boolean pollAxis(boolean inGui) {
		if (!pollNeeded(inGui))
			return false;

		// TODO: how to handle GUI
		if (inGui) return false;

		Hand hand =	nextFrame().hands().rightmost();

		// Right Hand controls camera

		// Angles:
		// pitch (angle x-axis) (fingers up/down) [-1.5708,1.5708]
		// yaw (angle y-axis) (sideways rotation) [0,6.28319]
		// roll (angle z-axis) (thumbs up/down) [-1.5708,1.5708]

		float pitch = hand.direction().pitch();
		float roll = hand.palmNormal().roll();
		// substract the initial/rest zone
		roll -= RIGHT_ROLL_INIT;
		pitch -= RIGHT_PITCH_INIT;
		// NOTE: normalizing to 1.0, not sure if necessary
		roll /= 1.5708f;
		pitch /= 1.5708f;

		deltaX = calculateDelta(roll, RIGHT_ROLL_THRESHOLD);
		deltaY = calculateDelta(pitch, RIGHT_PITCH_THRESHOLD);

		LogHelper.Info("Camera deltaX: " + deltaX + " Camera deltaY: " + deltaY);
		return deltaX != 0 || deltaY != 0;
	}

	public static void centerCrosshairs() {
		final ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth,
				mc.displayHeight);

		x = scaledResolution.getScaledWidth() / 2;
		y = scaledResolution.getScaledHeight() / 2;

		mcY = mc.displayHeight - (int) (y * scaledResolution.getScaleFactor());
		mcX = x * scaledResolution.getScaleFactor();
	}

	private static float getModifiedMultiplier(float currentMultiplier) {
		long elapsed = Minecraft.getSystemTime() - last0Reading;
		if (elapsed < 500)
		{
			float base = currentMultiplier * 0.5f;

			// increase the multiplier by 10% every 100 ms
			currentMultiplier = base + (base * elapsed) / 500;
			if (ControllerSettings.loggingLevel > 2)
				LogHelper.Info("CameraMultiplier " + currentMultiplier);
		}

		return currentMultiplier;
	}

	public static float calculateDelta(float delta, float threshold) {
		if (Math.abs(delta) < 0.01)
			return 0;

		if (delta < threshold && delta > -1 * threshold) {
			return 0;
		}

		float cameraMultiplier = ControllerSettings.inGameSensitivity;
		if (Math.abs(delta) < Math.abs(threshold / 2))
			cameraMultiplier *= 0.3;
		else if (Math.abs(delta) < Math.abs(threshold))
			cameraMultiplier *= 0.5;
		else
			cameraMultiplier = getModifiedMultiplier(cameraMultiplier);

		float finalDelta = delta * cameraMultiplier;

		// return at minimum a 1 or -1
		if (finalDelta < 1.0 && finalDelta > 0.0)
			finalDelta = 1.0f;
		else if (finalDelta < 0 && finalDelta > -1.0)
			finalDelta = -1.0f;

		return finalDelta;
	}

	public static void setXY(int newX, int newY) {
		final ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth,
				mc.displayHeight);

		x = newX;
		y = newY;
		mcX = x * scaledResolution.getScaleFactor();
		mcY = mc.displayHeight - (int) (y * scaledResolution.getScaleFactor());
	}

	public static void updateXY() {

		final ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth,
				mc.displayHeight);

		pollAxis();

		if (mc.currentScreen != null)
		{
			x += (int) deltaX;
			y += (int) deltaY;

			if (x < 0)
				x = 0;
			if (y < 0)
				y = 0;
			if (x > scaledResolution.getScaledWidth())
				x = scaledResolution.getScaledWidth() - 5;
			if (y > scaledResolution.getScaledHeight())
				y = scaledResolution.getScaledHeight() - 5;

			if (ControllerSettings.loggingLevel > 2)
				LogHelper.Debug("Virtual Mouse x: " + x + " y: " + y);

			mcY = mc.displayHeight - (int) (y * scaledResolution.getScaleFactor());
			mcX = x * scaledResolution.getScaleFactor();
			deltaX = 0;
			deltaY = 0;
		}
	}

}
