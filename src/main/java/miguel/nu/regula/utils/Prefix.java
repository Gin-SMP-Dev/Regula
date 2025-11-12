package miguel.nu.regula.utils;

import miguel.nu.regula.roles.RoleManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Prefix implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        RoleManager.updateTabListPrefix(e.getPlayer());
    }
}
