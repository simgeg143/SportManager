package com.sportmanager.ui.controller;

import com.sportmanager.SportManager;
import com.sportmanager.core.League;
import com.sportmanager.core.Team;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Lets the user choose which team they will manage. (LM-6)
 * Calls SportManager.selectManagedTeam(team) when a team is confirmed.
 */
public class TeamSelectionController implements Initializable {

    @FXML private Label  leagueTitleLabel;
    @FXML private Label  teamCountLabel;
    @FXML private GridPane teamGrid;

    private Team     selectedTeam;
    private Button   confirmButton;

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
    }

    private VBox buildTeamCard(Team team) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("team-card");
        card.setPrefWidth(220);

        Label nameLabel   = new Label(team.getName());
        nameLabel.getStyleClass().add("team-card-name");
        nameLabel.setWrapText(true);

        int players = team.getRoster().size();
        Label statsLabel  = new Label(players + " players  |  avg skill "
                + String.format("%.0f", team.getAverageSkill()));
        statsLabel.getStyleClass().add("team-card-stats");

        int coaches = team.getCoaches().size();
        Label coachLabel  = new Label(coaches + " coach" + (coaches != 1 ? "es" : ""));
        coachLabel.getStyleClass().add("team-card-stats");

        card.getChildren().addAll(nameLabel, statsLabel, coachLabel);
        card.setOnMouseClicked(e -> selectTeam(team, card));
        return card;
    }

    private void selectTeam(Team team, VBox card) {
        // Deselect all
        teamGrid.getChildren().forEach(n -> n.getStyleClass().remove("team-card-selected"));
        selectedTeam = team;
        card.getStyleClass().add("team-card-selected");
    }

    @FXML
    private void onConfirm() {
        if (selectedTeam != null) {
            SportManager.getInstance().selectManagedTeam(selectedTeam);
        }
    }

    @FXML
    private void onBack() {
        SportManager.getInstance().showMainMenu();
    }
}
