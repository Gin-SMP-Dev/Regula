package miguel.nu.regula.Classes;

import miguel.nu.regula.ConfigManager;
import miguel.nu.regula.Main;
import miguel.nu.regula.menus.RoleEditMenu;
import miguel.nu.regula.roles.RoleManager;
import miguel.nu.regula.utils.LuckyPerms;
import org.bukkit.Material;

import java.util.*;

import static miguel.nu.regula.roles.RoleManager.getPlayerPermission;

public class Role {
    public static List<Role> getAllRoles() {
        List<Role> outRoles = new ArrayList<>();

        List<?> permissionList = Main.plugin.getConfig().getList("permissions");
        if (permissionList == null) permissionList = List.of();

        for (Object obj : Main.plugin.getConfig().getList("roles")) {
            if (!(obj instanceof Map<?, ?> roleMap)) continue;

            int rolePermMask = ((Number) roleMap.get("permission")).intValue();

            List<String> mcPerms = new ArrayList<>();

            for (int i = 0; i < permissionList.size(); i++) {
                Object pObj = permissionList.get(i);
                if (!(pObj instanceof Map<?, ?> permMap)) continue;

                int permBit = 1 << i;

                if ((rolePermMask & permBit) == 0) continue;

                Object rawMc = permMap.get("mc-perm");
                if (rawMc instanceof List<?> rawList) {
                    for (Object entry : rawList) {
                        if (entry != null) mcPerms.add(entry.toString());
                    }
                }
            }

            outRoles.add(new Role(
                    roleMap.get("name").toString(),
                    roleMap.get("display").toString(),
                    roleMap.get("placeholder").toString(),
                    mcPerms,
                    rolePermMask,
                    roleMap.get("namecolor").toString()
            ));
        }

        return outRoles;
    }
    public static Role getRole(String name){
        for (Object obj : Main.plugin.getConfig().getList("roles")){
            if(!(obj instanceof Map<?,?> map)) continue;
            if(!Objects.equals(map.get("name").toString(), name)) continue;

            List<?> permissionList = Main.plugin.getConfig().getList("permissions");
            if (permissionList == null) permissionList = List.of();

            int rolePermMask = ((Number) map.get("permission")).intValue();

            List<String> mcPerms = new ArrayList<>();

            for (int i = 0; i < permissionList.size(); i++) {
                Object pObj = permissionList.get(i);
                if (!(pObj instanceof Map<?, ?> permMap)) continue;

                int permBit = 1 << i;

                if ((rolePermMask & permBit) == 0) continue;

                Object rawMc = permMap.get("mc-perm");
                if (rawMc instanceof List<?> rawList) {
                    for (Object entry : rawList) {
                        if (entry != null) mcPerms.add(entry.toString());
                    }
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

    public static List<String> getAllMinecraftPermissions() {
        List<String> out = new ArrayList<>();

        List<?> permissionList = Main.plugin.getConfig().getList("permissions");
        if (permissionList == null) return out;

        for (Object obj : permissionList) {
            if (!(obj instanceof Map<?, ?> map)) continue;

            Object raw = map.get("mc-perm");
            if (raw instanceof List<?> rawList) {
                for (Object entry : rawList) {
                    if (entry != null) out.add(entry.toString());
                }
            }
        }

        return out;
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

        List<String> affectedUuids = RoleManager.getAllPlayersWithRole(roleName);

        Map<?, ?> raw = roles.get(rolePos);
        @SuppressWarnings("unchecked")
        Map<Object, Object> roleMap = (Map<Object, Object>) raw;

        Object p = roleMap.get("permission");
        int current = (p instanceof Number) ? ((Number) p).intValue() : 0;
        int updated = current ^ bit;

        roleMap.put("permission", updated);
        roles.set(rolePos, roleMap);

        Main.plugin.getConfig().set("roles", roles);
        Main.plugin.saveConfig();

        for(String uuid : affectedUuids){
            new LuckyPerms().syncPermOfUuid(UUID.fromString(uuid));
        }
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
