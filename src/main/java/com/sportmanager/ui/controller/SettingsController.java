package com.sportmanager.ui.controller;

import com.sportmanager.SceneManager;
import com.sportmanager.settings.AppSettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Settings screen controller.
 * Reads current AppSettings on load, lets the user adjust all options,
 * and writes them back on "Save Settings".  Theme changes are applied
 * instantly via SceneManager.applyTheme().
 */
public class SettingsController implements Initializable {

    // ── Gameplay toggles ─────────────────────────────────────────────────────
    @FXML private ToggleButton subs3Btn;
    @FXML private ToggleButton subs5Btn;

    @FXML private ToggleButton injLowBtn;
    @FXML private ToggleButton injNormalBtn;
    @FXML private ToggleButton injHighBtn;
    @FXML private Label        injuryHintLabel;

    @FXML private ToggleButton diffEasyBtn;
    @FXML private ToggleButton diffNormalBtn;
    @FXML private ToggleButton diffHardBtn;
    @FXML private Label        difficultyHintLabel;
    @FXML private ToggleButton botBeginnerBtn;
    @FXML private ToggleButton botStreetBtn;
    @FXML private ToggleButton botSemiProBtn;
    @FXML private ToggleButton botProBtn;
    @FXML private Label        botModeHintLabel;

    @FXML private ToggleButton timeoutCasualBtn;
    @FXML private ToggleButton timeoutStandardBtn;
    @FXML private ToggleButton timeoutStrictBtn;
    @FXML private Label        timeoutHintLabel;

    @FXML private CheckBox autoAdvanceCheck;
    @FXML private TextField startYearField;

    // ── Display toggles ──────────────────────────────────────────────────────
    @FXML private ToggleButton themeTealBtn;
    @FXML private ToggleButton themeAmberBtn;
    @FXML private ToggleButton themePurpleBtn;

    @FXML private CheckBox detailedEventsCheck;

    // ── Status label ─────────────────────────────────────────────────────────
    @FXML private Label statusLabel;

    private final AppSettings settings = AppSettings.getInstance();

    // ── Toggle groups (enforced in Java — FXML ToggleGroup binding is fragile) ─

    private final ToggleGroup subsGroup    = new ToggleGroup();
    private final ToggleGroup injGroup     = new ToggleGroup();
    private final ToggleGroup diffGroup    = new ToggleGroup();
    private final ToggleGroup botGroup     = new ToggleGroup();
    private final ToggleGroup timeoutGroup = new ToggleGroup();
    private final ToggleGroup themeGroup   = new ToggleGroup();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Register toggle groups
        subs3Btn.setToggleGroup(subsGroup);
        subs5Btn.setToggleGroup(subsGroup);

        injLowBtn.setToggleGroup(injGroup);
        injNormalBtn.setToggleGroup(injGroup);
        injHighBtn.setToggleGroup(injGroup);

        diffEasyBtn.setToggleGroup(diffGroup);
        diffNormalBtn.setToggleGroup(diffGroup);
        diffHardBtn.setToggleGroup(diffGroup);
        botBeginnerBtn.setToggleGroup(botGroup);
        botStreetBtn.setToggleGroup(botGroup);
        botSemiProBtn.setToggleGroup(botGroup);
        botProBtn.setToggleGroup(botGroup);

        timeoutCasualBtn.setToggleGroup(timeoutGroup);
        timeoutStandardBtn.setToggleGroup(timeoutGroup);
        timeoutStrictBtn.setToggleGroup(timeoutGroup);

        themeTealBtn.setToggleGroup(themeGroup);
        themeAmberBtn.setToggleGroup(themeGroup);
        themePurpleBtn.setToggleGroup(themeGroup);

        loadCurrentSettings();
    }

    // ── Load → UI ─────────────────────────────────────────────────────────────

    private void loadCurrentSettings() {
        // Max subs
        if (settings.getMaxSubstitutions() == 5) subs5Btn.setSelected(true);
        else                                      subs3Btn.setSelected(true);

        // Injury frequency
        switch (settings.getInjuryFrequency()) {
            case "Low"  -> injLowBtn.setSelected(true);
            case "High" -> injHighBtn.setSelected(true);
            default     -> injNormalBtn.setSelected(true);
        }
        updateInjuryHint(settings.getInjuryFrequency());

        // Difficulty
        switch (settings.getDifficulty()) {
            case "Easy" -> diffEasyBtn.setSelected(true);
            case "Hard" -> diffHardBtn.setSelected(true);
            default     -> diffNormalBtn.setSelected(true);
        }
        updateDifficultyHint(settings.getDifficulty());

        switch (settings.getRivalBotMode()) {
            case "Beginner" -> botBeginnerBtn.setSelected(true);
            case "Street Player" -> botStreetBtn.setSelected(true);
            case "Professional" -> botProBtn.setSelected(true);
            default -> botSemiProBtn.setSelected(true);
        }
        updateBotModeHint(settings.getRivalBotMode());

        // Timeout presets
        switch (settings.getTimeoutPreset()) {
            case "Casual" -> timeoutCasualBtn.setSelected(true);
            case "Strict" -> timeoutStrictBtn.setSelected(true);
            default -> timeoutStandardBtn.setSelected(true);
        }
        updateTimeoutHint(settings.getTimeoutPreset());

        // Checkboxes
        autoAdvanceCheck.setSelected(settings.isAutoAdvance());
        detailedEventsCheck.setSelected(settings.isShowDetailedEvents());

        // Start year
        startYearField.setText(String.valueOf(settings.getStartYear()));

        // Theme
        switch (settings.getAccentTheme()) {
            case "Amber"  -> themeAmberBtn.setSelected(true);
            case "Purple" -> themePurpleBtn.setSelected(true);
            default       -> themeTealBtn.setSelected(true);
        }

        statusLabel.setText("");
    }

    // ── FXML handlers ─────────────────────────────────────────────────────────

    @FXML private void onSubsChanged() {
        statusLabel.setText("");
    }

    @FXML private void onInjuryChanged() {
        String val = injLowBtn.isSelected() ? "Low"
                   : injHighBtn.isSelected() ? "High" : "Normal";
        updateInjuryHint(val);
        statusLabel.setText("");
    }

    @FXML private void onDifficultyChanged() {
        String val = diffEasyBtn.isSelected() ? "Easy"
                   : diffHardBtn.isSelected()  ? "Hard" : "Normal";
        updateDifficultyHint(val);
        statusLabel.setText("");
    }

    @FXML private void onAutoAdvanceChanged() {
        statusLabel.setText("");
    }

    @FXML private void onBotModeChanged() {
        String val = botBeginnerBtn.isSelected() ? "Beginner"
                : botStreetBtn.isSelected() ? "Street Player"
                : botProBtn.isSelected() ? "Professional" : "Semi-Pro";
        updateBotModeHint(val);
        statusLabel.setText("");
    }

    @FXML private void onTimeoutPresetChanged() {
        String val = timeoutCasualBtn.isSelected() ? "Casual"
                : timeoutStrictBtn.isSelected() ? "Strict" : "Standard";
        updateTimeoutHint(val);
        statusLabel.setText("");
    }

    @FXML private void onDetailedEventsChanged() {
        statusLabel.setText("");
    }

    /** Apply theme immediately so the user sees the change live. */
    @FXML private void onThemeChanged() {
        String theme = themeAmberBtn.isSelected() ? "Amber"
                     : themePurpleBtn.isSelected() ? "Purple" : "Teal";
        settings.setAccentTheme(theme);
        SceneManager.getInstance().applyTheme(theme);
        statusLabel.setText("");
    }

    @FXML private void onSave() {
        // Subs
        settings.setMaxSubstitutions(subs5Btn.isSelected() ? 5 : 3);

        // Injury
        settings.setInjuryFrequency(
                injLowBtn.isSelected()  ? "Low"  :
                injHighBtn.isSelected() ? "High" : "Normal");

        // Difficulty
        settings.setDifficulty(
                diffEasyBtn.isSelected() ? "Easy" :
                diffHardBtn.isSelected() ? "Hard" : "Normal");
        settings.setRivalBotMode(
                botBeginnerBtn.isSelected() ? "Beginner" :
                botStreetBtn.isSelected() ? "Street Player" :
                botProBtn.isSelected() ? "Professional" : "Semi-Pro");

        settings.setTimeoutPreset(
                timeoutCasualBtn.isSelected() ? "Casual" :
                timeoutStrictBtn.isSelected() ? "Strict" : "Standard");

        // Checkboxes
        settings.setAutoAdvance(autoAdvanceCheck.isSelected());
        settings.setShowDetailedEvents(detailedEventsCheck.isSelected());

        // Start year
        try {
            int year = Integer.parseInt(startYearField.getText().trim());
            if (year >= 1900 && year <= 2100) settings.setStartYear(year);
            else startYearField.setText(String.valueOf(settings.getStartYear()));
        } catch (NumberFormatException ignored) {
            startYearField.setText(String.valueOf(settings.getStartYear()));
        }

        // Theme already applied live — just confirm accent saved
        String theme = themeAmberBtn.isSelected() ? "Amber"
                     : themePurpleBtn.isSelected() ? "Purple" : "Teal";
        settings.setAccentTheme(theme);

        statusLabel.setText("✓  Settings saved.");
    }

    @FXML private void onReset() {
        settings.resetDefaults();
        SceneManager.getInstance().applyTheme("Teal");
        loadCurrentSettings();
        statusLabel.setText("Settings reset to defaults.");
    }

    @FXML private void onBack() {
        SceneManager.getInstance().goBack();
    }

    // ── Hint helpers ──────────────────────────────────────────────────────────

    private void updateInjuryHint(String freq) {
        injuryHintLabel.setText(switch (freq) {
            case "Low"  -> "~2 % chance per player per half.";
            case "High" -> "~8 % chance per player per half. Expect a busy physio room.";
            default     -> "~4 % chance per player per half.";
        });
    }

    private void updateDifficultyHint(String diff) {
        difficultyHintLabel.setText(switch (diff) {
            case "Easy" -> "AI teams are 25 % weaker — good for learning the game.";
            case "Hard" -> "AI teams are 30 % stronger — a real challenge.";
            default     -> "Balanced — AI teams play at their natural strength.";
        });
    }

    private void updateTimeoutHint(String preset) {
        timeoutHintLabel.setText(switch (preset) {
            case "Casual" -> "More tactical pauses and timeout calls per match.";
            case "Strict" -> "Fewer timeout calls; decisions are more costly.";
            default -> "Balanced timeout budget for each sport.";
        });
    }

    private void updateBotModeHint(String mode) {
        botModeHintLabel.setText(switch (mode) {
            case "Beginner" -> "Minimal tactical reactions, rarely uses substitutions.";
            case "Street Player" -> "Occasional tactical switches and basic timeout usage.";
            case "Professional" -> "Aggressive tactical adaptation with frequent strategic decisions.";
            default -> "Balanced rival AI with practical in-match adjustments.";
        });
    }
}
