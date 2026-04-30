package org.please.publicGoon;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandExecutor {
    private final LobbyManager lobbyManager;
    private DuelManager duelManager;

    public LobbyCommand(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    public void setDuelManager(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        Player player = (Player) sender;
        String name = command.getName().toLowerCase();

        if (name.equals("setlobby")) {
            if (!player.hasPermission("publicgoon.admin") && !player.isOp()) {
                player.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            lobbyManager.setLobby(player.getLocation());
            player.sendMessage("§a§l» §7Lobby spawn set to your current location.");
            return true;
        }

        // /lobby
        // Check if in a duel first - this ends the match
        if (duelManager != null && duelManager.inDuel(player.getUniqueId())) {
            Duel duel = duelManager.getDuel(player.getUniqueId());
            duel.handleQuit(player);
            player.sendMessage("§aYou have left the duel and returned to the lobby.");
            return true;
        }

        if (!lobbyManager.hasLobby()) {
            player.sendMessage("§cThe lobby has not been set yet! Ask an admin to run /setlobby.");
            return true;
        }
        lobbyManager.teleportToLobby(player);
        player.sendMessage("§a§l» §7Teleported to the lobby.");
        return true;
    }
}
