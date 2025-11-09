package miguel.nu.regula.utils;

import com.mojang.brigadier.Message;
import miguel.nu.regula.roles.RoleManager;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Kick {
    public static void kickPlayer(Player self, OfflinePlayer target, String message) {
        if(!RoleManager.hasPlayerPermission(self.getUniqueId().toString(), "KICK_MEMBER")){
            self.sendMessage("You dont have permission to kick players.");
            return;
        }

        if(!target.isOnline()) {
            self.sendMessage(target.getName() + " is not online.");
            return;
        }

        target.getPlayer().kick(Component.text(message));
        self.sendMessage("You kicked " + target.getName() + ".");
    }
}
