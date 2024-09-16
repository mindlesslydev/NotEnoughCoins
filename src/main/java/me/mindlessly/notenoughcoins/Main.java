package me.mindlessly.notenoughcoins;

import me.mindlessly.notenoughcoins.commands.NECCommand;
import me.mindlessly.notenoughcoins.commands.subcommand.*;
import me.mindlessly.notenoughcoins.configuration.ConfigHandler;
import me.mindlessly.notenoughcoins.utils.ApiHandler;
import me.mindlessly.notenoughcoins.utils.Blacklist;
import me.mindlessly.notenoughcoins.websocket.Client;
import me.mindlessly.notenoughcoins.keybind.KeybindHandler; // Import KeybindHandler
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge; // Import MinecraftForge
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.FMLLog; // Import for logging

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

        // Register keybindings
        KeybindHandler.registerKeybinds();
        // Register this class to listen for events
        MinecraftForge.EVENT_BUS.register(this);

        FMLLog.info("[NEC] Mod initialized and keybindings registered.");
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        // Delegate to keybind handler
        KeybindHandler.onKeyInput();
        FMLLog.info("[NEC] Key input event detected.");
    }
}
