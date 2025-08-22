package com.waskronos.tetris;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class SettingsView extends BorderPane {

    public SettingsView() {
        // ----- Header -----
        Label title = new Label("Settings");
        title.setFont(Font.font(22));
        BorderPane.setAlignment(title, Pos.CENTER);
        setTop(title);
        BorderPane.setMargin(title, new Insets(16, 0, 8, 0));

        // ----- Body Grid -----
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(20));

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(35);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(65);
        grid.getColumnConstraints().addAll(c1, c2);

        // Field Width
        Label widthLbl = new Label("Field width");
        Slider widthSlider = new Slider(6, 20, 10); // min 6, max 20, default 10
        widthSlider.setMajorTickUnit(2);
        widthSlider.setShowTickMarks(true);
        widthSlider.setSnapToTicks(true);
        Label widthVal = new Label("10");
        widthSlider.valueProperty().addListener((o, a, b) -> widthVal.setText(String.valueOf(b.intValue())));
        HBox widthBox = new HBox(10, widthSlider, widthVal);
        widthBox.setAlignment(Pos.CENTER_LEFT);
        grid.addRow(0, widthLbl, widthBox);

        // Field Height
        Label heightLbl = new Label("Field height");
        Slider heightSlider = new Slider(10, 40, 20); // min 10, max 40, default 20
        heightSlider.setMajorTickUnit(5);
        heightSlider.setShowTickMarks(true);
        heightSlider.setSnapToTicks(true);
        Label heightVal = new Label("20");
        heightSlider.valueProperty().addListener((o, a, b) -> heightVal.setText(String.valueOf(b.intValue())));
        HBox heightBox = new HBox(10, heightSlider, heightVal);
        heightBox.setAlignment(Pos.CENTER_LEFT);
        grid.addRow(1, heightLbl, heightBox);

        // Starting level
        Label levelLbl = new Label("Starting level");
        Slider levelSlider = new Slider(1, 20, 1);
        levelSlider.setMajorTickUnit(1);
        levelSlider.setMinorTickCount(0);
        levelSlider.setSnapToTicks(true);
        levelSlider.setShowTickMarks(true);
        Label levelVal = new Label("1");
        levelSlider.valueProperty().addListener((o, a, b) -> levelVal.setText(String.valueOf(b.intValue())));
        HBox levelBox = new HBox(10, levelSlider, levelVal);
        levelBox.setAlignment(Pos.CENTER_LEFT);
        grid.addRow(2, levelLbl, levelBox);

        // ----- Music -----
        Label musicLbl = new Label("Music");
        CheckBox musicEnabled = new CheckBox("Enable music");
        musicEnabled.setSelected(true);

        Slider musicVol = new Slider(0, 100, 60);
        musicVol.setShowTickMarks(true);
        musicVol.setMajorTickUnit(25);
        Label musicVolVal = new Label("60%");
        musicVol.valueProperty().addListener((o, a, b) -> musicVolVal.setText(b.intValue() + "%"));

        HBox musicVolRow = new HBox(10, new Label("Volume:"), musicVol, musicVolVal);
        musicVolRow.setAlignment(Pos.CENTER_LEFT);
        VBox musicBox = new VBox(6, musicEnabled, musicVolRow);
        musicBox.setAlignment(Pos.CENTER_LEFT);
        grid.addRow(3, musicLbl, musicBox);

        // ----- Sound Effects -----
        Label sfxLbl = new Label("Sound effects");
        CheckBox sfxEnabled = new CheckBox("Enable sound effects");
        sfxEnabled.setSelected(true);

        Slider sfxVol = new Slider(0, 100, 80);
        sfxVol.setShowTickMarks(true);
        sfxVol.setMajorTickUnit(25);
        Label sfxVolVal = new Label("80%");
        sfxVol.valueProperty().addListener((o, a, b) -> sfxVolVal.setText(b.intValue() + "%"));

        HBox sfxVolRow = new HBox(10, new Label("Volume:"), sfxVol, sfxVolVal);
        sfxVolRow.setAlignment(Pos.CENTER_LEFT);
        VBox sfxBox = new VBox(6, sfxEnabled, sfxVolRow);
        sfxBox.setAlignment(Pos.CENTER_LEFT);
        grid.addRow(4, sfxLbl, sfxBox);

        // AI play
        Label aiLbl = new Label("AI play");
        CheckBox aiPlay = new CheckBox("Enable AI player");
        grid.addRow(5, aiLbl, aiPlay);

        // Extended mode
        Label extLbl = new Label("Extended mode");
        CheckBox extendedMode = new CheckBox("Enable extended pieces/modes");
        grid.addRow(6, extLbl, extendedMode);

        setCenter(grid);

        // ----- Footer -----
        Button resetBtn = new Button("Reset to defaults");
        resetBtn.setOnAction(e -> {
            widthSlider.setValue(10);
            heightSlider.setValue(20);
            levelSlider.setValue(1);
            musicEnabled.setSelected(true);
            musicVol.setValue(60);
            sfxEnabled.setSelected(true);
            sfxVol.setValue(80);
            aiPlay.setSelected(false);
            extendedMode.setSelected(false);
        });

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> App.setRoot(new MainMenuView()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(10, resetBtn, spacer, backBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(0, 20, 20, 20));
        setBottom(footer);

        //key press esc to go to main menu (testing keypress before game dev)
        setFocusTraversable(true);
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                App.setRoot(new MainMenuView());
            }
        });
    }
    // good practice in JavaFX if wanting to incorporate FXML files later
    public static Parent create() {
        return new SettingsView();
    }
}
