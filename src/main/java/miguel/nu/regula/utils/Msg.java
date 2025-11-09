package miguel.nu.regula.utils;

import net.ess3.api.events.PrivateMessageSentEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class Msg implements Listener {
    private static final Set<UUID> listeningPlayers = new HashSet<>();

    @EventHandler
    public void onPrivateMsg(PrivateMessageSentEvent event) {
        String sender = event.getSender().getName();
        String recipient = event.getRecipient().getName();
        String message = event.getMessage();

        for (UUID uuid : listeningPlayers) {
            Player p = getServer().getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage("§7[Msg] §f" + sender + " → " + recipient + ": " + message);
            }
        }
    }

    public static void toggleListening(Player player) {
        UUID id = player.getUniqueId();
        if (listeningPlayers.remove(id)) {
            player.sendMessage("§cYou are no longer listening to private messages.");
        } else {
            listeningPlayers.add(id);
            player.sendMessage("§aYou are now listening to private messages.");
        }
    }

    public static boolean isListening(Player player) {
        return listeningPlayers.contains(player.getUniqueId());
    }
}
