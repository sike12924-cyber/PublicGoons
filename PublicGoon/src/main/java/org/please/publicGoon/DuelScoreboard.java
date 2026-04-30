package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Per-viewer duel sidebar in the PVP GOONS theme.
 *
 * Layout (top to bottom):
 *   PVP GOONS   (objective title)
 *   §9/§c🚩 viewerName
 *   §7ℹ Score: §9 self §7- §c opp
 *   §8⌚ %server_time_d/L/y%
 *   §7→ %player_world% §8(§7%ping%ms§8)
 *   §6pvpgoons.elytra.top
 *
 * Lines are anchored to five invisible entries owned by five scoreboard teams;
 * we only mutate each team's prefix on update, so values change in place without
 * the classic flicker caused by removing & re-adding score entries.
 */
public class DuelScoreboard {
    private final Scoreboard scoreboard;
    private final Objective objective;
    private Objective healthObj;
    private final Player viewer;
    private final Player opponent;
    private final GameModeConfig mode;
    private final boolean isBlue;

    // Anchor entries (one per row). Each is a unique color sequence which renders
    // as effectively-empty text, so the actual line content lives in the team prefix.
    private static final String[] ANCHORS = {
            "§0§r", "§1§r", "§2§r", "§3§r", "§4§r"
    };

    public DuelScoreboard(Player viewer, Player opponent, GameModeConfig mode) {
        this(viewer, opponent, mode, true);
    }

    public DuelScoreboard(Player viewer, Player opponent, GameModeConfig mode, boolean isBlue) {
        this.viewer = viewer;
        this.opponent = opponent;
        this.mode = mode;
        this.isBlue = isBlue;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("pvpgoons", "dummy", title());
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        hideScoreNumbers(this.objective);
        initLines();
        // Health below-name (disabled for AXE)
        if (mode != GameModeConfig.AXE) {
            healthObj = scoreboard.registerNewObjective("pvphealth", "dummy", "\u00a7c\u00a7l\u2764");
            healthObj.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }
        viewer.setScoreboard(scoreboard);
        // Initial paint so the board has data the moment it appears.
        update(0, 0, 1);
    }

    private static String hex(String h) {
        StringBuilder s = new StringBuilder("§x");
        for (char c : h.toUpperCase().toCharArray()) s.append('§').append(c);
        return s.toString();
    }

    private static String title() {
        // &l<#CCCCCC>PVP&r &#E38200&lG&#B46700&lO&#854C00&lO&#553100&lN&#261600&lS
        return hex("CCCCCC") + "§lPVP§r "
                + hex("E38200") + "§lG"
                + hex("B46700") + "§lO"
                + hex("854C00") + "§lO"
                + hex("553100") + "§lN"
                + hex("261600") + "§lS";
    }

    private void initLines() {
        for (int i = 0; i < ANCHORS.length; i++) {
            String entry = ANCHORS[i];
            Team team = scoreboard.registerNewTeam("pvpgoons_line" + i);
            team.addEntry(entry);
            // Top row (i = 0) gets the highest score so it renders at the top.
            objective.getScore(entry).setScore(ANCHORS.length - i);
        }
    }

    private void setLine(int idx, String content) {
        Team team = scoreboard.getTeam("pvpgoons_line" + idx);
        if (team == null) return;
        team.setPrefix(content == null ? "" : content);
    }

    public void update(int selfScore, int oppScore, int round) {
        if (viewer == null || !viewer.isOnline()) return;

        String selfColor = isBlue ? "§9" : "§c";
        String oppColor = isBlue ? "§c" : "§9";

        setLine(0, selfColor + "§l\uD83D\uDEA9 §r" + selfColor + viewer.getName());
        setLine(1, "§7\u2139 Score: " + selfColor + selfScore + " §7- " + oppColor + oppScore);
        setLine(2, "§8\u231A " + resolve(viewer, "%server_time_d/L/y%"));
        setLine(3, "§7\u2192 " + resolve(viewer, "%player_world%") + " §8(§7" + getPing(viewer) + "ms§8)");
        setLine(4, "§6pvpgoons.elytra.top");
    }

    public void updateHealth(Player self, Player opp) {
        if (healthObj == null) return;
        if (self != null && self.isOnline())
            healthObj.getScore(self.getName()).setScore((int) Math.round(self.getHealth()));
        if (opp != null && opp.isOnline())
            healthObj.getScore(opp.getName()).setScore((int) Math.round(opp.getHealth()));
    }

    public void remove() {
        if (viewer != null && viewer.isOnline()) {
            viewer.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    private static boolean hasPAPI() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    /** Resolve placeholders via PlaceholderAPI when present, otherwise via a manual fallback. */
    private static String resolve(Player player, String text) {
        if (text == null || text.isEmpty()) return text;
        if (hasPAPI()) {
            try {
                Class<?> papi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                return (String) papi.getMethod("setPlaceholders", Player.class, String.class)
                        .invoke(null, player, text);
            } catch (Throwable ignored) {
                // Fall through to manual replacement.
            }
        }
        String date = new SimpleDateFormat("d/L/y").format(new Date());
        String world = player.getWorld() != null ? player.getWorld().getName() : "?";
        int ping = getPing(player);
        return text
                .replace("%server_time_d/L/y%", date)
                .replace("%player_world%", world)
                .replace("%ping%", String.valueOf(ping));
    }

    private static int getPing(Player p) {
        if (p == null) return 0;
        try {
            return p.getPing(); // Paper 1.17+
        } catch (Throwable t) {
            return 0;
        }
    }

    /**
     * Hide the right-side score numbers using Paper's NumberFormat API (1.20.3+).
     * Falls back silently on older builds where the API isn't available.
     */
    private static void hideScoreNumbers(Objective objective) {
        try {
            Class<?> nfClass = Class.forName("org.bukkit.scoreboard.NumberFormat");
            Object blank = nfClass.getMethod("blank").invoke(null);
            objective.getClass().getMethod("numberFormat", nfClass).invoke(objective, blank);
        } catch (Throwable ignored) {
            // Number formatting not supported on this server; numbers will remain visible.
        }
    }
}
