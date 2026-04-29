package org.please.publicGoon;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class DuelScoreboard {
    private final Scoreboard scoreboard;
    private final Objective objective;
    private final Player player;
    private final Player opponent;
    private final GameModeConfig mode;
    
    private Score timeScore;
    private Score opponentScore;
    private Score pingScore;
    private Score yourHealthScore;
    private Score opponentHealthScore;
    private Score roundScore;
    private Score blank1;
    private Score blank2;
    
    private long startTime;
    
    private String lastTimeText;
    private String lastPingText;
    private String lastYourHealthText;
    private String lastOpponentHealthText;
    private String lastRoundText;
    
    public DuelScoreboard(Player player, Player opponent, GameModeConfig mode) {
        this.player = player;
        this.opponent = opponent;
        this.mode = mode;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        
        this.objective = scoreboard.registerNewObjective("duel", "dummy", "§9§l⚔ DUEL");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        initializeScores();
        this.startTime = System.currentTimeMillis();
        
        player.setScoreboard(scoreboard);
    }
    
    private void initializeScores() {
        blank1 = objective.getScore(" ");
        blank1.setScore(8);
        
        opponentScore = objective.getScore("§eOpponent: §f" + opponent.getName());
        opponentScore.setScore(7);
        
        pingScore = objective.getScore("§7Ping: §f" + getPing() + "ms");
        pingScore.setScore(6);
        
        blank2 = objective.getScore("  ");
        blank2.setScore(5);
        
        yourHealthScore = objective.getScore("§aYour HP: §f" + getHealth(player));
        yourHealthScore.setScore(4);
        
        opponentHealthScore = objective.getScore("§cTheir HP: §f" + getHealth(opponent));
        opponentHealthScore.setScore(3);
        
        roundScore = objective.getScore("§7Round: §f-");
        roundScore.setScore(2);
        
        timeScore = objective.getScore("§7Time: §f0:00");
        timeScore.setScore(1);
    }
    
    public void update(int scoreP1, int scoreP2, int round) {
        // Update time
        long elapsed = System.currentTimeMillis() - startTime;
        int seconds = (int) (elapsed / 1000);
        int minutes = seconds / 60;
        int secs = seconds % 60;
        String timeText = "§7Time: §f" + minutes + ":" + String.format("%02d", secs);
        if (lastTimeText != null) scoreboard.resetScores(lastTimeText);
        timeScore = objective.getScore(timeText);
        timeScore.setScore(1);
        lastTimeText = timeText;
        
        // Update ping
        String pingText = "§7Ping: §f" + getPing() + "ms";
        if (lastPingText != null) scoreboard.resetScores(lastPingText);
        pingScore = objective.getScore(pingText);
        pingScore.setScore(6);
        lastPingText = pingText;
        
        // Update health
        String yourHealthText = "§aYour HP: §f" + getHealth(player);
        if (lastYourHealthText != null) scoreboard.resetScores(lastYourHealthText);
        yourHealthScore = objective.getScore(yourHealthText);
        yourHealthScore.setScore(4);
        lastYourHealthText = yourHealthText;
        
        String opponentHealthText = "§cTheir HP: §f" + getHealth(opponent);
        if (lastOpponentHealthText != null) scoreboard.resetScores(lastOpponentHealthText);
        opponentHealthScore = objective.getScore(opponentHealthText);
        opponentHealthScore.setScore(3);
        lastOpponentHealthText = opponentHealthText;
        
        // Update round/score
        String scoreText = "§f" + scoreP1 + " §7- §f" + scoreP2;
        String roundText = "§7Score: " + scoreText + " §7(Round " + round + ")";
        if (lastRoundText != null) scoreboard.resetScores(lastRoundText);
        roundScore = objective.getScore(roundText);
        roundScore.setScore(2);
        lastRoundText = roundText;
    }
    
    private int getPing() {
        try {
            Object entityPlayer = opponent.getClass().getMethod("getHandle").invoke(opponent);
            return (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
        } catch (Exception e) {
            return -1;
        }
    }
    
    private double getHealth(Player p) {
        if (p == null || !p.isOnline()) return 0;
        return Math.round(p.getHealth() * 10.0) / 10.0;
    }
    
    public void remove() {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}
