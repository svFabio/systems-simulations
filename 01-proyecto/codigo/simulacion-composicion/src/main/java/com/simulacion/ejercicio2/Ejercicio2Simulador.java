package com.simulacion.ejercicio2;

import com.simulacion.util.GeneradorCongruencial;
import java.util.ArrayList;
import java.util.List;

/**
 * Asymmetric triangular distribution simulation using the method of composition.
 *
 * Points: (8, 0.25), (9, 0.75), (10, 0.25)
 *
 * f1(x) = 2x - 16  ; 8 <= x <= 9
 * f2(x) = -2x + 20 ; 9 <= x <= 10
 *
 * A1 = 1/2, A2 = 1/2
 *
 * LIMITATION: This implementation assumes Punto2Y is always the maximum
 * and Punto1Y == Punto3Y. The formulas for f1, f2, A1, A2 are hardcoded
 * for the specific triangular distribution (8,0.25)-(9,0.75)-(10,0.25).
 * Changing the Y values of the points will NOT produce correct results
 * unless the formulas are generalized.
 */
public class Ejercicio2Simulador {

    // Default parameters (easily editable)
    static final double DEFAULT_P1_X = 8.0;
    static final double DEFAULT_P1_Y = 0.25;
    static final double DEFAULT_P2_X = 9.0;
    static final double DEFAULT_P2_Y = 0.75;
    static final double DEFAULT_P3_X = 10.0;
    static final double DEFAULT_P3_Y = 0.25;

    public record SimulacionRow(double rRegion, double rValor, String region, double x) {}

    private final GeneradorCongruencial generador;

    public Ejercicio2Simulador(GeneradorCongruencial generador) {
        this.generador = generador;
    }

    /**
     * Simulate N values from the asymmetric triangular distribution.
     *
     * LIMITATION: Only works correctly when Punto2Y is the maximum and Punto1Y == Punto3Y.
     */
    public List<SimulacionRow> simular(int n, double p1x, double p1y, double p2x, double p2y, double p3x, double p3y) {
        List<SimulacionRow> results = new ArrayList<>(n);

        // A1 = 0.5, A2 = 0.5 (as per specification for this distribution)
        double A1 = 0.5;

        for (int i = 0; i < n; i++) {
            double rRegion = generador.siguiente();
            double rValor = generador.siguiente();
            double x;
            String region;

            if (rRegion <= A1) {
                // Region f1: 8 <= x <= 9
                x = p1x + Math.sqrt(rValor);
                region = "f1";
            } else {
                // Region f2: 9 <= x <= 10
                x = p3x - Math.sqrt(1 - rValor);
                region = "f2";
            }

            results.add(new SimulacionRow(rRegion, rValor, region, x));
        }

        return results;
    }

    /**
     * Compute the combined probability density f(x) for the triangular distribution.
     *
     * This is the RECOMBINED density (weighted sum of the two sub-densities),
     * NOT the individual sub-functions used internally for the composition method.
     *
     * A1 = 0.5, A2 = 0.5 (each region has equal probability)
     * f1(x) = 2x - 16  ; 8 <= x <= 9  (area = 1, so A1*f1 = x-8)
     * f2(x) = -2x + 20 ; 9 <= x <= 10 (area = 1, so A2*f2 = -x+10)
     *
     * Combined: f(x) = x - 8   for 8 <= x <= 9
     *           f(x) = -x + 10 for 9 <= x <= 10
     *
     * This function is continuous and integrates to 1.
     */
    public static double theoreticalF(double x) {
        if (x < 8 || x > 10) return 0;
        if (x <= 9) {
            return x - 8;       // A1 * f1(x) = 0.5 * (2x - 16)
        } else {
            return -x + 10;     // A2 * f2(x) = 0.5 * (-2x + 20)
        }
    }
}
