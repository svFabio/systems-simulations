package com.simulacion.ejercicio3;

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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

public class Ejercicio3Controller extends VBox {

    private final Stage stage;
    private final TextField fieldN = new TextField("1000");
    private final Button btnSimular = new Button("Simular");
    private final Button btnLimpiar = new Button("Limpiar");
    private final TableView<SimRow> table = new TableView<>();
    private final ObservableList<SimRow> tableData = FXCollections.observableArrayList();
    private final SwingNode chartNode = new SwingNode();
    private final Label probLabel = new Label();
    private final Label decisionLabel = new Label();
    private Ejercicio3Simulador.SimulationResult lastResult;

    public record SimRow(
        IntegerProperty num, DoubleProperty af, DoubleProperty ac,
        DoubleProperty x1, DoubleProperty x2, DoubleProperty x3, DoubleProperty x4, DoubleProperty x5,
        DoubleProperty i1, DoubleProperty i2, DoubleProperty i3, DoubleProperty i4, DoubleProperty i5,
        DoubleProperty s1, DoubleProperty s2, DoubleProperty s3, DoubleProperty s4, DoubleProperty s5,
        DoubleProperty vr, DoubleProperty tir
    ) {}

    public Ejercicio3Controller(Stage stage) {
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

        HBox buttonBox = new HBox(10, btnSimular, btnLimpiar);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        btnSimular.getStyleClass().add("action-button");
        btnLimpiar.getStyleClass().add("action-button");

        btnSimular.setOnAction(e -> simulate());
        btnLimpiar.setOnAction(e -> clear());

        probLabel.getStyleClass().add("info-label");
        decisionLabel.getStyleClass().add("decision-label");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        setupTable();
        Tab dataTab = new Tab("Datos", newScrollPane(table));

        VBox chartContent = new VBox(10, chartNode, probLabel, decisionLabel);
        chartContent.setPadding(new Insets(10));
        ScrollPane chartScroll = new ScrollPane(chartContent);
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

        Label helpText = new Label("Evaluación de proyecto de inversión - Compañía X. Triangulares para todos los parámetros.");
        helpText.getStyleClass().add("help-text");

        HBox row1 = new HBox(10,
            createFieldBox("Número de simulaciones:", fieldN)
        );

        panel.getChildren().addAll(row1, helpText);
        return panel;
    }

    private VBox createFieldBox(String label, TextField field) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("field-label");
        field.setPrefWidth(120);
        return new VBox(4, lbl, field);
    }

    private void setupTable() {
        TableColumn<SimRow, Integer> colNum = new TableColumn<>("#");
        colNum.setCellValueFactory(cd -> cd.getValue().num().asObject());
        colNum.setPrefWidth(40);

        TableColumn<SimRow, Double> colAF = new TableColumn<>("AF");
        colAF.setCellValueFactory(cd -> cd.getValue().af().asObject());
        colAF.setPrefWidth(90);

        TableColumn<SimRow, Double> colAC = new TableColumn<>("AC");
        colAC.setCellValueFactory(cd -> cd.getValue().ac().asObject());
        colAC.setPrefWidth(90);

        for (int i = 1; i <= 5; i++) {
            int idx = i;
            TableColumn<SimRow, Double> colX = new TableColumn<>("x" + i);
            colX.setCellValueFactory(cd -> {
                var row = cd.getValue();
                return switch (idx) {
                    case 1 -> row.x1().asObject();
                    case 2 -> row.x2().asObject();
                    case 3 -> row.x3().asObject();
                    case 4 -> row.x4().asObject();
                    case 5 -> row.x5().asObject();
                    default -> null;
                };
            });
            colX.setPrefWidth(80);
            table.getColumns().add(colX);
        }

        for (int i = 1; i <= 5; i++) {
            int idx = i;
            TableColumn<SimRow, Double> colI = new TableColumn<>("i" + i);
            colI.setCellValueFactory(cd -> {
                var row = cd.getValue();
                return switch (idx) {
                    case 1 -> row.i1().asObject();
                    case 2 -> row.i2().asObject();
                    case 3 -> row.i3().asObject();
                    case 4 -> row.i4().asObject();
                    case 5 -> row.i5().asObject();
                    default -> null;
                };
            });
            colI.setPrefWidth(60);
            table.getColumns().add(colI);
        }

        for (int i = 1; i <= 5; i++) {
            int idx = i;
            TableColumn<SimRow, Double> colS = new TableColumn<>("S" + idx);
            colS.setCellValueFactory(cd -> {
                var row = cd.getValue();
                return switch (idx) {
                    case 1 -> row.s1().asObject();
                    case 2 -> row.s2().asObject();
                    case 3 -> row.s3().asObject();
                    case 4 -> row.s4().asObject();
                    case 5 -> row.s5().asObject();
                    default -> null;
                };
            });
            colS.setPrefWidth(90);
            table.getColumns().add(colS);
        }

        TableColumn<SimRow, Double> colVR = new TableColumn<>("VR");
        colVR.setCellValueFactory(cd -> cd.getValue().vr().asObject());
        colVR.setPrefWidth(100);

        TableColumn<SimRow, Double> colTIR = new TableColumn<>("TIR");
        colTIR.setCellValueFactory(cd -> cd.getValue().tir().asObject());
        colTIR.setPrefWidth(80);

        table.getColumns().addAll(colVR, colTIR);
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
            int n = Integer.parseInt(fieldN.getText().trim());

            if (n <= 0) {
                showAlert("Error de validación", "El número de simulaciones debe ser un entero positivo.");
                return;
            }

            GeneradorCongruencial gen = new GeneradorCongruencial(System.currentTimeMillis());
            Ejercicio3Simulador simulador = new Ejercicio3Simulador(gen);
            lastResult = simulador.simular(n);

            tableData.clear();
            List<Ejercicio3Simulador.SimulacionRow> rows = lastResult.rows();
            for (int i = 0; i < rows.size(); i++) {
                var r = rows.get(i);
                tableData.add(new SimRow(
                    new SimpleIntegerProperty(i + 1),
                    new SimpleDoubleProperty(r.af()),
                    new SimpleDoubleProperty(r.ac()),
                    new SimpleDoubleProperty(r.x1()),
                    new SimpleDoubleProperty(r.x2()),
                    new SimpleDoubleProperty(r.x3()),
                    new SimpleDoubleProperty(r.x4()),
                    new SimpleDoubleProperty(r.x5()),
                    new SimpleDoubleProperty(r.i1()),
                    new SimpleDoubleProperty(r.i2()),
                    new SimpleDoubleProperty(r.i3()),
                    new SimpleDoubleProperty(r.i4()),
                    new SimpleDoubleProperty(r.i5()),
                    new SimpleDoubleProperty(r.s1()),
                    new SimpleDoubleProperty(r.s2()),
                    new SimpleDoubleProperty(r.s3()),
                    new SimpleDoubleProperty(r.s4()),
                    new SimpleDoubleProperty(r.s5()),
                    new SimpleDoubleProperty(r.vr()),
                    new SimpleDoubleProperty(r.tir())
                ));
            }

            probLabel.setText(String.format("Prob(TIR > TREMA) = %.1f%%", lastResult.probability() * 100));
            decisionLabel.setText("Decisión: " + lastResult.decision());
            if (lastResult.decision().equals("ACEPTAR")) {
                decisionLabel.setTextFill(Color.GREEN);
            } else {
                decisionLabel.setTextFill(Color.RED);
            }

            showChart();

        } catch (NumberFormatException ex) {
            showAlert("Error de formato", "Ingrese un número entero válido para simulaciones.");
        }
    }

    private void clear() {
        fieldN.setText("1000");
        tableData.clear();
        lastResult = null;
        probLabel.setText("");
        decisionLabel.setText("");
        chartNode.setContent(null);
    }

    private void showChart() {
        if (lastResult == null || lastResult.rows().isEmpty()) return;

        List<Double> tirData = new ArrayList<>();
        for (var row : lastResult.rows()) {
            tirData.add(row.tir() * 100); // Convert to percentage
        }

        double minTIR = Ejercicio3Simulador.TIR_MIN * 100;
        double maxTIR = Ejercicio3Simulador.TIR_MAX * 100;

        JFreeChart chart = EstadisticasUtil.createSimpleHistogram(
            tirData, Ejercicio3Simulador.NUM_BINS, minTIR, maxTIR,
            "Distribución TIR",
            "TIR (%)", "Frecuencia"
        );

        SwingUtilities.invokeLater(() -> {
            ChartPanel panel = new ChartPanel(chart);
            panel.setPreferredSize(new Dimension(700, 400));
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
