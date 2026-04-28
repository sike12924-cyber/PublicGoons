package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class QueueManager {
    private final PublicGoon plugin;
    private final Map<UUID, QueueEntry> playerQueues = new HashMap<>();
    // queueKey -> ordered player ids (FIFO)
    private final Map<String, Deque<UUID>> queues = new LinkedHashMap<>();

    public QueueManager(PublicGoon plugin) {
        this.plugin = plugin;
        startActionBarUpdater();
        startMatchmakingTask();
    }

    private static String key(String gameMode, boolean ranked) {
        return (ranked ? "r:" : "n:") + gameMode.toLowerCase();
    }

    public boolean addToQueue(Player player, String gameMode, boolean ranked) {
        UUID playerId = player.getUniqueId();

        if (playerQueues.containsKey(playerId)) {
            player.sendMessage("§cYou are already in a queue! Use /queue leave to exit.");
            return false;
        }

        if (!isValidGameMode(gameMode)) {
            player.sendMessage("§cInvalid game mode!");
            return false;
        }

        QueueEntry entry = new QueueEntry(playerId, gameMode.toLowerCase(), ranked, System.currentTimeMillis());
        playerQueues.put(playerId, entry);
        queues.computeIfAbsent(key(entry.gameMode, ranked), k -> new ArrayDeque<>()).addLast(playerId);

        String queueType = ranked ? "§6Ranked" : "§aNormal";
        player.sendMessage("§a§l» §7Joined " + queueType + " §f" + gameMode.toUpperCase() + " §7queue. §7(§f" + getQueueSize(entry.gameMode, ranked) + " §7waiting)");
        return true;
    }

    public boolean removeFromQueue(Player player) {
        return removeFromQueue(player.getUniqueId(), true);
    }

    public boolean removeFromQueue(UUID playerId, boolean notify) {
        QueueEntry entry = playerQueues.remove(playerId);
        if (entry == null) return false;

        Deque<UUID> q = queues.get(key(entry.gameMode, entry.ranked));
        if (q != null) q.remove(playerId);

        if (notify) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null && p.isOnline()) {
                p.sendMessage("§c§l» §7You left the queue.");
            }
        }
        return true;
    }

    public QueueEntry getPlayerQueue(UUID playerId) {
        return playerQueues.get(playerId);
    }

    public int getQueueCount(String gameMode) {
        return getQueueSize(gameMode, false) + getQueueSize(gameMode, true);
    }

    public int getQueueSize(String gameMode, boolean ranked) {
        Deque<UUID> q = queues.get(key(gameMode, ranked));
        return q == null ? 0 : q.size();
    }

    private boolean isValidGameMode(String gameMode) {
        return gameMode.equalsIgnoreCase("axe")
                || gameMode.equalsIgnoreCase("uhc")
                || gameMode.equalsIgnoreCase("sword");
    }

    private void startMatchmakingTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<String, Deque<UUID>> e : queues.entrySet()) {
                    Deque<UUID> q = e.getValue();
                    // Drop offline players first
                    Iterator<UUID> it = q.iterator();
                    while (it.hasNext()) {
                        UUID id = it.next();
                        Player p = Bukkit.getPlayer(id);
                        if (p == null || !p.isOnline()) {
                            it.remove();
                            playerQueues.remove(id);
                        }
                    }
                    while (q.size() >= 2) {
                        UUID a = q.pollFirst();
                        UUID b = q.pollFirst();
                        if (a == null || b == null) break;
                        QueueEntry ea = playerQueues.remove(a);
                        QueueEntry eb = playerQueues.remove(b);
                        Player pa = Bukkit.getPlayer(a);
                        Player pb = Bukkit.getPlayer(b);
                        if (ea == null || eb == null || pa == null || pb == null) {
                            // Re-queue any survivors
                            if (pa != null && ea != null) { playerQueues.put(a, ea); q.addFirst(a); }
                            if (pb != null && eb != null) { playerQueues.put(b, eb); q.addFirst(b); }
                            break;
                        }
                        announceMatch(pa, pb, ea);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void announceMatch(Player a, Player b, QueueEntry entry) {
        String mode = entry.gameMode.substring(0, 1).toUpperCase() + entry.gameMode.substring(1);
        String type = entry.ranked ? "§6Ranked" : "§aNormal";
        String header = "§8§m                    §r §d§lMATCH FOUND §8§m                    ";
        for (Player p : new Player[]{a, b}) {
            Player opp = (p == a) ? b : a;
            p.sendMessage("");
            p.sendMessage(header);
            p.sendMessage("§7Mode: " + type + " §f" + mode);
            p.sendMessage("§7Opponent: §f" + opp.getName());
            p.sendMessage("§eArenas are not yet available - you have been removed from the queue.");
            p.sendMessage(header);
            p.sendMessage("");
            p.sendActionBar("§d§lMatch found vs §f" + opp.getName());
            p.playSound(p.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
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
        String type = entry.ranked ? "§6Ranked" : "§aNormal";
        String mode = entry.gameMode.substring(0, 1).toUpperCase() + entry.gameMode.substring(1);
        int waiting = getQueueSize(entry.gameMode, entry.ranked);
        String bar = String.format(
                "§d§lQUEUE §8» %s §f%s §8| §7%ds §8| §f%d §7waiting",
                type, mode, secs, waiting
        );
        player.sendActionBar(bar);
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
