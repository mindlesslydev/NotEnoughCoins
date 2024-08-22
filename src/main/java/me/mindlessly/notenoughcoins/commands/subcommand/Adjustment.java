package me.mindlessly.notenoughcoins.commands.subcommand;
import me.mindlessly.notenoughcoins.configuration.ConfigHandler;
import me.mindlessly.notenoughcoins.utils.Constants;
import me.mindlessly.notenoughcoins.utils.Utils;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class Adjustment implements Subcommand {
	public Adjustment() {

	}

	public static void updateConfig() {

	}

	@Override
	public String getCommandName() {
		return Constants.ADJUSTMENT;
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
		return "Set a percentage adjustment for prices";
	}

	@Override
	public boolean processCommand(ICommandSender sender, String[] args) {
		if (args.length != 1) {
			return false;
		}

		try {
			int adjustment = Integer.parseInt(args[0]);
			if(adjustment < 0) {
				sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Only accepting integers greater than or equal to 0!"));
				return false;
			}
			ConfigHandler.write(Constants.ADJUSTMENT, Utils.gson.toJsonTree(adjustment));
			sender.addChatMessage(new ChatComponentText(
					EnumChatFormatting.GREEN + "Successfully updated Adjustment to " + String.valueOf(adjustment)));
			return true;
		} catch (Exception e) {
			sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "That is not a valid integer!"));
			return false;
		}

	}
}