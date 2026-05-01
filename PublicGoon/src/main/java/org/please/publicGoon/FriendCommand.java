package org.please.publicGoon;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class FriendCommand implements CommandExecutor {
    private final PublicGoon plugin;
    private final FriendConfig friendConfig;
    private final FriendGUI friendGUI;

    public FriendCommand(PublicGoon plugin, FriendConfig friendConfig, FriendGUI friendGUI) {
        this.plugin = plugin;
        this.friendConfig = friendConfig;
        this.friendGUI = friendGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        player.sendMessage("§eFriends §8» §cComing soon!");
        return true;
    }

    private boolean handleAdd(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /friend add <player>");
            return true;
        }

        String targetName = args[1];
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(targetName);

        if (target == null || (!target.hasPlayedBefore() && target.getUniqueId() == null)) {
            player.sendMessage("§cPlayer not found.");
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cYou cannot add yourself as a friend.");
            return true;
        }

        UUID playerUuid = player.getUniqueId();
        UUID targetUuid = target.getUniqueId();

        if (friendConfig.areFriends(playerUuid, targetUuid)) {
            player.sendMessage("§cYou are already friends with §e" + target.getName() + "§c.");
            return true;
        }

        if (friendConfig.hasRequest(targetUuid, playerUuid)) {
            player.sendMessage("§cYou have already sent a friend request to §e" + target.getName() + "§c.");
            return true;
        }

        // Add the request
        friendConfig.addRequest(targetUuid, playerUuid);
        player.sendMessage("§aFriend request sent to §e" + target.getName() + "§a!");

        // Notify the target if they're online
        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            onlineTarget.sendMessage("§e" + player.getName() + " §asent you a friend request!");
            onlineTarget.sendMessage("§7Use §f/friend requests §7to view and accept it.");
        }

        return true;
    }

    private boolean handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /friend remove <player>");
            return true;
        }

        String targetName = args[1];
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(targetName);

        if (target == null || (!target.hasPlayedBefore() && target.getUniqueId() == null)) {
            player.sendMessage("§cPlayer not found.");
            return true;
        }

        UUID playerUuid = player.getUniqueId();
        UUID targetUuid = target.getUniqueId();

        if (!friendConfig.areFriends(playerUuid, targetUuid)) {
            player.sendMessage("§cYou are not friends with §e" + target.getName() + "§c.");
            return true;
        }

        // Remove from both players' friend lists
        friendConfig.removeFriend(playerUuid, targetUuid);
        friendConfig.removeFriend(targetUuid, playerUuid);

        player.sendMessage("§cYou are no longer friends with §e" + target.getName() + "§c.");

        return true;
    }

    private boolean handleAccept(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /friend accept <player>");
            return true;
        }

        String requesterName = args[1];
        OfflinePlayer requester = plugin.getServer().getOfflinePlayer(requesterName);

        if (requester == null || (!requester.hasPlayedBefore() && requester.getUniqueId() == null)) {
            player.sendMessage("§cPlayer not found.");
            return true;
        }

        UUID playerUuid = player.getUniqueId();
        UUID requesterUuid = requester.getUniqueId();

        if (!friendConfig.hasRequest(playerUuid, requesterUuid)) {
            player.sendMessage("§cYou don't have a friend request from §e" + requester.getName() + "§c.");
            return true;
        }

        // Remove the request
        friendConfig.removeRequest(playerUuid, requesterUuid);

        // Add both as friends
        friendConfig.addFriend(playerUuid, requesterUuid);
        friendConfig.addFriend(requesterUuid, playerUuid);

        player.sendMessage("§aYou are now friends with §e" + requester.getName() + "§a!");

        // Notify the requester if they're online
        Player onlineRequester = requester.getPlayer();
        if (onlineRequester != null) {
            onlineRequester.sendMessage("§e" + player.getName() + " §aaccepted your friend request!");
        }

        return true;
    }

    private boolean handleDeny(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /friend deny <player>");
            return true;
        }

        String requesterName = args[1];
        OfflinePlayer requester = plugin.getServer().getOfflinePlayer(requesterName);

        if (requester == null || (!requester.hasPlayedBefore() && requester.getUniqueId() == null)) {
            player.sendMessage("§cPlayer not found.");
            return true;
        }

        UUID playerUuid = player.getUniqueId();
        UUID requesterUuid = requester.getUniqueId();

        if (!friendConfig.hasRequest(playerUuid, requesterUuid)) {
            player.sendMessage("§cYou don't have a friend request from §e" + requester.getName() + "§c.");
            return true;
        }

        // Remove the request
        friendConfig.removeRequest(playerUuid, requesterUuid);

        player.sendMessage("§cYou denied §e" + requester.getName() + "§c's friend request.");

        return true;
    }
}
