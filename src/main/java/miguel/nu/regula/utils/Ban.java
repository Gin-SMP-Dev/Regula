package miguel.nu.regula.utils;

import miguel.nu.regula.roles.RoleManager;
import net.kyori.adventure.text.Component;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;
import miguel.nu.discordRelay.API.DiscordAPI;
import java.time.Duration;
import java.util.Date;

public class Ban {
    public static boolean banPlayerModern(OfflinePlayer target, Player self, String reason, long durationSeconds) {
        if(!RoleManager.hasPlayerPermission(self.getUniqueId().toString(), "BAN_MEMBER")){
            self.sendMessage("You dont have permission to ban players.");
            return false;
        }

        if (target == null) return false;

        Date expires = (durationSeconds > 0)
                ? new Date(System.currentTimeMillis() + durationSeconds * 1000L)
                : null;


        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(
                target.getName(),
                reason,
                expires,
                "Banned by: " + (self != null ? self.getName() : "Console")
        );

        if (target.isOnline()) {
            target.getPlayer().kickPlayer("§cYou have been banned.\n§7Reason: " + reason +
                    (expires == null ? "\n§7Duration: Permanent" : "\n§7Expires: " + expires));
        }

        if (self != null) {
            self.sendMessage("§aYou banned §f" + target.getName() +
                    (expires != null ? " §7(until " + expires + ")" : " §7(permanently)") + ".");
        }

        DiscordAPI.sendModLog(target, "Ban", reason, durationSeconds, self);
        return true;
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

        DiscordAPI.sendModLog(target, "Unban", null, -2, self);
    }

    public static String getBanTimeRemaining(OfflinePlayer target) {
        if (target == null) return "Invalid player.";

        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        BanEntry entry = banList.getBanEntry(target.getName());

        if (entry == null) {
            return "Not banned.";
        }

        Date expires = entry.getExpiration();
        if (expires == null) {
            return "Permanent";
        }

        long remainingMillis = expires.getTime() - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            return "Expired";
        }

        long seconds = remainingMillis / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (sb.isEmpty()) sb.append(secs).append("s");

        return sb.toString().trim();
    }

    public static boolean isPlayerBanned(OfflinePlayer target) {
        if (target == null) return false;

        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        BanEntry entry = banList.getBanEntry(target.getName());

        if (entry == null) {
            return false;
        }

        if (entry.getExpiration() != null && entry.getExpiration().getTime() <= System.currentTimeMillis()) {
            banList.pardon(target.getName());
            return false;
        }

        return true;
    }
}
