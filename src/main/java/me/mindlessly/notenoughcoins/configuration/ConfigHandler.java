package me.mindlessly.notenoughcoins.configuration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.mindlessly.notenoughcoins.Reference;
import me.mindlessly.notenoughcoins.utils.Utils;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ConfigHandler {

    private static File configFile;
    private static JsonObject config;

    /**
     * Method to load config
     *
     * @throws IOException - Thrown if there is an error reading the config file
     */
    public static void init() throws IOException {
        configFile = new File(Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + "//NotEnoughCoins//nec.json");
        if (configFile.exists() && !configFile.isDirectory()) {
            InputStream is = Files.newInputStream(configFile.toPath());
            String jsonTxt = IOUtils.toString(is, StandardCharsets.UTF_8);
            config = new JsonParser().parse(jsonTxt).getAsJsonObject();
            // Compatibility fix for some users
            if (!config.has("mindemand")) {
                config.add("mindemand", Utils.gson.toJsonTree(0));
                config.add("minprofit", Utils.gson.toJsonTree(0));
                config.add("minpercent", Utils.gson.toJsonTree(0));
            }
        } else {
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
            try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(configFile.toPath()), StandardCharsets.UTF_8))) {
                config = new JsonObject();
                config.add("toggle", Utils.gson.toJsonTree(false));
                config.add("mindemand", Utils.gson.toJsonTree(0));
                config.add("minprofit", Utils.gson.toJsonTree(0));
                config.add("minpercent", Utils.gson.toJsonTree(0));
                writer.write(config.toString());
                writer.close();
            }
        }
    }

    /**
     * Method to write to config file
     *
     * @param key      - The field being edited
     * @param jsonTree - The config data structure
     */
    public static void write(String key, JsonElement jsonTree) {
        config.add(key, jsonTree);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(configFile.toPath()), StandardCharsets.UTF_8))) {
            writer.write(config.toString());
            writer.close();
        } catch (Exception e) {
            Reference.logger.error(e.getMessage());
        }
    }

    public static JsonObject getConfig() {
        return config;
    }


    /**
     * Method to remove a field from the config
     *
     * @param key The field to remove
     */
    public static void remove(String key) {
        JsonObject config = getConfig();
        if (config.has(key)) {
            config.remove(key);
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(configFile.toPath()), StandardCharsets.UTF_8))) {
            writer.write(config.toString());
        } catch (Exception e) {
            Reference.logger.error(e.getMessage());
        }
    }

}
