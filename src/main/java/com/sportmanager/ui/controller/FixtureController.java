package com.sportmanager.ui.controller;

import com.sportmanager.SportManager;
import com.sportmanager.core.Match;
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
 * Displays the full season fixture schedule. (TM-2)
 * Called by SportManager.showFixture().
 *
 * Shows all matchdays in a TableView with columns:
 * Week | Home Team | Score | Away Team | Status
 * The current week is scrolled into view; played matches show the result.
 */
public class FixtureController implements Initializable {

    @FXML private Label leagueNameLabel;
    @FXML private Label seasonLabel;
    @FXML private TableView<FixtureRow>         fixtureTable;
    @FXML private TableColumn<FixtureRow, Number> weekCol;
    @FXML private TableColumn<FixtureRow, String> homeCol;
    @FXML private TableColumn<FixtureRow, String> scoreCol;
    @FXML private TableColumn<FixtureRow, String> awayCol;
    @FXML private TableColumn<FixtureRow, String> statusCol;

    private SportManager sm;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sm = SportManager.getInstance();

        if (sm.getLeague() != null) {
            leagueNameLabel.setText(sm.getLeague().getName());
        }
        seasonLabel.setText("Season Schedule  ·  "
                + sm.getTotalWeeks() + " matchdays");

        setupColumns();
        populateFixtures();
        scrollToCurrentWeek();
    }

    // ── Column wiring ─────────────────────────────────────────────────────────

    private void setupColumns() {
        weekCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().week));
        homeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().home));
        scoreCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().score));
        awayCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().away));
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status));

        // Centre align numeric columns
        weekCol.setStyle("-fx-alignment: CENTER;");
        scoreCol.setStyle("-fx-alignment: CENTER;");

        // Colour-code by status
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                getStyleClass().removeAll("status-played","status-current","status-upcoming");
                if (!empty && item != null) {
                    if      (item.equals("PLAYED"))   getStyleClass().add("status-played");
                    else if (item.equals("▶ NOW"))    getStyleClass().add("status-current");
                    else                              getStyleClass().add("status-upcoming");
                }
            }
        });

        // Highlight managed team matches
        Team managed = sm.getManagedTeam();
        fixtureTable.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(FixtureRow item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-managed", "row-current-week");
                if (!empty && item != null) {
                    if (item.isManaged) getStyleClass().add("row-managed");
                    if (item.isCurrent) getStyleClass().add("row-current-week");
                }
            }
        });
    }

    // ── Data population ───────────────────────────────────────────────────────

    private void populateFixtures() {
        List<List<Match>> allRounds = sm.showFixtureData();
        int currentWeek = sm.getCurrentWeek();
        Team managed    = sm.getManagedTeam();

        var rows = FXCollections.<FixtureRow>observableArrayList();

        for (int ri = 0; ri < allRounds.size(); ri++) {
            int week = ri + 1;
            for (Match m : allRounds.get(ri)) {
                boolean isManaged = managed != null
                        && (m.getHomeTeam() == managed || m.getAwayTeam() == managed);
                boolean isCurrent = (week == currentWeek);
                boolean played    = m.isFinished();

                String score  = played ? m.getScoreDisplay() : "vs";
                String status = played  ? "PLAYED"
                              : isCurrent ? "▶ NOW"
                              : "Upcoming";

                rows.add(new FixtureRow(week,
                        m.getHomeTeam().getName(),
                        score,
                        m.getAwayTeam().getName(),
                        status, isManaged, isCurrent));
            }
        }
        fixtureTable.setItems(rows);
    }

    private void scrollToCurrentWeek() {
        int currentWeek = sm.getCurrentWeek();
        for (int i = 0; i < fixtureTable.getItems().size(); i++) {
            if (fixtureTable.getItems().get(i).week == currentWeek) {
                fixtureTable.scrollTo(Math.max(0, i - 2));
                break;
            }
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void onBack() { sm.showDashboard(); }

    // ── Inner row model ───────────────────────────────────────────────────────

    static class FixtureRow {
        final int     week;
        final String  home, score, away, status;
        final boolean isManaged, isCurrent;

        FixtureRow(int week, String home, String score, String away,
                   String status, boolean isManaged, boolean isCurrent) {
            this.week      = week;
            this.home      = home;
            this.score     = score;
            this.away      = away;
            this.status    = status;
            this.isManaged = isManaged;
            this.isCurrent = isCurrent;
        }
    }
}
