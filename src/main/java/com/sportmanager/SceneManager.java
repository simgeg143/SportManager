package com.sportmanager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Singleton navigator that manages scene transitions across the application.
 * Controllers call methods here to navigate; no controller knows about other controllers.
 */
public class SceneManager {

    private static final SceneManager INSTANCE = new SceneManager();
    private Stage primaryStage;

    private static final String FXML_BASE = "/com/sportmanager/fxml/";
    private static final String CSS_PATH  = "/com/sportmanager/css/style.css";

    private SceneManager() {}

    public static SceneManager getInstance() {
        return INSTANCE;
    }

    public void init(Stage stage) {
        this.primaryStage = stage;
    }

    // ── Navigation helpers ────────────────────────────────────────────────────

    public void showMainMenu()       { navigate("main-menu.fxml");       }
    public void showSportSelection() { navigate("sport-selection.fxml"); }
    public void showTeamSelection()  { navigate("team-selection.fxml");  }
    public void showDashboard()      { navigate("dashboard.fxml");       }
    public void showLeagueTable()    { navigate("league-table.fxml");    }
    public void showMatchScreen()    { navigate("match-screen.fxml");    }
    public void showFixture()        { navigate("fixture.fxml");         }
    public void showSquad()          { navigate("squad.fxml");           }

    public Stage getStage() {
        return primaryStage;
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void navigate(String fxmlFile) {
        try {
            URL resource = getClass().getResource(FXML_BASE + fxmlFile);
            if (resource == null) {
                throw new IllegalStateException("FXML not found: " + FXML_BASE + fxmlFile);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
                URL css = getClass().getResource(CSS_PATH);
                if (css != null) scene.getStylesheets().add(css.toExternalForm());
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene: " + fxmlFile, e);
        }
    }
}
