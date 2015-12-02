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
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class ControllerSettings
{
	private static Controller controller = null;
	private static Listener connectionListener = null;

	public static int loggingLevel = 1;
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

	private static ConfigFile config = null;

	public ControllerSettings(File configFile)
	{
		config = new ConfigFile(configFile);
		config.init();
		controllerUtils = new ControllerUtils();
	}

	public void init()
	{
		LogHelper.Info("Minecraft LeapMotion Mod v" + ModVersionHelper.VERSION
				+ " by LeapTeam\n---");

		LogHelper.Info("Initializing Controller");
	}

	public static void enableLeapMotion(String javaLibraryPath) {
		if (controller == null) {
			try
			{
				// Modify java.library.path in an horrible manner
				File dir = new File(javaLibraryPath);
				String oldJavaLibraryPath = System.getProperty("java.library.path");
				System.setProperty("java.library.path", oldJavaLibraryPath + File.pathSeparatorChar + dir.getAbsolutePath());
				synchronized(Runtime.getRuntime()) {
					try {
						Field field = ClassLoader.class.getDeclaredField("usr_paths");
						field.setAccessible(true);
						field.set(null, null);

						field = ClassLoader.class.getDeclaredField("sys_paths");
						field.setAccessible(true);
						field.set(null, null);
					} catch (NoSuchFieldException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}

				connectionListener = new Listener() {
					public void onConnect()
					{
						LogHelper.Info("LeapMotion controller detected and connected!");
					}
				};
				controller = new Controller(connectionListener);
				LeapMotionMouse.create(controller);
			} catch (Exception e) {
				LogHelper.Error("Can't find Leap native libraries in the provided directory ("+javaLibraryPath+")");
				controller = null;
				connectionListener = null;
			}
		}
	}

	public static boolean hasDevice()
	{
		return isLeapMotionEnabled() && controller.devices().count() > 0;
	}

	public static String getControllerName()
	{
		 return (isLeapMotionEnabled() ? controller.devices().get(0).toString() : "Native libraries not loaded");
	}

	public static boolean isConnected()
	{
		return isLeapMotionEnabled() && controller.isConnected();
	}

	public static boolean isInputEnabled()
	{
		return inputEnabled;
	}

	public static boolean isLeapMotionEnabled()
	{
		return LeapMotionMouse.isCreated();
	}

	public static LeapMotionMouse getLeapMotionMouse()
	{
		return LeapMotionMouse.getInstance();
	}

	public static void setInputEnabled(boolean b)
	{
		if (!isLeapMotionEnabled()) {
			LeapMotionMouse lmm = LeapMotionMouse.getInstance();
			unpressAll();
			if (!b)
			{
				lmm.setXY(0, 0);
				VirtualMouse.setXY(0, 0);
				inputEnabled = false;
				return;
			}
			inputEnabled = true;
			lmm.centerCrosshairs();
		}
	}

	private static long suspendMax;
	private static long suspendStart;

	public static void suspendControllerInput(boolean suspend, long maxTicksToSuspend)
	{
		if (isLeapMotionEnabled()) {
			if (suspend)
			{
				suspendStart = Minecraft.getSystemTime();
				suspendMax = maxTicksToSuspend;
			}
			ControllerSettings.suspendControllerInput = suspend;
			LeapMotionMouse.getInstance().UnpressButtons();
		}
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
	}
}
