package org.please.publicGoon;

import org.bukkit.plugin.java.JavaPlugin;

public final class PublicGoon extends JavaPlugin {
    private QueueManager queueManager;
    private NormalGameModesGUI normalGameModesGUI;
    private RankedGameModesGUI rankedGameModesGUI;
    private MainInventoryGUI mainInventoryGUI;
    private QueueCommand queueCommand;
    private QueueListener queueListener;
    private InventorySwords inventorySwords;

    @Override
    public void onEnable() {
        // Initialize queue system
        queueManager = new QueueManager(this);
        normalGameModesGUI = new NormalGameModesGUI(queueManager);
        rankedGameModesGUI = new RankedGameModesGUI(queueManager);
        mainInventoryGUI = new MainInventoryGUI(this, normalGameModesGUI, rankedGameModesGUI);
        queueCommand = new QueueCommand(queueManager, mainInventoryGUI);
        queueListener = new QueueListener(queueManager);
        inventorySwords = new InventorySwords(mainInventoryGUI);
        
        // Register commands and events
        getCommand("queue").setExecutor(queueCommand);
        getServer().getPluginManager().registerEvents(queueCommand, this);
        getServer().getPluginManager().registerEvents(queueListener, this);
        getServer().getPluginManager().registerEvents(inventorySwords, this);
        
        getLogger().info("PublicGoon PvP Queue Plugin enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("PublicGoon PvP Queue Plugin disabled!");
    }
}
