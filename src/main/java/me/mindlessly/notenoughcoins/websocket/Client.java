package me.mindlessly.notenoughcoins.websocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import me.mindlessly.notenoughcoins.configuration.ConfigHandler;
import me.mindlessly.notenoughcoins.utils.Blacklist;
import me.mindlessly.notenoughcoins.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class Client {

	public static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
	private static final String SERVER_HOST = "vps-9587f748.vps.ovh.ca";
	private static final int SERVER_PORT = 8087;
	private static Gson gson = new Gson();
	private static Socket socket;

	private static Minecraft mc;

	public static void start() {
		try {
			socket = new Socket(SERVER_HOST, SERVER_PORT);

			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Listen for server messages in a separate thread
			Thread serverListenerThread = new Thread(() -> {
				try {

					byte[] buffer = new byte[1024];
					int bytesRead;
					while ((bytesRead = socket.getInputStream().read(buffer)) != -1) {
						// Sometimes multiple JSON objects will be received from the websocket at the
						// same time, we need to seperate these
						String receivedData = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);

						// Define the regular expression pattern to match JSON objects
						String regexPattern = "\\{.*?\\}";

						// Create a regular expression matcher
						Pattern pattern = Pattern.compile(regexPattern);
						Matcher matcher = pattern.matcher(receivedData);

						// Process each matched JSON object
						while (matcher.find()) {
							String jsonObject = matcher.group();

							JsonObject config = ConfigHandler.getConfig();
							try {
								// Parse the JSON object of the flip
								JsonObject flip = new Gson().fromJson(jsonObject, JsonObject.class);
								JsonObject blacklist = Blacklist.json.get("items").getAsJsonObject();

								boolean skip = getIfBlacklisted(blacklist, flip);

								if (skip) {
									continue;
								}

								JsonObject override = null;
								String id = flip.get("id").getAsString();

								if (blacklist.has(id)) {
									override = blacklist.get(id).getAsJsonObject();
								}
								int minProfit = config.get("minprofit").getAsInt();
								int minPercent = config.get("minpercent").getAsInt();
								int minDemand = config.get("mindemand").getAsInt();

								if (override != null) {
									if (override.has("minprofit")) {
										minProfit = override.get("minprofit").getAsInt();
									}
									if (override.has("minpercent")) {
										minPercent = override.get("minpercent").getAsInt();
									}
								}

								mc = Minecraft.getMinecraft();
								if (mc.theWorld != null && mc.theWorld.getScoreboard() != null) {
									String name = flip.get("name").getAsString();
									String stars = "";
									int index = name.indexOf("✪");
									if (index > -1) {
										stars = name.substring(index);
										name = name.substring(0, index);
									}

									double price = flip.get("price").getAsDouble();
									double listFor = flip.get("listFor").getAsDouble();
									double profit = flip.get("profit").getAsDouble();

									if (config.has("adjustment")) {
										int adjustment = config.get("adjustment").getAsInt();
										listFor = listFor * (double) (100 - adjustment) / 100;
										profit = Utils.getProfit(price, listFor);
									}

									if (config.has("maxcost") && config.get("maxcost").getAsInt() < price) {
										continue;

									} else if (price > Utils.getPurse()) {
										continue;
									}

									if (profit < minProfit) {
										continue;
									}

									if ((profit / listFor) * 100 < minPercent) {
										continue;
									}

									if (flip.has("sales") && flip.get("sales").getAsInt() < minDemand) {
										continue;
									}

									ChatComponentText msg = new ChatComponentText(EnumChatFormatting.GOLD + "[NEC] "
											+ Utils.getColorCodeFromRarity(flip.get("rarity").getAsString()) + name
											+ EnumChatFormatting.GOLD + stars + EnumChatFormatting.GREEN + " "
											+ Utils.formatPrice(price) + EnumChatFormatting.WHITE + "->"
											+ EnumChatFormatting.GREEN + Utils.formatPrice(listFor) + " " + "+"
											+ Utils.formatPrice(profit));

									msg.setChatStyle(new ChatStyle()
											.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
													"/viewauction " + flip.get("uuid").getAsString())));

									if (config.get("toggle").getAsBoolean()) {
										mc.thePlayer.addChatMessage(new ChatComponentText(""));
										mc.thePlayer.addChatMessage(msg);
									}
								}

							} catch (JsonSyntaxException e) {
								e.printStackTrace();
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			serverListenerThread.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean getIfBlacklisted(JsonObject blacklist, JsonObject flip) {
		JsonArray enchants = null;
		JsonArray gems = null;
		int upgradeLevel = 0;
		JsonArray scrolls = null;
		String reforge = null;
		String enrichment = null;

		String id = flip.get("id").getAsString();

		if (flip.has("enchants")) {
			enchants = flip.getAsJsonArray("enchants");
		}
		if (flip.has("gems")) {
			gems = flip.getAsJsonArray("gems");
		}
		if (flip.has("upgrade_level")) {
			upgradeLevel = flip.get("upgrade_level").getAsInt();
		}
		if (flip.has("scrolls")) {
			scrolls = flip.getAsJsonArray("scrolls");
		}
		if (flip.has("reforge")) {
			reforge = flip.get("reforge").getAsString();
		}
		if (flip.has("enrichment")) {
			enrichment = flip.get("enrichment").getAsString();
		}

		if (blacklist.has(id)) {
			JsonObject info = blacklist.get(id).getAsJsonObject();

			if (info.has("all")) {
				if (info.get("all").getAsBoolean()) {
					return true;
				}
			}
			if (info.has("clean")) {
				if (info.get("clean").getAsBoolean()) {
					if (enchants == null && gems == null && upgradeLevel == 0 && scrolls == null && reforge == null
							&& enrichment == null) {
						return true;
					}
				}
			}
			if (info.has("enchants") && enchants != null) {
				JsonArray blacklistedEnchants = info.getAsJsonArray("enchants");
				for (JsonElement enchant : blacklistedEnchants) {
					for (JsonElement e : enchants) {
						if (enchant.equals(e)) {
							return true;
						}
					}
				}

			}
			if (info.has("gems") && gems != null) {
				// TODO - finish gem blacklist
			}

			if (info.has("stars")) {
				JsonArray blacklistedStars = info.getAsJsonArray("stars");
				for (JsonElement star : blacklistedStars) {
					if (star.getAsInt() == upgradeLevel) {
						return true;
					}
				}
			}

			if (info.has("scrolls") && scrolls != null) {
				JsonArray blacklistedScrolls = info.getAsJsonArray("scrolls");
				for (JsonElement scroll : blacklistedScrolls) {
					for (JsonElement s : scrolls) {
						if (scroll.equals(s)) {
							return true;
						}
					}
				}
			}

			if (info.has("reforges") && reforge != null) {
				JsonArray blacklistedReforges = info.getAsJsonArray("reforges");
				for (JsonElement r : blacklistedReforges) {
					if (r.getAsString().equals("reforge")) {
						return true;
					}
				}
			}

			if (info.has("enrichments") && enrichment != null) {
				JsonArray blacklistedEnrichments = info.getAsJsonArray("enrichments");
				for (JsonElement e : blacklistedEnrichments) {
					if (e.getAsString().equals("enrichment")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static void autoReconnect() {
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			if (!isSocketConnected(socket)) {
				start();
			}
		}, 10, 1, TimeUnit.SECONDS);

	}

	private static boolean isSocketConnected(Socket socket) {
		try {
			// Check if the socket's input stream is closed
			if (socket.getInputStream().read() == -1) {
				return false;
			}

			// Check if the socket's output stream is closed
			socket.getOutputStream().write(0);
		} catch (IOException e) {
			return false;
		}

		return true;
	}
}
