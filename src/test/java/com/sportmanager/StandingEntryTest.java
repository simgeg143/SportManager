package com.sportmanager;

import com.sportmanager.core.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StandingEntryTest {

    static class TestTeam extends Team {
        public TestTeam(String name) { super(name); }
        @Override public int getRequiredLineupSize()  { return 5; }
        @Override public int getMaxSubstituteCount()  { return 3; }
        @Override public void generateDefaultLineup() { }
    }

    @Test
    void standings_shouldSortTeamsByPointsDescending() {
        TestTeam t1 = new TestTeam("A");
        TestTeam t2 = new TestTeam("B");

        // t1 gets 3 pts (1 win), t2 gets 6 pts (2 wins)
        t1.recordWin(1, 0);
        t2.recordWin(1, 0);
        t2.recordWin(1, 0);

        StandingEntry e1 = new StandingEntry(t1);
        StandingEntry e2 = new StandingEntry(t2);

        List<StandingEntry> list = new ArrayList<>();
        list.add(e1);
        list.add(e2);
        Collections.sort(list);

        assertEquals("B", list.get(0).getTeam().getName(), "Higher-points team should be first");
        assertEquals("A", list.get(1).getTeam().getName(), "Lower-points team should be second");
    }
}
