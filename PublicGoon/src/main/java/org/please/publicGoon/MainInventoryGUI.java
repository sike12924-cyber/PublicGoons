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

public class MainInventoryGUI {
    private final PublicGoon plugin;
    private final NormalGameModesGUI normalGUI;
    private final RankedGameModesGUI rankedGUI;

    public MainInventoryGUI(PublicGoon plugin, NormalGameModesGUI normalGUI, RankedGameModesGUI rankedGUI) {
        this.plugin = plugin;
        this.normalGUI = normalGUI;
        this.rankedGUI = rankedGUI;
    }

    public void openMainInventory(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "§6§lPvP Queue System");

        // Fill with glass panes
        ItemStack glassPane = createGlassPane();
        for (int i = 0; i < 9; i++) {
            if (i != 0 && i != 1) { // Leave slots 0 and 1 for swords
                gui.setItem(i, glassPane);
            }
        }

        // Iron Sword (Normal Game Modes) - Slot 0
        gui.setItem(0, createIronSword());

        // Diamond Sword (Ranked Game Modes) - Slot 1
        gui.setItem(1, createDiamondSword());

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

    private ItemStack createIronSword() {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = sword.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§a§lNormal Game Modes");
            meta.setLore(Arrays.asList(
                "§7Casual unranked matches",
                "§7No ELO requirements",
                "§7Practice and fun gameplay",
                "",
                "§eClick to browse normal game modes"
            ));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            sword.setItemMeta(meta);
        }
        
        return sword;
    }

    private ItemStack createDiamondSword() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6§lRanked Game Modes");
            meta.setLore(Arrays.asList(
                "§7Competitive ranked matches",
                "§7ELO rating system",
                "§7Higher rewards and stakes",
                "",
                "§eClick to browse ranked game modes"
            ));
            
            // Add enchantment glow effect
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            sword.setItemMeta(meta);
        }
        
        return sword;
    }

    public static boolean isMainInventory(Inventory inventory) {
        return inventory.getType().getDefaultTitle().equals("§6§lPvP Queue System") && inventory.getSize() == 9;
    }

    public static boolean isIronSwordSlot(int slot) {
        return slot == 0;
    }

    public static boolean isDiamondSwordSlot(int slot) {
        return slot == 1;
    }

    public NormalGameModesGUI getNormalGUI() {
        return normalGUI;
    }

    public RankedGameModesGUI getRankedGUI() {
        return rankedGUI;
    }
}
