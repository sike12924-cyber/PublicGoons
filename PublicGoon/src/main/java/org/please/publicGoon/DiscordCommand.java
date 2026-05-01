package org.please.publicGoon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DiscordCommand implements CommandExecutor {
    private static final String DISCORD_LINK = "https://discord.gg/HgQEhnQ3S2";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Component message = Component.text("§eDiscord §8» §7Click here to join our Discord!")
                .hoverEvent(HoverEvent.showText(Component.text("§aClick to open Discord invite")))
                .clickEvent(ClickEvent.openUrl(DISCORD_LINK));

        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage(message);
        } else {
            sender.sendMessage("§eDiscord §8» §7" + DISCORD_LINK);
        }

        return true;
    }
}
