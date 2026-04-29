package org.please.publicGoon;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpectateCommand implements CommandExecutor {
    private final PublicGoon plugin;
    private final DuelManager duelManager;
    private final Set<UUID> spectators = new HashSet<>();
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

        // Set spectator mode
        player.setGameMode(GameMode.SPECTATOR);
        spectators.add(player.getUniqueId());
        player.sendMessage("§aYou are now in spectator mode. Use /leave to return to lobby.");

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

    public void shutdown() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }
    }
}
