package com.sportmanager.ui.component;

import com.sportmanager.basketball.BasketballTactics;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.Map;

/**
 * Draws either a football pitch (11-a-side formations) or a basketball half-court
 * with five-man offensive spacing, depending on the tactic name.
 */
public class TacticPitchCanvas extends Canvas {

    private record Dot(double x, double y, String label) {}

    private static final Map<String, List<Dot>> FOOTBALL_FORMATIONS = Map.of(

        "4-3-3", List.of(
            new Dot(0.50, 0.88, "GK"),
            new Dot(0.14, 0.70, "LB"),  new Dot(0.37, 0.73, "CB"),
            new Dot(0.63, 0.73, "CB"),  new Dot(0.86, 0.70, "RB"),
            new Dot(0.24, 0.48, "LM"),  new Dot(0.50, 0.45, "CM"),
            new Dot(0.76, 0.48, "RM"),
            new Dot(0.22, 0.19, "LW"),  new Dot(0.50, 0.13, "CF"),
            new Dot(0.78, 0.19, "RW")
        ),

        "4-4-2", List.of(
            new Dot(0.50, 0.88, "GK"),
            new Dot(0.11, 0.70, "LB"),  new Dot(0.36, 0.73, "CB"),
            new Dot(0.64, 0.73, "CB"),  new Dot(0.89, 0.70, "RB"),
            new Dot(0.11, 0.47, "LM"),  new Dot(0.36, 0.46, "CM"),
            new Dot(0.64, 0.46, "CM"),  new Dot(0.89, 0.47, "RM"),
            new Dot(0.35, 0.17, "ST"),  new Dot(0.65, 0.17, "ST")
        ),

        "4-2-3-1", List.of(
            new Dot(0.50, 0.88, "GK"),
            new Dot(0.11, 0.70, "LB"),  new Dot(0.36, 0.73, "CB"),
            new Dot(0.64, 0.73, "CB"),  new Dot(0.89, 0.70, "RB"),
            new Dot(0.35, 0.54, "DM"),  new Dot(0.65, 0.54, "DM"),
            new Dot(0.16, 0.33, "LAM"), new Dot(0.50, 0.31, "CAM"),
            new Dot(0.84, 0.33, "RAM"),
            new Dot(0.50, 0.12, "ST")
        ),

        "3-5-2", List.of(
            new Dot(0.50, 0.88, "GK"),
            new Dot(0.25, 0.71, "CB"),  new Dot(0.50, 0.74, "CB"),
            new Dot(0.75, 0.71, "CB"),
            new Dot(0.07, 0.50, "LWB"), new Dot(0.29, 0.48, "CM"),
            new Dot(0.50, 0.46, "CM"),  new Dot(0.71, 0.48, "CM"),
            new Dot(0.93, 0.50, "RWB"),
            new Dot(0.35, 0.17, "ST"),  new Dot(0.65, 0.17, "ST")
        ),

        "5-3-2", List.of(
            new Dot(0.50, 0.88, "GK"),
            new Dot(0.09, 0.67, "LWB"), new Dot(0.27, 0.73, "CB"),
            new Dot(0.50, 0.76, "CB"),  new Dot(0.73, 0.73, "CB"),
            new Dot(0.91, 0.67, "RWB"),
            new Dot(0.25, 0.46, "CM"),  new Dot(0.50, 0.44, "CM"),
            new Dot(0.75, 0.46, "CM"),
            new Dot(0.35, 0.17, "ST"),  new Dot(0.65, 0.17, "ST")
        )
    );

    private String currentFormation = "4-4-2";
    private boolean basketballMode;

    public TacticPitchCanvas() {
        super(240, 290);
        widthProperty().addListener(e -> redraw());
        heightProperty().addListener(e -> redraw());
    }

    public TacticPitchCanvas(double w, double h) {
        super(w, h);
        widthProperty().addListener(e -> redraw());
        heightProperty().addListener(e -> redraw());
    }

    public void drawFormation(String formation) {
        String bbKey = BasketballTactics.diagramKeyForTactic(formation);
        if (bbKey != null) {
            basketballMode = true;
            currentFormation = bbKey;
        } else {
            basketballMode = false;
            currentFormation = (formation != null && FOOTBALL_FORMATIONS.containsKey(formation))
                    ? formation : "4-4-2";
        }
        redraw();
    }

    public String getCurrentFormation() { return currentFormation; }

    @Override public boolean isResizable()               { return true; }
    @Override public double  prefWidth(double height)    { return 240; }
    @Override public double  prefHeight(double width)    { return 290; }

    @Override
    public void resize(double width, double height) {
        setWidth(width);
        setHeight(height);
        redraw();
    }

    private void redraw() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        if (basketballMode) {
            drawBasketballCourt(gc, w, h);
            drawBasketballPlayers(gc, w, h);
        } else {
            drawFootballPitch(gc, w, h);
            drawFootballPlayers(gc, w, h);
        }
        drawFormationLabel(gc, w, h);
    }

    private void drawFootballPitch(GraphicsContext gc, double w, double h) {
        double pad = 8;

        gc.setFill(Color.web("#1a4a1a"));
        gc.fillRoundRect(0, 0, w, h, 10, 10);

        gc.setFill(Color.web("#1e5220", 0.55));
        double stripeH = (h - 2 * pad) / 7.0;
        for (int i = 0; i < 7; i += 2) {
            gc.fillRect(pad, pad + i * stripeH, w - 2 * pad, stripeH);
        }

        gc.setStroke(Color.web("#ffffff", 0.45));
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(pad, pad, w - 2 * pad, h - 2 * pad, 4, 4);

        double midY = h / 2.0;
        gc.strokeLine(pad, midY, w - pad, midY);

        double cr = Math.min(w, h) * 0.11;
        gc.strokeOval(w / 2 - cr, midY - cr, cr * 2, cr * 2);
        gc.setFill(Color.web("#ffffff", 0.55));
        gc.fillOval(w / 2 - 2, midY - 2, 4, 4);

        double paW = w * 0.54;
        double paH = h * 0.16;
        gc.strokeRect((w - paW) / 2, pad, paW, paH);
        gc.strokeRect((w - paW) / 2, h - pad - paH, paW, paH);

        double gaW = w * 0.28;
        double gaH = h * 0.065;
        gc.strokeRect((w - gaW) / 2, pad, gaW, gaH);
        gc.strokeRect((w - gaW) / 2, h - pad - gaH, gaW, gaH);

        double psY1 = pad + paH * 0.62;
        double psY2 = h - pad - paH * 0.62;
        gc.fillOval(w / 2 - 2, psY1, 4, 4);
        gc.fillOval(w / 2 - 2, psY2 - 4, 4, 4);

        gc.setLineWidth(0.8);
        double ca = 7;
        gc.strokeArc(pad - ca, pad - ca, ca * 2, ca * 2, 270, 90, javafx.scene.shape.ArcType.OPEN);
        gc.strokeArc(w - pad - ca, pad - ca, ca * 2, ca * 2, 180, 90, javafx.scene.shape.ArcType.OPEN);
        gc.strokeArc(pad - ca, h - pad - ca, ca * 2, ca * 2, 0, 90, javafx.scene.shape.ArcType.OPEN);
        gc.strokeArc(w - pad - ca, h - pad - ca, ca * 2, ca * 2, 90, 90, javafx.scene.shape.ArcType.OPEN);
    }

    /** Half court: basket at top (y = pad). Spacing matches {@link BasketballTactics#OFFENSE_DIAGRAM}. */
    private void drawBasketballCourt(GraphicsContext gc, double w, double h) {
        double pad = 8;
        gc.setFill(Color.web("#c9956b"));
        gc.fillRoundRect(0, 0, w, h, 10, 10);

        gc.setStroke(Color.web("#ffffff", 0.85));
        gc.setLineWidth(1.4);
        gc.strokeRoundRect(pad, pad, w - 2 * pad, h - 2 * pad, 6, 6);

        double cx = w / 2.0;
        double top = pad;
        double rimY = top + 14;

        gc.setFill(Color.web("#ff6b35"));
        gc.fillOval(cx - 5, rimY - 2, 10, 6);

        double keyW = (w - 2 * pad) * 0.36;
        double keyH = h * 0.22;
        gc.strokeRect(cx - keyW / 2, top, keyW, keyH);

        double ftY = top + keyH;
        gc.strokeLine(cx - keyW / 2, ftY, cx + keyW / 2, ftY);

        double arcR = Math.min(w, h) * 0.38;
        gc.strokeArc(cx - arcR, top - arcR * 0.15, arcR * 2, arcR * 2, 195, 150, javafx.scene.shape.ArcType.OPEN);

        double midY = h - pad - (h - 2 * pad) * 0.08;
        gc.strokeLine(pad, midY, w - pad, midY);
        gc.setFill(Color.web("#ffffff", 0.5));
        gc.strokeOval(cx - 18, midY - 18, 36, 36);
    }

    private void drawFootballPlayers(GraphicsContext gc, double w, double h) {
        List<Dot> dots = FOOTBALL_FORMATIONS.getOrDefault(currentFormation, FOOTBALL_FORMATIONS.get("4-4-2"));
        drawDots(gc, w, h, dots, true);
    }

    private void drawBasketballPlayers(GraphicsContext gc, double w, double h) {
        List<BasketballTactics.Dot> raw = BasketballTactics.OFFENSE_DIAGRAM.get(currentFormation);
        if (raw == null) {
            raw = BasketballTactics.OFFENSE_DIAGRAM.get(BasketballTactics.DEFAULT_OFFENSE);
        }
        List<Dot> dots = raw.stream().map(d -> new Dot(d.x(), d.y(), d.label())).toList();
        drawDots(gc, w, h, dots, false);
    }

    private void drawDots(GraphicsContext gc, double w, double h, List<Dot> dots, boolean football) {
        double dotR = Math.max(9, Math.min(13, w * 0.05));

        for (Dot d : dots) {
            double px = d.x() * w;
            double py = d.y() * h;
            boolean highlight = football ? d.label().equals("GK") : d.label().equals("C");

            gc.setFill(Color.web("#000000", 0.35));
            gc.fillOval(px - dotR + 1.5, py - dotR + 2.5, dotR * 2, dotR * 2);

            gc.setFill(highlight ? Color.web("#f59e0b") : Color.web("#00dfa2"));
            gc.fillOval(px - dotR, py - dotR, dotR * 2, dotR * 2);

            gc.setStroke(highlight ? Color.web("#fde68a") : Color.web("#4dffc3"));
            gc.setLineWidth(1.5);
            gc.strokeOval(px - dotR, py - dotR, dotR * 2, dotR * 2);

            gc.setFill(Color.web("#0c1018"));
            double fontSize = d.label().length() > 2 ? dotR * 0.55 : dotR * 0.72;
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, fontSize));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(d.label(), px, py + fontSize * 0.38);
        }
    }

    private void drawFormationLabel(GraphicsContext gc, double w, double h) {
        gc.setFill(Color.web("#00dfa2", 0.75));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(currentFormation, w / 2, h - 4);
    }
}
