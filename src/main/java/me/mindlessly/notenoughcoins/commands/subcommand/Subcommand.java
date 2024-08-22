package me.mindlessly.notenoughcoins.commands.subcommand;

import net.minecraft.command.ICommandSender;

public interface Subcommand {
	String getCommandName();

	/**
	 * Returns a boolean that indicates whether to show the subcommand in the help
	 * message
	 */
	boolean isHidden();

	/**
	 * Returns a String that will be shown after the subcommand name in the help
	 * message and error message. It will look like: /cc subcommand (usage)
	 */
	String getCommandUsage();

	/**
	 * Returns a String that will be show after the subcommand usage in the help
	 * message. It will look like: /cc subcommand (usage) (description [in a
	 * different color])
	 */
	String getCommandDescription();

	/**
	 * This function will be called upon execution of the subcommand, and should
	 * return a boolean deciding whether the command succeeded or not, which will
	 * send an error message if not. Note: The arguments will not include the
	 * subcommand itself, example: /cc subcommand arg1 arg2 will have {"arg1",
	 * "arg2"} as its args
	 */
	boolean processCommand(ICommandSender sender, String[] args);

}