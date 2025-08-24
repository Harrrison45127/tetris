package com.waskronos.tetris;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Optional;

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

        // Text styling
        String buttonStyle = "-fx-font-size: 18px; -fx-padding: 10px 0;";
        startGame.setStyle(buttonStyle);
        highScores.setStyle(buttonStyle);
        settings.setStyle(buttonStyle);
        quit.setStyle(buttonStyle);

        // Button actions
        startGame.setOnAction(e -> App.setRoot(new GameView()));
        highScores.setOnAction(e -> App.setRoot(new HighScoresView()));
        settings.setOnAction(e -> App.setRoot(new SettingsView()));
        quit.setOnAction(e -> confirmQuit());

        getChildren().addAll(title, startGame, highScores, settings, quit);
    }

    private void confirmQuit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Program");
        alert.setHeaderText("Are you sure you want to quit?");
        alert.setContentText("Yes = exit program\nNo = return to main screen");

        ButtonType YES = new ButtonType("Yes");
        ButtonType NO  = new ButtonType("No");
        alert.getButtonTypes().setAll(YES, NO);

        // Tie the dialog to this window if available (optional but nice)
        if (getScene() != null && getScene().getWindow() != null) {
            alert.initOwner(getScene().getWindow());
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == YES) {
            Platform.exit(); // exit program
        }
        // Else: No -> just close dialog and remain on MainMenuView
    }
}
