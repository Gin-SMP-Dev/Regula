package miguel.nu.regula.utils;

import miguel.nu.discordRelay.API.DiscordAPI;
import miguel.nu.regula.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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

        if (announceLeave) {
            Component left = Component.text(p.getName() + " left the game", NamedTextColor.YELLOW);
            Bukkit.getServer().sendMessage(left);
        }

        p.setGameMode(GameMode.SPECTATOR);

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(p)) continue;
            other.hidePlayer(Main.plugin, p);
        }

        p.sendMessage(Component.text("You are now vanished.", NamedTextColor.GRAY));

        DiscordAPI.sendModLog(null, "VanishActivate", null, -2, p);
    }

    public static void unvanish(Player p, boolean announceJoin) {
        if (!vanished.remove(p.getUniqueId())) return;

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(p)) continue;
            other.showPlayer(Main.plugin, p);
        }

        p.setGameMode(GameMode.SURVIVAL);

        if (announceJoin) {
            Component join = Component.text(p.getName() + " joined the game", NamedTextColor.YELLOW);
            Bukkit.getServer().sendMessage(join);
        }

        p.sendMessage(Component.text("You are no longer vanished.", NamedTextColor.GRAY));

        DiscordAPI.sendModLog(null, "VanishDeactivate", null, -2, p);
    }
    public static void toggleVanish(Player p) {
        if(isVanished(p)){
            unvanish(p, true);
            return;
        }
        vanish(p, true);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        vanished.remove(player.getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        for(UUID uuid : vanished){
            if(uuid == player.getUniqueId()) return;
            player.hidePlayer(Main.plugin, Bukkit.getOfflinePlayer(uuid).getPlayer());
        }
    }
}
