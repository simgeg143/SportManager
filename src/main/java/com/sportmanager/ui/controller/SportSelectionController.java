package com.sportmanager.ui.controller;

import com.sportmanager.SportManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Lets the user pick a sport at the start of a new game. (LM-2)
 * Calls SportManager.selectSport(sportCode) when a sport is chosen.
 */
public class SportSelectionController implements Initializable {

    @FXML private FlowPane sportCardsPane;

    private static final List<String[]> SPORTS = List.of(
            new String[]{"football", "Football",  "11-a-side league competition"},
            new String[]{"basketball","Basketball","Coming soon",},
            new String[]{"tennis",   "Tennis",    "Coming soon"}
    );

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (String[] sport : SPORTS) {
            String code        = sport[0];
            String displayName = sport[1];
            String desc        = sport[2];

            VBox card = new VBox(8);
            card.setAlignment(Pos.CENTER);
            card.getStyleClass().add("sport-card");
            card.setPrefWidth(220);
            card.setPrefHeight(160);

            Label icon   = new Label(code.equals("football") ? "⚽" : "🏀");
            icon.getStyleClass().add("sport-card-icon");
            Label title  = new Label(displayName);
            title.getStyleClass().add("sport-card-name");
            Label detail = new Label(desc);
            detail.getStyleClass().add("sport-card-desc");
            card.getChildren().addAll(icon, title, detail);

            boolean enabled = code.equals("football");
            card.setOnMouseClicked(e -> {
                if (enabled) SportManager.getInstance().selectSport(code);
            });
            if (!enabled) card.getStyleClass().add("sport-card-disabled");

            sportCardsPane.getChildren().add(card);
        }
    }

    @FXML
    private void onBack() {
        SportManager.getInstance().showMainMenu();
    }
}
