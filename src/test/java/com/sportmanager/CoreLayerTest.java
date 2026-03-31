package com.sportmanager;

import com.sportmanager.factory.SportFactory;
import com.sportmanager.football.FootballSport;
import com.sportmanager.core.Sport;
import com.sportmanager.core.Match;
import com.sportmanager.core.MatchResult;
import com.sportmanager.core.InjuryRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CoreLayerTest {

    @Test
    @DisplayName("SportFactory should return FootballSport for valid sport name")
    void sportFactoryCreatesFootballSport() {
        Sport sport = SportFactory.create("football");
        assertNotNull(sport);
        assertInstanceOf(FootballSport.class, sport);
        assertEquals("Football", sport.getName());
    }

    @Test
    @DisplayName("SportFactory should throw exception for unknown sport name")
    void sportFactoryThrowsForUnknownSport() {
        assertThrows(IllegalArgumentException.class,
                () -> SportFactory.create("hockey"));
    }

    @Test
    @DisplayName("Match should produce a non-null result after all segments are simulated")
    void matchSimulationProducesResult() {
        Sport sport = SportFactory.create("football");
        com.sportmanager.core.League league = sport.createLeague("Test League", 4);
        Match match = sport.createMatch(
                league.getTeams().get(0),
                league.getTeams().get(1), 1);

        while (!match.isFinished()) {
            match.simulateSegment();
        }

        assertNotNull(match.getResult());
        assertEquals(2, match.getTotalSegments());
        assertTrue(match.getHomeScore() >= 0);
    }

    @Test
    @DisplayName("MatchResult should store scores and determine winner correctly")
    void matchResultStoresScoresCorrectly() {
        Sport sport = SportFactory.create("football");
        com.sportmanager.core.League league = sport.createLeague("Test League", 4);
        com.sportmanager.core.Team home = league.getTeams().get(0);
        com.sportmanager.core.Team away = league.getTeams().get(1);

        MatchResult result = new MatchResult(home, away, 3, 1, List.of());

        assertEquals(3, result.getHomeScore());
        assertEquals(1, result.getAwayScore());
        assertEquals(home, result.getWinner());
        assertEquals(MatchResult.HOME_WIN, result.getOutcome());
    }

    @Test
    @DisplayName("InjuryRecord should mark player as recovered after decrementing games")
    void injuryRecordDecrementsAndRecovers() {
        Sport sport = SportFactory.create("football");
        com.sportmanager.core.League league = sport.createLeague("Test League", 4);
        com.sportmanager.core.Player player =
                league.getTeams().get(0).getPlayers().get(0);

        InjuryRecord injury = new InjuryRecord(player, 3, "Hamstring strain");

        assertFalse(injury.isRecovered());
        injury.decrementGames();
        injury.decrementGames();
        injury.decrementGames();
        assertTrue(injury.isRecovered());
    }
}