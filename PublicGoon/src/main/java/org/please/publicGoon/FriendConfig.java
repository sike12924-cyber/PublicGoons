package org.please.publicGoon;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FriendConfig {
    private final PublicGoon plugin;
    private final File dataFolder;

    public FriendConfig(PublicGoon plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "friends");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    private File getPlayerFile(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".yml");
    }

    public void addFriend(UUID player, UUID friend) {
        FileConfiguration config = getConfig(player);
        List<String> friends = config.getStringList("friends");
        if (!friends.contains(friend.toString())) {
            friends.add(friend.toString());
            config.set("friends", friends);
            saveConfig(player, config);
        }
    }

    public void removeFriend(UUID player, UUID friend) {
        FileConfiguration config = getConfig(player);
        List<String> friends = config.getStringList("friends");
        friends.remove(friend.toString());
        config.set("friends", friends);
        saveConfig(player, config);
    }

    public List<UUID> getFriends(UUID player) {
        FileConfiguration config = getConfig(player);
        List<String> friends = config.getStringList("friends");
        List<UUID> result = new ArrayList<>();
        for (String uuid : friends) {
            try {
                result.add(UUID.fromString(uuid));
            } catch (IllegalArgumentException e) {
                // Invalid UUID, skip
            }
        }
        return result;
    }

    public boolean areFriends(UUID player1, UUID player2) {
        return getFriends(player1).contains(player2);
    }

    public void addRequest(UUID player, UUID requester) {
        FileConfiguration config = getConfig(player);
        List<String> requests = config.getStringList("requests");
        if (!requests.contains(requester.toString())) {
            requests.add(requester.toString());
            config.set("requests", requests);
            saveConfig(player, config);
        }
    }

    public void removeRequest(UUID player, UUID requester) {
        FileConfiguration config = getConfig(player);
        List<String> requests = config.getStringList("requests");
        requests.remove(requester.toString());
        config.set("requests", requests);
        saveConfig(player, config);
    }

    public List<UUID> getRequests(UUID player) {
        FileConfiguration config = getConfig(player);
        List<String> requests = config.getStringList("requests");
        List<UUID> result = new ArrayList<>();
        for (String uuid : requests) {
            try {
                result.add(UUID.fromString(uuid));
            } catch (IllegalArgumentException e) {
                // Invalid UUID, skip
            }
        }
        return result;
    }

    public boolean hasRequest(UUID player, UUID requester) {
        return getRequests(player).contains(requester);
    }

    public void clearRequests(UUID player) {
        FileConfiguration config = getConfig(player);
        config.set("requests", new ArrayList<>());
        saveConfig(player, config);
    }

    private FileConfiguration getConfig(UUID player) {
        File file = getPlayerFile(player);
        if (!file.exists()) {
            return YamlConfiguration.loadConfiguration(file);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void saveConfig(UUID player, FileConfiguration config) {
        try {
            config.save(getPlayerFile(player));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
