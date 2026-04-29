package org.please.publicGoon;

public enum ArenaSize {
    SMALL("small"),
    AVERAGE("average"),
    LARGE("large");

    public final String prefix;

    ArenaSize(String prefix) {
        this.prefix = prefix;
    }
}
