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
    protected List<Team>        teams;
    protected List<StandingEntry> standings;
    protected int currentWeek;

    protected League(String name) {
        this.name         = name;
        this.teams        = new ArrayList<>();
        this.standings = new ArrayList<>();
        this.currentWeek = 1;
    }

    // ── Season state ─────────────────────────────────────────────────────────

    public String     getName()         { return name; }
    public List<Team> getTeams()        { return teams; }

    public int getCurrentWeek() {
        return currentWeek;
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
