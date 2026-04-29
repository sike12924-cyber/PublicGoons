package org.please.publicGoon;

import org.bukkit.Material;

public enum GameModeConfig {
    SWORD("Sword", Material.DIAMOND_SWORD, ArenaSize.SMALL, 2, false, true),
    AXE("Axe", Material.IRON_AXE, ArenaSize.SMALL, 2, false, false),
    MACE("Mace", Material.MACE, ArenaSize.AVERAGE, 1, false, false),
    UHC("UHC", Material.GOLDEN_APPLE, ArenaSize.SMALL, 1, true, false),
    NETHOP("NethOP", Material.NETHERITE_SWORD, ArenaSize.SMALL, 1, false, false),
    DIAPOT("DiaPot", Material.SPLASH_POTION, ArenaSize.SMALL, 2, false, false),
    SMP("SMP", Material.IRON_SWORD, ArenaSize.AVERAGE, 1, false, false),
    VANILLA("Vanilla", Material.GRASS_BLOCK, ArenaSize.AVERAGE, 1, false, false);

    public final String displayName;
    public final Material icon;
    public final ArenaSize size;
    public final int rounds;
    public final boolean allowBreakPlace;
    public final boolean enabled;

    GameModeConfig(String displayName, Material icon, ArenaSize size, int rounds, boolean allowBreakPlace, boolean enabled) {
        this.displayName = displayName;
        this.icon = icon;
        this.size = size;
        this.rounds = rounds;
        this.allowBreakPlace = allowBreakPlace;
        this.enabled = enabled;
    }

    public static GameModeConfig fromName(String name) {
        if (name == null) return null;
        for (GameModeConfig g : values()) {
            if (g.displayName.equalsIgnoreCase(name) || g.name().equalsIgnoreCase(name)) {
                return g;
            }
        }
        return null;
    }
}
