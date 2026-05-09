package com.sportmanager.ui.controller;

import com.sportmanager.SportManager;
import com.sportmanager.core.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Main game screen — shows weekly progression and lets the user act. (DashboardController)
 *
 * Responsibilities (per architecture document §4.3):
 *  – Shows current week, team stats, league position
 *  – Shows next opponent and provides "Play Match" button → SportManager.startMatch()
 *  – Links to FixtureController (full schedule) and SquadController (roster)
 *  – Shows top 5 of the league table for quick reference
 */
public class DashboardController implements Initializable {

    // ── Header ────────────────────────────────────────────────────────────────
    @FXML private Label teamNameLabel;
    @FXML private Label weekLabel;
    @FXML private Label positionLabel;
    @FXML private Label recordLabel;

    // ── Next match panel ──────────────────────────────────────────────────────
    @FXML private Label  nextMatchLabel;
    @FXML private Label  nextMatchVenueLabel;
    @FXML private Button playMatchButton;

    // ── Quick standings ───────────────────────────────────────────────────────
    @FXML private VBox standingsPreviewBox;

    // ── Squad preview ─────────────────────────────────────────────────────────
    @FXML private Label lineupStatusLabel;
    @FXML private VBox  injuryListBox;
    @FXML private StackPane saveOverlay;
    @FXML private TextField saveNameField;
    @FXML private Label saveOverlayStatusLabel;

    private SportManager sm;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sm = SportManager.getInstance();
        refreshAll();
    }

    private void refreshAll() {
        Team team = sm.getManagedTeam();
        if (team == null) return;

        // Header
        teamNameLabel.setText(team.getName());
        weekLabel.setText("Matchday  " + sm.getCurrentWeek() + "  /  " + sm.getTotalWeeks());
        int pos = sm.getManagedTeamPosition();
        positionLabel.setText(pos > 0 ? ordinal(pos) + " place" : "—");
        recordLabel.setText(team.getWins() + "W  " + team.getDraws() + "D  "
                + team.getLosses() + "L    Pts " + team.getPoints()
                + "    GD " + (team.getGoalDifference() >= 0 ? "+" : "") + team.getGoalDifference());

        // Season over banner
        if (sm.isSeasonFinished()) {
            nextMatchLabel.setText("Season Complete!");
            nextMatchVenueLabel.setText(
                    sm.getLeague().getChampion() != null
                    ? "🏆  Champion: " + sm.getLeague().getChampion().getName()
                    : "Final standings finalized.");
            playMatchButton.setVisible(false);
            lineupStatusLabel.setText("");
            buildStandingsPreview();
            buildInjuryList(team);
            return;
        }

        // Next match
        Match next = sm.getCurrentMatch();
        if (next == null) {
            // Peek at the current round to find managed team's fixture
            List<Match> roundMatches = sm.getLeague().getCurrentRoundMatches();
            for (Match m : roundMatches) {
                if (m.getHomeTeam() == team || m.getAwayTeam() == team) {
                    next = m;
                    break;
                }
            }
        }
        if (next != null) {
            boolean isHome = next.getHomeTeam() == team;
            Team    opp    = isHome ? next.getAwayTeam() : next.getHomeTeam();
            nextMatchLabel.setText(isHome
                    ? team.getName() + "  vs  " + opp.getName()
                    : opp.getName() + "  vs  " + team.getName());
            nextMatchVenueLabel.setText(isHome ? "Home  ·  Matchday " + sm.getCurrentWeek()
                                                : "Away  ·  Matchday " + sm.getCurrentWeek());
        } else {
            nextMatchLabel.setText("No fixture this round");
            nextMatchVenueLabel.setText("");
            playMatchButton.setVisible(false);
        }

        // Lineup validity
        if (team.hasValidLineup()) {
            lineupStatusLabel.setText("✔  Lineup ready (" + team.getStartingLineup().size() + " starters)");
            lineupStatusLabel.getStyleClass().removeAll("label-warn");
        } else {
            lineupStatusLabel.setText("⚠  Lineup invalid — visit Squad to fix");
            lineupStatusLabel.getStyleClass().add("label-warn");
        }

        buildStandingsPreview();
        buildInjuryList(team);
    }

    // ── Standings preview (top 6) ─────────────────────────────────────────────

    private void buildStandingsPreview() {
        standingsPreviewBox.getChildren().clear();
        if (sm.getLeague() == null) return;

        Team managed = sm.getManagedTeam();
        List<StandingEntry> table = sm.showLeagueTableData();
        int limit = Math.min(6, table.size());

        for (int i = 0; i < limit; i++) {
            StandingEntry e = table.get(i);
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            Label pos   = new Label(e.getPosition() + ".");
            pos.setPrefWidth(25);
            pos.getStyleClass().add("standing-pos");

            Label name  = new Label(e.getTeamName());
            name.setPrefWidth(160);
            name.getStyleClass().add(e.getTeam() == managed ? "standing-name-managed" : "standing-name");

            Label pts   = new Label(e.getPoints() + " pts");
            pts.setPrefWidth(55);
            pts.getStyleClass().add("standing-pts");

            Label gd    = new Label(e.getGoalDifferenceDisplay());
            gd.getStyleClass().add("standing-gd");

            row.getChildren().addAll(pos, name, pts, gd);
            standingsPreviewBox.getChildren().add(row);
        }

        if (managed != null) {
            int managedPos = sm.getManagedTeamPosition();
            if (managedPos > limit) {
                standingsPreviewBox.getChildren().add(new Label("  ⋮"));
                StandingEntry me = table.get(managedPos - 1);
                HBox row = buildStandingRow(me, managed);
                standingsPreviewBox.getChildren().add(row);
            }
        }
    }

    private HBox buildStandingRow(StandingEntry e, Team managed) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label pos  = new Label(e.getPosition() + ".");  pos.setPrefWidth(25);
        Label name = new Label(e.getTeamName()); name.setPrefWidth(160);
        Label pts  = new Label(e.getPoints() + " pts"); pts.setPrefWidth(55);
        Label gd   = new Label(e.getGoalDifferenceDisplay());
        pos.getStyleClass().add("standing-pos");
        name.getStyleClass().add(e.getTeam() == managed ? "standing-name-managed" : "standing-name");
        pts.getStyleClass().add("standing-pts");
        gd.getStyleClass().add("standing-gd");
        row.getChildren().addAll(pos, name, pts, gd);
        return row;
    }

    // ── Injury list ───────────────────────────────────────────────────────────

    private void buildInjuryList(Team team) {
        injuryListBox.getChildren().clear();
        long total = team.getRoster().stream().filter(Player::isInjured).count();
        if (total == 0) {
            injuryListBox.getChildren().add(new Label("No current injuries ✔"));
            return;
        }
        team.getRoster().stream().filter(Player::isInjured).forEach(p -> {
            Label lbl = new Label("🚑  " + p.getName()
                    + " — " + p.getPosition()
                    + "  (" + p.getInjuryMatchesRemaining() + " match(es) remaining)");
            lbl.getStyleClass().add("injury-label");
            injuryListBox.getChildren().add(lbl);
        });
    }

    // ── Button handlers ───────────────────────────────────────────────────────

    @FXML private void onPlayMatch()    { sm.startMatch(); }
    @FXML private void onViewFixtures() { sm.showFixture(); }
    @FXML private void onViewSquad()    { sm.showSquad(); }
    @FXML private void onViewTable()    { sm.showLeagueTable(); }
    @FXML private void onSaveGame() {
        Team team = sm.getManagedTeam();
        String def = team != null
                ? team.getName() + " — W" + sm.getCurrentWeek()
                : "Save " + System.currentTimeMillis();
        saveNameField.setText(def);
        saveOverlayStatusLabel.setText("");
        saveOverlay.setVisible(true);
        saveOverlay.setManaged(true);
    }

    @FXML
    private void onConfirmSaveGame() {
        String label = saveNameField.getText() == null ? "" : saveNameField.getText().trim();
        if (label.isBlank()) {
            saveOverlayStatusLabel.setText("Please enter a save name.");
            return;
        }
        try {
            sm.saveGame(label);
            saveOverlayStatusLabel.setText("Saved as \"" + label + "\".");
        } catch (Exception ex) {
            saveOverlayStatusLabel.setText("Save failed: " + ex.getMessage());
        }
    }

    @FXML
    private void onCloseSaveOverlay() {
        saveOverlay.setVisible(false);
        saveOverlay.setManaged(false);
    }
    @FXML private void onMainMenu() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Return to the main menu? (Current progress will not be saved)",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Main Menu");
        confirm.setHeaderText(null);
        confirm.showAndWait().filter(b -> b == ButtonType.YES)
               .ifPresent(b -> sm.showMainMenu());
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private static String ordinal(int n) {
        String suf = switch (n % 100) {
            case 11, 12, 13 -> "th";
            default -> switch (n % 10) {
                case 1 -> "st"; case 2 -> "nd"; case 3 -> "rd"; default -> "th";
            };
        };
        return n + suf;
    }
}
