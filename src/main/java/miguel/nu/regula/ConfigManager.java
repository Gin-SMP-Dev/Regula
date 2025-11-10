package miguel.nu.regula;

import miguel.nu.regula.Classes.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    public static List<Map<?,?>> getAllPermission(){
        List<Map<?,?>> outPerms = new ArrayList<>();

        for (Object obj : Main.plugin.getConfig().getList("permissions")){
            if(!(obj instanceof Map<?,?> map)) continue;
            outPerms.add(map);
        }
        return outPerms;
    }
    public static List<Map<?,?>> getAllAction(String type){
        List<Map<?,?>> outActions = new ArrayList<>();

        for (Object obj : Main.plugin.getConfig().getList("actions." + type)){
            if(!(obj instanceof Map<?,?> map)) continue;
            outActions.add(map);
        }
        return outActions;
    }
    public static Map<?,?> getAction(String type){
        String[] t = type.split("_");

        for (Object obj : Main.plugin.getConfig().getList("actions." + t[0].toLowerCase())){
            if(!(obj instanceof Map<?,?> map)) continue;
            if(map.get("id") == type) return map;
        }
        return null;
    }
}
