package miguel.nu.regula.Classes;

import miguel.nu.regula.ConfigManager;
import miguel.nu.regula.Main;
import miguel.nu.regula.menus.RoleEditMenu;
import miguel.nu.regula.roles.RoleManager;
import org.bukkit.Material;

import java.util.*;

import static miguel.nu.regula.roles.RoleManager.getPlayerPermission;

public class Role {
    public static List<Role> getAllRoles(){
        List<Role> outRoles = new ArrayList<>();

        for (Object obj : Main.plugin.getConfig().getList("roles")){
            if(!(obj instanceof Map<?,?> map)) continue;

            List<String> mcPerms = new ArrayList<>();
            Object raw = map.get("mc-perm");
            if (raw instanceof List<?> rawList) {
                for (Object entry : rawList) {
                    if (entry != null) mcPerms.add(entry.toString());
                }
            }

            outRoles.add(new Role(
                    map.get("name").toString(),
                    map.get("display").toString(),
                    map.get("placeholder").toString(),
                    mcPerms,
                    (int) map.get("permission"),
                    map.get("namecolor").toString()
            ));
        }
        return outRoles;
    }
    public static Role getRole(String name){
        for (Object obj : Main.plugin.getConfig().getList("roles")){
            if(!(obj instanceof Map<?,?> map)) continue;
            if(!Objects.equals(map.get("name").toString(), name)) continue;

            List<String> mcPerms = new ArrayList<>();
            Object raw = map.get("mc-perm");
            if (raw instanceof List<?> rawList) {
                for (Object entry : rawList) {
                    if (entry != null) mcPerms.add(entry.toString());
                }
            }

            return new Role(
                    map.get("name").toString(),
                    map.get("display").toString(),
                    map.get("placeholder").toString(),
                    mcPerms,
                    (int) map.get("permission"),
                    map.get("namecolor").toString()
            );
        }
        return null;
    }
    public static boolean saveRole(Role role){
        return true;
    }

    public static boolean hasPermission(String permission, Role role){
        List<Map<?, ?>> permissions = ConfigManager.getAllPermission();
        for(int i = 0; i < permissions.size(); i++){
            if(permissions.get(i).get("perm").equals(permission)){
                return (role.permission & (1 << i)) != 0;
            }
        }
        Main.plugin.getLogger().severe("Tried to check non existing permission for a role: " + permission);
        return false;
    }

    public static boolean hasMinecraftPermission(String permission, Role role) {
        if (permission == null || role == null) return false;

        List<Map<?, ?>> permissions = ConfigManager.getAllPermission();
        if (permissions.isEmpty()) return false;

        int total = permissions.size();

        // ----- wildcard "*" support -----
        if (permission.equals("*")) {
            // Build a mask with N lowest bits set: if total=3 → mask = 0b111
            int mask = (1 << total) - 1;
            return (role.permission & mask) == mask;
        }

        // ----- normal permission lookup -----
        for (int i = 0; i < total; i++) {
            Map<?, ?> entry = permissions.get(i);
            if (!(entry.get("mc-perm") instanceof List<?> list)) continue;

            // Does this entry contain the given mc permission?
            boolean match = false;
            for (Object obj : list) {
                if (obj != null && permission.equalsIgnoreCase(obj.toString())) {
                    match = true;
                    break;
                }
            }

            if (!match) continue;

            // Found matching index -> check bit
            return (role.permission & (1 << i)) != 0;
        }

        return false;
    }

    public static void togglePermission(String permission, String roleName){
        List<Map<?, ?>> permissions = ConfigManager.getAllPermission();

        int permissionIndex = -1;
        for(int i = 0; i < permissions.size(); i++){
            if(permissions.get(i).get("perm").equals(permission)){
                permissionIndex = i;
                break;
            }
        }
        if(permissionIndex == -1) {
            Main.plugin.getLogger().warning("Tried to remove non existing permission to a role: " + permission);
            return;
        }

        int bit = 1 << permissionIndex;
        List<Map<?, ?>> roles = Main.plugin.getConfig().getMapList("roles");
        int rolePos = -1;
        for (int i = 0; i < roles.size(); i++) {
            Map<?, ?> map = roles.get(i);
            Object name = map.get("name");
            if (name != null && name.toString().equalsIgnoreCase(roleName)) {
                rolePos = i;
                break;
            }
        }
        if (rolePos < 0) {
            Main.plugin.getLogger().warning("Role not found: " + roleName);
            return;
        }

        Map<?, ?> raw = roles.get(rolePos);
        @SuppressWarnings("unchecked")
        Map<Object, Object> roleMap = (Map<Object, Object>) raw;

        Object p = roleMap.get("permission");
        int current = (p instanceof Number) ? ((Number) p).intValue() : 0;
        int updated = current ^ bit;

        Role role = Role.getRole(roleName); // or roleMap.get("name").toString()
        if (role != null) {
            System.out.println(role.getMinecraftPermissions());
            // Permissions this role should now give
            List<String> newPerms = role.getMinecraftPermissions();

            // All players that currently have this role
            List<String> playerIds = RoleManager.getAllPlayersWithRole(roleName);

            for (String playerId : playerIds) {
                // Adjust this depending on what RoleManager returns (UUID string, name, etc.)
                UUID playerUuid = UUID.fromString(playerId);

                // Permissions the player currently has (from your own tracking / cache)
                List<String> oldPerms = getPlayerPermission(String.valueOf(playerUuid));

                List<String> permsToAdd = new ArrayList<>();
                List<String> permsToRemove = new ArrayList<>();

                // New perms that the player is missing → need to ADD
                for (String perm : newPerms) {
                    if (!oldPerms.contains(perm)) {
                        permsToAdd.add(perm);
                    }
                }

                // Old perms that are no longer in the role → need to REMOVE
                for (String perm : oldPerms) {
                    if (!newPerms.contains(perm)) {
                        permsToRemove.add(perm);
                    }
                }

                // Apply changes via LuckPerms
                for (String perm : permsToAdd) {
                    Main.luckyPerms.givePermToUuid(UUID.fromString(playerId), perm);

                }

                for (String perm : permsToRemove) {
                    Main.luckyPerms.removePermFromUuid(UUID.fromString(playerId), perm);
                }
            }
        }

        roleMap.put("permission", updated);
        roles.set(rolePos, roleMap);

        Main.plugin.getConfig().set("roles", roles);
        Main.plugin.saveConfig();
    }

    private String name;
    private String display;
    private Material placeholder;
    private String namecolor;
    private List<String> minecraftPermissions;
    private int permission;

    public Role(String name, String display, String placeholder, List<String> minecraftPermissions, int permission, String namecolor){
        this.name = name;
        this.display = display;
        this.placeholder = Material.valueOf(placeholder);
        this.minecraftPermissions = minecraftPermissions;
        this.permission = permission;
        this.namecolor = namecolor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public Material getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(Material placeholder) {
        this.placeholder = placeholder;
    }

    public List<String> getMinecraftPermissions() {
        return minecraftPermissions;
    }

    public void setMinecraftPermissions(List<String> minecraftPermissions) {
        this.minecraftPermissions = minecraftPermissions;
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public String getNamecolor() {
        return namecolor;
    }

    public void setNamecolor(String namecolor) {
        this.namecolor = namecolor;
    }
}
