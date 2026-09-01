package com.simulacion;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("Simulación - Composición y Transformada Inversa");
        stage.setMinWidth(900);
        stage.setMinHeight(650);

        Scene scene = new Scene(new MainMenuController(stage), 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/com/simulacion/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
