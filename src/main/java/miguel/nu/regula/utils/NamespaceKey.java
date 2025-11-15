package miguel.nu.regula.utils;

import miguel.nu.regula.Main;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NamespaceKey {
    private static List<NamespacedKey> namespaces = new ArrayList<>();

    @NotNull
    public static NamespacedKey getNamespacedKey(String key){
        for (NamespacedKey namespace : namespaces) {
            if (namespace.getKey().equals(key.toLowerCase())) return namespace;
        }
        throw new RuntimeException("Couldnt not find the correct namespace key for: " + key);
    }

    public static void createNamespaceKeys(){
        namespaces.add(new NamespacedKey(Main.plugin, "ROLE_NAME"));
        namespaces.add(new NamespacedKey(Main.plugin, "ROLE_PERMISSION"));
        namespaces.add(new NamespacedKey(Main.plugin, "MENU_TYPE"));
        namespaces.add(new NamespacedKey(Main.plugin, "TARGET_UUID"));
        namespaces.add(new NamespacedKey(Main.plugin, "ACTION_ID"));
        namespaces.add(new NamespacedKey(Main.plugin, "HAS_DURATION"));
        namespaces.add(new NamespacedKey(Main.plugin, "DURATION"));
        namespaces.add(new NamespacedKey(Main.plugin, "NICKNAME"));
        namespaces.add(new NamespacedKey(Main.plugin, "CURRENT_PAGE"));
    }
}
