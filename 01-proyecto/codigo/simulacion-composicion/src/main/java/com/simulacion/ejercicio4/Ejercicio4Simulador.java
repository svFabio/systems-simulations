package com.simulacion.ejercicio4;

import com.simulacion.util.GeneradorCongruencial;
import java.util.ArrayList;
import java.util.List;

/**
 * Truck Fleet Optimization simulation using composition method with grouped data.
 *
 * A manufacturing company decides how many trucks to buy for daily production
 * transport (250 days/year). Excess goes to external carrier at $100/ton.
 * Each truck costs $100,000/year. Find N that minimizes total annual cost.
 *
 * Method: R1 picks interval, R2 generates uniform value within interval.
 */
public class Ejercicio4Simulador {

    // ==================== PARAMETERS (easily editable) ====================

    static final double COST_PER_TRUCK = 100_000;   // $/year
    static final double EXTERNAL_FREIGHT = 100;      // $/ton
    static final int DEFAULT_DAYS = 250;              // working days/year

    // Production table (tons/day): cumProbStart, lowerBound, upperBound, classProb
    static final double[][] PROD_TABLE = {
        {0.00, 50, 55, 0.10},
        {0.10, 55, 60, 0.15},
        {0.25, 60, 65, 0.30},
        {0.55, 65, 70, 0.35},
        {0.90, 75, 80, 0.08},
        {0.98, 80, 85, 0.02}
        // Note: gap 70-75 is intentional per original problem statement
    };

    // Truck capacity table (tons/day): cumProbStart, lowerBound, upperBound, classProb
    static final double[][] CAP_TABLE = {
        {0.00, 4.0, 4.5, 0.30},
        {0.30, 4.5, 5.0, 0.40},
        {0.70, 5.0, 5.5, 0.20},
        {0.90, 5.5, 6.0, 0.10}
    };

    // ==================== RECORDS ====================

    /** One day of simulated data */
    public record DayRow(
        int day,
        double r1Prod, double r2Prod, double production,
        double r1Cap, double r2Cap, double capacityPerTruck
    ) {}

    /** Result for one candidate N */
    public record NResult(
        int n,
        double truckCost,
        double freightCost,
        double totalCost
    ) {}

    /** Full simulation result */
    public record SimulationResult(
        List<DayRow> dayRows,
        List<NResult> nResults,
        int optimalN,
        double minTotalCost
    ) {}

    private final GeneradorCongruencial generador;

    public Ejercicio4Simulador(GeneradorCongruencial generador) {
        this.generador = generador;
    }

    /**
     * Composition method for grouped data: R1 picks interval, R2 generates value within it.
     */
    private double sampleFromTable(double[][] table, double r1, double r2) {
        // Find the interval where cumulative probability >= r1
        for (double[] row : table) {
            double cumProbStart = row[0];
            double lower = row[1];
            double upper = row[2];
            double classProb = row[3];
            double cumProbEnd = cumProbStart + classProb;

            if (r1 < cumProbEnd || row == table[table.length - 1]) {
                // Uniform value within the interval
                return lower + r2 * (upper - lower);
            }
        }
        // Fallback (should never reach here)
        double[] last = table[table.length - 1];
        return last[1] + r2 * (last[2] - last[1]);
    }

    /**
     * Run the full simulation.
     *
     * @param minN minimum trucks to evaluate
     * @param maxN maximum trucks to evaluate
     * @param days number of working days
     * @return simulation result with day data and optimization results
     */
    public SimulationResult simular(int minN, int maxN, int days) {
        // Step 1: Simulate the full year of (production, capacity) pairs using common random numbers
        List<DayRow> dayRows = new ArrayList<>(days);
        double[] productions = new double[days];
        double[] capacities = new double[days];

        for (int d = 0; d < days; d++) {
            double r1Prod = generador.siguiente();
            double r2Prod = generador.siguiente();
            double production = sampleFromTable(PROD_TABLE, r1Prod, r2Prod);

            double r1Cap = generador.siguiente();
            double r2Cap = generador.siguiente();
            double capacity = sampleFromTable(CAP_TABLE, r1Cap, r2Cap);

            dayRows.add(new DayRow(d + 1, r1Prod, r2Prod, production, r1Cap, r2Cap, capacity));
            productions[d] = production;
            capacities[d] = capacity;
        }

        // Step 2: For each candidate N, compute total cost using the same random numbers
        List<NResult> nResults = new ArrayList<>();
        double minCost = Double.MAX_VALUE;
        int optimalN = minN;

        for (int n = minN; n <= maxN; n++) {
            double annualFreightCost = 0;

            for (int d = 0; d < days; d++) {
                double fleetCapacity = capacities[d] * n;
                double excess = Math.max(productions[d] - fleetCapacity, 0);
                annualFreightCost += excess * EXTERNAL_FREIGHT;
            }

            double annualTruckCost = n * COST_PER_TRUCK;
            double totalCost = annualTruckCost + annualFreightCost;

            nResults.add(new NResult(n, annualTruckCost, annualFreightCost, totalCost));

            if (totalCost < minCost) {
                minCost = totalCost;
                optimalN = n;
            }
        }

        return new SimulationResult(dayRows, nResults, optimalN, minCost);
    }
}
