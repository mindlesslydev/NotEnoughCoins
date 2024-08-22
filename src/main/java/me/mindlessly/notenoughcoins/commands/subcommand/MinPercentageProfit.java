package me.mindlessly.notenoughcoins.commands.subcommand;

import me.mindlessly.notenoughcoins.configuration.ConfigHandler;
import me.mindlessly.notenoughcoins.utils.Constants;
import me.mindlessly.notenoughcoins.utils.Utils;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class MinPercentageProfit implements Subcommand {
	public MinPercentageProfit() {

	}

	public static void updateConfig() {

	}

	@Override
	public String getCommandName() {
		return Constants.MIN_PERCENTAGE_PROFIT;
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
		return "Set your minimum percentage profit";
	}

	@Override
	public boolean processCommand(ICommandSender sender, String[] args) {
		if (args.length != 1) {
			return false;
		}

		try {
			int minProfit = Integer.parseInt(args[0]);
			if (minProfit < 0) {
				sender.addChatMessage(new ChatComponentText(
						EnumChatFormatting.RED + "Only accepting integers greater than or equal to 0!"));
				return false;
			}
			ConfigHandler.write(Constants.MIN_PERCENTAGE_PROFIT, Utils.gson.toJsonTree(minProfit));
			sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN
					+ "Successfully updated Minimum Percentage Profit to " + String.valueOf(minProfit)));
			return true;
		} catch (Exception e) {
			sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "That is not a valid integer!"));
			return false;
		}

	}
}