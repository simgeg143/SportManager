package com.sportmanager.ui;

import com.sportmanager.ui.component.TacticPitchCanvas;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;

/**
 * Keeps tactic canvases and radar canvases inside narrow viewports without horizontal clipping,
 * while scrollbars stay hidden ({@link ScrollPane.ScrollBarPolicy#NEVER}).
 */
public final class ScrollViewportBindings {

    private ScrollViewportBindings() {}

    /** Runs {@code action} on viewport / pane size changes (and twice after layout). */
    public static void attachScrollViewportListeners(ScrollPane scrollPane, Runnable action) {
        if (scrollPane == null || action == null) return;
        javafx.beans.value.ChangeListener<Object> listener = (o, a, b) -> action.run();
        scrollPane.viewportBoundsProperty().addListener(listener);
        scrollPane.widthProperty().addListener(listener);
        scrollPane.heightProperty().addListener(listener);
        Platform.runLater(() -> {
            action.run();
            Platform.runLater(action);
        });
    }

    public static void attachRegionLayoutListeners(Region region, Runnable action) {
        if (region == null || action == null) return;
        javafx.beans.value.ChangeListener<Number> listener = (o, a, b) -> action.run();
        region.widthProperty().addListener(listener);
        region.heightProperty().addListener(listener);
        Platform.runLater(() -> {
            action.run();
            Platform.runLater(action);
        });
    }

    /** Usable inner width for scroll content; {@code -1} if not laid out yet. */
    public static double scrollViewportInnerWidth(ScrollPane scrollPane, double horizontalInset) {
        if (scrollPane == null) return -1;
        double vw = scrollPane.getViewportBounds().getWidth();
        if (vw < 16) return -1;
        return Math.max(120, vw - horizontalInset);
    }

    public static void layoutTacticPitchCanvas(
            TacticPitchCanvas canvas,
            double innerWidth,
            double referenceWidth,
            double referenceHeight,
            double maxHeight) {
        if (canvas == null || innerWidth <= 0) return;
        double h = innerWidth * (referenceHeight / referenceWidth);
        if (maxHeight > 0) {
            h = Math.min(h, maxHeight);
        }
        canvas.setWidth(innerWidth);
        canvas.setHeight(h);
    }

    public static void layoutRadarCanvas(Canvas canvas, double innerWidth, double heightOverWidth) {
        if (canvas == null || innerWidth <= 0) return;
        canvas.setWidth(innerWidth);
        canvas.setHeight(Math.max(60, innerWidth * heightOverWidth));
    }

    /** Width available inside a {@link Region} after subtracting horizontal inset (padding approximation). */
    public static double regionInnerWidth(Region region, double horizontalInset) {
        if (region == null) return -1;
        double w = region.getWidth() - horizontalInset;
        return w < 40 ? -1 : w;
    }
}
