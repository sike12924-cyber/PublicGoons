package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.Base64;

public class KitConfig {
    private final PublicGoon plugin;
    private final UUID playerId;
    private final GameModeConfig mode;
    private final File configFile;
    private FileConfiguration config;

    public KitConfig(PublicGoon plugin, UUID playerId, GameModeConfig mode) {
        this.plugin = plugin;
        this.playerId = playerId;
        this.mode = mode;
        this.configFile = new File(plugin.getDataFolder(), "kits/" + playerId + "/" + mode.name() + ".yml");
        loadConfig();
    }

    private void loadConfig() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveKit(Inventory inventory) {
        config.set("helmet", serializeItem(inventory.getItem(39)));
        config.set("chestplate", serializeItem(inventory.getItem(38)));
        config.set("leggings", serializeItem(inventory.getItem(37)));
        config.set("boots", serializeItem(inventory.getItem(36)));
        
        for (int i = 0; i < 36; i++) {
            config.set("slot_" + i, serializeItem(inventory.getItem(i)));
        }
        
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void applyKit(PlayerInventory inventory) {
        inventory.setHelmet(deserializeItem(config.getString("helmet")));
        inventory.setChestplate(deserializeItem(config.getString("chestplate")));
        inventory.setLeggings(deserializeItem(config.getString("leggings")));
        inventory.setBoots(deserializeItem(config.getString("boots")));
        
        for (int i = 0; i < 36; i++) {
            inventory.setItem(i, deserializeItem(config.getString("slot_" + i)));
        }
    }

    public void applyKit(Inventory inventory) {
        inventory.setItem(39, deserializeItem(config.getString("helmet")));
        inventory.setItem(38, deserializeItem(config.getString("chestplate")));
        inventory.setItem(37, deserializeItem(config.getString("leggings")));
        inventory.setItem(36, deserializeItem(config.getString("boots")));
        
        for (int i = 0; i < 36; i++) {
            inventory.setItem(i, deserializeItem(config.getString("slot_" + i)));
        }
    }

    public boolean hasKit() {
        return config.contains("helmet") || config.contains("slot_0");
    }

    private String serializeItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.set("item", item);
            return yaml.saveToString();
        } catch (Exception e) {
            return null;
        }
    }

    private ItemStack deserializeItem(String data) {
        if (data == null) return null;
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString(data);
            return yaml.getItemStack("item");
        } catch (Exception e) {
            return null;
        }
    }
}
