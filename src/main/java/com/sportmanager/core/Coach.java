package com.sportmanager.core;

import java.io.Serializable;

/**
 * Represents a coaching staff member for a team.
 * Coaches affect training outcomes and preferred tactical shape.
 */
public class Coach implements Serializable {

    private final String name;
    private final String role;
    private final String preferredShape;
    private final int    trainingSkill;
    private final int    motivationSkill;

    public Coach(String name, String role, String preferredShape,
                 int trainingSkill, int motivationSkill) {
        this.name            = name;
        this.role            = role;
        this.preferredShape  = preferredShape;
        this.trainingSkill   = trainingSkill;
        this.motivationSkill = motivationSkill;
    }

    /** Minimal constructor when shape/skills are not relevant. */
    public Coach(String name, String role) {
        this(name, role, "4-4-2", 5, 5);
    }

    public String getName()            { return name; }
    public String getRole()            { return role; }
    public String getPreferredShape()  { return preferredShape; }
    public int    getTrainingSkill()   { return trainingSkill; }
    public int    getMotivationSkill() { return motivationSkill; }

    @Override
    public String toString() { return role + ": " + name; }
}
