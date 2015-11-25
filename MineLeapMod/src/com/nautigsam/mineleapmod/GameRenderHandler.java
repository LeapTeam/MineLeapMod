package com.nautigsam.mineleapmod;

import java.util.ArrayList;
import java.util.List;

import com.nautigsam.mineleapmod.helpers.ModVersionHelper;
import com.nautigsam.mineleapmod.inputevent.ControllerBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import org.lwjgl.input.Controllers;
import org.lwjgl.input.Mouse;

import com.nautigsam.mineleapmod.helpers.LogHelper;
import com.nautigsam.mineleapmod.helpers.McGuiHelper;
import com.nautigsam.mineleapmod.helpers.McObfuscationHelper;
import com.nautigsam.mineleapmod.inputevent.ControllerInputEvent;
import com.nautigsam.mineleapmod.lwjglVirtualInput.VirtualMouse;
import com.nautigsam.mineleapmod.minecraftExtensions.JoypadConfigMenu;



public class GameRenderHandler
{
	private static Minecraft mc = Minecraft.getMinecraft();
	public static int reticalColor = 0xFFFFFFFF;
	// boolean to allow the original controls menu.
	// normally we override the controls menu when seen
	public static boolean allowOrigControlsMenu = false;
	private static long lastInGuiTick = 0;
	private static long lastInGameTick = 0;
	private static boolean lastFlansModCheckValue = false;

	public static List<String> preRenderGuiBucket = new ArrayList<String>();
	public static List<String> preRenderGameBucket = new ArrayList<String>();

	public static boolean mouseDetected = false;

	public static void HandlePreRender()
	{
		try
		{
			if (mc.currentScreen != null && !ControllerSettings.isSuspended())
			{
				if (mc.currentScreen instanceof GuiControls)
				{
					if (!allowOrigControlsMenu)
					{
						ReplaceControlScreen((GuiControls) mc.currentScreen);
					}
				}
				else if (!(mc.currentScreen instanceof JoypadConfigMenu))
				{
					allowOrigControlsMenu = false;
				}

				if (InGuiCheckNeeded())
				{
					if (Mouse.isInsideWindow()
							&& Minecraft.getSystemTime() - LeapMotionMouse.lastNon0Reading > 1000)
					{
						if (Mouse.getDX() != 0 || Mouse.getDY() != 0)
						{
							if (ControllerSettings.loggingLevel > 2)
							{
								LogHelper.Info("Mouse sharing of screen detected");
							}
							mouseDetected = true;
						}
					}
					else
					{
						mouseDetected = false;
					}
					// This call here re-points the mouse position that Minecraft picks
					// up to determine if it should do the Hover over button effect.
					if (!mouseDetected)
						VirtualMouse.setXY(LeapMotionMouse.getmcX(), LeapMotionMouse.getmcY());
					HandleDragAndScrolling();
				}
			}
		}
		catch (Exception ex)
		{
			LogHelper.Fatal("Joypad mod unhandled exception caught in HandlePreRender! " + ex.toString());
		}
	}

	public static void HandlePostRender()
	{
		if (ControllerSettings.isSuspended())
			return;

		try
		{
			if (InGuiCheckNeeded())
			{
				// fixes issue with transitioning from inGui to game movement continuing
				if (Minecraft.getSystemTime() - lastInGameTick < 50)
				{
					ControllerSettings.unpressAll();
					Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = false;
				}

				DrawRetical();
			}

			if (InGameCheckNeeded())
			{
				// fixes issue with transitioning from inGame to Gui movement continuing
				if (Minecraft.getSystemTime() - lastInGuiTick < 50)
				{
					ControllerSettings.unpressAll();
					Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = false;
				}

				UpdateInGameCamera();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			LogHelper.Fatal("Joypad mod unhandled exception caught in HandlePostRender! " + ex.toString());
		}

	}

	private static long lastFlansModCheckTick = 0;

	public static void HandleClientStartTick()
	{
		if (ControllerSettings.isSuspended())
			return;

		if (Minecraft.getSystemTime() - lastFlansModCheckTick > 750)
		{
			lastFlansModCheckValue = mc.currentScreen != null
					&& mc.currentScreen.getClass().toString().contains("GuiDriveableController");
			lastFlansModCheckTick = Minecraft.getSystemTime();
		}

		if (InGuiCheckNeeded())
		{
			HandleJoystickInGui();
			lastInGuiTick = Minecraft.getSystemTime();
		}

		if (InGameCheckNeeded())
		{
			HandleJoystickInGame();
			lastInGameTick = Minecraft.getSystemTime();
		}
	}

	public static void HandleClientEndTick()
	{
		// does nothing currently
	}

	private static void DrawRetical()
	{

		if (mc.currentScreen == null || !ControllerSettings.isInputEnabled())
			return;

		LeapMotionMouse.updateXY();
		int x = LeapMotionMouse.getX();
		int y = LeapMotionMouse.getY();

		Gui.drawRect(x - 3, y, x + 4, y + 1, reticalColor);
		Gui.drawRect(x, y - 3, x + 1, y + 4, reticalColor);
	}

	private static void UpdateInGameCamera()
	{
		if (mc.thePlayer != null)
		{
			if (lastFlansModCheckValue)
			{
				if (LeapMotionMouse.pollAxis(false))
				{
					float multiplier = 4f * mc.gameSettings.mouseSensitivity;
					VirtualMouse.moveMouse(
							(int) (LeapMotionMouse.deltaX * multiplier),
							(int) (LeapMotionMouse.deltaY * multiplier));
				}
				else
				{
					VirtualMouse.moveMouse(0, 0);
				}
			}
			else if (LeapMotionMouse.pollAxis(false))
			{
				mc.thePlayer.setAngles(LeapMotionMouse.deltaX, LeapMotionMouse.deltaY);
			}
		}
	}

	private static void HandleDragAndScrolling()
	{

		if (VirtualMouse.isButtonDown(0) || VirtualMouse.isButtonDown(1))
		{
			// VirtualMouse.moveMouse(LeapMotionMouse.getmcX(), LeapMotionMouse.getmcY());
			McGuiHelper.guiMouseDrag(LeapMotionMouse.getX(), LeapMotionMouse.getY());
			VirtualMouse.setMouseButton(LeapMotionMouse.isLeftButtonDown() ? 0 : 1, true);
		}
	}

	private static void HandleJoystickInGui()
	{
		// update mouse coordinates
		// LeapMotionMouse.updateXY();
		if (!mouseDetected)
			VirtualMouse.setXY(LeapMotionMouse.getmcX(), LeapMotionMouse.getmcY());

		while (Controllers.next() && mc.currentScreen != null)
		{
			// ignore controller events in the milliseconds following in GAME
			// controlling
			if (Minecraft.getSystemTime() - lastInGameTick < 200)
				continue;

			if (ControllerSettings.loggingLevel > 3)
			{
				try
				{
					ControllerInputEvent inputEvent = ControllerSettings.controllerUtils.getLastEvent(
							Controllers.getController(ControllerSettings.joyNo), Controllers.getEventControlIndex());
					if (inputEvent != null)
					{
						LogHelper.Info("Input event " + inputEvent.toString()
								+ " triggered.  Finding associated binding");
					}
				}
				catch (Exception ex)
				{
					LogHelper.Error("Exception caught debugging controller input events: " + ex.toString());
				}
			}
		}
	}

	private static void HandleJoystickInGame()
	{

		while (Controllers.next() && (mc.currentScreen == null || lastFlansModCheckValue))
		{
			// ignore controller events in the milliseconds following in GUI
			// controlling
			if (Minecraft.getSystemTime() - lastInGuiTick < 100)
				continue;

			if (ControllerSettings.loggingLevel > 3)
			{
				try
				{
					ControllerInputEvent inputEvent = ControllerSettings.controllerUtils.getLastEvent(
							Controllers.getController(ControllerSettings.joyNo), Controllers.getEventControlIndex());
					if (inputEvent != null)
					{
						LogHelper.Info("Input event " + inputEvent.toString()
								+ " triggered.  Finding associated binding");
					}
				}
				catch (Exception ex)
				{
					LogHelper.Error("Exception caught debugging controller input events: " + ex.toString());
				}
			}

			mc.inGameHasFocus = true;
		}
	}

	private static void ReplaceControlScreen(GuiControls gui)
	{
		if (!(mc.currentScreen instanceof JoypadConfigMenu))
		{
			try
			{
				LogHelper.Debug("Replacing control screen");
				String[] names = McObfuscationHelper.getMcVarNames("parentScreen");
				GuiScreen parent = ObfuscationReflectionHelper.getPrivateValue(GuiControls.class, (GuiControls) gui,
						names[0], names[1]);
				mc.displayGuiScreen(new JoypadConfigMenu(parent));
			}
			catch (Exception ex)
			{
				LogHelper.Error("Failed to get parent of options gui.  aborting. Exception " + ex.toString());
				return;
			}
		}
	}

	public static boolean InGameCheckNeeded()
	{
		if (!CheckIfModEnabled() || mc.thePlayer == null || (mc.currentScreen != null && !lastFlansModCheckValue))
		{
			return false;
		}

		return true;
	}

	public static boolean InGuiCheckNeeded()
	{
		if (!CheckIfModEnabled() || mc.currentScreen == null || lastFlansModCheckValue)
		{
			return false;
		}

		return true;
	}

	public static boolean CheckIfModEnabled()
	{
		if (mc == null || !ControllerSettings.isInputEnabled())
		{
			return false;
		}

		return true;
	}
}
