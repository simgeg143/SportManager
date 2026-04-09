package com.sportmanager;

import com.sportmanager.core.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class LeagueTest {

    class TestTeam extends Team {
        public TestTeam(String name) { super(name); }
        public int getRequiredLineupSize() { return 5; }
    }

    class TestLeague extends League {
        public TestLeague(String name) { super(name); }
    }

    @Test
    void leagueTable_shouldUpdateAndSortCorrectly() {

        TestLeague league = new TestLeague("League");

        TestTeam t1 = new TestTeam("A");
        TestTeam t2 = new TestTeam("B");

        league.addTeam(t1);
        league.addTeam(t2);

        league.updateStandings(t1, 3);
        league.updateStandings(t2, 6);

        List<StandingEntry> table = league.getTable();

        assertEquals("B", table.get(0).getTeam().getName());
        assertEquals("A", table.get(1).getTeam().getName());
    }

    @Test
    void advanceWeek_shouldIncreaseWeekByOne() {

        TestLeague league = new TestLeague("League");

        int before = league.getCurrentWeek();

        league.advanceWeek();

        assertEquals(before + 1, league.getCurrentWeek());
    }
    @Test
    void updateStandings_shouldIncreasePointsCorrectly() {

        TestLeague league = new TestLeague("League");

        TestTeam team = new TestTeam("A");

        league.addTeam(team);

        league.updateStandings(team, 3);

        List<StandingEntry> table = league.getTable();

        assertEquals(3, table.get(0).getPoints());
    }
}