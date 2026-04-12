package com.sportmanager;

import com.sportmanager.core.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class TeamTest {

    static class TestPlayer extends Player {
        public TestPlayer(String name, int age, String position, int skill) {
            super(name, age, position, skill);
        }
        public Map<String, Integer> getSpecificAttributes() { return Collections.emptyMap(); }
        public String getStatusDisplay() { return isInjured() ? "INJ" : "FIT"; }
    }

    static class TestTeam extends Team {
        public TestTeam(String name) { super(name); }
        @Override public int getRequiredLineupSize()  { return 5; }
        @Override public int getMaxSubstituteCount()  { return 3; }
        @Override public void generateDefaultLineup() { }
    }

    @Test
    void getAvailablePlayers_shouldExcludeInjuredPlayers() {
        TestTeam team = new TestTeam("Team");

        TestPlayer healthy = new TestPlayer("Ali",   20, "Forward",    80);
        TestPlayer injured = new TestPlayer("Ahmet", 22, "Midfielder", 75);
        injured.setInjuryMatchesRemaining(2);

        team.addPlayer(healthy);
        team.addPlayer(injured);

        List<Player> available = team.getAvailablePlayers();

        assertEquals(1, available.size());
        assertTrue(available.contains(healthy));
        assertFalse(available.contains(injured));
    }
}
