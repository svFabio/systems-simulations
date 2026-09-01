package com.simulacion.ejercicio1;

import com.simulacion.util.GeneradorCongruencial;
import java.util.ArrayList;
import java.util.List;

/**
 * Trapezoidal distribution simulation using the method of composition.
 *
 * Parameters: a, b, c (must satisfy 0 < a < b < c).
 *
 * h  = 2 / (-a + b + c)
 * A1 = a / (-a + b + c)
 * A2 = (2b - 2a) / (-a + b + c)
 * A3 = (c - b) / (-a + b + c)
 *
 * f1(x) = 2x / a^2        ; 0 <= x <= a
 * f2(x) = 1 / (b - a)     ; a <= x <= b
 * f3(x) = (2c - 2x) / (b-c)^2 ; b <= x <= c
 */
public class Ejercicio1Simulador {

    // Default parameters (easily editable)
    static final double DEFAULT_A = 3.0;
    static final double DEFAULT_B = 7.0;
    static final double DEFAULT_C = 10.0;

    public record SimulacionRow(double rRegion, double rValor, String region, double x) {}

    private final GeneradorCongruencial generador;

    public Ejercicio1Simulador(GeneradorCongruencial generador) {
        this.generador = generador;
    }

    /**
     * Simulate N values from the trapezoidal distribution.
     */
    public List<SimulacionRow> simular(int n, double a, double b, double c) {
        List<SimulacionRow> results = new ArrayList<>(n);

        double denominator = -a + b + c;
        double A1 = a / denominator;
        double A2 = (2 * b - 2 * a) / denominator;

        for (int i = 0; i < n; i++) {
            double rRegion = generador.siguiente();
            double rValor = generador.siguiente();
            double x;
            String region;

            if (rRegion <= A1) {
                // Region f1: 0 <= x <= a
                x = a * Math.sqrt(rValor);
                region = "f1";
            } else if (rRegion <= A1 + A2) {
                // Region f2: a <= x <= b
                x = a + (b - a) * rValor;
                region = "f2";
            } else {
                // Region f3: b <= x <= c
                x = c - (c - b) * Math.sqrt(1 - rValor);
                region = "f3";
            }

            results.add(new SimulacionRow(rRegion, rValor, region, x));
        }

        return results;
    }

    /**
     * Compute the combined probability density f(x) for the trapezoidal distribution.
     *
     * This is the RECOMBINED density (weighted sum of the three sub-densities),
     * NOT the individual sub-functions used internally for the composition method.
     *
     * h = 2 / (-a + b + c)
     * f(x) = h * (x/a)        for 0 <= x <= a
     * f(x) = h                 for a <= x <= b
     * f(x) = h * (c-x)/(c-b)  for b <= x <= c
     *
     * This function is continuous (no jumps) and integrates to 1.
     */
    public static double theoreticalF(double x, double a, double b, double c) {
        if (x < 0 || x > c) return 0;
        double h = 2.0 / (-a + b + c);
        if (x <= a) {
            return h * (x / a);
        } else if (x <= b) {
            return h;
        } else {
            return h * (c - x) / (c - b);
        }
    }
}
