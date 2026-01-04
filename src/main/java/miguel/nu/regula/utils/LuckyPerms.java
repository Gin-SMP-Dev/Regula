package miguel.nu.regula.utils;

import miguel.nu.regula.Classes.Role;
import miguel.nu.regula.Main;
import miguel.nu.regula.roles.RoleManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class LuckyPerms {
    LuckPerms luckPerms;
    public LuckyPerms(){
        RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        }
    }

    public void givePermToUuid(UUID uuid, String permission) {
        luckPerms.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
            Node node = Node.builder(permission).value(true).build();
            user.data().add(node);
            luckPerms.getUserManager().saveUser(user);
        });
    }

    public void removePermFromUuid(UUID uuid, String permission) {
        luckPerms.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
            Node node = Node.builder(permission).value(false).build();
            user.data().add(node);
            luckPerms.getUserManager().saveUser(user);
        });
    }

    public void syncPermOfUuid(UUID uuid){
        String[] roles = RoleManager.getPlayerRoles(uuid.toString());

        List<String> addPerms = new ArrayList<>();
        List<String> remPerms = Role.getAllMinecraftPermissions();

        boolean isAdmin = RoleManager.hasPlayerPermission(uuid.toString(), "ADMIN");
        if(!isAdmin){
            for(String roleRaw : roles){
                Role role = Role.getRole(roleRaw);
                if(role == null) continue;

                for(String perm : role.getMinecraftPermissions()){
                    if(remPerms.contains(perm)) {
                        remPerms.remove(perm);
                        addPerms.add(perm);
                    }
                }
            }
        } else {
            addPerms = remPerms;
            remPerms = new ArrayList<>();
        }

        for(String perm : addPerms){
            Main.plugin.getLogger().info(perm);
        }
        for(String perm : remPerms){
            Main.plugin.getLogger().info(perm);
        }
    }
}
