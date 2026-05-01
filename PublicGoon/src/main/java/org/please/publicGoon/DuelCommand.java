package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand implements CommandExecutor {
    private final PublicGoon plugin;
    private final DuelGUI duelGUI;

    public DuelCommand(PublicGoon plugin, DuelGUI duelGUI) {
        this.plugin = plugin;
        this.duelGUI = duelGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("§cUsage: /duel <player> | /duel accept <player> | /duel deny <player>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "accept":
                return handleAccept(player, args);
            case "deny":
                return handleDeny(player, args);
            default:
                // Treat as player name for sending duel request
                return handleDuelRequest(player, subCommand);
        }
    }

    private boolean handleDuelRequest(Player player, String targetName) {
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null || !target.isOnline()) {
            player.sendMessage("§cPlayer not found or offline.");
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cYou cannot duel yourself.");
            return true;
        }

        // Check if player is in a duel
        if (plugin.getDuelManager().inDuel(player.getUniqueId())) {
            player.sendMessage("§cYou are already in a duel.");
            return true;
        }

        // Check if target is in a duel
        if (plugin.getDuelManager().inDuel(target.getUniqueId())) {
            player.sendMessage("§cThat player is already in a duel.");
            return true;
        }

        // Open the duel setup GUI
        duelGUI.openDuelSetup(player, target);
        player.sendMessage("§eDuel §8» §7Select gamemode, rounds, and map size!");

        return true;
    }

    private boolean handleAccept(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /duel accept <player>");
            return true;
        }

        String requesterName = args[1];
        Player requester = Bukkit.getPlayerExact(requesterName);

        if (requester == null || !requester.isOnline()) {
            player.sendMessage("§cThat player is no longer online.");
            return true;
        }

        // Check if there's a pending request from this player
        DuelRequest request = plugin.getDuelManager().getDuelRequest(player.getUniqueId(), requester.getUniqueId());
        if (request == null) {
            player.sendMessage("§cYou don't have a pending duel request from §e" + requesterName + "§c.");
            return true;
        }

        // Check if either player is already in a duel
        if (plugin.getDuelManager().inDuel(player.getUniqueId())) {
            player.sendMessage("§cYou are already in a duel.");
            return true;
        }

        if (plugin.getDuelManager().inDuel(requester.getUniqueId())) {
            player.sendMessage("§cThat player is already in a duel.");
            return true;
        }

        // Remove the request
        plugin.getDuelManager().removeDuelRequest(player.getUniqueId(), requester.getUniqueId());

        // Start the duel
        player.sendMessage("§eDuel §8» §aYou accepted the duel request from §f" + requesterName + "§a!");
        requester.sendMessage("§eDuel §8» §f" + player.getName() + " §aaccepted your duel request!");

        // Play sounds
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        requester.playSound(requester.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

        // Start the match with custom rounds
        plugin.getDuelManager().startMatch(requester, player, request.getGameMode(), request.getRounds());

        return true;
    }

    private boolean handleDeny(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /duel deny <player>");
            return true;
        }

        String requesterName = args[1];
        Player requester = Bukkit.getPlayerExact(requesterName);

        if (requester == null || !requester.isOnline()) {
            player.sendMessage("§cThat player is no longer online.");
            return true;
        }

        // Check if there's a pending request from this player
        DuelRequest request = plugin.getDuelManager().getDuelRequest(player.getUniqueId(), requester.getUniqueId());
        if (request == null) {
            player.sendMessage("§cYou don't have a pending duel request from §e" + requesterName + "§c.");
            return true;
        }

        // Remove the request
        plugin.getDuelManager().removeDuelRequest(player.getUniqueId(), requester.getUniqueId());

        player.sendMessage("§eDuel §8» §cYou denied the duel request from §f" + requesterName + "§c.");
        if (requester != null && requester.isOnline()) {
            requester.sendMessage("§eDuel §8» §f" + player.getName() + " §cdenied your duel request.");
        }

        return true;
    }

}
