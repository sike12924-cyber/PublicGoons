package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DuelGUI {
    private final Map<UUID, DuelSetupData> setupData = new HashMap<>();

    public void openDuelSetup(Player player, Player target) {
        // Initialize or reset setup data for this player
        DuelSetupData data = new DuelSetupData(target.getUniqueId());
        setupData.put(player.getUniqueId(), data);

        Inventory gui = Bukkit.createInventory(null, 27, "§eDuel Setup §8» §7" + target.getName());

        // Fill with gray glass
        ItemStack filler = createFiller();
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, filler);
        }

        // Gamemode section
        gui.setItem(10, createGameModeSelector(data.gameMode));

        // Rounds section (First to)
        gui.setItem(12, createRoundsSelector(data.rounds));

        // Map size section
        gui.setItem(14, createMapSizeSelector(data.mapSize));

        // Send Request button
        gui.setItem(16, createSendButton());

        // Cancel button
        gui.setItem(22, createCancelButton());

        player.openInventory(gui);
    }

    public DuelSetupData getSetupData(UUID playerUuid) {
        return setupData.get(playerUuid);
    }

    public void clearSetupData(UUID playerUuid) {
        setupData.remove(playerUuid);
    }

    public void cycleGameMode(UUID playerUuid) {
        DuelSetupData data = setupData.get(playerUuid);
        if (data != null) {
            GameModeConfig[] modes = GameModeConfig.values();
            int currentIndex = data.gameMode.ordinal();
            int nextIndex = (currentIndex + 1) % modes.length;
            // Skip disabled modes
            while (!modes[nextIndex].enabled) {
                nextIndex = (nextIndex + 1) % modes.length;
            }
            data.gameMode = modes[nextIndex];
            // Update map size to match gamemode default if needed
            data.mapSize = data.gameMode.size;
        }
    }

    public void cycleRounds(UUID playerUuid) {
        DuelSetupData data = setupData.get(playerUuid);
        if (data != null) {
            int[] roundOptions = {1, 2, 3, 5, 10};
            int currentIndex = -1;
            for (int i = 0; i < roundOptions.length; i++) {
                if (roundOptions[i] == data.rounds) {
                    currentIndex = i;
                    break;
                }
            }
            int nextIndex = (currentIndex + 1) % roundOptions.length;
            data.rounds = roundOptions[nextIndex];
        }
    }

    public void cycleMapSize(UUID playerUuid) {
        DuelSetupData data = setupData.get(playerUuid);
        if (data != null) {
            ArenaSize[] sizes = ArenaSize.values();
            int currentIndex = data.mapSize.ordinal();
            int nextIndex = (currentIndex + 1) % sizes.length;
            data.mapSize = sizes[nextIndex];
        }
    }

    private ItemStack createFiller() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGameModeSelector(GameModeConfig mode) {
        ItemStack item = new ItemStack(mode.icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§lGamemode");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Current: §f" + mode.displayName);
            lore.add("");
            lore.add("§eClick to change");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createRoundsSelector(int rounds) {
        ItemStack item = new ItemStack(Material.TARGET);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§lFirst To");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Rounds to win: §f" + rounds);
            lore.add("");
            lore.add("§eClick to change");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createMapSizeSelector(ArenaSize size) {
        Material material;
        switch (size) {
            case SMALL:
                material = Material.OAK_SAPLING;
                break;
            case AVERAGE:
                material = Material.OAK_LOG;
                break;
            case LARGE:
                material = Material.DARK_OAK_LOG;
                break;
            default:
                material = Material.OAK_LOG;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§lMap Size");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Size: §f" + size.name().charAt(0) + size.name().substring(1).toLowerCase());
            lore.add("");
            lore.add("§eClick to change");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createSendButton() {
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a§lSend Duel Request");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Click to send the request");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCancelButton() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lCancel");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Click to cancel");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void refreshGUI(Player player) {
        DuelSetupData data = setupData.get(player.getUniqueId());
        if (data == null) return;

        Inventory gui = player.getOpenInventory().getTopInventory();
        if (gui == null) return;

        gui.setItem(10, createGameModeSelector(data.gameMode));
        gui.setItem(12, createRoundsSelector(data.rounds));
        gui.setItem(14, createMapSizeSelector(data.mapSize));
    }

    public static class DuelSetupData {
        public UUID targetUuid;
        public GameModeConfig gameMode;
        public int rounds;
        public ArenaSize mapSize;

        public DuelSetupData(UUID targetUuid) {
            this.targetUuid = targetUuid;
            this.gameMode = GameModeConfig.SWORD; // Default
            this.rounds = 2; // Default
            this.mapSize = gameMode.size; // Default from gamemode
        }
    }
}
