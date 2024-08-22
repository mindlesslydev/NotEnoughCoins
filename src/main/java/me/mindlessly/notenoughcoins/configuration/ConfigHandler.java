package me.mindlessly.notenoughcoins.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.mindlessly.notenoughcoins.utils.Utils;
import net.minecraft.client.Minecraft;

public class ConfigHandler {

	private static File configFile;
	private static JsonObject config;

	public static void init() throws IOException {
		configFile = new File(Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + "//NotEnoughCoins//nec.json");
		if (configFile.exists() && !configFile.isDirectory()) {
			InputStream is = new FileInputStream(configFile);
			String jsonTxt = IOUtils.toString(is, "UTF-8");
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
					new OutputStreamWriter(new FileOutputStream(configFile), "utf-8"))) {
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

	public static void write(String key, JsonElement jsonTree) {
		config.add(key, jsonTree);
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), "utf-8"))) {
			writer.write(config.toString());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static JsonObject getConfig() {
		return config;
	}

	public static void remove(String string) {
		JsonObject config = getConfig();
		if (config.has(string)) {
			config.remove(string);
		}
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), "utf-8"))) {
			writer.write(config.toString());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
