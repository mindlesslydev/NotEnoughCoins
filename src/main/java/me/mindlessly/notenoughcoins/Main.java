package me.mindlessly.notenoughcoins;

import java.io.IOException;

import me.mindlessly.notenoughcoins.commands.NECCommand;
import me.mindlessly.notenoughcoins.commands.subcommand.Adjustment;
import me.mindlessly.notenoughcoins.commands.subcommand.BlacklistCommand;
import me.mindlessly.notenoughcoins.commands.subcommand.Folder;
import me.mindlessly.notenoughcoins.commands.subcommand.MaxCost;
import me.mindlessly.notenoughcoins.commands.subcommand.MinDemand;
import me.mindlessly.notenoughcoins.commands.subcommand.MinPercentageProfit;
import me.mindlessly.notenoughcoins.commands.subcommand.MinProfit;
import me.mindlessly.notenoughcoins.commands.subcommand.Subcommand;
import me.mindlessly.notenoughcoins.commands.subcommand.Toggle;
import me.mindlessly.notenoughcoins.configuration.ConfigHandler;
import me.mindlessly.notenoughcoins.utils.ApiHandler;
import me.mindlessly.notenoughcoins.utils.Blacklist;
import me.mindlessly.notenoughcoins.websocket.Client;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.NAME, version = Reference.VERSION)
public class Main {

	public static NECCommand commandManager = new NECCommand(new Subcommand[] { new Toggle(), new BlacklistCommand(),
			new MinProfit(), new MinDemand(), new MinPercentageProfit(), new MaxCost(), new Folder(), new Adjustment()});

	@EventHandler
	public void init(FMLInitializationEvent event) throws IOException {
		try {
			ConfigHandler.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ClientCommandHandler.instance.registerCommand(commandManager);
		ApiHandler.getItems();
		Blacklist.init();
		Client.start();
		Client.autoReconnect();
	}
}
