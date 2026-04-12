package com.sportmanager.session;

import com.sportmanager.core.*;
import com.sportmanager.settings.AppSettings;

import java.util.List;

/**
 * Holds the complete mutable state of the currently running game.
 *
 * As specified in the architecture document:
 *   – currentSeasonYear, currentWeek
 *   – selectedSport (stored as the Sport interface, never a concrete class)
 *   – managedTeam, league
 *   – isSeasonFinished(), startNextSeason()
 *
 * All access is via SportManager; no UI controller imports this class directly.
 */
public class GameSession {

    private static final GameSession INSTANCE = new GameSession();

    // ── State fields (doc §4.4b) ──────────────────────────────────────────────
    private int    currentSeasonYear = AppSettings.getInstance().getStartYear();
    private int    currentWeek       = 1;    // 1-based; mirrors league.currentRound + 1
    private Sport  selectedSport;
    private Team   managedTeam;
    private League league;
    private Match  currentMatch;
    private int    substitutionsUsed;

    private GameSession() {}

    public static GameSession getInstance() { return INSTANCE; }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /** Full state reset — called by SportManager.startNewGame(). */
    public void reset() {
        selectedSport     = null;
        managedTeam       = null;
        league            = null;
        currentMatch      = null;
        substitutionsUsed = 0;
        currentWeek       = 1;
        currentSeasonYear = AppSettings.getInstance().getStartYear();
    }

    /** Initialises sport + league after the user picks a sport. */
    public void init(Sport sport, League league) {
        this.selectedSport = sport;
        this.league        = league;
        this.currentWeek   = 1;
    }

    public void setManagedTeam(Team team) {
        this.managedTeam = team;
    }

    // ── Current match ─────────────────────────────────────────────────────────

    /** Finds the managed team's fixture for the current round and caches it. */
    public Match prepareCurrentMatch() {
        if (league == null || managedTeam == null) return null;
        for (Match m : league.getCurrentRoundMatches()) {
            if (m.getHomeTeam() == managedTeam || m.getAwayTeam() == managedTeam) {
                currentMatch      = m;
                substitutionsUsed = 0;
                return m;
            }
        }
        return null;
    }

    public Match getCurrentMatch()             { return currentMatch; }
    public int   getSubstitutionsUsed()        { return substitutionsUsed; }
    public void  incrementSubstitutionsUsed()  { substitutionsUsed++; }

    // ── Round / week progression ──────────────────────────────────────────────

    /**
     * Finalises the current round:
     *  1. Auto-simulates all AI matches for this round.
     *  2. Calls league.updateStandings() for every match using MatchResult.
     *  3. Decrements injury timers for all players.
     *  4. Rebuilds AI team lineups; re-validates managed team's lineup.
     *  5. Advances the round pointer and increments currentWeek.
     */
    public void finalizeRound() {
        if (league == null) return;

        for (Match m : league.getCurrentRoundMatches()) {
            if (!m.isFinished()) {
                while (!m.isFinished()) m.simulateSegment();
            }
            if (m.getResult() != null) {
                league.updateStandings(m.getResult());
            } else {
                // Fallback: apply via Team directly if MatchResult was not produced
                applyResultFallback(m);
            }
        }

        for (Team t : league.getTeams()) {
            for (Player p : t.getRoster()) p.decrementInjury();
            t.generateDefaultLineup();
        }
        if (managedTeam != null && !managedTeam.hasValidLineup()) {
            managedTeam.generateDefaultLineup();
        }

        league.advanceRound();
        currentWeek  = league.getCurrentRound() + 1;
        currentMatch = null;
    }

    /**
     * True when all matchdays in the season have been played. (doc §4.4b)
     * Corresponds to isSeasonFinished().
     */
    public boolean isSeasonFinished() {
        return league != null && league.isSeasonOver();
    }

    /**
     * Resets week/round pointers for a new season while keeping the sport
     * and managed team. (doc §4.4b)
     */
    public void startNextSeason() {
        if (league == null || selectedSport == null) return;
        // Create a fresh league with the same sport; keep managed team name
        String teamName  = managedTeam != null ? managedTeam.getName() : null;
        currentSeasonYear++;
        League newLeague = selectedSport.createLeague(
                selectedSport.getName() + " Premier League", 20);
        league       = newLeague;
        currentWeek  = 1;
        currentMatch = null;
        // Re-set managed team to the matching team in new league
        if (teamName != null) {
            managedTeam = newLeague.getTeams().stream()
                    .filter(t -> t.getName().equals(teamName))
                    .findFirst()
                    .orElse(newLeague.getTeams().get(0));
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public Sport  getSport()       { return selectedSport; }
    public League getLeague()      { return league; }
    public Team   getManagedTeam() { return managedTeam; }

    public int getCurrentWeek()       { return currentWeek; }
    public int getCurrentSeasonYear() { return currentSeasonYear; }

    public boolean isActive() {
        return selectedSport != null && league != null && managedTeam != null;
    }

    public int getManagedTeamPosition() {
        if (league == null || managedTeam == null) return -1;
        List<Team> sorted = league.getSortedStandings();
        return sorted.indexOf(managedTeam) + 1;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Fallback standings update for matches that didn't produce a MatchResult. */
    private void applyResultFallback(Match m) {
        String outcome = m.getOutcome();
        Team   home    = m.getHomeTeam();
        Team   away    = m.getAwayTeam();
        int    hScore  = m.getHomeScore();
        int    aScore  = m.getAwayScore();
        switch (outcome) {
            case "HOME_WIN" -> { home.recordWin(hScore, aScore);  away.recordLoss(aScore, hScore); }
            case "AWAY_WIN" -> { away.recordWin(aScore, hScore);  home.recordLoss(hScore, aScore); }
            default         -> { home.recordDraw(hScore, aScore); away.recordDraw(aScore, hScore); }
        }
    }
}
