package com.sportmanager.core;

import java.util.List;

/**
 * Central abstraction for a sport. The UI and game logic interact with this
 * interface only — never with concrete implementations such as FootballSport.
 */
public interface Sport {

    /** Human-readable name shown in the UI (e.g. "Football"). */
    String getName();

    /**
     * Creates and populates a full league ready for a new season.
     *
     * @param leagueName display name for the league
     * @param teamCount  number of teams to generate
     */
    League createLeague(String leagueName, int teamCount);

    /** Creates a blank team with the given name (roster not yet populated). */
    Team createTeam(String name);

    /**
     * Creates a match between two teams for the given matchday.
     * The weekNo is stored on the match and shown in the fixture list.
     */
    Match createMatch(Team home, Team away, int weekNo);

    /** All valid player positions for this sport (e.g. "GK", "CB", …). */
    List<String> getPositions();

    /** All available tactical formations / styles. */
    List<String> getTactics();

    /** Number of game segments (halves = 2, quarters = 4, sets = 5, …). */
    int getSegmentCount();

    /** Singular label for one segment (e.g. "Half", "Quarter", "Set"). */
    String getSegmentLabel();

    /** Number of outfield players required in a starting lineup (e.g. 11). */
    int getRequiredLineupSize();

    /** Maximum players on the substitute bench. */
    int getMaxSubstituteCount();
}
