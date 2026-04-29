package org.please.publicGoon;

import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Floating "HP ❤" indicator that hovers just below a player's username.
 * The display is implemented as a TextDisplay passenger of the player so it
 * tracks them with vanilla interpolation and never flickers.
 */
public class HealthNametag {
    private final PublicGoon plugin;
    private final Player target;
    private TextDisplay display;
    private BukkitTask updateTask;

    public HealthNametag(PublicGoon plugin, Player target) {
        this.plugin = plugin;
        this.target = target;
        spawn();
        startUpdateTask();
    }

    /**
     * Vertical translation applied on top of the passenger anchor on the player.
     * The passenger anchor sits roughly at head height; +0.45 lifts the tag a
     * little so it floats just below the floating username.
     */
    private static final float TRANSLATE_Y = 0.45f;

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
    }
}
