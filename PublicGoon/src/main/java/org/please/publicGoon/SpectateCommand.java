package org.please.publicGoon;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpectateCommand implements CommandExecutor, Listener {
    private final PublicGoon plugin;
    private final DuelManager duelManager;
    private final Set<UUID> spectators = new HashSet<>();
    private final Set<Location> playerPlacedBlocks = new HashSet<>();
    private BukkitTask actionBarTask;

    public SpectateCommand(PublicGoon plugin, DuelManager duelManager) {
        this.plugin = plugin;
        this.duelManager = duelManager;
        startActionBarTask();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        if (spectators.contains(player.getUniqueId())) {
            player.sendMessage("§cYou are already spectating. Use /leave to stop.");
            return true;
        }

        // Check if player is in a duel
        if (duelManager.inDuel(player.getUniqueId())) {
            player.sendMessage("§cYou cannot spectate while in a duel.");
            return true;
        }

        // Check if a target player is specified
        if (args.length < 1) {
            player.sendMessage("§cUsage: /spectate <player>");
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cPlayer not found.");
            return true;
        }

        // Check if trying to spectate yourself
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cYou Cannot Spectate Yourself.");
            return true;
        }

        // Check if target is in a duel
        if (!duelManager.inDuel(target.getUniqueId())) {
            player.sendMessage("§cThat player is not in a duel.");
            return true;
        }

        // Teleport 5 blocks above target first
        Location targetLoc = target.getLocation();
        Location teleportLoc = targetLoc.clone().add(0, 5, 0);
        player.teleport(teleportLoc);

        // Set spectator mode after teleport
        player.setGameMode(GameMode.SPECTATOR);
        spectators.add(player.getUniqueId());
        player.sendMessage("§aYou are now spectating §e" + target.getName() + "§a. Use /leave to return to lobby.");

        return true;
    }

    public void removeSpectator(Player player) {
        if (spectators.remove(player.getUniqueId())) {
            player.setGameMode(GameMode.ADVENTURE);
            player.sendActionBar("");
            duelManager.getLobbyManager().teleportToLobby(player);
        }
    }

    public boolean isSpectating(UUID uuid) {
        return spectators.contains(uuid);
    }

    private void startActionBarTask() {
        actionBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : spectators) {
                    Player player = plugin.getServer().getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        player.sendActionBar("§eSpectating - Use §f/leave §eto return to lobby");
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 40L);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        // Track blocks placed during duels (only in modes that allow building)
        Duel d = duelManager.getDuel(e.getPlayer().getUniqueId());
        if (d != null && d.allowsBreakPlace()) {
            playerPlacedBlocks.add(e.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onSpectatorMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (!spectators.contains(player.getUniqueId())) return;
        if (player.getGameMode() != GameMode.SPECTATOR) return;

        // Check if player is trying to move through a solid block
        Location to = e.getTo();
        if (to == null) return;

        Block block = to.getBlock();
        if (block.getType().isSolid() && !isPassable(block.getType())) {
            // Check if this is a player-placed block
            if (!playerPlacedBlocks.contains(block.getLocation())) {
                // Cancel movement through non-player-placed blocks
                e.setCancelled(true);
                // Push player back slightly
                Vector direction = to.toVector().subtract(e.getFrom().toVector()).normalize().multiply(-0.5);
                player.setVelocity(direction);
            }
        }
    }

    private boolean isPassable(Material material) {
        // Allow movement through these materials
        return material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR ||
               material == Material.WATER || material == Material.LAVA ||
               material.name().contains("GLASS") || material.name().contains("FENCE") ||
               material.name().contains("SLAB") || material.name().contains("STAIRS");
    }

    public void clearPlayerPlacedBlocks() {
        playerPlacedBlocks.clear();
    }

    public void shutdown() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }
    }
}
