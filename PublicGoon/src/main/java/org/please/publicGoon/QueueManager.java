package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QueueManager {
    private final PublicGoon plugin;
    private final Map<UUID, QueueEntry> playerQueues = new HashMap<>();
    private final Map<String, Integer> queueCounts = new HashMap<>();

    public QueueManager(PublicGoon plugin) {
        this.plugin = plugin;
        initializeQueueCounts();
        startActionBarUpdater();
    }

    private void initializeQueueCounts() {
        queueCounts.put("axe", 0);
        queueCounts.put("uhc", 0);
        queueCounts.put("sword", 0);
    }

    public boolean addToQueue(Player player, String gameMode, boolean ranked) {
        UUID playerId = player.getUniqueId();
        
        if (playerQueues.containsKey(playerId)) {
            player.sendMessage("§cYou are already in a queue!");
            return false;
        }

        if (!isValidGameMode(gameMode)) {
            player.sendMessage("§cInvalid game mode! Use: axe, uhc, or sword");
            return false;
        }

        QueueEntry entry = new QueueEntry(playerId, gameMode, ranked, System.currentTimeMillis());
        playerQueues.put(playerId, entry);
        queueCounts.put(gameMode, queueCounts.get(gameMode) + 1);

        String queueType = ranked ? "Ranked" : "Normal";
        player.sendMessage("§a§l» §7You have joined the " + queueType + " §f" + gameMode.toUpperCase() + " §7queue!");
        player.sendMessage("§7Position in queue: §f" + queueCounts.get(gameMode));

        return true;
    }

    public boolean removeFromQueue(Player player) {
        UUID playerId = player.getUniqueId();
        QueueEntry entry = playerQueues.remove(playerId);
        
        if (entry != null) {
            queueCounts.put(entry.gameMode, queueCounts.get(entry.gameMode) - 1);
            player.sendMessage("§c§l» §7You have left the queue!");
            return true;
        }
        
        return false;
    }

    public QueueEntry getPlayerQueue(UUID playerId) {
        return playerQueues.get(playerId);
    }

    public int getQueueCount(String gameMode) {
        return queueCounts.getOrDefault(gameMode, 0);
    }

    private boolean isValidGameMode(String gameMode) {
        return gameMode.equalsIgnoreCase("axe") || 
               gameMode.equalsIgnoreCase("uhc") || 
               gameMode.equalsIgnoreCase("sword");
    }

    private void startActionBarUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID playerId : playerQueues.keySet()) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        QueueEntry entry = playerQueues.get(playerId);
                        if (entry != null) {
                            sendQueueActionBar(player, entry);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Update every second
    }

    private void sendQueueActionBar(Player player, QueueEntry entry) {
        long timeInQueue = (System.currentTimeMillis() - entry.joinTime) / 1000;
        String queueType = entry.ranked ? "§6Ranked" : "§aNormal";
        String gameMode = entry.gameMode.substring(0, 1).toUpperCase() + entry.gameMode.substring(1);
        
        String actionBar = String.format(
            "§dɪɴ ǫᴜᴇᴜᴇ ꜰᴏʀ §f%s §7- %s §7(%ds) §7- §f%d §7players",
            gameMode, queueType, timeInQueue, queueCounts.get(entry.gameMode)
        );
        
        player.sendActionBar(actionBar);
    }

    public static class QueueEntry {
        public final UUID playerId;
        public final String gameMode;
        public final boolean ranked;
        public final long joinTime;

        public QueueEntry(UUID playerId, String gameMode, boolean ranked, long joinTime) {
            this.playerId = playerId;
            this.gameMode = gameMode;
            this.ranked = ranked;
            this.joinTime = joinTime;
        }
    }
}
