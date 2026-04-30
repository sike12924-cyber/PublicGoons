package org.please.publicGoon;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
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
            player.playSound(player.getLocation(), Sound.BLOCK_BAMBOO_WOOD_BUTTON_CLICK_ON, 1f, 1f);
            kitEditorGUI.handleInventoryClick(player, event.getSlot(), event.getCurrentItem());
            return;
        }

        if (!isLayoutView(title)) return;
        player.playSound(player.getLocation(), Sound.BLOCK_BAMBOO_WOOD_BUTTON_CLICK_ON, 1f, 1f);

        // Save button takes priority - always cancels and triggers a save.
        if (event.getRawSlot() == SAVE_BUTTON_SLOT) {
            event.setCancelled(true);
            kitLayoutGUI.handleInventoryClick(player, SAVE_BUTTON_SLOT, event.getCurrentItem());
            return;
        }

        Inventory top = event.getView().getTopInventory();
        Inventory clicked = event.getClickedInventory();

        // Allow free rearranging within the top inventory (slots 0-48 except save button at 49)
        if (clicked != null && clicked.equals(top)) {
            // Block number key swaps that would move items to player inventory
            if (event.getClick() == ClickType.NUMBER_KEY) {
                event.setCancelled(true);
                return;
            }
            // Allow all other interactions: left click, right click, shift click, drag, etc.
            // This lets players pick up and move items freely within the kit editor
            return;
        }

        // Block all interactions with bottom inventory (player's own inventory) or outside clicks
        event.setCancelled(true);
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
        if (SELECT_TITLE.equals(title) || isLayoutView(title)) {
            player.playSound(player.getLocation(), Sound.BLOCK_BAMBOO_WOOD_BUTTON_CLICK_OFF, 1f, 1f);
        }
        if (isLayoutView(title)) {
            // Drop the per-player mode mapping; closing without saving discards the changes.
            kitLayoutGUI.clearMode(player);
        }
    }
}
