package com.waskronos.tetris;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameView extends BorderPane {
    private static final int COLS = 10;
    private static final int ROWS = 20;
    private static final int CELL = 28;  // px

    private final Label score = new Label("Score: 0");
    private final GridPane board = new GridPane();

    public GameView() {
        // Top bar
        Button back = new Button("Back");
        back.setOnAction(e -> App.setRoot(new MainMenuView()));

        HBox topBar = new HBox(12, back, score);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 12, 8, 12));
        setTop(topBar);

        // Board grid (placeholder cells)
        board.setHgap(1);
        board.setVgap(1);
        board.setPadding(new Insets(12));
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Rectangle cell = new Rectangle(CELL, CELL, Color.BLACK);
                cell.setStroke(Color.web("#222")); // faint grid line
                board.add(cell, c, r);
            }
        }
        setCenter(board);
    }
}