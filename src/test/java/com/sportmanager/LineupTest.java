package com.sportmanager;

import com.sportmanager.core.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class LineupTest {

    class TestPlayer extends Player {
        public TestPlayer(String name, int age, String position, int skill) {
            super(name, age, position, skill);
        }
        public Map<String, Integer> getSpecificAttributes() { return null; }
        public String getStatusDisplay() { return "OK"; }
    }

    @Test
    void lineup_shouldBeValid_onlyWhenCorrectSizeAndHealthyPlayers() {

        List<Player> players = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            players.add(new TestPlayer("P" + i, 20, "Forward", 80));
        }

        Lineup valid = new Lineup(players, new ArrayList<>());
        assertTrue(valid.isValid(5));

        Lineup wrongSize = new Lineup(players, new ArrayList<>());
        assertFalse(wrongSize.isValid(6));

        players.get(0).setInjuryMatchesRemaining(1);
        Lineup injured = new Lineup(players, new ArrayList<>());
        assertFalse(injured.isValid(5));
    }
}