package miguel.nu.regula.utils;

import miguel.nu.regula.roles.RoleManager;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static miguel.nu.regula.utils.NamespaceKey.getNamespacedKey;

public class Prefix implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();

        RoleManager.updateTabListPrefix(player);

        String nickname = player.getPersistentDataContainer().getOrDefault(
                getNamespacedKey("NICKNAME"),
                org.bukkit.persistence.PersistentDataType.STRING,
                player.getName()
        );

        Component colored = Component.text(ChatColor.translateAlternateColorCodes('&', nickname));

        player.displayName(colored);
        player.playerListName(colored);
    }
}
