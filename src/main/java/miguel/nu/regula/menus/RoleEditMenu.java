package miguel.nu.regula.menus;

import miguel.nu.regula.Classes.Role;
import miguel.nu.regula.ConfigManager;
import miguel.nu.regula.Main;
import miguel.nu.regula.utils.NamespaceKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RoleEditMenu {
    public static void open(Player player, Role role){
        MenuHolder holder = new MenuHolder("ROLE_EDIT_MENU", 54, Component.text("Editing: " + role.getName()));
        Inventory inventory = holder.getInventory();



        MenuPrefab.drawBorder(inventory);
        inventory.setItem(49, exit());

        ItemMeta meta = inventory.getItem(0).getItemMeta();
        meta.getPersistentDataContainer().set(NamespaceKey.getNamespacedKey("ROLE_NAME"), PersistentDataType.STRING, role.getName());
        inventory.getItem(0).setItemMeta(meta);

        getPermissions(inventory, role, 0);
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

    static int[] slots = {
            10, 11, 12, 13, 14, 15, 16,
            28, 29, 30, 31, 32, 33, 34
    };

    public static void getPermissions(Inventory inventory, Role role, int offset) {
        List<Map<?, ?>> permissions = ConfigManager.getAllPermission();

        if (offset < 0) offset = 0;
        if (offset >= permissions.size() && !permissions.isEmpty()) {
            offset = 0;
        }

        for (int slot : slots) {
            inventory.setItem(slot, null);
            inventory.setItem(slot + 9, null);
        }

        inventory.setItem(52, MenuPrefab.emptyItem()); // next
        inventory.setItem(46, MenuPrefab.emptyItem()); // previous

        int count = Math.min(offset + slots.length, permissions.size());

        if (permissions.size() > offset + slots.length) {
            inventory.setItem(52, next());
        }
        if (offset > 0) {
            inventory.setItem(46, previous());
        }

        boolean hasAdmin = Role.hasPermission("ADMIN", role);

        for (int i = offset; i < count; i++) {
            int pageIndex = i - offset;

            Map<?, ?> permData = permissions.get(i);

            ItemStack item = new ItemStack(Material.valueOf(permData.get("placeholder").toString()));
            ItemMeta meta = item.getItemMeta();

            meta.displayName(Component.text(permData.get("desc").toString())
                    .color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));

            item.setItemMeta(meta);
            inventory.setItem(slots[pageIndex], item);

            ItemStack itemToggle;
            ItemMeta metaToggle;

            if (!Objects.equals(permData.get("perm").toString(), "ADMIN") && hasAdmin) {
                itemToggle = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                metaToggle = itemToggle.getItemMeta();

                metaToggle.displayName(Component.text("Enabled")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                metaToggle.lore(List.of(
                        Component.text("This role has admin")
                                .color(NamedTextColor.YELLOW)
                                .decoration(TextDecoration.ITALIC, false)
                ));
            } else {
                if (Role.hasPermission(permData.get("perm").toString(), role)) {
                    itemToggle = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                    metaToggle = itemToggle.getItemMeta();

                    metaToggle.displayName(Component.text("Enabled")
                            .color(NamedTextColor.GREEN)
                            .decoration(TextDecoration.ITALIC, false));
                    metaToggle.lore(List.of(
                            Component.text("Click to toggle access to: " + permData.get("desc").toString())
                                    .color(NamedTextColor.YELLOW)
                                    .decoration(TextDecoration.ITALIC, false)
                    ));
                } else {
                    itemToggle = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                    metaToggle = itemToggle.getItemMeta();

                    metaToggle.displayName(Component.text("Disabled")
                            .color(NamedTextColor.RED)
                            .decoration(TextDecoration.ITALIC, false));
                    metaToggle.lore(List.of(
                            Component.text("Click to toggle access to: " + permData.get("desc").toString())
                                    .color(NamedTextColor.YELLOW)
                                    .decoration(TextDecoration.ITALIC, false)
                    ));
                }
            }

            PersistentDataContainer data = metaToggle.getPersistentDataContainer();
            data.set(NamespaceKey.getNamespacedKey("ROLE_PERMISSION"),
                    PersistentDataType.STRING,
                    permData.get("perm").toString());

            itemToggle.setItemMeta(metaToggle);
            inventory.setItem(slots[pageIndex] + 9, itemToggle);
        }
    }

    public static void switchPage(Inventory inventory, Role role, int pageDiff){
        ItemMeta meta = inventory.getItem(0).getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        int currentOffset = data.getOrDefault(NamespaceKey.getNamespacedKey("CURRENT_PAGE"), PersistentDataType.INTEGER, 0) * slots.length;
        int newOffset = Math.max(0, currentOffset + pageDiff * slots.length);
        data.set(NamespaceKey.getNamespacedKey("CURRENT_PAGE"), PersistentDataType.INTEGER, newOffset / slots.length);
        inventory.getItem(0).setItemMeta(meta);
        getPermissions(inventory, role, newOffset);
    }
    public static void switchPage(Inventory inventory, Role role){
        ItemMeta meta = inventory.getItem(0).getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        int currentOffset = data.getOrDefault(NamespaceKey.getNamespacedKey("CURRENT_PAGE"), PersistentDataType.INTEGER, 0) * slots.length;
        getPermissions(inventory, role, currentOffset);
    }
}
