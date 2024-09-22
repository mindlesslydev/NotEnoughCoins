package me.mindlessly.notenoughcoins.utils;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Blacklist {

    public final static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static JsonObject json;
    private static File blacklistFile;

    public static void init() throws IOException {
        blacklistFile = new File(
            Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + "//NotEnoughCoins//blacklist.json");
        if (blacklistFile.exists() && !blacklistFile.isDirectory()) {
            InputStream is = new FileInputStream(blacklistFile);
            String jsonTxt = IOUtils.toString(is, "UTF-8");
            json = new JsonParser().parse(jsonTxt).getAsJsonObject();
            convert();
        } else {
            blacklistFile.getParentFile().mkdirs();
            blacklistFile.createNewFile();
            try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(blacklistFile), "utf-8"))) {
                json = new JsonObject();
                json.add("items", new JsonObject());
                writer.write(json.toString());
                writer.close();
            }
        }
    }

    private static void convert() {
        if (json.get("items").isJsonArray()) {
            JsonObject items = new JsonObject();
            for (JsonElement jsonElement : json.get("items").getAsJsonArray()) {
                JsonObject info = new JsonObject();
                info.add("all", gson.toJsonTree(true));
                items.add(jsonElement.getAsString(), info);
            }
            json.add("items", items);
        }
        save();
    }

    public static void add(String item) {
        JsonObject info = new JsonObject();
        info.add("all", gson.toJsonTree(true));
        json.getAsJsonObject("items").add(item, info);
        save();
    }

    public static void add(String item, String modifiers) {
        JsonObject info;
        JsonObject items = json.getAsJsonObject("items");
        if (items.has(item)) {
            info = items.get(item).getAsJsonObject();
        } else {
            info = new JsonObject();
        }

        JsonArray enchants = new JsonArray();
        JsonArray stars = new JsonArray();
        JsonArray reforges = new JsonArray();

        if (info.has("enchants")) {
            enchants = info.getAsJsonArray("enchants");
        }
        if (info.has("stars")) {
            stars = info.getAsJsonArray("stars");
        }
        if (info.has("reforges")) {
            reforges = info.getAsJsonArray("reforges");
        }

        List<String> attributes = Arrays.asList(modifiers.split("\\s*,\\s*"));
        String type = ApiHandler.itemTypes.get(item);

        for (String attribute : attributes) {
            String[] parts = attribute.split(" ");
            String enchantName = parts[0].replace(" ", "_").toUpperCase();
            String comparison = "=";
            int level = 1;

            if (parts.length >= 3) {
                comparison = parts[1];
                level = Integer.parseInt(parts[2]);
            }

            EnchantmentData enchantData = EnchantmentData.getEnchantmentData().get(enchantName);
            if (enchantData != null) {
                addEnchantsByComparison(enchants, enchantData.getId(), comparison, level, enchantData.getMax());
            }

            // Handle reforges
            for (JsonElement reforge : ApiHandler.reforges) {
                if (attribute.equalsIgnoreCase(reforge.getAsString())) {
                    reforges.add(Utils.gson.toJsonTree(attribute.toLowerCase()));
                    break;
                }
            }

            // Handle stars
            if (attribute.startsWith("stars")) {
                stars = handleStarAddition(attribute, stars);
            }

            // Handle other attributes (minprofit, minpercent, clean)
            if (attribute.startsWith("minprofit")) {
                double minProfit;
                String toConvert = attribute.split("minprofit ")[1];
                if (toConvert.matches("\\d+[mkb]")) {
                    minProfit = Utils.convertAbbreviatedNumber(toConvert);
                } else {
                    minProfit = Integer.valueOf(toConvert);
                }
                info.add("minprofit", Utils.gson.toJsonTree(minProfit));
            }

            if (attribute.startsWith("minpercent")) {
                double minPercent = Double.valueOf(attribute.split("minpercent ")[1]);
                info.add("minpercent", Utils.gson.toJsonTree(minPercent));
            }

            if (attribute.startsWith("clean")) {
                info.add("clean", Utils.gson.toJsonTree(true));
            }
        }

        info.add("enchants", enchants);
        info.add("stars", stars);
        info.add("reforges", reforges);
        items.add(item, info);
        save();
    }

    private static void addEnchantsByComparison(JsonArray enchants, String enchantId, String comparison, int level, int maxLevel) {
        switch (comparison) {
            case ">=":
                for (int i = level; i <= maxLevel; i++) {
                    addEnchantIfNotExists(enchants, enchantId, i);
                }
                break;
            case "<=":
                for (int i = 1; i <= level; i++) {
                    addEnchantIfNotExists(enchants, enchantId, i);
                }
                break;
            case ">":
                for (int i = level + 1; i <= maxLevel; i++) {
                    addEnchantIfNotExists(enchants, enchantId, i);
                }
                break;
            case "<":
                for (int i = 1; i < level; i++) {
                    addEnchantIfNotExists(enchants, enchantId, i);
                }
                break;
            case "=":
            default:
                addEnchantIfNotExists(enchants, enchantId, level);
                break;
        }
    }

    private static void addEnchantIfNotExists(JsonArray enchants, String enchantName, int level) {
        String enchantEntry = enchantName + "_" + level;
        boolean alreadyExists = false;

        for (JsonElement element : enchants) {
            if (element.getAsString().equalsIgnoreCase(enchantEntry)) {
                alreadyExists = true;
                break;
            }
        }

        if (!alreadyExists) {
            enchants.add(new JsonPrimitive(enchantEntry));
        }
    }

    public static void remove(String item) {
        JsonObject items = json.getAsJsonObject("items");
        if (items.has(item)) {
            items.remove(item);
        }
        save();
    }

public static void remove(String item, String modifiers) {
        JsonObject info;
        JsonObject items = json.getAsJsonObject("items");
        if (items.has(item)) {
            info = items.get(item).getAsJsonObject();
        } else {
            return;
        }

        List<String> attributes = Arrays.asList(modifiers.split("\\s*,\\s*"));
        JsonArray enchants = info.has("enchants") ? info.getAsJsonArray("enchants") : new JsonArray();
        JsonArray stars = info.has("stars") ? info.getAsJsonArray("stars") : new JsonArray();
        JsonArray reforges = info.has("reforges") ? info.getAsJsonArray("reforges") : new JsonArray();

        for (String attribute : attributes) {
            String[] parts = attribute.split(" ");
            String enchantName = parts[0].replace(" ", "_").toUpperCase();
            String comparison = "=";
            int level = 1;

            if (parts.length >= 3) {
                comparison = parts[1];
                level = Integer.parseInt(parts[2]);
            }

            EnchantmentData enchantData = EnchantmentData.getEnchantmentData().get(enchantName);
            if (enchantData != null) {
                enchants = removeEnchantsByComparison(enchants, enchantData.getId(), comparison, level, enchantData.getMax());
            } else if (attribute.startsWith("stars")) {
                stars = handleStarRemoval(attribute, stars);
            } else {
                for (JsonElement reforge : ApiHandler.reforges) {
                    if (attribute.equalsIgnoreCase(reforge.getAsString())) {
                        reforges = removeAttribute(reforges, attribute.toLowerCase());
                        break;
                    }
                }
            }

            if (attribute.startsWith("minprofit") && info.has("minprofit")) {
                info.remove("minprofit");
            }

            if (attribute.startsWith("minpercent") && info.has("minpercent")) {
                info.remove("minpercent");
            }

            if (attribute.startsWith("clean") && info.has("clean")) {
                info.remove("clean");
            }
        }

        if (enchants.size() == 0) {
            info.remove("enchants");
        } else {
            info.add("enchants", enchants);
        }

        if (stars.size() == 0) {
            info.remove("stars");
        } else {
            info.add("stars", stars);
        }

        if (reforges.size() == 0) {
            info.remove("reforges");
        } else {
            info.add("reforges", reforges);
        }

        if (info.entrySet().size() == 0) {
            items.remove(item);
        } else {
            items.add(item, info);
        }
        save();
    }


private static JsonArray removeEnchantsByComparison(JsonArray enchants, String enchantId, String comparison, int level, int maxLevel) {
        JsonArray updatedEnchants = new JsonArray();
        for (JsonElement element : enchants) {
            String enchant = element.getAsString();
            String[] enchantParts = enchant.split("_");
            String enchantBase = String.join("_", Arrays.copyOfRange(enchantParts, 0, enchantParts.length - 1));
            int enchantLevel = Integer.parseInt(enchantParts[enchantParts.length - 1]);

            if (enchantBase.equalsIgnoreCase(enchantId)) {
                boolean keep = false;
                switch (comparison) {
                    case ">=":
                        keep = enchantLevel < level;
                        break;
                    case "<=":
                        keep = enchantLevel > level;
                        break;
                    case ">":
                        keep = enchantLevel <= level;
                        break;
                    case "<":
                        keep = enchantLevel >= level;
                        break;
                    case "=":
                        keep = enchantLevel != level;
                        break;
                }
                if (keep) {
                    updatedEnchants.add(element);
                }
            } else {
                updatedEnchants.add(element);
            }
        }
        return updatedEnchants;
    }

    private static JsonArray removeAttribute(JsonArray array, String attribute) {
        JsonArray updatedArray = new JsonArray();
        for (JsonElement element : array) {
            if (!element.getAsString().equalsIgnoreCase(attribute)) {
                updatedArray.add(element);
            }
        }
        return updatedArray;
    }

private static JsonArray handleStarRemoval(String attribute, JsonArray stars) {
    JsonArray updatedStars = new JsonArray();

    if (attribute.contains(">=")) {
        int start = Integer.parseInt(attribute.split(">=")[1].trim());
        for (JsonElement star : stars) {
            if (Integer.parseInt(star.getAsString()) < start) {
                updatedStars.add(star);
            }
        }
    } else if (attribute.contains("<=")) {
        int end = Integer.parseInt(attribute.split("<=")[1].trim());
        // Remove all stars less than or equal to 'end'
        for (JsonElement star : stars) {
            if (Integer.parseInt(star.getAsString()) > end) {
                updatedStars.add(star);
            }
        }
    } else if (attribute.contains(">")) {
        int start = Integer.parseInt(attribute.split(">")[1].trim());
        for (JsonElement star : stars) {
            if (Integer.parseInt(star.getAsString()) > start) {
                updatedStars.add(star);
            }
        }
    } else if (attribute.contains("<")) {
        int end = Integer.parseInt(attribute.split("<")[1].trim());
        for (JsonElement star : stars) {
            if (Integer.parseInt(star.getAsString()) < end) {
                updatedStars.add(star);
            }
        }
    } else if (attribute.contains("=")) {
        int exact = Integer.parseInt(attribute.split("=")[1].trim());
        for (JsonElement star : stars) {
            if (Integer.parseInt(star.getAsString()) != exact) {
                updatedStars.add(star);
            }
        }
    }

    return updatedStars;
}

    private static JsonArray handleStarAddition(String attribute, JsonArray stars) {
        if (attribute.contains(">=")) {
            String starValue = StringUtils.strip(attribute.split(">=")[1]);
            int start = Integer.valueOf(starValue);
            if (start > 10 || start < 0) {
                return stars;
            }
            for (int star = start; star < 11; star++) {
                stars.add(Utils.gson.toJsonTree(String.valueOf(star)));
            }
        } else if (attribute.contains("<=")) {
            int end = Integer.valueOf(attribute.split("<=")[1]);
            if (end > 10 || end < 0) {
                return stars;
            }
            for (int star = 0; star < end + 1; star++) {
                stars.add(Utils.gson.toJsonTree(String.valueOf(star)));
            }
        } else if (attribute.contains(">")) {
            int start = Integer.valueOf(attribute.split(">")[1]) + 1;
            if (start > 10 || start < 0) {
                return stars;
            }
            for (int star = start; star < 11; star++) {
                stars.add(Utils.gson.toJsonTree(String.valueOf(star)));
            }
        } else if (attribute.contains("<")) {
            int end = Integer.valueOf(attribute.split("<")[1]);
            if (end > 11 || end < 0) {
                return stars;
            }
            for (int star = 0; star < end; star++) {
                stars.add(Utils.gson.toJsonTree(String.valueOf(star)));
            }
        } else if (attribute.contains("=")) {
            int start = Integer.valueOf(attribute.split("=")[1]);
            if (start < 0 || start > 10) {
                return stars;
            }
            stars.add(Utils.gson.toJsonTree(String.valueOf(start)));
        }
        return stars;
    }

    public static void addPet(JsonArray array) {
        for (JsonElement element : array) {
            JsonObject info = new JsonObject();
            info.add("all", gson.toJsonTree(true));
            json.getAsJsonObject("items").add(element.getAsString(), info);
        }
        save();
    }

    public static void removePet(JsonArray array) {
        for (JsonElement element : array) {
            if (json.getAsJsonObject("items").has(element.getAsString())) {
                json.getAsJsonObject("items").remove(element.getAsString());
            }
        }
        save();
    }

    public static void addSkin(String item) {
        JsonObject info = new JsonObject();
        info.add("all", gson.toJsonTree(true));
        json.getAsJsonObject("items").add(item, info);
        save();
    }

    public static void removeSkin(String item) {
        if (json.getAsJsonObject("items").has(item)) {
            json.getAsJsonObject("items").remove(item);
        }
        save();
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(blacklistFile)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            System.err.println("Error saving to file: " + e.getMessage());
        }
    }
}