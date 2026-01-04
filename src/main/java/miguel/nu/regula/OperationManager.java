package miguel.nu.regula;

import com.google.gson.*;
import miguel.nu.regula.Classes.Role;
import miguel.nu.regula.utils.Ban;
import miguel.nu.regula.utils.Kick;
import miguel.nu.regula.utils.Mute;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class OperationManager {
    public static boolean runOperation(Player self, OfflinePlayer target, String action){
        String type = action.split("_")[0];
        Map<?,?> actionMap = new HashMap<>();
        for (Object obj : Main.plugin.getConfig().getList("actions." + type.toLowerCase())){
            if(!(obj instanceof Map<?,?> map)) continue;
            if(!Objects.equals(map.get("id").toString(), action)) continue;
            actionMap = map;
        }
        if(Objects.equals(type, "KICK")){
            return Kick.kickPlayer(self, target, actionMap.get("reason").toString());
        }

        String playerUuid = target.getUniqueId().toString();

        addAction(playerUuid, action);
        int count = getActionCount(playerUuid, action);

        List<Integer> durations = (List<Integer>) actionMap.get("durations");
        int durationIndex = Math.min(count - 1, durations.size() - 1);
        int duration = durations.get(durationIndex);

        if(Objects.equals(type, "MUTE")){
            if(duration > 0){
                return Mute.mute(self, UUID.fromString(playerUuid), duration, (String) actionMap.get("reason"));
            }
            return Mute.mute(self, UUID.fromString(playerUuid), (String) actionMap.get("reason"));
        }
        else if(Objects.equals(type, "BAN")){
            return Ban.banPlayerModern(target, self, actionMap.get("reason").toString(), duration);
        }
        return false;
    }
    public static boolean runOperation(Player self, OfflinePlayer target, String action, Integer duration){
        String type = action.split("_")[0];
        Map<?,?> actionMap = new HashMap<>();
        for (Object obj : Main.plugin.getConfig().getList("actions." + type.toLowerCase())){
            if(!(obj instanceof Map<?,?> map)) continue;
            if(!Objects.equals(map.get("id").toString(), action)) continue;
            actionMap = map;
        }
        String playerUuid = target.getUniqueId().toString();

        if(Objects.equals(type, "KICK")){
            return Kick.kickPlayer(self, target, actionMap.get("reason").toString());
        }
        if(Objects.equals(type, "MUTE")){
            if(duration == -1){
                return Mute.mute(self, UUID.fromString(playerUuid), (String) actionMap.get("reason"));
            }
            return Mute.mute(self, UUID.fromString(playerUuid), duration, (String) actionMap.get("reason"));
        }
        else if(Objects.equals(type, "BAN")){
            return Ban.banPlayerModern(target, self, actionMap.get("reason").toString(), duration);
        }
        return false;
    }

    private static final Path file = Paths.get("plugins", "Regula", "actions.json");

    /* ------------------------ Core JSON I/O ------------------------ */

    private static JsonObject getRootObject() {
        Gson gson = new Gson();
        try {
            Files.createDirectories(file.getParent());

            if (Files.exists(file)) {
                try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    JsonObject root = gson.fromJson(r, JsonObject.class);
                    return (root != null) ? root : new JsonObject();
                }
            } else {
                JsonObject root = new JsonObject();
                try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                    gson.toJson(root, w);
                }
                return root;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new JsonObject();
        }
    }

    private static void saveRoot(JsonObject rootJson) {
        if (rootJson == null) return;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            Files.createDirectories(file.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                gson.toJson(rootJson, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ------------------------ Helpers ------------------------ */

    private static JsonObject getOrCreatePlayerObject(JsonObject root, String playerUuid) {
        JsonObject player = root.has(playerUuid) && root.get(playerUuid).isJsonObject()
                ? root.getAsJsonObject(playerUuid)
                : new JsonObject();

        root.add(playerUuid, player);
        return player;
    }

    private static int safeGetInt(JsonElement el) {
        if (el == null || !el.isJsonPrimitive()) return 0;
        JsonPrimitive prim = el.getAsJsonPrimitive();
        try {
            return prim.getAsInt();
        } catch (NumberFormatException ex) {

            return 0;
        }
    }

    /* ------------------------ Public API ------------------------ */

    /** Increment a player's action count by 1. */
    public static void addAction(String playerUuid, String actionId) {
        addAction(playerUuid, actionId, 1);
    }

    /** Increment a player's action count by a positive or negative delta. */
    public static void addAction(String playerUuid, String actionId, int delta) {
        if (playerUuid == null || playerUuid.isEmpty() || actionId == null || actionId.isEmpty()) return;

        JsonObject root = getRootObject();
        JsonObject playerObj = getOrCreatePlayerObject(root, playerUuid);

        int current = safeGetInt(playerObj.get(actionId));
        int updated = current + delta;
        if (updated <= 0) {
            playerObj.remove(actionId);
            if (playerObj.entrySet().isEmpty()) {
                root.remove(playerUuid);
            }
        } else {
            playerObj.addProperty(actionId, updated);
        }

        saveRoot(root);
    }

    /** Set a player's action count explicitly. */
    public static void setActionCount(String playerUuid, String actionId, int count) {
        if (playerUuid == null || playerUuid.isEmpty() || actionId == null || actionId.isEmpty()) return;

        JsonObject root = getRootObject();
        if (count <= 0) {
            JsonObject playerObj = root.has(playerUuid) && root.get(playerUuid).isJsonObject()
                    ? root.getAsJsonObject(playerUuid)
                    : null;
            if (playerObj != null) {
                playerObj.remove(actionId);
                if (playerObj.entrySet().isEmpty()) root.remove(playerUuid);
            }
        } else {
            JsonObject playerObj = getOrCreatePlayerObject(root, playerUuid);
            playerObj.addProperty(actionId, count);
        }
        saveRoot(root);
    }

    /** Get a player's count for a specific action (0 if absent). */
    public static int getActionCount(String playerUuid, String actionId) {
        if (playerUuid == null || playerUuid.isEmpty() || actionId == null || actionId.isEmpty()) return 0;

        JsonObject root = getRootObject();
        if (!root.has(playerUuid) || !root.get(playerUuid).isJsonObject()) return 0;

        JsonObject playerObj = root.getAsJsonObject(playerUuid);
        return safeGetInt(playerObj.get(actionId));
    }

    /** Get all actions and counts for a player as a Map<String, Integer>. */
    public static Map<String, Integer> getAllActionCounts(String playerUuid) {
        Map<String, Integer> out = new LinkedHashMap<>();
        if (playerUuid == null || playerUuid.isEmpty()) return out;

        JsonObject root = getRootObject();
        if (!root.has(playerUuid) || !root.get(playerUuid).isJsonObject()) return out;

        JsonObject playerObj = root.getAsJsonObject(playerUuid);
        for (Map.Entry<String, JsonElement> e : playerObj.entrySet()) {
            out.put(e.getKey(), safeGetInt(e.getValue()));
        }
        return out;
    }

    /** Remove a specific action from a player (equivalent to setting count to 0). */
    public static void removeAction(String playerUuid, String actionId) {
        setActionCount(playerUuid, actionId, 0);
    }

    /** Remove all actions for a player. */
    public static void clearPlayer(String playerUuid) {
        if (playerUuid == null || playerUuid.isEmpty()) return;
        JsonObject root = getRootObject();
        root.remove(playerUuid);
        saveRoot(root);
    }
}
