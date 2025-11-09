package miguel.nu.regula.menus;

import miguel.nu.regula.Classes.Role;
import miguel.nu.regula.DatabaseManager;
import miguel.nu.regula.Main;
import miguel.nu.regula.utils.NamespaceKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;

public class RoleMenu {
    public static void open(Player player){
        MenuHolder holder = new MenuHolder("ROLES_MENU", 45, Component.text("Roles Menu"));
        Inventory inventory = holder.getInventory();

        MenuPrefab.drawBorder(inventory);
        inventory.setItem(37, previous());
        inventory.setItem(40, exit());
        inventory.setItem(43, next());

        getRoles(inventory);
        player.openInventory(inventory);
    }

    private static ItemStack previous(){
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Previous Page")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Click to turn page!")
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }
    private static ItemStack next(){
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Next Page")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Click to turn page!")
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
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
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static void getRoles(Inventory inventory){
        List<Role> roles;
        int[] slots = {19, 21, 23, 25};
        roles = Role.getAllRoles(0);

        for(int i = 0; i < roles.size(); i++){
            ItemStack item = new ItemStack(Material.LEATHER_HELMET);
            ItemMeta meta = item.getItemMeta();

            meta.displayName(Component.text(roles.get(i).getName())
                    .color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Click to edit role!")
                            .color(NamedTextColor.YELLOW)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(NamespaceKey.getNamespacedKey("ROLE_NAME"), PersistentDataType.STRING, roles.get(i).getName());
            item.setItemMeta(meta);
            inventory.setItem(slots[i], item);
        }
    }
}
