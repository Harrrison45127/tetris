package com.waskronos.tetris;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MainMenuView extends VBox {
    public MainMenuView() {
        setSpacing(15);
        setAlignment(Pos.CENTER);

        Label title = new Label("Main Screen");
        Button goOther = new Button("Go to Other Screen");

        goOther.setOnAction(e -> App.setRoot(new SettingsView()));

        getChildren().addAll(title, goOther);
    }
}