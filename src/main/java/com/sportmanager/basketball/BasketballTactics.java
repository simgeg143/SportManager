package com.sportmanager.basketball;

import java.util.List;
import java.util.Map;
import java.util.Set;
<<<<<<< Updated upstream


=======
<<<<<<< HEAD
/**
 * Offensive formations (NBA-style spacing) and simulation tuning.
 * Diagram dots: normalized half-court coords, x = sideline to sideline (0–1),
 * y = basket endline (0) toward mid-court (1). Attacking toward the top (y ≈ 0).
 */
=======


>>>>>>> 500fd5138fc06faaeedc747d2b64dae2724e5e08
>>>>>>> Stashed changes
public final class BasketballTactics {

    private BasketballTactics() {}

<<<<<<< Updated upstream
=======
<<<<<<< HEAD
    /** User-selectable offensive sets (shown in tactic combo + canvas). */
=======
>>>>>>> 500fd5138fc06faaeedc747d2b64dae2724e5e08
>>>>>>> Stashed changes
    public static final List<String> OFFENSIVE_SETS = List.of(
            "5-Out",
            "4-Out 1-In",
            "Horns",
            "Pick & Roll",
            "Post Split"
    );

    public static final String DEFAULT_OFFENSE = "5-Out";

    private static final Set<String> LEGACY_TACTIC_NAMES = Set.of(
            "Balanced", "Fast Break", "Half Court", "Zone Defense"
    );

<<<<<<< Updated upstream
=======
<<<<<<< HEAD
    /**
     * If {@code formation} is a basketball offensive set (or legacy label), returns the diagram key;
     * otherwise {@code null} so the canvas can draw football.
     */
=======
>>>>>>> 500fd5138fc06faaeedc747d2b64dae2724e5e08
>>>>>>> Stashed changes
    public static String diagramKeyForTactic(String formation) {
        if (formation == null || formation.isBlank()) return null;
        if (OFFENSE_DIAGRAM.containsKey(formation)) return formation;
        if (LEGACY_TACTIC_NAMES.contains(formation)) return DEFAULT_OFFENSE;
        return null;
    }

    public record Dot(double x, double y, String label) {}

<<<<<<< Updated upstream
=======
<<<<<<< HEAD
    /**
     * Five-man spacing for each set; labels match roster positions (PG–C).
     */
=======
>>>>>>> 500fd5138fc06faaeedc747d2b64dae2724e5e08
>>>>>>> Stashed changes
    public static final Map<String, List<Dot>> OFFENSE_DIAGRAM = Map.of(
            "5-Out", List.of(
                    new Dot(0.50, 0.86, "PG"),
                    new Dot(0.10, 0.30, "SG"),
                    new Dot(0.90, 0.30, "SF"),
                    new Dot(0.14, 0.62, "PF"),
                    new Dot(0.86, 0.62, "C")
            ),
            "4-Out 1-In", List.of(
                    new Dot(0.50, 0.84, "PG"),
                    new Dot(0.10, 0.38, "SG"),
                    new Dot(0.90, 0.38, "SF"),
                    new Dot(0.14, 0.68, "PF"),
                    new Dot(0.62, 0.22, "C")
            ),
            "Horns", List.of(
                    new Dot(0.50, 0.82, "PG"),
                    new Dot(0.34, 0.58, "PF"),
                    new Dot(0.66, 0.58, "C"),
                    new Dot(0.10, 0.32, "SG"),
                    new Dot(0.90, 0.32, "SF")
            ),
            "Pick & Roll", List.of(
                    new Dot(0.50, 0.84, "PG"),
                    new Dot(0.54, 0.58, "C"),
                    new Dot(0.22, 0.44, "PF"),
                    new Dot(0.88, 0.48, "SG"),
                    new Dot(0.12, 0.62, "SF")
            ),
            "Post Split", List.of(
                    new Dot(0.50, 0.84, "PG"),
                    new Dot(0.76, 0.20, "C"),
                    new Dot(0.28, 0.36, "PF"),
                    new Dot(0.10, 0.52, "SG"),
                    new Dot(0.90, 0.52, "SF")
            )
    );

<<<<<<< Updated upstream
=======
<<<<<<< HEAD
    /** Multiplier on effective offensive rating (pace / shot quality). */
=======
>>>>>>> 500fd5138fc06faaeedc747d2b64dae2724e5e08
>>>>>>> Stashed changes
    public static double offensiveRatingModifier(String tactic) {
        if (tactic == null) return 1.0;
        return switch (tactic) {
            case "5-Out" -> 1.06;
            case "4-Out 1-In" -> 1.03;
            case "Horns" -> 1.04;
            case "Pick & Roll" -> 1.08;
            case "Post Split" -> 1.02;
            default -> 1.0;
        };
    }

<<<<<<< Updated upstream
=======
<<<<<<< HEAD
    /** Probability a made field goal is a three (0–1). */
=======
>>>>>>> 500fd5138fc06faaeedc747d2b64dae2724e5e08
>>>>>>> Stashed changes
    public static double threePointRate(String tactic) {
        if (tactic == null) return 0.38;
        return switch (tactic) {
            case "5-Out" -> 0.52;
            case "4-Out 1-In" -> 0.28;
            case "Horns" -> 0.36;
            case "Pick & Roll" -> 0.34;
            case "Post Split" -> 0.22;
            default -> 0.36;
        };
    }

<<<<<<< Updated upstream
=======
<<<<<<< HEAD
    /** Base turnover chance per possession before defense modifier. */
=======
>>>>>>> 500fd5138fc06faaeedc747d2b64dae2724e5e08
>>>>>>> Stashed changes
    public static double turnoverBaseChance(String tactic) {
        if (tactic == null) return 0.12;
        return switch (tactic) {
            case "5-Out" -> 0.11;
            case "4-Out 1-In" -> 0.10;
            case "Horns" -> 0.11;
            case "Pick & Roll" -> 0.14;
            case "Post Split" -> 0.13;
            default -> 0.12;
        };
    }

<<<<<<< Updated upstream
=======
<<<<<<< HEAD
    /** Normalize unknown saved tactics to a valid set. */
=======
>>>>>>> 500fd5138fc06faaeedc747d2b64dae2724e5e08
>>>>>>> Stashed changes
    public static String normalizeOffense(String tactic) {
        if (tactic == null || tactic.isBlank()) return DEFAULT_OFFENSE;
        if (OFFENSE_DIAGRAM.containsKey(tactic)) return tactic;
        if (LEGACY_TACTIC_NAMES.contains(tactic)) return DEFAULT_OFFENSE;
        return DEFAULT_OFFENSE;
    }
}
