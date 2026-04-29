package org.please.publicGoon;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class HealthNametag {
    private final PublicGoon plugin;
    private final Player target;
    private final Player viewer;
    private String originalName;
    private BukkitTask updateTask;
    
    public HealthNametag(PublicGoon plugin, Player target, Player viewer) {
        this.plugin = plugin;
        this.target = target;
        this.viewer = viewer;
        
        this.originalName = target.getCustomName();
        target.setCustomName(getHealthDisplay());
        target.setCustomNameVisible(true);
        
        startUpdateTask();
    }
    
    private void startUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!target.isOnline() || !viewer.isOnline()) {
                    remove();
                    cancel();
                    return;
                }
                
                target.setCustomName(getHealthDisplay());
            }
        }.runTaskTimer(plugin, 4L, 4L);
    }
    
    private String getHealthDisplay() {
        double health = target.getHealth();
        return "§f" + Math.round(health) + " §c❤";
    }
    
    public void remove() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        if (target != null && target.isOnline()) {
            target.setCustomName(originalName);
            target.setCustomNameVisible(originalName != null && !originalName.isEmpty());
        }
    }
}
