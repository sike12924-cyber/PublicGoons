package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class QueueManager {
    private final PublicGoon plugin;
    private DuelManager duelManager; // injected post-construction
    private final Map<UUID, QueueEntry> playerQueues = new HashMap<>();
    private final Map<GameModeConfig, Deque<UUID>> queues = new EnumMap<>(GameModeConfig.class);

    public QueueManager(PublicGoon plugin) {
        this.plugin = plugin;
        for (GameModeConfig g : GameModeConfig.values()) queues.put(g, new ArrayDeque<>());
        startActionBarUpdater();
        startMatchmakingTask();
    }

    public void setDuelManager(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    public boolean addToQueue(Player player, GameModeConfig mode) {
        if (mode == null) return false;
        if (!mode.enabled) {
            player.sendMessage("§c§l» §7" + mode.displayName + " is coming soon.");
            return false;
        }
        UUID id = player.getUniqueId();
        if (playerQueues.containsKey(id)) {
            player.sendMessage("§cYou are already in a queue. Use §f/leave§c to exit.");
            return false;
        }
        if (duelManager != null && duelManager.inDuel(id)) {
            player.sendMessage("§cYou are already in a duel.");
            return false;
        }
        QueueEntry entry = new QueueEntry(id, mode, System.currentTimeMillis());
        playerQueues.put(id, entry);
        queues.get(mode).addLast(id);
        player.sendMessage("§a§l» §7Joined §f" + mode.displayName + " §7queue. §8(§f" + queues.get(mode).size() + " §7waiting§8)");
        return true;
    }

    public boolean removeFromQueue(UUID playerId, boolean notify) {
        QueueEntry entry = playerQueues.remove(playerId);
        if (entry == null) return false;
        Deque<UUID> q = queues.get(entry.mode);
        if (q != null) q.remove(playerId);
        if (notify) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null && p.isOnline()) {
                p.sendActionBar("");
                p.sendMessage("§aSuccessfully left all queue modes.");
            }
        }
        return true;
    }

    public QueueEntry getPlayerQueue(UUID playerId) {
        return playerQueues.get(playerId);
    }

    public int getQueueCount(GameModeConfig mode) {
        Deque<UUID> q = queues.get(mode);
        return q == null ? 0 : q.size();
    }

    // Legacy helper kept for older GUI files (accepts string name)
    public int getQueueCount(String gameMode) {
        GameModeConfig mode = GameModeConfig.fromName(gameMode);
        return mode == null ? 0 : getQueueCount(mode);
    }

    private void startMatchmakingTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<GameModeConfig, Deque<UUID>> e : queues.entrySet()) {
                    Deque<UUID> q = e.getValue();
                    // Drop offline players
                    Iterator<UUID> it = q.iterator();
                    while (it.hasNext()) {
                        UUID id = it.next();
                        Player p = Bukkit.getPlayer(id);
                        if (p == null || !p.isOnline()) {
                            it.remove();
                            playerQueues.remove(id);
                        }
                    }
                    while (q.size() >= 2 && duelManager != null) {
                        UUID aId = q.pollFirst();
                        UUID bId = q.pollFirst();
                        if (aId == null || bId == null) break;
                        playerQueues.remove(aId);
                        playerQueues.remove(bId);
                        Player a = Bukkit.getPlayer(aId);
                        Player b = Bukkit.getPlayer(bId);
                        if (a == null || b == null) continue;
                        duelManager.startMatch(a, b, e.getKey());
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void startActionBarUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, QueueEntry> e : playerQueues.entrySet()) {
                    Player player = Bukkit.getPlayer(e.getKey());
                    if (player != null && player.isOnline()) {
                        sendQueueActionBar(player, e.getValue());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void sendQueueActionBar(Player player, QueueEntry entry) {
        long secs = (System.currentTimeMillis() - entry.joinTime) / 1000;
        player.sendActionBar("§7\u231A Queued for 1 mode.. §f" + secs + "s §7- Use §f/leave §7to exit queue");
    }

    public static class QueueEntry {
        public final UUID playerId;
        public final GameModeConfig mode;
        public final long joinTime;

        public QueueEntry(UUID playerId, GameModeConfig mode, long joinTime) {
            this.playerId = playerId;
            this.mode = mode;
            this.joinTime = joinTime;
        }
    }
}
