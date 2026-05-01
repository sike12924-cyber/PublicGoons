package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FriendGUI {
    private final PublicGoon plugin;
    private final FriendConfig friendConfig;
    private final Map<UUID, Integer> currentPages = new ConcurrentHashMap<>();

    public FriendGUI(PublicGoon plugin, FriendConfig friendConfig) {
        this.plugin = plugin;
        this.friendConfig = friendConfig;
    }

    public void openFriendsList(Player player, int page) {
        List<UUID> friends = friendConfig.getFriends(player.getUniqueId());
        int maxPages = (int) Math.ceil(friends.size() / 36.0);
        if (maxPages == 0) maxPages = 1;
        if (page < 0) page = 0;
        if (page >= maxPages) page = maxPages - 1;
        currentPages.put(player.getUniqueId(), page);

        Inventory gui = Bukkit.createInventory(null, 54, "§eFriends List §7(Page " + (page + 1) + "/" + maxPages + ")");

        // Fill border with black glass
        ItemStack filler = createFiller();
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, filler);
            gui.setItem(45 + i, filler);
        }
        for (int i = 0; i < 6; i++) {
            gui.setItem(i * 9, filler);
            gui.setItem(i * 9 + 8, filler);
        }

        // Add friends to the list (36 slots for friends)
        int startIndex = page * 36;
        int slot = 9;
        for (int i = startIndex; i < Math.min(startIndex + 36, friends.size()); i++) {
            UUID friendUuid = friends.get(i);
            OfflinePlayer friend = Bukkit.getOfflinePlayer(friendUuid);
            gui.setItem(slot, createFriendHead(friend));
            slot++;
            if (slot % 9 == 8) slot += 2;
            if (slot >= 45) break;
        }

        // Navigation buttons
        if (page > 0) {
            gui.setItem(45, createButton(Material.ARROW, "§aPrevious Page", "§7Click to go to page " + page));
        }
        if (page < maxPages - 1) {
            gui.setItem(53, createButton(Material.ARROW, "§aNext Page", "§7Click to go to page " + (page + 2)));
        }

        // Back button and info
        gui.setItem(4, createButton(Material.BOOK, "§eYour Friends", "§7Total: §f" + friends.size()));
        gui.setItem(49, createButton(Material.RED_BED, "§cClose", "§7Click to close"));

        player.openInventory(gui);
    }

    public void openRequests(Player player) {
        List<UUID> requests = friendConfig.getRequests(player.getUniqueId());
        Inventory gui = Bukkit.createInventory(null, 54, "§eFriend Requests §7(" + requests.size() + ")");

        // Fill with black glass
        ItemStack filler = createFiller();
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, filler);
        }

        // Add requests to the list with confirm/deny buttons
        int row = 1;
        for (UUID requesterUuid : requests) {
            if (row >= 5) break;
            OfflinePlayer requester = Bukkit.getOfflinePlayer(requesterUuid);
            int baseSlot = row * 9 + 1;

            // Head at slot + 0
            gui.setItem(baseSlot, createRequestHead(requester));
            // Confirm button at slot + 1
            gui.setItem(baseSlot + 1, createConfirmButton());
            // Deny button at slot + 2
            gui.setItem(baseSlot + 2, createDenyButton());

            row++;
        }

        // Back button
        gui.setItem(49, createButton(Material.RED_BED, "§cClose", "§7Click to close"));

        player.openInventory(gui);
    }

    public void handleFriendsClick(Player player, int slot, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        String name = item.getItemMeta().getDisplayName();

        if (name.equals("§cClose")) {
            player.closeInventory();
        } else if (name.equals("§aPrevious Page")) {
            int currentPage = currentPages.getOrDefault(player.getUniqueId(), 0);
            openFriendsList(player, currentPage - 1);
        } else if (name.equals("§aNext Page")) {
            int currentPage = currentPages.getOrDefault(player.getUniqueId(), 0);
            openFriendsList(player, currentPage + 1);
        } else if (item.getType() == Material.PLAYER_HEAD) {
            // Clicked on a friend - could open their profile
            String friendName = name.replaceFirst("^§. ", "").trim();
            OfflinePlayer friend = Bukkit.getOfflinePlayer(friendName);
            ProfileGUI profileGUI = new ProfileGUI(plugin, plugin.getLobbyManager(), plugin.getDuelManager());
            profileGUI.open(player, friend);
        }
    }

    public void handleRequestsClick(Player player, int slot, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        String name = item.getItemMeta().getDisplayName();

        if (name.equals("§cClose")) {
            player.closeInventory();
            return;
        }

        // Calculate row from slot (row 1-4 are used)
        int row = slot / 9;
        int col = slot % 9;

        // Only handle clicks in columns 1, 2, 3 (head, confirm, deny)
        if (row < 1 || row > 4 || col < 1 || col > 3) return;

        // Get the player head at the base slot of this row
        int headSlot = row * 9 + 1;
        ItemStack headItem = player.getOpenInventory().getTopInventory().getItem(headSlot);
        if (headItem == null || headItem.getType() != Material.PLAYER_HEAD || !headItem.hasItemMeta()) return;

        // Parse the requester name from the head display name
        String requesterName = headItem.getItemMeta().getDisplayName();
        requesterName = requesterName.replaceFirst("^§. ", "").trim();
        OfflinePlayer requester = Bukkit.getOfflinePlayer(requesterName);

        if (col == 1) {
            // Clicked on the head - do nothing or show profile
            return;
        } else if (col == 2) {
            // Confirm button
            acceptRequest(player, requester);
        } else if (col == 3) {
            // Deny button
            denyRequest(player, requester);
        }
    }

    private void acceptRequest(Player player, OfflinePlayer requester) {
        UUID playerUuid = player.getUniqueId();
        UUID requesterUuid = requester.getUniqueId();

        if (!friendConfig.hasRequest(playerUuid, requesterUuid)) {
            player.sendMessage("§cThis friend request no longer exists.");
            return;
        }

        friendConfig.removeRequest(playerUuid, requesterUuid);
        friendConfig.addFriend(playerUuid, requesterUuid);
        friendConfig.addFriend(requesterUuid, playerUuid);

        player.sendMessage("§aYou are now friends with §e" + requester.getName() + "§a!");

        // Notify the requester if they're online
        Player onlineRequester = requester.getPlayer();
        if (onlineRequester != null) {
            onlineRequester.sendMessage("§e" + player.getName() + " §aaccepted your friend request!");
        }

        openRequests(player);
    }

    private void denyRequest(Player player, OfflinePlayer requester) {
        UUID playerUuid = player.getUniqueId();
        UUID requesterUuid = requester.getUniqueId();

        friendConfig.removeRequest(playerUuid, requesterUuid);
        player.sendMessage("§cYou denied §e" + requester.getName() + "§c's friend request.");

        openRequests(player);
    }

    private ItemStack createFiller() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createButton(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> loreList = new ArrayList<>();
            loreList.add(lore);
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createFriendHead(OfflinePlayer friend) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(friend);
            Player onlineFriend = friend.getPlayer();
            String statusColor = (onlineFriend != null && onlineFriend.isOnline()) ? "§a" : "§7";
            meta.setDisplayName(statusColor + "● §f" + friend.getName());

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Click to view profile");
            lore.add("§7Status: " + (onlineFriend != null && onlineFriend.isOnline() ? "§aOnline" : "§7Offline"));
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }

    private ItemStack createRequestHead(OfflinePlayer requester) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(requester);
            meta.setDisplayName("§e● §f" + requester.getName());

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§eClick §aConfirm §eto accept");
            lore.add("§eClick §cDeny §eto decline");
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }

    public ItemStack createConfirmButton() {
        return createButton(Material.LIME_STAINED_GLASS_PANE, "§aConfirm", "§7Click to accept friend request");
    }

    public ItemStack createDenyButton() {
        return createButton(Material.RED_STAINED_GLASS_PANE, "§cDeny", "§7Click to decline friend request");
    }
}
