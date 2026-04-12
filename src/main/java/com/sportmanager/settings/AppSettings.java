package com.sportmanager.settings;

/**
 * Singleton that holds all user-configurable settings for the application.
 * Settings take effect immediately; no persistence to disk (resets on restart).
 *
 * Access via:  AppSettings.getInstance().getSomeSetting()
 */
public class AppSettings {

    private static final AppSettings INSTANCE = new AppSettings();

    // ── Gameplay ──────────────────────────────────────────────────────────────
    private int     maxSubstitutions = 3;        // 3 or 5
    private String  injuryFrequency  = "Normal"; // Low / Normal / High
    private String  difficulty       = "Normal"; // Easy / Normal / Hard
    private boolean autoAdvance      = false;    // auto-advance after match ends
    private int     startYear        = 2025;     // season starting year

    // ── Display ───────────────────────────────────────────────────────────────
    private String  accentTheme         = "Teal"; // Teal / Amber / Purple
    private boolean showDetailedEvents  = true;   // show all events vs goals/cards only

    private AppSettings() {}

    public static AppSettings getInstance() { return INSTANCE; }

    // ── Derived game-logic values ─────────────────────────────────────────────

    /** Per-player injury probability per match segment (base 0.04). */
    public double getInjuryChance() {
        return switch (injuryFrequency) {
            case "Low"  -> 0.02;
            case "High" -> 0.08;
            default     -> 0.04;
        };
    }

    /**
     * Attack/defense multiplier applied to AI (non-managed) teams only.
     *   Easy  → AI is 25 % weaker   (player-friendly)
     *   Normal → no change
     *   Hard  → AI is 30 % stronger  (challenge)
     */
    public double getDifficultyMultiplier() {
        return switch (difficulty) {
            case "Easy" -> 0.75;
            case "Hard" -> 1.30;
            default     -> 1.00;
        };
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public int     getMaxSubstitutions()   { return maxSubstitutions; }
    public String  getInjuryFrequency()    { return injuryFrequency;  }
    public String  getDifficulty()         { return difficulty;       }
    public boolean isAutoAdvance()         { return autoAdvance;      }
    public int     getStartYear()          { return startYear;        }
    public String  getAccentTheme()        { return accentTheme;      }
    public boolean isShowDetailedEvents()  { return showDetailedEvents; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setMaxSubstitutions(int v)       { maxSubstitutions  = v; }
    public void setInjuryFrequency(String v)     { injuryFrequency   = v; }
    public void setDifficulty(String v)          { difficulty        = v; }
    public void setAutoAdvance(boolean v)        { autoAdvance       = v; }
    public void setStartYear(int v)              { startYear         = v; }
    public void setAccentTheme(String v)         { accentTheme       = v; }
    public void setShowDetailedEvents(boolean v) { showDetailedEvents = v; }

    /** Resets all settings to defaults. */
    public void resetDefaults() {
        maxSubstitutions   = 3;
        injuryFrequency    = "Normal";
        difficulty         = "Normal";
        autoAdvance        = false;
        startYear          = 2025;
        accentTheme        = "Teal";
        showDetailedEvents = true;
    }
}
