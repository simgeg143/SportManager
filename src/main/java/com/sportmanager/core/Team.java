package com.sportmanager.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract base for every team across all sports.
 * Tracks the full squad (roster), coaching staff, the selected lineup,
 * the current tactic, and accumulated season statistics.
 */
public abstract class Team {

    protected String name;
    protected String logoPath;

    protected List<Player> roster;
    protected List<Player> startingLineup;
    protected List<Player> substitutes;
    protected List<Coach>  coaches;          // coaching staff
    protected String       currentTactic;    // tactic name string (e.g. "4-3-3")

    // Season statistics
    protected int wins, draws, losses;
    protected int goalsFor, goalsAgainst;

    protected Team(String name) {
        this.name           = name;
        this.roster         = new ArrayList<>();
        this.startingLineup = new ArrayList<>();
        this.substitutes    = new ArrayList<>();
        this.coaches        = new ArrayList<>();
    }

    // ── Identity ─────────────────────────────────────────────────────────────

    public String getName()    { return name; }
    public String getLogoPath() { return logoPath; }
    public void   setLogoPath(String path) { this.logoPath = path; }

    // ── Squad management ─────────────────────────────────────────────────────

    public List<Player> getRoster()         { return roster; }
    public List<Player> getStartingLineup() { return startingLineup; }
    public List<Player> getSubstitutes()    { return substitutes; }
    public List<Coach>  getCoaches()        { return coaches; }

    public String getCurrentTactic()         { return currentTactic; }
    public void   setCurrentTactic(String t) { this.currentTactic = t; }

    /**
     * Returns a Lineup wrapper around the current starting XI and substitutes.
     * Controllers use this for lineup validation (isValid()).
     */
    public Lineup getLineup() {
        return new Lineup(startingLineup, substitutes);
    }

    /**
     * Returns only players who are not currently injured.
     * Used to filter the selection list (TM-1, IM-2).
     */
    public List<Player> getAvailablePlayers() {
        return roster.stream()
                .filter(p -> !p.isInjured())
                .collect(Collectors.toList());
    }

    public double getAverageSkill() {
        return roster.stream().mapToInt(Player::getSkillLevel).average().orElse(0.0);
    }

    /** @return true if starting XI is full and contains no injured players. */
    public boolean hasValidLineup() {
        return getLineup().isValid(getRequiredLineupSize());
    }

    // ── Season statistics ────────────────────────────────────────────────────

    public int getWins()           { return wins; }
    public int getDraws()          { return draws; }
    public int getLosses()         { return losses; }
    public int getGoalsFor()       { return goalsFor; }
    public int getGoalsAgainst()   { return goalsAgainst; }
    public int getGoalDifference() { return goalsFor - goalsAgainst; }
    public int getMatchesPlayed()  { return wins + draws + losses; }
    public int getPoints()         { return wins * 3 + draws; }

    public void recordWin(int scored, int conceded) {
        wins++; goalsFor += scored; goalsAgainst += conceded;
    }
    public void recordDraw(int scored, int conceded) {
        draws++; goalsFor += scored; goalsAgainst += conceded;
    }
    public void recordLoss(int scored, int conceded) {
        losses++; goalsFor += scored; goalsAgainst += conceded;
    }

    // ── Abstract sport-specific rules ────────────────────────────────────────

    public abstract int  getRequiredLineupSize();
    public abstract int  getMaxSubstituteCount();
    public abstract void generateDefaultLineup();

    @Override public String toString() { return name; }
}
