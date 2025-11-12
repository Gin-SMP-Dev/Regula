package miguel.nu.regula.menus.utils;

import miguel.nu.regula.Main;
import miguel.nu.regula.menus.MenuHolder;
import miguel.nu.regula.menus.MenuPrefab;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfirmMenu {
    public static void open(Player player, String title, String[] description, Map<NamespacedKey, String> dataMap){
        MenuHolder holder = new MenuHolder("CONFIRM_MENU", 45, Component.text("Are you sure?"));
        Inventory inventory = holder.getInventory();

        MenuPrefab.drawBorder(inventory);
        inventory.setItem(20, cancel());
        inventory.setItem(22, description(title, description));
        inventory.setItem(24, confirm());

        ItemMeta meta = inventory.getItem(0).getItemMeta();
        for (NamespacedKey key : dataMap.keySet()){
            if(dataMap.get(key) == null){
                return;
            }
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, dataMap.get(key));
        }
        inventory.getItem(0).setItemMeta(meta);

        player.openInventory(inventory);
    }

    private static ItemStack cancel(){
        ItemStack item = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Cancel")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Click to cancel this operation")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }
    private static ItemStack confirm(){
        ItemStack item = new ItemStack(Material.LIME_WOOL);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Confirm")
                .color(NamedTextColor.GREEN)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Click to confirm this operation")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }
    private static ItemStack description(String title, String[] description){
        ItemStack item = new ItemStack(Material.OAK_SIGN);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(title)
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        for(String line : description){
            lore.add(Component.text(line)
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}

