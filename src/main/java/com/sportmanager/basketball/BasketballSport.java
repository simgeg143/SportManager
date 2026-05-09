package com.sportmanager.basketball;

import com.sportmanager.core.Coach;
import com.sportmanager.core.League;
import com.sportmanager.core.Match;
import com.sportmanager.core.Sport;
import com.sportmanager.core.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BasketballSport implements Sport {

    private static final int PLAYERS_PER_TEAM = 12;
    private static final List<String> POSITIONS = List.of("PG", "SG", "SF", "PF", "C");
    private static final List<String> POSITION_SLOTS = List.of(
            "PG", "SG", "SF", "PF", "C",
            "PG", "SG", "SF", "PF", "C",
            "SG", "PF"
    );

    /** Not persisted — {@link Random} is not {@link java.io.Serializable}. */
    private transient Random rng;

    private Random rng() {
        if (rng == null) rng = new Random();
        return rng;
    }

    private final List<String> firstNames = new ArrayList<>(Arrays.asList(
            "James", "Luka", "Jayson", "Nikola", "Anthony", "Damian", "Devin", "Donovan", "Shai", "Kawhi"
    ));
    private final List<String> lastNames = new ArrayList<>(Arrays.asList(
            "Carter", "Brooks", "Miller", "Wright", "Brown", "Turner", "Fisher", "Murphy", "Collins", "Reed"
    ));
    private final List<String> teamNames = new ArrayList<>(Arrays.asList(
            "Anatolia Hoops", "Bosphorus Ballers", "Capital Kings", "Aegean Waves", "Golden Horn Giants",
            "Metro Falcons", "Izmir Shooters", "Black Sea Crew", "Silver Court", "Thunder Dunkers",
            "Skyline Titans", "Phoenix Riders", "Iron Rim", "Blue Arena", "North Stars", "South Bears",
            "East Hawks", "West Lions", "Royal Dribblers", "City Breakers"
    ));

    @Override
    public String getName() {
        return "Basketball";
    }

    @Override
    public List<String> getPositions() {
        return POSITIONS;
    }

    @Override
    public List<String> getTactics() {
        return BasketballTactics.OFFENSIVE_SETS;
    }

    @Override
    public int getSegmentCount() {
        return 4;
    }

    @Override
    public String getSegmentLabel() {
        return "Quarter";
    }

    @Override
    public int getRequiredLineupSize() {
        return BasketballTeam.LINEUP_SIZE;
    }

    @Override
    public int getMaxSubstituteCount() {
        return BasketballTeam.MAX_SUBS;
    }

    @Override
    public League createLeague(String leagueName, int teamCount) {
        BasketballLeague league = new BasketballLeague(leagueName, this);
        List<String> shuffled = new ArrayList<>(teamNames);
        Collections.shuffle(shuffled, rng());

        for (int i = 0; i < teamCount; i++) {
            String name = i < shuffled.size() ? shuffled.get(i) : "Basketball Team " + (i + 1);
            BasketballTeam team = new BasketballTeam(name);
            populateRoster(team);
            populateCoaches(team);
            team.generateDefaultLineup();
            league.getTeams().add(team);
        }

        league.generateFixtures();
        return league;
    }

    @Override
    public Team createTeam(String name) {
        return new BasketballTeam(name);
    }

    @Override
    public Match createMatch(Team home, Team away, int weekNo) {
        return new BasketballMatch(home, away, weekNo);
    }

    private void populateRoster(BasketballTeam team) {
        for (int i = 0; i < PLAYERS_PER_TEAM; i++) {
            String slot = POSITION_SLOTS.get(i);
            int skill = 55 + rng().nextInt(35);
            team.getRoster().add(new BasketballPlayer(randomName(), slot, skill, rng()));
        }
    }

    private void populateCoaches(BasketballTeam team) {
        team.getCoaches().add(new Coach(randomName(), "Head Coach", "5-Out", 8, 8));
        team.getCoaches().add(new Coach(randomName(), "Assistant Coach", "Pick & Roll", 7, 7));
    }

    private String randomName() {
        return firstNames.get(rng().nextInt(firstNames.size())) + " " + lastNames.get(rng().nextInt(lastNames.size()));
    }
}
