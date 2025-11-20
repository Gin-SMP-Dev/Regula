package miguel.nu.regula.utils;

import miguel.nu.regula.Classes.Role;
import miguel.nu.regula.commands.NicknameCommand;
import miguel.nu.regula.roles.RoleManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Join implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        String[] roles = RoleManager.getPlayerRoles(e.getPlayer().getUniqueId().toString());

        if(roles.length == 0){
            RoleManager.addPlayerRole(e.getPlayer().getUniqueId().toString(), "Default");
            roles = RoleManager.getPlayerRoles(e.getPlayer().getUniqueId().toString());
        }

        Role role = Role.getRole(roles[0]);
        if(role == null) return;
        if(!role.getNamecolor().isEmpty()){
            NicknameCommand.changePlayerNickname(e.getPlayer(), role.getNamecolor() + e.getPlayer().getName());
        }
    }
}
