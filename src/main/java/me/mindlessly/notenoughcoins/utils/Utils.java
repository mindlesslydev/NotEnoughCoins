package me.mindlessly.notenoughcoins.utils;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;

public class Utils {

	public static Gson gson = new Gson();

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

	public static JsonElement getJson(String jsonUrl) {
		try {
			URL url = new URL(jsonUrl);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Connection", "close");
			JsonReader reader = new JsonReader(new InputStreamReader(conn.getInputStream()));
			JsonElement element = new Gson().fromJson(reader, JsonElement.class);
			return element;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static EnumChatFormatting getColorCodeFromRarity(String rarity) {
		switch (rarity) {
		case "COMMON":
			return EnumChatFormatting.WHITE;
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
					return Double.parseDouble(line.replaceAll("\\(\\+[\\d]+\\)", "").replaceAll("[^\\d.]", ""));
				}
			}

		} else {
			return 0;
		}
		return 0;
	}

	public static String removeColorCodes(String in) {
		return in.replaceAll("(?i)\\u00A7.", "");
	}

	public static JsonArray deleteAllFromJsonArray(JsonArray input, ArrayList<Integer> toSkip) {
		JsonArray temp = new JsonArray();
		for (int i = 0; i < input.size(); i++) {
			if (!toSkip.contains(i)) {
				temp.add(input.get(i));
			}
		}
		return temp;
	}

	public static JsonArray deleteFromJsonArray(JsonArray input, int toSkip) {
		JsonArray temp = new JsonArray();
		for (int i = 0; i < input.size(); i++) {
			if (i != toSkip) {
				temp.add(input.get(i));
			}
		}
		return temp;
	}

	public static int convertAbbreviatedNumber(String input) {
		int multiplier = 1;

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
