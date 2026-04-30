package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class DuelManager {
    private final PublicGoon plugin;
    private final ArenaManager arenaManager;
    private final LobbyManager lobbyManager;
    private final Map<UUID, Duel> activeDuels = new HashMap<>();
    private final Deque<PendingMatch> pendingMatches = new ArrayDeque<>();

    public DuelManager(PublicGoon plugin, ArenaManager arenaManager, LobbyManager lobbyManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.lobbyManager = lobbyManager;
        startPendingProcessor();
    }

    public ArenaManager getArenaManager() { return arenaManager; }
    public LobbyManager getLobbyManager() { return lobbyManager; }
    public PublicGoon getPlugin() { return plugin; }

    public Duel getDuel(UUID id) { return activeDuels.get(id); }

    public boolean inDuel(UUID id) { return activeDuels.containsKey(id); }

    /**
     * Returns the number of online players currently inside an active duel for the given mode.
     * Each duel contributes up to two players.
     */
    public int getActivePlayersInMode(GameModeConfig mode) {
        if (mode == null) return 0;
        int count = 0;
        // activeDuels has both player entries pointing at the same Duel; iterate keys directly.
        for (Map.Entry<UUID, Duel> e : activeDuels.entrySet()) {
            Duel d = e.getValue();
            if (d.getMode() != mode) continue;
            Player p = Bukkit.getPlayer(e.getKey());
            if (p != null && p.isOnline()) count++;
        }
        return count;
    }

    public void startMatch(Player a, Player b, GameModeConfig mode) {
        for (Player p : new Player[]{a, b}) {
            if (p == null) continue;
            p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
            p.sendTitle("§a§l\u2694Match Found\u2694", "§7Preparing an arena...", 5, 60, 20);
            p.sendActionBar("");
        }

        World arena = arenaManager.acquire(mode.size);
        if (arena == null) {
            for (Player p : new Player[]{a, b}) {
                if (p == null) continue;
                p.sendTitle("§c§lNo Arena Available", "§7Rejoin the queue to try again.", 5, 60, 15);
                p.sendActionBar("§c§lYou were kicked from queue §8— §7no arena available right now.");
                p.sendMessage("§c§l» §7No arena available. Please rejoin the queue.");
            }
            return;
        }

        launch(a, b, mode, arena);
    }

    private void launch(Player a, Player b, GameModeConfig mode, World arena) {
        Duel duel = new Duel(plugin, this, a, b, mode, arena);
        activeDuels.put(a.getUniqueId(), duel);
        activeDuels.put(b.getUniqueId(), duel);
        duel.beginRound();
    }

    public void endDuel(Duel d) {
        activeDuels.remove(d.getPlayer1());
        activeDuels.remove(d.getPlayer2());
        arenaManager.release(d.getArena());
        tryDispatchPending();
    }

    private void tryDispatchPending() {
        Iterator<PendingMatch> it = pendingMatches.iterator();
        while (it.hasNext()) {
            PendingMatch pm = it.next();
            Player a = Bukkit.getPlayer(pm.a);
            Player b = Bukkit.getPlayer(pm.b);
            if (a == null || b == null) { it.remove(); continue; }
            World arena = arenaManager.acquire(pm.mode.size);
            if (arena == null) break;
            it.remove();
            launch(a, b, pm.mode, arena);
        }
    }

    private void startPendingProcessor() {
        new BukkitRunnable() {
            @Override
            public void run() {
                tryDispatchPending();
            }
        }.runTaskTimer(plugin, 40L, 40L);
    }

    private static class PendingMatch {
        final UUID a;
        final UUID b;
        final GameModeConfig mode;
        final long since;

        PendingMatch(UUID a, UUID b, GameModeConfig mode, long since) {
            this.a = a;
            this.b = b;
            this.mode = mode;
            this.since = since;
        }
    }
}
