package miguel.nu.regula.Classes;

import miguel.nu.regula.ConfigManager;
import miguel.nu.regula.Main;
import miguel.nu.regula.menus.RoleEditMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Role {
    public static List<Role> getAllRoles(int offset){
        List<Role> outRoles = new ArrayList<>();

        int count = 0;
        for (Object obj : Main.plugin.getConfig().getList("roles")){
            if(count >= 4) continue;
            if(!(obj instanceof Map<?,?> map)) continue;
            outRoles.add(new Role(
                    map.get("name").toString(),
                    map.get("display").toString(),
                    (int) map.get("permission")
            ));
            count++;
        }
        return outRoles;
    }
    public static Role getRole(String name){
        List<Role> outRoles = new ArrayList<>();
        System.out.println(name);
        for (Object obj : Main.plugin.getConfig().getList("roles")){
            if(!(obj instanceof Map<?,?> map)) continue;
            if(!Objects.equals(map.get("name").toString(), name)) continue;
            return new Role(
                    map.get("name").toString(),
                    map.get("display").toString(),
                    (int) map.get("permission")
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
        Main.plugin.getLogger().warning("Tried to check non existing permission for a role: " + permission);
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

        roleMap.put("permission", updated);
        roles.set(rolePos, roleMap);

        Main.plugin.getConfig().set("roles", roles);
        Main.plugin.saveConfig();
    }

    private String name;
    private String display;
    private int permission;

    public Role(String name, String display, int permission){
        this.name = name;
        this.display = display;
        this.permission = permission;
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

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }
}
