package miguel.nu.regula.menus.player;

import com.destroystokyo.paper.profile.PlayerProfile;
import miguel.nu.regula.ConfigManager;
import miguel.nu.regula.Main;
import miguel.nu.regula.menus.MenuHolder;
import miguel.nu.regula.menus.MenuPrefab;
import miguel.nu.regula.menus.RoleMenu;
import miguel.nu.regula.roles.RoleManager;
import miguel.nu.regula.utils.NamespaceKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModMenu {
    public static void open(Player player, OfflinePlayer target){
        MenuHolder holder = new MenuHolder("MOD_MENU", 54, Component.text("Moderation Menu"));
        Inventory inventory = holder.getInventory();

        MenuPrefab.drawBorder(inventory);
        drawOperation(player, inventory);

        ItemMeta meta = inventory.getItem(0).getItemMeta();
        meta.getPersistentDataContainer().set(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING, target.getUniqueId().toString());
        inventory.getItem(0).setItemMeta(meta);

        inventory.setItem(49, exit());
        player.openInventory(inventory);
    }

    private static void drawOperation(Player player, Inventory inventory){
        String[] perms = {"KICK_MEMBER", "BAN_MEMBER", "UNBAN_MEMBER", "MUTE_MEMBER", "UNMUTE_MEMBER", "INVSEE", "TELEPORT"};
        int[] slots = {19, 29, 21, 31, 23, 33, 25};
        List<Map<?, ?>> raw = ConfigManager.getAllPermission();

        Map<String, Map<?, ?>> byPerm = new HashMap<>();
        for (Map<?, ?> m : raw) {
            Object p = m.get("perm");
            if (p != null) byPerm.put(p.toString(), m);
        }

        for (int i = 0; i < perms.length; i++) {
            String perm = perms[i];
            if (!RoleManager.hasPlayerPermission(player.getUniqueId().toString(), perm)) continue;

            Map<?, ?> entry = byPerm.get(perm);
            if (entry == null) continue;

            String matName = String.valueOf(entry.get("placeholder"));
            Material mat = Material.matchMaterial(matName);
            if (mat == null) continue;

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();

            Object desc2 = entry.get("desc2");
            if (desc2 != null) {
                meta.displayName(Component.text(desc2.toString())
                        .color(NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false));
            }

            item.setItemMeta(meta);
            inventory.setItem(slots[i], item);
        }

        for(int i : slots){
            if(inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR){
                inventory.setItem(i, MenuPrefab.drawNoPermission());
            }
        }
    }

    private static ItemStack exit(){
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Back")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Click to close the menu.")
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }
}
