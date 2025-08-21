package com.waskronos.tetris;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class HighScoresView extends VBox {
    // provisioning for dynamic list, preloaded with dummy data for now
    private static final ObservableList<String> SCORES = FXCollections.observableArrayList(
             "1) Alice - 15000",
        "2) Bob - 14000",
        "3) Charlie - 13500",
        "4) Diana - 12000",
        "5) Ethan - 11000",
        "6) Fiona - 9500",
        "7) George - 8800",
        "8) Hannah - 7600",
        "9) Isaac - 6900",
        "10) Julia - 6200"
    );

    public HighScoresView() {
        setAlignment(Pos.CENTER);
        setSpacing(30);
        setPadding(new Insets(16));

        Label title = new Label("High Scores");
        ListView<String> list = new ListView<>(SCORES);
        list.setPrefSize(260, 200);

        Button back = new Button("Back");
        back.setOnAction(e -> App.setRoot(new MainMenuView()));

        getChildren().addAll(title, list, back);
    }

    //provisioning for later dev
    public static void addScore(String name, int score) {
        SCORES.add(String.format("%s - %d", name, score));
    }
}
