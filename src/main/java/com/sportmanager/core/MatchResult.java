package com.sportmanager.core;
import java.io.Serializable;

import java.io.Serializable;
import java.util.List;

/**
 * Immutable record of a completed match's outcome.
 * Holds final scores, the outcome constant, the winner reference,
 * and any injuries sustained during the match.
 */
public class MatchResult implements Serializable {

    public static final String HOME_WIN = "HOME_WIN";
    public static final String AWAY_WIN = "AWAY_WIN";
    public static final String DRAW     = "DRAW";

    private final Team              homeTeam;
    private final Team              awayTeam;
    private final int               homeScore;
    private final int               awayScore;
    private final String            outcome;
    private final List<InjuryRecord> injuries;

    public MatchResult(Team homeTeam, Team awayTeam,
                       int homeScore, int awayScore,
                       List<InjuryRecord> injuries) {
        this.homeTeam  = homeTeam;
        this.awayTeam  = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.injuries  = injuries != null ? injuries : List.of();
        this.outcome   = homeScore > awayScore ? HOME_WIN
                       : awayScore > homeScore ? AWAY_WIN
                       : DRAW;
    }

    /** Legacy constructor without injuries (kept for backward compatibility). */
    public MatchResult(Team homeTeam, Team awayTeam, int homeScore, int awayScore) {
        this(homeTeam, awayTeam, homeScore, awayScore, List.of());
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public Team   getHomeTeam()  { return homeTeam; }
    public Team   getAwayTeam()  { return awayTeam; }
    public int    getHomeScore() { return homeScore; }
    public int    getAwayScore() { return awayScore; }
    public String getOutcome()   { return outcome; }
    public List<InjuryRecord> getInjuries() { return injuries; }

    public Team getWinner() {
        return switch (outcome) {
            case HOME_WIN -> homeTeam;
            case AWAY_WIN -> awayTeam;
            default       -> null;  // draw
        };
    }

    public boolean isDraw() { return DRAW.equals(outcome); }

    public String getScore() {
        return homeScore + " - " + awayScore;
    }
}
