package com.sportmanager.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base for a full-season league competition.
 * Holds all teams, the generated fixture rounds, and the current round pointer.
 *
 * Concrete sport leagues implement fixture generation and standings sorting.
 */
public abstract class League implements Serializable {

    protected String             name;
    protected Sport              sport;
    protected List<Team>         teams;
    protected List<List<Match>>  rounds;
    protected int                currentRound;

    protected League(String name, Sport sport) {
        this.name         = name;
        this.sport        = sport;
        this.teams        = new ArrayList<>();
        this.rounds       = new ArrayList<>();
        this.currentRound = 0;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String     getName()  { return name; }
    public List<Team> getTeams() { return teams; }
    public Sport      getSport() { return sport; }
    public int        getCurrentRound() { return currentRound; }

    public int getTotalRounds() { return rounds.size(); }

    /**
     * Returns all matches scheduled for the given 1-based week number.
     * Returns an empty list if the week is out of range.
     */
    public List<Match> getMatchesOfWeek(int weekNo) {
        int idx = weekNo - 1;
        if (idx < 0 || idx >= rounds.size()) return new ArrayList<>();
        return rounds.get(idx);
    }

    /** Advances the internal round pointer by one. */
    public void advanceRound() { currentRound++; }

    /** Returns all matches in the current round. */
    public List<Match> getCurrentRoundMatches() {
        return getMatchesOfWeek(currentRound + 1);
    }

    /** Returns true when all rounds have been played. */
    public boolean isSeasonOver() {
        return currentRound >= rounds.size();
    }

    // ── Abstract contract ─────────────────────────────────────────────────────

    /** Generates the complete fixture schedule and populates {@code rounds}. */
    public abstract void generateFixtures();

    /** Returns the full standings table, each entry wrapping a team and its position. */
    public abstract List<StandingEntry> getTable();

    /** Updates both teams' season statistics based on the given match result. */
    public abstract void updateStandings(MatchResult result);

    /** Returns teams sorted by the sport-specific tiebreaker rules. */
    public abstract List<Team> getSortedStandings();

    /** Returns the current league leader (champion candidate). */
    public abstract Team getChampion();
}
