package miguel.nu.regula.menus;

import miguel.nu.regula.Classes.Role;
import miguel.nu.regula.Main;
import miguel.nu.regula.OperationManager;
import miguel.nu.regula.menus.player.ActionMenu;
import miguel.nu.regula.menus.player.DurationMenu;
import miguel.nu.regula.menus.player.ModMenu;
import miguel.nu.regula.menus.player.PlayerMenu;
import miguel.nu.regula.menus.utils.ConfirmMenu;
import miguel.nu.regula.roles.RoleManager;
import miguel.nu.regula.utils.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
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

import java.util.*;

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
                        case 29 -> {
                            if(!RoleManager.hasPlayerPermission(event.getWhoClicked().getUniqueId().toString(), "MSG_SPY")) return;
                            Msg.toggleListening((Player) event.getWhoClicked());
                            AdminMenu.msgSpy(event.getInventory(), ((Player) event.getWhoClicked()));
                        }
                        case 21 -> {
                            if(!RoleManager.hasPlayerPermission(event.getWhoClicked().getUniqueId().toString(), "ADMIN")) return;
                            Map<NamespacedKey, String> roleMenuData = new HashMap<>();
                            roleMenuData.put(NamespaceKey.getNamespacedKey("MENU_TYPE"), "ASSIGNED");
                            RoleMenu.open((Player)event.getWhoClicked(), roleMenuData);
                        }
                        case 31 -> {
                            if(!RoleManager.hasPlayerPermission(event.getWhoClicked().getUniqueId().toString(), "ADMIN")) return;
                            Map<NamespacedKey, String> roleMenuData = new HashMap<>();
                            roleMenuData.put(NamespaceKey.getNamespacedKey("MENU_TYPE"), "EDIT");
                            RoleMenu.open((Player)event.getWhoClicked(), roleMenuData);
                        }
                        case 33 -> {
                            if(!RoleManager.hasPlayerPermission(event.getWhoClicked().getUniqueId().toString(), "VANISH")) return;
                            Vanish.toggleVanish((Player) event.getWhoClicked());
                            AdminMenu.appearAs(event.getInventory(), ((Player) event.getWhoClicked()));
                        }
                        case 49 -> {
                            event.getInventory().close();
                        }
                    }
                }
                case "MANAGING_MENU" -> {
                    event.setCancelled(true);

                    String targetUuid = event.getInventory().getItem(0).getItemMeta().getPersistentDataContainer().get(
                            NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING);

                    switch (event.getSlot()){
                        case 19 -> {
                            if(!RoleManager.hasPlayerPermission(event.getWhoClicked().getUniqueId().toString(), "ADMIN")) return;
                            Map<NamespacedKey, String> roleMenuData = new HashMap<>();
                            roleMenuData.put(NamespaceKey.getNamespacedKey("MENU_TYPE"), "REMOVE");
                            roleMenuData.put(NamespaceKey.getNamespacedKey("TARGET_UUID"), targetUuid);
                            RoleMenu.open((Player)event.getWhoClicked(), roleMenuData);
                        }
                        case 29 -> {
                            if(!RoleManager.hasPlayerPermission(event.getWhoClicked().getUniqueId().toString(), "ADMIN")) return;
                            Map<NamespacedKey, String> roleMenuData = new HashMap<>();
                            roleMenuData.put(NamespaceKey.getNamespacedKey("MENU_TYPE"), "ADD");
                            roleMenuData.put(NamespaceKey.getNamespacedKey("TARGET_UUID"), targetUuid);
                            RoleMenu.open((Player)event.getWhoClicked(), roleMenuData);
                        }
                        case 31 -> {
                            if(!RoleManager.hasPlayerPermission(event.getWhoClicked().getUniqueId().toString(), new String[]{
                                    "KICK_MEMBER", "BAN_MEMBER", "UNBAN_MEMBER", "MUTE_MEMBER", "INVSEE", "APPEAR_OFFLINE", "TELEPORT"})) return;
                            ModMenu.open((Player) event.getWhoClicked(), Bukkit.getOfflinePlayer(UUID.fromString(event.getInventory().getItem(0).getItemMeta().getPersistentDataContainer()
                                    .get(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING))));
                        }
                        case 49 -> {
                            event.getInventory().close();
                        }
                    }
                }
                case "ROLES_MENU" -> {
                    event.setCancelled(true);
                    int clickedSlot = event.getSlot();

                    PersistentDataContainer data = null;
                    if(event.getCurrentItem() != null){
                        data = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
                    }

                    if(data != null && data.has(NamespaceKey.getNamespacedKey("ROLE_NAME"))){
                        String roleName = data.get(
                            NamespaceKey.getNamespacedKey("ROLE_NAME"),
                            PersistentDataType.STRING);

                        Role role = Role.getRole(roleName);
                        if(role == null){
                            event.getWhoClicked().sendMessage("Tried to access a role that doesn't exist");
                            return;
                        }

                        String inventoryType = event.getInventory().getItem(0).getItemMeta().getPersistentDataContainer().get(
                                NamespaceKey.getNamespacedKey("MENU_TYPE"), PersistentDataType.STRING);
                        if(Objects.equals(inventoryType, "EDIT")) {
                            RoleEditMenu.open((Player)event.getWhoClicked(), role);
                        }
                        else if(Objects.equals(inventoryType, "ASSIGNED")) {
                            AssignedMenu.open((Player)event.getWhoClicked(), role);
                        }
                        else if (Objects.equals(inventoryType, "ADD")) {
                            Map<NamespacedKey, String> confirmMenuData = new HashMap<>();
                            confirmMenuData.put(NamespaceKey.getNamespacedKey("MENU_TYPE"), "ADD_ROLE");
                            confirmMenuData.put(NamespaceKey.getNamespacedKey("ROLE_NAME"), roleName);
                            confirmMenuData.put(NamespaceKey.getNamespacedKey("TARGET_UUID"), event.getInventory().getItem(0).getItemMeta().getPersistentDataContainer()
                                    .get(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING));

                            ConfirmMenu.open(
                                    (Player)event.getWhoClicked(),
                                    "Add " + roleName + " to this player?",
                                    new String[]{"By clicking confirm you will", "give this player the role", roleName},
                                    confirmMenuData
                            );
                        }
                        else if (Objects.equals(inventoryType, "REMOVE")) {
                            Map<NamespacedKey, String> confirmMenuData = new HashMap<>();
                            confirmMenuData.put(NamespaceKey.getNamespacedKey("MENU_TYPE"), "REMOVE_ROLE");
                            confirmMenuData.put(NamespaceKey.getNamespacedKey("ROLE_NAME"), roleName);
                            confirmMenuData.put(NamespaceKey.getNamespacedKey("TARGET_UUID"), event.getInventory().getItem(0).getItemMeta().getPersistentDataContainer()
                                    .get(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING));

                            ConfirmMenu.open(
                                    (Player)event.getWhoClicked(),
                                    "Remove " + roleName + " from this player?",
                                    new String[]{"By clicking confirm you will", "remove " + roleName + " from this", "player"},
                                    confirmMenuData
                            );
                        }

                    }
                    else if(clickedSlot == 40) {
                        String inventoryType = event.getInventory().getItem(0).getItemMeta().getPersistentDataContainer().get(
                                NamespaceKey.getNamespacedKey("MENU_TYPE"), PersistentDataType.STRING);
                        if(Objects.equals(inventoryType, "EDIT")) {
                            AdminMenu.open((Player)event.getWhoClicked());
                        } else if(Objects.equals(inventoryType, "ASSIGNED")) {
                            AdminMenu.open((Player)event.getWhoClicked());
                        } else if (Objects.equals(inventoryType, "ADD") || Objects.equals(inventoryType, "REMOVE")) {
                            OfflinePlayer target = Bukkit.getOfflinePlayer(
                                    UUID.fromString(event.getInventory().getItem(0).getItemMeta().getPersistentDataContainer().get(
                                            NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING)));
                            PlayerMenu.open((Player) event.getWhoClicked(), target);
                        }
                    }
                }
                case "ROLE_EDIT_MENU" -> {
                    event.setCancelled(true);
                    int clickedSlot = event.getSlot();
                    if(event.getCurrentItem() == null) return;

                    PersistentDataContainer data = null;
                    if(event.getCurrentItem() != null){
                        data = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
                    }

                    if(clickedSlot == 49) {
                        Map<NamespacedKey, String> roleMenuData = new HashMap<>();
                        roleMenuData.put(NamespaceKey.getNamespacedKey("MENU_TYPE"), "EDIT");
                        RoleMenu.open((Player)event.getWhoClicked(), roleMenuData);
                    }
                    else if (data != null && data.has(NamespaceKey.getNamespacedKey("ROLE_PERMISSION"), PersistentDataType.STRING)){
                        String role = data.get(NamespaceKey.getNamespacedKey("ROLE_NAME"), PersistentDataType.STRING);
                        String permission = data.get(NamespaceKey.getNamespacedKey("ROLE_PERMISSION"), PersistentDataType.STRING);

                        if(event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) return;
                        boolean isEnabled = event.getCurrentItem().getType() == Material.GREEN_STAINED_GLASS_PANE;

                        Role.togglePermission(permission, role);
                        RoleEditMenu.getPermissions(event.getInventory(), Role.getRole(role));
                    }
                }
                case "ROLE_ASSIGNED_MENU" -> {
                    event.setCancelled(true);
                    int clickedSlot = event.getSlot();
                    if(event.getCurrentItem() == null) return;

                    PersistentDataContainer data = null;
                    if(event.getCurrentItem() != null){
                        data = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
                    }

                    if(clickedSlot == 49) {
                        Map<NamespacedKey, String> roleMenuData = new HashMap<>();
                        roleMenuData.put(NamespaceKey.getNamespacedKey("MENU_TYPE"), "ASSIGNED");
                        RoleMenu.open((Player)event.getWhoClicked(), roleMenuData);
                    }
                    else if (data != null && data.has(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING)){
                        String target = data.get(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING);
                        if(!RoleManager.hasPermissions(event.getWhoClicked().getUniqueId().toString())) {
                            event.getWhoClicked().sendMessage(Component.text("You dont have permission to open this menu."));
                            event.getInventory().close();
                            return;
                        }
                        PlayerMenu.open((Player) event.getWhoClicked(), Bukkit.getOfflinePlayer(UUID.fromString(target)));
                    }
                }
                case "CONFIRM_MENU" -> {
                    event.setCancelled(true);
                    PersistentDataContainer data = event.getInventory().getItem(0).getItemMeta().getPersistentDataContainer();
                    String menuType = data.get(NamespaceKey.getNamespacedKey("MENU_TYPE"), PersistentDataType.STRING);
                    switch (event.getSlot()){
                        case 20 -> {
                            if(Objects.equals(menuType, "ADD_ROLE")){
                                String target = data.get(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING);

                                Map<NamespacedKey, String> roleMenuData = new HashMap<>();
                                roleMenuData.put(NamespaceKey.getNamespacedKey("MENU_TYPE"), "ADD");
                                roleMenuData.put(NamespaceKey.getNamespacedKey("TARGET_UUID"), target);
                                RoleMenu.open((Player)event.getWhoClicked(), roleMenuData);
                            } else if(Objects.equals(menuType, "REMOVE_ROLE")){
                                String target = data.get(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING);

                                Map<NamespacedKey, String> roleMenuData = new HashMap<>();
                                roleMenuData.put(NamespaceKey.getNamespacedKey("MENU_TYPE"), "REMOVE");
                                roleMenuData.put(NamespaceKey.getNamespacedKey("TARGET_UUID"), target);
                                RoleMenu.open((Player)event.getWhoClicked(), roleMenuData);
                            }
                        }
                        case 24 -> {
                            if(Objects.equals(menuType, "ADD_ROLE")){
                                String target = data.get(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING);
                                String roleName = data.get(NamespaceKey.getNamespacedKey("ROLE_NAME"), PersistentDataType.STRING);
                                RoleManager.addPlayerRole(target, roleName);
                                PlayerMenu.open((Player)event.getWhoClicked(), Bukkit.getOfflinePlayer(UUID.fromString(target)));
                            } else if(Objects.equals(menuType, "REMOVE_ROLE")){
                                String target = data.get(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING);
                                String roleName = data.get(NamespaceKey.getNamespacedKey("ROLE_NAME"), PersistentDataType.STRING);
                                RoleManager.removePlayerRole(target, roleName);
                                PlayerMenu.open((Player)event.getWhoClicked(), Bukkit.getOfflinePlayer(UUID.fromString(target)));
                            }
                        }
                    }
                }
                case "MOD_MENU" -> {
                    event.setCancelled(true);
                    OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(event.getInventory().getItem(0).getItemMeta().getPersistentDataContainer()
                            .get(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING)));
                    switch (event.getSlot()){
                        case 19 -> {
                            if(!RoleManager.hasPlayerPermission(event.getWhoClicked().getUniqueId().toString(), "KICK_MEMBER")) return;
                            Map<NamespacedKey, String> modMenuData = new HashMap<>();
                            modMenuData.put(NamespaceKey.getNamespacedKey("TARGET_UUID"), target.getUniqueId().toString());
                            ActionMenu.open("Kick", (Player) event.getWhoClicked(), target, "kick", modMenuData);
                        }
                        case 21 -> {
                            if(!RoleManager.hasPlayerPermission(event.getWhoClicked().getUniqueId().toString(), "UNBAN_MEMBER")) return;
                            Ban.unbanPlayer((Player) event.getWhoClicked(), target);
                            event.getInventory().close();
                        }
                        case 23 -> {
                            if(!RoleManager.hasPlayerPermission(event.getWhoClicked().getUniqueId().toString(), "UNMUTE_MEMBER")) return;
                            Mute.unmute((Player) event.getWhoClicked(), target.getUniqueId());
                            event.getInventory().close();
                        }
                        case 25 -> {
                            if(!RoleManager.hasPlayerPermission(event.getWhoClicked().getUniqueId().toString(), "TELEPORT")) return;
                            Teleport.teleportToPlayer((Player) event.getWhoClicked(), target);
                            event.getInventory().close();
                        }
                        case 29 -> {
                            if(!RoleManager.hasPlayerPermission(event.getWhoClicked().getUniqueId().toString(), "BAN_MEMBER")) return;
                            Map<NamespacedKey, String> modMenuData = new HashMap<>();
                            modMenuData.put(NamespaceKey.getNamespacedKey("TARGET_UUID"), target.getUniqueId().toString());
                            ActionMenu.open("Ban", (Player) event.getWhoClicked(), target, "ban", modMenuData);
                        }
                        case 31 -> {
                            if(!RoleManager.hasPlayerPermission(event.getWhoClicked().getUniqueId().toString(), "MUTE_MEMBER")) return;
                            Map<NamespacedKey, String> modMenuData = new HashMap<>();
                            modMenuData.put(NamespaceKey.getNamespacedKey("TARGET_UUID"), target.getUniqueId().toString());
                            ActionMenu.open("Mute", (Player) event.getWhoClicked(), target, "mute", modMenuData);
                        }
                        case 33 -> {
                            if(!RoleManager.hasPlayerPermission(event.getWhoClicked().getUniqueId().toString(), "INVSEE")) return;
                            if(!target.isOnline()){
                                event.getWhoClicked().sendMessage(target.getName() + " is not online");
                                event.getInventory().close();
                                return;
                            }
                            Invensee.open((Player) event.getWhoClicked(), target.getPlayer(), Invensee.Mode.INVENTORY);
                        }
                        case 49 -> {
                            PlayerMenu.open((Player) event.getWhoClicked(), target);
                        }
                    }
                }
                case "ACTION_MENU" -> {
                    event.setCancelled(true);
                    int clicked = event.getSlot();
                    PersistentDataContainer data = null;
                    if(event.getCurrentItem() != null){
                        data = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
                    }

                    if(clicked == 49){
                        ModMenu.open((Player) event.getWhoClicked(), Bukkit.getOfflinePlayer(UUID.fromString(event.getInventory().getItem(0).getItemMeta().getPersistentDataContainer()
                                .get(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING))));
                    }
                    else if (data != null && data.has(NamespaceKey.getNamespacedKey("ACTION_ID"), PersistentDataType.STRING)){
                        if(!data.has(NamespaceKey.getNamespacedKey("HAS_DURATION"), PersistentDataType.STRING)){
                            if(OperationManager.runOperation((Player) event.getWhoClicked(), Bukkit.getOfflinePlayer(UUID.fromString(event.getInventory().getItem(0).getItemMeta().getPersistentDataContainer()
                                            .get(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING))),
                                    data.get(NamespaceKey.getNamespacedKey("ACTION_ID"), PersistentDataType.STRING))){
                                ModMenu.open((Player) event.getWhoClicked(), Bukkit.getOfflinePlayer(UUID.fromString(event.getInventory().getItem(0).getItemMeta().getPersistentDataContainer()
                                        .get(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING))));
                            } else{
                                event.getInventory().close();
                            }
                        } else {
                            OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(event.getInventory().getItem(0).getItemMeta().getPersistentDataContainer()
                                    .get(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING)));

                            Map<NamespacedKey, String> durationMenuData = new HashMap<>();
                            durationMenuData.put(NamespaceKey.getNamespacedKey("TARGET_UUID"), target.getUniqueId().toString());

                            DurationMenu.open((Player) event.getWhoClicked(), target,
                                    data.get(NamespaceKey.getNamespacedKey("ACTION_ID"), PersistentDataType.STRING),
                                    durationMenuData);
                        }
                    }
                }
                case "DURATION_MENU" -> {
                    event.setCancelled(true);
                    int clicked = event.getSlot();
                    PersistentDataContainer data = null;
                    if(event.getCurrentItem() != null){
                        data = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
                    }

                    if(clicked == 49){
                        ModMenu.open((Player) event.getWhoClicked(), Bukkit.getOfflinePlayer(UUID.fromString(event.getInventory().getItem(0).getItemMeta().getPersistentDataContainer()
                                .get(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING))));
                    }
                    else if (data != null && data.has(NamespaceKey.getNamespacedKey("ACTION_ID"), PersistentDataType.STRING)){
                        if(OperationManager.runOperation((Player) event.getWhoClicked(), Bukkit.getOfflinePlayer(UUID.fromString(event.getInventory().getItem(0).getItemMeta().getPersistentDataContainer()
                                        .get(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING))),
                                data.get(NamespaceKey.getNamespacedKey("ACTION_ID"), PersistentDataType.STRING),
                                data.get(NamespaceKey.getNamespacedKey("DURATION"), PersistentDataType.INTEGER))){
                            ModMenu.open((Player) event.getWhoClicked(), Bukkit.getOfflinePlayer(UUID.fromString(event.getInventory().getItem(0).getItemMeta().getPersistentDataContainer()
                                    .get(NamespaceKey.getNamespacedKey("TARGET_UUID"), PersistentDataType.STRING))));
                        } else{
                            event.getInventory().close();
                        }
                    }
                }
            }
        }
    }
}
