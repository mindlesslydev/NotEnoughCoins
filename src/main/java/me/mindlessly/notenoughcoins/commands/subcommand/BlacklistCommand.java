package me.mindlessly.notenoughcoins.commands.subcommand;

import me.mindlessly.notenoughcoins.utils.ApiHandler;
import me.mindlessly.notenoughcoins.utils.Blacklist;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class BlacklistCommand implements Subcommand {
	public BlacklistCommand() {

	}

	public static void updateConfig() {

	}

	@Override
	public String getCommandName() {
		return "blacklist";
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
		return "Add or remove items from the blacklist";
	}

	@Override
	public boolean processCommand(ICommandSender sender, String[] args) {
		if (args.length < 2) {
			return false;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			sb.append(args[i]).append(" ");
		}
		String name = sb.toString().trim();

		String modifiers = null;

		if (name.contains("where")) {
			modifiers = name.split("where")[1].trim();
			name = name.split("where")[0].trim();
		}
		boolean item = false;
		boolean pet = false;
		boolean skin = false;

		if (ApiHandler.items.containsKey(name)) {
			item = true;
		}
		if (ApiHandler.pets.has(name)) {
			pet = true;
		}
		if(ApiHandler.skins.has(name)) {
			skin = true;
		}

		if (!item && !pet && !skin) {
			sender.addChatMessage(new ChatComponentText("Item does not exist, Note: CaSe SeNsItIvE"));
			return false;
		}
		if (args[0].equals("add")) {
			if (item) {
				if (modifiers != null) {
					Blacklist.add(ApiHandler.items.get(name), modifiers);
				} else {
					Blacklist.add(ApiHandler.items.get(name));
				}
			} else if (pet) {
				Blacklist.addPet(ApiHandler.pets.get(name).getAsJsonArray());
			} else if (skin) {
				Blacklist.addSkin(ApiHandler.skins.get(name).getAsString());
			}
			sender.addChatMessage(new ChatComponentText("Successfully added " + EnumChatFormatting.GREEN + name
					+ EnumChatFormatting.WHITE + " to the blacklist"));
		} else if (args[0].equals("remove")) {
			if (item) {
				if (modifiers != null) {
					Blacklist.remove(ApiHandler.items.get(name), modifiers);
				} else {
					Blacklist.remove(ApiHandler.items.get(name));
				}
			} else if (pet) {
				Blacklist.removePet(ApiHandler.pets.get(name).getAsJsonArray());
			} else if (skin) {
				Blacklist.removeSkin(ApiHandler.skins.get(name).getAsString());
			}
			sender.addChatMessage(new ChatComponentText("Successfully removed " + EnumChatFormatting.GREEN + name
					+ EnumChatFormatting.WHITE + " from the blacklist"));

		} else {
			return false;
		}
		return true;
	}
}