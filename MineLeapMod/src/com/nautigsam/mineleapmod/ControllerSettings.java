package com.nautigsam.mineleapmod;

// Common code

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Listener;
import com.nautigsam.mineleapmod.helpers.ConfigFile;
import com.nautigsam.mineleapmod.helpers.LogHelper;
import com.nautigsam.mineleapmod.helpers.ModVersionHelper;
import com.nautigsam.mineleapmod.inputevent.ControllerBinding;
import com.nautigsam.mineleapmod.inputevent.ControllerUtils;
import com.nautigsam.mineleapmod.lwjglVirtualInput.VirtualMouse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ControllerSettings
{
	private static Controller controller = null;
	private static Listener connectionListener = null;
	//public static final float defaultAxisDeadZone = 0.20f;
	//public static final float defaultAxisThreshhold = 0.7f;
	//public static final float defaultPovThreshhold = 0.9f;

	public static List<ControllerBinding> userDefinedBindings;

	public static Map<String, ControllerBinding> joyBindingsMap = null;

	//public static boolean useConstantCameraMovement = false;
	//public static boolean displayHints = false;
	// public static Controller joystick;
	public static int joyNo = -1;

	public static int inGameSensitivity = 25;
	public static int inMenuSensitivity = 10;
	//public static int scrollDelay = 50;

	public static int loggingLevel = 1;

	// used for some preliminary safe checks
	//private static int requiredMinButtonCount = 4;
	//private static int requiredButtonCount = 12;
	//private static int requiredAxisCount = 4;

	//private static Map<String, List<Integer>> validControllers;
	//private static Map<String, List<Integer>> inValidControllers;
	public static ControllerUtils controllerUtils;

	// modDisabled will not set up the event handlers and will therefore render
	// the mod inoperable
	public static boolean modDisabled = false;

	// inputEnabled will control whether the mod will continually poll the
	// selected joystick for data
	private static boolean inputEnabled = false;

	// suspending the controller will tell the main controller loop to stop
	// polling.
	// this is used during the controller setup screen when listening for
	// controller events to map to an action
	private static boolean suspendControllerInput = false;

	public static boolean invertYAxis = false;
	public static boolean grabMouse = false;

	private static ConfigFile config = null;

	public ControllerSettings(File configFile)
	{
		config = new ConfigFile(configFile);
		config.init();
		controllerUtils = new ControllerUtils();
		grabMouse = ControllerSettings.getGameOption("-Global-.GrabMouse").equals("true");
	}

	public void init()
	{
		LogHelper.Info("Minecraft LeapMotion Mod v" + ModVersionHelper.VERSION
				+ " by LeapTeam\n---");

		LogHelper.Info("Initializing Controller");

		connectionListener = new Listener() {
			public void onConnect()
			{
				LogHelper.Info("LeapMotion controller detected and connected!");
			}
		};
		controller = new Controller(connectionListener);
	}


	public static boolean hasDevice()
	{
		return controller.devices().count() > 0;
	}

	public static String getControllerName()
	{
		 return controller.devices().get(0).toString();
	}

	public static boolean isConnected()
	{
		return controller.isConnected();
	}

	public static boolean isInputEnabled()
	{
		return inputEnabled;
	}

	public static void setInputEnabled(boolean b)
	{
		unpressAll();
		if (!b)
		{
			LeapMotionMouse.setXY(0, 0);
			VirtualMouse.setXY(0, 0);
			inputEnabled = false;
			return;
		}
		inputEnabled = true;
		LeapMotionMouse.centerCrosshairs();
	}

	private static long suspendMax;
	private static long suspendStart;

	public static void suspendControllerInput(boolean suspend, long maxTicksToSuspend)
	{
		if (suspend)
		{
			suspendStart = Minecraft.getSystemTime();
			suspendMax = maxTicksToSuspend;
		}
		ControllerSettings.suspendControllerInput = suspend;
		LeapMotionMouse.UnpressButtons();
	}

	public static boolean isSuspended()
	{
		if (ControllerSettings.suspendControllerInput)
		{
			if (Minecraft.getSystemTime() - suspendStart > suspendMax)
			{
				ControllerSettings.suspendControllerInput = false;
			}
		}
		return ControllerSettings.suspendControllerInput;
	}

	public static void unpressAll()
	{
		KeyBinding.unPressAllKeys();
		VirtualMouse.unpressAllButtons();
	}

	public static String getGameOption(String optionKey)
	{
		return config.getConfigFileSetting(optionKey);
	}

	public static void setGameOption(String optionKey, String value)
	{
		config.setConfigFileSetting(optionKey, value);
		if (optionKey.contains("GrabMouse"))
		{
			grabMouse = Boolean.parseBoolean(value);
		}
	}
}
