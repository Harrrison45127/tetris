package com.waskronos.tetris;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class App extends Application {
    private static Scene scene;

    @Override
    public void start(Stage stage) {
        // Start with the Main Menu
        
         SplashView splash = new SplashView(() -> App.setRoot(new MainMenuView()));
        scene = new Scene(splash, 480, 720);
        stage.setTitle("Tetris");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    /** Simple helper to change screen */
    public static void setRoot(Parent root) {
        scene.setRoot(root);
    }

    public static void main(String[] args) {
        launch();
    }
}