package com.simulacion.ejercicio2;

import com.simulacion.MainMenuController;
import com.simulacion.util.EstadisticasUtil;
import com.simulacion.util.GeneradorCongruencial;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

public class Ejercicio2Controller extends VBox {

    private final Stage stage;
    private final TextField fieldP1X = new TextField(String.valueOf(Ejercicio2Simulador.DEFAULT_P1_X));
    private final TextField fieldP1Y = new TextField(String.valueOf(Ejercicio2Simulador.DEFAULT_P1_Y));
    private final TextField fieldP2X = new TextField(String.valueOf(Ejercicio2Simulador.DEFAULT_P2_X));
    private final TextField fieldP2Y = new TextField(String.valueOf(Ejercicio2Simulador.DEFAULT_P2_Y));
    private final TextField fieldP3X = new TextField(String.valueOf(Ejercicio2Simulador.DEFAULT_P3_X));
    private final TextField fieldP3Y = new TextField(String.valueOf(Ejercicio2Simulador.DEFAULT_P3_Y));
    private final TextField fieldN = new TextField("10000");
    private final Button btnSimular = new Button("Simular");
    private final Button btnLimpiar = new Button("Limpiar");
    private final Button btnGrafico = new Button("Mostrar Gráfico");
    private final TableView<SimRow> table = new TableView<>();
    private final ObservableList<SimRow> tableData = FXCollections.observableArrayList();
    private final SwingNode chartNode = new SwingNode();
    private List<Ejercicio2Simulador.SimulacionRow> lastResults;

    public record SimRow(IntegerProperty num, DoubleProperty rRegion, DoubleProperty rValor,
                         StringProperty region, DoubleProperty x) {}

    public Ejercicio2Controller(Stage stage) {
        this.stage = stage;
        getStyleClass().add("root-pane");
        buildUI();
    }

    private void buildUI() {
        setSpacing(10);
        setPadding(new Insets(15));

        Button btnBack = new Button("⬅ Volver al menú");
        btnBack.getStyleClass().add("back-button");
        btnBack.setOnAction(e -> goToMenu());

        VBox inputPanel = createInputPanel();

        HBox buttonBox = new HBox(10, btnSimular, btnLimpiar, btnGrafico);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        btnSimular.getStyleClass().add("action-button");
        btnLimpiar.getStyleClass().add("action-button");
        btnGrafico.getStyleClass().add("action-button");
        btnGrafico.setDisable(true);

        btnSimular.setOnAction(e -> simulate());
        btnLimpiar.setOnAction(e -> clear());
        btnGrafico.setOnAction(e -> showChart());

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        setupTable();
        Tab dataTab = new Tab("Datos", newScrollPane(table));

        ScrollPane chartScroll = new ScrollPane(chartNode);
        chartScroll.setFitToWidth(true);
        chartScroll.setFitToHeight(true);
        Tab chartTab = new Tab("Gráfico", chartScroll);

        tabPane.getTabs().addAll(dataTab, chartTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        getChildren().addAll(btnBack, inputPanel, buttonBox, tabPane);
    }

    private VBox createInputPanel() {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("input-panel");

        Label helpText = new Label("Nota: A1=0.5, A2=0.5. Fórmulas asumen que Punto2Y es el máximo y Punto1Y=Punto3Y.");
        helpText.getStyleClass().add("help-text");

        HBox row1 = new HBox(10,
            createFieldBox("Punto 1 (X):", fieldP1X),
            createFieldBox("Punto 1 (Y):", fieldP1Y),
            createFieldBox("Punto 2 - vértice (X):", fieldP2X),
            createFieldBox("Punto 2 (Y):", fieldP2Y)
        );

        HBox row2 = new HBox(10,
            createFieldBox("Punto 3 (X):", fieldP3X),
            createFieldBox("Punto 3 (Y):", fieldP3Y),
            createFieldBox("Número de simulaciones:", fieldN)
        );

        panel.getChildren().addAll(row1, row2, helpText);
        return panel;
    }

    private VBox createFieldBox(String label, TextField field) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("field-label");
        field.setPrefWidth(100);
        return new VBox(4, lbl, field);
    }

    private void setupTable() {
        TableColumn<SimRow, Integer> colNum = new TableColumn<>("#");
        colNum.setCellValueFactory(cd -> cd.getValue().num().asObject());
        colNum.setPrefWidth(50);

        TableColumn<SimRow, Double> colRRegion = new TableColumn<>("R_region");
        colRRegion.setCellValueFactory(cd -> cd.getValue().rRegion().asObject());
        colRRegion.setPrefWidth(100);

        TableColumn<SimRow, Double> colRValor = new TableColumn<>("R_valor");
        colRValor.setCellValueFactory(cd -> cd.getValue().rValor().asObject());
        colRValor.setPrefWidth(100);

        TableColumn<SimRow, String> colRegion = new TableColumn<>("Región");
        colRegion.setCellValueFactory(cd -> cd.getValue().region());
        colRegion.setPrefWidth(80);

        TableColumn<SimRow, Double> colX = new TableColumn<>("Valor X");
        colX.setCellValueFactory(cd -> cd.getValue().x().asObject());
        colX.setPrefWidth(120);

        table.getColumns().addAll(colNum, colRRegion, colRValor, colRegion, colX);
        table.setItems(tableData);
    }

    private ScrollPane newScrollPane(javafx.scene.Node content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        return sp;
    }

    private void simulate() {
        try {
            double p1x = Double.parseDouble(fieldP1X.getText().trim());
            double p1y = Double.parseDouble(fieldP1Y.getText().trim());
            double p2x = Double.parseDouble(fieldP2X.getText().trim());
            double p2y = Double.parseDouble(fieldP2Y.getText().trim());
            double p3x = Double.parseDouble(fieldP3X.getText().trim());
            double p3y = Double.parseDouble(fieldP3Y.getText().trim());
            int n = Integer.parseInt(fieldN.getText().trim());

            if (n <= 0) {
                showAlert("Error de validación", "El número de simulaciones debe ser un entero positivo.");
                return;
            }

            GeneradorCongruencial gen = new GeneradorCongruencial(System.currentTimeMillis());
            Ejercicio2Simulador simulador = new Ejercicio2Simulador(gen);
            lastResults = simulador.simular(n, p1x, p1y, p2x, p2y, p3x, p3y);

            tableData.clear();
            for (int i = 0; i < lastResults.size(); i++) {
                var r = lastResults.get(i);
                tableData.add(new SimRow(
                    new SimpleIntegerProperty(i + 1),
                    new SimpleDoubleProperty(r.rRegion()),
                    new SimpleDoubleProperty(r.rValor()),
                    new SimpleStringProperty(r.region()),
                    new SimpleDoubleProperty(r.x())
                ));
            }

            btnGrafico.setDisable(false);

        } catch (NumberFormatException ex) {
            showAlert("Error de formato", "Ingrese valores numéricos válidos en todos los campos.");
        }
    }

    private void clear() {
        fieldP1X.setText(String.valueOf(Ejercicio2Simulador.DEFAULT_P1_X));
        fieldP1Y.setText(String.valueOf(Ejercicio2Simulador.DEFAULT_P1_Y));
        fieldP2X.setText(String.valueOf(Ejercicio2Simulador.DEFAULT_P2_X));
        fieldP2Y.setText(String.valueOf(Ejercicio2Simulador.DEFAULT_P2_Y));
        fieldP3X.setText(String.valueOf(Ejercicio2Simulador.DEFAULT_P3_X));
        fieldP3Y.setText(String.valueOf(Ejercicio2Simulador.DEFAULT_P3_Y));
        fieldN.setText("10000");
        tableData.clear();
        lastResults = null;
        btnGrafico.setDisable(true);
        chartNode.setContent(null);
    }

    private void showChart() {
        if (lastResults == null || lastResults.isEmpty()) return;

        List<Double> data = new ArrayList<>();
        for (var row : lastResults) {
            data.add(row.x());
        }

        double minX = 8.0;
        double maxX = 10.0;

        // Theoretical curve (triangle)
        int curvePoints = 200;
        double[] tx = new double[curvePoints];
        double[] ty = new double[curvePoints];
        double binWidth = (maxX - minX) / 20.0;
        for (int i = 0; i < curvePoints; i++) {
            tx[i] = minX + ((maxX - minX) * i) / (curvePoints - 1);
            ty[i] = Ejercicio2Simulador.theoreticalF(tx[i]) * data.size() * binWidth;
        }

        JFreeChart chart = EstadisticasUtil.createHistogramWithCurve(
            data, 20, minX, maxX,
            "Distribución Triangular - (8,0.25) (9,0.75) (10,0.25)",
            "Valor X", "Frecuencia", tx, ty
        );

        SwingUtilities.invokeLater(() -> {
            ChartPanel panel = new ChartPanel(chart);
            panel.setPreferredSize(new Dimension(700, 450));
            chartNode.setContent(panel);
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void goToMenu() {
        Scene scene = new Scene(new MainMenuController(stage), 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/com/simulacion/styles.css").toExternalForm());
        stage.setScene(scene);
    }
}
