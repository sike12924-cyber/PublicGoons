package org.please.publicGoon;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class KitEditorListener implements Listener {
    private final KitEditorGUI kitEditorGUI;
    private final KitLayoutGUI kitLayoutGUI;

    public KitEditorListener(KitEditorGUI kitEditorGUI, KitLayoutGUI kitLayoutGUI) {
        this.kitEditorGUI = kitEditorGUI;
        this.kitLayoutGUI = kitLayoutGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals("§e✎ Kit Editor - Select Mode")) {
            event.setCancelled(true);
            kitEditorGUI.handleInventoryClick(player, event.getSlot(), event.getCurrentItem());
        } else if (title.startsWith("§e✎ Kit Editor - ")) {
            // Handle save button
            if (event.getSlot() == 49) {
                event.setCancelled(true);
                kitLayoutGUI.handleInventoryClick(player, event.getSlot(), event.getCurrentItem());
            }
            // Allow normal inventory manipulation for the rest
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        if (title.startsWith("§e✎ Kit Editor - ")) {
            // If they close without saving, we don't do anything special
            // The kit is only saved when they click the save button
        }
    }
}
