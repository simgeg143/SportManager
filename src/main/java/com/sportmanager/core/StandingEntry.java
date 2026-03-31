package com.sportmanager.core;

/**
 * A snapshot of one team's standing in the league table.
 * Derives all values from the Team's accumulated season statistics.
 * Used by League.getTable() and displayed by StandingsController.
 * As defined in the architecture document.
 */
public class StandingEntry implements Comparable<StandingEntry> {

    private final Team team;
    private int points;

    public StandingEntry(Team team) {
        this.team     = team;
        this.points = 0;
    }

    // ── Delegated to Team ─────────────────────────────────────────────────────

    public Team   getTeam()           { return team; }
    public int    getPoints()         { return points; }


    public void addPoints(int p) {
        this.points += p;
    }
    @Override
    public int compareTo(StandingEntry other) {
        return Integer.compare(other.getPoints(), this.getPoints());
    }
}
