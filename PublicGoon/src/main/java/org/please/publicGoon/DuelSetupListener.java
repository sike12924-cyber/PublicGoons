package org.please.publicGoon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class DuelSetupListener implements Listener {
    private final PublicGoon plugin;
    private final DuelGUI duelGUI;

    public DuelSetupListener(PublicGoon plugin, DuelGUI duelGUI) {
        this.plugin = plugin;
        this.duelGUI = duelGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        if (!title.startsWith("§eDuel Setup §8» §7")) return;

        e.setCancelled(true);
        player.playSound(player.getLocation(), Sound.BLOCK_BAMBOO_WOOD_BUTTON_CLICK_ON, 1f, 1f);

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        String name = item.getItemMeta().getDisplayName();

        switch (name) {
            case "§e§lGamemode":
                duelGUI.cycleGameMode(player.getUniqueId());
                duelGUI.refreshGUI(player);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                break;

            case "§e§lFirst To":
                duelGUI.cycleRounds(player.getUniqueId());
                duelGUI.refreshGUI(player);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                break;

            case "§e§lMap Size":
                duelGUI.cycleMapSize(player.getUniqueId());
                duelGUI.refreshGUI(player);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                break;

            case "§a§lSend Duel Request":
                handleSendRequest(player);
                break;

            case "§c§lCancel":
                player.closeInventory();
                duelGUI.clearSetupData(player.getUniqueId());
                player.sendMessage("§eDuel §8» §cCancelled duel request.");
                break;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player player = (Player) e.getPlayer();
        String title = e.getView().getTitle();

        if (title.startsWith("§eDuel Setup §8» §7")) {
            player.playSound(player.getLocation(), Sound.BLOCK_BAMBOO_WOOD_BUTTON_CLICK_OFF, 1f, 1f);
        }
    }

    private void handleSendRequest(Player player) {
        DuelGUI.DuelSetupData data = duelGUI.getSetupData(player.getUniqueId());
        if (data == null) {
            player.sendMessage("§cAn error occurred. Please try again.");
            return;
        }

        Player target = Bukkit.getPlayer(data.targetUuid);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cThat player is no longer online.");
            duelGUI.clearSetupData(player.getUniqueId());
            player.closeInventory();
            return;
        }

        // Check if target is still available
        if (plugin.getDuelManager().inDuel(target.getUniqueId())) {
            player.sendMessage("§cThat player is already in a duel.");
            duelGUI.clearSetupData(player.getUniqueId());
            player.closeInventory();
            return;
        }

        // Close the GUI and clear data
        player.closeInventory();
        duelGUI.clearSetupData(player.getUniqueId());

        // Store the duel request
        DuelRequest request = new DuelRequest(player.getUniqueId(), target.getUniqueId(), data.gameMode, data.rounds, data.mapSize);
        plugin.getDuelManager().addDuelRequest(request);

        // Send request message to sender
        player.sendMessage("§eDuel §8» §7You sent a duel request to §f" + target.getName() + "§7!");
        player.sendMessage("§e  Gamemode: §f" + data.gameMode.displayName);
        player.sendMessage("§e  First to: §f" + data.rounds + " rounds");
        player.sendMessage("§e  Map size: §f" + data.mapSize.name().charAt(0) + data.mapSize.name().substring(1).toLowerCase());

        // Send request message to target with clickable buttons
        target.sendMessage("§eDuel §8» §f" + player.getName() + " §7wants to duel you!");
        target.sendMessage("§e  Gamemode: §f" + data.gameMode.displayName);
        target.sendMessage("§e  First to: §f" + data.rounds + " rounds");
        target.sendMessage("§e  Map size: §f" + data.mapSize.name().charAt(0) + data.mapSize.name().substring(1).toLowerCase());

        // Send clickable accept/deny buttons
        Component acceptButton = Component.text(" §a[ACCEPT]")
                .hoverEvent(HoverEvent.showText(Component.text("§aClick to accept the duel")))
                .clickEvent(ClickEvent.runCommand("/duel accept " + player.getName()));

        Component denyButton = Component.text(" §c[DENY] ")
                .hoverEvent(HoverEvent.showText(Component.text("§cClick to deny the duel")))
                .clickEvent(ClickEvent.runCommand("/duel deny " + player.getName()));

        Component buttons = Component.text("§eDuel §8» ").append(acceptButton).append(denyButton);
        target.sendMessage(buttons);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
    }
}
