package com.sportmanager.basketball;

import com.sportmanager.core.League;
import com.sportmanager.core.Match;
import com.sportmanager.core.MatchResult;
import com.sportmanager.core.Sport;
import com.sportmanager.core.StandingEntry;
import com.sportmanager.core.Team;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BasketballLeague extends League {

    public BasketballLeague(String name, Sport sport) {
        super(name, sport);
    }

    @Override
    public void generateFixtures() {
        rounds.clear();
        int n = teams.size();
        if (n < 2) return;

        List<Team> rotation = new ArrayList<>(teams);
        if (n % 2 != 0) rotation.add(null);
        int size = rotation.size();
        int totalRounds = size - 1;
        List<List<Match>> firstLeg = new ArrayList<>();

        for (int round = 0; round < totalRounds; round++) {
            int weekNo = round + 1;
            List<Match> matches = new ArrayList<>();
            for (int i = 0; i < size / 2; i++) {
                Team home = rotation.get(i);
                Team away = rotation.get(size - 1 - i);
                if (home != null && away != null) {
                    matches.add(sport.createMatch(home, away, weekNo));
                }
            }
            firstLeg.add(matches);
            Team last = rotation.remove(size - 1);
            rotation.add(1, last);
        }

        List<List<Match>> secondLeg = new ArrayList<>();
        for (int r = 0; r < firstLeg.size(); r++) {
            int weekNo = firstLeg.size() + r + 1;
            List<Match> matches = new ArrayList<>();
            for (Match m : firstLeg.get(r)) {
                matches.add(sport.createMatch(m.getAwayTeam(), m.getHomeTeam(), weekNo));
            }
            secondLeg.add(matches);
        }

        rounds.addAll(firstLeg);
        rounds.addAll(secondLeg);
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

    @Override
    public void updateStandings(MatchResult result) {
        Team home = result.getHomeTeam();
        Team away = result.getAwayTeam();
        int h = result.getHomeScore();
        int a = result.getAwayScore();
        switch (result.getOutcome()) {
            case MatchResult.HOME_WIN -> {
                home.recordWin(h, a);
                away.recordLoss(a, h);
            }
            case MatchResult.AWAY_WIN -> {
                away.recordWin(a, h);
                home.recordLoss(h, a);
            }
            default -> {
                home.recordDraw(h, a);
                away.recordDraw(a, h);
            }
        }
    }

    @Override
    public List<Team> getSortedStandings() {
        return teams.stream()
                .sorted(Comparator.comparingInt(Team::getPoints)
                        .thenComparingInt(Team::getGoalDifference)
                        .thenComparingInt(Team::getGoalsFor)
                        .reversed())
                .toList();
    }

    @Override
    public Team getChampion() {
        List<Team> sorted = getSortedStandings();
        return sorted.isEmpty() ? null : sorted.get(0);
    }
}