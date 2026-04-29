package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MainInventoryGUI {
    public static final String TITLE = "§9\u2694 Queue Duels";
    private static final int SIZE = 27;
    private static final int[] SLOTS = {10, 11, 12, 13, 19, 20, 21, 22};

    private final QueueManager queueManager;
    private final DuelManager duelManager;

    public MainInventoryGUI(QueueManager queueManager, DuelManager duelManager) {
        this.queueManager = queueManager;
        this.duelManager = duelManager;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, SIZE, TITLE);
        ItemStack glass = glass();
        for (int i = 0; i < SIZE; i++) gui.setItem(i, glass);

        GameModeConfig[] modes = GameModeConfig.values();
        for (int i = 0; i < SLOTS.length && i < modes.length; i++) {
            gui.setItem(SLOTS[i], createModeItem(modes[i]));
        }
        player.openInventory(gui);
    }

    private int countActiveFor(GameModeConfig mode) {
        int queued = queueManager != null ? queueManager.getQueueCount(mode) : 0;
        int dueling = duelManager != null ? duelManager.getActivePlayersInMode(mode) : 0;
        return queued + dueling;
    }

    private ItemStack createModeItem(GameModeConfig mode) {
        ItemStack item = new ItemStack(mode.icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§l" + mode.displayName);
            int active = countActiveFor(mode);
            String circle = (mode.enabled && active > 0) ? "§a\u25CF" : "§c\u25CF";
            List<String> lore = new ArrayList<>();
            lore.add(circle + " §f" + active + " active players §8(EU)");
            if (!mode.enabled) {
                lore.add("");
                lore.add("§c§lComing Soon");
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack glass() {
        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            pane.setItemMeta(meta);
        }
        return pane;
    }

    public static boolean isOurView(InventoryView view) {
        return view != null && TITLE.equals(view.getTitle());
    }

    public static GameModeConfig getModeAtSlot(int slot) {
        GameModeConfig[] modes = GameModeConfig.values();
        for (int i = 0; i < SLOTS.length; i++) {
            if (SLOTS[i] == slot) {
                return i < modes.length ? modes[i] : null;
            }
        }
        return null;
    }
}
