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
    private int scorePoints = 0;

    private final Label score = new Label("Score: 0");
    private void updateScoreLabel() { score.setText("Score: " + scorePoints); }

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

    // --- Pause state & overlay ---
    private boolean paused = false;
    private final Label pauseOverlay = new Label("PAUSED\nPress P to resume");
    private final StackPane wrapper = new StackPane();

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

        // Center the board + overlay
        wrapper.getChildren().add(board);
        wrapper.setPadding(new Insets(12));
        setCenter(wrapper);

        pauseOverlay.setVisible(false);
        pauseOverlay.setMouseTransparent(true);
        pauseOverlay.setStyle(
            "-fx-background-color: rgba(0,0,0,0.55);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 30px;" +
            "-fx-background-radius: 12px;" +
            "-fx-text-alignment: center;"
        );
        StackPane.setAlignment(pauseOverlay, Pos.CENTER);
        wrapper.getChildren().add(pauseOverlay);

        // Start game
        spawnNewPiece();
        startGravity(GRAVITY_MS);

        // Key handling (attach when scene appears)
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, this::handleKey);
            }
        });
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
        if (paused) return;          // do nothing while paused
        if (active == null) return;

        // try to move down; if can't, lock and respawn
        if (canPlace(active, active.row + 1, active.col, active.rotationIndex)) {
            active.row++;
        } else {
            lock(active);
            int cleared = clearFullLines(); 
            if (cleared > 0) {
                addScoreForClears(cleared);
            }
            spawnNewPiece();
        }
        render();
    }

    // -------------------- Key Press --------------------
    public void handleKey(KeyEvent e) {
        if (e.getCode() == KeyCode.P) {
            togglePause();
            e.consume();
            return;
        }

        if (paused) { // ignore gameplay input while paused
            e.consume();
            return;
        }

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

    private void togglePause() {
        paused = !paused;
        if (paused) {
            if (fallTimer != null) fallTimer.pause();
            pauseOverlay.setVisible(true);
        } else {
            if (fallTimer != null) fallTimer.play();
            pauseOverlay.setVisible(false);
            requestFocus();
        }
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

    // -------------------- clear full lines --------------------
    private int clearFullLines() {
        int write = ROWS - 1;   // where to write the next kept row
        int cleared = 0;

        // walk from bottom to top, copying rows that are not full
        for (int r = ROWS - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < COLS; c++) {
                if (boardState[r][c] == 0) { full = false; break; }
            }
            if (!full) {
                // keep row and copy to write index
                for (int c = 0; c < COLS; c++) {
                    boardState[write][c] = boardState[r][c];
                }
                write--;
            } else {
                // Full row is not copied
                cleared++;
            }
        }

        // zero out top rows after compacting
        for (int r = write; r >= 0; r--) {
            for (int c = 0; c < COLS; c++) {
                boardState[r][c] = 0;
            }
        }

        return cleared;
    }

    // -------------------- Scoring --------------------
    private void addScoreForClears(int lines) {
        switch (lines) {
            case 1: scorePoints += 100; break;
            case 2: scorePoints += 300; break;
            case 3: scorePoints += 500; break;
            case 4: scorePoints += 800; break;
            default: break;
        }
        updateScoreLabel();
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
    // ---- paint locked board (enhanced loop over each row) ----
    for (int r = 0; r < ROWS; r++) {
        int[] rowVals = boardState[r];
        Rectangle[] rowRects = cellViews[r];

        int c = 0; // column index to pair with the for-each value
        for (int val : rowVals) {
            Rectangle rect = rowRects[c++];
            if (val == 0) {
                rect.setFill(Color.BLACK);
            } else {
                rect.setFill(Color.rgb((val >> 16) & 0xFF, (val >> 8) & 0xFF, val & 0xFF));
            }
        }
    }

    // ---- overlay active piece (enhanced loop over each row of the shape) ----
    if (active != null) {
        int[][] shape = active.shape();
        Color color = toColor(colorFor(active.type));

        for (int sr = 0; sr < shape.length; sr++) {
            int[] shapeRow = shape[sr];
            int sc = 0; // shape column index paired with the for-each
            for (int cell : shapeRow) {
                if (cell == 1) {
                    int br = active.row + sr;
                    int bc = active.col + sc;
                    if (inBounds(br, bc)) {
                        cellViews[br][bc].setFill(color);
                    }
                }
                sc++;
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
