package com.sportmanager.ui.controller;

import com.sportmanager.SportManager;
import com.sportmanager.core.StandingEntry;
import com.sportmanager.core.Team;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Displays the current league standings. (LM-7, StandingsController in architecture doc)
 * Calls SportManager.showLeagueTableData() — no direct League/Team access.
 */
public class LeagueTableController implements Initializable {

    @FXML private Label leagueNameLabel;
    @FXML private Label weekLabel;

    @FXML private TableView<StandingEntry>             standingsTable;
    @FXML private TableColumn<StandingEntry, Number>  posCol;
    @FXML private TableColumn<StandingEntry, String>  teamCol;
    @FXML private TableColumn<StandingEntry, Number>  pldCol;
    @FXML private TableColumn<StandingEntry, Number>  wonCol;
    @FXML private TableColumn<StandingEntry, Number>  drawnCol;
    @FXML private TableColumn<StandingEntry, Number>  lostCol;
    @FXML private TableColumn<StandingEntry, Number>  gfCol;
    @FXML private TableColumn<StandingEntry, Number>  gaCol;
    @FXML private TableColumn<StandingEntry, String>  gdCol;
    @FXML private TableColumn<StandingEntry, Number>  ptsCol;

    private SportManager sm;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sm = SportManager.getInstance();

        if (sm.getLeague() != null) leagueNameLabel.setText(sm.getLeague().getName());
        weekLabel.setText("Matchday " + sm.getCurrentWeek() + "  /  " + sm.getTotalWeeks());

        setupColumns();
        populateTable();
    }

    private void setupColumns() {
        posCol.setCellValueFactory(d    -> new SimpleIntegerProperty(d.getValue().getPosition()));
        teamCol.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue().getTeamName()));
        pldCol.setCellValueFactory(d    -> new SimpleIntegerProperty(d.getValue().getPlayed()));
        wonCol.setCellValueFactory(d    -> new SimpleIntegerProperty(d.getValue().getWon()));
        drawnCol.setCellValueFactory(d  -> new SimpleIntegerProperty(d.getValue().getDrawn()));
        lostCol.setCellValueFactory(d   -> new SimpleIntegerProperty(d.getValue().getLost()));
        gfCol.setCellValueFactory(d     -> new SimpleIntegerProperty(d.getValue().getGoalsFor()));
        gaCol.setCellValueFactory(d     -> new SimpleIntegerProperty(d.getValue().getGoalsAgainst()));
        gdCol.setCellValueFactory(d     -> new SimpleStringProperty(d.getValue().getGoalDifferenceDisplay()));
        ptsCol.setCellValueFactory(d    -> new SimpleIntegerProperty(d.getValue().getPoints()));

        posCol.setStyle("-fx-alignment: CENTER;");
        pldCol.setStyle("-fx-alignment: CENTER;");
        wonCol.setStyle("-fx-alignment: CENTER;");
        drawnCol.setStyle("-fx-alignment: CENTER;");
        lostCol.setStyle("-fx-alignment: CENTER;");
        gfCol.setStyle("-fx-alignment: CENTER;");
        gaCol.setStyle("-fx-alignment: CENTER;");
        gdCol.setStyle("-fx-alignment: CENTER;");
        ptsCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        // Highlight managed team's row
        Team managed = sm.getManagedTeam();
        standingsTable.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(StandingEntry e, boolean empty) {
                super.updateItem(e, empty);
                getStyleClass().removeAll("row-managed");
                if (!empty && e != null && e.getTeam() == managed)
                    getStyleClass().add("row-managed");
            }
        });
    }

    private void populateTable() {
        List<StandingEntry> table = sm.showLeagueTableData();
        standingsTable.setItems(FXCollections.observableArrayList(table));
    }

    @FXML private void onBack() { sm.showDashboard(); }
}
