package com.sportmanager.core;

import java.io.Serializable;

/**
 * Represents a tactical configuration for a team.
 * Stores the formation name, shape description, and attack/defence bias values.
 * As defined in the architecture document: name, shape, offenseLevel, defenseLevel.
 */
public class Tactic implements Serializable {

    private String name;
    private String style;


    public Tactic(String name, String style) {
        this.name = name;
        this.style  = style;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    @Override public String toString() { return name + "-" + style; }
}
