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
    protected List<Coach>  coaches;          // coaching staff
    protected Lineup currentLineup;
    protected Tactic currentTactic;

    protected Team(String name) {
        this.name           = name;
        this.roster         = new ArrayList<>();
        this.coaches        = new ArrayList<>();
    }

    // ── Identity ─────────────────────────────────────────────────────────────

    public String getName()    { return name; }

    // ── Squad management ─────────────────────────────────────────────────────

    public List<Player> getPlayers()         { return roster; }
    public List<Coach>  getCoaches()        { return coaches; }
    public void addCoach(Coach coach) {
        coaches.add(coach);
    }

    public Tactic getCurrentTactic(){
        return currentTactic;
    }
    public void setCurrentTactic(Tactic tactic) {
        this.currentTactic = tactic;
    }
    public void addPlayer(Player player) {
        roster.add(player);
    }
    public void removePlayer(Player player) {
        roster.remove(player);
    }

    /**
     * Returns a Lineup wrapper around the current starting XI and substitutes.
     * Controllers use this for lineup validation (isValid()).
     */
    public Lineup getLineup() {
        return currentLineup;
    }
    public void setLineup(Lineup lineup) {
        this.currentLineup = lineup;
    }

    /**
     * Returns only players who are not currently injured.
     * Used to filter the selection list (TM-1, IM-2).
     */
    public List<Player> getAvailablePlayers() {
        return roster.stream()
                .filter(Player::isAvailable)
                .collect(Collectors.toList());
    }

    public double getAverageSkill() {
        return roster.stream().mapToInt(Player::getSkillLevel).average().orElse(0.0);
    }

    /** @return true if starting XI is full and contains no injured players. */
    public boolean hasValidLineup() {
        if (currentLineup==null){
            return false;
        }
        return currentLineup.isValid(getRequiredLineupSize());
    }
    public abstract int getRequiredLineupSize();
    @Override public String toString() { return name; }
}
