package me.mindlessly.notenoughcoins;

import me.mindlessly.notenoughcoins.commands.NECCommand;
import me.mindlessly.notenoughcoins.commands.subcommand.*;
import me.mindlessly.notenoughcoins.configuration.ConfigHandler;
import me.mindlessly.notenoughcoins.utils.ApiHandler;
import me.mindlessly.notenoughcoins.utils.Blacklist;
import me.mindlessly.notenoughcoins.websocket.Client;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.io.IOException;

@Mod(modid = Reference.MOD_ID, name = Reference.NAME, version = Reference.VERSION)
public class Main {

    public static NECCommand commandManager = new NECCommand(new Subcommand[]{new Toggle(), new BlacklistCommand(),
        new MinProfit(), new MinDemand(), new MinPercentageProfit(), new MaxCost(), new Folder(), new Adjustment()});

    @EventHandler
    public void init(FMLInitializationEvent event) throws IOException {
        try {
            ConfigHandler.init();
        } catch (IOException e) {
            Reference.logger.error(e.getMessage());
        }
        ClientCommandHandler.instance.registerCommand(commandManager);
        ApiHandler.getItems();
        Blacklist.init();
        Client.start();
        Client.autoReconnect();
    }
}
