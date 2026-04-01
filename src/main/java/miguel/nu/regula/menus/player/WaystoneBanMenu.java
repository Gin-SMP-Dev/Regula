package miguel.nu.regula.menus.player;

import miguel.nu.wayStone.API.WaystoneAPI;
import miguel.nu.wayStone.Classes.Waystone;
import miguel.nu.regula.menus.MenuHolder;
import miguel.nu.regula.menus.MenuPrefab;
import miguel.nu.regula.utils.NamespaceKey;
import miguel.nu.wayStone.WaystoneManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WaystoneBanMenu {
    public static void open(Player player, OfflinePlayer offlinePlayer, Map<NamespacedKey, String> dataMap){
        MenuHolder holder = new MenuHolder("WAYSTONE_MENU_BAN", 54, Component.text("Ban " + offlinePlayer.getName() + " from waystone"));
        Inventory inventory = holder.getInventory();

        MenuPrefab.drawBorder(inventory);

        Waystone[] waystones = WaystoneManager.getAllWaystone();

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

        int count = Math.min(fillSlots.size(), waystones.length);
        if (fillSlots.isEmpty()) return;

        for(int i = 0; i < count; i++){
            inventory.setItem(fillSlots.get(i), drawWaystone(waystones[i], WaystoneAPI.isPlayerBanned(offlinePlayer.getUniqueId(), waystones[i].getName())));
        }

        inventory.setItem(49, exit());

        ItemMeta meta = inventory.getItem(0).getItemMeta();
        for (NamespacedKey key : dataMap.keySet()){
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, dataMap.get(key));
        }
        inventory.getItem(0).setItemMeta(meta);

        player.openInventory(inventory);
    }

    static ItemStack drawWaystone(Waystone waystone, boolean isBanned){
        ItemStack item = new ItemStack(waystone.getPlaceholder());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(waystone.getName())
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        if(isBanned){
            meta.lore(List.of(
                    Component.text("Player is already banned.")
                            .color(NamedTextColor.RED)
                            .decoration(TextDecoration.ITALIC, false)
            ));
        } else {
            meta.lore(List.of(
                    Component.text("Click to ban player.")
                            .color(NamedTextColor.RED)
                            .decoration(TextDecoration.ITALIC, false)
            ));
        }

        if(!isBanned){
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(NamespaceKey.getNamespacedKey("WAYSTONE_NAME"), PersistentDataType.STRING, waystone.getName());
        }
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack exit(){
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Close")
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
