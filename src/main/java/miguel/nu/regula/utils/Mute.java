package miguel.nu.regula.utils;

import miguel.nu.discordRelay.API.DiscordAPI;
import miguel.nu.regula.Main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import io.papermc.paper.event.player.AsyncChatEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Mute implements Listener {

    // Persist here so it works no matter what the plugin's data folder is
    private static final Path STORE_FILE = Paths.get("plugins", "Regula", "mutes.json");

    // UUID -> -1L (permanent) or absolute epoch millis (expiry)
    private static final Map<UUID, Long> mutedPlayers = new ConcurrentHashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Common private-message commands to block while muted
    private static final Set<String> BLOCKED_PM_CMDS = new HashSet<>(Arrays.asList(
            "msg","w","whisper","tell","r","reply","message","m","pm"
    ));

    /** Call this in your main class onEnable(): Mute.init(); */
    public static void init() {
        loadFromDisk();
        Bukkit.getPluginManager().registerEvents(new Mute(), Main.plugin);
    }

    /* ----------------- Public API ----------------- */

    /** Permanent mute (stored as -1). */
    public static boolean mute(Player player, UUID uuid, String reason) {
        mutedPlayers.put(uuid, -1L);
        saveToDiskAsync();
        if(player != null) player.sendMessage("Muted player!");
        DiscordAPI.sendModLog(Bukkit.getOfflinePlayer(uuid), "Unmute", reason, -1, player);
        return true;
    }

    /** Timed mute for durationSeconds — stored as absolute expiry epoch millis. */
    public static boolean mute(Player player, UUID uuid, long durationSeconds, String reason) {
        long expireAt = System.currentTimeMillis() + (durationSeconds * 1000L);
        mutedPlayers.put(uuid, expireAt);
        saveToDiskAsync();
        if(player != null) player.sendMessage("Muted player!");
        DiscordAPI.sendModLog(Bukkit.getOfflinePlayer(uuid), "Unmute", reason, -2, player);
        return true;
    }

    /** Timed mute until a specific instant (absolute epoch millis). */
    public static boolean muteUntil(UUID uuid, Instant until) {
        mutedPlayers.put(uuid, until.toEpochMilli());
        saveToDiskAsync();
        return true;
    }

    /** Unmute immediately. */
    public static void unmute(Player player, UUID uuid) {
        if (mutedPlayers.remove(uuid) != null) {
            saveToDiskAsync();
            if(player != null) player.sendMessage("Unmuted player!");
            DiscordAPI.sendModLog(Bukkit.getOfflinePlayer(uuid), "Unmute", null, -2, player);
        }
    }

    /** Is player muted? Prunes expired mutes and persists removal. */
    public static boolean isMuted(UUID uuid) {
        Long until = mutedPlayers.get(uuid);
        if (until == null) return false;
        if (until == -1L) return true;
        if (System.currentTimeMillis() > until) {
            mutedPlayers.remove(uuid);
            saveToDiskAsync();
            return false;
        }
        return true;
    }

    /* ----------------- Listeners ----------------- */

    // Block normal chat while muted
    @EventHandler
    public void onChat(AsyncChatEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (!isMuted(uuid)) return;

        Long expireAt = mutedPlayers.get(uuid);
        if (expireAt != null && expireAt == -1L) {
            e.getPlayer().sendMessage("§cYou are permanently muted and cannot chat.");
        } else if (expireAt != null) {
            long remaining = expireAt - System.currentTimeMillis();
            if (remaining <= 0) {
                unmute(null, uuid);
                return;
            }
            e.getPlayer().sendMessage("§cYou are muted for another §f" + formatDuration(remaining) + "§c.");
        }
        e.setCancelled(true);
    }

    // Block common PM commands while muted (e.g., /msg, /w, /tell, /r)
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        if (!isMuted(player.getUniqueId())) return;

        String raw = e.getMessage(); // like "/msg name hi"
        if (raw == null || raw.isEmpty() || raw.charAt(0) != '/') return;

        String[] split = raw.substring(1).split("\\s+", 2); // remove leading "/"
        String root = split[0];
        int colon = root.indexOf(':'); // strip namespace if present
        if (colon != -1) root = root.substring(colon + 1);
        root = root.toLowerCase(Locale.ROOT);

        if (BLOCKED_PM_CMDS.contains(root)) {
            Long expireAt = mutedPlayers.get(player.getUniqueId());
            if (expireAt != null && expireAt == -1L) {
                player.sendMessage("§cYou are permanently muted and cannot send private messages.");
            } else if (expireAt != null) {
                long remaining = expireAt - System.currentTimeMillis();
                if (remaining <= 0) {
                    unmute(null, player.getUniqueId());
                    return;
                }
                player.sendMessage("§cYou are muted for another §f" + formatDuration(remaining) + "§c and cannot send private messages.");
            } else {
                player.sendMessage("§cYou are muted and cannot send private messages.");
            }
            e.setCancelled(true);
        }
    }

    /* ----------------- Persistence ----------------- */

    private static void loadFromDisk() {
        try {
            Files.createDirectories(STORE_FILE.getParent());
            if (!Files.exists(STORE_FILE)) {
                try (Writer w = Files.newBufferedWriter(STORE_FILE, StandardCharsets.UTF_8)) {
                    GSON.toJson(new JsonObject(), w); // write empty object
                }
                mutedPlayers.clear();
                return;
            }

            try (Reader r = Files.newBufferedReader(STORE_FILE, StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(r, JsonObject.class);
                mutedPlayers.clear();
                if (root != null) {
                    for (Map.Entry<String, com.google.gson.JsonElement> entry : root.entrySet()) {
                        try {
                            UUID uuid = UUID.fromString(entry.getKey());
                            long val = entry.getValue().getAsLong(); // -1 or epoch ms
                            mutedPlayers.put(uuid, val);
                        } catch (Exception ignore) {
                            // skip malformed
                        }
                    }
                }
            }

            // Prune already-expired mutes on startup
            long now = System.currentTimeMillis();
            boolean changed = false;
            Iterator<Map.Entry<UUID, Long>> it = mutedPlayers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Long> en = it.next();
                long v = en.getValue();
                if (v != -1L && now > v) {
                    it.remove();
                    changed = true;
                }
            }
            if (changed) saveToDiskSync();

        } catch (IOException ex) {
            Main.plugin.getLogger().severe("Failed to load mutes.json: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void saveToDiskAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, Mute::saveToDiskSync);
    }

    private static synchronized void saveToDiskSync() {
        try {
            Files.createDirectories(STORE_FILE.getParent());
            JsonObject root = new JsonObject();
            for (Map.Entry<UUID, Long> e : mutedPlayers.entrySet()) {
                root.addProperty(e.getKey().toString(), e.getValue());
            }
            try (BufferedWriter w = Files.newBufferedWriter(STORE_FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(root, w);
            }
        } catch (IOException ex) {
            Main.plugin.getLogger().severe("Failed to save mutes.json: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static String getMuteTimeLeft(UUID uuid){
        Long expireAt = mutedPlayers.get(uuid);
        if (expireAt != null && expireAt == -1L) {
            return "Forever";
        } else if (expireAt != null) {
            long remaining = expireAt - System.currentTimeMillis();
            return formatDuration(remaining);
        }
        return "Not muted";
    }
    /* ----------------- Utils ----------------- */

    private static String formatDuration(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / 1000 / 60) % 60;
        long hours   = (millis / 1000 / 60 / 60) % 24;
        long days    = (millis / 1000 / 60 / 60 / 24);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.isEmpty()) sb.append(seconds).append("s");
        return sb.toString();
    }


}
