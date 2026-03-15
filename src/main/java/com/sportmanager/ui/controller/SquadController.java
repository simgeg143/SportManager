package com.sportmanager.ui.controller;

import com.sportmanager.SportManager;
import com.sportmanager.core.Coach;
import com.sportmanager.core.Player;
import com.sportmanager.core.Team;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Displays the full squad roster with player attributes and availability. (TM-1)
 * Also shows the coaching staff. (LM-4)
 * Called by SportManager.showSquad().
 *
 * Separate screen from the Dashboard per the architecture document's
 * SquadController specification.
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

    private SportManager sm;

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

        // Colour injured rows
        playerTable.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Player p, boolean empty) {
                super.updateItem(p, empty);
                getStyleClass().removeAll("row-injured");
                if (!empty && p != null && p.isInjured()) getStyleClass().add("row-injured");
            }
        });

        // Status cell colour
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                getStyleClass().removeAll("status-fit","status-injured");
                if (!empty && item != null)
                    getStyleClass().add(item.startsWith("INJ") ? "status-injured" : "status-fit");
            }
        });

        // Sort by position then skill descending by default
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

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void onBack() { sm.showDashboard(); }
}
