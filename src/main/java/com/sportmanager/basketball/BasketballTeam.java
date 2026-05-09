package com.sportmanager.basketball;

import com.sportmanager.core.Lineup;
import com.sportmanager.core.Player;
import com.sportmanager.core.Tactic;
import com.sportmanager.core.Team;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BasketballTeam extends Team {

    static final int LINEUP_SIZE = 5;
    static final int MAX_SUBS = 7;

    private static final Map<String, Integer> POSITION_PRIORITY = Map.of(
            "PG", 0,
            "SG", 1,
            "SF", 2,
            "PF", 3,
            "C", 4
    );

    public BasketballTeam(String name) {
        super(name);
        this.currentTactic =
                new Tactic(
                        BasketballTactics.DEFAULT_OFFENSE,
                        "Balanced"
                );
    }

    @Override
    public int getRequiredLineupSize() {
        return LINEUP_SIZE;
    }

    @Override
    public int getMaxSubstituteCount() {
        return MAX_SUBS;
    }

    @Override
    public void generateDefaultLineup() {
        startingLineup.clear();
        substitutes.clear();

        List<Player> sorted = roster.stream()
                .filter(Player::isAvailable)
                .sorted(Comparator
                        .comparingInt((Player p) -> POSITION_PRIORITY.getOrDefault(p.getPosition(), 99))
                        .thenComparingInt(p -> -p.getSkillLevel()))
                .toList();

        for (Player p : sorted) {
            if (startingLineup.size() < LINEUP_SIZE) {
                startingLineup.add(p);
            } else if (substitutes.size() < MAX_SUBS) {
                substitutes.add(p);
            } else {
                break;
            }
        }

        currentLineup = new Lineup(startingLineup, substitutes);
    }
}
