package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class KitEditorGUI {
    private final PublicGoon plugin;
    private final KitLayoutGUI kitLayoutGUI;

    public KitEditorGUI(PublicGoon plugin) {
        this.plugin = plugin;
        this.kitLayoutGUI = new KitLayoutGUI(plugin);
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, "§e✎ Kit Editor - Select Mode");

        // Add decorative border matching MainInventoryGUI style
        ItemStack borderGlass = borderGlass();
        ItemStack fillerGlass = fillerGlass();
        
        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, borderGlass);
            } else {
                gui.setItem(i, fillerGlass);
            }
        }

        // Add gamemode options
        int slot = 10;
        for (GameModeConfig mode : GameModeConfig.values()) {
            if (!mode.enabled) continue;

            ItemStack item = new ItemStack(mode.icon);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§b§l" + mode.displayName);
                
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add("§7Rounds: §f" + mode.rounds);
                lore.add("§7Arena: §f" + mode.size.name());
                lore.add("");
                lore.add("§eClick to edit kit");
                meta.setLore(lore);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES, org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS, org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
                item.setItemMeta(meta);
            }

            gui.setItem(slot, item);
            slot++;
            if (slot % 9 == 8) slot += 2;
        }

        player.openInventory(gui);
    }

    private ItemStack borderGlass() {
        ItemStack pane = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
            pane.setItemMeta(meta);
        }
        return pane;
    }

    private ItemStack fillerGlass() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
            pane.setItemMeta(meta);
        }
        return pane;
    }

    public void handleInventoryClick(Player player, int slot, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        
        for (GameModeConfig mode : GameModeConfig.values()) {
            if (!mode.enabled) continue;
            if (item.getItemMeta().getDisplayName().equals("§b§l" + mode.displayName)) {
                kitLayoutGUI.open(player, mode);
                return;
            }
        }
    }
}
