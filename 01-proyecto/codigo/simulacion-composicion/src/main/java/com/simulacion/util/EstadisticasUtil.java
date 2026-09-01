package com.simulacion.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.List;

/**
 * Utility class for statistical operations: histogram binning and chart creation.
 */
public final class EstadisticasUtil {

    private EstadisticasUtil() {
    }

    /**
     * Computes histogram bins from data.
     *
     * @param data  list of double values
     * @param bins  number of bins
     * @param min   minimum value for binning
     * @param max   maximum value for binning
     * @return array of counts per bin
     */
    public static int[] computeBins(List<Double> data, int bins, double min, double max) {
        int[] counts = new int[bins];
        double binWidth = (max - min) / bins;
        for (double val : data) {
            int idx = (int) ((val - min) / binWidth);
            if (idx < 0) idx = 0;
            if (idx >= bins) idx = bins - 1;
            counts[idx]++;
        }
        return counts;
    }

    /**
     * Creates a JFreeChart histogram with an overlaid theoretical curve.
     *
     * @param data            simulated values
     * @param bins            number of histogram bins
     * @param min             min for histogram range
     * @param max             max for histogram range
     * @param title           chart title
     * @param xLabel          x-axis label
     * @param yLabel          y-axis label
     * @param theoreticalX    x values for the theoretical curve
     * @param theoreticalY    y values for the theoretical curve (frequency-scaled)
     * @return JFreeChart instance
     */
    public static JFreeChart createHistogramWithCurve(
            List<Double> data, int bins, double min, double max,
            String title, String xLabel, String yLabel,
            double[] theoreticalX, double[] theoreticalY) {

        // Create histogram dataset
        org.jfree.data.statistics.HistogramDataset histDataset = new org.jfree.data.statistics.HistogramDataset();
        double[] dataArray = data.stream().mapToDouble(Double::doubleValue).toArray();
        histDataset.addSeries("Histograma Experimental", dataArray, bins, min, max);
        histDataset.setType(HistogramType.FREQUENCY);

        // Create theoretical curve dataset
        XYSeriesCollection curveDataset = new XYSeriesCollection();
        if (theoreticalX != null && theoreticalY != null) {
            XYSeries series = new XYSeries("Distribución Teórica");
            for (int i = 0; i < theoreticalX.length; i++) {
                series.add(theoreticalX[i], theoreticalY[i]);
            }
            curveDataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createHistogram(
                title, xLabel, yLabel, histDataset,
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                true, true, false);

        XYPlot plot = chart.getXYPlot();

        // Add theoretical curve on secondary dataset
        if (theoreticalX != null && theoreticalY != null) {
            plot.setDataset(1, curveDataset);
            XYSplineRenderer lineRenderer = new XYSplineRenderer();
            lineRenderer.setSeriesPaint(0, java.awt.Color.RED);
            lineRenderer.setSeriesStroke(0, new java.awt.BasicStroke(2.5f));
            lineRenderer.setSeriesShapesVisible(0, false);
            plot.setRenderer(1, lineRenderer);
            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        }

        // Style the histogram
        plot.getRenderer(0).setSeriesPaint(0, new java.awt.Color(70, 130, 180)); // steel blue
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setRangeGridlinePaint(java.awt.Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(java.awt.Color.LIGHT_GRAY);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        return chart;
    }

    /**
     * Creates a simple histogram chart (no overlaid curve).
     */
    public static JFreeChart createSimpleHistogram(
            List<Double> data, int bins, double min, double max,
            String title, String xLabel, String yLabel) {

        org.jfree.data.statistics.HistogramDataset histDataset = new org.jfree.data.statistics.HistogramDataset();
        double[] dataArray = data.stream().mapToDouble(Double::doubleValue).toArray();
        histDataset.addSeries("Frecuencia", dataArray, bins, min, max);
        histDataset.setType(HistogramType.FREQUENCY);

        JFreeChart chart = ChartFactory.createHistogram(
                title, xLabel, yLabel, histDataset,
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                true, true, false);

        XYPlot plot = chart.getXYPlot();
        plot.getRenderer(0).setSeriesPaint(0, new java.awt.Color(70, 130, 180));
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setRangeGridlinePaint(java.awt.Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(java.awt.Color.LIGHT_GRAY);

        return chart;
    }
}
