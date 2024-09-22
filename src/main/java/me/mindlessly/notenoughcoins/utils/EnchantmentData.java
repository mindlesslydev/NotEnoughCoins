package me.mindlessly.notenoughcoins.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class EnchantmentData {
    private static Map<String, EnchantmentData> enchantmentData;

    private String id;
    private int max;

    public EnchantmentData(String id, int max) {
        this.id = id;
        this.max = max;
    }

    public String getId() {
        return id;
    }

    public int getMax() {
        return max;
    }

    public static Map<String, EnchantmentData> getEnchantmentData() {
        if (enchantmentData == null) {
            loadEnchantmentData();
        }
        return enchantmentData;
    }

    private static void loadEnchantmentData() {
        try {
            InputStream is = EnchantmentData.class.getResourceAsStream("/enchantmentsid.json");
            String jsonTxt = IOUtils.toString(is, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(jsonTxt, JsonObject.class);

            enchantmentData = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                String enchantName = entry.getKey().toUpperCase().replace(" ", "_");
                JsonObject enchantInfo = ((JsonObject) entry.getValue());
                String id = enchantInfo.get("id").getAsString();
                int max = enchantInfo.get("max").getAsInt();
                enchantmentData.put(enchantName, new EnchantmentData(id, max));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}