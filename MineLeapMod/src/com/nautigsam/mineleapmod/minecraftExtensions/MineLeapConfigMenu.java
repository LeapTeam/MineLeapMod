package com.nautigsam.mineleapmod.minecraftExtensions;

import com.nautigsam.mineleapmod.ControllerSettings;
import com.nautigsam.mineleapmod.GameRenderHandler;
import com.nautigsam.mineleapmod.helpers.LogHelper;
import com.nautigsam.mineleapmod.helpers.McObfuscationHelper;
import net.minecraft.client.gui.*;

import java.io.IOException;

public class MineLeapConfigMenu extends GuiScreen
{
	// start of text at top
	private int labelYStart = 2;

	// top button parameters
	// Y start of the buttons at the top of the screen
	private int buttonYStart_top = labelYStart + 12;
	// Y end of the buttons at the top of the screen
	private int buttonYEnd_top;
	// X start of the buttons at the top of the screen
	public int buttonXStart_top;

	// private int sensitivityXStart;
	private int sensitivityYStart;
	private int controllerStringY;

	// determined by size of screen
	public int controllerButtonWidth;

	// bottom button parameters
	private int buttonYStart_bottom;
	public int bottomButtonWidth = 70;

	private GuiScreen parentScr;
	private GuiTextField leapLibraryPath;

	private int sensitivity_menuStart;
	private int sensitivity_gameStart;

	private enum ButtonsEnum
	{
		control, done, mouseMenu
	}

	public MineLeapConfigMenu(GuiScreen parent)
	{
		super();
		parentScr = parent;
		sensitivity_menuStart = ControllerSettings.inMenuSensitivity;
		sensitivity_gameStart = ControllerSettings.inGameSensitivity;
	}

	@Override
	public void initGui()
	{
		controllerButtonWidth = width - width / 5;
		if (controllerButtonWidth > 310)
			controllerButtonWidth = 310;
		buttonXStart_top = (width - controllerButtonWidth) / 2;
		buttonYStart_bottom = height - 20;

		controllerStringY = buttonYStart_top;

		int buttonYOffset = 10;
		// controller button
		addButton(new GuiButton(100, buttonXStart_top, buttonYStart_top + buttonYOffset, controllerButtonWidth, 20,
				getControllerName()), ControllerSettings.isInputEnabled());
		buttonYOffset += 30;
		leapLibraryPath = new GuiTextField(200, getFontRenderer(), buttonXStart_top, buttonYStart_top + buttonYOffset, controllerButtonWidth, 20);
		leapLibraryPath.setFocused(false);
		buttonYOffset += 30;
		addButton(new GuiButton(101, buttonXStart_top, buttonYStart_top + buttonYOffset, bottomButtonWidth, 20,
				"Refresh"));

		// add bottom buttons
		int buttonNum = 0;
		int numBottomButtons = 2;
		int bottomButtonStart = buttonXStart_top + controllerButtonWidth / 2 - (bottomButtonWidth / 2)
				* numBottomButtons;

		addButton(new GuiButton(500, bottomButtonStart + bottomButtonWidth * buttonNum++, buttonYStart_bottom,
				bottomButtonWidth, 20, sGet("gui.done")));

		addButton(new GuiButton(520, bottomButtonStart + bottomButtonWidth * buttonNum++, buttonYStart_bottom,
				bottomButtonWidth, 20, sGet("controlMenu.mouse") + " " + sGet("joy.menu")));
	}

	@Override
	public void onGuiClosed()
	{
		LogHelper.Info("MineLeapConfigMenu OnGuiClosed");
		ControllerSettings.suspendControllerInput(false, 0);
	}

	public void mouseClicked(int a, int b, int c)
	{
		leapLibraryPath.mouseClicked(a, b, c);
		try
		{
			super.mouseClicked(a, b, c);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void actionPerformed(GuiButton guiButton)
	{
		LogHelper.Info("Action performed on buttonID " + getButtonId(guiButton));

		switch (getButtonId(guiButton))
		{
		case 100: // Controller button
			toggleController();
			break;
		case 101: // Refresh button
			ControllerSettings.enableLeapMotion(leapLibraryPath.getText()); // Load LeapMotion
			break;
		case 500: // Done
			mc.displayGuiScreen(this.parentScr);
			break;
		case 520: // Mouse menu
			GameRenderHandler.allowOrigControlsMenu = true;
			mc.displayGuiScreen(new GuiControls(this, mc.gameSettings));
			break;
		}
	}

	private String getControllerName()
	{
		String ret = "";

		if (!ControllerSettings.isConnected() || !ControllerSettings.hasDevice())
			return sGet("controlMenu.noControllers");

		ret += ControllerSettings.getControllerName() + ": ";
		ret += ControllerSettings.isInputEnabled() ? sGet("options.on") : sGet("options.off");
		return ret;
	}

	@Override
	public void drawScreen(int par1, int par2, float par3)
	{
		drawDefaultBackground();

		String titleText = String.format("MineLeap Mod - %s - %s", sGet("controls.title"),
				sGet("controlMenu.toggleInstructions"));
		this.drawCenteredString(getFontRenderer(), titleText, width / 2, labelYStart, -1);
		leapLibraryPath.drawTextBox();

		// CONTROLLER NAME BUTTON
		// PREV NEXT OTHER

		super.drawScreen(par1, par2, par3);
	}

	/**
	 * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
	 */
	protected void keyTyped(char c, int code)
	{
		if (leapLibraryPath.isFocused())
		{
			leapLibraryPath.textboxKeyTyped(c, code);
		}
		else
		{
			if (c == ' ' && ControllerSettings.isConnected())
			{
				toggleController();
			}
			else
			{
				try{
					super.keyTyped(c, code);}
				catch(java.io.IOException e){}
			}
		}
	}

	private void changeButtonText(int buttonIndex, String text)
	{
		((GuiButton) buttonList.get(buttonIndex)).displayString = text;
	}

	/*
	 * private void enableDisableButton(int buttonIndex, boolean enable) { ((GuiButton) buttonList.get(buttonIndex)).enabled = enable; }
	 */

	private void toggleController()
	{
		LogHelper.Info("Enable/disable input");
		ControllerSettings.setInputEnabled(!ControllerSettings.isInputEnabled());
		updateControllerButton();
	}

	private void updateControllerButton()
	{
		changeButtonText(ButtonsEnum.control.ordinal(), getControllerName());
	}

	// Obfuscation & back porting helpers -- here and not in ObfuscationHelper
	// because accessing protected methods

	@SuppressWarnings("unchecked")
	private void addButton(GuiButton guiButton, boolean enabled)
	{
		if (!enabled)
			guiButton.enabled = false;
		// field_146292_n.add(guiButton);
		buttonList.add(guiButton);
	}

	@SuppressWarnings("unchecked")
	private void addButton(GuiButton guiButton)
	{
		buttonList.add(guiButton);
	}

	private int getButtonId(GuiButton guiButton)
	{
		// return guiButton.field_146127_k;
		return guiButton.id;
	}

	public FontRenderer getFontRenderer()
	{
		// return this.field_146289_q;
		// return this.fontRenderer;
		return this.fontRendererObj;
	}

	public String sGet(String inputCode)
	{
		return McObfuscationHelper.lookupString(inputCode);
	}
}
