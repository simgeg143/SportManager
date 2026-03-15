package com.sportmanager.football;

import com.sportmanager.core.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Football-specific player.
 * Extends the generic Player with five FIFA-style attributes (PAC, SHO, PAS, DEF, PHY)
 * that are derived from the base skill level with position-appropriate biases.
 */
public class FootballPlayer extends Player {

    private final int pace;
    private final int shooting;
    private final int passing;
    private final int defending;
    private final int physical;

    public FootballPlayer(String name, String position, int skillLevel, Random rng) {
        super(name, position, skillLevel);

        int base = skillLevel;
        int pac = clamp(base + rng.nextInt(21) - 10, 40, 99);
        int sho = clamp(base + rng.nextInt(21) - 10, 40, 99);
        int pas = clamp(base + rng.nextInt(21) - 10, 40, 99);
        int def = clamp(base + rng.nextInt(21) - 10, 40, 99);
        int phy = clamp(base + rng.nextInt(21) - 10, 40, 99);

        // Position biases: amplify relevant attributes
        switch (position) {
            case "GK"       -> { def = clamp(def + 20, 40, 99); sho = clamp(sho - 25, 40, 99); pac = clamp(pac - 10, 40, 99); }
            case "CB"       -> { def = clamp(def + 18, 40, 99); phy = clamp(phy + 10, 40, 99); sho = clamp(sho - 15, 40, 99); }
            case "LB", "RB" -> { def = clamp(def + 12, 40, 99); pac = clamp(pac + 8, 40, 99);  sho = clamp(sho - 10, 40, 99); }
            case "CDM"      -> { def = clamp(def + 12, 40, 99); pas = clamp(pas + 8, 40, 99);  sho = clamp(sho - 8, 40, 99); }
            case "CM"       -> { pas = clamp(pas + 12, 40, 99); phy = clamp(phy + 5, 40, 99); }
            case "CAM"      -> { pas = clamp(pas + 15, 40, 99); sho = clamp(sho + 8, 40, 99); def = clamp(def - 12, 40, 99); }
            case "LW", "RW" -> { pac = clamp(pac + 18, 40, 99); sho = clamp(sho + 8, 40, 99); def = clamp(def - 15, 40, 99); }
            case "CF"       -> { sho = clamp(sho + 12, 40, 99); pas = clamp(pas + 8, 40, 99); def = clamp(def - 12, 40, 99); }
            case "ST"       -> { sho = clamp(sho + 18, 40, 99); phy = clamp(phy + 8, 40, 99); def = clamp(def - 18, 40, 99); }
        }

        this.pace      = pac;
        this.shooting  = sho;
        this.passing   = pas;
        this.defending = def;
        this.physical  = phy;
    }

    // ── Attribute accessors ───────────────────────────────────────────────────

    public int getPace()      { return pace; }
    public int getShooting()  { return shooting; }
    public int getPassing()   { return passing; }
    public int getDefending() { return defending; }
    public int getPhysical()  { return physical; }

    @Override
    public Map<String, Integer> getSpecificAttributes() {
        Map<String, Integer> attrs = new LinkedHashMap<>();
        attrs.put("PAC", pace);
        attrs.put("SHO", shooting);
        attrs.put("PAS", passing);
        attrs.put("DEF", defending);
        attrs.put("PHY", physical);
        return attrs;
    }

    @Override
    public String getStatusDisplay() {
        return isInjured() ? "INJ (" + injuryMatchesRemaining + ")" : "FIT";
    }

    // ── Match simulation helpers ──────────────────────────────────────────────

    /** Offensive contribution used by FootballMatch goal probability. */
    public double getAttackScore() {
        return switch (position) {
            case "ST", "CF" -> shooting * 0.55 + passing  * 0.25 + pace     * 0.20;
            case "LW", "RW" -> shooting * 0.40 + pace     * 0.35 + passing  * 0.25;
            case "CAM"      -> passing  * 0.45 + shooting * 0.35 + pace     * 0.20;
            case "CM"       -> passing  * 0.50 + shooting * 0.25 + physical * 0.25;
            case "CDM"      -> passing  * 0.55 + defending* 0.25 + physical * 0.20;
            default         -> passing  * 0.40 + physical * 0.30 + defending* 0.30;
        };
    }

    /** Defensive contribution used by FootballMatch goal probability. */
    public double getDefenseScore() {
        return switch (position) {
            case "GK"       -> defending * 1.60;
            case "CB"       -> defending * 0.65 + physical * 0.25 + pace     * 0.10;
            case "LB", "RB" -> defending * 0.55 + pace     * 0.25 + physical * 0.20;
            case "CDM"      -> defending * 0.50 + physical * 0.30 + passing  * 0.20;
            case "CM"       -> defending * 0.30 + physical * 0.40 + passing  * 0.30;
            default         -> defending * 0.20 + physical * 0.35 + passing  * 0.45;
        };
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
