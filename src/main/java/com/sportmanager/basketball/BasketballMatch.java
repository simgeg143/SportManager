package com.sportmanager.basketball;

import com.sportmanager.core.InjuryRecord;
import com.sportmanager.core.Match;
import com.sportmanager.core.MatchResult;
import com.sportmanager.core.MatchSegment;
import com.sportmanager.core.Player;
import com.sportmanager.core.Team;
import com.sportmanager.session.GameSession;
import com.sportmanager.settings.AppSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * NBA-style game flow: four quarters, possession-based scoring, team fouls per quarter
 * (bonus FT pressure at 5+), turnovers, and offensive sets from {@link BasketballTactics}.
 */
public class BasketballMatch extends Match {

    private static final int TOTAL_SEGMENTS = 4;
    /** ~NBA pace scaled to our shorter event log per quarter. */
    private static final int POSSESSIONS_MIN = 10;
    private static final int POSSESSIONS_RANGE = 5;
    private final Random rng = new Random();
    private MatchSegment activeSegment;
    private List<Boolean> possessionOrder;
    private int possessionCursor;
    private QuarterFoulState liveFouls;
    private boolean segmentStarted;

    public BasketballMatch(Team home, Team away, int weekNo) {
        super(home, away, weekNo);
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
        activeSegment = new MatchSegment(currentSegment, getSegmentLabel(currentSegment));
        activeSegment.addEvent("── " + activeSegment.getLabel().toUpperCase() + " (12:00) ──");
        segments.add(activeSegment);

        int hPoss = POSSESSIONS_MIN + rng.nextInt(POSSESSIONS_RANGE);
        int aPoss = POSSESSIONS_MIN + rng.nextInt(POSSESSIONS_RANGE);
        possessionOrder = new ArrayList<>();
        int max = Math.max(hPoss, aPoss);
        for (int i = 0; i < max; i++) {
            if (i < hPoss) possessionOrder.add(true);
            if (i < aPoss) possessionOrder.add(false);
        }
        Collections.shuffle(possessionOrder, rng);
        possessionCursor = 0;
        liveFouls = new QuarterFoulState();
        segmentStarted = true;
    }

    @Override
    public boolean hasPendingSegmentEvents() {
        return segmentStarted && !finished;
    }

    @Override
    public String simulateNextSegmentEvent() {
        if (!segmentStarted || activeSegment == null) return null;
        if (possessionCursor >= possessionOrder.size()) {
            maybeInjure(homeTeam, activeSegment);
            maybeInjure(awayTeam, activeSegment);
            String end = String.format("— End %s: fouls %s %d | %d %s —",
                    activeSegment.getLabel(),
                    homeTeam.getName(), liveFouls.homeTeamFouls,
                    liveFouls.awayTeamFouls, awayTeam.getName());
            activeSegment.addEvent(end);
            events.add(end);
            segmentStarted = false;
            currentSegment++;
            if (currentSegment >= TOTAL_SEGMENTS) {
                finished = true;
                String ft = "Final: " + homeTeam.getName() + " " + homeScore + " - " + awayScore + " " + awayTeam.getName();
                activeSegment.addEvent(ft);
                events.add(ft);
                buildResult();
            }
            return end;
        }

        boolean homeBall = possessionOrder.get(possessionCursor++);
        String ev;
        if (homeBall) ev = runPossession(homeTeam, awayTeam, activeSegment, liveFouls, true);
        else ev = runPossession(awayTeam, homeTeam, activeSegment, liveFouls, false);
        activeSegment.addEvent(ev);
        events.add(ev);
        return ev;
    }

    @Override
    public int getTotalSegments() {
        return TOTAL_SEGMENTS;
    }

    @Override
    public String getSegmentLabel(int idx) {
        return "Q" + (idx + 1);
    }

    @Override
    public boolean isAtBreak() {
        // Allow tactical intervention at each quarter break before the final quarter:
        // after Q1, Q2, and Q3.
        return currentSegment > 0 && currentSegment < TOTAL_SEGMENTS && !finished;
    }

    private void simulateQuarter(Team home, Team away, MatchSegment segment, QuarterFoulState fouls) {
        int hPoss = POSSESSIONS_MIN + rng.nextInt(POSSESSIONS_RANGE);
        int aPoss = POSSESSIONS_MIN + rng.nextInt(POSSESSIONS_RANGE);
        List<Boolean> order = new ArrayList<>();
        int max = Math.max(hPoss, aPoss);
        for (int i = 0; i < max; i++) {
            if (i < hPoss) order.add(true);
            if (i < aPoss) order.add(false);
        }
        Collections.shuffle(order, rng);

        for (boolean homeBall : order) {
            if (homeBall) {
                runPossession(home, away, segment, fouls, true);
            } else {
                runPossession(away, home, segment, fouls, false);
            }
        }

        segment.addEvent(String.format("— End %s: fouls %s %d | %d %s —",
                segment.getLabel(),
                home.getName(), fouls.homeTeamFouls,
                fouls.awayTeamFouls, away.getName()));
    }

    private String runPossession(Team offense, Team defense, MatchSegment segment,
                                 QuarterFoulState fouls, boolean offenseIsHome) {
        String set = BasketballTactics.normalizeOffense(offense.getCurrentTactic().getName());

        double toChance = BasketballTactics.turnoverBaseChance(set);
        toChance *= (0.92 + rng.nextDouble() * 0.06);
        toChance += Math.max(0, (averageDefense(defense) - 72.0) * 0.0012);
        toChance = Math.min(0.24, toChance);

        if (rng.nextDouble() < toChance) {
            return "↩️ Turnover — " + offense.getName();
        }

        if (rng.nextDouble() < 0.11) {
            if (offenseIsHome) fouls.awayTeamFouls++;
            else fouls.homeTeamFouls++;

            int defFouls = offenseIsHome ? fouls.awayTeamFouls : fouls.homeTeamFouls;
            String foulEvent = "🟥 Team foul — " + defense.getName() + " (" + defFouls + ")";
            if (defFouls >= 5 && rng.nextDouble() < 0.88) {
                addPoints(offenseIsHome, 2);
                return foulEvent + " | 🏀 " + offense.getName() + " +2 FT (bonus)";
            }
            return foulEvent;
        }

        double off = averageAttack(offense) * BasketballTactics.offensiveRatingModifier(set);
        off = applyDifficulty(offense, off);
        double def = averageDefense(defense);
        def = applyDifficulty(defense, def);

        double pMake = 0.38 + (off / (off + def) - 0.5) * 0.38;
        pMake = Math.max(0.27, Math.min(0.64, pMake));

        if (rng.nextDouble() < pMake) {
            boolean three = rng.nextDouble() < BasketballTactics.threePointRate(set);
            int pts = three ? 3 : 2;
            addPoints(offenseIsHome, pts);
            String scorer = randomScorerName(offense);
            return "🏀 " + scorer + " " + pts + (three ? "PT (3FG)" : "PT (2FG)") + " — " + offense.getName();
        }

        if (rng.nextDouble() < 0.24) {
            if (rng.nextDouble() < 0.42) {
                addPoints(offenseIsHome, 2);
                return "🏀 Putback +2 — " + offense.getName();
            } else {
                return "❌ Miss — " + offense.getName();
            }
        } else {
            return "❌ Miss — " + offense.getName();
        }
    }

    private void addPoints(boolean offenseIsHome, int pts) {
        if (offenseIsHome) homeScore += pts;
        else awayScore += pts;
    }

    private double applyDifficulty(Team team, double score) {
        Team managed = GameSession.getInstance().getManagedTeam();
        if (team == managed) return score;
        return score * AppSettings.getInstance().getDifficultyMultiplier();
    }

    private String randomScorerName(Team team) {
        List<Player> xi = team.getStartingLineup();
        if (xi.isEmpty()) return "?";
        return xi.get(rng.nextInt(xi.size())).getName();
    }

    private double averageAttack(Team team) {
        return team.getStartingLineup().stream()
                .mapToDouble(p -> p instanceof BasketballPlayer bp ? bp.getAttackScore() : p.getSkillLevel())
                .average()
                .orElse(65.0);
    }

    private double averageDefense(Team team) {
        return team.getStartingLineup().stream()
                .mapToDouble(p -> p instanceof BasketballPlayer bp ? bp.getDefenseScore() : p.getSkillLevel())
                .average()
                .orElse(65.0);
    }

    private void maybeInjure(Team team, MatchSegment segment) {
        for (Player p : team.getStartingLineup()) {
            if (!p.isInjured() && rng.nextDouble() < AppSettings.getInstance().getInjuryChance() * 0.55) {
                int matches = 1 + rng.nextInt(2);
                p.setInjuryMatchesRemaining(matches);
                segment.addEvent("🚑 " + p.getName() + " injured (" + matches + " game(s))");
            }
        }
    }

    private void buildResult() {
        List<InjuryRecord> injuries = new ArrayList<>();
        for (Team team : List.of(homeTeam, awayTeam)) {
            for (Player p : team.getRoster()) {
                if (p.isInjured()) {
                    injuries.add(new InjuryRecord(p, p.getInjuryMatchesRemaining(), "Injured in game"));
                }
            }
        }
        this.result = new MatchResult(homeTeam, awayTeam, homeScore, awayScore, injuries);
    }

    private static final class QuarterFoulState {
        int homeTeamFouls;
        int awayTeamFouls;
    }
}
