package com.sportmanager;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sport Manager");
        primaryStage.setWidth(1280);
        primaryStage.setHeight(800);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(680);

        SceneManager.getInstance().init(primaryStage);
        SceneManager.getInstance().showMainMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
