package miguel.nu.regula.utils;

import miguel.nu.regula.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Msg implements Listener {

    // ====== CONFIG ======
    private static final boolean DEBUG = false;

    private static final Set<String> PM_COMMANDS = new HashSet<>(Arrays.asList(
            "msg", "w", "whisper", "tell", "t", "dm", "pm", "m", "message", "r", "reply"
    ));
    // =====================

    private static final Set<UUID> listeningPlayers = ConcurrentHashMap.newKeySet();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String raw = event.getMessage();
        if (raw == null || raw.isEmpty() || raw.charAt(0) != '/') return;

        // Split into at most 3 parts: /<root> <target> <message>
        String[] parts = raw.substring(1).trim().split("\\s+", 3);
        if (parts.length < 1) return;

        // Extract root command
        String root = parts[0];
        int colon = root.indexOf(':');
        if (colon != -1) root = root.substring(colon + 1);
        root = root.toLowerCase(Locale.ROOT);

        if (!PM_COMMANDS.contains(root)) return;

        String senderName = event.getPlayer().getName();
        String recipientToken;
        String messageBody;

        if (root.equals("r") || root.equals("reply")) {

            if (parts.length < 2) return; // no message provided
            recipientToken = "(reply)";

            messageBody = raw.substring(1 + parts[0].length()).trim();
        } else {
            if (parts.length < 3) return; // need a target and a message
            recipientToken = parts[1];
            messageBody = parts[2];
        }

        Player maybeRecipient = Bukkit.getPlayerExact(recipientToken);
        for (UUID uuid : listeningPlayers) {
            Player spy = Bukkit.getPlayer(uuid);
            if (spy == null || !spy.isOnline()) continue;
            if (spy.equals(event.getPlayer())) continue;
            if (spy.equals(maybeRecipient)) continue;

            LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("PST"));
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String formattedDate = dateTime.format(dateTimeFormatter);

            spy.sendMessage("§7[Msg]" + formattedDate + " PST §f" + senderName + " → " + recipientToken + ": §7" + messageBody);
        }

        if (DEBUG) {
            Main.plugin.getLogger().info("[MsgSpy] " + senderName + " -> " + recipientToken + " :: " + messageBody + " (cmd=" + root + ")");
        }
    }

    /** Toggle spying on/off for a player */
    public static void toggleListening(Player player) {
        UUID id = player.getUniqueId();
        if (listeningPlayers.remove(id)) {
            player.sendMessage("§cYou are no longer listening to private messages.");
        } else {
            listeningPlayers.add(id);
            player.sendMessage("§aYou are now listening to private messages.");
        }
        if (DEBUG) Main.plugin.getLogger().info("[MsgSpy] " + player.getName() + " listening=" + listeningPlayers.contains(id));
    }

    /** Check if a player is currently listening */
    public static boolean isListening(Player player) {
        return listeningPlayers.contains(player.getUniqueId());
    }
}
