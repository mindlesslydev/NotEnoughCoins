package me.mindlessly.notenoughcoins.websocket;

import com.google.gson.*;
import me.mindlessly.notenoughcoins.Reference;
import me.mindlessly.notenoughcoins.configuration.ConfigHandler;
import me.mindlessly.notenoughcoins.utils.Blacklist;
import me.mindlessly.notenoughcoins.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

    private static final String SERVER_HOST = "vps-9587f748.vps.ovh.ca";
    private static final int SERVER_PORT = 8087;
    public static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
    private static Socket socket;
    private static Minecraft mc;
    private static final List<JsonObject> currentFlips = new ArrayList<>();
    private static final Set<String> sentCommands = new HashSet<>(); // Set to store sent commands
    private static long lastFlipTimestamp = 0; // Track the time of the last flip
    private static int currentFlipIndex = 0; // Track the current index of the flip being viewed

    /**
     * Method to connect to the flip server
     */
    public static void start() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            handleFlipMessages();
            scheduleFlipListClearTask(); // Start the task to clear the list

        } catch (IOException e) {
            Reference.logger.error(e.getMessage());
        }
    }

    /**
     * Method to check if a flip is blacklisted
     *
     * @param blacklist - The user's blacklist
     * @param flip      - The flip info we are checking
     * @return - True if an item is blacklisted, false otherwise
     */
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
                    if (enchants == null && gems == null && upgradeLevel == 0 && scrolls == null && reforge == null && enrichment == null) {
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
                // Add logic to check for blacklisted gems here
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
                    if (r.getAsString().equals(reforge)) {
                        return true;
                    }
                }
            }
            if (info.has("enrichments") && enrichment != null) {
                JsonArray blacklistedEnrichments = info.getAsJsonArray("enrichments");
                for (JsonElement e : blacklistedEnrichments) {
                    if (e.getAsString().equals(enrichment)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Method to reconnect to NEC server if it is lost
     */
    public static void autoReconnect() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (!isSocketConnected(socket)) {
                start();
            }
        }, 10, 1, TimeUnit.SECONDS);
    }

    /**
     * Method to check if a socket is connected
     *
     * @param socket - The socket to check
     * @return - If the socket is connected
     */
    private static boolean isSocketConnected(Socket socket) {
        try {
            if (socket.getInputStream().read() == -1) {
                return false;
            }
            socket.getOutputStream().write(0);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Method to handle flip messages
     */
    private static void handleFlipMessages() {
        // Listen for server messages in a separate thread
        Thread serverListenerThread = new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = socket.getInputStream().read(buffer)) != -1) {
                    String receivedData = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);

                    // Define the regular expression pattern to match JSON objects
                    String regexPattern = "\\{.*?}";

                    Pattern pattern = Pattern.compile(regexPattern);
                    Matcher matcher = pattern.matcher(receivedData);

                    // Process each matched JSON object
                    while (matcher.find()) {
                        String jsonObject = matcher.group();
                        JsonObject config = ConfigHandler.getConfig();
                        try {
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

                                // Add the flip to the list of current flips
                                synchronized (currentFlips) {
                                    currentFlips.add(flip);
                                }

                                ChatComponentText msg = new ChatComponentText(EnumChatFormatting.GOLD + "[NEC] " + Utils.getColorCodeFromRarity(flip.get("rarity").getAsString()) + name + EnumChatFormatting.GOLD + stars + EnumChatFormatting.GREEN + " " + Utils.formatPrice(price) + EnumChatFormatting.WHITE + "->" + EnumChatFormatting.GREEN + Utils.formatPrice(listFor) + " " + "+" + Utils.formatPrice(profit));

                                msg.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewauction " + flip.get("uuid").getAsString())));
                                lastFlipTimestamp = System.currentTimeMillis(); // Update the timestamp after processing
                                if (config.get("toggle").getAsBoolean()) {
                                    mc.thePlayer.addChatMessage(new ChatComponentText(""));
                                    mc.thePlayer.addChatMessage(msg);
                                }
                            }

                        } catch (JsonSyntaxException e) {
                            Reference.logger.error(e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                Reference.logger.error(e.getMessage());
            }
        });
        serverListenerThread.start();
    }

    /**
     * Method to schedule a task that clears the flip list after 10 seconds of inactivity
     */
    private static void scheduleFlipListClearTask() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFlipTimestamp >= 10000) { // 10 seconds in milliseconds
                synchronized (currentFlips) {
                    currentFlips.clear();
                    sentCommands.clear(); // Clear the sent commands set
                    currentFlipIndex = 0; // Reset index when the list is cleared
                }
            }
        }, 10, 1, TimeUnit.SECONDS);
    }

    /**
     * Method to open the next most profitable deal
     */
    public static void openNextProfitableDeal() {
        try {
            if (currentFlips.isEmpty()) {
                ChatComponentText noFlipsMessage = new ChatComponentText(
				    EnumChatFormatting.GOLD + "[NEC] " + EnumChatFormatting.RED + "No flips available!"
				);
				Minecraft.getMinecraft().thePlayer.addChatMessage(noFlipsMessage);
                return;
            }

            JsonObject config = ConfigHandler.getConfig();
            JsonObject blacklist = Blacklist.json.get("items").getAsJsonObject();

            // Sort flips by profit in descending order
            List<JsonObject> sortedFlips;
            synchronized (currentFlips) {
                sortedFlips = new ArrayList<>(currentFlips);
            }
            sortedFlips.sort(Comparator.comparingDouble(f -> -f.get("profit").getAsDouble()));

            // Cycle through the sorted flips
            JsonObject flipToOpen;
            synchronized (currentFlips) {
                do {
                    if (currentFlipIndex >= sortedFlips.size()) {
                        currentFlipIndex = 0; // Reset to the first flip
                    }
                    flipToOpen = sortedFlips.get(currentFlipIndex);
                    currentFlipIndex++;
                } while (sentCommands.contains(flipToOpen.get("uuid").getAsString())); // Check if command was already sent
            }

            // Send the command to view the auction
            String uuid = flipToOpen.get("uuid").getAsString();
            sentCommands.add(uuid); // Mark this command as sent
            String command = "/viewauction " + uuid;
            Minecraft.getMinecraft().thePlayer.sendChatMessage(command);

        } catch (Exception e) {
            Reference.logger.error("Failed to open the next profitable deal: " + e.getMessage());
        }
    }

    /**
     * Method to get the current list of flips
     */
    public static List<JsonObject> getCurrentFlips() {
        synchronized (currentFlips) {
            return new ArrayList<>(currentFlips); // Return a copy of the list to avoid concurrency issues
        }
    }
}
