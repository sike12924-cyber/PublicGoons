package org.please.publicGoon;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class FriendListener implements Listener {
    private final FriendGUI friendGUI;

    public FriendListener(FriendGUI friendGUI) {
        this.friendGUI = friendGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        if (title.startsWith("§eFriends List")) {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_BAMBOO_WOOD_BUTTON_CLICK_ON, 1f, 1f);
            friendGUI.handleFriendsClick(player, e.getSlot(), e.getCurrentItem());
        } else if (title.startsWith("§eFriend Requests")) {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_BAMBOO_WOOD_BUTTON_CLICK_ON, 1f, 1f);
            friendGUI.handleRequestsClick(player, e.getSlot(), e.getCurrentItem());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player player = (Player) e.getPlayer();
        String title = e.getView().getTitle();
        if (title.startsWith("§eFriends List") || title.startsWith("§eFriend Requests")) {
            player.playSound(player.getLocation(), Sound.BLOCK_BAMBOO_WOOD_BUTTON_CLICK_OFF, 1f, 1f);
        }
    }
}
