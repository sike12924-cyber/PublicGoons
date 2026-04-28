package org.please.publicGoon;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class InventorySwords implements Listener {
    private final MainInventoryGUI mainInventoryGUI;
    private LobbyManager lobbyManager;
    private final String ironSwordName = "§a§lNormal Game Modes";
    private final String diamondSwordName = "§6§lRanked Game Modes";
    private static final int OFFHAND_SLOT = 40;

    public InventorySwords(MainInventoryGUI mainInventoryGUI) {
        this.mainInventoryGUI = mainInventoryGUI;
    }

    public void setLobbyManager(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !item.hasItemMeta()) return;
        
        String itemName = item.getItemMeta().getDisplayName();
        
        if (itemName.equals(ironSwordName)) {
            event.setCancelled(true);
            if (!isAllowedHere(player)) return;
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                mainInventoryGUI.getNormalGUI().openNormalGameModes(player);
            }
        } else if (itemName.equals(diamondSwordName)) {
            event.setCancelled(true);
            if (!isAllowedHere(player)) return;
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                mainInventoryGUI.getRankedGUI().openRankedGameModes(player);
            }
        }
    }

    private boolean isAllowedHere(Player player) {
        return lobbyManager == null || lobbyManager.isInLobby(player);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack weapon = player.getInventory().getItemInMainHand();
            
            if (isQueueSword(weapon)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        // Block placing queue swords into the offhand slot via shift-click / hotbar swap / number keys
        if (event.getSlot() == OFFHAND_SLOT || event.getRawSlot() == OFFHAND_SLOT) {
            if (isQueueSword(event.getCurrentItem()) || isQueueSword(event.getCursor())) {
                event.setCancelled(true);
                return;
            }
        }
        if (event.getClick() != null && event.getClick().name().contains("SWAP_OFFHAND")) {
            if (isQueueSword(event.getCurrentItem())) {
                event.setCancelled(true);
                return;
            }
        }

        // Prevent moving queue swords around
        if (isQueueSword(event.getCurrentItem()) || isQueueSword(event.getCursor())) {
            event.setCancelled(true);
            return;
        }

        // Prevent moving items into the reserved sword slots (0 and 1)
        if (event.getSlot() == 0 || event.getSlot() == 1) {
            if (!isQueueSword(event.getCurrentItem())) {
                event.setCancelled(true);
                giveSwordsToPlayer(player);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            // Prevent dragging items over sword slots
            for (Integer slot : event.getInventorySlots()) {
                if (slot == 0 || slot == 1) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isQueueSword(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            // Prevent picking up items that would replace sword slots
            if (player.getInventory().getHeldItemSlot() == 0 || player.getInventory().getHeldItemSlot() == 1) {
                if (!isQueueSword(player.getInventory().getItem(player.getInventory().getHeldItemSlot()))) {
                    giveSwordsToPlayer(player);
                }
            }
        }
    }

    public void giveSwordsToPlayer(Player player) {
        // Only the world lobby has the queue swords
        if (lobbyManager != null && !lobbyManager.isInLobby(player)) {
            return;
        }
        // Give Iron Sword (Normal) to slot 0
        ItemStack ironSword = createIronSword();
        player.getInventory().setItem(0, ironSword);

        // Give Diamond Sword (Ranked) to slot 1
        ItemStack diamondSword = createDiamondSword();
        player.getInventory().setItem(1, diamondSword);

        // Make sure no queue sword exists in offhand
        if (isQueueSword(player.getInventory().getItemInOffHand())) {
            player.getInventory().setItemInOffHand(null);
        }
    }

    public void removeSwordsFromPlayer(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (isQueueSword(contents[i])) {
                player.getInventory().setItem(i, null);
            }
        }
        if (isQueueSword(player.getInventory().getItemInOffHand())) {
            player.getInventory().setItemInOffHand(null);
        }
    }

    private ItemStack createIronSword() {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = sword.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ironSwordName);
            meta.setLore(Arrays.asList(
                "§7Right-click to open",
                "§7Normal game modes",
                "",
                "§eCasual unranked matches"
            ));
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            sword.setItemMeta(meta);
        }
        
        return sword;
    }

    private ItemStack createDiamondSword() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(diamondSwordName);
            meta.setLore(Arrays.asList(
                "§7Right-click to open",
                "§7Ranked game modes",
                "",
                "§eCompetitive ranked matches"
            ));
            meta.setUnbreakable(true);
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            sword.setItemMeta(meta);
        }
        
        return sword;
    }

    public boolean isQueueSword(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        String itemName = item.getItemMeta().getDisplayName();
        return itemName.equals(ironSwordName) || itemName.equals(diamondSwordName);
    }
}
