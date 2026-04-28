package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class NormalGameModesGUI {
    private final QueueManager queueManager;

    public NormalGameModesGUI(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    public void openNormalGameModes(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§a§lNormal Game Modes");

        // Fill with glass panes
        ItemStack glassPane = createGlassPane();
        for (int i = 0; i < 27; i++) {
            if (i != 11 && i != 13 && i != 15) { // Leave slots for game modes
                gui.setItem(i, glassPane);
            }
        }

        // Axe - Slot 11
        gui.setItem(11, createAxeItem());

        // UHC - Slot 13
        gui.setItem(13, createUHCItem());

        // Sword - Slot 15
        gui.setItem(15, createSwordItem());

        player.openInventory(gui);
    }

    private ItemStack createGlassPane() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            pane.setItemMeta(meta);
        }
        return pane;
    }

    private ItemStack createAxeItem() {
        ItemStack item = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§a§lNormal Axe PvP");
            meta.setLore(Arrays.asList(
                "§7Casual axe combat matches",
                "§7No ELO requirements",
                "§7Practice axe PvP skills",
                "",
                "§ePlayers in queue: §f" + queueManager.getQueueCount("axe"),
                "",
                "§aClick to join Normal Axe queue"
            ));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createUHCItem() {
        ItemStack item = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§a§lNormal UHC PvP");
            meta.setLore(Arrays.asList(
                "§7Casual UHC combat matches",
                "§7No ELO requirements",
                "§7Practice UHC PvP skills",
                "",
                "§ePlayers in queue: §f" + queueManager.getQueueCount("uhc"),
                "",
                "§aClick to join Normal UHC queue"
            ));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createSwordItem() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§a§lNormal Sword PvP");
            meta.setLore(Arrays.asList(
                "§7Casual sword combat matches",
                "§7No ELO requirements",
                "§7Practice sword PvP skills",
                "",
                "§ePlayers in queue: §f" + queueManager.getQueueCount("sword"),
                "",
                "§aClick to join Normal Sword queue"
            ));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    public static boolean isNormalGameModesGUI(Inventory inventory) {
        return inventory.getType().getDefaultTitle().equals("§a§lNormal Game Modes") && inventory.getSize() == 27;
    }

    public static boolean isAxeSlot(int slot) {
        return slot == 11;
    }

    public static boolean isUHCSlot(int slot) {
        return slot == 13;
    }

    public static boolean isSwordSlot(int slot) {
        return slot == 15;
    }

    public static String getGameModeFromSlot(int slot) {
        if (isAxeSlot(slot)) return "axe";
        if (isUHCSlot(slot)) return "uhc";
        if (isSwordSlot(slot)) return "sword";
        return null;
    }
}
