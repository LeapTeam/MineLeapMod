package com.nautigsam.mineleapmod.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.FMLClientHandler;

public class McKeyBindHelper
{
	public static KeyBinding getMinecraftKeyBind(String bindingKey)
	{
		for (KeyBinding kb : FMLClientHandler.instance().getClient().gameSettings.keyBindings)
		{
			String keyInputString = McObfuscationHelper.getKeyDescription(kb);
			if (keyInputString.compareTo(bindingKey) == 0)
			{
				return kb;
			}
		}
		return null;
	}
}
