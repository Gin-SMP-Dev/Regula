package miguel.nu.regula.utils;

import miguel.nu.discordRelay.API.DiscordAPI;
import miguel.nu.regula.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Invensee implements Listener {

    public enum Mode { INVENTORY, ENDER }

    private static final String TITLE_INV = "%s (Inventory)";
    private static final String TITLE_END = "%s (Ender Chest)";
    private static final int SIZE = 54;

    private static final int SLOT_HELM = 36;
    private static final int SLOT_CHEST = 37;
    private static final int SLOT_LEGS = 38;
    private static final int SLOT_BOOTS = 39;
    private static final int SLOT_OFFHAND = 40;
    private static final int SLOT_TOGGLE = 49;

    private static final ItemStack GLASS_BLOCKER = makeBlocker(Material.BLACK_STAINED_GLASS_PANE);
    private static final ItemStack LABEL_INV = makeLabel(Material.GRAY_STAINED_GLASS_PANE, "§7Inventory");
    private static final ItemStack LABEL_HOTBAR = makeLabel(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§7Hotbar");
    private static final ItemStack LABEL_ARMOR = makeLabel(Material.GRAY_STAINED_GLASS_PANE, "§7Armor / Offhand");
    private static final ItemStack LABEL_ENDER = makeLabel(Material.PURPLE_STAINED_GLASS_PANE, "§5Ender Chest");

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static int taskId = -1;

    private static class Session {
        final UUID viewer;
        final UUID target;
        Mode mode;
        Session(UUID viewer, UUID target, Mode mode) { this.viewer = viewer; this.target = target; this.mode = mode; }
    }

    public static void open(Player viewer, Player target, Mode mode) {
        Inventory gui = Bukkit.createInventory(new Holder(target.getUniqueId(), mode), SIZE,
                mode == Mode.INVENTORY ? String.format(TITLE_INV, target.getName())
                        : String.format(TITLE_END, target.getName()));
        paintFrame(gui, mode);
        paintContents(gui, target, mode);
        viewer.openInventory(gui);

        SESSIONS.put(viewer.getUniqueId(), new Session(viewer.getUniqueId(), target.getUniqueId(), mode));
        startRefresherIfNeeded();
        DiscordAPI.sendModLog(target, "Invsee", null, -2, viewer);
    }

    // ---------------- UI ----------------

    private static void paintFrame(Inventory gui, Mode mode) {
        gui.clear();
        for (int i = 45; i <= 53; i++) gui.setItem(i, GLASS_BLOCKER.clone());
        gui.setItem(SLOT_TOGGLE, makeToggle(mode));
        for (int i = 41; i <= 44; i++) gui.setItem(i, GLASS_BLOCKER.clone());
        gui.setItem(42, LABEL_ARMOR.clone());
        gui.setItem(13, mode == Mode.INVENTORY ? LABEL_INV.clone() : LABEL_ENDER.clone());
        gui.setItem(31, LABEL_HOTBAR.clone());
        if (mode == Mode.ENDER) for (int i = 27; i <= 40; i++) gui.setItem(i, GLASS_BLOCKER.clone());
    }

    private static void paintContents(Inventory gui, Player target, Mode mode) {
        if (mode == Mode.INVENTORY) {
            PlayerInventory pinv = target.getInventory();
            ItemStack[] store = pinv.getStorageContents();
            for (int i = 0; i < Math.min(36, store.length); i++) gui.setItem(i, safeClone(store[i]));
            gui.setItem(SLOT_HELM, safeClone(pinv.getHelmet()));
            gui.setItem(SLOT_CHEST, safeClone(pinv.getChestplate()));
            gui.setItem(SLOT_LEGS, safeClone(pinv.getLeggings()));
            gui.setItem(SLOT_BOOTS, safeClone(pinv.getBoots()));
            gui.setItem(SLOT_OFFHAND, safeClone(pinv.getItemInOffHand()));
        } else {
            ItemStack[] end = target.getEnderChest().getStorageContents();
            for (int i = 0; i < 27; i++) gui.setItem(i, i < end.length ? safeClone(end[i]) : null);
        }
    }

    private static ItemStack makeBlocker(Material type) {
        ItemStack it = new ItemStack(type);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(null);
        meta.setHideTooltip(true);
        it.setItemMeta(meta);
        return it;
    }

    private static ItemStack makeLabel(Material type, String name) {
        ItemStack it = new ItemStack(type);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        meta.setHideTooltip(true);
        it.setItemMeta(meta);
        return it;
    }

    private static ItemStack makeToggle(Mode mode) {
        ItemStack it = new ItemStack(mode == Mode.INVENTORY ? Material.ENDER_CHEST : Material.CHEST);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(mode == Mode.INVENTORY ? "§aView Ender Chest" : "§aView Inventory");
        it.setItemMeta(meta);
        return it;
    }

    // -------------- Events --------------

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof Holder holder)) return;
        Player viewer = (Player) e.getWhoClicked();
        Player target = Bukkit.getPlayer(holder.target());
        if (target == null || !target.isOnline()) { viewer.closeInventory(); return; }

        Inventory top = e.getView().getTopInventory();
        int raw = e.getRawSlot();
        Mode mode = holder.mode();

        if (raw == SLOT_TOGGLE) {
            e.setCancelled(true);
            Mode next = (mode == Mode.INVENTORY) ? Mode.ENDER : Mode.INVENTORY;
            open(viewer, target, next);
            return;
        }

        if (raw >= top.getSize()) return;

        ItemStack clicked = e.getCurrentItem();
        if (isBlocker(clicked) || isLabel(clicked)) { e.setCancelled(true); return; }
        if (mode == Mode.ENDER && raw > 26) { e.setCancelled(true); return; }

        e.setCancelled(true);

        InventoryAction action = e.getAction();
        switch (action) {
            case PICKUP_ALL, PICKUP_HALF, PICKUP_ONE, PICKUP_SOME -> pickup(e, top, raw, viewer);
            case PLACE_ALL, PLACE_ONE, PLACE_SOME -> place(e, top, raw, viewer);
            case SWAP_WITH_CURSOR -> swap(e, top, raw, viewer);
            case MOVE_TO_OTHER_INVENTORY -> shift(e, top, raw, viewer);
            case HOTBAR_SWAP -> hotbar(e, top, raw, viewer);
            case COLLECT_TO_CURSOR -> collect(e, top, viewer, mode);
            default -> {}
        }

        Bukkit.getScheduler().runTask(Main.plugin, () -> sync(top, target, mode));
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getInventory().getHolder() instanceof Holder holder)) return;
        Player viewer = (Player) e.getWhoClicked();
        Player target = Bukkit.getPlayer(holder.target());
        if (target == null || !target.isOnline()) { viewer.closeInventory(); return; }

        Inventory top = e.getInventory();
        int topSize = top.getSize();
        Mode mode = holder.mode();

        boolean touchesTop = e.getRawSlots().stream().anyMatch(s -> s < topSize);
        if (!touchesTop) return;

        for (int slot : e.getRawSlots()) {
            if (slot >= topSize) continue;
            if (isBlocker(top.getItem(slot)) || isLabel(top.getItem(slot))) { e.setCancelled(true); return; }
            if (mode == Mode.ENDER && slot > 26) { e.setCancelled(true); return; }
        }

        int placedTotal = 0;
        for (Map.Entry<Integer, ItemStack> en : e.getNewItems().entrySet()) {
            int slot = en.getKey();
            if (slot >= topSize) continue;
            ItemStack before = top.getItem(slot);
            ItemStack after = en.getValue();
            int b = (before == null || before.getType().isAir()) ? 0 : before.getAmount();
            int a = (after == null || after.getType().isAir()) ? 0 : after.getAmount();
            int delta = a - b;
            if (delta > 0) placedTotal += delta;
            top.setItem(slot, safeClone(after));
        }

        ItemStack old = e.getOldCursor();
        if (old != null && !old.getType().isAir()) {
            int newAmt = old.getAmount() - placedTotal;
            if (newAmt <= 0) viewer.setItemOnCursor(null);
            else {
                ItemStack c = old.clone();
                c.setAmount(newAmt);
                viewer.setItemOnCursor(c);
            }
        }

        e.setCancelled(true);
        Bukkit.getScheduler().runTask(Main.plugin, () -> sync(top, target, mode));
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof Holder)) return;
        SESSIONS.remove(e.getPlayer().getUniqueId());
        if (SESSIONS.isEmpty() && taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    // ----------- Handlers -----------

    private void pickup(InventoryClickEvent e, Inventory top, int slot, Player viewer) {
        ItemStack slotItem = top.getItem(slot);
        if (isEmpty(slotItem)) return;

        ItemStack cursor = viewer.getItemOnCursor();
        switch (e.getAction()) {
            case PICKUP_ALL -> {
                viewer.setItemOnCursor(slotItem.clone());
                top.setItem(slot, null);
            }
            case PICKUP_HALF -> {
                int half = (slotItem.getAmount() + 1) / 2;
                ItemStack toCursor = slotItem.clone(); toCursor.setAmount(half);
                viewer.setItemOnCursor(toCursor);
                slotItem.setAmount(slotItem.getAmount() - 1 * half);
                top.setItem(slot, slotItem.getAmount() <= 0 ? null : slotItem);
            }
            case PICKUP_ONE -> {
                if (isEmpty(cursor)) {
                    ItemStack one = slotItem.clone(); one.setAmount(1);
                    viewer.setItemOnCursor(one);
                    slotItem.setAmount(slotItem.getAmount() - 1);
                    top.setItem(slot, slotItem.getAmount() <= 0 ? null : slotItem);
                }
            }
            default -> {}
        }
    }

    private void place(InventoryClickEvent e, Inventory top, int slot, Player viewer) {
        ItemStack cursor = viewer.getItemOnCursor();
        if (isEmpty(cursor)) return;
        ItemStack inSlot = top.getItem(slot);
        int perMax = Math.min(cursor.getType().getMaxStackSize(), top.getMaxStackSize());

        switch (e.getAction()) {
            case PLACE_ALL -> {
                if (isEmpty(inSlot)) {
                    top.setItem(slot, cursor.clone());
                    viewer.setItemOnCursor(null);
                } else if (cursor.isSimilar(inSlot)) {
                    int canAdd = perMax - inSlot.getAmount();
                    int move = Math.min(canAdd, cursor.getAmount());
                    inSlot.setAmount(inSlot.getAmount() + move);
                    top.setItem(slot, inSlot);
                    int left = cursor.getAmount() - move;
                    viewer.setItemOnCursor(left <= 0 ? null : amount(cursor, left));
                }
            }
            case PLACE_ONE -> {
                if (isEmpty(inSlot)) {
                    ItemStack one = cursor.clone(); one.setAmount(1);
                    top.setItem(slot, one);
                    viewer.setItemOnCursor(amount(cursor, cursor.getAmount() - 1));
                } else if (cursor.isSimilar(inSlot) && inSlot.getAmount() < perMax) {
                    inSlot.setAmount(inSlot.getAmount() + 1);
                    top.setItem(slot, inSlot);
                    viewer.setItemOnCursor(amount(cursor, cursor.getAmount() - 1));
                }
            }
            default -> {}
        }
    }

    private void swap(InventoryClickEvent e, Inventory top, int slot, Player viewer) {
        ItemStack cursor = viewer.getItemOnCursor();
        ItemStack inSlot = top.getItem(slot);
        top.setItem(slot, isEmpty(cursor) ? null : cursor.clone());
        viewer.setItemOnCursor(isEmpty(inSlot) ? null : inSlot.clone());
    }

    private void shift(InventoryClickEvent e, Inventory top, int slot, Player viewer) {
        ItemStack item = top.getItem(slot);
        if (isEmpty(item)) return;
        Map<Integer, ItemStack> rem = viewer.getInventory().addItem(item.clone());
        if (rem.isEmpty()) top.setItem(slot, null);
        else top.setItem(slot, rem.values().iterator().next());
    }

    private void hotbar(InventoryClickEvent e, Inventory top, int slot, Player viewer) {
        int hotbar = e.getHotbarButton();
        if (hotbar < 0 || hotbar > 8) return;
        ItemStack hb = viewer.getInventory().getItem(hotbar);
        ItemStack inSlot = top.getItem(slot);
        top.setItem(slot, isEmpty(hb) ? null : hb.clone());
        viewer.getInventory().setItem(hotbar, isEmpty(inSlot) ? null : inSlot.clone());
    }

    private void collect(InventoryClickEvent e, Inventory top, Player viewer, Mode mode) {
        ItemStack cursor = viewer.getItemOnCursor();
        if (isEmpty(cursor)) return;
        int perMax = Math.min(cursor.getType().getMaxStackSize(), top.getMaxStackSize());
        int needed = perMax - cursor.getAmount();
        if (needed <= 0) return;

        for (int i = 0; i < top.getSize() && needed > 0; i++) {
            if (mode == Mode.ENDER && i > 26) break;
            if (i >= 41 && i <= 53) continue;
            ItemStack s = top.getItem(i);
            if (isEmpty(s) || !cursor.isSimilar(s)) continue;
            int move = Math.min(needed, s.getAmount());
            needed -= move;
            s.setAmount(s.getAmount() - move);
            top.setItem(i, s.getAmount() <= 0 ? null : s);
        }

        viewer.setItemOnCursor(amount(cursor, perMax - needed));
    }

    // ---------- Sync + Refresh ----------

    private static void sync(Inventory gui, Player target, Mode mode) {
        if (mode == Mode.INVENTORY) {
            PlayerInventory pinv = target.getInventory();
            for (int i = 0; i < 36; i++) pinv.setItem(i, safeClone(gui.getItem(i)));
            pinv.setHelmet(safeClone(gui.getItem(SLOT_HELM)));
            pinv.setChestplate(safeClone(gui.getItem(SLOT_CHEST)));
            pinv.setLeggings(safeClone(gui.getItem(SLOT_LEGS)));
            pinv.setBoots(safeClone(gui.getItem(SLOT_BOOTS)));
            pinv.setItemInOffHand(safeClone(gui.getItem(SLOT_OFFHAND)));
        } else {
            for (int i = 0; i < 27; i++) target.getEnderChest().setItem(i, safeClone(gui.getItem(i)));
        }
    }

    private static void startRefresherIfNeeded() {
        if (taskId != -1) return;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, () -> {
            if (SESSIONS.isEmpty()) return;

            for (Session s : SESSIONS.values().toArray(new Session[0])) {
                Player viewer = Bukkit.getPlayer(s.viewer);
                Player target = Bukkit.getPlayer(s.target);
                if (viewer == null || !viewer.isOnline() || target == null || !target.isOnline()) {
                    if (viewer != null) viewer.closeInventory();
                    SESSIONS.remove(s.viewer);
                    continue;
                }

                Inventory top = viewer.getOpenInventory().getTopInventory();
                if (!(top.getHolder() instanceof Holder holder)) { SESSIONS.remove(s.viewer); continue; }
                s.mode = holder.mode();

                if (s.mode == Mode.INVENTORY) {
                    PlayerInventory pinv = target.getInventory();
                    ItemStack[] store = pinv.getStorageContents();
                    for (int i = 0; i < 36; i++) updateIfDiff(top, i, store[i]);
                    updateIfDiff(top, SLOT_HELM, pinv.getHelmet());
                    updateIfDiff(top, SLOT_CHEST, pinv.getChestplate());
                    updateIfDiff(top, SLOT_LEGS, pinv.getLeggings());
                    updateIfDiff(top, SLOT_BOOTS, pinv.getBoots());
                    updateIfDiff(top, SLOT_OFFHAND, pinv.getItemInOffHand());
                } else {
                    ItemStack[] end = target.getEnderChest().getStorageContents();
                    for (int i = 0; i < 27; i++) updateIfDiff(top, i, end[i]);
                }
            }
        }, 10L, 10L);
    }

    private static void updateIfDiff(Inventory gui, int slot, ItemStack live) {
        if (!itemEquals(gui.getItem(slot), live)) gui.setItem(slot, safeClone(live));
    }

    // -------------- Utils --------------

    private static boolean isBlocker(ItemStack it) {
        return it != null && it.getType() == Material.BLACK_STAINED_GLASS_PANE;
    }

    private static boolean isLabel(ItemStack it) {
        if (it == null) return false;
        Material t = it.getType();
        return t == Material.GRAY_STAINED_GLASS_PANE
                || t == Material.LIGHT_GRAY_STAINED_GLASS_PANE
                || t == Material.PURPLE_STAINED_GLASS_PANE;
    }

    private static ItemStack safeClone(ItemStack it) {
        return it == null ? null : it.clone();
    }

    private static boolean isEmpty(ItemStack it) {
        return it == null || it.getType().isAir() || it.getAmount() <= 0;
    }

    private static ItemStack amount(ItemStack src, int amt) {
        if (amt <= 0) return null;
        ItemStack n = src.clone();
        n.setAmount(amt);
        return n;
    }

    private static boolean itemEquals(ItemStack a, ItemStack b) {
        boolean aEmpty = (a == null || a.getType().isAir());
        boolean bEmpty = (b == null || b.getType().isAir());
        if (aEmpty || bEmpty) return aEmpty == bEmpty;
        return a.isSimilar(b) && a.getAmount() == b.getAmount();
    }

    private static void setEquipment(PlayerInventory pinv, EquipmentSlot eq, ItemStack item) {
        switch (eq) {
            case HEAD -> pinv.setHelmet(item);
            case CHEST -> pinv.setChestplate(item);
            case LEGS -> pinv.setLeggings(item);
            case FEET -> pinv.setBoots(item);
            case OFF_HAND -> pinv.setItemInOffHand(item);
            default -> {}
        }
    }

    private record Holder(@NotNull UUID target, @NotNull Mode mode) implements InventoryHolder {
        @Override public @NotNull Inventory getInventory() { return Bukkit.createInventory(this, SIZE); }
    }
}
