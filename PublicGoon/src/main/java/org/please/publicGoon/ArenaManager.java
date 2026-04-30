package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArenaManager {
    private final PublicGoon plugin;
    private final Map<ArenaSize, List<String>> arenas = new EnumMap<>(ArenaSize.class);
    private final Set<String> occupied = new HashSet<>();

    public ArenaManager(PublicGoon plugin) {
        this.plugin = plugin;
        for (ArenaSize s : ArenaSize.values()) arenas.put(s, new ArrayList<>());
        discover();
    }

    private void discover() {
        File container = plugin.getServer().getWorldContainer();
        File[] files = container.listFiles();
        if (files == null) {
            plugin.getLogger().warning("Could not list world container for arena discovery.");
            return;
        }
        for (File f : files) {
            if (!f.isDirectory()) continue;
            String name = f.getName();
            for (ArenaSize s : ArenaSize.values()) {
                if (name.toLowerCase().startsWith(s.prefix + "_arena")) {
                    if (new File(f, "level.dat").exists()) {
                        arenas.get(s).add(name);
                        plugin.getLogger().info("Found arena: " + name + " (" + s.name() + ")");
                    }
                }
            }
        }
    }

    public synchronized World acquire(ArenaSize size) {
        List<String> list = arenas.get(size);
        if (list == null) return null;
        for (String worldName : list) {
            if (occupied.contains(worldName)) continue;
            World w = Bukkit.getWorld(worldName);
            if (w == null) {
                try {
                    w = Bukkit.createWorld(new WorldCreator(worldName));
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load arena world '" + worldName + "': " + e.getMessage());
                    continue;
                }
            }
            if (w != null) {
                occupied.add(worldName);
                return w;
            }
        }
        return null;
    }

    public synchronized void release(World world) {
        if (world != null) occupied.remove(world.getName());
    }

    public boolean isArenaWorld(World world) {
        if (world == null) return false;
        String n = world.getName().toLowerCase();
        for (ArenaSize s : ArenaSize.values()) {
            if (n.startsWith(s.prefix + "_arena")) return true;
        }
        return false;
    }

    public Location getSpawn(World world, ArenaSize size, int playerIndex) {
        // Known spawn points for small arenas
        if (size == ArenaSize.SMALL) {
            if (playerIndex == 0) return new Location(world, -7.5, 159.0, 4.5, 0f, 0f);   // SOUTH
            return new Location(world, -7.5, 159.0, 44.5, 180f, 0f); // NORTH
        }
        // Fallback: world spawn
        Location spawn = world.getSpawnLocation();
        if (playerIndex == 1) spawn.setYaw(spawn.getYaw() + 180f);
        return spawn;
    }
}
