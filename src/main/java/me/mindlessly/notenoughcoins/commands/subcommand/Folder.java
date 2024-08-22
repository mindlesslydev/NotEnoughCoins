package me.mindlessly.notenoughcoins.commands.subcommand;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;

public class Folder implements Subcommand {
	public Folder() {

	}

	public static void updateConfig() {

	}

	@Override
	public String getCommandName() {
		return "folder";
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public String getCommandUsage() {
		return "";
	}

	@Override
	public String getCommandDescription() {
		return "Open NEC folder in system explorer";
	}

	@Override
	public boolean processCommand(ICommandSender sender, String[] args) {
		try {
			Desktop.getDesktop().open(new File(Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + "//NotEnoughCoins//"));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}