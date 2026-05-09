package com.sportmanager.ui.controller;

import com.sportmanager.SportManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Main menu controller.
 * The UI layer only calls SportManager; no direct session or domain access.
 */
public class MainMenuController {
    @FXML private StackPane loadOverlay;
    @FXML private Label loadStatusLabel;
    @FXML private ListView<SportManager.SaveGameEntry> saveListView;


    private static final DateTimeFormatter SAVE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    @FXML
    private void onNewGame() {
        SportManager.getInstance().startNewGame();
    }

    @FXML
    private void onLoadGame() {
        SportManager sm = SportManager.getInstance();
        var saves = sm.listSaveGames();
        if (saves.isEmpty()) {
            showLoadOverlay();
            loadStatusLabel.setText("No saves yet. Start a game and save from dashboard.");
            saveListView.getItems().clear();
            return;
        }
        showLoadOverlay();
        saveListView.setItems(FXCollections.observableArrayList(saves));
        saveListView.setFixedCellSize(56);
        saveListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SportManager.SaveGameEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                String when = SAVE_TIME.format(Instant.ofEpochMilli(item.savedAtEpochMs()));
                setText(item.displayName() + "\n" + when + " · " + item.detailsLine());
            }
        });
        saveListView.getSelectionModel().selectFirst();
        loadStatusLabel.setText("Select a save and load or delete.");
        Platform.runLater(saveListView::requestFocus);
    }

    @FXML
    private void onLoadSelectedSave() {
        SportManager.SaveGameEntry chosen = saveListView.getSelectionModel().getSelectedItem();
        if (chosen == null) {
            loadStatusLabel.setText("Pick a save first.");
            return;
        }
        try {
            if (!SportManager.getInstance().loadGame(chosen.id())) {
                loadStatusLabel.setText("Save file could not be restored.");
            }
        } catch (Exception ex) {
            loadStatusLabel.setText("Load failed: " + ex.getMessage());
        }
    }

    @FXML
    private void onDeleteSelectedSave() {
        SportManager.SaveGameEntry sel = saveListView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            loadStatusLabel.setText("Pick a save to delete.");
            return;
        }
        try {
            SportManager.getInstance().deleteSaveGame(sel.id());
            saveListView.getItems().remove(sel);
            if (saveListView.getItems().isEmpty()) {
                loadStatusLabel.setText("No saves left.");
            } else {
                saveListView.getSelectionModel().selectFirst();
                loadStatusLabel.setText("Save deleted.");
            }
        } catch (Exception ex) {
            loadStatusLabel.setText("Delete failed: " + ex.getMessage());
        }
    }

    @FXML
    private void onCloseLoadOverlay() {
        if (loadOverlay != null) {
            loadOverlay.setVisible(false);
            loadOverlay.setManaged(false);
        }
    }

    private void showLoadOverlay() {
        if (loadOverlay != null) {
            loadOverlay.setVisible(true);
            loadOverlay.setManaged(true);
        }
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }
}
