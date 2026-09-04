import java.util.Random;
import java.util.Locale;

/**
 * Taller de Simulacion de Sistemas
 * Implementa en Java los dos modelos teoricos de simulacion (Monte Carlo)
 * correspondientes a la misma logica usada en el archivo Excel de simulacion manual.
 *
 * Problema 1: Decision de inversion (flujos triangulares + inflacion triangular + TIR)
 * Problema 2: Numero optimo de camiones (produccion y capacidad empiricas)
 */
public class SimulacionTaller {

    static final Random RND = new Random();

    // ---------------------------------------------------------------------------------
    // Utilidades generales
    // ---------------------------------------------------------------------------------

    /** Genera una variable triangular a partir de un numero aleatorio U(0,1) dado. */
    static double triangular(double pesimista, double probable, double optimista, double u) {
        double a = Math.min(pesimista, optimista); // minimo
        double b = Math.max(pesimista, optimista); // maximo
        double m = probable;                       // moda
        double fc = (m - a) / (b - a);
        if (u <= fc) {
            return a + Math.sqrt(u * (b - a) * (m - a));
        } else {
            return b - Math.sqrt((1 - u) * (b - a) * (b - m));
        }
    }

    static double triangular(double pesimista, double probable, double optimista, Random rnd) {
        return triangular(pesimista, probable, optimista, rnd.nextDouble());
    }

    /** Muestrea una distribucion empirica por tramos (uniforme dentro de cada tramo). */
    static double sampleEmpirical(double[][] bins, double u) {
        // bins[i] = {lowerBound, upperBound, prob}
        double cum = 0.0;
        for (double[] bin : bins) {
            double lower = bin[0], upper = bin[1], prob = bin[2];
            if (u <= cum + prob || bin == bins[bins.length - 1]) {
                double width = upper - lower;
                double frac = (u - cum) / prob;
                return lower + width * frac;
            }
            cum += prob;
        }
        // fallback (no deberia llegar aqui si las probabilidades suman 1)
        double[] last = bins[bins.length - 1];
        return last[0];
    }

    static double sampleEmpirical(double[][] bins, Random rnd) {
        return sampleEmpirical(bins, rnd.nextDouble());
    }

    /** TIR (IRR) por bisección sobre el VPN. flows[0] es el desembolso inicial (t=0). */
    static double irrBisection(double[] flows) {
        double lo = -0.9999, hi = 10.0;
        double npvLo = npv(flows, lo);
        double npvHi = npv(flows, hi);
        if (Double.isNaN(npvLo) || Double.isNaN(npvHi) || npvLo * npvHi > 0) {
            return Double.NaN; // no hay cambio de signo en el rango: no se puede acotar la TIR
        }
        for (int it = 0; it < 200; it++) {
            double mid = (lo + hi) / 2.0;
            double npvMid = npv(flows, mid);
            if (Math.abs(npvMid) < 1e-6) return mid;
            if (npvLo * npvMid < 0) {
                hi = mid;
            } else {
                lo = mid;
                npvLo = npvMid;
            }
        }
        return (lo + hi) / 2.0;
    }

    static double npv(double[] flows, double rate) {
        double total = 0.0;
        for (int t = 0; t < flows.length; t++) {
            total += flows[t] / Math.pow(1 + rate, t);
        }
        return total;
    }

    // ---------------------------------------------------------------------------------
    // PROBLEMA 1: DECISION DE INVERSION
    // ---------------------------------------------------------------------------------

    static class Problema1Resultado {
        int n;
        double probIrrMayorTrema;
        double irrPromedio, irrStdev, irrMin, irrMax;
        int casosAceptables;
    }

    static Problema1Resultado simularProblema1(int nIteraciones, double tasaImpuesto, double trema) {
        // Parametros de las distribuciones triangulares (pesimista, probable, optimista)
        double[] afParams = {-100000, -70000, -60000};
        double[] acParams = {-40000, -30000, -25000};
        double[] flujoParams = {30000, 40000, 45000};
        double[][] inflParams = {
                {18, 15, 12}, // año 1
                {18, 15, 12}, // año 2
                {22, 18, 15}, // año 3
                {25, 20, 18}, // año 4
                {28, 22, 19}, // año 5
        };
        int vidaFiscal = 5;

        double[] irrValores = new double[nIteraciones];
        int validos = 0;
        int aceptables = 0;
        double sumaIrr = 0.0;

        for (int it = 0; it < nIteraciones; it++) {
            double af = triangular(afParams[0], afParams[1], afParams[2], RND);
            double ac = triangular(acParams[0], acParams[1], acParams[2], RND);

            double[] flujo = new double[5];
            double[] infl = new double[5];
            for (int y = 0; y < 5; y++) {
                flujo[y] = triangular(flujoParams[0], flujoParams[1], flujoParams[2], RND);
                infl[y] = triangular(inflParams[y][0], inflParams[y][1], inflParams[y][2], RND);
            }

            double[] flujoInfl = new double[5];
            double acumInfl = 1.0;
            for (int y = 0; y < 5; y++) {
                acumInfl *= (1 + infl[y] / 100.0);
                flujoInfl[y] = flujo[y] * acumInfl;
            }

            double depreciacion = Math.abs(af) / vidaFiscal;
            double valorRescate = 0.2 * Math.abs(af) + 1.0 * Math.abs(ac);

            double[] cf = new double[6]; // cf[0]=t0 ... cf[5]=t5
            cf[0] = af + ac;
            for (int y = 0; y < 5; y++) {
                double ebt = flujoInfl[y] - depreciacion;
                double impuesto = ebt * tasaImpuesto;
                double utilidadNeta = ebt - impuesto;
                double fne = utilidadNeta + depreciacion;
                if (y == 4) fne += valorRescate;
                cf[y + 1] = fne;
            }

            double irr = irrBisection(cf);
            if (!Double.isNaN(irr)) {
                irrValores[validos++] = irr;
                sumaIrr += irr;
                if (irr > trema) aceptables++;
            }
        }

        double media = sumaIrr / validos;
        double sumSqDiff = 0.0, min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (int i = 0; i < validos; i++) {
            double d = irrValores[i] - media;
            sumSqDiff += d * d;
            if (irrValores[i] < min) min = irrValores[i];
            if (irrValores[i] > max) max = irrValores[i];
        }
        double stdev = Math.sqrt(sumSqDiff / (validos - 1));

        Problema1Resultado res = new Problema1Resultado();
        res.n = validos;
        res.casosAceptables = aceptables;
        res.probIrrMayorTrema = (double) aceptables / validos;
        res.irrPromedio = media;
        res.irrStdev = stdev;
        res.irrMin = min;
        res.irrMax = max;
        return res;
    }

    // ---------------------------------------------------------------------------------
    // PROBLEMA 2: NUMERO OPTIMO DE CAMIONES
    // ---------------------------------------------------------------------------------

    static double[][] PRODUCCION_BINS = {
            {50, 55, 0.10},
            {55, 60, 0.15},
            {60, 65, 0.30},
            {65, 70, 0.35},
            {75, 80, 0.08},
            {80, 85, 0.02},
    };

    static double[][] CAMION_BINS = {
            {4.0, 4.5, 0.30},
            {4.5, 5.0, 0.40},
            {5.0, 5.5, 0.20},
            {5.5, 6.0, 0.10},
    };

    /**
     * Simula 'replicas' años de 'diasPorAnio' dias para camionMax camiones (usando numeros
     * aleatorios comunes: la produccion y las capacidades de cada camion se generan una sola
     * vez por dia y se reutilizan para evaluar todos los valores de N, igual que en Excel).
     * Devuelve el costo total anual promedio para cada N (indice 0 => N=1).
     */
    static double[] simularProblema2(int camionMax, int diasPorAnio, int replicas,
                                      double costoCamion, double costoTonExterno) {
        double[] excesoAcumulado = new double[camionMax]; // toneladas excedentes acumuladas (todas las replicas)

        for (int rep = 0; rep < replicas; rep++) {
            double[] excesoAnio = new double[camionMax];
            for (int d = 0; d < diasPorAnio; d++) {
                double produccion = sampleEmpirical(PRODUCCION_BINS, RND);
                double capAcumulada = 0.0;
                for (int k = 0; k < camionMax; k++) {
                    double capCamion = sampleEmpirical(CAMION_BINS, RND);
                    capAcumulada += capCamion;
                    double exceso = Math.max(produccion - capAcumulada, 0.0);
                    excesoAnio[k] += exceso;
                }
            }
            for (int k = 0; k < camionMax; k++) excesoAcumulado[k] += excesoAnio[k];
        }

        double[] costoTotalPromedio = new double[camionMax];
        for (int k = 0; k < camionMax; k++) {
            double excesoPromedioAnual = excesoAcumulado[k] / replicas;
            int n = k + 1;
            double costoCamiones = n * costoCamion;
            double costoTransporte = excesoPromedioAnual * costoTonExterno;
            costoTotalPromedio[k] = costoCamiones + costoTransporte;
        }
        return costoTotalPromedio;
    }

    // ---------------------------------------------------------------------------------
    // MAIN
    // ---------------------------------------------------------------------------------

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        System.out.println("=============================================================");
        System.out.println(" PROBLEMA 1: DECISION DE INVERSION (Simulacion Montecarlo)");
        System.out.println("=============================================================");

        double tasaImpuesto = 0.50;
        double trema = 0.15;
        double probMinima = 0.90;

        // (a) Replicando el tamaño de muestra del Excel (100 iteraciones)
        Problema1Resultado r100 = simularProblema1(100, tasaImpuesto, trema);
        imprimirProblema1("n = 100 (igual que la hoja de Excel)", r100, probMinima);

        // (b) Muestra grande para una estimacion mas robusta de la probabilidad
        Problema1Resultado rGrande = simularProblema1(20000, tasaImpuesto, trema);
        imprimirProblema1("n = 20,000 (estimacion de referencia)", rGrande, probMinima);

        System.out.println();
        System.out.println("=============================================================");
        System.out.println(" PROBLEMA 2: NUMERO OPTIMO DE CAMIONES (Simulacion Montecarlo)");
        System.out.println("=============================================================");

        int camionMax = 18;
        int diasPorAnio = 250;
        double costoCamion = 100000;
        double costoTonExterno = 100;

        // (a) una sola "corrida" de 250 dias, igual que la hoja de Excel
        double[] costoUnaCorrida = simularProblema2(camionMax, diasPorAnio, 1, costoCamion, costoTonExterno);
        imprimirProblema2("Una corrida de 250 dias (comparable con Excel)", costoUnaCorrida);

        // (b) 2000 replicas (años) para una estimacion mas robusta del numero optimo
        double[] costoPromedio = simularProblema2(camionMax, diasPorAnio, 2000, costoCamion, costoTonExterno);
        imprimirProblema2("Promedio de 2000 replicas (estimacion de referencia)", costoPromedio);
    }

    static void imprimirProblema1(String titulo, Problema1Resultado r, double probMinima) {
        System.out.println("-- " + titulo + " --");
        System.out.printf("  Iteraciones validas       : %d%n", r.n);
        System.out.printf("  Casos con TIR > TREMA     : %d%n", r.casosAceptables);
        System.out.printf("  P(TIR > TREMA)            : %.2f%%%n", r.probIrrMayorTrema * 100);
        System.out.printf("  TIR promedio              : %.2f%%%n", r.irrPromedio * 100);
        System.out.printf("  Desviacion estandar TIR   : %.2f%%%n", r.irrStdev * 100);
        System.out.printf("  TIR minima / maxima       : %.2f%% / %.2f%%%n", r.irrMin * 100, r.irrMax * 100);
        String decision = r.probIrrMayorTrema >= probMinima ? "ACEPTAR el proyecto" : "RECHAZAR el proyecto";
        System.out.println("  DECISION                  : " + decision);
        System.out.println();
    }

    static void imprimirProblema2(String titulo, double[] costoTotal) {
        System.out.println("-- " + titulo + " --");
        int mejorN = 1;
        double mejorCosto = Double.MAX_VALUE;
        for (int i = 0; i < costoTotal.length; i++) {
            int n = i + 1;
            System.out.printf("  N=%2d camiones  ->  Costo total anual = $%,.2f%n", n, costoTotal[i]);
            if (costoTotal[i] < mejorCosto) {
                mejorCosto = costoTotal[i];
                mejorN = n;
            }
        }
        System.out.printf("  NUMERO OPTIMO DE CAMIONES : %d  (costo total anual = $%,.2f)%n", mejorN, mejorCosto);
        System.out.println();
    }
}
