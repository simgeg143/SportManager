package com.sportmanager.core;

import java.util.Map;

/**
 * Abstract base for every player across all sports.
 * Sport-specific attributes are exposed via {@link #getSpecificAttributes()}.
 */
public abstract class Player {

    protected String name;
    protected String position;
    protected int skillLevel;          // 40–99
    protected int injuryMatchesRemaining; // 0 = healthy

    protected Player(String name, String position, int skillLevel) {
        this.name = name;
        this.position = position;
        this.skillLevel = Math.max(40, Math.min(99, skillLevel));
        this.injuryMatchesRemaining = 0;
    }

    // ── Getters / setters ────────────────────────────────────────────────────

    public String getName()     { return name; }
    public String getPosition() { return position; }
    public int getSkillLevel()  { return skillLevel; }

    public int  getInjuryMatchesRemaining()             { return injuryMatchesRemaining; }
    public void setInjuryMatchesRemaining(int matches)  { this.injuryMatchesRemaining = Math.max(0, matches); }

    public boolean isInjured() { return injuryMatchesRemaining > 0; }

    /** Called at the start of each new week to count down injury duration. */
    public void decrementInjury() {
        if (injuryMatchesRemaining > 0) injuryMatchesRemaining--;
    }

    // ── Abstract API ─────────────────────────────────────────────────────────

    /** Sport-specific stats shown in the detail panel (e.g. PAC/SHO/PAS). */
    public abstract Map<String, Integer> getSpecificAttributes();

    /** Short status string for the squad table (e.g. "FIT", "INJ (2)"). */
    public abstract String getStatusDisplay();

    @Override
    public String toString() {
        return name + " (" + position + ", " + skillLevel + ")";
    }
}
