package com.waskronos.tetris;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SettingsView extends VBox {
    public SettingsView() {
        setSpacing(15);
        setAlignment(Pos.CENTER);

        Label title = new Label("Configuration Settings");
        Button back = new Button("Back to Main");

        back.setOnAction(e -> App.setRoot(new MainMenuView()));

        getChildren().addAll(title, back);
    }
}
