package com.sportmanager.core;
<<<<<<< Updated upstream
=======
<<<<<<< HEAD

=======
>>>>>>> 500fd5138fc06faaeedc747d2b64dae2724e5e08
>>>>>>> Stashed changes
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base for a single match between two teams.
 * Concrete sport implementations provide segment-based simulation.
 */
public abstract class Match implements Serializable {

    protected Team homeTeam;
    protected Team awayTeam;
    protected int  homeScore;
    protected int  awayScore;
    protected int  weekNo;
    protected int  currentSegment;
    protected boolean finished;

    protected MatchResult       result;
    protected List<MatchSegment> segments;
    protected List<String>       events;

    protected Match(Team home, Team away, int weekNo) {
        this.homeTeam      = home;
        this.awayTeam      = away;
        this.weekNo        = weekNo;
        this.homeScore     = 0;
        this.awayScore     = 0;
        this.currentSegment = 0;
        this.finished      = false;
        this.segments      = new ArrayList<>();
        this.events        = new ArrayList<>();
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public Team getHomeTeam()  { return homeTeam; }
    public Team getAwayTeam()  { return awayTeam; }
    public int  getHomeScore() { return homeScore; }
    public int  getAwayScore() { return awayScore; }
    public int  getWeekNo()    { return weekNo; }
    public int  getCurrentSegment() { return currentSegment; }
    public boolean isFinished()     { return finished; }

    public MatchResult        getResult()   { return result; }
    public List<MatchSegment> getSegments() { return segments; }
    public List<String>       getEvents()   { return events; }

    public String getScore() {
        return homeScore + " - " + awayScore;
    }

    /** Display-friendly score string (same as getScore, kept for controller compatibility). */
    public String getScoreDisplay() { return getScore(); }

    /**
     * Returns the outcome string of a finished match.
     * Delegates to the MatchResult if available, otherwise computes inline.
     */
    public String getOutcome() {
        if (result != null) return result.getOutcome();
        if (!finished)      return "IN_PROGRESS";
        if (homeScore > awayScore) return MatchResult.HOME_WIN;
        if (awayScore > homeScore) return MatchResult.AWAY_WIN;
        return MatchResult.DRAW;
    }

    // ── Abstract contract ─────────────────────────────────────────────────────

    /** Simulate one segment (e.g. a half). Advances currentSegment internally. */
    public abstract void simulateSegment();

    /**
     * Begin simulating a segment in live mode. Default falls back to full segment simulation.
     * Concrete matches can override for true event-by-event progression.
     */
    public void beginSegmentSimulation() {
        simulateSegment();
    }

    /** True if the current segment still has event(s) to emit in live mode. */
    public boolean hasPendingSegmentEvents() {
        return false;
    }

    /**
     * Emits the next live event for the current segment.
     * Returns null when no pending segment event exists.
     */
    public String simulateNextSegmentEvent() {
        return null;
    }

    /** Total number of segments for this sport (e.g. 2 for football). */
    public abstract int getTotalSegments();

    /** Human-readable label for the given segment index (e.g. "First Half"). */
    public abstract String getSegmentLabel(int idx);

    /**
     * Returns true when the match is paused at a valid intervention point
     * (e.g. half-time) where the manager can change lineup or tactics.
     */
    public abstract boolean isAtBreak();

    /** Legacy hook kept for compatibility; delegates to simulateSegment by default. */
    public void playMatch() {
        while (!finished) simulateSegment();
    }

    /**
     * Begin simulating a segment in live mode. Default falls back to full segment simulation.
     * Concrete matches can override for true event-by-event progression.
     */
    public void beginSegmentSimulation() {
        simulateSegment();
    }

    /** True if the current segment still has event(s) to emit in live mode. */
    public boolean hasPendingSegmentEvents() {
        return false;
    }

    /**
     * Emits the next live event for the current segment.
     * Returns null when no pending segment event exists.
     */
    public String simulateNextSegmentEvent() {
        return null;
    }
}
