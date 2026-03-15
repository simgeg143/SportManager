package com.sportmanager.ui.controller;

import com.sportmanager.SportManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Main menu controller.
 * The UI layer only calls SportManager; no direct session or domain access.
 */
public class MainMenuController {

    @FXML
    private void onNewGame() {
        SportManager.getInstance().startNewGame();
    }

    @FXML
    private void onLoadGame() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Save / Load functionality will be available in a future update.",
                ButtonType.OK);
        alert.setTitle("Load Game");
        alert.setHeaderText("Not Yet Available");
        alert.showAndWait();
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }
}
