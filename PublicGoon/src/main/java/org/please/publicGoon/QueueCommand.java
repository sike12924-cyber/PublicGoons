package org.please.publicGoon;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

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
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length >= 1 && args[0].equalsIgnoreCase("leave")) {
            if (!queueManager.removeFromQueue(player.getUniqueId(), false)) {
                player.sendMessage("§cYou are not in any queue.");
            } else {
                player.sendActionBar("");
                player.sendMessage("§aSuccessfully left all queue modes.");
            }
            return true;
        }

        if (lobbyManager != null && !lobbyManager.isInLobby(player)) {
            player.sendMessage("§c§l» §7You can only open the queue from the lobby. Use §f/lobby§7.");
            return true;
        }
        mainInventoryGUI.open(player);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!MainInventoryGUI.isOurView(event.getView())) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        GameModeConfig mode = MainInventoryGUI.getModeAtSlot(event.getRawSlot());
        if (mode == null) return;

        if (lobbyManager != null && !lobbyManager.isInLobby(player)) {
            player.sendMessage("§cYou must be in the lobby to queue.");
            player.closeInventory();
            return;
        }
        queueManager.addToQueue(player, mode);
        player.closeInventory();
    }
}
