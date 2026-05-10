package com.sportmanager.ui.controller;

import com.sportmanager.SportManager;
import com.sportmanager.core.Coach;
import com.sportmanager.core.Player;
import com.sportmanager.core.Team;
import com.sportmanager.ui.ScrollViewportBindings;
import com.sportmanager.ui.component.TacticPitchCanvas;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.util.Duration;

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
    @FXML private Label             squadCardNameLabel;
    @FXML private Label             squadCardMetaLabel;
    @FXML private Label             squadCardOverallLabel;
    @FXML private Canvas            squadRadarCanvas;
    @FXML private VBox              squadCardStatsBox;
    @FXML private Label             squadCardExtraLabel;
    @FXML private SplitPane squadRightSplit;
    @FXML private VBox squadTacticCanvasHost;
    @FXML private VBox squadBottomPane;

    private SportManager sm;
    private Map<String, Double> squadRadarPrev = Map.of();

    private static final Map<String, String> TACTIC_DESCRIPTIONS = mergeDescriptions();

    private static Map<String, String> mergeDescriptions() {
        return Map.ofEntries(
                Map.entry("4-3-3",   "Attacking wide play with wingers. High pressing, quick build-up."),
                Map.entry("4-4-2",   "Balanced and compact. Strong defensive shape, direct counter-attacks."),
                Map.entry("4-2-3-1", "Double pivot shields defence. Playmaker behind the striker."),
                Map.entry("3-5-2",   "Wing-backs provide width. Midfield dominance with two strikers."),
                Map.entry("5-3-2",   "Defensively solid. Wing-backs join attacks from deep."),
                Map.entry("5-Out",   "All five players outside the arc — maximum spacing for drives and threes."),
                Map.entry("4-Out 1-In", "One post touch with four shooters around the perimeter (inside–outside)."),
                Map.entry("Horns",   "Two screeners at the elbows; pick-and-pop and dribble hand-off entries."),
                Map.entry("Pick & Roll", "Ball-screen at the top — high PNR with rollers and kick-out shooters."),
                Map.entry("Post Split", "Low-post entry with weak-side cutters and split cuts off the block.")
        );
    }

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
        if (squadRightSplit != null) {
            ScrollViewportBindings.attachRegionLayoutListeners(squadRightSplit, this::resizeSquadSidePanelContents);
        }
        if (squadTacticCanvasHost != null) {
            ScrollViewportBindings.attachRegionLayoutListeners(squadTacticCanvasHost, this::resizeSquadSidePanelContents);
        }
        if (squadBottomPane != null) {
            ScrollViewportBindings.attachRegionLayoutListeners(squadBottomPane, this::resizeSquadSidePanelContents);
        }
        Platform.runLater(() -> {
            if (squadRightSplit == null) return;
            for (javafx.scene.Node n : squadRightSplit.lookupAll(".split-pane-divider")) {
                n.setMouseTransparent(true);
            }
        });
    }

    private void resizeSquadSidePanelContents() {
        if (squadRightSplit == null || squadTacticCanvasHost == null || squadBottomPane == null
                || squadTacticCanvas == null || squadRadarCanvas == null) return;

        double splitInner = ScrollViewportBindings.regionInnerWidth(squadRightSplit, 12);
        if (splitInner <= 40) return;

        double inset = 10;
        double hostW = Math.max(36, squadTacticCanvasHost.getWidth() - inset);
        double hostH = Math.max(48, squadTacticCanvasHost.getHeight() - inset);

        double idealPitchH = hostW * (300.0 / 256.0);
        double pitchH = Math.min(Math.max(idealPitchH, 72), hostH);
        squadTacticCanvas.setWidth(hostW);
        squadTacticCanvas.setHeight(pitchH);

        String tactic = squadTacticCombo != null ? squadTacticCombo.getValue() : null;
        if (tactic != null && !tactic.isBlank()) {
            squadTacticCanvas.drawFormation(tactic);
        }

        double bottomH = squadBottomPane.getHeight();
        double radarW = ScrollViewportBindings.regionInnerWidth(squadBottomPane, 28);
        if (radarW <= 40 || bottomH < 80) return;

        double radarH = Math.max(52, bottomH - 220);
        radarH = Math.min(radarH, radarW * 0.92);

        ScrollViewportBindings.layoutRadarCanvas(squadRadarCanvas, radarW, radarH / radarW);

        if (playerTable != null) {
            Player sel = playerTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                drawRadar(squadRadarCanvas, new LinkedHashMap<>(sel.getSpecificAttributes()));
            }
        }
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
        playerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> renderSquadPlayerCard(newP));
        if (!playerTable.getItems().isEmpty()) {
            playerTable.getSelectionModel().selectFirst();
            renderSquadPlayerCard(playerTable.getItems().get(0));
        }
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

        String current = team.getCurrentTacticName();
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

    private void renderSquadPlayerCard(Player p) {
        if (p == null) {
            squadCardNameLabel.setText("Select a player");
            squadCardMetaLabel.setText("-");
            squadCardOverallLabel.setText("OVR 0");
            squadCardStatsBox.getChildren().clear();
            squadCardExtraLabel.setText("-");
            clearCanvas(squadRadarCanvas);
            return;
        }
        squadCardNameLabel.setText(p.getName());
        squadCardMetaLabel.setText(p.getPosition() + "  |  Age " + p.getAge() + "  |  " + p.getStatusDisplay());
        squadCardOverallLabel.setText("OVR " + p.getSkillLevel());
        drawRadarAnimated(squadRadarCanvas, p.getSpecificAttributes());

        squadCardStatsBox.getChildren().clear();
        for (Map.Entry<String, Integer> e : p.getSpecificAttributes().entrySet()) {
            HBox row = new HBox(8);
            Label k = new Label(e.getKey());
            k.getStyleClass().add("player-stat-row");
            Label v = new Label(String.valueOf(e.getValue()));
            v.getStyleClass().add("player-stat-row-value");
            row.getChildren().addAll(k, v);
            squadCardStatsBox.getChildren().add(row);
        }

        int avg = (int) p.getSpecificAttributes().values().stream().mapToInt(Integer::intValue).average().orElse(p.getSkillLevel());
        int peak = p.getSpecificAttributes().values().stream().mapToInt(Integer::intValue).max().orElse(p.getSkillLevel());
        int floor = p.getSpecificAttributes().values().stream().mapToInt(Integer::intValue).min().orElse(p.getSkillLevel());
        squadCardExtraLabel.setText("Avg: " + avg + "   Peak: " + peak + "   Floor: " + floor
                + "   Injury Matches Left: " + p.getInjuryMatchesRemaining());
    }

    private void clearCanvas(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void drawRadarAnimated(Canvas canvas, Map<String, Integer> attrs) {
        Map<String, Double> previous = squadRadarPrev;
        Map<String, Double> target = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : attrs.entrySet()) target.put(e.getKey(), e.getValue().doubleValue());

        Timeline anim = new Timeline();
        int frames = 12;
        for (int i = 1; i <= frames; i++) {
            final double t = i / (double) frames;
            anim.getKeyFrames().add(new KeyFrame(Duration.millis(i * 18), e -> {
                Map<String, Integer> step = new java.util.LinkedHashMap<>();
                for (Map.Entry<String, Double> entry : target.entrySet()) {
                    double from = previous.getOrDefault(entry.getKey(), entry.getValue());
                    int v = (int) Math.round(from + (entry.getValue() - from) * t);
                    step.put(entry.getKey(), v);
                }
                drawRadar(canvas, step);
            }));
        }
        anim.setOnFinished(e -> squadRadarPrev = target);
        anim.play();
    }

    private void drawRadar(Canvas canvas, Map<String, Integer> attrs) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth(), h = canvas.getHeight();
        gc.clearRect(0, 0, w, h);
        if (attrs == null || attrs.isEmpty()) return;
        var statList = new ArrayList<>(attrs.entrySet());
        int n = statList.size();
        double cx = w / 2.0, cy = h / 2.0, r = Math.min(w, h) * 0.33;

        gc.setStroke(javafx.scene.paint.Color.web("#253042"));
        for (int ring = 1; ring <= 4; ring++) {
            double rr = r * ring / 4.0;
            gc.beginPath();
            for (int i = 0; i < n; i++) {
                double a = -Math.PI / 2 + i * 2 * Math.PI / n;
                double x = cx + Math.cos(a) * rr;
                double y = cy + Math.sin(a) * rr;
                if (i == 0) gc.moveTo(x, y); else gc.lineTo(x, y);
            }
            gc.closePath();
            gc.stroke();
        }

        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            double a = -Math.PI / 2 + i * 2 * Math.PI / n;
            double v = Math.max(40, Math.min(99, statList.get(i).getValue()));
            double vr = r * ((v - 40.0) / 59.0);
            xs[i] = cx + Math.cos(a) * vr;
            ys[i] = cy + Math.sin(a) * vr;
        }
        gc.setFill(javafx.scene.paint.Color.web("#00dfa2", 0.28));
        gc.setStroke(javafx.scene.paint.Color.web("#00dfa2"));
        gc.fillPolygon(xs, ys, n);
        gc.strokePolygon(xs, ys, n);
    }
}
