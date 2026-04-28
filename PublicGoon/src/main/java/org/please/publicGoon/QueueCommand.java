package org.please.publicGoon;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class QueueCommand implements CommandExecutor, Listener {
    private final QueueManager queueManager;
    private final MainInventoryGUI mainInventoryGUI;
    private final InventorySwords inventorySwords;

    public QueueCommand(QueueManager queueManager, MainInventoryGUI mainInventoryGUI) {
        this.queueManager = queueManager;
        this.mainInventoryGUI = mainInventoryGUI;
        this.inventorySwords = new InventorySwords(mainInventoryGUI);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Give swords to player and open main inventory
            inventorySwords.giveSwordsToPlayer(player);
            mainInventoryGUI.openMainInventory(player);
            player.sendMessage("§a§l» §7Queue swords added to your inventory!");
            player.sendMessage("§7You can also right-click the swords to open game mode selection");
            return true;
        }

        String arg = args[0].toLowerCase();
        
        if (arg.equals("leave")) {
            if (queueManager.removeFromQueue(player)) {
                player.sendMessage("§c§l» §7You have left the queue!");
            } else {
                player.sendMessage("§cYou are not in a queue!");
            }
            return true;
        }
        
        // For backwards compatibility, handle direct game mode arguments
        if (arg.equals("axe") || arg.equals("uhc") || arg.equals("sword")) {
            player.sendMessage("§cPlease use /queue to open the inventory interface!");
            return true;
        }

        player.sendMessage("§cUsage: /queue or /queue leave");
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        
        event.setCancelled(true);
        
        // Handle main inventory clicks
        if (MainInventoryGUI.isMainInventory(inventory)) {
            handleMainInventoryClick(player, event.getRawSlot());
            return;
        }
        
        // Handle normal game modes GUI clicks
        if (NormalGameModesGUI.isNormalGameModesGUI(inventory)) {
            handleNormalGameModesClick(player, event.getRawSlot());
            return;
        }
        
        // Handle ranked game modes GUI clicks
        if (RankedGameModesGUI.isRankedGameModesGUI(inventory)) {
            handleRankedGameModesClick(player, event.getRawSlot());
            return;
        }
    }

    private void handleMainInventoryClick(Player player, int slot) {
        if (MainInventoryGUI.isIronSwordSlot(slot)) {
            // Open normal game modes
            mainInventoryGUI.getNormalGUI().openNormalGameModes(player);
        } else if (MainInventoryGUI.isDiamondSwordSlot(slot)) {
            // Open ranked game modes
            mainInventoryGUI.getRankedGUI().openRankedGameModes(player);
        }
    }

    private void handleNormalGameModesClick(Player player, int slot) {
        String gameMode = NormalGameModesGUI.getGameModeFromSlot(slot);
        if (gameMode != null) {
            // Check if player is already in queue
            if (queueManager.getPlayerQueue(player.getUniqueId()) != null) {
                player.sendMessage("§cYou are already in a queue! Use /queue leave to exit.");
                return;
            }
            
            // Add to normal queue
            queueManager.addToQueue(player, gameMode, false);
            player.closeInventory();
        }
    }

    private void handleRankedGameModesClick(Player player, int slot) {
        String gameMode = RankedGameModesGUI.getGameModeFromSlot(slot);
        if (gameMode != null) {
            // Check if player is already in queue
            if (queueManager.getPlayerQueue(player.getUniqueId()) != null) {
                player.sendMessage("§cYou are already in a queue! Use /queue leave to exit.");
                return;
            }
            
            // Add to ranked queue
            queueManager.addToQueue(player, gameMode, true);
            player.closeInventory();
        }
    }
}
