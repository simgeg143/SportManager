package com.sportmanager.ui.controller;

import com.sportmanager.SceneManager;
import com.sportmanager.session.GameSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Controller for the persistent global toolbar injected by SceneManager.
 * Manages visibility of in-game navigation, context labels, and screen breadcrumb.
 */
public class AppToolbarController {

    @FXML private Label  screenLabel;
    @FXML private VBox   contextBox;
    @FXML private Label  teamLabel;
    @FXML private Label  weekLabel;
    @FXML private HBox   navBox;
    @FXML private Button btnDashboard;
    @FXML private Button btnFixtures;
    @FXML private Button btnSquad;
    @FXML private Button btnTable;
    @FXML private Button btnSettings;

    /** FXML file that was active before opening Settings, for goBack(). */
    private String lastFxml = "dashboard.fxml";

    private static final String ACTIVE_STYLE   = "toolbar-nav-btn toolbar-nav-btn-active";
    private static final String INACTIVE_STYLE = "toolbar-nav-btn";

    // ── Public API called by SceneManager ─────────────────────────────────────

    /**
     * Updates the toolbar for the given screen. Shows/hides game-context widgets
     * and highlights the currently active nav button.
     *
     * @param fxmlFile the filename of the FXML that was just loaded (e.g. "dashboard.fxml")
     */
    public void updateForScreen(String fxmlFile) {
        if (!fxmlFile.equals("settings.fxml")) lastFxml = fxmlFile;
        String screenName = toScreenName(fxmlFile);
        screenLabel.setText(screenName);

        boolean inGame = GameSession.getInstance().isActive();
        boolean isMenu = fxmlFile.equals("main-menu.fxml")
                || fxmlFile.equals("sport-selection.fxml")
                || fxmlFile.equals("team-selection.fxml");

        // Game context (team name + week)
        contextBox.setVisible(inGame && !isMenu);
        contextBox.setManaged(inGame && !isMenu);
        if (inGame) {
            GameSession gs = GameSession.getInstance();
            teamLabel.setText(gs.getManagedTeam().getName());
            weekLabel.setText("WEEK " + gs.getCurrentWeek()
                    + "  ·  " + gs.getCurrentSeasonYear());
        }

        // Navigation buttons
        navBox.setVisible(inGame && !isMenu);
        navBox.setManaged(inGame && !isMenu);

        // Highlight active nav button
        if (inGame && !isMenu) {
            resetNavButtons();
            switch (fxmlFile) {
                case "dashboard.fxml"    -> btnDashboard.getStyleClass().setAll("toolbar-nav-btn", "toolbar-nav-btn-active");
                case "fixture.fxml"      -> btnFixtures.getStyleClass().setAll("toolbar-nav-btn", "toolbar-nav-btn-active");
                case "squad.fxml"        -> btnSquad.getStyleClass().setAll("toolbar-nav-btn", "toolbar-nav-btn-active");
                case "league-table.fxml" -> btnTable.getStyleClass().setAll("toolbar-nav-btn", "toolbar-nav-btn-active");
            }
        }
    }

    // ── FXML handlers ─────────────────────────────────────────────────────────

    @FXML private void onDashboard() { SceneManager.getInstance().showDashboard();    }
    @FXML private void onFixtures()  { SceneManager.getInstance().showFixture();      }
    @FXML private void onSquad()     { SceneManager.getInstance().showSquad();        }
    @FXML private void onTable()     { SceneManager.getInstance().showLeagueTable();  }
    @FXML private void onMainMenu()  { SceneManager.getInstance().showMainMenu();     }
    @FXML private void onSettings()  { SceneManager.getInstance().showSettings(lastFxml); }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void resetNavButtons() {
        for (Button btn : new Button[]{btnDashboard, btnFixtures, btnSquad, btnTable}) {
            btn.getStyleClass().setAll("toolbar-nav-btn");
        }
    }

    private String toScreenName(String fxmlFile) {
        return switch (fxmlFile) {
            case "main-menu.fxml"      -> "MAIN MENU";
            case "sport-selection.fxml"-> "SELECT SPORT";
            case "team-selection.fxml" -> "SELECT TEAM";
            case "dashboard.fxml"      -> "DASHBOARD";
            case "fixture.fxml"        -> "FIXTURES";
            case "squad.fxml"          -> "SQUAD";
            case "league-table.fxml"   -> "LEAGUE TABLE";
            case "match-screen.fxml"   -> "MATCH";
            case "settings.fxml"       -> "SETTINGS";
            default -> fxmlFile.replace(".fxml", "").toUpperCase().replace("-", " ");
        };
    }
}
