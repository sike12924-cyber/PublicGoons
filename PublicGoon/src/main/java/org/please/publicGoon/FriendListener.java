package org.please.publicGoon;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

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
            friendGUI.handleFriendsClick(player, e.getSlot(), e.getCurrentItem());
        } else if (title.startsWith("§eFriend Requests")) {
            e.setCancelled(true);
            friendGUI.handleRequestsClick(player, e.getSlot(), e.getCurrentItem());
        }
    }
}
