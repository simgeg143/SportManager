package com.sportmanager.football;

import com.sportmanager.core.Player;
import com.sportmanager.core.Team;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Football-specific team. Requires an 11-player starting lineup and up to 7 subs.
 * Includes coaching staff (Head Coach + Assistant).
 */
public class FootballTeam extends Team {

    private static final int LINEUP_SIZE = 11;
    private static final int MAX_SUBS    = 7;

    private static final Map<String, Integer> POSITION_PRIORITY =
            Map.ofEntries(
                    Map.entry("GK",  0), Map.entry("CB",  1), Map.entry("LB",  2),
                    Map.entry("RB",  3), Map.entry("CDM", 4), Map.entry("CM",  5),
                    Map.entry("CAM", 6), Map.entry("LW",  7), Map.entry("RW",  8),
                    Map.entry("CF",  9), Map.entry("ST",  10)
            );

    public FootballTeam(String name) {
        super(name);
        this.currentTactic = "4-3-3";
    }

    @Override public int getRequiredLineupSize() { return LINEUP_SIZE; }
    @Override public int getMaxSubstituteCount() { return MAX_SUBS; }

    /**
     * Builds a default 11-man starting XI using position priority and skill,
     * then fills the bench with the next MAX_SUBS healthy players.
     */
    @Override
    public void generateDefaultLineup() {
        startingLineup.clear();
        substitutes.clear();

        List<Player> sorted = roster.stream()
                .filter(p -> !p.isInjured())
                .sorted(Comparator
                        .comparingInt((Player p) -> POSITION_PRIORITY.getOrDefault(p.getPosition(), 99))
                        .thenComparingInt(p -> -p.getSkillLevel()))
                .toList();

        for (Player p : sorted) {
            if      (startingLineup.size() < LINEUP_SIZE) startingLineup.add(p);
            else if (substitutes.size()    < MAX_SUBS)    substitutes.add(p);
            else break;
        }
    }

    /** Returns the roster sorted by positional order for the squad display. */
    public List<Player> getRosterSortedByPosition() {
        return roster.stream()
                .sorted(Comparator.comparingInt(p ->
                        POSITION_PRIORITY.getOrDefault(p.getPosition(), 99)))
                .toList();
    }
}
