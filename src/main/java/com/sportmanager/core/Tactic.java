package com.sportmanager.core;

/**
 * Represents a tactical configuration for a team.
 * Stores the formation name, shape description, and attack/defence bias values.
 * As defined in the architecture document: name, shape, offenseLevel, defenseLevel.
 */
public class Tactic {

    private final String name;         // e.g. "4-3-3"
    private final String shape;        // e.g. "Attacking", "Balanced", "Defensive"
    private final int    offenseLevel; // 1–10
    private final int    defenseLevel; // 1–10
    private final String description;

    public Tactic(String name, String shape, int offenseLevel, int defenseLevel, String description) {
        this.name         = name;
        this.shape        = shape;
        this.offenseLevel = Math.max(1, Math.min(10, offenseLevel));
        this.defenseLevel = Math.max(1, Math.min(10, defenseLevel));
        this.description  = description;
    }

    public String getName()         { return name; }
    public String getShape()        { return shape; }
    public int    getOffenseLevel() { return offenseLevel; }
    public int    getDefenseLevel() { return defenseLevel; }
    public String getDescription()  { return description; }

    /** Attack multiplier used by the match simulation engine (1.0 = baseline). */
    public double getAttackMultiplier()  { return 0.8 + offenseLevel * 0.04; }
    /** Defence multiplier used by the match simulation engine (1.0 = baseline). */
    public double getDefenceMultiplier() { return 0.8 + defenseLevel * 0.04; }

    @Override public String toString() { return name; }
}
