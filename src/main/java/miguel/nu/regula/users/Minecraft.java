package miguel.nu.regula.users;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.geysermc.floodgate.api.FloodgateApi;

public class Minecraft {
    /**
     * Validates a name as a "real" player in a Geyser/Floodgate environment:
     * 1) If online & Floodgate: true
     * 2) If seen before (offline cache) & Floodgate UUID: true
     * 3) If present in our Bedrock name index: true
     * 4) Else: Mojang Java lookup
     */
    public static boolean isValidMinecraftAccount(String name) {
        if (name == null || name.isEmpty()) return false;

        // 1) Online Floodgate?
        Player online = Bukkit.getPlayerExact(name);
        if (online != null && isFloodgateUuid(online.getUniqueId())) {
            return true;
        }

        // 2) Offline cache hit + Floodgate UUID?
        OfflinePlayer off = Bukkit.getOfflinePlayerIfCached(name);
        if (off != null && off.getUniqueId() != null && isFloodgateUuid(off.getUniqueId())) {
            return true;
        }

        // 3) Our Bedrock name index (populated on join)
        if (BedrockNameIndex.containsName(name)) {
            return true;
        }

        // 4) Java (Mojang) profile lookup
        return hasMojangProfile(name);
    }

    private static boolean isFloodgateUuid(java.util.UUID uuid) {
        try {
            FloodgateApi api = FloodgateApi.getInstance();
            return api != null && api.isFloodgatePlayer(uuid);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** Calls Mojang's username -> UUID endpoint. True if a Java profile exists. */
    private static boolean hasMojangProfile(String name) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + urlEncode(name));
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestProperty("User-Agent", "Regula/1.0");

            int code = conn.getResponseCode();
            if (code == 200) {
                try (InputStreamReader r =
                             new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                    JsonObject json = JsonParser.parseReader(r).getAsJsonObject();
                    return json.has("id") && json.get("id").getAsString().length() == 32;
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (conn != null) conn.disconnect();
        }
        return false;
    }

    private static String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return s;
        }
    }
}