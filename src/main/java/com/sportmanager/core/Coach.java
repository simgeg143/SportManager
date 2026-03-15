package com.sportmanager.core;

/**
 * Represents a coaching staff member for a team.
 * Coaches affect training outcomes (fitness/attribute gains).
 * As defined in the architecture document: name, role, shape, trainingSkill, motivationSkill.
 */
public class Coach {

    private String name;
    private String role;           // e.g. "Head Coach", "Assistant Coach", "GK Coach"
    private String preferredShape; // preferred formation, e.g. "4-3-3"
    private int    trainingSkill;  // 1–10, affects how much players improve in training
    private int    motivationSkill;// 1–10, affects match performance multiplier

    public Coach(String name, String role, String preferredShape,
                 int trainingSkill, int motivationSkill) {
        this.name             = name;
        this.role             = role;
        this.preferredShape   = preferredShape;
        this.trainingSkill    = Math.max(1, Math.min(10, trainingSkill));
        this.motivationSkill  = Math.max(1, Math.min(10, motivationSkill));
    }

    public String getName()            { return name; }
    public String getRole()            { return role; }
    public String getPreferredShape()  { return preferredShape; }
    public int    getTrainingSkill()   { return trainingSkill; }
    public int    getMotivationSkill() { return motivationSkill; }

    /** Combined quality score used for UI display (1–10 scale). */
    public double getOverallRating() {
        return (trainingSkill + motivationSkill) / 2.0;
    }

    @Override public String toString() {
        return role + ": " + name + " (Training " + trainingSkill
                + ", Motivation " + motivationSkill + ")";
    }
}
