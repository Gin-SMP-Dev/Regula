package miguel.nu.regula.menus.player;

import miguel.nu.regula.menus.MenuHolder;
import miguel.nu.regula.menus.MenuPrefab;
import miguel.nu.regula.roles.RoleManager;
import miguel.nu.regula.utils.NamespaceKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static miguel.nu.regula.menus.MenuPrefab.drawNoPermission;

public class PlayerMenu {
    public static void open(Player player, OfflinePlayer target){
        MenuHolder holder = new MenuHolder("MANAGING_MENU", 54, Component.text("Managing " + target.getName()));
        Inventory inventory = holder.getInventory();

        MenuPrefab.drawBorder(inventory);
        inventory.setItem(13, playerHead(target));
        if(RoleManager.hasPlayerPermission(player.getUniqueId().toString(), "ADMIN"))
            inventory.setItem(19, removeRole());
        else
            inventory.setItem(19, drawNoPermission());

        if(RoleManager.hasPlayerPermission(player.getUniqueId().toString(), "ADMIN"))
            inventory.setItem(29, addRole());
        else
            inventory.setItem(29, drawNoPermission());

        if(RoleManager.hasPlayerPermission(player.getUniqueId().toString(), new String[]{
                "KICK_MEMBER", "BAN_MEMBER", "UNBAN_MEMBER", "MUTE_MEMBER", "INVSEE", "APPEAR_OFFLINE", "TELEPORT"}))
            inventory.setItem(31, moderate());
        else
            inventory.setItem(31, drawNoPermission());

        inventory.setItem(49, exit());

        ItemMeta meta = inventory.getItem(0).getItemMeta();
        meta.getPersistentDataContainer().set(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING, target.getUniqueId().toString());
        inventory.getItem(0).setItemMeta(meta);

        player.openInventory(inventory);
    }
    private static ItemStack playerHead(OfflinePlayer player){
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        meta.displayName(Component.text(player.getName())
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Known bug with skin")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }
    private static ItemStack removeRole(){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Remove Role")
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Remove a role from this user.")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }
    private static ItemStack addRole(){
        ItemStack item = new ItemStack(Material.DIAMOND_HELMET);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Add Role")
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Add a role from this user.")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }
    private static ItemStack moderate(){
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Moderate")
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Click to moderate this player")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
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
