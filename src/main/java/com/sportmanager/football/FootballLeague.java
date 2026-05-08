package com.sportmanager.football;

import com.sportmanager.core.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Football league: full home-and-away round-robin fixture generation,
 * standings ordered by Pts → GD → GF, and updateStandings() via MatchResult.
 */
public class FootballLeague extends League {

    public FootballLeague(String name, Sport sport) {
        super(name, sport);
    }

    // ── Fixture generation (circle-method round-robin) ────────────────────────

    @Override
    public void generateFixtures() {
        rounds.clear();
        int n = teams.size();
        if (n < 2) return;

        // Circle method requires an even number of teams.
        // If odd, add a null sentinel as a "bye" slot — any match involving
        // null is simply not added to the round, giving that team a bye.
        List<Team> rotation = new ArrayList<>(teams);
        boolean hasBye = (n % 2 != 0);
        if (hasBye) rotation.add(null);          // null == BYE

        int size = rotation.size();              // now always even
        int totalRounds = size - 1;
        List<List<Match>> homeRounds = new ArrayList<>();

        for (int round = 0; round < totalRounds; round++) {
            int weekNo = round + 1;
            List<Match> roundMatches = new ArrayList<>();
            for (int i = 0; i < size / 2; i++) {
                Team home = rotation.get(i);
                Team away = rotation.get(size - 1 - i);
                if (home != null && away != null) {         // skip bye slots
                    roundMatches.add(sport.createMatch(home, away, weekNo));
                }
            }
            homeRounds.add(roundMatches);

            // Rotate: fix element 0, shift the rest one position clockwise
            Team last = rotation.remove(size - 1);
            rotation.add(1, last);
        }

        // Away legs: reverse every home fixture
        List<List<Match>> awayRounds = new ArrayList<>();
        for (int r = 0; r < homeRounds.size(); r++) {
            int weekNo = homeRounds.size() + r + 1;
            List<Match> reverseRound = new ArrayList<>();
            for (Match m : homeRounds.get(r)) {
                reverseRound.add(sport.createMatch(m.getAwayTeam(), m.getHomeTeam(), weekNo));
            }
            awayRounds.add(reverseRound);
        }

        rounds.addAll(homeRounds);
        rounds.addAll(awayRounds);
    }

    // ── Standings ─────────────────────────────────────────────────────────────

    @Override
    public List<Team> getSortedStandings() {
        return teams.stream()
                .sorted(Comparator.comparingInt(Team::getPoints)
                        .thenComparingInt(Team::getScoreDifference)
                        .thenComparingInt(Team::getScoreFor)
                        .reversed())
                .toList();
    }

    @Override
    public List<StandingEntry> getTable() {
        List<Team> sorted = getSortedStandings();
        List<StandingEntry> table = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            table.add(new StandingEntry(sorted.get(i), i + 1));
        }
        return table;
    }

    /**
     * Applies a MatchResult to the two participating teams' season records.
     * Football points: Win = 3, Draw = 1, Loss = 0. (MS-5)
     */
    @Override
    public void updateStandings(MatchResult result) {
        Team home   = result.getHomeTeam();
        Team away   = result.getAwayTeam();
        int  hScore = result.getHomeScore();
        int  aScore = result.getAwayScore();

        switch (result.getOutcome()) {
            case MatchResult.HOME_WIN -> { home.recordWin(hScore, aScore);  away.recordLoss(aScore, hScore); }
            case MatchResult.AWAY_WIN -> { away.recordWin(aScore, hScore);  home.recordLoss(hScore, aScore); }
            default                   -> { home.recordDraw(hScore, aScore); away.recordDraw(aScore, hScore); }
        }
    }

    @Override
    public Team getChampion() {
        List<Team> sorted = getSortedStandings();
        return sorted.isEmpty() ? null : sorted.get(0);
    }
}
