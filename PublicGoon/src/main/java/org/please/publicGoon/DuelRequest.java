package org.please.publicGoon;

import java.util.UUID;

public class DuelRequest {
    private final UUID requesterUuid;
    private final UUID targetUuid;
    private final GameModeConfig gameMode;
    private final int rounds;
    private final ArenaSize mapSize;
    private final long timestamp;

    public DuelRequest(UUID requesterUuid, UUID targetUuid, GameModeConfig gameMode, int rounds, ArenaSize mapSize) {
        this.requesterUuid = requesterUuid;
        this.targetUuid = targetUuid;
        this.gameMode = gameMode;
        this.rounds = rounds;
        this.mapSize = mapSize;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getRequesterUuid() {
        return requesterUuid;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public GameModeConfig getGameMode() {
        return gameMode;
    }

    public int getRounds() {
        return rounds;
    }

    public ArenaSize getMapSize() {
        return mapSize;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > 60000; // 60 seconds expiry
    }
}
