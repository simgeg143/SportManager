package com.sportmanager.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the starting XI and substitutes for a single match.
 * Provides isValid() to enforce sport-specific lineup constraints,
 * as defined in the architecture document.
 */
public class Lineup {

    private final List<Player> starters;
    private final List<Player> substitutes;

    public Lineup(List<Player> starters, List<Player> substitutes) {
        this.starters     = new ArrayList<>(starters);
        this.substitutes  = new ArrayList<>(substitutes);
    }

    public List<Player> getStarters()     { return starters; }
    public List<Player> getSubstitutes()  { return substitutes; }

    /**
     * Checks lineup validity:
     *  – exactly {@code requiredStarterCount} healthy players in the starting XI
     *  – no starter is currently injured
     *
     * @param requiredStarterCount sport-specific required count (e.g. 11 for football)
     */
    public boolean isValid(int requiredStarterCount) {
        if (starters.size() != requiredStarterCount) return false;
        return starters.stream().noneMatch(Player::isInjured);
    }

    /** Returns all players in this lineup (starters + subs). */
    public List<Player> all() {
        List<Player> all = new ArrayList<>(starters);
        all.addAll(substitutes);
        return all;
    }

    @Override public String toString() {
        return "Lineup[starters=" + starters.size()
                + ", subs=" + substitutes.size() + "]";
    }
}
