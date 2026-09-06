package com.simulacion;

import com.simulacion.ejercicio1.Ejercicio1Controller;
import com.simulacion.ejercicio2.Ejercicio2Controller;
import com.simulacion.ejercicio3.Ejercicio3Controller;
import com.simulacion.ejercicio4.Ejercicio4Controller;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class MainMenuController extends StackPane {

    private final Stage stage;

    public MainMenuController(Stage stage) {
        this.stage = stage;
        getStyleClass().add("root-pane");
        buildUI();
    }

    private void buildUI() {
        Label titleLabel = new Label("Simulación - Composición y Transformada Inversa");
        titleLabel.getStyleClass().add("menu-title");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20));

        String[][] cards = {
            {"1", "Distribución Trapezoidal", "Método de composición y transformada inversa"},
            {"2", "Distribución Triangular", "Método de composición y transformada inversa"},
            {"3", "Simulación TIR", "Evaluación de proyecto de inversión"},
            {"4", "Optimización de Flota", "Número óptimo de camiones"}
        };

        for (int i = 0; i < 4; i++) {
            int row = i / 2;
            int col = i % 2;
            grid.add(createCard(cards[i][0], cards[i][1], cards[i][2], i + 1), col, row);
        }

        VBox centerBox = new VBox(25, titleLabel, grid);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setMaxWidth(800);

        getChildren().add(centerBox);
    }

    private StackPane createCard(String number, String title, String description, int exercise) {
        StackPane card = new StackPane();
        card.getStyleClass().add("menu-card");
        card.setPrefSize(360, 200);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        Label numLabel = new Label(number);
        numLabel.getStyleClass().add("card-number");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("card-description");
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.CENTER);

        Button openBtn = new Button("Abrir");
        openBtn.getStyleClass().add("card-button");
        openBtn.setOnAction(e -> navigateTo(exercise));

        content.getChildren().addAll(numLabel, titleLabel, descLabel, openBtn);
        card.getChildren().add(content);

        return card;
    }

    private void navigateTo(int exercise) {
        Parent screen;
        switch (exercise) {
            case 1 -> screen = new Ejercicio1Controller(stage);
            case 2 -> screen = new Ejercicio2Controller(stage);
            case 3 -> screen = new Ejercicio3Controller(stage);
            case 4 -> screen = new Ejercicio4Controller(stage);
            default -> {
                return;
            }
        }
        Scene scene = new Scene(screen, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/com/simulacion/styles.css").toExternalForm());
        stage.setScene(scene);
    }
}
