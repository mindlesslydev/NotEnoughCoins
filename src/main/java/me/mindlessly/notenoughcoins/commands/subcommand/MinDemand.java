package me.mindlessly.notenoughcoins.commands.subcommand;

import me.mindlessly.notenoughcoins.configuration.ConfigHandler;
import me.mindlessly.notenoughcoins.utils.Constants;
import me.mindlessly.notenoughcoins.utils.Utils;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class MinDemand implements Subcommand {
	public MinDemand() {

	}

	public static void updateConfig() {

	}

	@Override
	public String getCommandName() {
		return Constants.MIN_DEMAND;
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
		return "Set your minimum demand";
	}

	@Override
	public boolean processCommand(ICommandSender sender, String[] args) {
		if (args.length != 1) {
			return false;
		}

		try {
			int minDemand = Integer.parseInt(args[0]);
			if(minDemand <0 || minDemand > 50) {
				sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Only accepting values between 0 and 50!"));
				return false;
			}
			ConfigHandler.write(Constants.MIN_DEMAND, Utils.gson.toJsonTree(minDemand));
			sender.addChatMessage(new ChatComponentText(
					EnumChatFormatting.GREEN + "Successfully updated Minimum Demand to " + String.valueOf(minDemand)));
			return true;
		} catch (Exception e) {
			sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "That is not a valid integer!"));
			return false;
		}

	}
}