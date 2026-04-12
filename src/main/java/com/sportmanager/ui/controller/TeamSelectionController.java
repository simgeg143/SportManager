package com.sportmanager.ui.controller;

import com.sportmanager.SportManager;
import com.sportmanager.core.League;
import com.sportmanager.core.Team;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Lets the user choose which team they will manage. (LM-6)
 * Single-click selects and highlights a card; a second click (or the
 * Confirm button) on the already-selected card navigates immediately.
 * This avoids hiding the confirm button below the fold.
 */
public class TeamSelectionController implements Initializable {

    @FXML private Label    leagueTitleLabel;
    @FXML private Label    teamCountLabel;
    @FXML private GridPane teamGrid;
    @FXML private Label    confirmHintLabel;

    private Team selectedTeam;
    private VBox selectedCard;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SportManager sm     = SportManager.getInstance();
        League       league = sm.getLeague();
        if (league == null) return;

        leagueTitleLabel.setText(league.getName());
        List<Team> teams = league.getTeams();
        teamCountLabel.setText("Choose your team  ·  " + teams.size() + " clubs available");

        // Layout: 4 columns of team cards
        int cols = 4;
        for (int i = 0; i < teams.size(); i++) {
            Team t    = teams.get(i);
            VBox card = buildTeamCard(t);
            teamGrid.add(card, i % cols, i / cols);
        }

        if (confirmHintLabel != null) {
            confirmHintLabel.setText("Click a club to select it — click again to confirm");
        }
    }

    private VBox buildTeamCard(Team team) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("team-card");
        card.setPrefWidth(220);

        Label nameLabel = new Label(team.getName());
        nameLabel.getStyleClass().add("team-card-name");
        nameLabel.setWrapText(true);

        int players = team.getRoster().size();
        Label statsLabel = new Label(players + " players  |  avg skill "
                + String.format("%.0f", team.getAverageSkill()));
        statsLabel.getStyleClass().add("team-card-stats");

        int coaches = team.getCoaches().size();
        Label coachLabel = new Label(coaches + " coach" + (coaches != 1 ? "es" : ""));
        coachLabel.getStyleClass().add("team-card-stats");

        // "Confirm" row — hidden until this card is selected
        Label confirmRow = new Label("▶  Manage this club");
        confirmRow.getStyleClass().add("team-card-confirm");
        confirmRow.setVisible(false);
        confirmRow.setManaged(false);

        card.getChildren().addAll(nameLabel, statsLabel, coachLabel, confirmRow);
        card.setOnMouseClicked(e -> handleCardClick(team, card, confirmRow));
        return card;
    }

    private void handleCardClick(Team team, VBox card, Label confirmRow) {
        if (selectedTeam == team) {
            // Second click on the already-selected card → confirm immediately
            confirmAndNavigate(team);
            return;
        }

        // First click → select and highlight; show confirm hint inside card
        teamGrid.getChildren().forEach(n -> {
            n.getStyleClass().remove("team-card-selected");
            // Hide confirm label on all cards
            if (n instanceof VBox vb) {
                vb.getChildren().stream()
                  .filter(c -> c instanceof Label l && l.getStyleClass().contains("team-card-confirm"))
                  .forEach(c -> { c.setVisible(false); ((Label) c).setManaged(false); });
            }
        });

        selectedTeam = team;
        selectedCard = card;
        card.getStyleClass().add("team-card-selected");

        confirmRow.setVisible(true);
        confirmRow.setManaged(true);

        // Fade in the confirm row
        FadeTransition ft = new FadeTransition(Duration.millis(200), confirmRow);
        ft.setFromValue(0); ft.setToValue(1); ft.play();

        // Pulse the card once to invite the user to click again
        ScaleTransition pulse = new ScaleTransition(Duration.millis(120), card);
        pulse.setToX(1.04); pulse.setToY(1.04); pulse.setAutoReverse(true); pulse.setCycleCount(2);
        pulse.play();

        if (confirmHintLabel != null)
            confirmHintLabel.setText("▶  Click \"" + team.getName() + "\" again to start managing");
    }

    private void confirmAndNavigate(Team team) {
        SportManager.getInstance().selectManagedTeam(team);
    }

    @FXML
    private void onConfirm() {
        if (selectedTeam != null) confirmAndNavigate(selectedTeam);
    }

    @FXML
    private void onBack() {
        SportManager.getInstance().showMainMenu();
    }
}
