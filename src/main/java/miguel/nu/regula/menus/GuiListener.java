package miguel.nu.regula.menus;

import miguel.nu.regula.Classes.Role;
import miguel.nu.regula.Main;
import miguel.nu.regula.utils.NamespaceKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Objects;

public class GuiListener implements Listener {
    private final Plugin plugin;

    public GuiListener(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();

        if (top.getHolder() instanceof MenuHolder holder) {
            switch (holder.getId()) {
                case "ADMIN_MENU" -> {
                    event.setCancelled(true);
                    switch (event.getSlot()){
                        case 31 -> {
                            RoleMenu.open((Player)event.getWhoClicked());
                        }
                        case 49 -> {
                            event.getInventory().close();
                        }
                    }
                }
                case "MANAGING_MENU" -> {
                    event.setCancelled(true);
                    switch (event.getSlot()){
                        case 49 -> {
                            event.getInventory().close();
                        }
                    }
                }
                case "ROLES_MENU" -> {
                    event.setCancelled(true);
                    int clickedSlot = event.getSlot();
                    if(clickedSlot == 19 || clickedSlot == 21 || clickedSlot == 23 || clickedSlot == 25){
                        PersistentDataContainer data = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
                        String roleName = data.getOrDefault(
                            NamespaceKey.getNamespacedKey("ROLE_NAME"),
                            PersistentDataType.STRING,
                            "ERROR");

                        Role role = Role.getRole(roleName);
                        if(role == null){
                            event.getWhoClicked().sendMessage("Tried to access a role that doesn't exist");
                            return;
                        }
                        RoleEditMenu.open((Player)event.getWhoClicked(), role);
                    }
                    else if(clickedSlot == 40) AdminMenu.open((Player)event.getWhoClicked());
                }
                case "ROLE_EDIT_MENU" -> {
                    event.setCancelled(true);
                    int clickedSlot = event.getSlot();
                    if(event.getCurrentItem() == null) return;

                    PersistentDataContainer data = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
                    System.out.println(data.has(NamespaceKey.getNamespacedKey("ROLE_PERMISSION"), PersistentDataType.STRING));
                    if(clickedSlot == 49) {
                        RoleMenu.open((Player)event.getWhoClicked());
                    }
                    else if (data.has(NamespaceKey.getNamespacedKey("ROLE_PERMISSION"), PersistentDataType.STRING)){
                        String role = data.get(NamespaceKey.getNamespacedKey("ROLE_NAME"), PersistentDataType.STRING);
                        String permission = data.get(NamespaceKey.getNamespacedKey("ROLE_PERMISSION"), PersistentDataType.STRING);

                        if(event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) return;
                        boolean isEnabled = event.getCurrentItem().getType() == Material.GREEN_STAINED_GLASS_PANE;

                        Role.togglePermission(permission, role);
                        RoleEditMenu.getPermissions(event.getInventory(), Role.getRole(role));
                    }
                }
            }
        }
    }
}
