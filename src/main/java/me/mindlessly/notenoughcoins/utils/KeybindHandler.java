package me.mindlessly.notenoughcoins.keybind;

import me.mindlessly.notenoughcoins.websocket.Client; // Import the Client class
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeybindHandler {
    // Define a new keybinding
    public static final KeyBinding OPEN_MOST_PROFITABLE_DEAL = new KeyBinding(
            "Open most profitable deal", // Description of the keybind
            Keyboard.KEY_P,               // Default key (P)
            "NotEnoughCoins" // Key category
    );

    public static void registerKeybinds() {
        // Register the keybinding in Minecraft
        ClientRegistry.registerKeyBinding(OPEN_MOST_PROFITABLE_DEAL);
    }

    // Method to handle the key press event
    public static void onKeyInput() {
        // Check if the key is pressed
        if (OPEN_MOST_PROFITABLE_DEAL.isPressed()) {
            Client.openNextProfitableDeal(); // Updated method call to the new method
        }
    }
}
