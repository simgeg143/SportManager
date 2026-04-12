package com.sportmanager.football;

import com.sportmanager.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Football match: two halves, goal-based scoring, injury and yellow-card events.
 * Each simulated segment creates a MatchSegment with partial scores and events.
 * A MatchResult is produced when the match finishes.
 */
public class FootballMatch extends Match {

    private static final int TOTAL_SEGMENTS = 2;
    private final Random rng;

    private static final java.util.Map<String, double[]> TACTIC_MODS =
            java.util.Map.of(
                    "4-3-3",   new double[]{1.10, 0.95},
                    "4-4-2",   new double[]{1.00, 1.00},
                    "4-2-3-1", new double[]{1.05, 1.00},
                    "3-5-2",   new double[]{1.00, 0.90},
                    "5-3-2",   new double[]{0.90, 1.10}
            );

    public FootballMatch(Team home, Team away, int weekNo) {
        super(home, away, weekNo);
        this.rng = new Random();
    }

    // ── Match abstract implementation ─────────────────────────────────────────

    @Override public int     getTotalSegments() { return TOTAL_SEGMENTS; }
    @Override public boolean isAtBreak()        { return currentSegment == 1 && !finished; }

    @Override
    public String getSegmentLabel(int idx) {
        return switch (idx) {
            case 0 -> "First Half";
            case 1 -> "Second Half";
            default -> "Extra Time";
        };
    }

    @Override
    public void simulateSegment() {
        if (finished) return;

        String label = getSegmentLabel(currentSegment);
        MatchSegment segment = new MatchSegment(currentSegment, label);
        segment.addEvent("── " + label.toUpperCase() + " ──");

        double homeAtk = teamAttack(homeTeam) * 1.05;     // home advantage
        double homeDef = teamDefense(homeTeam);
        double awayAtk = teamAttack(awayTeam);
        double awayDef = teamDefense(awayTeam);

        double[] homeMods = tacticMods(homeTeam.getCurrentTactic());
        double[] awayMods = tacticMods(awayTeam.getCurrentTactic());
        homeAtk *= homeMods[0]; homeDef *= homeMods[1];
        awayAtk *= awayMods[0]; awayDef *= awayMods[1];

        int hGoals = simulateGoals(homeAtk, awayDef, homeTeam, segment);
        int aGoals = simulateGoals(awayAtk, homeDef, awayTeam, segment);

        homeScore += hGoals;
        awayScore += aGoals;
        for (int g = 0; g < hGoals; g++) segment.addHomeGoal();
        for (int g = 0; g < aGoals; g++) segment.addAwayGoal();

        simulateIncidents(homeTeam, segment);
        simulateIncidents(awayTeam, segment);

        // Copy segment events to the flat events list (backward compat)
        events.addAll(segment.getEvents());
        segments.add(segment);

        currentSegment++;
        if (currentSegment >= TOTAL_SEGMENTS) {
            finished = true;
            String ft = "Full Time: " + homeTeam.getName() + " "
                    + homeScore + " – " + awayScore + " " + awayTeam.getName();
            segment.addEvent(ft);
            events.add(ft);
            buildResult();
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void buildResult() {
        List<InjuryRecord> injuries = new ArrayList<>();

        // Collect all players who were injured during simulation.
        // simulateIncidents() sets injuryMatchesRemaining directly on the Player,
        // so we scan both teams' starting XIs and benches after the match.
        for (Team team : List.of(homeTeam, awayTeam)) {
            for (Player p : team.getStartingLineup()) {
                if (p.isInjured())
                    injuries.add(new InjuryRecord(p, p.getInjuryMatchesRemaining(), "Injured in match"));
            }
            for (Player p : team.getSubstitutes()) {
                if (p.isInjured())
                    injuries.add(new InjuryRecord(p, p.getInjuryMatchesRemaining(), "Injured in warm-up"));
            }
        }
        this.result = new MatchResult(homeTeam, awayTeam, homeScore, awayScore, injuries);
    }

    private int simulateGoals(double attack, double defense, Team attacker, MatchSegment seg) {
        double ratio    = attack / (attack + defense);
        double goalProb = 0.12 + (ratio - 0.5) * 0.28;
        int    chances  = 3 + rng.nextInt(4);
        int    goals    = 0;
        for (int i = 0; i < chances; i++) {
            if (rng.nextDouble() < goalProb) {
                goals++;
                String scorer = randomPlayerName(attacker.getStartingLineup(), "GK");
                seg.addEvent("⚽ GOAL!  " + scorer + " (" + attacker.getName() + ")");
            }
        }
        return goals;
    }

    private void simulateIncidents(Team team, MatchSegment seg) {
        for (Player p : team.getStartingLineup()) {
            if (!p.isInjured() && rng.nextDouble() < 0.04) {
                int dur = 1 + rng.nextInt(3);
                p.setInjuryMatchesRemaining(dur);
                seg.addEvent("🚑 INJURY  " + p.getName() + " (" + team.getName()
                        + ") – out for " + dur + " match(es)");
            }
        }
        if (!team.getStartingLineup().isEmpty() && rng.nextDouble() < 0.35) {
            String carded = randomPlayerName(team.getStartingLineup(), "");
            seg.addEvent("🟨 Yellow card – " + carded + " (" + team.getName() + ")");
        }
    }

    private double teamAttack(Team team) {
        List<Player> xi = team.getStartingLineup();
        if (xi.isEmpty()) return 60.0;
        return xi.stream()
                .mapToDouble(p -> p instanceof FootballPlayer fp
                        ? fp.getAttackScore()
                        : p.getSkillLevel() * 0.8)
                .average().orElse(60.0);
    }

    private double teamDefense(Team team) {
        List<Player> xi = team.getStartingLineup();
        if (xi.isEmpty()) return 60.0;
        return xi.stream()
                .mapToDouble(p -> p instanceof FootballPlayer fp
                        ? fp.getDefenseScore()
                        : p.getSkillLevel() * 0.8)
                .average().orElse(60.0);
    }

    private double[] tacticMods(String tactic) {
        return TACTIC_MODS.getOrDefault(tactic, new double[]{1.0, 1.0});
    }

    private String randomPlayerName(List<Player> players, String excludePos) {
        List<Player> eligible = players.stream()
                .filter(p -> !p.getPosition().equals(excludePos))
                .collect(Collectors.toList());
        if (eligible.isEmpty()) return "Unknown";
        return eligible.get(rng.nextInt(eligible.size())).getName();
    }
}
