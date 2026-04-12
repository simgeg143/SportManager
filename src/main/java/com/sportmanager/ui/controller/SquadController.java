package com.sportmanager.ui.controller;

import com.sportmanager.SportManager;
import com.sportmanager.core.Coach;
import com.sportmanager.core.Player;
import com.sportmanager.core.Team;
import com.sportmanager.ui.component.TacticPitchCanvas;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Displays the full squad roster with player attributes and availability. (TM-1)
 * Also shows coaching staff and a live tactic formation selector with pitch canvas.
 */
public class SquadController implements Initializable {

    // ── Header ────────────────────────────────────────────────────────────────
    @FXML private Label teamNameLabel;
    @FXML private Label availableLabel;

    // ── Player table ─────────────────────────────────────────────────────────
    @FXML private TableView<Player>             playerTable;
    @FXML private TableColumn<Player, String>   nameCol;
    @FXML private TableColumn<Player, String>   posCol;
    @FXML private TableColumn<Player, Number>   skillCol;
    @FXML private TableColumn<Player, String>   statusCol;
    @FXML private TableColumn<Player, String>   attribCol;

    // ── Coach table ──────────────────────────────────────────────────────────
    @FXML private TableView<Coach>              coachTable;
    @FXML private TableColumn<Coach, String>    coachNameCol;
    @FXML private TableColumn<Coach, String>    coachRoleCol;
    @FXML private TableColumn<Coach, String>    coachShapeCol;
    @FXML private TableColumn<Coach, Number>    coachTrainCol;
    @FXML private TableColumn<Coach, Number>    coachMotivCol;

    // ── Tactic panel ─────────────────────────────────────────────────────────
    @FXML private TacticPitchCanvas squadTacticCanvas;
    @FXML private ComboBox<String>  squadTacticCombo;
    @FXML private Label             tacticDescLabel;
    @FXML private Label             tacticStatusLabel;

    private SportManager sm;

    // Short descriptions for each formation
    private static final Map<String, String> TACTIC_DESCRIPTIONS = Map.of(
        "4-3-3",   "Attacking wide play with wingers. High pressing, quick build-up.",
        "4-4-2",   "Balanced and compact. Strong defensive shape, direct counter-attacks.",
        "4-2-3-1", "Double pivot shields defence. Playmaker behind the striker.",
        "3-5-2",   "Wing-backs provide width. Midfield dominance with two strikers.",
        "5-3-2",   "Defensively solid. Wing-backs join attacks from deep."
    );

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sm = SportManager.getInstance();
        Team team = sm.getManagedTeam();
        if (team == null) return;

        teamNameLabel.setText(team.getName() + " — Full Squad");
        long available = team.getRoster().stream().filter(p -> !p.isInjured()).count();
        availableLabel.setText(available + " available  /  " + team.getRoster().size() + " total");

        setupPlayerTable(team);
        setupCoachTable(team);
        setupTacticPanel(team);
    }

    // ── Player table ─────────────────────────────────────────────────────────

    private void setupPlayerTable(Team team) {
        nameCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getName()));
        posCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPosition()));
        skillCol.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getSkillLevel()));
        statusCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatusDisplay()));
        attribCol.setCellValueFactory(d -> {
            var attrs = d.getValue().getSpecificAttributes();
            String summary = attrs.entrySet().stream()
                    .map(e -> e.getKey() + ":" + e.getValue())
                    .reduce("", (a, b) -> a.isEmpty() ? b : a + "  " + b);
            return new SimpleStringProperty(summary);
        });

        playerTable.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Player p, boolean empty) {
                super.updateItem(p, empty);
                getStyleClass().removeAll("row-injured");
                if (!empty && p != null && p.isInjured()) getStyleClass().add("row-injured");
            }
        });

        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                getStyleClass().removeAll("status-fit", "status-injured");
                if (!empty && item != null)
                    getStyleClass().add(item.startsWith("INJ") ? "status-injured" : "status-fit");
            }
        });

        playerTable.setItems(FXCollections.observableArrayList(team.getRoster()));
        skillCol.setSortType(TableColumn.SortType.DESCENDING);
        playerTable.getSortOrder().add(posCol);
    }

    // ── Coach table ──────────────────────────────────────────────────────────

    private void setupCoachTable(Team team) {
        coachNameCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getName()));
        coachRoleCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getRole()));
        coachShapeCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPreferredShape()));
        coachTrainCol.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getTrainingSkill()));
        coachMotivCol.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getMotivationSkill()));

        coachTable.setItems(FXCollections.observableArrayList(team.getCoaches()));
        coachTable.setPlaceholder(new Label("No coaching staff assigned."));
    }

    // ── Tactic panel ─────────────────────────────────────────────────────────

    private void setupTacticPanel(Team team) {
        if (sm.getSport() != null) {
            squadTacticCombo.getItems().setAll(sm.getSport().getTactics());
        }

        String current = team.getCurrentTactic();
        squadTacticCombo.setValue(current);
        updateTacticPreview(current);

        // Live update: canvas and description react instantly on selection change
        squadTacticCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) updateTacticPreview(newVal);
        });

        tacticStatusLabel.setText("");
    }

    private void updateTacticPreview(String tactic) {
        if (squadTacticCanvas != null) squadTacticCanvas.drawFormation(tactic);
        tacticDescLabel.setText(TACTIC_DESCRIPTIONS.getOrDefault(tactic, ""));
    }

    @FXML
    private void onApplySquadTactic() {
        String chosen = squadTacticCombo.getValue();
        Team team = sm.getManagedTeam();
        if (chosen != null && team != null) {
            team.setCurrentTactic(chosen);
            tacticStatusLabel.setText("✓  Formation set to " + chosen);
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void onBack() { sm.showDashboard(); }
}
