package com.sportmanager.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract base for every team across all sports.
 * Tracks the full squad (roster), coaching staff, the selected lineup,
 * the current tactic string, and accumulated season statistics.
 */
public abstract class Team implements Serializable {

    protected String name;
    protected String logoPath;

    // ── Squad ─────────────────────────────────────────────────────────────────
    protected List<Player> roster;
    protected List<Player> startingLineup;
    protected List<Player> substitutes;
    protected List<Coach>  coaches;
    protected Lineup       currentLineup;
    protected Tactic       currentTactic;   // formation string, e.g. "4-3-3"

    // ── Season statistics ─────────────────────────────────────────────────────
    protected int wins;
    protected int draws;
    protected int losses;
    protected int scoreFor;
    protected int scoreAgainst;

    protected Team(String name) {
        this.name           = name;
        this.roster         = new ArrayList<>();
        this.startingLineup = new ArrayList<>();
        this.substitutes    = new ArrayList<>();
        this.coaches        = new ArrayList<>();
        this.currentTactic  =
                new Tactic(
                        "Balanced",
                        "Balanced"
                );
    }

    // ── Identity ──────────────────────────────────────────────────────────────

    public String getName()     { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    @Override public String toString() { return name; }

    // ── Squad access ──────────────────────────────────────────────────────────

    public List<Player> getPlayers()        { return roster; }
    public List<Player> getRoster()         { return roster; }
    public List<Player> getStartingLineup() { return startingLineup; }
    public List<Player> getSubstitutes()    { return substitutes; }
    public List<Coach>  getCoaches()        { return coaches; }

    public void addPlayer(Player player)    { roster.add(player); }
    public void removePlayer(Player player) { roster.remove(player); }
    public void addCoach(Coach coach)       { coaches.add(coach); }

    public List<Player> getAvailablePlayers() {
        return roster.stream().filter(Player::isAvailable).collect(Collectors.toList());
    }

    public double getAverageSkill() {
        return roster.stream().mapToInt(Player::getSkillLevel).average().orElse(0.0);
    }

    // ── Tactic ────────────────────────────────────────────────────────────────

    public Tactic getCurrentTactic()              { return currentTactic; }
    public void   setCurrentTactic(Tactic tactic) { this.currentTactic = tactic; }

    /** Formation / offensive set name for UI and simulation (e.g. {@code "4-4-2"}, {@code "5-Out"}). */
    public String getCurrentTacticName() {
        return currentTactic != null ? currentTactic.getName() : "";
    }

    /** Updates formation name while preserving existing style when possible. */
    public void setCurrentTactic(String formationName) {
        if (formationName == null) return;
        String style = currentTactic != null ? currentTactic.getStyle() : "Balanced";
        this.currentTactic = new Tactic(formationName, style);
    }

    // ── Lineup ────────────────────────────────────────────────────────────────

    public Lineup getLineup()          { return currentLineup; }
    public void   setLineup(Lineup l)  { this.currentLineup = l; }

    public boolean hasValidLineup() {
        return currentLineup != null && currentLineup.isValid(getRequiredLineupSize());
    }

    /** Populate startingLineup and substitutes from the current roster. */
    public abstract void generateDefaultLineup();

    /** Sport-specific minimum number of starting players required. */
    public abstract int getRequiredLineupSize();

    /** Sport-specific maximum number of substitutes allowed on the bench. */
    public abstract int getMaxSubstituteCount();

    // ── Season statistics ─────────────────────────────────────────────────────

    public int getWins()          { return wins; }
    public int getDraws()         { return draws; }
    public int getLosses()        { return losses; }
    public int getScoreFor()      { return scoreFor; }
    public int getScoreAgainst()  { return scoreAgainst; }
    public int getScoreDifference(){ return scoreFor - scoreAgainst; }
    public int getMatchesPlayed() { return wins + draws + losses; }
    public int getPoints()        { return wins * 3 + draws; }

    public void recordWin(int scored, int conceded) {
        wins++;
        scoreFor      += scored;
        scoreAgainst  += conceded;
    }

    public void recordDraw(int scored, int conceded) {
        draws++;
        scoreFor     += scored;
        scoreAgainst += conceded;
    }

    public void recordLoss(int scored, int conceded) {
        losses++;
        scoreFor     += scored;
        scoreAgainst += conceded;
    }

    public void resetStats() {
        wins = draws = losses = scoreFor = scoreAgainst = 0;
    }
}
