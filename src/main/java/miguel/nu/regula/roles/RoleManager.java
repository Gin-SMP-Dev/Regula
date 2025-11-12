package miguel.nu.regula.roles;

import com.google.gson.*;
import io.papermc.paper.persistence.PersistentDataContainerView;
import miguel.nu.regula.Classes.Role;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoleManager {
    static Path file = Paths.get("plugins", "Regula", "roles.json");
    private static JsonObject getRootArray() {
        Gson gson = new Gson();

        try {
            Files.createDirectories(file.getParent());

            // Load or create empty root object
            if (Files.exists(file)) {
                try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    JsonObject root = gson.fromJson(r, JsonObject.class);
                    return (root != null) ? root : new JsonObject();
                }
            } else {
                JsonObject root = new JsonObject();
                // create an empty file
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
    public static void saveRoot(JsonObject rootJson) {
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

    public static String[] getPlayerRoles(String playerUuid) {
        JsonObject root = getRootArray();

        if (root.has(playerUuid) && root.get(playerUuid).isJsonArray()) {
            JsonArray arr = root.getAsJsonArray(playerUuid);
            List<String> roles = new ArrayList<>();
            for (JsonElement e : arr) {
                if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
                    roles.add(e.getAsString());
                }
            }
            return roles.toArray(new String[0]);
        }

        // return empty array
        return new String[0];
    }

    public static void addPlayerRole(String playerUuid, String role){
        JsonObject rootJson = getRootArray();
        JsonArray roles = rootJson.has(playerUuid) && rootJson.get(playerUuid).isJsonArray()
                ? rootJson.getAsJsonArray(playerUuid)
                : new JsonArray();

        if (!arrayContains(roles, role)) {
            roles.add(role);
        }

        rootJson.add(playerUuid, roles);
        saveRoot(rootJson);
        updateTabListPrefix(Bukkit.getOfflinePlayer(UUID.fromString(playerUuid)));
    }

    public static void removePlayerRole(String playerUuid, String role) {
        if (playerUuid == null || playerUuid.isEmpty() || role == null || role.isEmpty())
            return;

        JsonObject rootJson = getRootArray();
        JsonArray roles = rootJson.has(playerUuid) && rootJson.get(playerUuid).isJsonArray()
                ? rootJson.getAsJsonArray(playerUuid)
                : new JsonArray();

        JsonArray updatedRoles = new JsonArray();

        for (JsonElement e : roles) {
            if (e.isJsonPrimitive()) {
                String existingRole = e.getAsString();
                if (!existingRole.equalsIgnoreCase(role)) {
                    updatedRoles.add(existingRole);
                }
            }
        }

        if (updatedRoles.isEmpty()) {
            rootJson.remove(playerUuid);
        } else {
            rootJson.add(playerUuid, updatedRoles);
        }
        saveRoot(rootJson);
    }

    public static List<String> getAllPlayersWithRole(String role) {
        List<String> players = new ArrayList<>();
        JsonObject root = getRootArray();

        for (String uuid : root.keySet()) {
            JsonElement value = root.get(uuid);
            if (!value.isJsonArray()) continue;

            JsonArray roles = value.getAsJsonArray();
            for (JsonElement e : roles) {
                if (!e.isJsonPrimitive()) continue;

                if (e.getAsString().equals(role)) {
                    players.add(uuid);
                    break;
                }
            }
        }
        return players;
    }
    public static boolean hasPlayerPermission(String playerUuid, String permission){
        String[] playerRoles = getPlayerRoles(playerUuid);
        for(String role : playerRoles){
            if(Role.hasPermission(permission, Role.getRole(role)) || Role.hasPermission("ADMIN", Role.getRole(role))){
                return true;
            }
        }
        return false;
    }
    public static boolean hasPermissions(String playerUuid){
        String[] playerRoles = getPlayerRoles(playerUuid);
        for(String role : playerRoles){
            return Role.getRole(role).getPermission() > 0;
        }
        return false;
    }
    public static boolean hasPlayerPermission(String playerUuid, String[] permission){
        String[] playerRoles = getPlayerRoles(playerUuid);
        for(String role : playerRoles){
            for(String perm : permission){
                if(Role.hasPermission(perm, Role.getRole(role)) || Role.hasPermission("ADMIN", Role.getRole(role))){
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean arrayContains(JsonArray arr, String value) {
        for (JsonElement e : arr) {
            if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString() && value.equals(e.getAsString())) {
                return true;
            }
        }
        return false;
    }

    public static void updateTabListPrefix(OfflinePlayer player){
        if(!player.isOnline()) return;
        PersistentDataContainer data = player.getPlayer().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey("essential", "tab_list_prefix_regula");

        String[] roles = getPlayerRoles(player.getUniqueId().toString());
        if(roles.length < 1) return;

        Role role = Role.getRole(roles[0]);
        if(role == null) return;

        data.set(key, PersistentDataType.STRING, role.getDisplay());
    }
}
