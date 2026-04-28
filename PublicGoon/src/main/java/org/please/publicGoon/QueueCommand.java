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
    private LobbyManager lobbyManager;

    public QueueCommand(QueueManager queueManager, MainInventoryGUI mainInventoryGUI) {
        this.queueManager = queueManager;
        this.mainInventoryGUI = mainInventoryGUI;
    }

    public void setLobbyManager(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            if (lobbyManager != null && !lobbyManager.isInLobby(player)) {
                player.sendMessage("§c§l» §7You can only queue from the lobby. Use §f/lobby§7.");
                return true;
            }
            mainInventoryGUI.openMainInventory(player);
            return true;
        }

        String arg = args[0].toLowerCase();
        
        if (arg.equals("leave")) {
            // leave allowed anywhere
            ;
            if (queueManager.removeFromQueue(player)) {
                // message already sent by manager
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
            if (lobbyManager != null && !lobbyManager.isInLobby(player)) {
                player.sendMessage("§cYou must be in the lobby to queue.");
                player.closeInventory();
                return;
            }
            queueManager.addToQueue(player, gameMode, false);
            player.closeInventory();
        }
    }

    private void handleRankedGameModesClick(Player player, int slot) {
        String gameMode = RankedGameModesGUI.getGameModeFromSlot(slot);
        if (gameMode != null) {
            if (lobbyManager != null && !lobbyManager.isInLobby(player)) {
                player.sendMessage("§cYou must be in the lobby to queue.");
                player.closeInventory();
                return;
            }
            queueManager.addToQueue(player, gameMode, true);
            player.closeInventory();
        }
    }
}
