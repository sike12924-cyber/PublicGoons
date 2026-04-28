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
    private LobbyManager lobbyManager;
    private LobbyListener lobbyListener;
    private LobbyCommand lobbyCommand;

    @Override
    public void onEnable() {
        // Lobby
        lobbyManager = new LobbyManager(this);

        // Queue system
        queueManager = new QueueManager(this);
        normalGameModesGUI = new NormalGameModesGUI(queueManager);
        rankedGameModesGUI = new RankedGameModesGUI(queueManager);
        mainInventoryGUI = new MainInventoryGUI(this, normalGameModesGUI, rankedGameModesGUI);
        queueCommand = new QueueCommand(queueManager, mainInventoryGUI);
        queueCommand.setLobbyManager(lobbyManager);
        queueListener = new QueueListener(queueManager);
        inventorySwords = new InventorySwords(mainInventoryGUI);
        inventorySwords.setLobbyManager(lobbyManager);
        lobbyListener = new LobbyListener(this, lobbyManager, inventorySwords, queueManager);
        lobbyCommand = new LobbyCommand(lobbyManager);

        // Commands
        getCommand("queue").setExecutor(queueCommand);
        getCommand("lobby").setExecutor(lobbyCommand);
        getCommand("setlobby").setExecutor(lobbyCommand);

        // Events
        getServer().getPluginManager().registerEvents(queueCommand, this);
        getServer().getPluginManager().registerEvents(queueListener, this);
        getServer().getPluginManager().registerEvents(inventorySwords, this);
        getServer().getPluginManager().registerEvents(lobbyListener, this);

        getLogger().info("PublicGoon PvP Queue Plugin enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("PublicGoon PvP Queue Plugin disabled!");
    }
}
