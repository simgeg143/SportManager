package com.sportmanager;

import com.sportmanager.core.League;
import com.sportmanager.core.StandingEntry;
import com.sportmanager.core.Team;
import com.sportmanager.session.GameSession;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SportManagerTest {

    private static boolean javafxStarted = false;

    private SportManager manager;
    private GameSession session;

    @BeforeAll
    static void initJavaFx() throws Exception {
        if (!javafxStarted) {
            CountDownLatch startupLatch = new CountDownLatch(1);
            Platform.startup(startupLatch::countDown);
            assertTrue(startupLatch.await(5, TimeUnit.SECONDS), "JavaFX platform failed to start");
            javafxStarted = true;
        }

        runOnFxThreadAndWait(() -> {
            Stage stage = new Stage();
            stage.setScene(new Scene(new Pane(), 1000, 700));
            SceneManager.getInstance().init(stage);
        });
    }

    @BeforeEach
    void setUp() {
        manager = SportManager.getInstance();
        session = GameSession.getInstance();
        session.reset();
    }

    @Test
    @DisplayName("startNewGame should create a fresh session state")
    void startNewGame_shouldCreateFreshSession() throws Exception {
        runOnFxThreadAndWait(() -> manager.startNewGame());

        assertNotNull(session);
        assertNull(session.getSport());
        assertNull(session.getLeague());
        assertNull(session.getManagedTeam());
        assertEquals(1, session.getCurrentWeek());
    }

    @Test
    @DisplayName("selectSport should throw exception for invalid sport code")
    void selectSport_invalidCode_shouldThrowException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> runOnFxThreadAndWait(() -> manager.selectSport("invalid-sport"))
        );

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().toLowerCase().contains("unknown")
                || ex.getMessage().toLowerCase().contains("unsupported"));
    }

    @Test
    @DisplayName("selectManagedTeam should assign the correct team")
    void selectManagedTeam_shouldAssignCorrectTeam() throws Exception {
        runOnFxThreadAndWait(() -> {
            manager.startNewGame();
            manager.selectSport("football");
        });

        League league = session.getLeague();
        assertNotNull(league);
        assertFalse(league.getTeams().isEmpty());

        Team selectedTeam = league.getTeams().get(0);

        runOnFxThreadAndWait(() -> manager.selectManagedTeam(selectedTeam));

        assertEquals(selectedTeam, session.getManagedTeam());
    }

    @Test
    @DisplayName("advanceWeek should increase the current week")
    void advanceWeek_shouldIncreaseWeek() throws Exception {
        runOnFxThreadAndWait(() -> {
            manager.startNewGame();
            manager.selectSport("football");
        });

        int before = session.getCurrentWeek();

        runOnFxThreadAndWait(() -> manager.advanceWeek());

        int after = session.getCurrentWeek();

        assertTrue(after > before, "Current week should increase after advanceWeek()");
    }

    @Test
    @DisplayName("End-to-end flow should work")
    void endToEndFlow_shouldWork() throws Exception {
        runOnFxThreadAndWait(() -> {
            manager.startNewGame();
            manager.selectSport("football");
        });

        League league = session.getLeague();
        assertNotNull(league);
        assertFalse(league.getTeams().isEmpty());

        Team selectedTeam = league.getTeams().get(0);

        runOnFxThreadAndWait(() -> {
            manager.selectManagedTeam(selectedTeam);
            manager.advanceWeek();
        });

        List<StandingEntry> table = manager.showLeagueTableData();

        assertNotNull(session.getSport());
        assertNotNull(session.getLeague());
        assertEquals(selectedTeam, session.getManagedTeam());
        assertNotNull(table);
        assertFalse(table.isEmpty());
    }

    private static void runOnFxThreadAndWait(ThrowingRunnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                errorRef.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "FX task timed out");

        if (errorRef.get() != null) {
            Throwable t = errorRef.get();
            if (t instanceof Exception e) {
                throw e;
            }
            if (t instanceof Error e) {
                throw e;
            }
            throw new RuntimeException(t);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}