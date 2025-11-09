package miguel.nu.regula.utils;

import miguel.nu.regula.roles.RoleManager;
import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;

import java.time.Duration;
import java.util.Date;

public class Ban {
    public static void banPlayerModern(OfflinePlayer target, Player self, String reason, long durationSeconds) {
        if(!RoleManager.hasPlayerPermission(self.getUniqueId().toString(), "BAN_MEMBER")){
            self.sendMessage("You dont have permission to ban players.");
            return;
        }

        if (target == null) return;

        Date expires = (durationSeconds > 0)
                ? new Date(System.currentTimeMillis() + durationSeconds * 1000L)
                : null;


        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(
                target.getName(),
                reason,
                expires,
                "Banned by: " + (self != null ? self.getName() : "Console")
        );

        // Kick if they’re online
        if (target.isOnline()) {
            target.getPlayer().kickPlayer("§cYou have been banned.\n§7Reason: " + reason +
                    (expires == null ? "\n§7Duration: Permanent" : "\n§7Expires: " + expires));
        }

        if (self != null) {
            self.sendMessage("§aYou banned §f" + target.getName() +
                    (expires != null ? " §7(until " + expires + ")" : " §7(permanently)") + ".");
        }
    }

    public static void unbanPlayer(Player self, OfflinePlayer target) {
        if(!RoleManager.hasPlayerPermission(self.getUniqueId().toString(), "UNBAN_MEMBER")){
            self.sendMessage("You dont have permission to unban players.");
            return;
        }
        if (target == null) return;

        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(target.getName());

        if (self != null) {
            self.sendMessage("§aYou unbanned §f" + target.getName() + "§a.");
        }
    }
}
