package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Floating "HP ❤" indicator that hovers just below a player's username.
 * The display is implemented as a TextDisplay passenger of the player so it
 * tracks them with vanilla interpolation and never flickers.
 * The player's username is pushed up slightly via a scoreboard team prefix.
 */
public class HealthNametag {
    private final PublicGoon plugin;
    private final Player target;
    private TextDisplay display;
    private BukkitTask updateTask;
    private Team nameTeam;

    public HealthNametag(PublicGoon plugin, Player target) {
        this.plugin = plugin;
        this.target = target;
        spawn();
        startUpdateTask();
    }

    /**
     * Vertical translation applied on top of the passenger anchor on the player.
     * Lowered by 0.15 from 0.45 → 0.30 to position the HP bar correctly below the username.
     */
    private static final float TRANSLATE_Y = 0.30f;

    private void spawn() {
        display = target.getWorld().spawn(target.getLocation(), TextDisplay.class, td -> {
            td.setBillboard(Display.Billboard.CENTER);
            td.setSeeThrough(true);
            td.setShadowed(false);
            td.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            td.setViewRange(64f);
            td.setText(format(target.getHealth()));
            td.setTransformation(new Transformation(
                    new Vector3f(0f, TRANSLATE_Y, 0f),
                    new Quaternionf(),
                    new Vector3f(1f, 1f, 1f),
                    new Quaternionf()
            ));
            td.setPersistent(false);
        });
        // Mount as a passenger so it inherits the player's position natively (no client lag).
        target.addPassenger(display);
        
        // Push the username up by adding a newline prefix via scoreboard team.
        setupNameTeam();
    }
    
    private void setupNameTeam() {
        Scoreboard sb = target.getScoreboard();
        if (sb == null) sb = Bukkit.getScoreboardManager().getMainScoreboard();
        
        String teamName = "hptag_" + target.getUniqueId().toString().substring(0, 8);
        nameTeam = sb.getTeam(teamName);
        if (nameTeam == null) {
            nameTeam = sb.registerNewTeam(teamName);
        }
        
        // Add a newline before the name to push it up visually
        nameTeam.setPrefix("\n");
        nameTeam.addEntry(target.getName());
    }

    private void startUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!target.isOnline() || display == null || display.isDead()) {
                    remove();
                    cancel();
                    return;
                }
                display.setText(format(target.getHealth()));
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }

    private String format(double hp) {
        if (hp < 0) hp = 0;
        // Round to 1 decimal so the user sees values like 18.4 / 0.3 and clean "20".
        double rounded = Math.round(hp * 10.0) / 10.0;
        String hpStr;
        if (rounded == Math.floor(rounded)) {
            hpStr = Integer.toString((int) rounded);
        } else {
            hpStr = String.format(java.util.Locale.ROOT, "%.1f", rounded);
        }
        // White HP, red bold heart.
        return "§f" + hpStr + " §c§l❤";
    }

    public void remove() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        if (display != null) {
            try {
                if (target != null && target.isOnline()) target.removePassenger(display);
            } catch (Throwable ignored) {
            }
            if (!display.isDead()) display.remove();
            display = null;
        }
        if (nameTeam != null) {
            try {
                nameTeam.removeEntry(target.getName());
                nameTeam.unregister();
            } catch (Throwable ignored) {
            }
            nameTeam = null;
        }
    }
}
