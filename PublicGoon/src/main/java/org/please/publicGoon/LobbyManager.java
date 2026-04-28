package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class LobbyManager {
    private final PublicGoon plugin;
    private Location lobbyLocation;

    public LobbyManager(PublicGoon plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        load();
    }

    private void load() {
        FileConfiguration cfg = plugin.getConfig();
        if (cfg.contains("lobby.world")) {
            String worldName = cfg.getString("lobby.world");
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                lobbyLocation = new Location(
                        world,
                        cfg.getDouble("lobby.x"),
                        cfg.getDouble("lobby.y"),
                        cfg.getDouble("lobby.z"),
                        (float) cfg.getDouble("lobby.yaw"),
                        (float) cfg.getDouble("lobby.pitch")
                );
                return;
            }
        }
        // Fallback: world named "lobby" spawn, otherwise first world's spawn
        World fallback = Bukkit.getWorld("lobby");
        if (fallback == null && !Bukkit.getWorlds().isEmpty()) {
            fallback = Bukkit.getWorlds().get(0);
        }
        if (fallback != null) {
            lobbyLocation = fallback.getSpawnLocation();
        }
    }

    public void setLobby(Location location) {
        this.lobbyLocation = location.clone();
        FileConfiguration cfg = plugin.getConfig();
        cfg.set("lobby.world", location.getWorld().getName());
        cfg.set("lobby.x", location.getX());
        cfg.set("lobby.y", location.getY());
        cfg.set("lobby.z", location.getZ());
        cfg.set("lobby.yaw", location.getYaw());
        cfg.set("lobby.pitch", location.getPitch());
        plugin.saveConfig();
    }

    public Location getLobbyLocation() {
        return lobbyLocation == null ? null : lobbyLocation.clone();
    }

    public boolean hasLobby() {
        return lobbyLocation != null && lobbyLocation.getWorld() != null;
    }

    public boolean isInLobby(Player player) {
        if (!hasLobby()) return false;
        return player.getWorld().getName().equalsIgnoreCase(lobbyLocation.getWorld().getName());
    }

    public boolean isLobbyWorld(World world) {
        if (!hasLobby() || world == null) return false;
        return world.getName().equalsIgnoreCase(lobbyLocation.getWorld().getName());
    }

    public void teleportToLobby(Player player) {
        if (!hasLobby()) {
            player.sendMessage("§cThe lobby has not been set yet! Ask an admin to run /setlobby.");
            return;
        }
        player.teleport(lobbyLocation);
    }
}
