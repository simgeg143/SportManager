package com.sportmanager.football;

import com.sportmanager.core.*;
import com.sportmanager.session.GameSession;
import com.sportmanager.settings.AppSettings;

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
    private MatchSegment activeSegment;
    private int liveEventsRemaining;
    private boolean segmentStarted;

    private static final java.util.Map<String, double[]> TACTIC_MODS =
            java.util.Map.of(
                    "4-3-3", new double[]{1.10, 0.95},
                    "4-4-2", new double[]{1.00, 1.00},
                    "4-2-3-1", new double[]{1.05, 1.00},
                    "3-5-2", new double[]{1.00, 0.90},
                    "5-3-2", new double[]{0.90, 1.10}
            );

    public FootballMatch(Team home, Team away, int weekNo) {
        super(home, away, weekNo);
        this.rng = new Random();
    }

    // ── Match abstract implementation ─────────────────────────────────────────

    @Override
    public int getTotalSegments() {
        return TOTAL_SEGMENTS;
    }

    @Override
    public boolean isAtBreak() {
        return currentSegment == 1 && !finished;
    }

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
        beginSegmentSimulation();
        while (hasPendingSegmentEvents()) {
            simulateNextSegmentEvent();
        }
    }

    @Override
    public void beginSegmentSimulation() {
        if (finished || segmentStarted) return;
        String label = getSegmentLabel(currentSegment);
        activeSegment = new MatchSegment(currentSegment, label);
        activeSegment.addEvent("── " + label.toUpperCase() + " ──");
        segments.add(activeSegment);
        liveEventsRemaining = 9 + rng.nextInt(4);
        segmentStarted = true;
    }

    @Override
    public boolean hasPendingSegmentEvents() {
        return segmentStarted && liveEventsRemaining >= 0 && !finished;
    }

    @Override
    public String simulateNextSegmentEvent() {
        if (!segmentStarted || activeSegment == null) return null;
        if (liveEventsRemaining == 0) {
            String end = "End of " + activeSegment.getLabel() + ": " + homeScore + " - " + awayScore;
            activeSegment.addEvent(end);
            events.add(end);
            liveEventsRemaining = -1;
            segmentStarted = false;
            currentSegment++;
            if (currentSegment >= TOTAL_SEGMENTS) {
                finished = true;
                String ft = "Full Time: " + homeTeam.getName() + " "
                        + homeScore + " – " + awayScore + " " + awayTeam.getName();
                activeSegment.addEvent(ft);
                events.add(ft);
                buildResult();
            }
            return end;
        }

        liveEventsRemaining--;
        String ev = generateLiveEvent();
        activeSegment.addEvent(ev);
        events.add(ev);
        return ev;
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

    private String generateLiveEvent() {
        boolean homeAttack = rng.nextBoolean();
        Team attacker = homeAttack ? homeTeam : awayTeam;
        Team defender = homeAttack ? awayTeam : homeTeam;

        double atk = applyDifficulty(attacker, teamAttack(attacker));
        double def = applyDifficulty(defender, teamDefense(defender));
        double[] aMods = tacticMods(attacker.getCurrentTactic().getName());
        double[] dMods = tacticMods(defender.getCurrentTactic().getName());
        atk *= aMods[0];
        def *= dMods[1];
        double ratio = atk / (atk + def);
        double goalProb = 0.09 + (ratio - 0.5) * 0.30;
        goalProb = Math.max(0.03, Math.min(0.24, goalProb));
        if (rng.nextDouble() < goalProb) {
            String scorer = randomPlayerName(attacker.getStartingLineup(), "GK");
            if (homeAttack) {
                homeScore++;
                activeSegment.addHomeGoal();
            } else {
                awayScore++;
                activeSegment.addAwayGoal();
            }
            return "⚽ GOAL!  " + scorer + " (" + attacker.getName() + ")";
        }
        if (rng.nextDouble() < AppSettings.getInstance().getInjuryChance() * 0.8) {
            Player injured = attacker.getStartingLineup().isEmpty() ? null
                    : attacker.getStartingLineup().get(rng.nextInt(attacker.getStartingLineup().size()));
            if (injured != null && !injured.isInjured()) {
                int dur = 1 + rng.nextInt(3);
                injured.setInjuryMatchesRemaining(dur);
                return "🚑 " + injured.getName() + " injured (" + dur + " match(es))";
            }
        }
        if (rng.nextDouble() < 0.22) {
            String carded = randomPlayerName(defender.getStartingLineup(), "");
            return "🟨 Yellow card – " + carded + " (" + defender.getName() + ")";
        }

        return "• Possession battle between " + attacker.getName() + " and " + defender.getName();
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

    /**
     * Applies the difficulty multiplier to AI (non-managed) teams.
     * The managed team's score is never modified so difficulty only affects opponents.
     */
    private double applyDifficulty(Team team, double score) {
        Team managed = GameSession.getInstance().getManagedTeam();
        if (team == managed) return score;
        return score * AppSettings.getInstance().getDifficultyMultiplier();
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
