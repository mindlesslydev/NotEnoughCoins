package me.mindlessly.notenoughcoins.utils;

import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ApiHandler {

	public static HashMap<String, String> items = new HashMap<>();
	public static HashMap<String, String> itemTypes = new HashMap<>();
	public static JsonObject pets;
	public static JsonObject enchants;
	public static JsonArray reforges;
	public static JsonObject skins;

	public static void getItems() {
		JsonArray jsonArray = Utils.getJson("https://api.hypixel.net/resources/skyblock/items").getAsJsonObject()
				.get("items").getAsJsonArray();
		for (JsonElement jsonElement : jsonArray) {
			JsonObject item = jsonElement.getAsJsonObject();
			if (!item.has("category")) {
				continue;
			}
			String name = item.get("name").getAsString();
			String id = item.get("id").getAsString();
			if (id.contains("STARRED")) {
				continue;
			}
			String category = item.get("category").getAsString();
			items.put(name, id);
			itemTypes.put(id, category);
		}
		pets = Utils.getJson("https://notenoughcoins.net/static/pets.json").getAsJsonObject();
		reforges = Utils.getJson("https://notenoughcoins.net/static/reforges.json").getAsJsonArray();
		skins = Utils.getJson("https://notenoughcoins.net/static/skins.json").getAsJsonObject();

		// Credit to the NEU Repo for enchant list used in here for some blacklist stuff
		enchants = Utils.getJson("https://notenoughcoins.net/static/enchants.json").getAsJsonObject().get("enchants")
				.getAsJsonObject();
	}

}
