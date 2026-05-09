package com.sportmanager;

import com.sportmanager.core.*;
import com.sportmanager.factory.SportFactory;
import com.sportmanager.session.GameSession;
import com.sportmanager.session.SaveGameService;
import java.io.IOException;

import java.util.List;

/**
 * Facade and single entry point for the entire UI layer.
 *
 * As specified in the architecture document:
 *   "SportManager is the main entry point of the game application and
 *    the only class that the UI layer directly interacts with."
 *
 * All UI controllers call methods on this singleton; none of them
 * import GameSession, League, or any concrete sport class directly.
 */
public final class SportManager {

    private static final SportManager INSTANCE = new SportManager();
    private final GameSession session = GameSession.getInstance();

    /** Row for the load-game picker (UI uses this instead of importing session types). */
    public record SaveGameEntry(String id, String displayName, long savedAtEpochMs, String detailsLine) {}

    private SportManager() {}

    public static SportManager getInstance() { return INSTANCE; }

    // ══════════════════════════════════════════════════════════════════════════
    // 1. Game lifecycle — called by controllers, cause navigation side-effects
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Starts a brand-new game: resets the session and navigates to the
     * Sport Selection screen. (LM-1, called by MainMenuController)
     */
    public void startNewGame() {
        session.reset();
        SceneManager.getInstance().showSportSelection();
    }

    /**
     * Instantiates the chosen sport via SportFactory, generates a league
     * with 20 teams, and navigates to Team Selection. (LM-2, LM-3, LM-4)
     * Called by SportSelectionController.
     */
    public void selectSport(String sportCode) {
        Sport sport  = SportFactory.create(sportCode);
        League league = sport.createLeague(sportCode + " Premier League", 20);
        session.init(sport, league);
        SceneManager.getInstance().showTeamSelection();
    }

    /**
     * Assigns the managed team and navigates to the Dashboard. (LM-6)
     * Called by TeamSelectionController.
     */
    public void selectManagedTeam(Team team) {
        session.setManagedTeam(team);
        SceneManager.getInstance().showDashboard();
    }

    /**
     * Prepares the managed team's match for the current week and navigates
     * to the Match screen. (MS-1)
     * Called by DashboardController.
     */
    public void startMatch() {
        session.prepareCurrentMatch();
        SceneManager.getInstance().showMatchScreen();
    }

    /**
     * Finalises the current round (applies all results, decrements injuries,
     * rebuilds AI lineups) and advances to the next week.
     * Returns to the Dashboard. (LM-8, LM-9)
     * Called by MatchScreenController after the match is complete.
     */
    public void advanceWeek() {
        session.finalizeRound();
        SceneManager.getInstance().showDashboard();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 2. Navigation — pure screen switches, no state change
    // ══════════════════════════════════════════════════════════════════════════

    /** Displays the league standings table. (LM-7) */
    public void showLeagueTable() { SceneManager.getInstance().showLeagueTable(); }

    /** Displays the full season fixture schedule. (TM-2) */
    public void showFixture()     { SceneManager.getInstance().showFixture(); }

    /** Displays the squad management screen. (TM-1) */
    public void showSquad()       { SceneManager.getInstance().showSquad(); }

    /** Returns to the main Dashboard. */
    public void showDashboard()   { SceneManager.getInstance().showDashboard(); }

    /** Returns to the main menu. */
    public void showMainMenu()    { SceneManager.getInstance().showMainMenu(); }

    // ══════════════════════════════════════════════════════════════════════════
    // 3. Data accessors — called by controllers to retrieve display data
    // ══════════════════════════════════════════════════════════════════════════

    /** @return the active Sport (interface type — never a concrete class). */
    public Sport getSport()       { return session.getSport(); }

    /** @return the current League instance. */
    public League getLeague()     { return session.getLeague(); }

    /** @return the team the user is managing. */
    public Team getManagedTeam()  { return session.getManagedTeam(); }

    /** @return the current match prepared for this week, or null if not yet set. */
    public Match getCurrentMatch(){ return session.getCurrentMatch(); }

    /** @return 1-based current week number within the season. */
    public int getCurrentWeek()   { return session.getCurrentWeek(); }

    /** @return total number of matchdays (rounds) in the season. */
    public int getTotalWeeks() {
        League l = session.getLeague();
        return l != null ? l.getTotalRounds() : 0;
    }

    /** @return true when all fixtures have been played. */
    public boolean isSeasonFinished() { return session.isSeasonFinished(); }

    /** @return league standing position (1-based) of the managed team. */
    public int getManagedTeamPosition() { return session.getManagedTeamPosition(); }

    /**
     * Returns the sorted league table as a list of StandingEntry objects.
     * Called by StandingsController. (LM-7)
     */
    public List<StandingEntry> showLeagueTableData() {
        if (session.getLeague() == null) return List.of();
        return session.getLeague().getTable();
    }

    /**
     * Returns all fixture rounds for the current season.
     * Called by FixtureController. (TM-2)
     */
    public List<List<Match>> showFixtureData() {
        League l = session.getLeague();
        if (l == null) return List.of();
        int total = l.getTotalRounds();
        List<List<Match>> all = new java.util.ArrayList<>();
        for (int w = 1; w <= total; w++) all.add(l.getMatchesOfWeek(w));
        return all;
    }


    /**
     * Returns available (non-injured) players for the managed team. (TM-1, IM-2)
     */
    public List<Player> getAvailablePlayers() {
        Team t = session.getManagedTeam();
        if (t == null) return List.of();
        return t.getAvailablePlayers();
    }

    /** @return how many substitutions have been used in the current match. */
    public int getSubstitutionsUsed()     { return session.getSubstitutionsUsed(); }
    public void incrementSubstitutions()  { session.incrementSubstitutionsUsed(); }

    /**
     * Creates a new save file with the given label (multiple saves allowed).
     * @return internal save id (for debugging); UI can ignore it.
     */
    public String saveGame(String displayName) throws IOException {
        if (!session.isActive()) {
            throw new IllegalStateException("No active game session to save.");
        }
        return SaveGameService.saveNew(displayName, session.createSnapshot());
    }

    public List<SaveGameEntry> listSaveGames() {
        return SaveGameService.listSaves().stream()
                .map(s -> new SaveGameEntry(s.id(), s.displayName(), s.savedAtEpochMs(), s.detailsLine()))
                .toList();
    }

    public boolean loadGame(String saveId) throws IOException, ClassNotFoundException {
        session.restoreFromSnapshot(SaveGameService.loadById(saveId));
        if (!session.isActive()) return false;
        SceneManager.getInstance().showDashboard();
        return true;
    }

    public void deleteSaveGame(String saveId) throws IOException {
        SaveGameService.deleteById(saveId);
    }
}
