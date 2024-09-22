package me.mindlessly.notenoughcoins.commands.subcommand;

import me.mindlessly.notenoughcoins.utils.ApiHandler;
import me.mindlessly.notenoughcoins.utils.Blacklist;
import me.mindlessly.notenoughcoins.utils.EnchantmentData;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import java.util.Map;
import java.util.Set;

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

        // Normalize the API names by removing color codes
        String normalizedItemName = getNormalizedApiName(ApiHandler.items, name);
        String normalizedPetName = getNormalizedJsonObjectName(ApiHandler.pets, name);
        String normalizedSkinName = getNormalizedJsonObjectName(ApiHandler.skins, name);

        if (normalizedItemName != null) {
            item = true;
        }
        if (normalizedPetName != null) {
            pet = true;
        }
        if (normalizedSkinName != null) {
            skin = true;
        }

        if (!item && !pet && !skin) {
            sender.addChatMessage(new ChatComponentText("Item does not exist, Note: CaSe SeNsItIvE"));
            return false;
        }

        if (args[0].equals("add")) {
            if (item) {
                if (modifiers != null) {
                    // Convert user-friendly enchant names to internal IDs
                    String convertedModifiers = convertEnchantNames(modifiers);
                    Blacklist.add(ApiHandler.items.get(normalizedItemName), convertedModifiers);
                } else {
                    Blacklist.add(ApiHandler.items.get(normalizedItemName));
                }
            } else if (pet) {
                Blacklist.addPet(ApiHandler.pets.getAsJsonArray(normalizedPetName));
            } else {
                Blacklist.addSkin(ApiHandler.skins.get(normalizedSkinName).getAsString());
            }
            sender.addChatMessage(new ChatComponentText("Successfully added " + EnumChatFormatting.GREEN + name
                + EnumChatFormatting.WHITE + " to the blacklist"));
        } else if (args[0].equals("remove")) {
            if (item) {
                if (modifiers != null) {
                    // Convert user-friendly enchant names to internal IDs
                    String convertedModifiers = convertEnchantNames(modifiers);
                    Blacklist.remove(ApiHandler.items.get(normalizedItemName), convertedModifiers);
                } else {
                    Blacklist.remove(ApiHandler.items.get(normalizedItemName));
                }
            } else if (pet) {
                Blacklist.removePet(ApiHandler.pets.getAsJsonArray(normalizedPetName));
            } else {
                Blacklist.removeSkin(ApiHandler.skins.get(normalizedSkinName).getAsString());
            }
            sender.addChatMessage(new ChatComponentText("Successfully removed " + EnumChatFormatting.GREEN + name
                + EnumChatFormatting.WHITE + " from the blacklist"));
        } else {
            return false;
        }
        return true;
    }
	
    private String convertEnchantNames(String modifiers) {
        String[] parts = modifiers.split("\\s+");
        StringBuilder converted = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            // Check if this part is an enchantment name
            String enchantId = getEnchantmentId(part);
            if (enchantId != null) {
                converted.append(enchantId);
            } else {
                converted.append(part);
            }
            if (i < parts.length - 1) {
                converted.append(" ");
            }
        }
        return converted.toString();
    }

    private String getEnchantmentId(String enchantName) {
        for (Map.Entry<String, EnchantmentData> entry : EnchantmentData.getEnchantmentData().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(enchantName.replace(" ", "_"))) {
                return entry.getValue().getId();
            }
        }
        return null; // Return null if no matching enchantment is found
    }

    // Method to normalize a name by removing color codes
    private String normalizeName(String input) {
        // Remove any color codes (%%...%%)
        return input.replaceAll("%%[a-zA-Z_]+%%", "").trim().toLowerCase();
    }

    // Method to get the normalized API name for Map
    private String getNormalizedApiName(Map<String, ?> apiMap, String inputName) {
        String normalizedInputName = normalizeName(inputName);
        for (String key : apiMap.keySet()) {
            String normalizedKey = normalizeName(key);
            if (normalizedKey.equals(normalizedInputName)) {
                return key; // Return the original key with color codes
            }
        }
        return null;
    }

    // Method to get the normalized API name for JsonObject
    private String getNormalizedJsonObjectName(JsonObject jsonObject, String inputName) {
        String normalizedInputName = normalizeName(inputName);
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String normalizedKey = normalizeName(entry.getKey());
            if (normalizedKey.equals(normalizedInputName)) {
                return entry.getKey(); // Return the original key with color codes
            }
        }
        return null;
    }
}
