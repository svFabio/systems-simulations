package com.simulacion.ejercicio4;

import com.simulacion.MainMenuController;
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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

public class Ejercicio4Controller extends VBox {

    private final Stage stage;
    private final TextField fieldMinN = new TextField("8");
    private final TextField fieldMaxN = new TextField("17");
    private final TextField fieldDays = new TextField(String.valueOf(Ejercicio4Simulador.DEFAULT_DAYS));
    private final Button btnSimular = new Button("Simular");
    private final Button btnLimpiar = new Button("Limpiar");
    private final Button btnGrafico = new Button("Mostrar Gráfico");
    private final TableView<DayRowView> dayTable = new TableView<>();
    private final TableView<NRowView> nTable = new TableView<>();
    private final ObservableList<DayRowView> dayData = FXCollections.observableArrayList();
    private final ObservableList<NRowView> nData = FXCollections.observableArrayList();
    private final SwingNode chartNode = new SwingNode();
    private final Label optimalLabel = new Label();
    private final Label costLabel = new Label();
    private Ejercicio4Simulador.SimulationResult lastResult;

    // Day table row view
    public record DayRowView(
        IntegerProperty day,
        DoubleProperty r1Prod, DoubleProperty r2Prod, DoubleProperty production,
        DoubleProperty r1Cap, DoubleProperty r2Cap, DoubleProperty capacity
    ) {}

    // N result table row view
    public record NRowView(
        IntegerProperty n,
        DoubleProperty truckCost, DoubleProperty freightCost, DoubleProperty totalCost,
        BooleanProperty isOptimal
    ) {}

    public Ejercicio4Controller(Stage stage) {
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

        optimalLabel.getStyleClass().add("info-label");
        costLabel.getStyleClass().add("info-label");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Data tab: day table + N result table
        setupDayTable();
        setupNTable();

        VBox dataContent = new VBox(10);
        dataContent.setPadding(new Insets(10));

        Label dayTitle = new Label("Datos diarios de simulación");
        dayTitle.getStyleClass().add("info-label");
        dayTitle.setFont(Font.font("System", FontWeight.NORMAL, 14));

        Label nTitle = new Label("Optimización por número de camiones");
        nTitle.getStyleClass().add("info-label");
        nTitle.setFont(Font.font("System", FontWeight.NORMAL, 14));

        ScrollPane dayScroll = new ScrollPane(dayTable);
        dayScroll.setFitToWidth(true);
        dayScroll.setPrefHeight(250);

        ScrollPane nScroll = new ScrollPane(nTable);
        nScroll.setFitToWidth(true);
        nScroll.setPrefHeight(200);

        dataContent.getChildren().addAll(dayTitle, dayScroll, nTitle, nScroll);
        Tab dataTab = new Tab("Datos", dataContent);

        // Chart tab
        VBox chartContent = new VBox(10, chartNode, optimalLabel, costLabel);
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

        Label helpText = new Label("Optimización de flota de camiones. Método de composición con datos agrupados.");
        helpText.getStyleClass().add("help-text");

        HBox row1 = new HBox(10,
            createFieldBox("Mín. camiones:", fieldMinN),
            createFieldBox("Máx. camiones:", fieldMaxN),
            createFieldBox("Días de trabajo:", fieldDays)
        );

        panel.getChildren().addAll(row1, helpText);
        return panel;
    }

    private VBox createFieldBox(String label, TextField field) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("field-label");
        field.setPrefWidth(100);
        return new VBox(4, lbl, field);
    }

    private void setupDayTable() {
        TableColumn<DayRowView, Integer> colDay = new TableColumn<>("#");
        colDay.setCellValueFactory(cd -> cd.getValue().day().asObject());
        colDay.setPrefWidth(50);

        TableColumn<DayRowView, Double> colR1P = new TableColumn<>("R1_prod");
        colR1P.setCellValueFactory(cd -> cd.getValue().r1Prod().asObject());
        colR1P.setPrefWidth(80);

        TableColumn<DayRowView, Double> colR2P = new TableColumn<>("R2_prod");
        colR2P.setCellValueFactory(cd -> cd.getValue().r2Prod().asObject());
        colR2P.setPrefWidth(80);

        TableColumn<DayRowView, Double> colProd = new TableColumn<>("Producción (tons)");
        colProd.setCellValueFactory(cd -> cd.getValue().production().asObject());
        colProd.setPrefWidth(120);

        TableColumn<DayRowView, Double> colR1C = new TableColumn<>("R1_cap");
        colR1C.setCellValueFactory(cd -> cd.getValue().r1Cap().asObject());
        colR1C.setPrefWidth(80);

        TableColumn<DayRowView, Double> colR2C = new TableColumn<>("R2_cap");
        colR2C.setCellValueFactory(cd -> cd.getValue().r2Cap().asObject());
        colR2C.setPrefWidth(80);

        TableColumn<DayRowView, Double> colCap = new TableColumn<>("Capacidad/camión (tons)");
        colCap.setCellValueFactory(cd -> cd.getValue().capacity().asObject());
        colCap.setPrefWidth(150);

        dayTable.getColumns().addAll(colDay, colR1P, colR2P, colProd, colR1C, colR2C, colCap);
        dayTable.setItems(dayData);
    }

    private void setupNTable() {
        TableColumn<NRowView, Integer> colN = new TableColumn<>("N (camiones)");
        colN.setCellValueFactory(cd -> cd.getValue().n().asObject());
        colN.setPrefWidth(100);

        TableColumn<NRowView, Double> colTruck = new TableColumn<>("Costo camiones ($)");
        colTruck.setCellValueFactory(cd -> cd.getValue().truckCost().asObject());
        colTruck.setPrefWidth(140);

        TableColumn<NRowView, Double> colFreight = new TableColumn<>("Costo flete ($)");
        colFreight.setCellValueFactory(cd -> cd.getValue().freightCost().asObject());
        colFreight.setPrefWidth(130);

        TableColumn<NRowView, Double> colTotal = new TableColumn<>("Costo total ($)");
        colTotal.setCellValueFactory(cd -> cd.getValue().totalCost().asObject());
        colTotal.setPrefWidth(130);

        nTable.getColumns().addAll(colN, colTruck, colFreight, colTotal);
        nTable.setItems(nData);
    }

    private void simulate() {
        try {
            int minN = Integer.parseInt(fieldMinN.getText().trim());
            int maxN = Integer.parseInt(fieldMaxN.getText().trim());
            int days = Integer.parseInt(fieldDays.getText().trim());

            if (minN <= 0 || maxN <= 0 || days <= 0) {
                showAlert("Error de validación", "Todos los valores deben ser enteros positivos.");
                return;
            }
            if (minN > maxN) {
                showAlert("Error de validación", "El mínimo de camiones no puede ser mayor que el máximo.");
                return;
            }

            GeneradorCongruencial gen = new GeneradorCongruencial(System.currentTimeMillis());
            Ejercicio4Simulador simulador = new Ejercicio4Simulador(gen);
            lastResult = simulador.simular(minN, maxN, days);

            // Populate day table
            dayData.clear();
            for (var row : lastResult.dayRows()) {
                dayData.add(new DayRowView(
                    new SimpleIntegerProperty(row.day()),
                    new SimpleDoubleProperty(row.r1Prod()),
                    new SimpleDoubleProperty(row.r2Prod()),
                    new SimpleDoubleProperty(row.production()),
                    new SimpleDoubleProperty(row.r1Cap()),
                    new SimpleDoubleProperty(row.r2Cap()),
                    new SimpleDoubleProperty(row.capacityPerTruck())
                ));
            }

            // Populate N result table
            nData.clear();
            int optN = lastResult.optimalN();
            for (var nr : lastResult.nResults()) {
                nData.add(new NRowView(
                    new SimpleIntegerProperty(nr.n()),
                    new SimpleDoubleProperty(nr.truckCost()),
                    new SimpleDoubleProperty(nr.freightCost()),
                    new SimpleDoubleProperty(nr.totalCost()),
                    new SimpleBooleanProperty(nr.n() == optN)
                ));
            }

            optimalLabel.setText("Óptimo: " + lastResult.optimalN() + " camiones");
            optimalLabel.setTextFill(Color.web("#a6e3a1"));
            costLabel.setText(String.format("Costo mínimo anual: $%,.0f", lastResult.minTotalCost()));
            costLabel.setTextFill(Color.web("#cdd6f4"));

            btnGrafico.setDisable(false);

        } catch (NumberFormatException ex) {
            showAlert("Error de formato", "Ingrese números enteros válidos en todos los campos.");
        }
    }

    private void clear() {
        fieldMinN.setText("8");
        fieldMaxN.setText("17");
        fieldDays.setText(String.valueOf(Ejercicio4Simulador.DEFAULT_DAYS));
        dayData.clear();
        nData.clear();
        lastResult = null;
        btnGrafico.setDisable(true);
        optimalLabel.setText("");
        costLabel.setText("");
        chartNode.setContent(null);
    }

    private void showChart() {
        if (lastResult == null || lastResult.nResults().isEmpty()) return;

        XYSeries series = new XYSeries("Costo total");
        for (var nr : lastResult.nResults()) {
            series.add(nr.n(), nr.totalCost());
        }
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
            "Costo Total Anual vs. Número de Camiones",
            "Número de camiones (N)",
            "Costo total anual ($)",
            dataset,
            PlotOrientation.VERTICAL,
            false, true, false
        );

        XYPlot plot = chart.getXYPlot();
        plot.getRenderer(0).setSeriesPaint(0, new java.awt.Color(70, 130, 180));
        plot.getRenderer(0).setSeriesStroke(0, new java.awt.BasicStroke(2.5f));
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setRangeGridlinePaint(java.awt.Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(java.awt.Color.LIGHT_GRAY);

        // Highlight optimal point
        XYSeries optSeries = new XYSeries("Óptimo");
        optSeries.add(lastResult.optimalN(), lastResult.minTotalCost());
        dataset.addSeries(optSeries);
        XYLineAndShapeRenderer optRenderer = new XYLineAndShapeRenderer(false, true);
        optRenderer.setSeriesPaint(0, java.awt.Color.RED);
        optRenderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-5, -5, 10, 10));
        plot.setDataset(1, dataset);
        plot.setRenderer(1, optRenderer);

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
