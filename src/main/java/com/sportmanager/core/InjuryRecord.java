package com.sportmanager.core;

/** Tracks a single player's injury and how many matches they will miss. */
public class InjuryRecord {

    private final Player player;
    private int    gamesRemaining;
    private final String description;

    public InjuryRecord(Player player, int gamesRemaining, String description) {
        this.player          = player;
        this.gamesRemaining  = Math.max(0, gamesRemaining);
        this.description     = description != null ? description : "Injury";
    }

    public InjuryRecord(Player player, int gamesRemaining) {
        this(player, gamesRemaining, "Injury");
    }

    public Player getPlayer()          { return player; }
    public int    getGamesRemaining()  { return gamesRemaining; }
    public String getDescription()     { return description; }

    public boolean isRecovered() { return gamesRemaining == 0; }

    public void decrementGames() {
        if (gamesRemaining > 0) gamesRemaining--;
        if (isRecovered()) player.setInjuryMatchesRemaining(0);
    }

    @Override
    public String toString() {
        String games = gamesRemaining == 1 ? "1 match" : gamesRemaining + " matches";
        return player.getName() + " (" + player.getPosition() + ")  —  out for " + games;
    }
}
