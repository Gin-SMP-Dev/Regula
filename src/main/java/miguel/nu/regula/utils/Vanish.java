package miguel.nu.regula.utils;

import miguel.nu.regula.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Vanish implements Listener {

    private static final Set<UUID> vanished = new HashSet<>();

    public static boolean isVanished(Player p) {
        return vanished.contains(p.getUniqueId());
    }

    public static void vanish(Player p, boolean announceLeave) {
        if (!vanished.add(p.getUniqueId())) return;

        // Fake quit message (yellow, like vanilla)
        if (announceLeave) {
            Component left = Component.text(p.getName() + " left the game", NamedTextColor.YELLOW);
            Bukkit.getServer().sendMessage(left);
        }

        // Spectator mode
        p.setGameMode(GameMode.SPECTATOR);

        // Hide from everyone (also removes from TAB for those players)
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(p)) continue;
            other.hidePlayer(Main.plugin, p);
        }

        // Optional: hide sounds/chat feedback to self
        p.sendMessage(Component.text("You are now vanished.", NamedTextColor.GRAY));
    }

    public static void unvanish(Player p, boolean announceJoin) {
        if (!vanished.remove(p.getUniqueId())) return;

        // Show to everyone again (returns to TAB)
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(p)) continue;
            other.showPlayer(Main.plugin, p);
        }

        // Put them back to SURVIVAL (change if you prefer their previous mode)
        p.setGameMode(GameMode.SURVIVAL);

        if (announceJoin) {
            Component join = Component.text(p.getName() + " joined the game", NamedTextColor.YELLOW);
            Bukkit.getServer().sendMessage(join);
        }

        p.sendMessage(Component.text("You are no longer vanished.", NamedTextColor.GRAY));
    }
    public static void toggleVanish(Player p) {
        if(isVanished(p)){
            unvanish(p, true);
            return;
        }
        vanish(p, true);
    }

    // Re-apply hiding on join if they were vanished before a relog/reload
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player joined = e.getPlayer();
        for (UUID id : vanished) {
            Player vanishedPlayer = Bukkit.getPlayer(id);
            if (vanishedPlayer != null && vanishedPlayer.isOnline()) {
                joined.hidePlayer(Main.plugin, vanishedPlayer);
            }
        }
    }
}
