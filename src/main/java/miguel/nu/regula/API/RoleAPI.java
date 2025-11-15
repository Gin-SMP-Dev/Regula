package miguel.nu.regula.API;

import miguel.nu.regula.roles.RoleManager;
import org.bukkit.OfflinePlayer;

public class RoleAPI {
    public static String[] getPlayerRoles(OfflinePlayer player){
        return RoleManager.getPlayerRoles(player.getUniqueId().toString());
    }
    public static boolean hasPlayerRole(OfflinePlayer player, String role){
        return RoleManager.hasPlayerRole(player, role);
    }
    public static boolean hasPlayerPermission(OfflinePlayer player, String permission){
        return RoleManager.hasPlayerPermission(player.getUniqueId().toString(), permission);
    }
}
