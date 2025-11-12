package miguel.nu.regula.utils;

import miguel.nu.regula.Main;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;

public class GraveBreak implements Listener {
    public static boolean isActive(Player player) {
        NamespacedKey key = new NamespacedKey(Main.plugin, "breaking_enabled");
        return player.getPersistentDataContainer().getOrDefault(key, PersistentDataType.BYTE, (byte)0) == 1;
    }

    public static void toggleBreaking(Player player) {
        NamespacedKey key = new NamespacedKey(Main.plugin, "breaking_enabled");
        var data = player.getPersistentDataContainer();

        boolean nowEnabled;
        if (data.has(key, PersistentDataType.BYTE)) {
            byte current = data.get(key, PersistentDataType.BYTE);
            nowEnabled = current == 0;
        } else {
            nowEnabled = true;
        }

        data.set(key, PersistentDataType.BYTE, (byte) (nowEnabled ? 1 : 0));
        player.sendMessage(nowEnabled
                ? "§aYou can now break gravestones."
                : "§cYou can now break gravestones.");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        resetBreaking(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        resetBreaking(event.getPlayer());
    }

    private void resetBreaking(Player player) {
        NamespacedKey key = new NamespacedKey(Main.plugin, "breaking_enabled");
        player.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 0);
    }
}
