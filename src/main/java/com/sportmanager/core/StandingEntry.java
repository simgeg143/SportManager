package com.sportmanager.core;

/**
 * A snapshot of one team's standing in the league table.
 * Derives all values from the Team's accumulated season statistics.
 * Used by League.getTable() and displayed by StandingsController.
 * As defined in the architecture document.
 */
public class StandingEntry {

    private final Team team;
    private final int  position;

    public StandingEntry(Team team, int position) {
        this.team     = team;
        this.position = position;
    }

    // ── Delegated to Team ─────────────────────────────────────────────────────

    public Team   getTeam()           { return team; }
    public String getTeamName()       { return team.getName(); }
    public int    getPosition()       { return position; }
    public int    getPlayed()         { return team.getMatchesPlayed(); }
    public int    getWon()            { return team.getWins(); }
    public int    getDrawn()          { return team.getDraws(); }
    public int    getLost()           { return team.getLosses(); }
    public int    getGoalsFor()       { return team.getGoalsFor(); }
    public int    getGoalsAgainst()   { return team.getGoalsAgainst(); }
    public int    getGoalDifference() { return team.getGoalDifference(); }
    public int    getPoints()         { return team.getPoints(); }

    public String getGoalDifferenceDisplay() {
        int gd = getGoalDifference();
        return (gd >= 0 ? "+" : "") + gd;
    }

    @Override public String toString() {
        return position + ". " + team.getName()
                + "  P=" + getPlayed()
                + "  W=" + getWon()
                + "  D=" + getDrawn()
                + "  L=" + getLost()
                + "  GD=" + getGoalDifferenceDisplay()
                + "  Pts=" + getPoints();
    }
}
