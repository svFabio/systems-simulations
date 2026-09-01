package com.simulacion.ejercicio3;

import com.simulacion.util.GeneradorCongruencial;
import java.util.ArrayList;
import java.util.List;

/**
 * TIR Monte Carlo simulation for Compañía X.
 *
 * Simulates investment project evaluation with triangular distributions
 * for all uncertain parameters. Calculates IRR (TIR) by bisection method.
 */
public class Ejercicio3Simulador {

    // ==================== PARAMETERS (easily editable) ====================

    // Fixed asset (AF): triangular(a=60000, b=70000, c=100000)
    static final double AF_A = 60000;
    static final double AF_B = 70000;
    static final double AF_C = 100000;

    // Current asset (AC): triangular(a=25000, b=30000, c=40000)
    static final double AC_A = 25000;
    static final double AC_B = 30000;
    static final double AC_C = 40000;

    // Cash flow before taxes (same for 5 years): triangular(a=30000, b=40000, c=45000)
    static final double CF_A = 30000;
    static final double CF_B = 40000;
    static final double CF_C = 45000;

    // Inflation by year (triangular {a,b,c})
    static final double[][] INFLACION = {
        {0.12, 0.15, 0.18},  // Year 1
        {0.12, 0.15, 0.18},  // Year 2
        {0.15, 0.18, 0.22},  // Year 3
        {0.18, 0.20, 0.25},  // Year 4
        {0.19, 0.22, 0.28}   // Year 5
    };

    static final double TAX_RATE = 0.50;        // 50%
    static final double TREMA = 0.15;           // 15%
    static final int LIFETIME = 5;              // years
    static final double RESCUE_AF_RATIO = 0.20; // 20% of AF
    static final double RESCUE_AC_RATIO = 1.00; // 100% of AC

    // TIR bisection limits
    static final double TIR_MIN = -0.031;   // -3.1%
    static final double TIR_MAX = 0.1991;   // 19.91%
    static final int NUM_BINS = 20;
    static final double TIR_THRESHOLD = 0.15; // 15% TREMA for probability calc
    static final double ACCEPTANCE_PROB = 0.90;

    public record SimulacionRow(
        double af, double ac,
        double x1, double x2, double x3, double x4, double x5,
        double i1, double i2, double i3, double i4, double i5,
        double s1, double s2, double s3, double s4, double s5,
        double vr, double tir
    ) {}

    public record SimulationResult(
        List<SimulacionRow> rows,
        double probability,
        String decision
    ) {}

    private final GeneradorCongruencial generador;

    public Ejercicio3Simulador(GeneradorCongruencial generador) {
        this.generador = generador;
    }

    /**
     * Standard triangular distribution sampling.
     */
    private double triangular(double a, double b, double c) {
        double r = generador.siguiente();
        double puntoQuiebre = (b - a) / (c - a);
        if (r <= puntoQuiebre) {
            return a + Math.sqrt((c - a) * (b - a) * r);
        } else {
            return c - Math.sqrt((c - a) * (c - b) * (1 - r));
        }
    }

    /**
     * Run one complete simulation (steps 4-7).
     */
    private SimulacionRow simularUna() {
        // Step 4: simulate AF and AC
        double af = triangular(AF_A, AF_B, AF_C);
        double ac = triangular(AC_A, AC_B, AC_C);

        double[] x = new double[5]; // cash flows before taxes
        double[] inflacion = new double[5]; // inflation rates
        double[] inflacionAcum = new double[5]; // accumulated inflation
        double[] s = new double[5]; // constant cash flows

        double inversionAC = 0; // additional AC investment

        // Step 5: simulate years 1-5
        for (int t = 0; t < 5; t++) {
            x[t] = triangular(CF_A, CF_B, CF_C);
            inflacion[t] = triangular(INFLACION[t][0], INFLACION[t][1], INFLACION[t][2]);

            // Accumulated inflation
            if (t == 0) {
                inflacionAcum[t] = 1 + inflacion[t];
            } else {
                inflacionAcum[t] = inflacionAcum[t - 1] * (1 + inflacion[t]);
            }

            // Additional AC investment (only in years 2-5, based on accumulated inflation)
            if (t > 0) {
                inversionAC = ac * (inflacionAcum[t] - inflacionAcum[t - 1]);
            }

            // Nominal cash flow before taxes
            double flujoNominal = x[t] * inflacionAcum[t];

            // Depreciation (straight line, 5 years)
            double depreciacion = af / LIFETIME;

            // Taxable income
            double ingresoGravable = flujoNominal - depreciacion - inversionAC;

            // Taxes (if positive)
            double impuestos = Math.max(0, ingresoGravable * TAX_RATE);

            // Net cash flow: nominal flow minus taxes minus additional AC investment
            // inversionAC is a cash outflow (working capital increase) that must be
            // subtracted from the net cash flow, even though it already reduced taxable income.
            s[t] = flujoNominal - impuestos - inversionAC;
        }

        // Step 6: salvage value
        double vr = RESCUE_AF_RATIO * af + RESCUE_AC_RATIO * ac;

        // Step 7: final cash flows and TIR calculation
        // Deflate nominal cash flows to real (constant) terms by dividing by
        // accumulated inflation so the resulting TIR is a REAL rate comparable
        // to the real TREMA (15%).
        double[] flujos = new double[6];
        flujos[0] = -(af + ac); // initial investment (already in year-0 terms)
        for (int t = 0; t < 4; t++) {
            flujos[t + 1] = s[t] / inflacionAcum[t];
        }
        flujos[5] = (s[4] + vr) / inflacionAcum[4]; // last year + salvage, deflated

        double tir = calcularTIR(flujos);

        return new SimulacionRow(
            af, ac,
            x[0], x[1], x[2], x[3], x[4],
            inflacion[0], inflacion[1], inflacion[2], inflacion[3], inflacion[4],
            s[0], s[1], s[2], s[3], s[4],
            vr, tir
        );
    }

    /**
     * Calculate IRR (TIR) using the bisection method.
     */
    private double calcularTIR(double[] flujos) {
        // Wide bounds for bisection search — TIR_MIN/TIR_MAX are histogram bounds only
        double low = -0.99;   // -99%
        double high = 10.0;   // 1000%

        for (int iter = 0; iter < 1000; iter++) {
            double mid = (low + high) / 2.0;
            double vpnMid = calcularVPN(flujos, mid);

            if (Math.abs(vpnMid) < 1e-10) {
                return mid;
            }

            if (vpnMid > 0) {
                low = mid;
            } else {
                high = mid;
            }
        }

        return (low + high) / 2.0;
    }

    /**
     * Calculate NPV (VPN) for a given discount rate.
     */
    private double calcularVPN(double[] flujos, double tasa) {
        double vpn = 0;
        for (int t = 0; t < flujos.length; t++) {
            vpn += flujos[t] / Math.pow(1 + tasa, t);
        }
        return vpn;
    }

    /**
     * Run N simulations.
     */
    public SimulationResult simular(int n) {
        List<SimulacionRow> rows = new ArrayList<>(n);
        int countAboveTREMA = 0;

        for (int i = 0; i < n; i++) {
            SimulacionRow row = simularUna();
            rows.add(row);
            if (row.tir() > TIR_THRESHOLD) {
                countAboveTREMA++;
            }
        }

        double probability = (double) countAboveTREMA / n;
        String decision = probability >= ACCEPTANCE_PROB ? "ACEPTAR" : "RECHAZAR";

        return new SimulationResult(rows, probability, decision);
    }
}
