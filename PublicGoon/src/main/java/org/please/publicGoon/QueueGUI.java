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

public class QueueGUI {
    private final PublicGoon plugin;
    private final QueueManager queueManager;

    public QueueGUI(PublicGoon plugin, QueueManager queueManager) {
        this.plugin = plugin;
        this.queueManager = queueManager;
    }

    public void openQueueSelection(Player player, String gameMode) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6§lSelect Queue Type");

        // Create decorative glass panes
        ItemStack glassPane = createGlassPane();
        for (int i = 0; i < 27; i++) {
            if (i != 11 && i != 15) { // Leave slots 11 and 15 for main options
                gui.setItem(i, glassPane);
            }
        }

        // Ranked queue item
        gui.setItem(11, createRankedItem(gameMode));

        // Normal queue item
        gui.setItem(15, createNormalItem(gameMode));

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

    private ItemStack createRankedItem(String gameMode) {
        Material material = getGameModeMaterial(gameMode);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6§lRanked Queue");
            meta.setLore(Arrays.asList(
                "§7Competitive ranked matches",
                "§7ELO rating system",
                "§7Higher rewards and stakes",
                "",
                "§eClick to join ranked " + gameMode.toUpperCase() + " queue"
            ));
            
            // Add enchantment glow effect
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createNormalItem(String gameMode) {
        Material material = getGameModeMaterial(gameMode);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§a§lNormal Queue");
            meta.setLore(Arrays.asList(
                "§7Casual unranked matches",
                "§7No ELO requirements",
                "§7Practice and fun gameplay",
                "",
                "§eClick to join normal " + gameMode.toUpperCase() + " queue"
            ));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private Material getGameModeMaterial(String gameMode) {
        switch (gameMode.toLowerCase()) {
            case "axe":
                return Material.IRON_AXE;
            case "sword":
                return Material.IRON_SWORD;
            case "uhc":
                return Material.GOLDEN_APPLE;
            default:
                return Material.DIAMOND_SWORD;
        }
    }

    public static boolean isQueueGUI(Inventory inventory) {
        return inventory.getType().getDefaultTitle().equals("§6§lSelect Queue Type") && inventory.getSize() == 27;
    }

    public static boolean isRankedSlot(int slot) {
        return slot == 11;
    }

    public static boolean isNormalSlot(int slot) {
        return slot == 15;
    }
}
