package miguel.nu.regula.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MenuPrefab {
    public static ItemStack emptyItem(){
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setHideTooltip(true);
        item.setItemMeta(meta);
        return item;
    }
    public static void drawBorder(Inventory inventory){
        ItemStack item = emptyItem();
        int size = inventory.getSize();

        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;

            boolean isTop = row == 0;
            boolean isBottom = row == (size / 9) - 1;
            boolean isLeft = col == 0;
            boolean isRight = col == 8;

            if (isTop || isBottom || isLeft || isRight) {
                inventory.setItem(i, item);
            }
        }
    }
    public static ItemStack drawNoPermission(){
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Not Enough Permission")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        item.setItemMeta(meta);
        return item;
    }
}
