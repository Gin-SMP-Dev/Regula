package miguel.nu.regula.utils;

import miguel.nu.discordRelay.API.DiscordAPI;
import miguel.nu.regula.roles.RoleManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Teleport {
    public static void teleportToPlayer(Player self, OfflinePlayer target) {
        if(!RoleManager.hasPlayerPermission(self.getUniqueId().toString(), "TELEPORT")){
            self.sendMessage("You dont have permission to teleport.");
            return;
        }

        if(!target.isOnline()) {
            self.sendMessage("Player is not online.");
            return;
        }

        self.teleport(target.getPlayer());
        self.sendMessage("You teleported to " + target.getName() + ".");
        DiscordAPI.sendModLog(target, "Teleport", null, -2, self);
    }
}
