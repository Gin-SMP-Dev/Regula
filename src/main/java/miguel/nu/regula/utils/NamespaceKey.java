package miguel.nu.regula.utils;

import miguel.nu.regula.Main;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

public class NamespaceKey {
    private static List<NamespacedKey> namespaces = new ArrayList<>();
    public static NamespacedKey getNamespacedKey(String key){
        for(int i = 0; i < namespaces.size(); i++){
            if(namespaces.get(i).getKey().equals(key.toLowerCase())) return namespaces.get(i);
        }
        return null;
    }

    public static void createNamespaceKeys(){
        namespaces.add(new NamespacedKey(Main.plugin, "ROLE_NAME"));
        namespaces.add(new NamespacedKey(Main.plugin, "ROLE_PERMISSION"));
    }
}
