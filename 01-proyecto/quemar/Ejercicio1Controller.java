package com.simulacion.ejercicio1;

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

public class Ejercicio1Controller extends VBox {

    private final Stage stage;
    private final TextField fieldA = new TextField(String.valueOf(Ejercicio1Simulador.DEFAULT_A));
    private final TextField fieldB = new TextField(String.valueOf(Ejercicio1Simulador.DEFAULT_B));
    private final TextField fieldC = new TextField(String.valueOf(Ejercicio1Simulador.DEFAULT_C));
    private final TextField fieldN = new TextField("10000");
    private final Button btnSimular = new Button("Simular");
    private final Button btnLimpiar = new Button("Limpiar");
    private final TableView<SimRow> table = new TableView<>();
    private final ObservableList<SimRow> tableData = FXCollections.observableArrayList();
    private final SwingNode chartNode = new SwingNode();
    private List<Ejercicio1Simulador.SimulacionRow> lastResults;

    public record SimRow(IntegerProperty num, DoubleProperty rRegion, DoubleProperty rValor,
                         StringProperty region, DoubleProperty x) {}

    public Ejercicio1Controller(Stage stage) {
        this.stage = stage;
        getStyleClass().add("root-pane");
        buildUI();
    }

    private void buildUI() {
        setSpacing(10);
        setPadding(new Insets(15));

        // Back button
        Button btnBack = new Button("⬅ Volver al menú");
        btnBack.getStyleClass().add("back-button");
        btnBack.setOnAction(e -> goToMenu());

        // Input fields
        VBox inputPanel = createInputPanel();

        // Buttons
        HBox buttonBox = new HBox(10, btnSimular, btnLimpiar);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        btnSimular.getStyleClass().add("action-button");
        btnLimpiar.getStyleClass().add("action-button");

        btnSimular.setOnAction(e -> simulate());
        btnLimpiar.setOnAction(e -> clear());

        // TabPane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Data tab
        setupTable();
        Tab dataTab = new Tab("Datos", newScrollPane(table));

        // Chart tab
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

        Label helpText = new Label("Debe cumplir: 0 < a < b < c");
        helpText.getStyleClass().add("help-text");

        HBox row1 = new HBox(10,
            createFieldBox("Parámetro a (límite inferior):", fieldA),
            createFieldBox("Parámetro b (inicio meseta):", fieldB),
            createFieldBox("Parámetro c (límite superior):", fieldC)
        );

        HBox row2 = new HBox(10,
            createFieldBox("Número de simulaciones:", fieldN)
        );

        panel.getChildren().addAll(row1, row2, helpText);
        return panel;
    }

    private VBox createFieldBox(String label, TextField field) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("field-label");
        field.setPrefWidth(120);
        VBox box = new VBox(4, lbl, field);
        return box;
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
            double a = Double.parseDouble(fieldA.getText().trim());
            double b = Double.parseDouble(fieldB.getText().trim());
            double c = Double.parseDouble(fieldC.getText().trim());
            int n = Integer.parseInt(fieldN.getText().trim());

            // Validation
            if (a <= 0) {
                showAlert("Error de validación", "El parámetro 'a' debe ser mayor que 0.");
                return;
            }
            if (b <= a) {
                showAlert("Error de validación", "El parámetro 'b' debe ser mayor que 'a' (b > a).");
                return;
            }
            if (c <= b) {
                showAlert("Error de validación", "El parámetro 'c' debe ser mayor que 'b' (c > b).");
                return;
            }
            if (n <= 0) {
                showAlert("Error de validación", "El número de simulaciones debe ser un entero positivo.");
                return;
            }

            GeneradorCongruencial gen = new GeneradorCongruencial(System.currentTimeMillis());
            Ejercicio1Simulador simulador = new Ejercicio1Simulador(gen);
            lastResults = simulador.simular(n, a, b, c);

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

            showChart();

        } catch (NumberFormatException ex) {
            showAlert("Error de formato", "Ingrese valores numéricos válidos en todos los campos.");
        }
    }

    private void clear() {
        fieldA.setText(String.valueOf(Ejercicio1Simulador.DEFAULT_A));
        fieldB.setText(String.valueOf(Ejercicio1Simulador.DEFAULT_B));
        fieldC.setText(String.valueOf(Ejercicio1Simulador.DEFAULT_C));
        fieldN.setText("10000");
        tableData.clear();
        lastResults = null;
        chartNode.setContent(null);
    }

    private void showChart() {
        if (lastResults == null || lastResults.isEmpty()) return;

        double a = Double.parseDouble(fieldA.getText().trim());
        double b = Double.parseDouble(fieldB.getText().trim());
        double c = Double.parseDouble(fieldC.getText().trim());

        List<Double> data = new ArrayList<>();
        for (var row : lastResults) {
            data.add(row.x());
        }

        // Theoretical curve
        int curvePoints = 200;
        double[] tx = new double[curvePoints];
        double[] ty = new double[curvePoints];
        double binWidth = c / 20.0;
        for (int i = 0; i < curvePoints; i++) {
            tx[i] = (c * i) / (curvePoints - 1);
            ty[i] = Ejercicio1Simulador.theoreticalF(tx[i], a, b, c) * data.size() * binWidth;
        }

        JFreeChart chart = EstadisticasUtil.createHistogramWithCurve(
            data, 20, 0, c,
            "Distribución Trapezoidal - a=" + a + ", b=" + b + ", c=" + c,
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
