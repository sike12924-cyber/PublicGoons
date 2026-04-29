package org.please.publicGoon;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor {
    private final QueueManager queueManager;

    public LeaveCommand(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (queueManager.removeFromQueue(player.getUniqueId(), false)) {
            player.sendActionBar("");
            player.sendMessage("§aSuccessfully left all queue modes.");
        } else {
            player.sendMessage("§cYou are not in any queue.");
        }
        return true;
    }
}
