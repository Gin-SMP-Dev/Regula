package miguel.nu.regula.menus.player;

import miguel.nu.regula.ConfigManager;
import miguel.nu.regula.menus.MenuHolder;
import miguel.nu.regula.menus.MenuPrefab;
import miguel.nu.regula.utils.Ban;
import miguel.nu.regula.utils.Mute;
import miguel.nu.regula.utils.NamespaceKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.ObjectInputFilter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DurationMenu {
    public static void open(Player player, OfflinePlayer target, String type, Map<NamespacedKey, String> dataMap){
        MenuHolder holder = new MenuHolder("DURATION_MENU", 54, Component.text("Pick Duration"));
        Inventory inventory = holder.getInventory();

        MenuPrefab.drawBorder(inventory);
        timeRemaining(inventory, target, type);
        inventory.setItem(46, previous());
        inventory.setItem(49, exit());
        inventory.setItem(52, next());
        getDurations(inventory, type);
        ItemMeta meta = inventory.getItem(0).getItemMeta();
        for (NamespacedKey key : dataMap.keySet()){
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, dataMap.get(key));
        }
        inventory.getItem(0).setItemMeta(meta);

        player.openInventory(inventory);
    }

    private static ItemStack previous(){
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Previous Page")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Click to turn page!")
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }
    private static ItemStack next(){
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Next Page")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Click to turn page!")
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }
    private static ItemStack exit(){
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Back")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Click to close the menu.")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }

    public static void getDurations(Inventory inventory, String type){
        int[] slots = {
                20, 21, 22, 23, 24,
                29, 30, 31, 32, 33
        };
        System.out.println(type);
        Map<?, ?> action = ConfigManager.getAction(type);
        List<Integer> durations = (List<Integer>) action.get("options");

        for(int i = 0; i < durations.size(); i++){
            ItemStack item = new ItemStack(Material.RED_WOOL);
            ItemMeta meta = item.getItemMeta();

            meta.displayName(Component.text(convertStrToTime(durations.get(i)))
                    .color(NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));

            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(NamespaceKey.getNamespacedKey("ACTION_ID"), PersistentDataType.STRING, action.get("id").toString());
            data.set(NamespaceKey.getNamespacedKey("DURATION"), PersistentDataType.INTEGER, durations.get(i));

            item.setItemMeta(meta);
            inventory.setItem(slots[i], item);
        }
    }

    public static void timeRemaining(Inventory inventory, OfflinePlayer player, String type){
        type = type.split("_")[0];
        if(!Objects.equals(type, "ban") && !Objects.equals(type, "mute")){
            return;
        }

        if(type.equals("mute") && !Mute.isMuted(player.getUniqueId()) || type.equals("ban") && !Ban.isPlayerBanned(player)){
            return;
        }

        String time = type.equals("ban") ? Ban.getBanTimeRemaining(player) : Mute.getMuteTimeLeft(player.getUniqueId());

        ItemStack item = new ItemStack(Material.OAK_SIGN);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(player.getName() + " is " + (type.equals("ban") ? "banned" : "muted"))
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text(time)
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        inventory.setItem(13, item);
    }

    private static String convertStrToTime(Integer duration){
        if(duration == -1){
            return "Forever";
        }
        else if(duration > 86400f){
            return Math.round(duration / 86400f) + "d";
        }
        else if(duration > 3600f){
            return Math.round(duration / 3600f) + "h";
        }
        else if(duration > 60f){
            return Math.round(duration / 60) + "m";
        }
        else {
            return duration + "s";
        }
    }
}
