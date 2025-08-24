package com.waskronos.tetris;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class SplashView extends StackPane {

    public SplashView(Runnable onFinished) {

        setStyle("-fx-background-color: linear-gradient(to bottom, #1e1e1e, #2b2b2b);");
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);

        Rectangle logo = new Rectangle(120, 120);
        logo.setArcWidth(24);
        logo.setArcHeight(24);
        logo.setFill(Color.DARKSLATEBLUE);
        logo.setEffect(new DropShadow(20, Color.BLACK));

        Label app = new Label("TETRIS");
        app.setFont(Font.font(32));
        app.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label group  = new Label("Group: 22 ");
        Label course = new Label("Course: 2006ICT");
        Label info   = new Label("Trimester 2, 2025 Milestone 1");

        group.setStyle("-fx-text-fill: #dddddd;");
        course.setStyle("-fx-text-fill: #dddddd;");
        info.setStyle("-fx-text-fill: #dddddd;");

        // spinner to prentend loading
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(40, 40);

        box.getChildren().addAll(logo, app, group, course, info, spinner);
        getChildren().add(box);

        // splash countdown logic
        PauseTransition wait = new PauseTransition(Duration.seconds(3)); 
        wait.setOnFinished(e -> { if (onFinished != null) onFinished.run(); });
        wait.play();
    }
}
