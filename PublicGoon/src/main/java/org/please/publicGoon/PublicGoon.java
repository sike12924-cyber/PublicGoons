package org.please.publicGoon;

import org.bukkit.plugin.java.JavaPlugin;

public final class PublicGoon extends JavaPlugin {
    private LobbyManager lobbyManager;
    private ArenaManager arenaManager;
    private QueueManager queueManager;
    private DuelManager duelManager;
    private MainInventoryGUI mainInventoryGUI;
    private QueueCommand queueCommand;
    private QueueListener queueListener;
    private InventorySwords inventorySwords;
    private LobbyListener lobbyListener;
    private LobbyCommand lobbyCommand;
    private LobbyProtectionListener lobbyProtectionListener;
    private DuelListener duelListener;
    private LeaveCommand leaveCommand;
    private SpectateCommand spectateCommand;
    private KitEditorGUI kitEditorGUI;
    private KitLayoutGUI kitLayoutGUI;
    private KitEditorListener kitEditorListener;
    private ProfileCommand profileCommand;
    private ProfileListener profileListener;
    private FriendConfig friendConfig;
    private FriendGUI friendGUI;
    private FriendCommand friendCommand;
    private FriendListener friendListener;

    @Override
    public void onEnable() {
        // Core managers
        lobbyManager = new LobbyManager(this);
        arenaManager = new ArenaManager(this);
        queueManager = new QueueManager(this);
        duelManager = new DuelManager(this, arenaManager, lobbyManager);
        queueManager.setDuelManager(duelManager);

        // GUIs / helpers
        mainInventoryGUI = new MainInventoryGUI(queueManager, duelManager);
        inventorySwords = new InventorySwords(mainInventoryGUI);
        inventorySwords.setLobbyManager(lobbyManager);

        // Commands
        queueCommand = new QueueCommand(queueManager, mainInventoryGUI);
        queueCommand.setLobbyManager(lobbyManager);
        queueListener = new QueueListener(queueManager);
        lobbyListener = new LobbyListener(this, lobbyManager, inventorySwords, queueManager);
        lobbyCommand = new LobbyCommand(lobbyManager);
        lobbyProtectionListener = new LobbyProtectionListener(lobbyManager);
        duelListener = new DuelListener(duelManager);
        leaveCommand = new LeaveCommand(queueManager, duelManager);
        spectateCommand = new SpectateCommand(this, duelManager);
        leaveCommand.setSpectateCommand(spectateCommand);
        kitEditorGUI = new KitEditorGUI(this);
        kitLayoutGUI = new KitLayoutGUI(this);
        kitEditorListener = new KitEditorListener(kitEditorGUI, kitLayoutGUI);
        inventorySwords.setKitEditorGUI(kitEditorGUI);
        lobbyCommand.setDuelManager(duelManager);
        profileCommand = new ProfileCommand(this);
        profileListener = new ProfileListener();
        friendConfig = new FriendConfig(this);
        friendGUI = new FriendGUI(this, friendConfig);
        friendCommand = new FriendCommand(this, friendConfig, friendGUI);
        friendListener = new FriendListener(friendGUI);

        getCommand("queue").setExecutor(queueCommand);
        getCommand("lobby").setExecutor(lobbyCommand);
        getCommand("setlobby").setExecutor(lobbyCommand);
        getCommand("leave").setExecutor(leaveCommand);
        getCommand("spectate").setExecutor(spectateCommand);
        getCommand("profile").setExecutor(profileCommand);
        getCommand("friend").setExecutor(friendCommand);

        // Events
        getServer().getPluginManager().registerEvents(queueCommand, this);
        getServer().getPluginManager().registerEvents(queueListener, this);
        getServer().getPluginManager().registerEvents(inventorySwords, this);
        getServer().getPluginManager().registerEvents(lobbyListener, this);
        getServer().getPluginManager().registerEvents(lobbyProtectionListener, this);
        getServer().getPluginManager().registerEvents(duelListener, this);
        getServer().getPluginManager().registerEvents(spectateCommand, this);
        getServer().getPluginManager().registerEvents(kitEditorListener, this);
        getServer().getPluginManager().registerEvents(friendListener, this);

        getLogger().info("PublicGoon PvP Queue Plugin enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("PublicGoon PvP Queue Plugin disabled!");
    }
}
