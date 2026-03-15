package com.sportmanager.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base for a single match between two teams.
 * Stores context (weekNo, teams), running scores, match segments,
 * and the final MatchResult once the match is complete.
 *
 * Concrete implementations define how each segment is simulated and
 * what scoring rules apply (e.g. FootballMatch uses two halves with
 * goal-based probability).
 */
public abstract class Match {

    protected Team   homeTeam;
    protected Team   awayTeam;
    protected int    homeScore;
    protected int    awayScore;
    protected int    weekNo;              // which matchday this fixture belongs to
    protected int    currentSegment;      // 0-based; equals getTotalSegments() when finished
    protected boolean finished;

    protected List<MatchSegment> segments;  // one entry per simulated segment
    protected List<String>       events;    // flat event list (kept for backward compat)
    protected MatchResult        result;    // set when match is finished

    protected Match(Team home, Team away, int weekNo) {
        this.homeTeam       = home;
        this.awayTeam       = away;
        this.weekNo         = weekNo;
        this.homeScore      = 0;
        this.awayScore      = 0;
        this.currentSegment = 0;
        this.finished       = false;
        this.segments       = new ArrayList<>();
        this.events         = new ArrayList<>();
        this.result         = null;
    }

    // ── Read-only state ───────────────────────────────────────────────────────

    public Team          getHomeTeam()       { return homeTeam; }
    public Team          getAwayTeam()       { return awayTeam; }
    public int           getHomeScore()      { return homeScore; }
    public int           getAwayScore()      { return awayScore; }
    public int           getWeekNo()         { return weekNo; }
    public int           getCurrentSegment() { return currentSegment; }
    public boolean       isFinished()        { return finished; }
    public List<MatchSegment> getSegments()  { return segments; }
    public List<String>  getEvents()         { return events; }
    public MatchResult   getResult()         { return result; }

    public String getScoreDisplay() {
        return homeScore + " – " + awayScore;
    }

    /** Returns "HOME_WIN", "AWAY_WIN", or "DRAW". Valid after finish. */
    public String getOutcome() {
        if (homeScore > awayScore) return MatchResult.HOME_WIN;
        if (homeScore < awayScore) return MatchResult.AWAY_WIN;
        return MatchResult.DRAW;
    }

    /** Convenience: true when the managed team won (requires knowing which side they are). */
    public boolean didTeamWin(Team team) {
        if (team == homeTeam) return homeScore > awayScore;
        if (team == awayTeam) return awayScore > homeScore;
        return false;
    }

    // ── Abstract simulation API ───────────────────────────────────────────────

    /**
     * Simulates the next segment.
     * Implementations append a MatchSegment and add events.
     */
    public abstract void simulateSegment();

    /** Total number of segments in this match type. */
    public abstract int getTotalSegments();

    /**
     * Human-readable label for the given segment index
     * (e.g. "First Half", "Second Half", "Q1", "Q2").
     */
    public abstract String getSegmentLabel(int segmentIndex);

    /**
     * Returns true when one segment has just finished but the match has not
     * ended — i.e. there is a break for tactical changes and substitutions.
     */
    public abstract boolean isAtBreak();
}
