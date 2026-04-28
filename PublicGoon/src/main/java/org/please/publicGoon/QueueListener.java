package org.please.publicGoon;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QueueListener implements Listener {
    private final QueueManager queueManager;

    public QueueListener(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove player from queue when they disconnect
        queueManager.removeFromQueue(event.getPlayer().getUniqueId(), false);
    }
}
