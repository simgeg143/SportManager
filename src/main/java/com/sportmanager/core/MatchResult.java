package com.sportmanager.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable final outcome of a completed match.
 * Stores the final score, the winning team reference (null for a draw),
 * and all InjuryRecords produced during the match.
 * As defined in the architecture document.
 */
public class MatchResult {

    public static final String HOME_WIN = "HOME_WIN";
    public static final String AWAY_WIN = "AWAY_WIN";
    public static final String DRAW     = "DRAW";

    private final Team   homeTeam;
    private final Team   awayTeam;
    private final int    homeScore;
    private final int    awayScore;
    private final String outcome;           // HOME_WIN | AWAY_WIN | DRAW
    private final List<InjuryRecord> injuries;

    public MatchResult(Team homeTeam, Team awayTeam,
                       int homeScore, int awayScore,
                       List<InjuryRecord> injuries) {
        this.homeTeam  = homeTeam;
        this.awayTeam  = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.injuries  = new ArrayList<>(injuries);
        this.outcome   = homeScore > awayScore ? HOME_WIN
                       : homeScore < awayScore ? AWAY_WIN
                       : DRAW;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public Team   getHomeTeam()  { return homeTeam; }
    public Team   getAwayTeam()  { return awayTeam; }
    public int    getHomeScore() { return homeScore; }
    public int    getAwayScore() { return awayScore; }
    public String getOutcome()   { return outcome; }

    /** @return the winning team, or {@code null} for a draw. */
    public Team getWinner() {
        return switch (outcome) {
            case HOME_WIN -> homeTeam;
            case AWAY_WIN -> awayTeam;
            default       -> null;
        };
    }

    public List<InjuryRecord> getInjuries() { return injuries; }

    public String getScoreDisplay() {
        return homeScore + " – " + awayScore;
    }

    public String getSummary() {
        return homeTeam.getName() + "  " + homeScore
                + " – " + awayScore + "  " + awayTeam.getName();
    }

    @Override public String toString() { return getSummary(); }
}
