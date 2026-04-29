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
    public static final String TITLE = "§9§l⚔ Queue Duels";
    private static final int SIZE = 45;
    private static final int[] SLOTS = {11, 12, 13, 14, 20, 21, 22, 23};

    private final QueueManager queueManager;
    private final DuelManager duelManager;

    public MainInventoryGUI(QueueManager queueManager, DuelManager duelManager) {
        this.queueManager = queueManager;
        this.duelManager = duelManager;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, SIZE, TITLE);
        
        // Fill with decorative glass
        ItemStack borderGlass = borderGlass();
        ItemStack fillerGlass = fillerGlass();
        
        for (int i = 0; i < SIZE; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, borderGlass);
            } else {
                gui.setItem(i, fillerGlass);
            }
        }

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
            String circle = (mode.enabled && active > 0) ? "§a●" : "§c●";
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Players: " + circle + " §f" + active + " §8(EU)");
            lore.add("§7Rounds: §f" + mode.rounds);
            lore.add("§7Arena: §f" + mode.size.name());
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

    private ItemStack borderGlass() {
        ItemStack pane = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            pane.setItemMeta(meta);
        }
        return pane;
    }

    private ItemStack fillerGlass() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
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
