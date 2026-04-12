package com.sportmanager;

import com.sportmanager.core.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class LeagueTest {

    // ── Minimal stubs ─────────────────────────────────────────────────────────

    static class TestTeam extends Team {
        public TestTeam(String name) { super(name); }
        @Override public int getRequiredLineupSize()  { return 5; }
        @Override public int getMaxSubstituteCount()  { return 3; }
        @Override public void generateDefaultLineup() { }
    }

    static class TestLeague extends League {
        public TestLeague(String name) { super(name, null); }
        @Override public void generateFixtures()            { }
        @Override public List<StandingEntry> getTable()     {
            List<Team> sorted = getSortedStandings();
            List<StandingEntry> table = new ArrayList<>();
            for (int i = 0; i < sorted.size(); i++)
                table.add(new StandingEntry(sorted.get(i), i + 1));
            return table;
        }
        @Override public void updateStandings(MatchResult r) {
            Team home = r.getHomeTeam(); Team away = r.getAwayTeam();
            switch (r.getOutcome()) {
                case MatchResult.HOME_WIN -> { home.recordWin(r.getHomeScore(), r.getAwayScore());  away.recordLoss(r.getAwayScore(), r.getHomeScore()); }
                case MatchResult.AWAY_WIN -> { away.recordWin(r.getAwayScore(), r.getHomeScore());  home.recordLoss(r.getHomeScore(), r.getAwayScore()); }
                default                   -> { home.recordDraw(r.getHomeScore(), r.getAwayScore()); away.recordDraw(r.getAwayScore(), r.getHomeScore()); }
            }
        }
        @Override public List<Team> getSortedStandings() {
            return teams.stream()
                    .sorted(Comparator.comparingInt(Team::getPoints).reversed())
                    .toList();
        }
        @Override public Team getChampion() {
            List<Team> s = getSortedStandings(); return s.isEmpty() ? null : s.get(0);
        }
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    void leagueTable_shouldSortByPointsDescending() {
        TestLeague league = new TestLeague("League");
        TestTeam t1 = new TestTeam("A");
        TestTeam t2 = new TestTeam("B");
        league.getTeams().add(t1);
        league.getTeams().add(t2);

        league.updateStandings(new MatchResult(t2, t1, 2, 0, List.of())); // B wins (6 pts)
        league.updateStandings(new MatchResult(t2, t1, 1, 0, List.of())); // B wins again

        List<StandingEntry> table = league.getTable();
        assertEquals("B", table.get(0).getTeam().getName());
        assertEquals("A", table.get(1).getTeam().getName());
    }

    @Test
    void advanceRound_shouldIncrementRoundCounter() {
        TestLeague league = new TestLeague("League");
        int before = league.getCurrentRound();
        league.advanceRound();
        assertEquals(before + 1, league.getCurrentRound());
    }

    @Test
    void updateStandings_shouldAward3PointsForWin() {
        TestLeague league = new TestLeague("League");
        TestTeam teamA = new TestTeam("A");
        TestTeam teamB = new TestTeam("B");
        league.getTeams().add(teamA);
        league.getTeams().add(teamB);

        league.updateStandings(new MatchResult(teamA, teamB, 3, 0, List.of()));

        List<StandingEntry> table = league.getTable();
        assertEquals(3, table.get(0).getPoints(), "Winner should have 3 points");
        assertEquals(0, table.get(1).getPoints(), "Loser should have 0 points");
    }
}
