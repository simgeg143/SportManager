package com.sportmanager.session;

import com.sportmanager.core.League;
import com.sportmanager.core.Match;
import com.sportmanager.core.Sport;
import com.sportmanager.core.Team;
import com.sportmanager.factory.SportFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Save/load uses Java serialization; domain objects must not embed non-serializable state
 * (e.g. {@link java.util.Random}) or saves fail for football and basketball alike.
 */
class SessionSnapshotSerializationTest {

    @ParameterizedTest
    @ValueSource(strings = {"football", "basketball"})
    @DisplayName("SessionSnapshot round-trips with league, sport, and in-progress match")
    void snapshotRoundTripIncludesSportAndMatch(String sportCode) throws Exception {
        Sport sport = SportFactory.create(sportCode);
        League league = sport.createLeague(sportCode + " Test League", 8);
        Team managed = league.getTeams().get(0);

        Match fixture = null;
        for (Match m : league.getCurrentRoundMatches()) {
            if (m.getHomeTeam() == managed || m.getAwayTeam() == managed) {
                fixture = m;
                break;
            }
        }
        assertNotNull(fixture);
        fixture.beginSegmentSimulation();

        GameSession.SessionSnapshot snap = new GameSession.SessionSnapshot(
                2026,
                2,
                sport,
                managed,
                league,
                fixture,
                1
        );

        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(snap);
            bytes = bos.toByteArray();
        }

        GameSession.SessionSnapshot restored;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            restored = (GameSession.SessionSnapshot) ois.readObject();
        }

        assertNotNull(restored.selectedSport());
        assertEquals(sport.getName(), restored.selectedSport().getName());
        assertEquals(sport.getClass(), restored.selectedSport().getClass());
        assertNotNull(restored.league());
        assertEquals(managed.getName(), restored.managedTeam().getName());
        assertNotNull(restored.currentMatch());
        assertTrue(restored.currentMatch().hasPendingSegmentEvents() || !restored.currentMatch().isFinished());
    }
}
