package com.sportmanager;

import com.sportmanager.core.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StandingEntryTest {

    class TestTeam extends Team {
        public TestTeam(String name) { super(name); }
        public int getRequiredLineupSize() { return 5; }
    }

    @Test
    void standings_shouldSortTeamsByPointsDescending() {

        TestTeam t1 = new TestTeam("A");
        TestTeam t2 = new TestTeam("B");

        StandingEntry e1 = new StandingEntry(t1);
        StandingEntry e2 = new StandingEntry(t2);

        e1.addPoints(3);
        e2.addPoints(6);

        List<StandingEntry> list = new ArrayList<>();
        list.add(e1);
        list.add(e2);

        Collections.sort(list);

        assertEquals("B", list.get(0).getTeam().getName());
        assertEquals("A", list.get(1).getTeam().getName());
    }
}