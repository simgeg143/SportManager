package com.sportmanager;

import com.sportmanager.ui.controller.AppToolbarController;
import javafx.animation.*;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

/**
 * Singleton navigator.  The scene graph has a stable outer shell:
 *
 *   Scene
 *     └── shellPane  (BorderPane, fixed)
 *           ├── top:    global toolbar  (AppToolbarController, hidden on main-menu)
 *           └── center: contentPane    (StackPane — FXML root swapped here)
 *
 * On first launch a full-screen animated splash is shown before the main menu.
 * On subsequent navigations a brief loading overlay fades in and out.
 */
public class SceneManager {

    private static final SceneManager INSTANCE = new SceneManager();

    private static final String FXML_BASE    = "/com/sportmanager/fxml/";
    private static final String TOOLBAR_FXML = FXML_BASE + "app-toolbar.fxml";
    private static final String CSS_PATH     = "/com/sportmanager/css/style.css";

    private static final double SCENE_W = 1280;
    private static final double SCENE_H = 768;

    // Transition durations (ms)
    private static final int FADE_OUT_MS = 120;
    private static final int FADE_IN_MS  = 150;
    private static final int SPLASH_MS   = 5000;  // 5-second startup splash

    private Stage      primaryStage;
    private BorderPane shellPane;
    private StackPane  contentPane;
    private Node       toolbarNode;
    private AppToolbarController toolbarController;

    private boolean splashShown = false;

    private SceneManager() {}

    public static SceneManager getInstance() { return INSTANCE; }

    // ── Init ─────────────────────────────────────────────────────────────────

    public void init(Stage stage) {
        this.primaryStage = stage;
        contentPane = new StackPane();
        shellPane   = new BorderPane(contentPane);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(TOOLBAR_FXML));
            toolbarNode       = loader.load();
            toolbarController = loader.getController();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load toolbar", e);
        }
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

    public Stage getStage() { return primaryStage; }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void navigate(String fxmlFile) {
        if (!splashShown) {
            splashShown = true;
            attachToStage();
            primaryStage.show();
            showSplash(() -> loadAndShow(fxmlFile, false));
            return;
        }

        loadAndShow(fxmlFile, true);
    }

    /** Loads FXML and transitions to it with a simple fade swap. */
    private void loadAndShow(String fxmlFile, boolean animate) {
        Parent newContent = loadFxml(fxmlFile);
        updateToolbar(fxmlFile);

        if (!animate || contentPane.getChildren().isEmpty()) {
            contentPane.getChildren().setAll(newContent);
            contentPane.setOpacity(1.0);
            return;
        }

        // Simple fade out → swap → fade in, no loading overlay
        FadeTransition fadeOut = new FadeTransition(Duration.millis(FADE_OUT_MS), contentPane);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            contentPane.getChildren().setAll(newContent);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_IN_MS), contentPane);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    // ── Animated Splash Screen ────────────────────────────────────────────────

    /**
     * Shows a full-screen animated splash for SPLASH_MS ms, then calls onComplete.
     * Elements: pulsing ball icon · app title · progress bar · version tagline.
     */
    private void showSplash(Runnable onComplete) {
        // ── Layout ──
        StackPane splash = new StackPane();
        splash.setStyle("-fx-background-color:#0c1018;");

        // Ball icon with glow
        Label icon = new Label("⚽");
        icon.setStyle("-fx-font-size:72px;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,223,162,0.65),32,0.5,0,0);");

        // Title
        Label title = new Label("SPORT MANAGER");
        title.setStyle("-fx-font-family:'Segoe UI',-apple-system,sans-serif;"
                + "-fx-font-size:36px;-fx-font-weight:bold;"
                + "-fx-text-fill:#eef2ff;-fx-letter-spacing:10px;");

        // Tagline
        Label tagline = new Label("FOOTBALL EDITION  ·  2025");
        tagline.setStyle("-fx-font-size:12px;-fx-text-fill:#2a3a50;-fx-letter-spacing:4px;");

        // Spacer
        Region gap = new Region();
        gap.setMinHeight(28);

        // Progress bar
        ProgressBar bar = new ProgressBar(0.0);
        bar.setPrefWidth(300);
        bar.setPrefHeight(4);
        bar.setStyle(
            "-fx-accent:#00dfa2;"
            + "-fx-background-color:#1b2333;"
            + "-fx-background-radius:2px;");

        // Loading hint
        Label hint = new Label("INITIALISING...");
        hint.setStyle("-fx-font-size:10px;-fx-text-fill:#2a3a50;-fx-letter-spacing:3px;");

        VBox content = new VBox(16, icon, title, tagline, gap, bar, hint);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(440);

        splash.getChildren().add(content);
        StackPane.setAlignment(content, Pos.CENTER);

        // ── Animations ──

        // Pulse the ball
        ScaleTransition pulse = new ScaleTransition(Duration.millis(900), icon);
        pulse.setFromX(1.0); pulse.setFromY(1.0);
        pulse.setToX(1.18);  pulse.setToY(1.18);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);

        // Progress bar fills smoothly over SPLASH_MS - 600ms (leaving room for fade-out)
        int steps = 50;
        Timeline progress = new Timeline();
        double stepDuration = (SPLASH_MS - 600.0) / steps;
        for (int i = 1; i <= steps; i++) {
            final double val = (double) i / steps;
            final String[] hints = { "LOADING ASSETS...", "GENERATING LEAGUE...", "READY!" };
            final String hintText = hints[Math.min(i / (steps / 3), 2)];
            progress.getKeyFrames().add(
                new KeyFrame(Duration.millis(i * stepDuration),
                    e -> { bar.setProgress(val); hint.setText(hintText); }
                )
            );
        }

        // After progress: fade out splash → run onComplete
        progress.setOnFinished(e -> {
            pulse.stop();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), splash);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(ev -> {
                contentPane.getChildren().remove(splash);
                onComplete.run();
            });
            fadeOut.play();
        });

        // Fade in the splash, then start animations
        splash.setOpacity(0.0);
        contentPane.getChildren().setAll(splash);
        shellPane.setTop(null);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), splash);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(ev -> {
            pulse.play();
            progress.play();
        });
        fadeIn.play();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void attachToStage() {
        if (primaryStage.getScene() != null) return;
        Scene scene = new Scene(shellPane, SCENE_W, SCENE_H);
        URL css = getClass().getResource(CSS_PATH);
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        primaryStage.setScene(scene);
    }

    private void updateToolbar(String fxmlFile) {
        boolean hideToolbar = fxmlFile.equals("main-menu.fxml");
        shellPane.setTop(hideToolbar ? null : toolbarNode);
        if (!hideToolbar) toolbarController.updateForScreen(fxmlFile);
    }

    private Parent loadFxml(String fxmlFile) {
        try {
            URL resource = getClass().getResource(FXML_BASE + fxmlFile);
            if (resource == null)
                throw new IllegalStateException("FXML not found: " + FXML_BASE + fxmlFile);
            return new FXMLLoader(resource).load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene: " + fxmlFile, e);
        }
    }
}
