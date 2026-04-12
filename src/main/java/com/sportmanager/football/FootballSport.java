package com.sportmanager.football;

import com.sportmanager.core.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Concrete Sport implementation for Football.
 * Creates all football-specific domain objects and populates them
 * with randomly generated but realistic data.
 *
 * As specified in the architecture document, the factory uses
 * sport.createLeague(20) — 20 teams for a full Premier-League-style season.
 */
public class FootballSport implements Sport {

    private static final int PLAYERS_PER_TEAM = 22;

    // Positional slots per team (22 players covering all positions)
    private static final List<String> POSITION_SLOTS = List.of(
            "GK",  "GK",
            "CB",  "CB", "CB",
            "LB",  "LB",
            "RB",  "RB",
            "CDM", "CDM",
            "CM",  "CM", "CM",
            "CAM",
            "LW",  "RW",
            "CF",
            "ST",  "ST",
            "LB",  "CB"
    );

    private static final List<String> POSITIONS = List.of(
            "GK", "CB", "LB", "RB", "CDM", "CM", "CAM", "LW", "RW", "CF", "ST"
    );

    private static final List<String> TACTICS = List.of(
            "4-3-3", "4-4-2", "4-2-3-1", "3-5-2", "5-3-2"
    );

    // Coach role labels
    private static final String[] COACH_ROLES = {
            "Head Coach", "Assistant Coach", "Goalkeeping Coach", "Fitness Coach"
    };

    private final Random       rng = new Random();
    private List<String> firstNames;
    private List<String> lastNames;
    private List<String> teamNames;

    public FootballSport() {
        firstNames = loadResource("/com/sportmanager/data/player-first-names.txt");
        lastNames  = loadResource("/com/sportmanager/data/player-last-names.txt");
        teamNames  = loadResource("/com/sportmanager/data/team-names.txt");
        if (firstNames.isEmpty()) firstNames = defaultFirstNames();
        if (lastNames.isEmpty())  lastNames  = defaultLastNames();
        if (teamNames.isEmpty())  teamNames  = defaultTeamNames();
    }

    // ── Sport interface ───────────────────────────────────────────────────────

    @Override public String       getName()             { return "Football"; }
    @Override public List<String> getPositions()        { return POSITIONS; }
    @Override public List<String> getTactics()          { return TACTICS; }
    @Override public int          getSegmentCount()     { return 2; }
    @Override public String       getSegmentLabel()     { return "Half"; }
    @Override public int  getRequiredLineupSize() { return FootballTeam.LINEUP_SIZE; }
    @Override public int  getMaxSubstituteCount() { return FootballTeam.MAX_SUBS; }

    @Override
    public Team createTeam(String name) {
        return new FootballTeam(name);
    }

    @Override
    public Match createMatch(Team home, Team away, int weekNo) {
        return new FootballMatch(home, away, weekNo);
    }

    @Override
    public League createLeague(String leagueName, int teamCount) {
        FootballLeague league = new FootballLeague(leagueName, this);

        List<String> shuffled = new ArrayList<>(teamNames);
        Collections.shuffle(shuffled, rng);

        for (int i = 0; i < teamCount; i++) {
            String tName = i < shuffled.size() ? shuffled.get(i) : "Team " + (i + 1);
            FootballTeam team = (FootballTeam) createTeam(tName);
            populateRoster(team);
            populateCoaches(team);
            team.generateDefaultLineup();
            league.getTeams().add(team);
        }

        league.generateFixtures();
        return league;
    }

    // ── Roster & coach generation ─────────────────────────────────────────────

    private void populateRoster(FootballTeam team) {
        for (String slot : POSITION_SLOTS) {
            int skill = 55 + rng.nextInt(35);   // 55–89
            team.getRoster().add(new FootballPlayer(randomName(), slot, skill, rng));
        }
    }

    private void populateCoaches(FootballTeam team) {
        // Head Coach with high motivation, then supporting staff
        for (int i = 0; i < COACH_ROLES.length; i++) {
            int training    = 5 + rng.nextInt(5);   // 5–9
            int motivation  = 5 + rng.nextInt(5);
            String shape    = TACTICS.get(rng.nextInt(TACTICS.size()));
            team.getCoaches().add(
                    new Coach(randomName(), COACH_ROLES[i], shape, training, motivation));
        }
    }

    // ── Name generation ───────────────────────────────────────────────────────

    private String randomName() {
        return firstNames.get(rng.nextInt(firstNames.size()))
                + " " + lastNames.get(rng.nextInt(lastNames.size()));
    }

    // ── Resource loading ──────────────────────────────────────────────────────

    private List<String> loadResource(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) return Collections.emptyList();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                return br.lines()
                        .map(String::trim)
                        .filter(l -> !l.isEmpty() && !l.startsWith("#"))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // ── Fallback data ─────────────────────────────────────────────────────────

    private List<String> defaultFirstNames() {
        return new ArrayList<>(Arrays.asList(
                "James","Liam","Carlos","Diego","Marco","Mohamed","Yusuf","Kenji",
                "Lucas","Noah","Rafael","Pedro","Luis","Omar","Hiroshi","Kwame",
                "Ethan","Mason","Andres","Gabriel","Tariq","Kofi","Seo-Jin","Logan",
                "Oliver","Miguel","Juan","Khalid","Min-Jun","Benjamin","Amara","Chidi"
        ));
    }

    private List<String> defaultLastNames() {
        return new ArrayList<>(Arrays.asList(
                "Smith","Johnson","Rodriguez","Silva","Fernandez","Hassan","Yamamoto",
                "Kim","Williams","Taylor","Lopez","Santos","Ahmed","Nakamura","Park",
                "Mensah","Davies","Evans","Gonzalez","Costa","Ali","Tanaka","Lee",
                "Osei","Wilson","Thompson","Ferreira","Ibrahim","Diallo","White","Okafor"
        ));
    }

    private List<String> defaultTeamNames() {
        return new ArrayList<>(Arrays.asList(
                "Ironclad FC","Silverbridge United","Northgate City","Eastmoor Athletic",
                "Westfield Rovers","Southshore FC","Kingsley Town","Blackrock FC",
                "Ridgemont United","Halcyon City","Crestfall AFC","Thornwall FC",
                "Ironport Rangers","Ashvale City","Greymoor United",
                "Harborlight FC","Stormgate Athletic","Redcliff Rovers",
                "Copperfield United","Midlands City"
        ));
    }
}
