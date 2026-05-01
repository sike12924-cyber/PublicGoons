package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class WorldSpawnProtectionListener implements Listener {
    private final LobbyManager lobbyManager;
    private static final int MIN_Y_LEVEL = 114;

    public WorldSpawnProtectionListener(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if the player actually moved to a new block (to avoid excessive checks)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        World world = player.getWorld();
        
        // Check if player is in the world spawn (not in lobby)
        if (isInWorldSpawn(world) && !lobbyManager.isInLobby(player)) {
            // Check if player is below Y level 114
            if (event.getTo().getY() < MIN_Y_LEVEL) {
                // Teleport player back to lobby
                lobbyManager.teleportToLobby(player);
                player.sendMessage("§cYou went below Y level " + MIN_Y_LEVEL + " and were teleported back to the lobby!");
            }
        }
    }

    /**
     * Checks if the given world is the main world spawn (first world in the list)
     */
    private boolean isInWorldSpawn(World world) {
        if (world == null) return false;
        
        // Get the main world (usually the first world loaded)
        World mainWorld = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        if (mainWorld == null) return false;
        
        return world.equals(mainWorld);
    }
}
