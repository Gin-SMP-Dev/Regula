package miguel.nu.regula.utils;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Mute implements Listener {

    private static final Map<UUID, Long> mutedPlayers = new HashMap<>();

    /**
     * Mute a player permanently.
     */
    public static void mute(UUID uuid) {
        mutedPlayers.put(uuid, -1L); // -1 means permanent mute
    }

    /**
     * Mute a player for a certain duration (in seconds).
     */
    public static void mute(UUID uuid, long durationSeconds) {
        long expireTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        mutedPlayers.put(uuid, expireTime);
    }

    /**
     * Unmute a player.
     */
    public static void unmute(UUID uuid) {
        mutedPlayers.remove(uuid);
    }

    /**
     * Check if a player is currently muted.
     */
    public static boolean isMuted(UUID uuid) {
        if (!mutedPlayers.containsKey(uuid)) return false;

        long expireTime = mutedPlayers.get(uuid);

        if (expireTime == -1L) return true;

        if (System.currentTimeMillis() > expireTime) {
            mutedPlayers.remove(uuid);
            return false;
        }

        return true;
    }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (isMuted(uuid)) {
            Long expireTime = mutedPlayers.get(uuid);

            if (expireTime == -1L) {
                // Permanent mute
                e.getPlayer().sendMessage("§cYou are permanently muted and cannot chat.");
            } else {
                long remaining = expireTime - System.currentTimeMillis();

                if (remaining > 0) {
                    long seconds = remaining / 1000 % 60;
                    long minutes = (remaining / 1000 / 60) % 60;
                    long hours = (remaining / 1000 / 60 / 60) % 24;
                    long days = (remaining / 1000 / 60 / 60 / 24);

                    StringBuilder timeLeft = new StringBuilder();
                    if (days > 0) timeLeft.append(days).append("d ");
                    if (hours > 0) timeLeft.append(hours).append("h ");
                    if (minutes > 0) timeLeft.append(minutes).append("m ");
                    if (seconds > 0 || timeLeft.isEmpty()) timeLeft.append(seconds).append("s");

                    e.getPlayer().sendMessage("§cYou are muted for another §f" + timeLeft + "§c.");
                } else {
                    unmute(uuid);
                    return;
                }
            }

            e.setCancelled(true);
        }
    }

}
