package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Duel {
    public enum Phase { PREPARING, COUNTDOWN, FIGHTING, ROUND_ENDING, ENDED }

    private final PublicGoon plugin;
    private final DuelManager manager;
    private final UUID p1;
    private final UUID p2;
    private final GameModeConfig mode;
    private final World arena;

    private int scoreP1 = 0;
    private int scoreP2 = 0;
    private int round = 0;
    private Phase phase = Phase.PREPARING;

    private final Set<UUID> readied = new HashSet<>();
    private final Set<UUID> frozen = new HashSet<>();
    private BukkitTask countdownTask;
    private int countdown;
    private boolean hungerActive = false;
    
    private DuelScoreboard scoreboardP1;
    private DuelScoreboard scoreboardP2;
    private BukkitTask scoreboardUpdateTask;
    
    private HealthNametag healthNametagP1;
    private HealthNametag healthNametagP2;

    public Duel(PublicGoon plugin, DuelManager manager, Player p1, Player p2, GameModeConfig mode, World arena) {
        this.plugin = plugin;
        this.manager = manager;
        this.p1 = p1.getUniqueId();
        this.p2 = p2.getUniqueId();
        this.mode = mode;
        this.arena = arena;
    }

    public UUID getPlayer1() { return p1; }
    public UUID getPlayer2() { return p2; }
    public World getArena() { return arena; }
    public GameModeConfig getMode() { return mode; }
    public Phase getPhase() { return phase; }
    public boolean isFrozen(UUID id) { return frozen.contains(id); }
    public boolean allowsBreakPlace() { return mode.allowBreakPlace; }
    public boolean isHungerActive() { return hungerActive; }
    public boolean involves(UUID id) { return p1.equals(id) || p2.equals(id); }

    public void activateHunger() {
        if (hungerActive) return;
        hungerActive = true;
        // Drop saturation to 0 so hunger actually starts ticking down
        Player a = Bukkit.getPlayer(p1);
        Player b = Bukkit.getPlayer(p2);
        if (a != null) a.setSaturation(0f);
        if (b != null) b.setSaturation(0f);
    }

    public void beginRound() {
        round++;
        readied.clear();
        hungerActive = false;
        Player a = Bukkit.getPlayer(p1);
        Player b = Bukkit.getPlayer(p2);
        if (a == null || b == null) { abandon(); return; }

        Location s1 = manager.getArenaManager().getSpawn(arena, mode.size, 0);
        Location s2 = manager.getArenaManager().getSpawn(arena, mode.size, 1);

        resetPlayer(a);
        resetPlayer(b);
        applyKit(a);
        applyKit(b);
        applyHungerStart(a);
        applyHungerStart(b);
        a.teleport(s1);
        b.teleport(s2);

        frozen.add(p1);
        frozen.add(p2);
        phase = Phase.COUNTDOWN;
        countdown = 10;

        if (round > 1) {
            a.playSound(a.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 1f, 1f);
            b.playSound(b.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 1f, 1f);
        }

        // Initialize scoreboards on first round
        if (round == 1) {
            scoreboardP1 = new DuelScoreboard(a, b, mode);
            scoreboardP2 = new DuelScoreboard(b, a, mode);
            startScoreboardUpdater();
            
            // Initialize health nametags
            healthNametagP1 = new HealthNametag(plugin, b, a);
            healthNametagP2 = new HealthNametag(plugin, a, b);
        }

        // Immediate first tick (shows "10")
        tickCountdown();
        countdown--;

        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (phase != Phase.COUNTDOWN) { cancel(); return; }
                if (countdown <= 0) {
                    cancel();
                    go();
                    return;
                }
                tickCountdown();
                countdown--;
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void tickCountdown() {
        Player a = Bukkit.getPlayer(p1);
        Player b = Bukkit.getPlayer(p2);
        if (a == null || b == null) { abandon(); return; }

        String title = "§e§l" + countdown;
        String sub = "§7Get ready...";
        for (Player p : new Player[]{a, b}) {
            p.sendTitle(title, sub, 0, 25, 5);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
            // After the first 3 seconds, allow ready-up via sneak
            if (countdown <= 7) {
                int ready = readied.size();
                p.sendActionBar("§eLeft Shift: §fSkip countdown §c❌ §7(" + ready + "/2)");
            }
        }
    }

    public void tryReady(Player p) {
        if (phase != Phase.COUNTDOWN) return;
        if (countdown > 7) return;
        if (!readied.add(p.getUniqueId())) return;

        // Confirmation sound for both players when one of them readies up
        Player a = Bukkit.getPlayer(p1);
        Player b = Bukkit.getPlayer(p2);
        for (Player pl : new Player[]{a, b}) {
            if (pl != null) pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }

        if (readied.size() >= 2) {
            if (countdownTask != null) countdownTask.cancel();
            go();
        } else {
            // Refresh display immediately
            Player other = Bukkit.getPlayer(p.getUniqueId().equals(p1) ? p2 : p1);
            p.sendActionBar("§eLeft Shift: §fSkip countdown §c❌ §7(1/2)");
            if (other != null) other.sendActionBar("§eLeft Shift: §fSkip countdown §c❌ §7(1/2)");
        }
    }

    private void go() {
        if (phase == Phase.ENDED) return;
        phase = Phase.FIGHTING;
        frozen.clear();
        Player a = Bukkit.getPlayer(p1);
        Player b = Bukkit.getPlayer(p2);
        for (Player p : new Player[]{a, b}) {
            if (p == null) continue;
            p.sendTitle("§a§lGO!", "", 0, 15, 10);
            p.sendActionBar("");
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
        }
    }

    private void startScoreboardUpdater() {
        scoreboardUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (phase == Phase.ENDED) {
                    cancel();
                    return;
                }
                Player a = Bukkit.getPlayer(p1);
                Player b = Bukkit.getPlayer(p2);
                if (a == null || b == null) {
                    cancel();
                    return;
                }
                if (scoreboardP1 != null) scoreboardP1.update(scoreP1, scoreP2, round);
                if (scoreboardP2 != null) scoreboardP2.update(scoreP2, scoreP1, round);
            }
        }.runTaskTimer(plugin, 5L, 5L);
    }

    public void handleLethal(Player loser) {
        if (phase != Phase.FIGHTING) return;
        phase = Phase.ROUND_ENDING;

        boolean loserIsP1 = loser.getUniqueId().equals(p1);
        if (loserIsP1) scoreP2++; else scoreP1++;

        UUID winnerId = loserIsP1 ? p2 : p1;
        Player winner = Bukkit.getPlayer(winnerId);

        int sWinner = loserIsP1 ? scoreP2 : scoreP1;
        int sLoser = loserIsP1 ? scoreP1 : scoreP2;

        // Heal/reset loser to prevent death
        loser.setHealth(maxHealth(loser));
        loser.setFoodLevel(20);
        loser.setSaturation(20f);
        for (PotionEffect eff : loser.getActivePotionEffects()) loser.removePotionEffect(eff.getType());
        loser.setGameMode(GameMode.SPECTATOR);

        if (winner != null) {
            winner.setHealth(maxHealth(winner));
            winner.setFoodLevel(20);
            winner.setSaturation(20f);
        }

        boolean matchOver = sWinner >= mode.rounds;

        if (!matchOver) {
            // Titles
            loser.sendTitle("§4§lRound Lost", "§fScore: §9" + sLoser + " §7- §c" + sWinner, 5, 40, 10);
            if (winner != null) {
                winner.sendTitle("§a§lRound Won", "§fScore: §9" + sWinner + " §7- §c" + sLoser, 5, 40, 10);
            }
            sendRoundChat(winner, loser, sWinner, sLoser);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (phase == Phase.ENDED) return;
                    Player a = Bukkit.getPlayer(p1);
                    Player b = Bukkit.getPlayer(p2);
                    if (a == null || b == null) { abandon(); return; }
                    beginRound();
                }
            }.runTaskLater(plugin, 60L);
        } else {
            endMatch(winner, loser, sWinner, sLoser);
        }
    }

    private void sendRoundChat(Player winner, Player loser, int sWinner, int sLoser) {
        int remaining = mode.rounds - sWinner;
        String winnerName = winner != null ? winner.getName() : "Opponent";
        for (Player target : new Player[]{winner, loser}) {
            if (target == null) continue;
            boolean targetIsWinner = (winner != null && target.getUniqueId().equals(winner.getUniqueId()));
            int s1 = targetIsWinner ? sWinner : sLoser;
            int s2 = targetIsWinner ? sLoser : sWinner;
            target.sendMessage("§eRound Results");
            target.sendMessage("§c/§9" + winnerName + " §fwon the round.");
            target.sendMessage("They need §e" + remaining + " §fmore to win.");
            target.sendMessage("§fScore: §c" + s1 + " §7- §9" + s2);
        }
    }

    private void endMatch(Player winner, Player loser, int sWinner, int sLoser) {
        phase = Phase.ENDED;
        
        // Clean up scoreboards
        if (scoreboardUpdateTask != null) scoreboardUpdateTask.cancel();
        if (scoreboardP1 != null) scoreboardP1.remove();
        if (scoreboardP2 != null) scoreboardP2.remove();
        
        // Clean up health nametags
        if (healthNametagP1 != null) healthNametagP1.remove();
        if (healthNametagP2 != null) healthNametagP2.remove();
        
        if (winner != null) {
            winner.sendTitle("§e§l\uD83C\uDFC6 VICTORY \uD83C\uDFC6", "§fScore: §c" + sWinner + " §7- §9" + sLoser, 10, 80, 20);
            winner.playSound(winner.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
            winner.sendMessage("§eMatch Complete");
            winner.sendMessage("§fScore: §c" + sWinner + " §7- §9" + sLoser);
        }
        loser.sendTitle("§4§lDEFEAT", "§fScore: §c" + sLoser + " §7- §9" + sWinner, 10, 80, 20);
        loser.playSound(loser.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
        loser.sendMessage("§eMatch Complete");
        loser.sendMessage("§fScore: §c" + sLoser + " §7- §9" + sWinner);

        new BukkitRunnable() {
            @Override
            public void run() {
                teleportOut();
                manager.endDuel(Duel.this);
            }
        }.runTaskLater(plugin, 60L);
    }

    public void handleQuit(Player quitter) {
        if (phase == Phase.ENDED) return;
        UUID otherId = quitter.getUniqueId().equals(p1) ? p2 : p1;
        Player other = Bukkit.getPlayer(otherId);
        phase = Phase.ENDED;
        
        // Clean up scoreboards
        if (countdownTask != null) countdownTask.cancel();
        if (scoreboardUpdateTask != null) scoreboardUpdateTask.cancel();
        if (scoreboardP1 != null) scoreboardP1.remove();
        if (scoreboardP2 != null) scoreboardP2.remove();
        
        // Clean up health nametags
        if (healthNametagP1 != null) healthNametagP1.remove();
        if (healthNametagP2 != null) healthNametagP2.remove();
        
        if (other != null && other.isOnline()) {
            other.sendTitle("§e§l\uD83C\uDFC6 VICTORY \uD83C\uDFC6", "§fOpponent left the match.", 10, 80, 20);
            other.playSound(other.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
            new BukkitRunnable() {
                @Override
                public void run() {
                    teleportOut();
                    manager.endDuel(Duel.this);
                }
            }.runTaskLater(plugin, 40L);
        } else {
            teleportOut();
            manager.endDuel(this);
        }
    }

    private void abandon() {
        if (phase == Phase.ENDED) return;
        phase = Phase.ENDED;
        if (countdownTask != null) countdownTask.cancel();
        if (scoreboardUpdateTask != null) scoreboardUpdateTask.cancel();
        if (scoreboardP1 != null) scoreboardP1.remove();
        if (scoreboardP2 != null) scoreboardP2.remove();
        if (healthNametagP1 != null) healthNametagP1.remove();
        if (healthNametagP2 != null) healthNametagP2.remove();
        teleportOut();
        manager.endDuel(this);
    }

    private void teleportOut() {
        for (UUID id : new UUID[]{p1, p2}) {
            Player p = Bukkit.getPlayer(id);
            if (p == null) continue;
            p.getInventory().clear();
            p.getInventory().setArmorContents(new ItemStack[4]);
            for (PotionEffect eff : p.getActivePotionEffects()) p.removePotionEffect(eff.getType());
            p.setGameMode(GameMode.ADVENTURE);
            p.setHealth(maxHealth(p));
            p.setFoodLevel(20);
            p.setSaturation(20f);
            manager.getLobbyManager().teleportToLobby(p);
        }
    }

    private double maxHealth(Player p) {
        try {
            return p.getAttribute(Attribute.MAX_HEALTH).getValue();
        } catch (Throwable t) {
            return 20.0;
        }
    }

    private void resetPlayer(Player p) {
        p.getInventory().clear();
        p.getInventory().setArmorContents(new ItemStack[4]);
        p.setHealth(maxHealth(p));
        p.setFoodLevel(20);
        p.setSaturation(20f);
        p.setGameMode(GameMode.SURVIVAL);
        p.setFireTicks(0);
        for (PotionEffect eff : p.getActivePotionEffects()) p.removePotionEffect(eff.getType());
    }

    private void applyHungerStart(Player p) {
        if (mode == GameModeConfig.SWORD) {
            // Sword: start with full hunger but ZERO saturation.
            // FoodLevelChangeEvent is cancelled (in DuelListener) until the first hit lands,
            // so even with 0 saturation the hunger bar won't tick down yet.
            p.setFoodLevel(20);
            p.setSaturation(0f);
            p.setExhaustion(0f);
        } else {
            p.setFoodLevel(20);
            p.setSaturation(20f);
            p.setExhaustion(0f);
        }
    }

    private void applyKit(Player p) {
        // Check if player has a custom kit saved for this gamemode
        KitConfig kitConfig = new KitConfig(plugin, p.getUniqueId(), mode);
        if (kitConfig.hasKit()) {
            kitConfig.applyKit(p.getInventory());
        } else {
            // Apply default kit
            if (mode == GameModeConfig.SWORD) {
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                sword.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
                p.getInventory().setItem(0, sword);
                p.getInventory().setHelmet(armor(Material.DIAMOND_HELMET, 4));
                p.getInventory().setChestplate(armor(Material.DIAMOND_CHESTPLATE, 4));
                p.getInventory().setLeggings(armor(Material.DIAMOND_LEGGINGS, 3));
                p.getInventory().setBoots(armor(Material.DIAMOND_BOOTS, 3));
            }
        }
    }

    private ItemStack armor(Material m, int protLevel) {
        ItemStack i = new ItemStack(m);
        i.addUnsafeEnchantment(Enchantment.PROTECTION, protLevel);
        i.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        return i;
    }
}
