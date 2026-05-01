package org.please.publicGoon;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PublicGoonExpansion extends PlaceholderExpansion {
    private final PublicGoon plugin;

    public PublicGoonExpansion(PublicGoon plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "publicgoon";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return plugin.getDescription().getAuthors().isEmpty() ? "PublicGoon" : plugin.getDescription().getAuthors().get(0);
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        if (params.equalsIgnoreCase("statusicon")) {
            Player onlinePlayer = player.getPlayer();
            if (onlinePlayer == null || !onlinePlayer.isOnline()) {
                return "§7●";
            }
            if (plugin.getDuelManager().inDuel(onlinePlayer.getUniqueId())) {
                return "§7⚔️";
            }
            if (plugin.getLobbyManager().isInLobby(onlinePlayer)) {
                return "§7🌲";
            }
            return "§6●";
        }

        if (params.equalsIgnoreCase("status")) {
            Player onlinePlayer = player.getPlayer();
            if (onlinePlayer == null || !onlinePlayer.isOnline()) {
                return "Offline";
            }
            if (plugin.getDuelManager().inDuel(onlinePlayer.getUniqueId())) {
                return "In Game";
            }
            if (plugin.getLobbyManager().isInLobby(onlinePlayer)) {
                return "Lobby";
            }
            return "Unknown";
        }

        if (params.equalsIgnoreCase("induel")) {
            Player onlinePlayer = player.getPlayer();
            if (onlinePlayer == null || !onlinePlayer.isOnline()) {
                return "false";
            }
            return plugin.getDuelManager().inDuel(onlinePlayer.getUniqueId()) ? "true" : "false";
        }

        if (params.equalsIgnoreCase("inlobby")) {
            Player onlinePlayer = player.getPlayer();
            if (onlinePlayer == null || !onlinePlayer.isOnline()) {
                return "false";
            }
            return plugin.getLobbyManager().isInLobby(onlinePlayer) ? "true" : "false";
        }

        return null;
    }
}
