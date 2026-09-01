package com.simulacion.util;

public class GeneradorCongruencial {
    private long semilla;
    private final long a; // multiplicador
    private final long c; // incremento
    private final long m; // modulo

    public GeneradorCongruencial(long semilla) {
        this.a = 1103515245L;
        this.c = 12345L;
        this.m = 2147483648L; // 2^31
        this.semilla = Math.floorMod(semilla, m); // reduce al rango [0, m) para evitar overflow en a*semilla
    }

    /** Devuelve un uniforme en [0,1) */
    public double siguiente() {
        semilla = (a * semilla + c) % m;
        return (double) semilla / (double) m;
    }
}
