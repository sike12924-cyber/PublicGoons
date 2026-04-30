package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KitLayoutGUI {
    private final PublicGoon plugin;
    /** Per-player mode currently being edited, so concurrent users don't clash. */
    private final Map<UUID, GameModeConfig> currentModes = new HashMap<>();

    public KitLayoutGUI(PublicGoon plugin) {
        this.plugin = plugin;
    }

    public GameModeConfig getCurrentMode(Player player) {
        return currentModes.get(player.getUniqueId());
    }

    public void clearMode(Player player) {
        currentModes.remove(player.getUniqueId());
    }

    public void open(Player player, GameModeConfig mode) {
        currentModes.put(player.getUniqueId(), mode);
        Inventory gui = Bukkit.createInventory(null, 54, "§e✎ Kit Editor - " + mode.displayName);

        // Add save button
        ItemStack saveButton = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta saveMeta = saveButton.getItemMeta();
        if (saveMeta != null) {
            saveMeta.setDisplayName("§aSave Kit");
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to save your kit");
            lore.add("§7for " + mode.displayName);
            saveMeta.setLore(lore);
            saveButton.setItemMeta(saveMeta);
        }
        gui.setItem(49, saveButton);

        // Load existing kit if available, otherwise load default kit
        KitConfig kitConfig = new KitConfig(plugin, player.getUniqueId(), mode);
        if (kitConfig.hasKit()) {
            // Create a temporary inventory to load the kit
            Inventory tempInv = Bukkit.createInventory(null, 54);
            kitConfig.applyKit(tempInv);
            
            // Copy to the GUI
            for (int i = 0; i < 36; i++) {
                gui.setItem(i, tempInv.getItem(i));
            }
            gui.setItem(39, tempInv.getItem(39)); // Helmet
            gui.setItem(38, tempInv.getItem(38)); // Chestplate
            gui.setItem(37, tempInv.getItem(37)); // Leggings
            gui.setItem(36, tempInv.getItem(36)); // Boots
        } else {
            // Load default kit for this gamemode
            loadDefaultKit(gui, mode);
        }

        player.openInventory(gui);
    }

    private void loadDefaultKit(Inventory gui, GameModeConfig mode) {
        if (mode == GameModeConfig.SWORD) {
            // Default sword kit
            ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
            sword.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 3);
            gui.setItem(0, sword);
            gui.setItem(39, armor(Material.DIAMOND_HELMET, 4));
            gui.setItem(38, armor(Material.DIAMOND_CHESTPLATE, 4));
            gui.setItem(37, armor(Material.DIAMOND_LEGGINGS, 3));
            gui.setItem(36, armor(Material.DIAMOND_BOOTS, 3));
        }
    }

    private ItemStack armor(Material m, int protLevel) {
        ItemStack i = new ItemStack(m);
        i.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, protLevel);
        i.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 3);
        return i;
    }

    private ItemStack createArmorIndicator(String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add("§7Place armor here");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleInventoryClick(Player player, int slot, ItemStack item) {
        if (slot != 49) return;
        GameModeConfig mode = currentModes.get(player.getUniqueId());
        if (mode == null) {
            player.closeInventory();
            return;
        }
        Inventory top = player.getOpenInventory().getTopInventory();
        // Snapshot the current top inventory into a transfer holder
        Inventory tempInv = Bukkit.createInventory(null, 54);
        for (int i = 0; i < 36; i++) tempInv.setItem(i, top.getItem(i));
        tempInv.setItem(36, top.getItem(36));
        tempInv.setItem(37, top.getItem(37));
        tempInv.setItem(38, top.getItem(38));
        tempInv.setItem(39, top.getItem(39));

        KitConfig kitConfig = new KitConfig(plugin, player.getUniqueId(), mode);
        kitConfig.saveKit(tempInv);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.4f);
        player.sendActionBar("§aSuccessfully saved kit for " + mode.displayName);
        clearMode(player);
        player.closeInventory();
    }
}
