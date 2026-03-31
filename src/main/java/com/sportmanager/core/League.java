package com.sportmanager.core;

import java.util.ArrayList;
import java.util.Collections;
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
    protected List<StandingEntry> standings;
    protected int currentWeek;
    protected List<List<Match>> rounds;      // outer = matchday, inner = that day's matches
    protected int               currentRound; // 0-based

    protected League(String name, Sport sport) {
        this.name         = name;
        this.sport        = sport;
        this.teams        = new ArrayList<>();
        this.rounds       = new ArrayList<>();
        this.standings = new ArrayList<>();
        this.currentRound = 0;
        this.currentWeek = 1;
    }

    // ── Season state ─────────────────────────────────────────────────────────

    public String     getName()         { return name; }
    public Sport      getSport()        { return sport; }
    public List<Team> getTeams()        { return teams; }
    public int        getCurrentRound() { return currentRound; }

    public int getCurrentWeek() {
        return currentWeek;
    }

    public int        getTotalRounds()  { return rounds.size(); }
    public boolean    isSeasonOver()    { return currentRound >= rounds.size(); }

    public List<Match> getCurrentRoundMatches() {
        return getRoundMatches(currentRound);
    }

    public List<Match> getRoundMatches(int round) {
        if (round >= 0 && round < rounds.size()) return rounds.get(round);
        return new ArrayList<>();
    }
    public void addTeam(Team team){
        teams.add(team);
        standings.add(new StandingEntry(team));
    }
    public List<StandingEntry> getTable(){
        Collections.sort(standings);
        return standings;
    }
    public void updateStandings(Team team, int points) {
        for (StandingEntry entry : standings) {
            if (entry.getTeam().equals(team)) {
                entry.addPoints(points);
            }
        }
    }
    public void advanceWeek() {
        currentWeek++;
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
    public abstract List<Team> getSortedStandings();

    /** Returns the team leading the table (champion at end of season). */
    public abstract Team getChampion();
}
