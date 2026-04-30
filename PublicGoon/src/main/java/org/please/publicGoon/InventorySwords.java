package org.please.publicGoon;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventorySwords implements Listener {
    public static final String SWORD_NAME = "§6\u2694 Queue Duels";
    public static final String KIT_EDITOR_NAME = "§e✎ Kit Editor";
    private static final int QUEUE_SLOT = 0;
    private static final int KIT_EDITOR_SLOT = 5;
    private static final int OFFHAND_SLOT = 40;

    private final MainInventoryGUI mainInventoryGUI;
    private LobbyManager lobbyManager;
    private KitEditorGUI kitEditorGUI;

    public InventorySwords(MainInventoryGUI mainInventoryGUI) {
        this.mainInventoryGUI = mainInventoryGUI;
    }

    public void setLobbyManager(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    public void setKitEditorGUI(KitEditorGUI kitEditorGUI) {
        this.kitEditorGUI = kitEditorGUI;
    }

    private boolean inLobby(Player p) {
        return lobbyManager != null && lobbyManager.isInLobby(p);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
        
        if (isQueueSword(item)) {
            // Right-click in lobby opens the Queue Duels menu; outside lobby do nothing
            if (!inLobby(player)) return;
            event.setCancelled(true);
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                mainInventoryGUI.open(player);
            }
        } else if (isKitEditor(item)) {
            if (!inLobby(player)) return;
            event.setCancelled(true);
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (kitEditorGUI != null) {
                    kitEditorGUI.open(player);
                }
            }
        }
    }

    // Never let the queue sword deal damage
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        if (isQueueSword(player.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        // Restrictions apply only when the player is in the lobby world
        if (!inLobby(player)) return;

        // Block all number key swaps involving lobby items
        if (event.getClick().name().contains("NUMBER_KEY")) {
            ItemStack current = event.getCurrentItem();
            if (isQueueSword(current) || isKitEditor(current)) {
                event.setCancelled(true);
                return;
            }
            // Also prevent swapping regular items into the reserved slots
            int hotbarButton = event.getHotbarButton();
            if (hotbarButton == QUEUE_SLOT || hotbarButton == KIT_EDITOR_SLOT) {
                event.setCancelled(true);
                return;
            }
        }

        // Block queue swords and kit editor from being placed in the offhand slot or swapped to offhand
        if (event.getSlot() == OFFHAND_SLOT || event.getRawSlot() == OFFHAND_SLOT) {
            if (isQueueSword(event.getCurrentItem()) || isQueueSword(event.getCursor()) ||
                isKitEditor(event.getCurrentItem()) || isKitEditor(event.getCursor())) {
                event.setCancelled(true);
                return;
            }
        }
        if (event.getClick() != null && event.getClick().name().contains("SWAP_OFFHAND")) {
            if (isQueueSword(event.getCurrentItem()) || isKitEditor(event.getCurrentItem())) {
                event.setCancelled(true);
                return;
            }
        }

        // Prevent moving queue swords and kit editor around
        if (isQueueSword(event.getCurrentItem()) || isQueueSword(event.getCursor()) ||
            isKitEditor(event.getCurrentItem()) || isKitEditor(event.getCursor())) {
            event.setCancelled(true);
            return;
        }

        // Reserved queue-sword slot (0) cannot be overwritten
        if (event.getSlot() == QUEUE_SLOT) {
            if (!isQueueSword(event.getCurrentItem())) {
                event.setCancelled(true);
                giveSwordsToPlayer(player);
            }
        }

        // Reserved kit editor slot (5) cannot be overwritten
        if (event.getSlot() == KIT_EDITOR_SLOT) {
            if (!isKitEditor(event.getCurrentItem())) {
                event.setCancelled(true);
                giveSwordsToPlayer(player);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!inLobby(player)) return;
        for (Integer slot : event.getInventorySlots()) {
            if (slot == QUEUE_SLOT || slot == KIT_EDITOR_SLOT || slot == OFFHAND_SLOT) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!inLobby(event.getPlayer())) return;
        if (isQueueSword(event.getItemDrop().getItemStack()) || isKitEditor(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!inLobby(player)) return;
        if (player.getInventory().getHeldItemSlot() == QUEUE_SLOT) {
            if (!isQueueSword(player.getInventory().getItem(QUEUE_SLOT))) {
                giveSwordsToPlayer(player);
            }
        }
        if (player.getInventory().getHeldItemSlot() == KIT_EDITOR_SLOT) {
            if (!isKitEditor(player.getInventory().getItem(KIT_EDITOR_SLOT))) {
                giveSwordsToPlayer(player);
            }
        }
    }

    public void giveSwordsToPlayer(Player player) {
        if (!inLobby(player)) return;
        player.getInventory().setItem(QUEUE_SLOT, createQueueSword());
        player.getInventory().setItem(KIT_EDITOR_SLOT, createKitEditorBook());
        if (isQueueSword(player.getInventory().getItemInOffHand())) {
            player.getInventory().setItemInOffHand(null);
        }
    }

    public void removeSwordsFromPlayer(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (isQueueSword(contents[i]) || isKitEditor(contents[i])) {
                player.getInventory().setItem(i, null);
            }
        }
        if (isQueueSword(player.getInventory().getItemInOffHand()) || isKitEditor(player.getInventory().getItemInOffHand())) {
            player.getInventory().setItemInOffHand(null);
        }
    }

    private ItemStack createQueueSword() {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(SWORD_NAME);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
            sword.setItemMeta(meta);
        }
        return sword;
    }

    private ItemStack createKitEditorBook() {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(KIT_EDITOR_NAME);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
            book.setItemMeta(meta);
        }
        return book;
    }

    public boolean isQueueSword(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String name = item.getItemMeta().getDisplayName();
        return name != null && name.equals(SWORD_NAME);
    }

    public boolean isKitEditor(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String name = item.getItemMeta().getDisplayName();
        return name != null && name.equals(KIT_EDITOR_NAME);
    }
}
