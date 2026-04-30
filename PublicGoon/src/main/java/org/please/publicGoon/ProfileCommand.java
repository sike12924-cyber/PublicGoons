package org.please.publicGoon;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProfileCommand implements CommandExecutor {
    private final PublicGoon plugin;
    private final ProfileGUI profileGUI;

    public ProfileCommand(PublicGoon plugin) {
        this.plugin = plugin;
        this.profileGUI = new ProfileGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        OfflinePlayer target;
        if (args.length == 0) {
            target = player;
        } else {
            // Try to get offline player by name
            target = plugin.getServer().getOfflinePlayer(args[0]);
            if (target == null || (!target.hasPlayedBefore() && target.getUniqueId() == null)) {
                player.sendMessage("§cPlayer not found.");
                return true;
            }
        }

        profileGUI.open(player, target);
        return true;
    }
}
