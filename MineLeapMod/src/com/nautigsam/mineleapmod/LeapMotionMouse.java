package com.nautigsam.mineleapmod;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.nautigsam.mineleapmod.helpers.LogHelper;
import com.nautigsam.mineleapmod.lwjglVirtualInput.VirtualMouse;
import com.nautigsam.mineleapmod.lwjglVirtualInput.VirtualKeyboard;
import com.nautigsam.mineleapmod.helpers.McObfuscationHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.settings.GameSettings;


public final class LeapMotionMouse {

	private static volatile LeapMotionMouse instance = null;

	private Minecraft mc = FMLClientHandler.instance().getClient();
	private GameSettings settings = Minecraft.getMinecraft().gameSettings;

	private float inGameSensitivity = 25f;

	// Hand angle constants
	private float LEFT_ROLL_THRESHOLD = 0.25f;
	private float LEFT_ROLL_INIT = 0.75f;
	private float LEFT_PITCH_THRESHOLD = 0.25f;
	private float LEFT_PITCH_INIT = 0.35f;
	private float RIGHT_ROLL_THRESHOLD = 0.15f;
	private float RIGHT_ROLL_INIT = -0.55f;
	private float RIGHT_PITCH_THRESHOLD = 0.15f;
	private float RIGHT_PITCH_INIT = 0.35f;

	// hit and release values
	private static float RIGHT_GRAB_TOLERANCE = 0.0f;
	private static int positive = 10;
	private static int fistHandTicks = 0;
	private static int flatHandTicks = 0;

	// last delta movement of axis
	public float deltaX;
	public float deltaY;

	// last virtual mouse position
	private int x = 0;
	private int y = 0;

	// values that Minecraft expects when reading the actual mouse
	private int mcX = 0;
	private int mcY = 0;

	private long lastAxisReading = 0;
	private long guiPollTimeout = 30;
	private long gamePollTimeout = 10;
	public long last0Reading = 0;
	public long lastNon0Reading = 0;

	private long lastFrameID = 0;
	private Controller controller;

	private LeapMotionMouse(Controller leapController) {
		super();
		controller = leapController;
	}

	private static final LeapMotionMouse getInstance(Controller leapController) {
		if (LeapMotionMouse.instance == null) {
			synchronized (LeapMotionMouse.class) {
				if (LeapMotionMouse.instance == null) {
					LeapMotionMouse.instance = new LeapMotionMouse(leapController);
				}
			}
		}
		return LeapMotionMouse.instance;
	}

	public static final LeapMotionMouse getInstance() {
		return LeapMotionMouse.getInstance(null);
	}

	public static final void create(Controller leapController) {
		getInstance(leapController);
	}

	public static final boolean isCreated() {
		return LeapMotionMouse.instance != null;
	}

	public Frame nextFrame() {
		if (controller != null) {
			Frame frame = controller.frame();

			// NOTE: should this be done?
			// if( frame.id() == lastFrameID ) return;
			// lastFrameID = frame.id();

			return frame;
		}
		return null;
	}

	public Hand getRightHand() {
		Frame f = nextFrame();
		if (f == null) return null;
		Hand hand =	f.hands().rightmost();
		if (!hand.isValid()) return null;
		return hand;
	}

	public Hand getLeftHand() {
		Frame f = nextFrame();
		if (f == null) return null;
		Hand hand =	f.hands().leftmost();
		if (!hand.isValid()) return null;
		return hand;
	}

	public int getmcX() {
		return mcX;
	}

	public int getmcY() {
		return mcY;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	private void leftButtonDown() {
		if (!VirtualMouse.isButtonDown(VirtualMouse.LEFT_BUTTON))
		{
			VirtualMouse.setXY(mcX, mcY);
			boolean onlyIfNotHeld = true;
			VirtualMouse.holdMouseButton(VirtualMouse.LEFT_BUTTON, onlyIfNotHeld);
		}
	}

	private void leftButtonUp() {
		if (VirtualMouse.isButtonDown(VirtualMouse.LEFT_BUTTON))
		{
			boolean onlyIfHeld = true;
			VirtualMouse.releaseMouseButton(VirtualMouse.LEFT_BUTTON, onlyIfHeld);
		}
	}

	public boolean isLeftButtonDown() {
		return VirtualMouse.isButtonDown(VirtualMouse.LEFT_BUTTON);
	}

	private void rightButtonDown() {
		if (!VirtualMouse.isButtonDown(VirtualMouse.RIGHT_BUTTON))
		{
			VirtualMouse.setXY(mcX, mcY);
			boolean onlyIfNotHeld = true;
			VirtualMouse.holdMouseButton(VirtualMouse.RIGHT_BUTTON, onlyIfNotHeld);
		}
	}

	private void rightButtonUp() {
		if (VirtualMouse.isButtonDown(VirtualMouse.RIGHT_BUTTON))
		{
			boolean onlyIfHeld = true;
			VirtualMouse.releaseMouseButton(VirtualMouse.RIGHT_BUTTON, onlyIfHeld);
		}
	}

	public boolean isRightButtonDown() {
		return VirtualMouse.isButtonDown(VirtualMouse.RIGHT_BUTTON);
	}

	private void moveRight() {
		holdDownKeyboardKey(McObfuscationHelper.keyCode(settings.keyBindRight));
	}

	private void moveLeft() {
		holdDownKeyboardKey(McObfuscationHelper.keyCode(settings.keyBindLeft));
	}

	private void moveForward() {
		holdDownKeyboardKey(McObfuscationHelper.keyCode(settings.keyBindForward));
	}

	private void moveBackward() {
		holdDownKeyboardKey(McObfuscationHelper.keyCode(settings.keyBindBack));
	}

	private void stopMoving() {
		releaseKeyboardKey(settings.keyBindRight);
		releaseKeyboardKey(settings.keyBindLeft);
		releaseKeyboardKey(settings.keyBindBack);
		releaseKeyboardKey(settings.keyBindForward);
	}

	private void pressKeyboardKey(int keycode) {
		VirtualKeyboard.pressKey(keycode);
	}

	private void holdDownKeyboardKey(int keycode) {
		VirtualKeyboard.holdKey(keycode, true);
	}

	private void releaseKeyboardKey(int keycode) {
		VirtualKeyboard.releaseKey(keycode, false);
	}

	public void UnpressButtons() {
		rightButtonUp();
		leftButtonUp();
	}

	public boolean pollNeeded(boolean inGui) {
		if (Minecraft.getSystemTime() - lastAxisReading < (inGui ? guiPollTimeout : gamePollTimeout))
			return false;
		return true;
	}

	public void pollAxis() {
		pollAxis(mc.currentScreen != null);
	}

	// pollAxis() will update the x and y position of the
	// 'mouse' with respect to right hand's movements.
	public boolean pollAxis(boolean inGui) {
		if (!pollNeeded(inGui))
			return false;

		// TODO: how to handle GUI
		if (inGui) return false;

		boolean rightHandPoll = pollRightHandAngles() || pollRightFist();
		boolean leftHandPoll = pollLeftHandAngles();

		return rightHandPoll || leftHandPoll;
	}

	private boolean pollRightHandAngles() {
		Hand hand =	getRightHand();
		if (hand == null) return false;

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

		// invert roll (issue with incorrect x-axis rotation)
		roll = -1 * roll;
		deltaX = calculateDelta(roll, RIGHT_ROLL_THRESHOLD);
		deltaY = calculateDelta(pitch, RIGHT_PITCH_THRESHOLD);

		if (ControllerSettings.loggingLevel > 2)
			LogHelper.Debug("Camera deltaX: " + deltaX + " Camera deltaY: " + deltaY);

		return deltaX != 0 || deltaY != 0;
	}

	private boolean pollRightFist() {
		Hand hand =	getRightHand();
		if (hand == null) return false;

		boolean changeOccurred = false;
		float grabStrength = hand.grabStrength();

		// 'debouncing' the fist and flat hand gestures
		if (grabStrength + RIGHT_GRAB_TOLERANCE >= 1.0f) {
			if (fistHandTicks < positive) fistHandTicks += 1; // prevent overflowing
		} else {
			changeOccurred = true;
			fistHandTicks = 0;
			leftButtonUp();
		}
		if (grabStrength - RIGHT_GRAB_TOLERANCE <= 0.0f) {
			if (flatHandTicks < positive) flatHandTicks += 1; // prevent overflowing
		} else {
			changeOccurred = true;
			flatHandTicks = 0;
			rightButtonUp();
		}

		if (fistHandTicks >= positive) {
			changeOccurred = true;
			leftButtonDown();
		} else if (flatHandTicks >= positive) {
			changeOccurred = true;
			rightButtonDown();
		}

		return changeOccurred;
	}

	private boolean pollLeftHandAngles() {
		Hand hand =	getLeftHand();
		if (hand == null) {
			stopMoving();
			return false;
		}

		// Left Hand controls camera

		// Angles:
		// pitch (angle x-axis) (fingers up/down) [-1.5708,1.5708]
		// yaw (angle y-axis) (sideways rotation) [0,6.28319]
		// roll (angle z-axis) (thumbs up/down) [-1.5708,1.5708]

		float pitch = hand.direction().pitch();
		float roll = hand.palmNormal().roll();
		// substract the initial/rest zone
		roll -= LEFT_ROLL_INIT;
		pitch -= LEFT_PITCH_INIT;
		// NOTE: normalizing to 1.0, not sure if necessary
		roll /= 1.5708f;
		pitch /= 1.5708f;

		boolean moved = false;
		if (roll > LEFT_ROLL_THRESHOLD) {
			moveLeft();
			moved = true;
		} else if (roll < LEFT_ROLL_THRESHOLD) {
			moveRight();
			moved = true;
		}

		if (pitch > LEFT_PITCH_THRESHOLD) {
			moveBackward();
			moved = true;
		} else if (pitch < LEFT_PITCH_THRESHOLD) {
			moveForward();
			moved = true;
		}

		// make sure to stop any movement
		if (!moved) stopMoving();

		return moved;
	}

	public void centerCrosshairs() {
		final ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth,
				mc.displayHeight);

		x = scaledResolution.getScaledWidth() / 2;
		y = scaledResolution.getScaledHeight() / 2;

		mcY = mc.displayHeight - (int) (y * scaledResolution.getScaleFactor());
		mcX = x * scaledResolution.getScaleFactor();
	}

	private float getModifiedMultiplier(float currentMultiplier) {
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

	public float calculateDelta(float delta, float threshold) {
		if (Math.abs(delta) < 0.01)
			return 0;

		if (delta < threshold && delta > -1 * threshold) {
			return 0;
		}

		float cameraMultiplier = inGameSensitivity;
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

	public void setXY(int newX, int newY) {
		final ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth,
				mc.displayHeight);

		x = newX;
		y = newY;
		mcX = x * scaledResolution.getScaleFactor();
		mcY = mc.displayHeight - (int) (y * scaledResolution.getScaleFactor());
	}

	public void updateXY() {

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
