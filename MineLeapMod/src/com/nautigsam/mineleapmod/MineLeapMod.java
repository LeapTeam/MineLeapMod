package com.nautigsam.mineleapmod;

/*
 * Main class for Joypad mod. This initializes everything.
 */

import com.nautigsam.mineleapmod.helpers.LogHelper;
import com.nautigsam.mineleapmod.helpers.ModVersionHelper;
import com.nautigsam.mineleapmod.lwjglVirtualInput.VirtualKeyboard;
import com.nautigsam.mineleapmod.lwjglVirtualInput.VirtualMouse;
import com.nautigsam.mineleapmod.minecraftExtensions.JoypadMouseHelper;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;


@Mod(modid = MineLeapMod.MODID, name = MineLeapMod.NAME, version = ModVersionHelper.VERSION + "-" + MineLeapMod.MINVERSION
		+ MineLeapMod.REVISION)
// 1.6.4
// @NetworkMod(serverSideRequired = false)
public class MineLeapMod
{
	public static final String MODID = "MineLeapMod";
	public static final String NAME = "MineLeap Mod";
	public static final float MINVERSION = 0.1f;
	public static final String REVISION = "";

	private static ControllerSettings controllerSettings;

	private ModVersionHelper modHelper;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		LogHelper.Info("preInit");
		controllerSettings = new ControllerSettings(event.getSuggestedConfigurationFile());
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		LogHelper.Info("init");
		try
		{
			if (!(FMLClientHandler.instance().getClient().mouseHelper instanceof net.minecraft.util.MouseHelper))
			{
				LogHelper.Warn("Replacing Mousehelper that may have already been replaced by another mod!");
			}
			FMLClientHandler.instance().getClient().mouseHelper = new JoypadMouseHelper();
			LogHelper.Info("Replaced mousehelper in Minecraft with JoypadMouseHelper");
		}
		catch (Exception ex)
		{
			LogHelper.Warn("Unable to exchange mousehelper. Game may grab mouse from keyboard players!");
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		LogHelper.Info("postInit");
		controllerSettings.init();
		try
		{
			VirtualKeyboard.create();
		}
		catch (Exception ex)
		{
			LogHelper.Fatal("Unable to initialize VirtualKeyboard.  Limited compatibility with some mods likely. "
					+ ex.toString());
		}

		try
		{
			VirtualMouse.create();
		}
		catch (Exception ex)
		{
			LogHelper.Fatal("Unable to initialize VirtualMouse.  Unable to continue. " + ex.toString());
			ControllerSettings.modDisabled = true;
		}

//		try
//		{
//			McGuiHelper.create();
//		}
//		catch (Exception ex)
//		{
//			LogHelper.Fatal("Unable to initialize McGuiHelper.  Unable to continue. " + ex.toString());
//			ControllerSettings.modDisabled = true;
//		}

		modHelper = new ModVersionHelper();
		modHelper.gameInit();
	}

}
