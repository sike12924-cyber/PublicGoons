package org.please.publicGoon;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ProfileGUI {
    private final PublicGoon plugin;

    public ProfileGUI(PublicGoon plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer, OfflinePlayer target) {
        Inventory gui = Bukkit.createInventory(null, 27, "§e" + target.getName() + "'s Profile");

        // Fill with black glass panes as background
        ItemStack filler = createFiller();
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, filler);
        }

        // Player head in center (slot 13) - only item shown
        ItemStack head = createPlayerHead(target);
        gui.setItem(13, head);

        viewer.openInventory(gui);
    }

    private ItemStack createFiller() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createPlayerHead(OfflinePlayer target) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(target);

            // Online/offline status with colored dot
            Player onlineTarget = target.getPlayer();
            String statusColor = (onlineTarget != null && onlineTarget.isOnline()) ? "§a" : "§7";
            String statusSymbol = "●";

            meta.setDisplayName(statusColor + statusSymbol + " §f" + target.getName());

            List<String> lore = new ArrayList<>();
            lore.add("");

            // Use PlaceholderAPI placeholders (LuckPerms for rank)
            String rank = getPlaceholder(target, "%luckperms_prefix%");
            String playtime = getPlaceholder(target, "%statistic_time_played%");
            String kills = getPlaceholder(target, "%statistic_player_kills%");
            String deaths = getPlaceholder(target, "%statistic_deaths%");
            String kd = getPlaceholder(target, "%kd_ratio%");
            String coins = getPlaceholder(target, "%balance%");

            lore.add("§7Rank: §f" + (rank.isEmpty() ? "Member" : rank));
            lore.add("§7Playtime: §f" + (playtime.isEmpty() ? "0d, 0hrs, 0m" : playtime));
            lore.add("");
            lore.add("§7Kills: §f" + (kills.isEmpty() ? "0" : kills));
            lore.add("§7Deaths: §f" + (deaths.isEmpty() ? "0" : deaths));
            lore.add("§7K/D: §f" + (kd.isEmpty() ? "0.00" : kd));
            lore.add("");
            lore.add("§7Coins: §6" + (coins.isEmpty() ? "0" : coins));

            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }

    private String getPlaceholder(OfflinePlayer player, String placeholder) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled()) {
            return PlaceholderAPI.setPlaceholders(player, placeholder);
        }
        return "";
    }
}
