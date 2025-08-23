package com.waskronos.tetris;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.Random;

public class GameView extends BorderPane {
    private static final int COLS = 10;
    private static final int ROWS = 20;
    private static final int CELL = 28; // px

    private final Label score = new Label("Score: 0");
    private final GridPane board = new GridPane();

    // --- Model (logic-only)
    private final int[][] boardState = new int[ROWS][COLS];   // 0 empty, else ARGB color
    private Tetromino active;                                  // currently falling piece

    // --- View cache
    private final Rectangle[][] cellViews = new Rectangle[ROWS][COLS];

    // --- Timer
    private Timeline fallTimer;
    private static final int GRAVITY_MS = 500;
    private final Random rng = new Random();

    public GameView() {
        // Top bar
        Button back = new Button("Back");
        back.setOnAction(e -> App.setRoot(new MainMenuView()));
        back.setFocusTraversable(false);

        HBox topBar = new HBox(12, back, score);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 12, 8, 12));
        setTop(topBar);

        // Board grid (View)
        board.setHgap(1);
        board.setVgap(1);
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Rectangle cell = new Rectangle(CELL, CELL, Color.BLACK);
                cell.setStroke(Color.web("#222"));
                board.add(cell, c, r);
                cellViews[r][c] = cell;
            }
        }
        board.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        board.setAlignment(Pos.CENTER);

        // Center the board
        StackPane wrapper = new StackPane(board);
        wrapper.setPadding(new Insets(12));
        setCenter(wrapper);

        // Start game
        spawnNewPiece();
        startGravity(GRAVITY_MS);

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, this::handleKey);
            }
        });

        // ensure the view can get focus if you add keys later
        setFocusTraversable(true);
        requestFocus();
       
    }

    // -------------------- Game loop --------------------

    private void startGravity(int msPerStep) {
        if (fallTimer != null) fallTimer.stop();
        fallTimer = new Timeline(new KeyFrame(Duration.millis(msPerStep), e -> tick()));
        fallTimer.setCycleCount(Timeline.INDEFINITE);
        fallTimer.play();
    }

    private void tick() {
        if (active == null) return;

        // try to move down; if can't, lock and respawn
        if (canPlace(active, active.row + 1, active.col, active.rotationIndex)) {
            active.row++;
        } else {
            lock(active);
            spawnNewPiece();
        }
        render();
    }
// -------------------- Key Press --------------------
    public void handleKey(KeyEvent e) {
    switch (e.getCode()) {
        case LEFT:  tryMove(0, -1); break;
        case RIGHT: tryMove(0,  1); break;
        case DOWN:  tryMove(1,  0); break;
        case UP:    tryRotateCW();  break;
        case X:     tryRotateCW();  break;
        case Z:     tryRotateCCW(); break;
        default:    return;
    }
    render();
}
// -------------------- Movement & rotation  --------------------

    private boolean tryMove(int dRow, int dCol) {
        if (active == null) return false;
        int newRow = active.row + dRow;
        int newCol = active.col + dCol;
        if (canPlace(active, newRow, newCol, active.rotationIndex)) {
            active.row = newRow;
            active.col = newCol;
            return true;
        }
        return false;
    }

    private boolean tryRotateCW() {
        if (active == null) return false;
        int nextRot = (active.rotationIndex + 1) % active.type.rotations.length;
        if (canPlace(active, active.row, active.col, nextRot)) {
            active.rotationIndex = nextRot;
            return true;
        }
        return false;
    }

    private boolean tryRotateCCW() {
        if (active == null) return false;
        int nextRot = (active.rotationIndex - 1 + active.type.rotations.length) % active.type.rotations.length;
        if (canPlace(active, active.row, active.col, nextRot)) {
            active.rotationIndex = nextRot;
            return true;
        }
        return false;
    }

    // -------------------- Spawning/locking --------------------

    private void spawnNewPiece() {
        TetrominoType type = randomType();
        active = new Tetromino(type);
        active.row = 0;
        // center-ish spawn; adjust for shape width
        int spawnCol = (COLS / 2) - (active.shape()[0].length / 2);
        active.col = Math.max(0, Math.min(COLS - active.shape()[0].length, spawnCol));

        if (!canPlace(active, active.row, active.col, active.rotationIndex)) {
            // game over for now: stop timer
            if (fallTimer != null) fallTimer.stop();
            System.out.println("Game Over");
            active = null;
        }
        render();
    }

    private TetrominoType randomType() {
        TetrominoType[] bag = TetrominoType.values();
        return bag[rng.nextInt(bag.length)];
    }

    private void lock(Tetromino t) {
        int[][] shape = t.shape();
        int color = colorFor(t.type);
        for (int sr = 0; sr < shape.length; sr++) {
            for (int sc = 0; sc < shape[0].length; sc++) {
                if (shape[sr][sc] == 1) {
                    int br = t.row + sr;
                    int bc = t.col + sc;
                    if (inBounds(br, bc)) {
                        boardState[br][bc] = color;
                    }
                }
            }
        }
    }

    // -------------------- Collision --------------------

    private boolean canPlace(Tetromino t, int newRow, int newCol, int newRotIdx) {
        int[][] shape = t.type.rotations[newRotIdx];
        for (int sr = 0; sr < shape.length; sr++) {
            for (int sc = 0; sc < shape[0].length; sc++) {
                if (shape[sr][sc] == 0) continue;
                int br = newRow + sr;
                int bc = newCol + sc;
                if (!inBounds(br, bc)) return false;
                if (boardState[br][bc] != 0) return false;
            }
        }
        return true;
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    // -------------------- Rendering --------------------

    private void render() {
        // paint locked board
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int val = boardState[r][c];
                Rectangle rect = cellViews[r][c];
                if (val == 0) {
                    rect.setFill(Color.BLACK);
                } else {
                    rect.setFill(Color.rgb((val >> 16) & 0xFF, (val >> 8) & 0xFF, val & 0xFF));
                }
            }
        }

        // overlay active piece
        if (active != null) {
            int[][] shape = active.shape();
            Color color = toColor(colorFor(active.type));
            for (int sr = 0; sr < shape.length; sr++) {
                for (int sc = 0; sc < shape[0].length; sc++) {
                    if (shape[sr][sc] == 1) {
                        int br = active.row + sr;
                        int bc = active.col + sc;
                        if (inBounds(br, bc)) {
                            cellViews[br][bc].setFill(color);
                        }
                    }
                }
            }
        }
    }

    private static int colorFor(TetrominoType t) {
        // simple ARGB colors (alpha FF)
        switch (t) {
        case I: return 0xFF00BCD4; // teal
        case O: return 0xFFFFC107; // amber
        case T: return 0xFF9C27B0; // purple
        case J: return 0xFF3F51B5; // blue
        case L: return 0xFFFF9800; // orange
        case S: return 0xFF4CAF50; // green
        case Z: return 0xFFF44336; // red
        default: return 0xFFFFFFFF;
    }
    }

    private static Color toColor(int argb) {
        return Color.rgb((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF);
    }

    // -------------------- Piece definitions --------------------

    private enum TetrominoType {
        // 1 = filled, 0 = empty. Rotations are small matrices.
        I(new int[][][] {
                { {1,1,1,1} },                 // rotation 0 (1 x 4)
                { {1},{1},{1},{1} }            // rotation 1 (4 x 1)
        }),
        O(new int[][][] {
                { {1,1},
                  {1,1} }                      // O effectively one rotation
        }),
        T(new int[][][] {
                { {1,1,1},
                  {0,1,0} },
                { {0,1},
                  {1,1},
                  {0,1} },
                { {0,1,0},
                  {1,1,1} },
                { {1,0},
                  {1,1},
                  {1,0} }
        }),
        J(new int[][][] {
            {{1,0,0},
             {1,1,1}},
            {{1,1},
             {1,0},
             {1,0}},
            {{1,1,1},
             {0,0,1}},
            {{0,1},
             {0,1},
             {1,1}},
        }),
        L(new int[][][] {
            {{0,0,1},
             {1,1,1}},
            {{1,0},
             {1,0},
             {1,1}},
            {{1,1,1},
             {1,0,0}},
            {{1,1},
             {0,1},
             {0,1}},
        }),
        S(new int[][][] {
            { {0,1,1},
              {1,1,0} },
            { {1,0},
              {1,1},
              {0,1} }
        }),
        Z(new int[][][] {
            { {1,1,0},
              {0,1,1} },
            { {0,1},
              {1,1},
              {1,0} }
        });

        final int[][][] rotations;
        TetrominoType(int[][][] rotations) { this.rotations = rotations; }
    }

    private static final class Tetromino {
        final TetrominoType type;
        int rotationIndex = 0;
        int row = 0;
        int col = 0;

        Tetromino(TetrominoType type) { this.type = type; }
        int[][] shape() { return type.rotations[rotationIndex]; }
        void rotateCW() { rotationIndex = (rotationIndex + 1) % type.rotations.length; }
    }
}

