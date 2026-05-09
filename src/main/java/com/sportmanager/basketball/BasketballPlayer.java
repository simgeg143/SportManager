package com.sportmanager.basketball;

import com.sportmanager.core.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class BasketballPlayer extends Player {

    private final int shooting;
    private final int passing;
    private final int defense;
    private final int rebounding;
    private final int athleticism;

    public BasketballPlayer(String name, String position, int skillLevel, Random rng) {
        super(name, 21, position, skillLevel);
        int base = skillLevel;
        this.shooting = clamp(base + rng.nextInt(21) - 10);
        this.passing = clamp(base + rng.nextInt(21) - 10);
        this.defense = clamp(base + rng.nextInt(21) - 10);
        this.rebounding = clamp(base + rng.nextInt(21) - 10);
        this.athleticism = clamp(base + rng.nextInt(21) - 10);
    }

    @Override
    public Map<String, Integer> getSpecificAttributes() {
        Map<String, Integer> attrs = new LinkedHashMap<>();
        attrs.put("SHT", shooting);
        attrs.put("PAS", passing);
        attrs.put("DEF", defense);
        attrs.put("REB", rebounding);
        attrs.put("ATH", athleticism);
        return attrs;
    }

    @Override
    public String getStatusDisplay() {
        return isInjured() ? "INJ (" + injuryMatchesRemaining + ")" : "FIT";
    }

    public double getAttackScore() {
        return shooting * 0.5 + passing * 0.25 + athleticism * 0.25;
    }

    public double getDefenseScore() {
        return defense * 0.55 + rebounding * 0.25 + athleticism * 0.2;
    }

    private static int clamp(int value) {
        return Math.max(40, Math.min(99, value));
    }
}
