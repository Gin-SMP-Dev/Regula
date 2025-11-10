package miguel.nu.regula.menus;

import miguel.nu.regula.Classes.Role;
import miguel.nu.regula.ConfigManager;
import miguel.nu.regula.roles.RoleManager;
import miguel.nu.regula.utils.Msg;
import miguel.nu.regula.utils.NamespaceKey;
import miguel.nu.regula.utils.Vanish;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class AssignedMenu {
    public static void open(Player player, Role role){
        MenuHolder holder = new MenuHolder("ROLE_ASSIGNED_MENU", 54, Component.text("Player list of: " + role.getName()));
        Inventory inventory = holder.getInventory();

        MenuPrefab.drawBorder(inventory);
        inventory.setItem(46, previous());
        inventory.setItem(49, exit());
        inventory.setItem(52, next());
        playerHead(inventory, role);
        player.openInventory(inventory);
    }
    private static void playerHead(Inventory inventory, Role role){
        List<String> players = RoleManager.getAllPlayersWithRole(role.getName());

        int size = inventory.getSize();
        if (size % 9 != 0) return;

        List<Integer> fillSlots = new ArrayList<>();
        int rows = size / 9;
        for (int slot = 0; slot < size; slot++) {
            int col = slot % 9;
            int row = slot / 9;
            if (row == 0 || row == rows - 1 || col == 0 || col == 8) continue;
            fillSlots.add(slot);
        }
        if (fillSlots.isEmpty()) return;

        for(int i = 0; i < players.size(); i++){
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(players.get(i)));

            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwningPlayer(player);
            meta.displayName(Component.text(player.getName())
                    .color(NamedTextColor.GOLD)
                    .decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));

            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING, players.get(i));

            item.setItemMeta(meta);
            inventory.setItem(fillSlots.get(i), item);
        }
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
}
