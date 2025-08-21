package com.waskronos.tetris;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MainMenuView extends VBox {
    public MainMenuView() {
        setSpacing(15);
        setAlignment(Pos.CENTER);

        Label title = new Label("TETRIS");
        Button startGame   = new Button("Start Game");
        Button highScores  = new Button("High Scores");
        Button settings    = new Button("Settings");
        Button quit        = new Button("Quit");

        startGame.setOnAction(e -> App.setRoot(new GameView()));
        highScores.setOnAction(e -> App.setRoot(new HighScoresView()));
        settings.setOnAction(e -> App.setRoot(new SettingsView()));
        quit.setOnAction(e -> System.exit(0));

        getChildren().addAll(title, startGame, highScores, settings, quit);
    }
}