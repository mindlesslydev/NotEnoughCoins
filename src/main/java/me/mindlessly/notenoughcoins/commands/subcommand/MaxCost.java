package me.mindlessly.notenoughcoins.commands.subcommand;
import me.mindlessly.notenoughcoins.configuration.ConfigHandler;
import me.mindlessly.notenoughcoins.utils.Constants;
import me.mindlessly.notenoughcoins.utils.Utils;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class MaxCost implements Subcommand {
	public MaxCost() {

	}

	public static void updateConfig() {

	}

	@Override
	public String getCommandName() {
		return Constants.MAX_COST;
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
		return "Set the maximum buying price for a flip";
	}

	@Override
	public boolean processCommand(ICommandSender sender, String[] args) {
		if (args.length != 1) {
			return false;
		}
		
		int maxCost;
		if (args[0].matches("\\d+[mkb]")) {
			maxCost = Utils.convertAbbreviatedNumber(args[0]);
		}else if(args[0].equalsIgnoreCase("purse")) {
			ConfigHandler.remove(Constants.MAX_COST);
			sender.addChatMessage(new ChatComponentText(
					EnumChatFormatting.GREEN + "Successfully updated Max Cost to use purse balance"));
			return true;
		}else {
			try {
				maxCost = Integer.parseInt(args[0]);
			} catch (Exception e) {
				sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "That is not a valid integer!"));
				return false;
			}
		}
		if (maxCost < 0) {
			sender.addChatMessage(
					new ChatComponentText(EnumChatFormatting.RED + "Only accepting integers greater than or equal to 0!"));
			return false;
		}
		ConfigHandler.write(Constants.MAX_COST, Utils.gson.toJsonTree(maxCost));
		sender.addChatMessage(new ChatComponentText(
				EnumChatFormatting.GREEN + "Successfully updated Max Cost to " + String.valueOf(maxCost)));
		return true;

	}
}