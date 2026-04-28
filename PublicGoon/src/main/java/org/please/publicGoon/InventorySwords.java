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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class InventorySwords implements Listener {
    private final MainInventoryGUI mainInventoryGUI;
    private final String ironSwordName = "§a§lNormal Game Modes";
    private final String diamondSwordName = "§6§lRanked Game Modes";

    public InventorySwords(MainInventoryGUI mainInventoryGUI) {
        this.mainInventoryGUI = mainInventoryGUI;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        giveSwordsToPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !item.hasItemMeta()) return;
        
        String itemName = item.getItemMeta().getDisplayName();
        
        if (itemName.equals(ironSwordName)) {
            event.setCancelled(true);
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                mainInventoryGUI.getNormalGUI().openNormalGameModes(player);
            }
        } else if (itemName.equals(diamondSwordName)) {
            event.setCancelled(true);
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                mainInventoryGUI.getRankedGUI().openRankedGameModes(player);
            }
        }
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
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            
            // Prevent moving queue swords
            if (isQueueSword(event.getCurrentItem()) || isQueueSword(event.getCursor())) {
                event.setCancelled(true);
                return;
            }
            
            // Prevent moving items to sword slots (0 and 1)
            if (event.getSlot() == 0 || event.getSlot() == 1) {
                if (!isQueueSword(event.getCurrentItem())) {
                    event.setCancelled(true);
                    // Give the swords back if they were moved
                    giveSwordsToPlayer(player);
                }
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
        // Give Iron Sword (Normal) to slot 0
        ItemStack ironSword = createIronSword();
        player.getInventory().setItem(0, ironSword);
        
        // Give Diamond Sword (Ranked) to slot 1
        ItemStack diamondSword = createDiamondSword();
        player.getInventory().setItem(1, diamondSword);
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

    private boolean isQueueSword(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        String itemName = item.getItemMeta().getDisplayName();
        return itemName.equals(ironSwordName) || itemName.equals(diamondSwordName);
    }
}
