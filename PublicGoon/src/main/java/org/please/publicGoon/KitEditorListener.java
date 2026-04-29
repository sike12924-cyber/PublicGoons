package org.please.publicGoon;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class KitEditorListener implements Listener {
    private static final String SELECT_TITLE = "§e✎ Kit Editor - Select Mode";
    private static final String LAYOUT_PREFIX = "§e✎ Kit Editor - ";
    private static final int SAVE_BUTTON_SLOT = 49;

    private final KitEditorGUI kitEditorGUI;
    private final KitLayoutGUI kitLayoutGUI;

    public KitEditorListener(KitEditorGUI kitEditorGUI, KitLayoutGUI kitLayoutGUI) {
        this.kitEditorGUI = kitEditorGUI;
        this.kitLayoutGUI = kitLayoutGUI;
    }

    private static boolean isLayoutView(String title) {
        return title != null && title.startsWith(LAYOUT_PREFIX) && !title.equals(SELECT_TITLE);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (SELECT_TITLE.equals(title)) {
            event.setCancelled(true);
            kitEditorGUI.handleInventoryClick(player, event.getSlot(), event.getCurrentItem());
            return;
        }

        if (!isLayoutView(title)) return;

        // Save button takes priority - always cancels and triggers a save.
        if (event.getRawSlot() == SAVE_BUTTON_SLOT) {
            event.setCancelled(true);
            kitLayoutGUI.handleInventoryClick(player, SAVE_BUTTON_SLOT, event.getCurrentItem());
            return;
        }

        Inventory top = event.getView().getTopInventory();
        Inventory clicked = event.getClickedInventory();

        // Click outside any inventory or in the player's own inventory: forbidden.
        if (clicked == null || !clicked.equals(top)) {
            event.setCancelled(true);
            return;
        }

        // Shift-click would move items between top and bottom -> forbidden.
        if (event.isShiftClick()) {
            event.setCancelled(true);
            return;
        }

        // Hotbar number keys / offhand swap would swap into the player's inv -> forbidden.
        ClickType click = event.getClick();
        if (click == ClickType.NUMBER_KEY || click == ClickType.SWAP_OFFHAND) {
            event.setCancelled(true);
            return;
        }

        // Pressing Q (drop) inside the editor would lose the item -> forbidden.
        InventoryAction action = event.getAction();
        if (action == InventoryAction.DROP_ONE_SLOT
                || action == InventoryAction.DROP_ALL_SLOT
                || action == InventoryAction.DROP_ONE_CURSOR
                || action == InventoryAction.DROP_ALL_CURSOR) {
            event.setCancelled(true);
            return;
        }

        // Free-form rearranging within the top inventory is allowed.
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (!isLayoutView(title)) return;
        int topSize = event.getView().getTopInventory().getSize();
        // Cancel drags that touch the bottom inventory or the save button slot.
        for (Integer rawSlot : event.getRawSlots()) {
            if (rawSlot >= topSize || rawSlot == SAVE_BUTTON_SLOT) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        if (isLayoutView(title)) {
            // Drop the per-player mode mapping; closing without saving discards the changes.
            kitLayoutGUI.clearMode(player);
        }
    }
}
