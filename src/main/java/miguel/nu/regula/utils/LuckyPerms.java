package miguel.nu.regula.utils;

import miguel.nu.regula.Main;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class LuckyPerms {
//    LuckPerms luckPerms;
    public LuckyPerms(){
//        RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
//        if (provider != null) {
//            luckPerms = provider.getProvider();
//        }
    }

    public void givePermToUuid(UUID uuid, String permission) {
//        Main.plugin.getLogger().severe("New perm: " + permission);
//        luckPerms.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
//            Node node = Node.builder(permission).value(true).build();
//            user.data().add(node);
//            luckPerms.getUserManager().saveUser(user);
//        });
    }

    public void removePermFromUuid(UUID uuid, String permission) {
//        Main.plugin.getLogger().severe("Remove perm: " + permission);
//        luckPerms.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
//            Node node = Node.builder(permission).value(false).build();
//            user.data().add(node);
//            luckPerms.getUserManager().saveUser(user);
//        });
    }
}
