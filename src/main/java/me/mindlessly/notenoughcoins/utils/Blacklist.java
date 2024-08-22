package me.mindlessly.notenoughcoins.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.mindlessly.notenoughcoins.configuration.ConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class Blacklist {

	private static File blacklistFile;
	public static JsonObject json;
	public final static Gson gson = new GsonBuilder().setPrettyPrinting().create();

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

	// Convert from legacy blacklist JsonArray format to new JsonObject format
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

	// TODO - Change to interfacing with the JsonObject to add/remove items and add
	// params for enchants etc
	// Add item to blacklist
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
		JsonArray gems = new JsonArray();
		JsonArray stars = new JsonArray();
		JsonArray scrolls = new JsonArray();
		JsonArray reforges = new JsonArray();
		JsonArray enrichments = new JsonArray();

		if (info.has("enchants")) {
			enchants = info.getAsJsonArray("enchants");
		}
		if (info.has("gems")) {
			gems = info.getAsJsonArray("gems");
		}

		if (info.has("stars")) {
			stars = info.getAsJsonArray("stars");
		}

		if (info.has("scrolls")) {
			scrolls = info.getAsJsonArray("scrolls");
		}

		if (info.has("reforges")) {
			reforges = info.getAsJsonArray("reforges");
		}

		if (info.has("enrichments")) {
			enrichments = info.getAsJsonArray("enrichments");
		}
		List<String> attributes = Arrays.asList(modifiers.split("\\s*,\\s*"));
		String type = ApiHandler.itemTypes.get(item);
		JsonArray enchantArray = null;
		if (ApiHandler.enchants.has(type)) {
			enchantArray = ApiHandler.enchants.getAsJsonArray(type);
		}
		for (String attribute : attributes) {
			// If translated to enchant, check if enchant is valid for type of item
			// Deal with level stuff
			// If not already in list, add enchant to list
			String attributeTranslated = attribute;
			attributeTranslated = attributeTranslated.replace(" ", "_");
			int i = attributeTranslated.lastIndexOf("_");

			if (enchantArray != null && i > -1) {
				for (JsonElement enchant : enchantArray) {
					String enchantID = attributeTranslated.substring(0, i);
					if (enchant.getAsString().equalsIgnoreCase(enchantID)) {
						enchants.add(Utils.gson.toJsonTree(attributeTranslated.toUpperCase()));
						break;
					} else if (enchant.getAsString().equalsIgnoreCase("ULTIMATE_" + enchantID)) {
						enchants.add(Utils.gson.toJsonTree("ULTIMATE_" + attributeTranslated.toUpperCase()));
						break;
					}
				}
			}

			for (JsonElement reforge : ApiHandler.reforges) {
				if (attribute.equalsIgnoreCase(reforge.getAsString())) {
					reforges.add(Utils.gson.toJsonTree(attribute.toLowerCase()));
					break;
				}
			}

			// TODO - Finish different star params
			if (attribute.startsWith("stars")) {
				if (attribute.contains(">=")) {
					String starValue = StringUtils.strip(attribute.split(">=")[1]);
					int start = Integer.valueOf(starValue);
					if (start > 10 || start < 0) {
						continue;
					}
					for (int star = start; star < 11; star++) {
						stars.add(Utils.gson.toJsonTree(String.valueOf(star)));
					}
					continue;
				} else if (attribute.contains("<=")) {
					int end = Integer.valueOf(attribute.split("<=")[1]);
					if (end > 10 || end < 0) {
						continue;
					}
					for (int star = 0; star < end + 1; star++) {
						stars.add(Utils.gson.toJsonTree(String.valueOf(star)));
					}
					continue;

				} else if (attribute.contains(">")) {
					int start = Integer.valueOf(attribute.split(">")[1]) + 1;
					if (start > 10 || start < 0) {
						continue;
					}
					for (int star = start; star < 11; star++) {
						stars.add(Utils.gson.toJsonTree(String.valueOf(star)));
					}
					continue;

				} else if (attribute.contains("<")) {
					int end = Integer.valueOf(attribute.split("<")[1]);
					if (end > 11 || end < 0) {
						continue;
					}
					for (int star = 0; star < end; star++) {
						stars.add(Utils.gson.toJsonTree(String.valueOf(star)));
					}
					continue;

				} else if (attribute.contains("=")) {
					int start = Integer.valueOf(attribute.split("=")[1]);
					if (start < 0 || start > 10) {
						continue;
					}
					stars.add(Utils.gson.toJsonTree(String.valueOf(start)));
					continue;
				}

			}

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
			// Handle other attributes

		}
		info.add("enchants", enchants);
		info.add("stars", stars);
		info.add("reforges", reforges);
		items.add(item, info);
		save();

	}

	// Remove item from blacklist
	public static void remove(String item) {
		JsonObject items = json.getAsJsonObject("items");
		if (items.has(item)) {
			items.remove(item);
		}
		save();
	}

	public static void remove(String item, String modifiers) {
		JsonObject info;
		List<String> attributes = Arrays.asList(modifiers.split("\\s*,\\s*"));
		JsonObject items = json.getAsJsonObject("items");
		if (items.has(item)) {
			info = items.get(item).getAsJsonObject();
		} else {
			return;
		}

		JsonArray enchants = new JsonArray();
		JsonArray gems = new JsonArray();
		JsonArray stars = new JsonArray();
		JsonArray scrolls = new JsonArray();
		JsonArray reforges = new JsonArray();
		JsonArray enrichments = new JsonArray();

		boolean all = false;

		if (info.has("all")) {
			if (info.get("all").getAsBoolean()) {
				all = true;
				info.add("all", Utils.gson.toJsonTree(false));
				String type = ApiHandler.itemTypes.get(item);
				JsonArray enchantArray = null;
				if (ApiHandler.enchants.has(type)) {
					enchantArray = ApiHandler.enchants.getAsJsonArray(type);
				}
				if (enchantArray != null) {
					for (JsonElement enchant : enchantArray) {
						enchants.add(enchant);
					}
				}
				for (JsonElement reforge : ApiHandler.reforges) {
					reforges.add(reforge);
				}
				for (int i = 0; i < 11; i++) {
					stars.add(Utils.gson.toJsonTree(String.valueOf(i)));
				}
			}
		}

		if (info.has("enchants") && !all) {
			enchants = info.getAsJsonArray("enchants");
		}
		if (info.has("gems") && !all) {
			gems = info.getAsJsonArray("gems");
		}

		if (info.has("stars") && !all) {
			stars = info.getAsJsonArray("stars");
		}

		if (info.has("scrolls") && !all) {
			scrolls = info.getAsJsonArray("scrolls");
		}

		if (info.has("reforges") && !all) {
			reforges = info.getAsJsonArray("reforges");
		}

		if (info.has("enrichments") && !all) {
			enrichments = info.getAsJsonArray("enrichments");
		}

		for (String attribute : attributes) {
			// If translated to enchant, check if enchant is valid for type of item
			// Deal with level stuff
			// If not already in list, add enchant to list
			String attributeTranslated = attribute;
			attributeTranslated = attributeTranslated.replace(" ", "_");
			int i = attributeTranslated.lastIndexOf("_");

			ArrayList<Integer> toSkip = new ArrayList<Integer>();

			for (int j = 0; j < enchants.size(); j++) {
				if (enchants.get(j).getAsString().equalsIgnoreCase(attributeTranslated)
						|| enchants.get(j).getAsString().equalsIgnoreCase("ULTIMATE_" + attributeTranslated)) {
					toSkip.add(j);
				}
				System.out.println(enchants.get(j).getAsString());
			}

			enchants = Utils.deleteAllFromJsonArray(enchants, toSkip);

			toSkip = new ArrayList<Integer>();
			for (int j = 0; j < reforges.size(); j++) {
				if (attribute.equalsIgnoreCase(reforges.get(j).getAsString())) {
					toSkip.add(j);
				}
			}
			reforges = Utils.deleteAllFromJsonArray(reforges, toSkip);

			// TODO - Finish different star params
			if (attribute.contains("stars")) {
				if (attribute.contains(">=")) {
					int start = Integer.valueOf(attribute.split(">=")[1]);
					if (start > 10 || start < 0) {
						continue;
					}
					toSkip = new ArrayList<Integer>();
					for (int j = 0; j < stars.size(); j++) {
						for (int star = start; star < 11; star++) {
							if (stars.get(j).getAsInt() == star) {
								toSkip.add(j);
							}
						}
					}
					stars = Utils.deleteAllFromJsonArray(stars, toSkip);
					continue;
				} else if (attribute.contains("<=")) {
					int end = Integer.valueOf(attribute.split("<=")[1]);
					if (end > 10 || end < 0) {
						continue;
					}
					toSkip = new ArrayList<Integer>();
					for (int j = 0; j < stars.size(); j++) {
						for (int star = 0; star < end + 1; star++) {
							if (stars.get(j).getAsInt() == star) {
								toSkip.add(j);
							}
						}
					}
					stars = Utils.deleteAllFromJsonArray(stars, toSkip);
					continue;

				} else if (attribute.contains(">")) {
					int start = Integer.valueOf(attribute.split(">")[1]) + 1;
					if (start > 10 || start < 0) {
						continue;
					}
					toSkip = new ArrayList<Integer>();
					for (int j = 0; j < stars.size(); j++) {
						for (int star = start; star < 11; star++) {
							if (stars.get(j).getAsInt() == star) {
								toSkip.add(j);
							}
						}
					}
					stars = Utils.deleteAllFromJsonArray(stars, toSkip);
					continue;

				} else if (attribute.contains("<")) {
					int end = Integer.valueOf(attribute.split("<")[1]);
					if (end > 11 || end < 0) {
						continue;
					}
					toSkip = new ArrayList<Integer>();
					for (int j = 0; j < stars.size(); j++) {
						for (int star = 0; star < end; star++) {
							if (stars.get(j).getAsInt() == star) {
								toSkip.add(j);
							}
						}
					}
					stars = Utils.deleteAllFromJsonArray(stars, toSkip);
					continue;

				} else if (attribute.contains("=")) {
					int start = Integer.valueOf(attribute.split("=")[1]);
					if (start < 0 || start > 10) {
						continue;
					}
					toSkip = new ArrayList<Integer>();
					for (int j = 0; j < stars.size(); j++) {
						if (stars.get(j).getAsInt() == start) {
							toSkip.add(j);
						}
					}
					stars = Utils.deleteAllFromJsonArray(stars, toSkip);
					continue;
				}

			}

			if (attribute.startsWith("minprofit")) {
				if (info.has("minprofit")) {
					info.remove("minprofit");
				}
			}

			if (attribute.startsWith("minpercent")) {
				if (info.has("minpercent")) {
					info.remove("minpercent");
				}
			}

			if (attribute.contains("clean")) {
				info.add("clean", Utils.gson.toJsonTree(false));
			}

			// Handle other attributes
		}
		info.add("enchants", enchants);
		info.add("stars", stars);
		info.add("reforges", reforges);
		items.add(item, info);
		save();

	}

	// Add pet to blacklist
	public static void addPet(JsonArray array) {
		for (JsonElement element : array) {
			JsonObject info = new JsonObject();
			info.add("all", gson.toJsonTree(true));
			json.getAsJsonObject("items").add(element.getAsString(), info);
		}
		save();

	}

	// Remove pet from blacklist
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