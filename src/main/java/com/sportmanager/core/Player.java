package com.sportmanager.core;

import java.util.Map;

/**
 * Abstract base for every player across all sports.
 * Sport-specific attributes are exposed via {@link #getSpecificAttributes()}.
 */
public abstract class Player {

    protected String name;
    protected int age;
    protected String position;
    protected int skillLevel;          // 40–99
    protected int injuryMatchesRemaining; // 0 = healthy

    protected Player(String name, int age, String position, int skillLevel) {
        this.name = name;
        this.age = age;
        this.position = position;
        this.skillLevel = Math.max(40,Math.min(99,skillLevel));
        this.injuryMatchesRemaining = 0;
    }

    // ── Getters / setters ────────────────────────────────────────────────────

    public String getName()     { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    public String getPosition() { return position; }
    public int getSkillLevel()  { return skillLevel; }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setSkillLevel(int skillLevel) {
        this.skillLevel = skillLevel;
    }

    public int  getInjuryMatchesRemaining()             { return injuryMatchesRemaining; }
    public void setInjuryMatchesRemaining(int matches)  { this.injuryMatchesRemaining = Math.max(0, matches); }

    public boolean isInjured(){
        return injuryMatchesRemaining>0;
    }
    public boolean isAvailable(){
        return !isInjured();
    }
    public void recover(){
        if(injuryMatchesRemaining>0){
            injuryMatchesRemaining--;
        }
    }
    public void train(){
        skillLevel = Math.min(99,skillLevel+1);
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
