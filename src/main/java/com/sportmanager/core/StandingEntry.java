package com.sportmanager.core;

import java.io.Serializable;

/**
 * A snapshot of one team's standing in the league table.
 * Derives all values from the Team's accumulated season statistics.
 * Used by League.getTable() and displayed by StandingsController.
 * As defined in the architecture document.
 */
public class StandingEntry implements Comparable<StandingEntry>, Serializable {

    private final Team team;

    public StandingEntry(Team team) {
        this.team = team;
    }

    public StandingEntry(Team team, int position) {
        this(team);
        this.position = position;
    }

    // ── Delegated to Team ─────────────────────────────────────────────────────

    public Team   getTeam()           { return team; }
    public int    getPoints()         { return team.getPoints(); }
    public int    getWins()           { return team.getWins(); }
    public int    getDraws()          { return team.getDraws(); }
    public int    getLosses()         { return team.getLosses(); }
    public int    getGoalsFor()       { return team.getGoalsFor(); }
    public int    getGoalsAgainst()   { return team.getGoalsAgainst(); }
    public int    getGoalDifference() { return team.getGoalDifference(); }
    public int    getMatchesPlayed()  { return team.getMatchesPlayed(); }

    // ── Controller-friendly aliases ───────────────────────────────────────────

    public String getTeamName()              { return team.getName(); }
    public int    getPlayed()                { return getMatchesPlayed(); }
    public int    getWon()                   { return getWins(); }
    public int    getDrawn()                 { return getDraws(); }
    public int    getLost()                  { return getLosses(); }
    public String getGoalDifferenceDisplay() {
        int gd = getGoalDifference();
        return gd >= 0 ? "+" + gd : String.valueOf(gd);
    }

    /** 1-based position in the table (set externally by League.getTable()). */
    private int position;
    public int    getPosition()   { return position; }
    public void   setPosition(int p) { this.position = p; }

    public void addPoints(int p) { /* stats derive from Team — no-op */ }
    @Override
    public int compareTo(StandingEntry other) {
        int cmp = Integer.compare(other.getPoints(), this.getPoints());
        if (cmp != 0) return cmp;
        cmp = Integer.compare(other.getGoalDifference(), this.getGoalDifference());
        if (cmp != 0) return cmp;
        return Integer.compare(other.getGoalsFor(), this.getGoalsFor());
    }
}
