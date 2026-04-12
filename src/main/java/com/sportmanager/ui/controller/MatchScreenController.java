package com.sportmanager.ui.controller;

import com.sportmanager.SportManager;
import com.sportmanager.core.*;
import com.sportmanager.ui.component.TacticPitchCanvas;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * MatchController — pre-match setup (lineup, tactics) and live match display. (MS-1 to MS-5)
 *
 * Before match: shows lineup editor and tactic selector.
 * During match: shows each segment's events and the running score.
 * After match: shows full-time result and the "Continue" button that calls
 *              SportManager.advanceWeek() to finalise the round.
 *
 * The controller never imports concrete sport classes; all data comes via
 * SportManager and the abstract Match / Team / Player types.
 */
public class MatchScreenController implements Initializable {

    // ── Score & header ────────────────────────────────────────────────────────
    @FXML private Label homeTeamLabel;
    @FXML private Label awayTeamLabel;
    @FXML private Label scoreLabel;
    @FXML private Label matchInfoLabel;

    // ── Events area ───────────────────────────────────────────────────────────
    @FXML private VBox  eventsBox;

    // ── Control buttons ───────────────────────────────────────────────────────
    @FXML private Button  simulateButton;
    @FXML private Button  continueButton;

    // ── Substitution panel ────────────────────────────────────────────────────
    @FXML private VBox    subPanel;
    @FXML private ComboBox<Player> removePlayerCombo;
    @FXML private ComboBox<Player> addPlayerCombo;
    @FXML private Label   subCountLabel;

    // ── Tactic panel ─────────────────────────────────────────────────────────
    @FXML private ComboBox<String>  tacticCombo;
    @FXML private TacticPitchCanvas tacticCanvas;

    private SportManager sm;
    private Match        match;
    private Team         managedTeam;

    private static final int MAX_SUBS = 3;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sm          = SportManager.getInstance();
        match       = sm.getCurrentMatch();
        managedTeam = sm.getManagedTeam();

        if (match == null || managedTeam == null) return;

        homeTeamLabel.setText(match.getHomeTeam().getName());
        awayTeamLabel.setText(match.getAwayTeam().getName());
        scoreLabel.setText("0 – 0");
        matchInfoLabel.setText("Matchday " + sm.getCurrentWeek()
                + "  ·  " + sm.getLeague().getName());

        continueButton.setVisible(false);

        setupSubstitutionPanel();
        setupTacticPanel();
    }

    // ── Setup helpers ─────────────────────────────────────────────────────────

    private void setupSubstitutionPanel() {
        Team managed = managedTeam;
        boolean isHome = match.getHomeTeam() == managed;

        // Only show substitution panel when it's a break and match not finished
        subPanel.setVisible(false);

        removePlayerCombo.getItems().setAll(managed.getStartingLineup());
        addPlayerCombo.getItems().setAll(managed.getSubstitutes());
        updateSubCountLabel();
    }

    private void setupTacticPanel() {
        Sport sport = sm.getSport();
        if (sport != null) {
            tacticCombo.getItems().setAll(sport.getTactics());
            String current = managedTeam.getCurrentTactic();
            tacticCombo.setValue(current);
            if (tacticCanvas != null) tacticCanvas.drawFormation(current);
        }
        // Live preview: redraw canvas whenever selection changes
        tacticCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && tacticCanvas != null) tacticCanvas.drawFormation(newVal);
        });
    }

    private void updateSubCountLabel() {
        int used = sm.getSubstitutionsUsed();
        subCountLabel.setText("Substitutions used: " + used + " / " + MAX_SUBS);
    }

    // ── Simulation ────────────────────────────────────────────────────────────

    @FXML
    private void onSimulateNext() {
        if (match == null || match.isFinished()) return;

        // Hide sub panel during simulation
        subPanel.setVisible(false);

        match.simulateSegment();
        scoreLabel.setText(match.getScoreDisplay());

        // Render the latest segment
        if (!match.getSegments().isEmpty()) {
            MatchSegment seg = match.getSegments().get(match.getSegments().size() - 1);
            renderSegment(seg);
        }

        if (match.isAtBreak()) {
            subPanel.setVisible(true);
            simulateButton.setText("▶  Play " + match.getSegmentLabel(match.getCurrentSegment()));
            addBreakDivider();
        }

        if (match.isFinished()) {
            simulateButton.setVisible(false);
            subPanel.setVisible(false);
            continueButton.setVisible(true);
            renderFullTimeBanner();
        }
    }

    private void renderSegment(MatchSegment seg) {
        Label segLabel = new Label(seg.getLabel().toUpperCase());
        segLabel.getStyleClass().add("segment-header");
        eventsBox.getChildren().add(segLabel);

        for (String ev : seg.getEvents()) {
            if (ev.startsWith("──")) continue;  // skip separator lines we already show
            Label evLabel = new Label(ev);
            evLabel.setWrapText(true);
            evLabel.getStyleClass().add(
                    ev.startsWith("⚽") ? "event-goal"
                    : ev.startsWith("🚑") ? "event-injury"
                    : ev.startsWith("🟨") ? "event-card"
                    : "event-normal");
            eventsBox.getChildren().add(evLabel);
        }

        // Segment score
        Label partial = new Label("End of " + seg.getLabel() + ":  "
                + match.getScoreDisplay());
        partial.getStyleClass().add("segment-score");
        eventsBox.getChildren().add(partial);
    }

    private void addBreakDivider() {
        Separator sep = new Separator();
        Label breakLbl = new Label("── HALF TIME ── Change tactics or make substitutions below ──");
        breakLbl.getStyleClass().add("break-label");
        eventsBox.getChildren().addAll(sep, breakLbl);
    }

    private void renderFullTimeBanner() {
        Label ft = new Label("━━  FULL TIME  ━━");
        ft.getStyleClass().add("fulltime-banner");

        MatchResult res = match.getResult();
        String outcome  = res != null ? res.getScore() : match.getScoreDisplay();
        Label result    = new Label(outcome);
        result.getStyleClass().add("fulltime-result");

        eventsBox.getChildren().addAll(ft, result);

        if (res != null && !res.getInjuries().isEmpty()) {
            Label injHeader = new Label("Match Injuries:");
            injHeader.getStyleClass().add("injury-header");
            eventsBox.getChildren().add(injHeader);
            for (InjuryRecord ir : res.getInjuries()) {
                Label inj = new Label("🚑  " + ir.toString());
                inj.getStyleClass().add("injury-label");
                eventsBox.getChildren().add(inj);
            }
        }
    }

    // ── In-match substitution ─────────────────────────────────────────────────

    @FXML
    private void onMakeSubstitution() {
        Player out = removePlayerCombo.getValue();
        Player in  = addPlayerCombo.getValue();
        if (out == null || in == null) return;
        if (sm.getSubstitutionsUsed() >= MAX_SUBS) {
            subCountLabel.setText("Maximum " + MAX_SUBS + " substitutions used.");
            return;
        }

        managedTeam.getStartingLineup().remove(out);
        managedTeam.getStartingLineup().add(in);
        managedTeam.getSubstitutes().remove(in);
        managedTeam.getSubstitutes().add(out);
        sm.incrementSubstitutions();

        // Refresh combos
        removePlayerCombo.getItems().setAll(managedTeam.getStartingLineup());
        addPlayerCombo.getItems().setAll(managedTeam.getSubstitutes());
        updateSubCountLabel();

        Label subEvent = new Label("🔄  " + in.getName() + " on  for  " + out.getName()
                + " (" + managedTeam.getName() + ")");
        subEvent.getStyleClass().add("event-sub");
        eventsBox.getChildren().add(subEvent);
    }

    @FXML
    private void onApplyTactic() {
        String chosen = tacticCombo.getValue();
        if (chosen != null && managedTeam != null) {
            managedTeam.setCurrentTactic(chosen);
            Label ev = new Label("🔧  Tactic changed to " + chosen);
            ev.getStyleClass().add("event-tactic");
            eventsBox.getChildren().add(ev);
        }
    }

    // ── Post-match navigation ─────────────────────────────────────────────────

    /**
     * "Continue" button — finalises the round (auto-simulates all other matches,
     * updates standings, decrements injuries) and returns to Dashboard.
     * Calls SportManager.advanceWeek() as specified in the architecture document.
     */
    @FXML
    private void onContinue() {
        sm.advanceWeek();
    }
}
