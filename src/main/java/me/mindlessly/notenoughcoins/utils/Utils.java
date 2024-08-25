package me.mindlessly.notenoughcoins.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import me.mindlessly.notenoughcoins.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Utils {

    public static Gson gson = new Gson();

    /**
     * Method to format a price
     *
     * @param value - The raw price
     * @return - The formatted price
     */
    public static String formatPrice(double value) {
        String result;
        if (value >= 1000000) {
            result = String.format("%.2fm", value / 1000000);
        } else if (value >= 1000) {
            result = String.format("%.1fk", value / 1000);
        } else {
            result = String.format("%.0f", value);
        }
        return result;
    }

    /**
     * Method to get JSON data from a URL
     *
     * @param jsonUrl - The address of the JSON file
     * @return - The JSON data
     */
    public static JsonElement getJson(String jsonUrl) {
        try {
            URL url = new URL(jsonUrl);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("Connection", "close");
            JsonReader reader = new JsonReader(new InputStreamReader(conn.getInputStream()));
            return new Gson().fromJson(reader, JsonElement.class);
        } catch (Exception e) {
            Reference.logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * Method to get the color code for a rarity
     *
     * @param rarity - The rarity to get the color code for
     * @return - The color code
     */
    public static EnumChatFormatting getColorCodeFromRarity(String rarity) {
        switch (rarity) {
            case "UNCOMMON":
                return EnumChatFormatting.GREEN;
            case "RARE":
                return EnumChatFormatting.BLUE;
            case "EPIC":
                return EnumChatFormatting.DARK_PURPLE;
            case "LEGENDARY":
                return EnumChatFormatting.GOLD;
            case "MYTHIC":
                return EnumChatFormatting.LIGHT_PURPLE;
            case "DIVINE":
                return EnumChatFormatting.AQUA;
            case "SPECIAL":
            case "VERY_SPECIAL":
                return EnumChatFormatting.RED;
            default:
                return EnumChatFormatting.WHITE;
        }
    }

    public static double getPurse() {
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        if (scoreboard != null) {
            List<Score> scores = new LinkedList<>(scoreboard.getSortedScores(scoreboard.getObjectiveInDisplaySlot(1)));
            for (Score score : scores) {
                ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(score.getPlayerName());
                String line = Utils
                    .removeColorCodes(ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score.getPlayerName()));
                if (line.contains("Purse: ") || line.contains("Piggy: ")) {
                    return Double.parseDouble(line.replaceAll("\\(\\+\\d+\\)", "").replaceAll("[^\\d.]", ""));
                }
            }

        } else {
            return 0;
        }
        return 0;
    }

    /**
     * Method to remove color codes from a string
     *
     * @param in - The string to format
     * @return - The cleaned string
     */
    public static String removeColorCodes(String in) {
        return in.replaceAll("(?i)\\u00A7.", "");
    }

    /**
     * Method to remove multiple elements from a JsonArray
     *
     * @param input  - The array to edit
     * @param toSkip - All items to keep
     * @return - The cleaned array
     */

    public static JsonArray deleteAllFromJsonArray(JsonArray input, ArrayList<Integer> toSkip) {
        JsonArray temp = new JsonArray();
        for (int i = 0; i < input.size(); i++) {
            if (!toSkip.contains(i)) {
                temp.add(input.get(i));
            }
        }
        return temp;
    }

    /**
     * Method to remove a single item from a JsonArray
     *
     * @param input  - The array to edit
     * @param toSkip - All items to keep
     * @return - The cleaned array
     */
    public static JsonArray deleteFromJsonArray(JsonArray input, int toSkip) {
        JsonArray temp = new JsonArray();
        for (int i = 0; i < input.size(); i++) {
            if (i != toSkip) {
                temp.add(input.get(i));
            }
        }
        return temp;
    }

    /**
     * Method to convert an abbreviated number to an integer
     *
     * @param input - The string to convert
     * @return - The converted integer
     */
    public static int convertAbbreviatedNumber(String input) {
        int multiplier;

        int numericValue = Integer.parseInt(input.substring(0, input.length() - 1));

        String suffix = input.substring(input.length() - 1);
        switch (suffix) {
            case "m":
                multiplier = 1000000;
                break;
            case "k":
                multiplier = 1000;
                break;
            case "b":
                multiplier = 1000000000;
                break;
            default:
                multiplier = -1;
                break;
        }

        return numericValue * multiplier;
    }

    /**
     * Method to get net profit from a flip
     *
     * @param price   - The purchasing price of the item
     * @param listFor - The listing price of the item
     * @return - The net profit
     */
    public static double getProfit(double price, double listFor) {
        double listingFee = 0;
        double tax = listFor * 0.01;
        if (listFor < 10000000) {
            listingFee = listFor * 0.01;
        } else if (listFor >= 10000000 && listFor < 100000000) {
            listingFee = listFor * 0.02;
        } else if (listFor >= 100000000) {
            listingFee = listFor * 0.025;
        }

        return (listFor - listingFee - tax - price) * 0.95;
    }
}
