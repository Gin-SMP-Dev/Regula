package miguel.nu.regula.utils;

import miguel.nu.discordRelay.API.DiscordAPI;
import miguel.nu.regula.roles.RoleManager;
import miguel.nu.wayStone.API.WaystoneAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Waystone {
    public static void banPlayer(Player self, OfflinePlayer target, String waystone) {
        if(!RoleManager.hasPlayerPermission(self.getUniqueId().toString(), "WAYSTONE_BAN")){
            self.sendMessage("You dont have permission to ban players from waystones.");
            return;
        }
        if (target == null) return;

        WaystoneAPI.banPlayer(target.getUniqueId(), waystone);

        if (self != null) {
            self.sendMessage("§aYou banned §f" + target.getName() + " from " + waystone + "§a.");
        }

        DiscordAPI.sendModLog(target, "Ban_waystone", null, -2, self);
    }

    public static void unbanPlayer(Player self, OfflinePlayer target, String waystone) {
        if(!RoleManager.hasPlayerPermission(self.getUniqueId().toString(), "WAYSTONE_UNBAN")){
            self.sendMessage("You dont have permission to unban players from waystones.");
            return;
        }
        if (target == null) return;

        WaystoneAPI.unbanPlayer(target.getUniqueId(), waystone);

        if (self != null) {
            self.sendMessage("§aYou unbanned §f" + target.getName() + " §afrom " + waystone + ".");
        }

        DiscordAPI.sendModLog(target, "Unban_waystone", null, -2, self);
    }
}
