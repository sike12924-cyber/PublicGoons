package org.please.publicGoon;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor {
    private final QueueManager queueManager;
    private final DuelManager duelManager;
    private SpectateCommand spectateCommand;

    public LeaveCommand(QueueManager queueManager, DuelManager duelManager) {
        this.queueManager = queueManager;
        this.duelManager = duelManager;
    }

    public void setSpectateCommand(SpectateCommand spectateCommand) {
        this.spectateCommand = spectateCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        // Check if in a duel first - this ends the match
        if (duelManager.inDuel(player.getUniqueId())) {
            Duel duel = duelManager.getDuel(player.getUniqueId());
            duel.handleQuit(player);
            player.sendMessage("§aYou have left the duel.");
            return true;
        }

        // Check if spectating
        if (spectateCommand != null && spectateCommand.isSpectating(player.getUniqueId())) {
            spectateCommand.removeSpectator(player);
            player.sendMessage("§aYou are no longer spectating.");
            return true;
        }

        if (queueManager.removeFromQueue(player.getUniqueId(), false)) {
            player.sendActionBar("");
            player.sendMessage("§aSuccessfully left all queue modes.");
        } else {
            player.sendMessage("§cYou are not in any queue.");
        }
        return true;
    }
}
