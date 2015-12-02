package com.nautigsam.mineleapmod.helpers;

import com.nautigsam.mineleapmod.ControllerSettings;
import com.nautigsam.mineleapmod.MineLeapMod;
import com.nautigsam.mineleapmod.inputevent.ControllerBinding;
import com.nautigsam.mineleapmod.inputevent.ControllerBinding.BindingOptions;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.input.Controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigFile
{
	private Configuration config;
	private File _configFile;
	private String userName;
	private String userCategory;

	private String globalCat = "-Global-";

	public ConfigFile(File configFile)
	{
		_configFile = configFile;
		reload();
	}

	private void reload()
	{
		config = new Configuration(_configFile, true);
	}

	public void init()
	{
		config.load();

		ControllerSettings.loggingLevel = config.get(globalCat, "LoggingLevel", 1).getInt();

		userName = "unknown";

		if (FMLClientHandler.instance().getClient() != null && FMLClientHandler.instance().getClient().thePlayer != null)
		{
			userName = FMLClientHandler.instance().getClient().thePlayer.getName();
		}

		userCategory = "-"+userName+"-";

		if (config.hasCategory(userCategory.toLowerCase()))
		{
			// using older case insensitive version
			config.removeCategory(config.getCategory(userCategory.toLowerCase()));
		}

		updateKey(getDefaultCategory(), "ConfigVersion", String.valueOf(MineLeapMod.MINVERSION), true);
	}

	public String getDefaultCategory()
	{
		return "-"+userName+"-";
	}

	public String getConfigFileSetting(String categoryKey)
	{
		int lastIndex = categoryKey.lastIndexOf('.');
		String category = categoryKey.substring(0, lastIndex);
		String key = categoryKey.substring(lastIndex + 1);
		return config.get(category, key, "false").getString();
	}

	public void setConfigFileSetting(String categoryKey, String value)
	{
		int lastIndex = categoryKey.lastIndexOf('.');
		String category = categoryKey.substring(0, lastIndex);
		String key = categoryKey.substring(lastIndex + 1);
		setConfigFileSetting(category, key, value);
	}

	public void setConfigFileSetting(String category, String key, String value)
	{
		updateKey(category, key, value, true);
	}

	private boolean deleteKey(String category, String key)
	{
		if (!config.hasCategory(category))
			return false;

		if (null != config.getCategory(category).remove(key))
		{
			config.save();
			LogHelper.Info("Deleted category " + category + " key " + key);
			return true;
		}

		return false;
	}

	// boolean true returns if a new key was created
	// false means key was updated
	private boolean updateKey(String category, String key, String value, boolean save)
	{
		boolean bRet = false;
		try
		{
			bRet = !deleteKey(category, key);
			config.get(category, key, value);

			LogHelper.Info(String.format("updateKey %s %s:%s with %s", bRet ? "created" : "updated", category, key,
					value));

			if (save)
				config.save();
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed trying to save key " + category + " value " + key + ":" + value + ". Exception: "
					+ ex.toString());
		}
		return bRet;
	}
}
