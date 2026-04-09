package com.sportmanager.core;

public class InjuryRecord {

    private Player player;
    private int gamesRemaining;

    public InjuryRecord(Player player, int gamesRemaining) {
        this.player = player;
        this.gamesRemaining = Math.max(0, gamesRemaining);
    }

    public Player getPlayer() {
        return player;
    }

    public int getGamesRemaining() {
        return gamesRemaining;
    }

    public boolean isRecovered() {
        return gamesRemaining == 0;
    }

    public void decrementGames() {
        if (gamesRemaining > 0) {
            gamesRemaining--;
        }
    }
}