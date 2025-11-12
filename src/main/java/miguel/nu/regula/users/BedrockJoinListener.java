package miguel.nu.regula.users;

import miguel.nu.regula.users.BedrockNameIndex;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Arrays;
import java.util.UUID;

public class BedrockJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (isFloodgateUuid(uuid)) {
            BedrockNameIndex.add(e.getPlayer().getName());
        }
    }

    /** Call this once in onEnable() to pre-seed from existing offline players. */
    public static void seedFromOfflineCache() {
        Arrays.stream(Bukkit.getOfflinePlayers()).forEach(off -> {
            if (off.getName() != null && off.getUniqueId() != null && isFloodgateUuid(off.getUniqueId())) {
                BedrockNameIndex.add(off.getName());
            }
        });
    }

    private static boolean isFloodgateUuid(UUID uuid) {
        try {
            FloodgateApi api = FloodgateApi.getInstance();
            return api != null && api.isFloodgatePlayer(uuid);
        } catch (Throwable ignored) {
            return false;
        }
    }
}