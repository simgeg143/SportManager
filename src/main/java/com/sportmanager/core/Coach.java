package com.sportmanager.core;

/**
 * Represents a coaching staff member for a team.
 * Coaches affect training outcomes (fitness/attribute gains).
 * As defined in the architecture document: name, role, shape, trainingSkill, motivationSkill.
 */
public class Coach {

    private String name;
    private String role;           // e.g. "Head Coach", "Assistant Coach", "GK Coach"

    public Coach(String name, String role) {
        this.name             = name;
        this.role             = role;
    }

    public String getName()            { return name; }
    public String getRole()            { return role; }

    @Override public String toString() {
        return role + ": " + name;
    }
}
