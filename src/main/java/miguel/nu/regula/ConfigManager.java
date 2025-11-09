package miguel.nu.regula;

import miguel.nu.regula.Classes.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    public static List<Map<?,?>> getAllPermission(){
        List<Map<?,?>> outPerms = new ArrayList<>();

        int count = 0;
        for (Object obj : Main.plugin.getConfig().getList("permissions")){
            if(count >= 14) continue;
            if(!(obj instanceof Map<?,?> map)) continue;
            outPerms.add(map);
            count++;
        }
        return outPerms;
    }
}
