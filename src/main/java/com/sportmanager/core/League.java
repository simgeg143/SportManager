package com.sportmanager.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base for a full-season league competition.
 * Holds all teams, the generated fixture rounds, and the current round pointer.
 *
 * Extended in the architecture document with:
 *  – getMatchesOfWeek(weekNo)  (TM-2 fixture view)
 *  – getTable()                (LM-7 standings, returns List<StandingEntry>)
 *  – updateStandings(result)   (LM-8, called after every match)
 */
public abstract class League {

    protected String            name;
    protected Sport             sport;
    protected List<Team>        teams;
    protected List<List<Match>> rounds;      // outer = matchday, inner = that day's matches
    protected int               currentRound; // 0-based

    protected League(String name, Sport sport) {
        this.name         = name;
        this.sport        = sport;
        this.teams        = new ArrayList<>();
        this.rounds       = new ArrayList<>();
        this.currentRound = 0;
    }

    // ── Season state ─────────────────────────────────────────────────────────

    public String     getName()         { return name; }
    public Sport      getSport()        { return sport; }
    public List<Team> getTeams()        { return teams; }
    public int        getCurrentRound() { return currentRound; }
    public int        getTotalRounds()  { return rounds.size(); }
    public boolean    isSeasonOver()    { return currentRound >= rounds.size(); }

    public List<Match> getCurrentRoundMatches() {
        return getRoundMatches(currentRound);
    }

    public List<Match> getRoundMatches(int round) {
        if (round >= 0 && round < rounds.size()) return rounds.get(round);
        return new ArrayList<>();
    }

    /**
     * Returns all matches for the given 1-based week number.
     * Called by FixtureController / SportManager.showFixtureData(). (TM-2)
     */
    public List<Match> getMatchesOfWeek(int weekNo) {
        return getRoundMatches(weekNo - 1);   // weekNo is 1-based, rounds is 0-based
    }

    public void advanceRound() {
        if (!isSeasonOver()) currentRound++;
    }

    // ── Abstract API ─────────────────────────────────────────────────────────

    /** Generates the full home-and-away round-robin fixture list. */
    public abstract void generateFixtures();

    /**
     * Returns the current league standings as a sorted list of StandingEntry.
     * Ordered by Pts → GD → GF (sport-specific tie-breaking may differ). (LM-7)
     */
    public abstract List<StandingEntry> getTable();

    /**
     * Applies a MatchResult to the two teams' season statistics. (LM-8)
     * Called by GameSession/SportManager after every match is finalised.
     */
    public abstract void updateStandings(MatchResult result);

    /** Returns teams ordered by points for convenience. */
    public abstract List<Team> getSortedStandings();

    /** Returns the team leading the table (champion at end of season). */
    public abstract Team getChampion();
}
