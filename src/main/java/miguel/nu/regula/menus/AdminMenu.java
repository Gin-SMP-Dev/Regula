package miguel.nu.regula.menus;

import miguel.nu.regula.roles.RoleManager;
import miguel.nu.regula.utils.Msg;
import miguel.nu.regula.utils.Vanish;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class AdminMenu {
    public static void open(Player player){
        MenuHolder holder = new MenuHolder("ADMIN_MENU", 54, Component.text("Admin Menu"));
        Inventory inventory = holder.getInventory();

        MenuPrefab.drawBorder(inventory);
        inventory.setItem(13, playerHead(player));
        if(RoleManager.hasPlayerPermission(player.getUniqueId().toString(), "MSG_SPY"))
            msgSpy(inventory, player);
        if(RoleManager.hasPlayerPermission(player.getUniqueId().toString(), "ADMIN"))
            inventory.setItem(31, roles());
        if(RoleManager.hasPlayerPermission(player.getUniqueId().toString(), "APPEAR_OFFLINE"))
            appearAs(inventory, player);
        inventory.setItem(49, exit());

        player.openInventory(inventory);
    }

    private static ItemStack playerHead(Player player){
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getName()));
        meta.displayName(Component.text(player.getName())
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        item.setItemMeta(meta);
        return item;
    }
    private static ItemStack roles(){
        ItemStack item = new ItemStack(Material.DIAMOND_HELMET);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Roles")
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Modify existing roles.")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }
    public static void appearAs(Inventory inventory, Player player){
        if(Vanish.isVanished(player)){
            ItemStack item = new ItemStack(Material.GRAY_DYE);
            ItemMeta meta = item.getItemMeta();

            meta.displayName(Component.text("Appearing as offline")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Click to toggle appearance")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            item.setItemMeta(meta);
            inventory.setItem(33, item);
        } else {
            ItemStack item = new ItemStack(Material.LIME_DYE);
            ItemMeta meta = item.getItemMeta();

            meta.displayName(Component.text("Appearing as online")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Click to toggle appearance")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            item.setItemMeta(meta);
            inventory.setItem(33, item);
        }
    }
    public static void msgSpy(Inventory inventory, Player player){
        if(Msg.isListening(player)){
            ItemStack item = new ItemStack(Material.LIME_DYE);
            ItemMeta meta = item.getItemMeta();

            meta.displayName(Component.text("Msg spy on")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Click to toggle msg spy")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            item.setItemMeta(meta);
            inventory.setItem(29, item);
        }else{
            ItemStack item = new ItemStack(Material.GRAY_DYE);
            ItemMeta meta = item.getItemMeta();

            meta.displayName(Component.text("Msg spy off")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Click to toggle msg spy")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            item.setItemMeta(meta);
            inventory.setItem(29, item);
        }
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
