package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class RankedGameModesGUI {
    private final QueueManager queueManager;

    public RankedGameModesGUI(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    public void openRankedGameModes(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6§lRanked Game Modes");

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
            meta.setDisplayName("§6§lRanked Axe PvP");
            meta.setLore(Arrays.asList(
                "§7Competitive axe combat matches",
                "§7ELO rating system",
                "§7Higher rewards and stakes",
                "",
                "§ePlayers in queue: §f" + queueManager.getQueueCount("axe"),
                "",
                "§aClick to join Ranked Axe queue"
            ));
            
            // Add enchantment glow effect
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createUHCItem() {
        ItemStack item = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6§lRanked UHC PvP");
            meta.setLore(Arrays.asList(
                "§7Competitive UHC combat matches",
                "§7ELO rating system",
                "§7Higher rewards and stakes",
                "",
                "§ePlayers in queue: §f" + queueManager.getQueueCount("uhc"),
                "",
                "§aClick to join Ranked UHC queue"
            ));
            
            // Add enchantment glow effect
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createSwordItem() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6§lRanked Sword PvP");
            meta.setLore(Arrays.asList(
                "§7Competitive sword combat matches",
                "§7ELO rating system",
                "§7Higher rewards and stakes",
                "",
                "§ePlayers in queue: §f" + queueManager.getQueueCount("sword"),
                "",
                "§aClick to join Ranked Sword queue"
            ));
            
            // Add enchantment glow effect
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    public static boolean isRankedGameModesGUI(Inventory inventory) {
        return inventory.getType().getDefaultTitle().equals("§6§lRanked Game Modes") && inventory.getSize() == 27;
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
