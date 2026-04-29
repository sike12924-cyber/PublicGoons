package org.please.publicGoon;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class DuelListener implements Listener {
    private final DuelManager duelManager;

    public DuelListener(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Duel d = duelManager.getDuel(e.getPlayer().getUniqueId());
        if (d == null) return;
        if (!d.isFrozen(e.getPlayer().getUniqueId())) return;
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) return;
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            Location keep = from.clone();
            keep.setYaw(to.getYaw());
            keep.setPitch(to.getPitch());
            e.setTo(keep);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        if (!e.isSneaking()) return;
        Duel d = duelManager.getDuel(e.getPlayer().getUniqueId());
        if (d == null) return;
        if (d.getPhase() != Duel.Phase.COUNTDOWN) return;
        d.tryReady(e.getPlayer());
    }

    @EventHandler
    public void onSpectateTeleport(PlayerTeleportEvent e) {
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.SPECTATE) return;
        Duel d = duelManager.getDuel(e.getPlayer().getUniqueId());
        if (d != null) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Duel d = duelManager.getDuel(e.getPlayer().getUniqueId());
        if (d == null) return;
        if (!d.allowsBreakPlace()) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Duel d = duelManager.getDuel(e.getPlayer().getUniqueId());
        if (d == null) return;
        if (!d.allowsBreakPlace()) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        Duel d = duelManager.getDuel(p.getUniqueId());
        if (d == null) return;
        if (d.getPhase() == Duel.Phase.COUNTDOWN || d.getPhase() == Duel.Phase.ROUND_ENDING
                || d.getPhase() == Duel.Phase.PREPARING || d.getPhase() == Duel.Phase.ENDED) {
            e.setCancelled(true);
            return;
        }
        // FIGHTING: intercept lethal damage to end round cleanly
        if (p.getHealth() - e.getFinalDamage() <= 0) {
            e.setCancelled(true);
            d.handleLethal(p);
        }
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        Duel d = duelManager.getDuel(p.getUniqueId());
        if (d == null) return;
        // Sword: keep food bar full UNTIL the first PvP hit lands; after that, hunger drains normally.
        if (d.getMode() == GameModeConfig.SWORD && !d.isHungerActive()) {
            e.setCancelled(true);
        }
    }

    // First PvP hit between the two duelists activates hunger drain for both.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPvpHit(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player victim = (Player) e.getEntity();
        Duel d = duelManager.getDuel(victim.getUniqueId());
        if (d == null) return;
        if (d.getPhase() != Duel.Phase.FIGHTING) return;

        Player attacker = null;
        if (e.getDamager() instanceof Player) attacker = (Player) e.getDamager();
        else if (e.getDamager() instanceof Projectile) {
            Projectile pr = (Projectile) e.getDamager();
            if (pr.getShooter() instanceof Player) attacker = (Player) pr.getShooter();
        }
        if (attacker == null) return;
        if (!d.involves(attacker.getUniqueId())) return;
        d.activateHunger();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Duel d = duelManager.getDuel(e.getPlayer().getUniqueId());
        if (d != null) d.handleQuit(e.getPlayer());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Duel d = duelManager.getDuel(e.getPlayer().getUniqueId());
        if (d == null) return;
        // Only lock drops while not actively fighting; during the fight, players may drop items freely.
        if (d.getPhase() != Duel.Phase.FIGHTING) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        Duel d = duelManager.getDuel(p.getUniqueId());
        if (d == null) return;
        // Allow inventory edits while fighting (take off armor, rearrange kit, etc.).
        if (d.getPhase() != Duel.Phase.FIGHTING) e.setCancelled(true);
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent e) {
        Duel d = duelManager.getDuel(e.getPlayer().getUniqueId());
        if (d == null) return;
        if (d.getPhase() != Duel.Phase.FIGHTING) e.setCancelled(true);
    }
}
