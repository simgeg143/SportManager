package com.sportmanager;

import com.sportmanager.core.*;
import com.sportmanager.football.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Football Module concrete classes:
 * FootballSport, FootballLeague, FootballTeam, FootballPlayer, FootballMatch.
 */
class FootballModuleTest {

    private FootballSport  sport;
    private FootballTeam   homeTeam;
    private FootballTeam   awayTeam;

    @BeforeEach
    void setUp() {
        sport    = new FootballSport();
        homeTeam = (FootballTeam) sport.createTeam("Home FC");
        awayTeam = (FootballTeam) sport.createTeam("Away United");

        // Add 22 players per team (GK + defenders + midfielders + forwards)
        String[] positions = {"GK","CB","CB","CB","LB","RB","CM","CM","CM","ST","ST",
                              "GK","CB","CB","LB","RB","CDM","CM","CAM","LW","RW","ST"};
        for (int i = 0; i < 22; i++) {
            homeTeam.addPlayer(new FootballPlayer("H-Player-" + i, positions[i], 70 + i % 20, new Random(i)));
            awayTeam.addPlayer(new FootballPlayer("A-Player-" + i, positions[i], 65 + i % 20, new Random(i + 100)));
        }
        homeTeam.generateDefaultLineup();
        awayTeam.generateDefaultLineup();
    }

    // ── Test 1: FootballSport ─────────────────────────────────────────────────

    @Test
    @DisplayName("FootballSport.createLeague() generates the correct number of teams with populated rosters")
    void createLeagueGeneratesCorrectTeamCount() {
        League created = sport.createLeague("Premier League", 20);

        assertEquals(20, created.getTeams().size(),
                "League should contain exactly 20 teams");
        for (Team team : created.getTeams()) {
            assertFalse(team.getPlayers().isEmpty(),
                    "Every team roster should be non-empty after creation");
        }
    }

    // ── Test 2: FootballSport ─────────────────────────────────────────────────

    @Test
    @DisplayName("FootballSport returns correct lineup size (11) and max substitutes (7)")
    void footballSportLineupConstants() {
        assertEquals(11, sport.getRequiredLineupSize(),
                "Football requires exactly 11 starters");
        assertEquals(7,  sport.getMaxSubstituteCount(),
                "Football allows up to 7 substitutes");
    }

    // ── Test 3: FootballTeam ──────────────────────────────────────────────────

    @Test
    @DisplayName("FootballTeam.generateDefaultLineup() picks exactly 11 starters and fills the bench up to 7")
    void generateDefaultLineupFillsCorrectSlots() {
        assertEquals(11, homeTeam.getStartingLineup().size(),
                "Starting lineup must contain exactly 11 players");
        assertTrue(homeTeam.getSubstitutes().size() <= 7,
                "Bench must not exceed 7 substitutes");
        assertTrue(homeTeam.getSubstitutes().size() > 0,
                "Bench should have at least one substitute when roster has 22 players");
    }

    // ── Test 4: FootballPlayer (GK position bias) ─────────────────────────────

    @Test
    @DisplayName("FootballPlayer GK receives a defending boost and a shooting penalty from position bias")
    void footballPlayerGkAttributeBias() {
        FootballPlayer gk = new FootballPlayer("Test GK", "GK", 70, new Random(42));

        // GK bias: def += 20, sho -= 25 (clamped to [40,99])
        assertTrue(gk.getDefending() > gk.getShooting(),
                "GK defending should exceed shooting due to positional bias");
        assertTrue(gk.getDefending() >= 40 && gk.getDefending() <= 99,
                "GK defending must stay within [40, 99]");
    }

    // ── Test 5: FootballPlayer (ST position bias) ─────────────────────────────

    @Test
    @DisplayName("FootballPlayer ST receives a shooting boost and a defending penalty from position bias")
    void footballPlayerStAttributeBias() {
        FootballPlayer st = new FootballPlayer("Test ST", "ST", 70, new Random(42));

        // ST bias: sho += 18, def -= 18 (clamped to [40,99])
        assertTrue(st.getShooting() > st.getDefending(),
                "ST shooting should exceed defending due to positional bias");
        assertTrue(st.getShooting() >= 40 && st.getShooting() <= 99,
                "ST shooting must stay within [40, 99]");
    }

    // ── Test 6: FootballMatch ─────────────────────────────────────────────────

    @Test
    @DisplayName("FootballMatch produces a valid MatchResult with 2 segments after full simulation")
    void footballMatchSimulationProducesValidResult() {
        FootballMatch match = new FootballMatch(homeTeam, awayTeam, 1);

        assertFalse(match.isFinished(), "Match must not be finished before simulation starts");

        while (!match.isFinished()) {
            match.simulateSegment();
        }

        assertTrue(match.isFinished(),         "Match must be finished after all segments played");
        assertNotNull(match.getResult(),       "Match must produce a non-null MatchResult");
        assertTrue(match.getHomeScore() >= 0,  "Home score must be non-negative");
        assertTrue(match.getAwayScore() >= 0,  "Away score must be non-negative");
        assertEquals(2, match.getSegments().size(),
                "Football match must generate exactly 2 segment records");
    }

    // ── Test 7: FootballLeague ────────────────────────────────────────────────

    @Test
    @DisplayName("FootballLeague.updateStandings() awards 3 pts to winner and 0 pts to loser on a home win")
    void updateStandingsAwardsCorrectPointsOnWin() {
        FootballLeague league = new FootballLeague("Test League", sport);
        league.getTeams().add(homeTeam);
        league.getTeams().add(awayTeam);

        MatchResult result = new MatchResult(homeTeam, awayTeam, 2, 0, List.of());
        league.updateStandings(result);

        assertEquals(3, homeTeam.getPoints(),  "Winner (home 2-0) should have exactly 3 points");
        assertEquals(0, awayTeam.getPoints(),  "Loser (away 0-2) should have 0 points");
        assertEquals(1, homeTeam.getWins(),    "Home team should record 1 win");
        assertEquals(1, awayTeam.getLosses(),  "Away team should record 1 loss");
    }

    // ── Test 8: FootballLeague — draw ─────────────────────────────────────────

    @Test
    @DisplayName("FootballLeague.updateStandings() awards 1 point each on a draw")
    void updateStandingsAwardsOnePointEachOnDraw() {
        FootballLeague league = new FootballLeague("Test League", sport);
        league.getTeams().add(homeTeam);
        league.getTeams().add(awayTeam);

        MatchResult result = new MatchResult(homeTeam, awayTeam, 1, 1, List.of());
        league.updateStandings(result);

        assertEquals(1, homeTeam.getPoints(), "Home team should get 1 point for a draw");
        assertEquals(1, awayTeam.getPoints(), "Away team should get 1 point for a draw");
        assertEquals(1, homeTeam.getDraws(),  "Home team should record 1 draw");
        assertEquals(1, awayTeam.getDraws(),  "Away team should record 1 draw");
    }

    // ── Test 9: FootballLeague — away win ─────────────────────────────────────

    @Test
    @DisplayName("FootballLeague.updateStandings() awards 3 pts to away winner and 0 to home loser")
    void updateStandingsAwardsCorrectPointsOnAwayWin() {
        FootballLeague league = new FootballLeague("Test League", sport);
        league.getTeams().add(homeTeam);
        league.getTeams().add(awayTeam);

        MatchResult result = new MatchResult(homeTeam, awayTeam, 0, 3, List.of());
        league.updateStandings(result);

        assertEquals(3, awayTeam.getPoints(), "Away winner should have 3 points");
        assertEquals(0, homeTeam.getPoints(), "Home loser should have 0 points");
        assertEquals(1, awayTeam.getWins(),   "Away team should record 1 win");
        assertEquals(1, homeTeam.getLosses(), "Home team should record 1 loss");
    }

    // ── Test 10: FootballTeam — hasValidLineup after generateDefaultLineup ────

    @Test
    @DisplayName("hasValidLineup() returns true after generateDefaultLineup() is called on a full roster")
    void hasValidLineupTrueAfterGenerateDefault() {
        assertTrue(homeTeam.hasValidLineup(),
                "hasValidLineup() must be true after generateDefaultLineup() with 22 fit players");
    }

    // ── Test 11: FootballLeague — fixture count for even teams ────────────────

    @Test
    @DisplayName("generateFixtures() creates 2*(n-1) rounds for n even teams (home-and-away)")
    void generateFixturesCorrectRoundCountForEvenTeams() {
        League league = sport.createLeague("Test League", 4); // 4 teams → 6 rounds
        assertEquals(6, league.getTotalRounds(),
                "4 teams should generate 2*(4-1) = 6 rounds");
    }

    // ── Test 12: FootballMatch — isAtBreak between halves ────────────────────

    @Test
    @DisplayName("FootballMatch.isAtBreak() is true only between First Half and Second Half")
    void isAtBreakTrueAfterFirstHalf() {
        FootballMatch match = new FootballMatch(homeTeam, awayTeam, 1);

        assertFalse(match.isAtBreak(), "Should not be at break before simulation starts");
        match.simulateSegment();   // simulate First Half
        assertTrue(match.isAtBreak(),  "Should be at break after First Half and before Second Half");
        match.simulateSegment();   // simulate Second Half
        assertFalse(match.isAtBreak(), "Should not be at break after the match is finished");
    }
}
