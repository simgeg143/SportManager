package com.sportmanager.core;

/**
 * Tracks a single player injury produced during a match.
 * As defined in the architecture document: player reference, games remaining,
 * description, and isRecovered() check.
 */
public class InjuryRecord {

    private final Player player;
    private int    gamesRemaining;
    private final String description;

    public InjuryRecord(Player player, int gamesRemaining, String description) {
        this.player          = player;
        this.gamesRemaining  = Math.max(0, gamesRemaining);
        this.description     = description;
    }

    public Player getPlayer()       { return player; }
    public int    getGamesRemaining() { return gamesRemaining; }
    public String getDescription()  { return description; }

    /** Returns true when the player has served their injury and is ready to return. */
    public boolean isRecovered() { return gamesRemaining <= 0; }

    /** Called at the start of each new match week; decrements the counter. */
    public void decrementGames() {
        if (gamesRemaining > 0) {
            gamesRemaining--;
            player.decrementInjury();
        }
    }

    @Override public String toString() {
        return player.getName() + " – " + description
                + " (" + gamesRemaining + " match(es) remaining)";
    }
}
