package miguel.nu.regula.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuPrefab {
    public static void drawBorder(Inventory inventory){
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setHideTooltip(true);
        item.setItemMeta(meta);
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
}
