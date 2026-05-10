package com.sportmanager.ui.controller;

import com.sportmanager.SportManager;
import com.sportmanager.basketball.BasketballTactics;
import com.sportmanager.core.InjuryRecord;
import com.sportmanager.core.Match;
import com.sportmanager.core.MatchResult;
import com.sportmanager.core.MatchSegment;
import com.sportmanager.core.Player;
import com.sportmanager.core.Sport;
import com.sportmanager.core.Team;
import com.sportmanager.settings.AppSettings;
import com.sportmanager.ui.ScrollViewportBindings;
import com.sportmanager.ui.component.TacticPitchCanvas;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.Random;

/**
 * MatchController — pre-match setup (lineup, tactics) and live match display. (MS-1 to MS-5)
 *
 * Before match: shows lineup editor and tactic selector.
 * During match: shows each segment's events and the running score.
 * After match: shows full-time result and the "Continue" button that calls
 *              SportManager.advanceWeek() to finalise the round.
 *
 * The controller never imports concrete sport classes; all data comes via
 * SportManager and the abstract Match / Team / Player types.
 */
public class MatchScreenController implements Initializable {

    @FXML private Label homeTeamLabel;
    @FXML private Label awayTeamLabel;
    @FXML private Label scoreLabel;
    @FXML private Label matchInfoLabel;
    @FXML private ImageView homeCrestImage;
    @FXML private ImageView awayCrestImage;
    @FXML private Label homeCrestFallbackLabel;
    @FXML private Label awayCrestFallbackLabel;
    @FXML private VBox eventsBox;
    @FXML private StackPane matchCenterShell;
    @FXML private HBox centerContentRow;
    @FXML private javafx.scene.control.SplitPane liveSplitPane;
    @FXML private StackPane liveFieldContainer;
    @FXML private Canvas liveFieldCanvas;
    @FXML private Pane liveFieldOverlayLayer;
    @FXML private VBox hoverPlayerCard;
    @FXML private Label hoverCardNameLabel;
    @FXML private Label hoverCardOverallLabel;
    @FXML private Label hoverCardMetaLabel;
    @FXML private Label hoverCardStatsLabel;
    @FXML private Canvas hoverRadarCanvas;
    @FXML private Label liveFieldHintLabel;
    @FXML private Button simulateButton;
    @FXML private Button continueButton;
    @FXML private ToggleButton autoplayToggle;
    @FXML private Slider eventSpeedSlider;
    @FXML private Label eventSpeedLabel;
    @FXML private Button pauseResumeButton;
    @FXML private Button timeoutButton;
    @FXML private Button nextEventButton;
    @FXML private VBox subPanelScroll;
    @FXML private Label breakPanelTitleLabel;
    @FXML private Label subCountLabel;
    @FXML private Label timeoutCountLabel;
    @FXML private Label pauseReasonLabel;
    @FXML private Label rivalFormationLabel;
    @FXML private javafx.scene.control.ComboBox<String> tacticCombo;
    @FXML private TacticPitchCanvas tacticCanvas;
    @FXML private StackPane subOverlay;
    @FXML private VBox subModalRoot;
    @FXML private Label subOverlayReasonLabel;
    @FXML private ListView<Player> overlayCurrentList;
    @FXML private ListView<Player> overlayBenchList;
    @FXML private Label leftPlayerNameLabel;
    @FXML private Label leftPlayerMetaLabel;
    @FXML private VBox leftStatsBox;
    @FXML private Label leftOverallBadgeLabel;
    @FXML private Label leftRoleChipLabel;
    @FXML private Label leftStatusChipLabel;
    @FXML private Label leftExtraStatsLabel;
    @FXML private Canvas leftRadarCanvas;
    @FXML private Label rightPlayerNameLabel;
    @FXML private Label rightPlayerMetaLabel;
    @FXML private VBox rightStatsBox;
    @FXML private Label rightOverallBadgeLabel;
    @FXML private Label rightRoleChipLabel;
    @FXML private Label rightStatusChipLabel;
    @FXML private Label rightExtraStatsLabel;
    @FXML private Canvas rightRadarCanvas;
    @FXML private VBox leftPlayerCardVBox;
    @FXML private VBox rightPlayerCardVBox;

    private SportManager sm;
    private Match match;
    private Team managedTeam;
    private final Queue<String> pendingEvents = new ArrayDeque<>();
    private Timeline autoplayTimeline;
    private boolean segmentRunning;
    private boolean userPaused;
    private boolean timeoutPaused;
    private boolean injuryPaused;
    private boolean injuryResolutionPending;
    private String pendingInjuryPlayerName;
    private int timeoutLimit;
    private int timeoutsUsed;
    private boolean subOverlayMandatory;
    private Player forcedOutPlayer;
    private Map<String, Double> leftRadarPrev = Map.of();
    private Map<String, Double> rightRadarPrev = Map.of();
    private final List<FieldPlayerNode> liveFieldNodes = new ArrayList<>();
    private FieldPlayerNode hoveredNode;
    private FieldPlayerNode lockedNode;
    private FadeTransition hoverFadeIn;
    private FadeTransition hoverFadeOut;
    private final Random botRng = new Random();
    private Team botTeam;
    private int botTimeoutsUsed;
    private int botSubstitutionsUsed;
    private int eventsSinceBotDecision;

    private enum BotMode {
        BEGINNER(0.11, 0.06, 0.07),
        STREET_PLAYER(0.20, 0.13, 0.14),
        SEMI_PRO(0.30, 0.22, 0.20),
        PROFESSIONAL(0.42, 0.32, 0.28);
        final double tacticChance;
        final double subChance;
        final double timeoutChance;
        BotMode(double tacticChance, double subChance, double timeoutChance) {
            this.tacticChance = tacticChance;
            this.subChance = subChance;
            this.timeoutChance = timeoutChance;
        }
    }

    private record Dot(double x, double y, String label) {}

    private static final Map<String, List<Dot>> FOOTBALL_FORMATIONS = Map.of(
            "4-3-3", List.of(
                    new Dot(0.50, 0.88, "GK"), new Dot(0.14, 0.70, "LB"), new Dot(0.37, 0.73, "CB"),
                    new Dot(0.63, 0.73, "CB"), new Dot(0.86, 0.70, "RB"), new Dot(0.24, 0.48, "LM"),
                    new Dot(0.50, 0.45, "CM"), new Dot(0.76, 0.48, "RM"), new Dot(0.22, 0.19, "LW"),
                    new Dot(0.50, 0.13, "CF"), new Dot(0.78, 0.19, "RW")
            ),
            "4-4-2", List.of(
                    new Dot(0.50, 0.88, "GK"), new Dot(0.11, 0.70, "LB"), new Dot(0.36, 0.73, "CB"),
                    new Dot(0.64, 0.73, "CB"), new Dot(0.89, 0.70, "RB"), new Dot(0.11, 0.47, "LM"),
                    new Dot(0.36, 0.46, "CM"), new Dot(0.64, 0.46, "CM"), new Dot(0.89, 0.47, "RM"),
                    new Dot(0.35, 0.17, "ST"), new Dot(0.65, 0.17, "ST")
            ),
            "4-2-3-1", List.of(
                    new Dot(0.50, 0.88, "GK"), new Dot(0.11, 0.70, "LB"), new Dot(0.36, 0.73, "CB"),
                    new Dot(0.64, 0.73, "CB"), new Dot(0.89, 0.70, "RB"), new Dot(0.35, 0.54, "DM"),
                    new Dot(0.65, 0.54, "DM"), new Dot(0.16, 0.33, "LAM"), new Dot(0.50, 0.31, "CAM"),
                    new Dot(0.84, 0.33, "RAM"), new Dot(0.50, 0.12, "ST")
            ),
            "3-5-2", List.of(
                    new Dot(0.50, 0.88, "GK"), new Dot(0.25, 0.71, "CB"), new Dot(0.50, 0.74, "CB"),
                    new Dot(0.75, 0.71, "CB"), new Dot(0.07, 0.50, "LWB"), new Dot(0.29, 0.48, "CM"),
                    new Dot(0.50, 0.46, "CM"), new Dot(0.71, 0.48, "CM"), new Dot(0.93, 0.50, "RWB"),
                    new Dot(0.35, 0.17, "ST"), new Dot(0.65, 0.17, "ST")
            ),
            "5-3-2", List.of(
                    new Dot(0.50, 0.88, "GK"), new Dot(0.09, 0.67, "LWB"), new Dot(0.27, 0.73, "CB"),
                    new Dot(0.50, 0.76, "CB"), new Dot(0.73, 0.73, "CB"), new Dot(0.91, 0.67, "RWB"),
                    new Dot(0.25, 0.46, "CM"), new Dot(0.50, 0.44, "CM"), new Dot(0.75, 0.46, "CM"),
                    new Dot(0.35, 0.17, "ST"), new Dot(0.65, 0.17, "ST")
            )
    );

    private int maxSubs() { return AppSettings.getInstance().getMaxSubstitutions(); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sm = SportManager.getInstance();
        match = sm.getCurrentMatch();
        managedTeam = sm.getManagedTeam();
        if (match == null || managedTeam == null) return;
        botTeam = getRivalTeam();
        botTimeoutsUsed = 0;
        botSubstitutionsUsed = 0;
        eventsSinceBotDecision = 0;

        homeTeamLabel.setText(match.getHomeTeam().getName());
        awayTeamLabel.setText(match.getAwayTeam().getName());
        scoreLabel.setText("0 – 0");
        matchInfoLabel.setText("Matchday " + sm.getCurrentWeek() + "  ·  " + sm.getLeague().getName());

        timeoutLimit = AppSettings.getInstance().getTimeoutLimitForSport(sm.getSport().getName());
        setupCrests();
        setupTacticPanel();
        updateSegmentAndBreakLabels();
        updateSubCountLabel();
        updateTimeoutLabel();
        continueButton.setVisible(false);
        continueButton.setManaged(false);
        setInterventionPanelVisible(false);
        if (subOverlay != null) {
            subOverlay.setVisible(false);
            subOverlay.setManaged(false);
        }
        updatePauseControls();
        setupOverlayBindings();
        if (eventSpeedSlider != null) {
            eventSpeedSlider.valueProperty().addListener((obs, oldV, newV) -> updateEventSpeedLabelAndTimeline());
            updateEventSpeedLabelAndTimeline();
        }
        setupResponsiveMatchLayout();
        bindBreakPanelTacticSizing();
        bindSubstitutionOverlayRadarSizing();
        setupLiveFieldPanel();
    }

    private void bindBreakPanelTacticSizing() {
        if (subPanelScroll == null || tacticCanvas == null) return;
        ScrollViewportBindings.attachRegionLayoutListeners(subPanelScroll, () -> {
            double inner = ScrollViewportBindings.regionInnerWidth(subPanelScroll, 12);
            if (inner <= 0) return;
            ScrollViewportBindings.layoutTacticPitchCanvas(tacticCanvas, inner, 276, 220, 320);
            String t = tacticCombo != null ? tacticCombo.getValue() : null;
            if (t != null && !t.isBlank()) tacticCanvas.drawFormation(t);
        });
    }

    private void bindSubstitutionOverlayRadarSizing() {
        if (leftPlayerCardVBox == null || rightPlayerCardVBox == null
                || leftRadarCanvas == null || rightRadarCanvas == null) return;
        Runnable resizeLeft = () -> {
            double w = ScrollViewportBindings.regionInnerWidth(leftPlayerCardVBox, 24);
            if (w <= 0) return;
            ScrollViewportBindings.layoutRadarCanvas(leftRadarCanvas, w, 230.0 / 360.0);
            Player left = overlayCurrentList != null ? overlayCurrentList.getSelectionModel().getSelectedItem() : null;
            if (left != null) drawRadar(leftRadarCanvas, new LinkedHashMap<>(left.getSpecificAttributes()), true);
            else clearCanvas(leftRadarCanvas);
        };
        Runnable resizeRight = () -> {
            double w = ScrollViewportBindings.regionInnerWidth(rightPlayerCardVBox, 24);
            if (w <= 0) return;
            ScrollViewportBindings.layoutRadarCanvas(rightRadarCanvas, w, 230.0 / 360.0);
            Player right = overlayBenchList != null ? overlayBenchList.getSelectionModel().getSelectedItem() : null;
            if (right != null) drawRadar(rightRadarCanvas, new LinkedHashMap<>(right.getSpecificAttributes()), false);
            else clearCanvas(rightRadarCanvas);
        };
        ScrollViewportBindings.attachRegionLayoutListeners(leftPlayerCardVBox, resizeLeft);
        ScrollViewportBindings.attachRegionLayoutListeners(rightPlayerCardVBox, resizeRight);
    }

    private void bindHoverRadarSizing() {
        if (hoverPlayerCard == null || hoverRadarCanvas == null) return;
        ScrollViewportBindings.attachRegionLayoutListeners(hoverPlayerCard, () -> {
            double w = ScrollViewportBindings.regionInnerWidth(hoverPlayerCard, 16);
            if (w <= 0) return;
            ScrollViewportBindings.layoutRadarCanvas(hoverRadarCanvas, w, 150.0 / 210.0);
            refreshHoverRadarAfterResize();
        });
    }

    private void refreshHoverRadarAfterResize() {
        if (hoverRadarCanvas == null || hoverPlayerCard == null) return;
        Player p = lockedNode != null ? lockedNode.player()
                : (hoveredNode != null ? hoveredNode.player() : null);
        if (p == null || !hoverPlayerCard.isVisible()) return;
        boolean managed = lockedNode != null && lockedNode.player() == p;
        drawHoverRadar(hoverRadarCanvas, p.getSpecificAttributes(), managed);
    }

    private void setupResponsiveMatchLayout() {
        if (matchCenterShell != null) {
            Runnable apply = () -> {
                double w = matchCenterShell.getWidth();
                double h = matchCenterShell.getHeight();
                if (w <= 0 || h <= 0) return;
                // User requested large outer breathing space.
                matchCenterShell.setPadding(new Insets(h * 0.10, w * 0.15, h * 0.10, w * 0.15));
            };
            matchCenterShell.widthProperty().addListener((obs, o, n) -> apply.run());
            matchCenterShell.heightProperty().addListener((obs, o, n) -> apply.run());
        }
        if (centerContentRow != null) {
            centerContentRow.setSpacing(34);
        }
        if (liveSplitPane != null) {
            liveSplitPane.setDividerPositions(0.45);
        }
    }

    private void setupTacticPanel() {
        Sport sport = sm.getSport();
        if (sport == null) return;
        tacticCombo.getItems().setAll(sport.getTactics());
        String current = managedTeam.getCurrentTacticName();
        tacticCombo.setValue(current);
        if (tacticCanvas != null) tacticCanvas.drawFormation(current);
        tacticCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && tacticCanvas != null) tacticCanvas.drawFormation(newVal);
        });
    }

    private void updateSegmentAndBreakLabels() {
        boolean bb = sm.getSport() != null && "Basketball".equals(sm.getSport().getName());
        if (!match.isFinished()) simulateButton.setText("▶  Start " + match.getSegmentLabel(match.getCurrentSegment()));
        breakPanelTitleLabel.setText(bb ? "QUARTER BREAK" : "HALF TIME");
    }

    private void setInterventionPanelVisible(boolean visible) {
        subPanelScroll.setVisible(visible);
        subPanelScroll.setManaged(visible);
        if (visible) updateRivalFormationLabel();
    }

    private void updateSubCountLabel() {
        subCountLabel.setText("Substitutions used: " + sm.getSubstitutionsUsed() + " / " + maxSubs());
    }

    private void updateTimeoutLabel() {
        int left = Math.max(0, timeoutLimit - timeoutsUsed);
        timeoutCountLabel.setText("Timeouts left: " + left + " / " + timeoutLimit);
        timeoutButton.setDisable(left == 0 || match.isFinished());
    }

    private boolean isAnyPauseActive() {
        return userPaused || timeoutPaused || injuryPaused;
    }

    private void updatePauseControls() {
        boolean autoplay = autoplayToggle.isSelected();
        autoplayToggle.setText(autoplay ? "Autoplay: ON" : "Autoplay: OFF");
        pauseResumeButton.setText(isAnyPauseActive() ? "Resume" : "Pause");
        nextEventButton.setDisable(autoplay || !segmentRunning || isAnyPauseActive());
    }

    @FXML
    private void onSimulateNext() {
        if (match == null || match.isFinished() || segmentRunning || injuryResolutionPending) return;
        startNextSegment();
    }

    private void startNextSegment() {
        setInterventionPanelVisible(false);
        userPaused = false;
        timeoutPaused = false;
        injuryPaused = false;
        injuryResolutionPending = false;
        pendingInjuryPlayerName = null;

        match.beginSegmentSimulation();
        MatchSegment seg = match.getSegments().get(match.getSegments().size() - 1);
        Label segLabel = new Label(seg.getLabel().toUpperCase());
        segLabel.getStyleClass().add("segment-header");
        eventsBox.getChildren().add(segLabel);

        segmentRunning = true;
        refreshLiveField();
        simulateButton.setDisable(true);
        simulateButton.setText("Segment Running...");
        if (autoplayToggle.isSelected()) {
            ensureAutoplayTimeline();
            autoplayTimeline.playFromStart();
        } else {
            consumeNextEvent();
        }
        updatePauseControls();
    }

    private void ensureAutoplayTimeline() {
        if (autoplayTimeline != null) return;
        autoplayTimeline = new Timeline(new KeyFrame(Duration.millis(650), e -> consumeNextEvent()));
        autoplayTimeline.setCycleCount(Timeline.INDEFINITE);
        updateEventSpeedLabelAndTimeline();
    }

    @FXML
    private void onEventSpeedChanged() {
        updateEventSpeedLabelAndTimeline();
    }

    private void updateEventSpeedLabelAndTimeline() {
        double speed = eventSpeedSlider != null ? eventSpeedSlider.getValue() : 1.0;
        if (eventSpeedLabel != null) {
            eventSpeedLabel.setText(String.format("%.1fx", speed));
        }
        if (autoplayTimeline != null) {
            autoplayTimeline.setRate(speed);
        }
    }

    @FXML
    private void onToggleAutoplay() {
        if (segmentRunning && autoplayToggle.isSelected() && !isAnyPauseActive()) {
            ensureAutoplayTimeline();
            autoplayTimeline.play();
        } else if (autoplayTimeline != null) {
            autoplayTimeline.stop();
        }
        updatePauseControls();
    }

    @FXML
    private void onPauseResume() {
        if (!segmentRunning && !match.isAtBreak()) return;
        if (isSubOverlayOpen()) return;
        if (isAnyPauseActive()) {
            userPaused = false;
            timeoutPaused = false;
            injuryPaused = false;
            pauseReasonLabel.setText("Match paused for tactical intervention.");
            setInterventionPanelVisible(false);
            if (segmentRunning && autoplayToggle.isSelected()) {
                ensureAutoplayTimeline();
                autoplayTimeline.play();
            }
        } else {
            userPaused = true;
            pauseReasonLabel.setText("Paused by user.");
            setInterventionPanelVisible(true);
            if (autoplayTimeline != null) autoplayTimeline.stop();
        }
        updatePauseControls();
    }

    @FXML
    private void onTakeTimeout() {
        if (match.isFinished() || timeoutLimit - timeoutsUsed <= 0) return;
        if (isSubOverlayOpen()) return;
        timeoutsUsed++;
        timeoutPaused = true;
        userPaused = true;
        pauseReasonLabel.setText("Timeout taken. Apply changes, then resume.");
        setInterventionPanelVisible(true);
        if (autoplayTimeline != null) autoplayTimeline.stop();
        updateTimeoutLabel();
        updatePauseControls();
    }

    @FXML
    private void onNextEvent() {
        if (isSubOverlayOpen()) return;
        consumeNextEvent();
        updatePauseControls();
    }

    private void consumeNextEvent() {
        if (!segmentRunning || isAnyPauseActive()) return;
        String ev = match.simulateNextSegmentEvent();
        if (ev == null) {
            finalizeSegment();
            return;
        }
        scoreLabel.setText(match.getScoreDisplay());
        if (ev.startsWith("End of ") || ev.startsWith("— End ")) {
            Label partial = new Label(ev);
            partial.getStyleClass().add("segment-score");
            eventsBox.getChildren().add(partial);
            finalizeSegment();
            return;
        }

        handlePossibleInjuryPause(ev);
        renderEventLine(ev);
        maybeRunBotDecision(false);
        if (isAnyPauseActive() && autoplayTimeline != null) autoplayTimeline.stop();
    }

    private void renderEventLine(String ev) {
        boolean detailed = AppSettings.getInstance().isShowDetailedEvents();
        if (!detailed && (ev.startsWith("🚑") || ev.startsWith("🟨") || ev.startsWith("🟥")
                || ev.startsWith("↩️") || ev.startsWith("❌"))) return;
        Label evLabel = new Label(ev);
        evLabel.setWrapText(true);
        evLabel.getStyleClass().add(
                ev.startsWith("⚽") || ev.startsWith("🏀") ? "event-goal"
                        : ev.startsWith("🚑") ? "event-injury"
                        : ev.startsWith("🟨") ? "event-card"
                        : "event-normal");
        eventsBox.getChildren().add(evLabel);
    }

    private void handlePossibleInjuryPause(String ev) {
        if (!ev.startsWith("🚑")) return;
        int idx = ev.indexOf(" injured");
        if (idx <= 2) return;
        String playerName = ev.substring(2, idx).trim();
        Optional<Player> injured = managedTeam.getStartingLineup().stream()
                .filter(p -> p.getName().equals(playerName))
                .findFirst();
        if (injured.isEmpty()) return;

        injuryPaused = true;
        userPaused = true;
        injuryResolutionPending = true;
        pendingInjuryPlayerName = playerName;
        pauseReasonLabel.setText("Injury detected: " + playerName + ". Resolve substitution.");
        setInterventionPanelVisible(true);
        onOpenSubstitutionModal();
    }

    private void finalizeSegment() {
        segmentRunning = false;
        if (autoplayTimeline != null) autoplayTimeline.stop();
        simulateButton.setDisable(false);
        updateSegmentAndBreakLabels();

        if (match.isAtBreak() && !match.isFinished()) {
            maybeRunBotDecision(true);
            userPaused = true;
            String rivalFormation = getRivalTeam().getCurrentTacticName();
            pauseReasonLabel.setText("Break reached. Rival formation: " + rivalFormation
                    + ". You can adjust tactics or substitute.");
            addBreakDivider();
            setInterventionPanelVisible(true);
        }

        if (match.isFinished()) {
            simulateButton.setVisible(false);
            simulateButton.setManaged(false);
            setInterventionPanelVisible(false);
            renderFullTimeBanner();
            if (AppSettings.getInstance().isAutoAdvance()) {
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(e -> onContinue());
                pause.play();
                continueButton.setText("Auto-advancing…");
            }
            continueButton.setVisible(true);
            continueButton.setManaged(true);
        }
        updatePauseControls();
    }

    private void addBreakDivider() {
        boolean bb = sm.getSport() != null && "Basketball".equals(sm.getSport().getName());
        String text = bb
                ? "── QUARTER BREAK ── Adjust sets or substitutions below ──"
                : "── HALF TIME ── Change tactics or make substitutions below ──";
        Label breakLbl = new Label(text);
        breakLbl.getStyleClass().add("break-label");
        eventsBox.getChildren().addAll(new Separator(), breakLbl);
    }

    private void renderFullTimeBanner() {
        boolean bb = sm.getSport() != null && "Basketball".equals(sm.getSport().getName());
        Label ft = new Label(bb ? "━━  FINAL  ━━" : "━━  FULL TIME  ━━");
        ft.getStyleClass().add("fulltime-banner");
        MatchResult res = match.getResult();
        String outcome = res != null ? res.getScore() : match.getScoreDisplay();
        Label result = new Label(outcome);
        result.getStyleClass().add("fulltime-result");
        eventsBox.getChildren().addAll(ft, result);
        if (res != null && !res.getInjuries().isEmpty()) {
            Label injHeader = new Label("Match Injuries:");
            injHeader.getStyleClass().add("injury-header");
            eventsBox.getChildren().add(injHeader);
            for (InjuryRecord ir : res.getInjuries()) {
                Label inj = new Label("🚑  " + ir);
                inj.getStyleClass().add("injury-label");
                eventsBox.getChildren().add(inj);
            }
        }
    }

    @FXML
    private void onOpenSubstitutionModal() {
        forcedOutPlayer = null;
        subOverlayMandatory = injuryResolutionPending;
        if (injuryResolutionPending && pendingInjuryPlayerName != null) {
            forcedOutPlayer = managedTeam.getStartingLineup().stream()
                    .filter(p -> p.getName().equals(pendingInjuryPlayerName))
                    .findFirst().orElse(null);
        }
        openSubOverlay();
    }

    private void applySubstitution(Player out, Player in) {
        if (sm.getSubstitutionsUsed() >= maxSubs()) {
            pauseReasonLabel.setText("Maximum substitutions reached.");
            return;
        }
        managedTeam.getStartingLineup().remove(out);
        managedTeam.getStartingLineup().add(in);
        managedTeam.getSubstitutes().remove(in);
        managedTeam.getSubstitutes().add(out);
        sm.incrementSubstitutions();
        updateSubCountLabel();
        Label subEvent = new Label("🔄  " + in.getName() + " on  for  " + out.getName() + " (" + managedTeam.getName() + ")");
        subEvent.getStyleClass().add("event-sub");
        eventsBox.getChildren().add(subEvent);
        refreshLiveField();
    }

    private String formatPlayerStats(Player p) {
        if (p == null) return "No player selected.";
        StringBuilder sb = new StringBuilder();
        sb.append(p.getName()).append(" | ").append(p.getPosition()).append(" | OVR ").append(p.getSkillLevel());
        for (Map.Entry<String, Integer> e : p.getSpecificAttributes().entrySet()) {
            sb.append(System.lineSeparator()).append(e.getKey()).append(": ").append(e.getValue());
        }
        return sb.toString();
    }

    private void setupOverlayBindings() {
        if (overlayCurrentList == null || overlayBenchList == null) return;
        overlayCurrentList.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
            renderPlayerCard(newP, leftPlayerNameLabel, leftPlayerMetaLabel, leftStatsBox, leftExtraStatsLabel,
                    leftOverallBadgeLabel, leftRoleChipLabel, leftStatusChipLabel, leftRadarCanvas, true);
        });
        overlayBenchList.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
            renderPlayerCard(newP, rightPlayerNameLabel, rightPlayerMetaLabel, rightStatsBox, rightExtraStatsLabel,
                    rightOverallBadgeLabel, rightRoleChipLabel, rightStatusChipLabel, rightRadarCanvas, false);
        });
    }

    private void openSubOverlay() {
        if (managedTeam.getSubstitutes().isEmpty()) {
            pauseReasonLabel.setText("No substitutes available.");
            injuryResolutionPending = false;
            injuryPaused = false;
            return;
        }

        subOverlayReasonLabel.setText(subOverlayMandatory
                ? "Injury substitution required before continuing."
                : "Select one starter and one bench player to compare and swap.");

        if (forcedOutPlayer != null) {
            overlayCurrentList.getItems().setAll(List.of(forcedOutPlayer));
            overlayCurrentList.getSelectionModel().selectFirst();
            overlayCurrentList.setDisable(true);
        } else {
            overlayCurrentList.getItems().setAll(managedTeam.getStartingLineup());
            overlayCurrentList.getSelectionModel().clearSelection();
            overlayCurrentList.setDisable(false);
        }
        overlayBenchList.getItems().setAll(managedTeam.getSubstitutes());
        overlayBenchList.getSelectionModel().clearSelection();

        renderPlayerCard(overlayCurrentList.getSelectionModel().getSelectedItem(),
                leftPlayerNameLabel, leftPlayerMetaLabel, leftStatsBox, leftExtraStatsLabel,
                leftOverallBadgeLabel, leftRoleChipLabel, leftStatusChipLabel, leftRadarCanvas, true);
        renderPlayerCard(null, rightPlayerNameLabel, rightPlayerMetaLabel, rightStatsBox, rightExtraStatsLabel,
                rightOverallBadgeLabel, rightRoleChipLabel, rightStatusChipLabel, rightRadarCanvas, false);

        subOverlay.setVisible(true);
        subOverlay.setManaged(true);
    }

    private void closeSubOverlay(boolean force) {
        if (subOverlayMandatory && !force) return;
        subOverlay.setVisible(false);
        subOverlay.setManaged(false);
    }

    private boolean isSubOverlayOpen() {
        return subOverlay != null && subOverlay.isVisible();
    }

    @FXML
    private void onCloseSubOverlay() {
        if (subOverlayMandatory) {
            pauseReasonLabel.setText("You must resolve injury substitution first.");
            return;
        }
        closeSubOverlay(false);
    }

    @FXML
    private void onKeepCurrentSubstitution() {
        if (subOverlayMandatory) {
            pauseReasonLabel.setText("Injury substitution required.");
            return;
        }
        injuryResolutionPending = false;
        injuryPaused = false;
        closeSubOverlay(false);
        updatePauseControls();
    }

    @FXML
    private void onApplyOverlaySubstitution() {
        Player out = forcedOutPlayer != null ? forcedOutPlayer : overlayCurrentList.getSelectionModel().getSelectedItem();
        Player in = overlayBenchList.getSelectionModel().getSelectedItem();
        if (out == null || in == null) {
            pauseReasonLabel.setText("Choose both players first.");
            return;
        }
        applySubstitution(out, in);
        injuryResolutionPending = false;
        injuryPaused = false;
        subOverlayMandatory = false;
        forcedOutPlayer = null;
        closeSubOverlay(true);
        updatePauseControls();
    }

    private void renderPlayerCard(Player player,
                                  Label nameLabel,
                                  Label metaLabel,
                                  VBox statsBox,
                                  Label extraStatsLabel,
                                  Label overallBadgeLabel,
                                  Label roleChipLabel,
                                  Label statusChipLabel,
                                  Canvas canvas,
                                  boolean left) {
        if (player == null) {
            nameLabel.setText("-");
            metaLabel.setText("Select a player");
            statsBox.getChildren().clear();
            extraStatsLabel.setText("-");
            overallBadgeLabel.setText("OVR 0");
            roleChipLabel.setText("ROLE");
            statusChipLabel.setText("N/A");
            statusChipLabel.getStyleClass().setAll("chip-status-inj");
            clearCanvas(canvas);
            return;
        }
        nameLabel.setText(player.getName());
        metaLabel.setText(player.getPosition() + "  |  Age " + player.getAge() + "  |  " + player.getStatusDisplay());
        populateStatRows(player, statsBox, !left);
        extraStatsLabel.setText(formatExtraStats(player));
        overallBadgeLabel.setText("OVR " + player.getSkillLevel());
        roleChipLabel.setText(normalizeRole(player.getPosition()));
        statusChipLabel.setText(player.isInjured() ? "INJURED" : "FIT");
        statusChipLabel.getStyleClass().setAll(player.isInjured() ? "chip-status-inj" : "chip-status-fit");
        Map<String, Integer> attrs = player.getSpecificAttributes();
        drawRadarAnimated(canvas, attrs, left);
    }

    private void populateStatRows(Player player, VBox target, boolean includeDiffAgainstLeft) {
        target.getChildren().clear();
        Map<String, Integer> attrs = player.getSpecificAttributes();
        Player leftSelected = overlayCurrentList.getSelectionModel().getSelectedItem();
        Map<String, Integer> leftAttrs = leftSelected != null ? leftSelected.getSpecificAttributes() : Map.of();

        for (Map.Entry<String, Integer> e : attrs.entrySet()) {
            HBox row = new HBox(8);
            Label key = new Label(e.getKey());
            key.getStyleClass().add("player-stat-row");
            Label val = new Label(String.valueOf(e.getValue()));
            val.getStyleClass().add("player-stat-row-value");
            row.getChildren().addAll(key, val);

            if (includeDiffAgainstLeft && leftAttrs.containsKey(e.getKey())) {
                int diff = e.getValue() - leftAttrs.get(e.getKey());
                Label delta = new Label((diff > 0 ? "+" : "") + diff);
                if (diff > 0) delta.getStyleClass().add("stat-diff-up");
                else if (diff < 0) delta.getStyleClass().add("stat-diff-down");
                else delta.getStyleClass().add("stat-diff-same");
                Label trend = new Label(diff > 0 ? "BETTER" : diff < 0 ? "WORSE" : "SAME");
                trend.getStyleClass().add(diff > 0 ? "stat-diff-up" : diff < 0 ? "stat-diff-down" : "stat-diff-same");
                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                row.getChildren().addAll(spacer, delta, trend);
            }
            target.getChildren().add(row);
        }
    }

    private String formatExtraStats(Player player) {
        Map<String, Integer> attrs = player.getSpecificAttributes();
        if (attrs.isEmpty()) {
            return "Base Skill: " + player.getSkillLevel();
        }

        int sum = 0;
        int best = Integer.MIN_VALUE;
        int worst = Integer.MAX_VALUE;
        String bestKey = "";
        String worstKey = "";
        for (Map.Entry<String, Integer> e : attrs.entrySet()) {
            int v = e.getValue();
            sum += v;
            if (v > best) {
                best = v;
                bestKey = e.getKey();
            }
            if (v < worst) {
                worst = v;
                worstKey = e.getKey();
            }
        }
        int avg = Math.round((float) sum / attrs.size());
        int consistency = 100 - Math.max(0, best - worst);
        return "Avg Attribute: " + avg
                + "   |   Best: " + bestKey + " " + best
                + "   |   Weakest: " + worstKey + " " + worst
                + "   |   Consistency: " + consistency
                + "   |   Injury Matches Left: " + player.getInjuryMatchesRemaining();
    }

    private String normalizeRole(String position) {
        return switch (position) {
            case "GK", "C" -> "ANCHOR";
            case "CB", "LB", "RB", "CDM", "PF" -> "DEFENDER";
            case "CM", "CAM", "PG", "SG" -> "PLAYMAKER";
            case "LW", "RW", "ST", "CF", "SF" -> "ATTACKER";
            default -> "UTILITY";
        };
    }

    private void clearCanvas(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void drawRadarAnimated(Canvas canvas, Map<String, Integer> attrs, boolean left) {
        Map<String, Double> previous = left ? leftRadarPrev : rightRadarPrev;
        Map<String, Double> target = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : attrs.entrySet()) {
            target.put(e.getKey(), e.getValue().doubleValue());
        }
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
                drawRadar(canvas, step, left);
            }));
        }
        anim.setOnFinished(e -> {
            if (left) leftRadarPrev = target;
            else rightRadarPrev = target;
        });
        anim.play();
    }

    private void drawRadar(Canvas canvas, Map<String, Integer> attrs, boolean left) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        gc.clearRect(0, 0, w, h);
        if (attrs == null || attrs.isEmpty()) return;

        List<Map.Entry<String, Integer>> statList = new ArrayList<>(attrs.entrySet());
        int n = statList.size();
        double cx = w / 2.0;
        double cy = h / 2.0;
        double r = Math.min(w, h) * 0.34;

        gc.setStroke(javafx.scene.paint.Color.web("#253042"));
        for (int ring = 1; ring <= 4; ring++) {
            double rr = r * ring / 4.0;
            gc.beginPath();
            for (int i = 0; i < n; i++) {
                double a = -Math.PI / 2 + i * 2 * Math.PI / n;
                double x = cx + Math.cos(a) * rr;
                double y = cy + Math.sin(a) * rr;
                if (i == 0) gc.moveTo(x, y);
                else gc.lineTo(x, y);
            }
            gc.closePath();
            gc.stroke();
        }

        gc.setStroke(javafx.scene.paint.Color.web("#2d3d57"));
        for (int i = 0; i < n; i++) {
            double a = -Math.PI / 2 + i * 2 * Math.PI / n;
            double x = cx + Math.cos(a) * r;
            double y = cy + Math.sin(a) * r;
            gc.strokeLine(cx, cy, x, y);
            gc.setFill(javafx.scene.paint.Color.web("#8ba0bf"));
            gc.fillText(statList.get(i).getKey(), cx + Math.cos(a) * (r + 14), cy + Math.sin(a) * (r + 14));
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

        javafx.scene.paint.Color fill = left
                ? javafx.scene.paint.Color.web("#00dfa2", 0.28)
                : javafx.scene.paint.Color.web("#6366f1", 0.28);
        javafx.scene.paint.Color stroke = left
                ? javafx.scene.paint.Color.web("#00dfa2")
                : javafx.scene.paint.Color.web("#818cf8");
        gc.setFill(fill);
        gc.setStroke(stroke);
        gc.fillPolygon(xs, ys, n);
        gc.strokePolygon(xs, ys, n);
    }

    @FXML
    private void onApplyTactic() {
        String chosen = tacticCombo.getValue();
        if (chosen == null) return;
        managedTeam.setCurrentTactic(chosen);
        Label ev = new Label("🔧  Tactic changed to " + chosen);
        ev.getStyleClass().add("event-tactic");
        eventsBox.getChildren().add(ev);
        refreshLiveField();
    }

    private BotMode currentBotMode() {
        String mode = AppSettings.getInstance().getRivalBotMode();
        return switch (mode) {
            case "Beginner" -> BotMode.BEGINNER;
            case "Street Player" -> BotMode.STREET_PLAYER;
            case "Professional" -> BotMode.PROFESSIONAL;
            default -> BotMode.SEMI_PRO;
        };
    }

    private void maybeRunBotDecision(boolean atBreak) {
        if (botTeam == null || match.isFinished()) return;
        eventsSinceBotDecision++;
        BotMode mode = currentBotMode();
        double cadenceBoost = atBreak ? 0.35 : (eventsSinceBotDecision >= 4 ? 0.10 : 0.0);
        if (botRng.nextDouble() < mode.tacticChance + cadenceBoost) {
            applyBotTacticChange();
        }
        if (botRng.nextDouble() < mode.subChance + cadenceBoost) {
            applyBotSubstitution();
        }
        if (botRng.nextDouble() < mode.timeoutChance + cadenceBoost) {
            applyBotTimeout();
        }
        if (atBreak || eventsSinceBotDecision >= 4) {
            eventsSinceBotDecision = 0;
        }
    }

    private void applyBotTacticChange() {
        List<String> options = sm.getSport() != null ? sm.getSport().getTactics() : List.of();
        if (options.isEmpty()) return;
        String current = botTeam.getCurrentTacticName();
        List<String> candidates = options.stream().filter(t -> !t.equals(current)).toList();
        if (candidates.isEmpty()) return;
        String next = candidates.get(botRng.nextInt(candidates.size()));
        botTeam.setCurrentTactic(next);
        Label ev = new Label("🤖  " + botTeam.getName() + " switched tactic to " + next);
        ev.getStyleClass().add("event-tactic");
        eventsBox.getChildren().add(ev);
        updateRivalFormationLabel();
        refreshLiveField();
    }

    private void applyBotSubstitution() {
        if (botSubstitutionsUsed >= maxSubs()) return;
        if (botTeam.getSubstitutes().isEmpty() || botTeam.getStartingLineup().isEmpty()) return;
        Optional<Player> worstStarter = botTeam.getStartingLineup().stream()
                .min(java.util.Comparator.comparingInt(Player::getSkillLevel));
        Optional<Player> bestBench = botTeam.getSubstitutes().stream()
                .max(java.util.Comparator.comparingInt(Player::getSkillLevel));
        if (worstStarter.isEmpty() || bestBench.isEmpty()) return;
        Player out = worstStarter.get();
        Player in = bestBench.get();
        if (in.getSkillLevel() <= out.getSkillLevel()) return;
        botTeam.getStartingLineup().remove(out);
        botTeam.getStartingLineup().add(in);
        botTeam.getSubstitutes().remove(in);
        botTeam.getSubstitutes().add(out);
        botSubstitutionsUsed++;
        Label ev = new Label("🔄  " + botTeam.getName() + ": " + in.getName() + " on for " + out.getName());
        ev.getStyleClass().add("event-sub");
        eventsBox.getChildren().add(ev);
        refreshLiveField();
    }

    private void applyBotTimeout() {
        if (botTimeoutsUsed >= timeoutLimit) return;
        botTimeoutsUsed++;
        Label ev = new Label("⏱️  " + botTeam.getName() + " used timeout (" + botTimeoutsUsed + "/" + timeoutLimit + ")");
        ev.getStyleClass().add("event-normal");
        eventsBox.getChildren().add(ev);
    }

    private void setupCrests() {
        setTeamCrest(match.getHomeTeam(), homeCrestImage, homeCrestFallbackLabel);
        setTeamCrest(match.getAwayTeam(), awayCrestImage, awayCrestFallbackLabel);
    }

    private Team getRivalTeam() {
        return match.getHomeTeam() == managedTeam ? match.getAwayTeam() : match.getHomeTeam();
    }

    private record FieldPlayerNode(Player player, boolean homeSide, boolean managed, double x, double y, double r) {}

    private void setupLiveFieldPanel() {
        if (liveFieldCanvas == null) return;
        if (liveFieldContainer != null) {
            liveFieldCanvas.widthProperty().bind(liveFieldContainer.widthProperty().subtract(2));
            liveFieldCanvas.heightProperty().bind(liveFieldContainer.heightProperty().subtract(2));
        }
        if (liveFieldOverlayLayer != null && liveFieldContainer != null) {
            liveFieldOverlayLayer.prefWidthProperty().bind(liveFieldContainer.widthProperty());
            liveFieldOverlayLayer.prefHeightProperty().bind(liveFieldContainer.heightProperty());
        }
        if (hoverPlayerCard != null) {
            hoverPlayerCard.setOpacity(0);
            hoverFadeIn = new FadeTransition(Duration.millis(150), hoverPlayerCard);
            hoverFadeIn.setFromValue(0);
            hoverFadeIn.setToValue(1);
            hoverFadeOut = new FadeTransition(Duration.millis(120), hoverPlayerCard);
            hoverFadeOut.setFromValue(hoverPlayerCard.getOpacity());
            hoverFadeOut.setToValue(0);
            hoverFadeOut.setOnFinished(e -> {
                if (lockedNode == null && hoveredNode == null) {
                    hoverPlayerCard.setVisible(false);
                    hoverPlayerCard.setManaged(false);
                }
            });
        }
        liveFieldCanvas.widthProperty().addListener((obs, o, n) -> redrawLiveField());
        liveFieldCanvas.heightProperty().addListener((obs, o, n) -> redrawLiveField());
        liveFieldCanvas.addEventHandler(MouseEvent.MOUSE_MOVED, this::onLiveFieldHover);
        liveFieldCanvas.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            hoveredNode = null;
            if (lockedNode == null) hideHoverCard();
            redrawLiveField();
        });
        liveFieldCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onLiveFieldClick);
        bindHoverRadarSizing();
        refreshLiveField();
    }

    private void refreshLiveField() {
        rebuildLiveFieldNodes();
        redrawLiveField();
    }

    private void rebuildLiveFieldNodes() {
        liveFieldNodes.clear();
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();
        double r = 16;
        addTeamNodes(home, true, home == managedTeam, r);
        addTeamNodes(away, false, away == managedTeam, r);
    }

    private void addTeamNodes(Team team, boolean homeSide, boolean managed, double r) {
        List<Player> lineup = new ArrayList<>(team.getStartingLineup());
        if (lineup.isEmpty()) {
            int fallbackCount = Math.max(5, Math.min(11, team.getPlayers().size()));
            lineup = new ArrayList<>(team.getPlayers().subList(0, fallbackCount));
        }
        if (lineup.isEmpty()) return;
        List<Dot> formationDots = resolveFormationDots(team.getCurrentTacticName(), lineup.size());
        int n = Math.min(lineup.size(), formationDots.size());
        for (int i = 0; i < n; i++) {
            Dot d = formationDots.get(i);
            double px = d.x();
            double py = d.y();
            // Vertical field: rival occupies top half, managed team bottom half.
            if (managed) {
                py = 0.52 + (py * 0.44);
            } else {
                py = 0.48 - ((1 - py) * 0.44);
            }
            px = 0.08 + px * 0.84;
            liveFieldNodes.add(new FieldPlayerNode(lineup.get(i), homeSide, managed, px, py, r));
        }
    }

    private List<Dot> resolveFormationDots(String tactic, int lineupSize) {
        String bbKey = BasketballTactics.diagramKeyForTactic(tactic);
        if (bbKey != null) {
            List<BasketballTactics.Dot> raw = BasketballTactics.OFFENSE_DIAGRAM.getOrDefault(
                    bbKey, BasketballTactics.OFFENSE_DIAGRAM.get(BasketballTactics.DEFAULT_OFFENSE));
            List<Dot> dots = new ArrayList<>();
            for (BasketballTactics.Dot d : raw) {
                dots.add(new Dot(d.x(), d.y(), d.label()));
            }
            return dots;
        }
        List<Dot> mapped = FOOTBALL_FORMATIONS.getOrDefault(tactic, FOOTBALL_FORMATIONS.get("4-4-2"));
        if (lineupSize == mapped.size()) return mapped;
        // Fallback to uniform spread if roster size doesn't match classic football XI.
        List<Dot> fallback = new ArrayList<>();
        int rows = lineupSize > 8 ? 4 : 3;
        int perRow = (int) Math.ceil(lineupSize / (double) rows);
        int idx = 0;
        for (int row = 0; row < rows && idx < lineupSize; row++) {
            int count = Math.min(perRow, lineupSize - idx);
            for (int c = 0; c < count; c++) {
                fallback.add(new Dot((c + 1.0) / (count + 1.0), (row + 1.0) / (rows + 1.0), "P"));
                idx++;
            }
        }
        return fallback;
    }

    private void redrawLiveField() {
        if (liveFieldCanvas == null) return;
        double w = liveFieldCanvas.getWidth();
        double h = liveFieldCanvas.getHeight();
        if (w <= 2 || h <= 2) return;
        GraphicsContext gc = liveFieldCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        boolean basketball = sm.getSport() != null && "Basketball".equals(sm.getSport().getName());
        if (basketball) drawBasketballLiveField(gc, w, h);
        else drawFootballLiveField(gc, w, h);

        for (FieldPlayerNode node : liveFieldNodes) {
            double x = node.x() * w;
            double y = node.y() * h;
            boolean active = node == hoveredNode || node == lockedNode;
            javafx.scene.paint.Color base = node.managed()
                    ? javafx.scene.paint.Color.web("#00dfa2")
                    : javafx.scene.paint.Color.web("#60a5fa");
            gc.setFill(base.deriveColor(0, 1, active ? 1.2 : 1.0, 1.0));
            gc.fillOval(x - node.r(), y - node.r(), node.r() * 2, node.r() * 2);
            gc.setStroke(active ? javafx.scene.paint.Color.web("#ffffff") : javafx.scene.paint.Color.web("#0b1220"));
            gc.setLineWidth(active ? 3.0 : 2.0);
            gc.strokeOval(x - node.r(), y - node.r(), node.r() * 2, node.r() * 2);
            gc.setFill(javafx.scene.paint.Color.web("#f8fbff"));
        gc.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.EXTRA_BOLD, 14));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.fillText(shortName(node.player().getName()), x, y + 4);
        }

        gc.setFill(javafx.scene.paint.Color.web("#e6efff"));
        gc.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 18));
        gc.fillText(getRivalTeam().getName() + " (Rival)", w * 0.5, 22);
        gc.fillText(managedTeam.getName() + " (Managed)", w * 0.5, h - 12);
    }

    private void drawFootballLiveField(GraphicsContext gc, double w, double h) {
        double pad = 10;
        gc.setFill(javafx.scene.paint.Color.web("#1a4a1a"));
        gc.fillRoundRect(0, 0, w, h, 10, 10);
        gc.setStroke(javafx.scene.paint.Color.web("#ffffff", 0.45));
        gc.strokeRoundRect(pad, pad, w - 2 * pad, h - 2 * pad, 6, 6);
        gc.strokeLine(pad, h / 2, w - pad, h / 2);
        gc.strokeOval(w / 2 - 40, h / 2 - 40, 80, 80);
    }

    private void drawBasketballLiveField(GraphicsContext gc, double w, double h) {
        double pad = 10;
        gc.setFill(javafx.scene.paint.Color.web("#d8a97f"));
        gc.fillRoundRect(0, 0, w, h, 10, 10);
        gc.setStroke(javafx.scene.paint.Color.web("#ffffff", 0.95));
        gc.strokeRoundRect(pad, pad, w - 2 * pad, h - 2 * pad, 6, 6);
        gc.strokeLine(pad, h / 2, w - pad, h / 2);
        gc.strokeOval(w / 2 - 35, h / 2 - 35, 70, 70);
    }

    private void onLiveFieldHover(MouseEvent e) {
        FieldPlayerNode node = pickNodeAt(e.getX(), e.getY());
        hoveredNode = node;
        if (lockedNode == null) {
            if (node != null) showHoverCard(node, e.getX(), e.getY());
            else hideHoverCard();
        }
        redrawLiveField();
    }

    private void onLiveFieldClick(MouseEvent e) {
        FieldPlayerNode node = pickNodeAt(e.getX(), e.getY());
        if (node == null) {
            lockedNode = null;
            hideHoverCard();
        } else {
            lockedNode = (lockedNode == node) ? null : node;
            if (lockedNode != null) {
                showHoverCard(lockedNode, e.getX(), e.getY());
            } else if (hoveredNode != null) {
                showHoverCard(hoveredNode, e.getX(), e.getY());
            } else {
                hideHoverCard();
            }
        }
        liveFieldHintLabel.setText(lockedNode != null
                ? "Player locked. Click the same player or empty field to unlock."
                : "Hover a player for collectible card view. Click to lock.");
        redrawLiveField();
    }

    private FieldPlayerNode pickNodeAt(double x, double y) {
        double w = liveFieldCanvas.getWidth();
        double h = liveFieldCanvas.getHeight();
        for (FieldPlayerNode node : liveFieldNodes) {
            double nx = node.x() * w;
            double ny = node.y() * h;
            double dx = x - nx;
            double dy = y - ny;
            if (dx * dx + dy * dy <= node.r() * node.r()) return node;
        }
        return null;
    }

    private void showHoverCard(FieldPlayerNode node, double canvasX, double canvasY) {
        if (hoverPlayerCard == null || liveFieldOverlayLayer == null || node == null) return;
        updateHoverCardContent(node);
        Point2D scenePoint = liveFieldCanvas.localToScene(canvasX + 14, canvasY + 14);
        Point2D local = liveFieldOverlayLayer.sceneToLocal(scenePoint);
        double cardW = 245;
        double cardH = 262;
        double maxX = Math.max(4, liveFieldOverlayLayer.getWidth() - cardW - 4);
        double maxY = Math.max(4, liveFieldOverlayLayer.getHeight() - cardH - 4);
        hoverPlayerCard.setLayoutX(Math.max(4, Math.min(maxX, local.getX())));
        hoverPlayerCard.setLayoutY(Math.max(4, Math.min(maxY, local.getY())));
        hoverPlayerCard.setVisible(true);
        hoverPlayerCard.setManaged(true);
        if (hoverFadeOut != null) hoverFadeOut.stop();
        if (hoverFadeIn != null) {
            hoverFadeIn.stop();
            hoverFadeIn.playFromStart();
        } else {
            hoverPlayerCard.setOpacity(1);
        }
    }

    private void hideHoverCard() {
        if (hoverPlayerCard == null) return;
        if (!hoverPlayerCard.isVisible()) return;
        if (hoverFadeIn != null) hoverFadeIn.stop();
        if (hoverFadeOut != null) {
            hoverFadeOut.stop();
            hoverFadeOut.setFromValue(hoverPlayerCard.getOpacity());
            hoverFadeOut.playFromStart();
        } else {
            hoverPlayerCard.setVisible(false);
            hoverPlayerCard.setManaged(false);
            hoverPlayerCard.setOpacity(0);
        }
    }

    private void updateHoverCardContent(FieldPlayerNode node) {
        Player p = node.player();
        hoverCardNameLabel.setText(p.getName());
        hoverCardOverallLabel.setText("OVR " + p.getSkillLevel());
        hoverCardMetaLabel.setText((node.managed() ? "Managed Team" : "Rival Team")
                + "  •  " + p.getPosition() + "  •  Age " + p.getAge());
        hoverCardStatsLabel.setText(formatAttributeInline(p));
        drawHoverRadar(hoverRadarCanvas, p.getSpecificAttributes(), node.managed());
    }

    private String shortName(String full) {
        String[] parts = full.split("\\s+");
        return parts.length == 0 ? "P" : parts[parts.length - 1].substring(0, Math.min(2, parts[parts.length - 1].length())).toUpperCase();
    }

    private String formatAttributeInline(Player p) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Integer> e : p.getSpecificAttributes().entrySet()) {
            if (!first) sb.append("   •   ");
            sb.append(e.getKey().toUpperCase()).append(" ").append(e.getValue());
            first = false;
        }
        return sb.toString();
    }

    private void drawHoverRadar(Canvas canvas, Map<String, Integer> attrs, boolean managed) {
        if (canvas == null) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        gc.clearRect(0, 0, w, h);
        if (attrs == null || attrs.isEmpty()) return;
        List<Map.Entry<String, Integer>> statList = new ArrayList<>(attrs.entrySet());
        int n = Math.min(6, statList.size());
        double cx = w / 2.0;
        double cy = h / 2.0 + 4;
        double r = Math.min(w, h) * 0.34;

        gc.setStroke(javafx.scene.paint.Color.web("#3a4e70"));
        gc.setLineWidth(1.0);
        for (int ring = 1; ring <= 4; ring++) {
            double rr = r * ring / 4.0;
            gc.beginPath();
            for (int i = 0; i < n; i++) {
                double a = -Math.PI / 2 + i * 2 * Math.PI / n;
                double x = cx + Math.cos(a) * rr;
                double y = cy + Math.sin(a) * rr;
                if (i == 0) gc.moveTo(x, y);
                else gc.lineTo(x, y);
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
        javafx.scene.paint.Color fill = managed
                ? javafx.scene.paint.Color.web("#00dfa2", 0.38)
                : javafx.scene.paint.Color.web("#60a5fa", 0.38);
        javafx.scene.paint.Color stroke = managed
                ? javafx.scene.paint.Color.web("#4dffc3")
                : javafx.scene.paint.Color.web("#93c5fd");
        gc.setFill(fill);
        gc.setStroke(stroke);
        gc.setLineWidth(2.0);
        gc.fillPolygon(xs, ys, n);
        gc.strokePolygon(xs, ys, n);
    }

    private void updateRivalFormationLabel() {
        if (rivalFormationLabel == null) return;
        Team rival = getRivalTeam();
        String formation = (rival != null && rival.getCurrentTactic() != null)
                ? rival.getCurrentTacticName()
                : "-";
        rivalFormationLabel.setText("Rival Formation: " + formation);
    }

    private void setTeamCrest(Team team, ImageView imageView, Label fallbackLabel) {
        fallbackLabel.setText(teamInitials(team.getName()));
        Image crest = loadCrest(team);
        if (crest != null) {
            imageView.setImage(crest);
            imageView.setVisible(true);
            fallbackLabel.setVisible(false);
        } else {
            imageView.setVisible(false);
            fallbackLabel.setVisible(true);
        }
    }

    private Image loadCrest(Team team) {
        String logoPath = team.getLogoPath();
        if (logoPath != null && !logoPath.isBlank()) {
            InputStream stream = getClass().getResourceAsStream(logoPath.startsWith("/") ? logoPath : "/" + logoPath);
            if (stream != null) return new Image(stream);
        }
        String slug = team.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        for (String path : List.of("/com/sportmanager/crests/" + slug + ".png", "/com/sportmanager/crests/" + slug + ".jpg")) {
            InputStream stream = getClass().getResourceAsStream(path);
            if (stream != null) return new Image(stream);
        }
        return null;
    }

    private String teamInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    @FXML
    private void onContinue() {
        sm.advanceWeek();
    }
}
