package com.waskronos.tetris;

import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MainMenuView extends VBox {
    public MainMenuView() {
        setSpacing(30);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(20));

        Label title = new Label("TETRIS");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

        Button startGame   = new Button("Start Game");
        Button highScores  = new Button("High Scores");
        Button settings    = new Button("Settings");
        Button quit        = new Button("Quit");

        // Uniform button width
        double buttonWidth = 180;
        startGame.setPrefWidth(buttonWidth);
        highScores.setPrefWidth(buttonWidth);
        settings.setPrefWidth(buttonWidth);
        quit.setPrefWidth(buttonWidth);

        //text on buttons
        String buttonStyle = "-fx-font-size: 18px; -fx-padding: 10px 0;";
        startGame.setStyle(buttonStyle);
        highScores.setStyle(buttonStyle);
        settings.setStyle(buttonStyle);
        quit.setStyle(buttonStyle);

        // Button actions
        startGame.setOnAction(e -> App.setRoot(new GameView()));
        highScores.setOnAction(e -> App.setRoot(new HighScoresView()));
        settings.setOnAction(e -> App.setRoot(new SettingsView()));
        quit.setOnAction(e -> System.exit(0));

        getChildren().addAll(title, startGame, highScores, settings, quit);
    }
}
