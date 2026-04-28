package org.please.publicGoon;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class LobbyListener implements Listener {
    private final PublicGoon plugin;
    private final LobbyManager lobbyManager;
    private final InventorySwords inventorySwords;
    private final QueueManager queueManager;

    public LobbyListener(PublicGoon plugin, LobbyManager lobbyManager, InventorySwords inventorySwords, QueueManager queueManager) {
        this.plugin = plugin;
        this.lobbyManager = lobbyManager;
        this.inventorySwords = inventorySwords;
        this.queueManager = queueManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Always send player to the lobby on join
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                if (lobbyManager.hasLobby()) {
                    lobbyManager.teleportToLobby(player);
                }
                applyLobbyState(player);
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (lobbyManager.hasLobby()) {
            event.setRespawnLocation(lobbyManager.getLobbyLocation());
        }
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                applyLobbyState(player);
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        applyLobbyState(event.getPlayer());
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (lobbyManager.isInLobby(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        // Block lobby/queue items from being placed in offhand
        if (inventorySwords.isQueueSword(event.getMainHandItem())
                || inventorySwords.isQueueSword(event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }

    private void applyLobbyState(Player player) {
        if (lobbyManager.isInLobby(player)) {
            player.setGameMode(GameMode.ADVENTURE);
            player.setFoodLevel(20);
            player.setSaturation(20f);
            inventorySwords.giveSwordsToPlayer(player);
        } else {
            // Outside lobby: remove queue swords / clear offhand if it has them
            inventorySwords.removeSwordsFromPlayer(player);
            // Ensure they don't keep the queue (no arena yet, but stay clean)
            queueManager.removeFromQueue(player);
        }
    }
}
