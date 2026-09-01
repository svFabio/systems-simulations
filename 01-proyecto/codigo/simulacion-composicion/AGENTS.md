# Contrato de Desarrollo — Simulación Composición

## Objetivo

Aplicación de escritorio en Java 21 + JavaFX, gestionada con Maven, que implementa 3 ejercicios de un curso de Simulación (métodos de composición y transformada inversa, y simulación Monte Carlo de una TIR), más un 4to ejercicio como placeholder "en desarrollo".

## Stack técnico obligatorio

- **Java 21**, **JavaFX 21** (controles, código Java + CSS)
- **Maven** (pom.xml con javafx-maven-plugin)
- **JFreeChart** (última versión estable) para TODOS los gráficos, embebido en JavaFX mediante `javafx.embed.swing.SwingNode` + `org.jfree.chart.fx.ChartViewer` si está disponible, o SwingNode envolviendo un `ChartPanel` de JFreeChart clásico.
- **CSS custom** para toda la interfaz (paleta profesional, tipografía legible, botones con estados hover, tarjetas con sombra sutil).

## Estructura del proyecto

```
simulacion-composicion/
  pom.xml
  src/main/java/com/simulacion/
    App.java                          (entry point, lanza el menú)
    MainMenuController.java           (pantalla de menú con 4 tarjetas/botones)
    util/GeneradorCongruencial.java   (PRNG compartido)
    util/EstadisticasUtil.java        (funciones compartidas: tabla de frecuencias, etc.)
    ejercicio1/Ejercicio1Controller.java   (Trapezoidal)
    ejercicio1/Ejercicio1Simulador.java
    ejercicio2/Ejercicio2Controller.java   (Triangular 8-9-10)
    ejercicio2/Ejercicio2Simulador.java
    ejercicio3/Ejercicio3Controller.java   (TIR Monte Carlo, Compañía X)
    ejercicio3/Ejercicio3Simulador.java
    ejercicio4/Ejercicio4Controller.java   (placeholder)
  src/main/resources/com/simulacion/
    styles.css
```

## Navegación general

- App.java abre la ventana con MainMenuController como escena inicial.
- El menú principal muestra 4 tarjetas grandes (una por ejercicio), con: número, título corto, descripción de 1 línea, y botón "Abrir".
- Cada pantalla de ejercicio tiene un botón "⬅ Volver al menú" arriba a la izquierda, SIEMPRE visible.
- Cada pantalla de ejercicio se divide en dos zonas verticales:
  - Panel superior fijo: inputs + botones de acción ("Simular", "Limpiar", "Mostrar Gráfico").
  - TabPane debajo, con exactamente 2 pestañas: "Datos" (TableView con TODAS las filas simuladas, con scroll) y "Gráfico" (el histograma/curva vía JFreeChart).

## PRNG compartido (OBLIGATORIO)

Usar `GeneradorCongruencial` — NO usar `Math.random()` ni `java.util.Random` en ningún cálculo.

```java
package com.simulacion.util;

public class GeneradorCongruencial {
    private long semilla;
    private final long a; // multiplicador
    private final long c; // incremento
    private final long m; // modulo

    public GeneradorCongruencial(long semilla) {
        this.semilla = semilla;
        this.a = 1103515245L;
        this.c = 12345L;
        this.m = 2147483648L; // 2^31
    }

    /** Devuelve un uniforme en [0,1) */
    public double siguiente() {
        semilla = (a * semilla + c) % m;
        return (double) semilla / (double) m;
    }
}
```

Cada `*Simulador.java` recibe una instancia de GeneradorCongruencial (semilla inicial = `System.currentTimeMillis()`).

**Nota para el futuro**: Los parámetros de las distribuciones están fijos por ahora. Estructurar el código para que vivan en constantes al inicio de cada `*Simulador.java`, fácilmente editables.

---

## PANTALLA: MENÚ PRINCIPAL

4 tarjetas, en cuadrícula 2x2 o fila de 4:

- "Ejercicio 1 — Distribución Trapezoidal" — "Método de composición y transformada inversa"
- "Ejercicio 2 — Distribución Triangular" — "Método de composición y transformada inversa"
- "Ejercicio 3 — Simulación TIR" — "Evaluación de proyecto de inversión (Monte Carlo)"
- "Ejercicio 4" — "Próximamente"

Estilo: fondo oscuro o claro profesional, tarjetas con borde redondeado, sombra, efecto hover, tipografía clara.

---

## EJERCICIO 1 — DISTRIBUCIÓN TRAPEZOIDAL (Método de Composición)

### Fórmulas

Parámetros: a, b, c (deben cumplir 0 < a < b < c).

```
h = 2 / (-a + b + c)
A1 = a / (-a + b + c)
A2 = (2b - 2a) / (-a + b + c)
A3 = (c - b) / (-a + b + c)
```

f1(x) = 2x / a² ; 0 <= x <= a
f2(x) = 1 / (b - a) ; a <= x <= b
f3(x) = (2c - 2x) / (b - c)² ; b <= x <= c

### Algoritmo de simulación

1. Generar R_region = generador.siguiente()
2. Si R_region <= A1: x = a * sqrt(R_valor), region = "f1"
   Sino si R_region <= A1 + A2: x = a + (b - a) * R_valor, region = "f2"
   Sino: x = c - (c - b) * sqrt(1 - R_valor), region = "f3"
3. Repetir N veces

### Valores por defecto

```java
static final double A = 3.0;
static final double B = 7.0;
static final double C = 10.0;
```

### Inputs en pantalla

- Campo "Parámetro a (límite inferior)" — validar 0 <= a < b
- Campo "Parámetro b (inicio meseta)" — validar a < b < c
- Campo "Parámetro c (límite superior)" — validar b < c
- Campo "Número de simulaciones" — default 10000, validar entero positivo
- Botones: "Simular", "Limpiar", "Mostrar Gráfico"

### Tabla (pestaña "Datos")

| # | R_region | R_valor | Región | Valor X |

### Gráfico (pestaña "Gráfico")

- Histograma de frecuencias de los valores X simulados
- Superpuesta: curva teórica f(x) escalada
- Título: "Distribución Trapezoidal - a=<a>, b=<b>, c=<c>"
- Leyenda: "Histograma Experimental" / "Distribución Teórica"

---

## EJERCICIO 2 — DISTRIBUCIÓN TRIANGULAR ASIMÉTRICA (Método de Composición)

### Fórmulas

Puntos fijos: (8, 1/4), (9, 3/4), (10, 1/4)

f1(x) = 2x - 16 ; 8 <= x <= 9
f2(x) = -2x + 20 ; 9 <= x <= 10

A1 = 1/2, A2 = 1/2

### Algoritmo de simulación

1. Generar R_region = generador.siguiente()
2. Si R_region <= A1: x = 8 + sqrt(R_valor), region = "f1"
   Sino: x = 10 - sqrt(1 - R_valor), region = "f2"
3. Repetir N veces

### Valores por defecto

```java
static final double PUNTO1_X = 8, PUNTO1_Y = 0.25;
static final double PUNTO2_X = 9, PUNTO2_Y = 0.75;
static final double PUNTO3_X = 10, PUNTO3_Y = 0.25;
```

### Inputs en pantalla

- Campo "Punto 1 (X)" y "Punto 1 (Y)" — default 8, 0.25
- Campo "Punto 2 - vértice (X)" y "(Y)" — default 9, 0.75
- Campo "Punto 3 (X)" y "(Y)" — default 10, 0.25
- Campo "Número de simulaciones" — default 10000
- Botones: "Simular", "Limpiar", "Mostrar Gráfico"

**Nota**: El cálculo de A1, A2, f1, f2 asume que Punto2Y siempre es el máximo y Punto1Y=Punto3Y. Documentar esta limitación con un comentario claro.

### Tabla (pestaña "Datos")

| # | R_region | R_valor | Región | Valor X |

### Gráfico (pestaña "Gráfico")

- Histograma experimental (azul) + curva teórica del triángulo (roja)
- Título: "Distribución Triangular - (8,0.25) (9,0.75) (10,0.25)"

---

## EJERCICIO 3 — SIMULACIÓN TIR (Compañía X)

### Enunciado

La Compañía X evalúa un proyecto de inversión con:

- Activo fijo inicial (AF): triangular(a=60000, b=70000, c=100000)
- Activo circulante inicial (AC): triangular(a=25000, b=30000, c=40000)
- Flujo de efectivo antes de impuestos, igual los 5 años: triangular(a=30000, b=40000, c=45000)
- Inflación por año (todas triangulares, {a,b,c}):
  - Año 1: {0.12, 0.15, 0.18}
  - Año 2: {0.12, 0.15, 0.18}
  - Año 3: {0.15, 0.18, 0.22}
  - Año 4: {0.18, 0.20, 0.25}
  - Año 5: {0.19, 0.22, 0.28}
- Tasa de impuestos T = 50%
- TREMA = 15%
- Vida fiscal del activo = 5 años
- Valor de rescate: 20% del AF simulado + 100% del AC simulado
- Umbral de decisión: aceptar si Prob(TIR > TREMA) >= 90%

### Algoritmo para triangular estándar

```
1. Generar R = generador.siguiente()
2. puntoQuiebre = (b - a) / (c - a)
3. Si R <= puntoQuiebre: x = a + sqrt((c - a) * (b - a) * R)
   Sino: x = c - sqrt((c - a) * (c - b) * (1 - R))
```

### Una simulación completa (Pasos 4 a 7)

- Paso 4: simular AF y AC
- Paso 5: para t=1..5, simular x_t e i_t, calcular inflación acumulada, inversión adicional en AC, flujo antes de impuestos nominal, depreciación, ingreso gravable, impuestos, flujo corriente, flujo constante (S_t)
- Paso 6: valor de rescate
- Paso 7: flujos finales = [-(AF+AC), S_1, S_2, S_3, S_4, S_5+VR], calcular TIR por bisección

### Clasificación

- 20 intervalos entre TIR_MIN=-3.1% y TIR_MAX=19.91% (constantes fijas)
- Prob(TIR > 15%) = (cantidad de TIR simuladas > 15) / N
- Decisión: si Prob >= 0.90 → "ACEPTAR", si no → "RECHAZAR"

### Inputs en pantalla

- Campo "Número de simulaciones" — default 1000
- Botones: "Simular", "Limpiar", "Mostrar Gráfico"

### Tabla (pestaña "Datos")

| # | AF | AC | x1..x5 | i1..i5 | S1..S5 | VR | TIR simulada |

### Gráfico (pestaña "Gráfico")

- Histograma de frecuencias de la TIR simulada
- Panel con texto: "Prob(TIR > TREMA) = X.X%" y "Decisión: ACEPTAR/RECHAZAR" (verde/rojo)

---

## EJERCICIO 4 — PLACEHOLDER

Pantalla simple con texto centrado "En desarrollo" y botón "⬅ Volver al menú".

---

## VALIDACIÓN Y MANEJO DE ERRORES

- Texto no numérico → Alert con mensaje claro
- Parámetros inválidos → Alert específica con restricción violada
- Texto de ayuda tipo "Debe cumplir: 0 < a < b < c" al lado de cada campo, siempre visible
- "Limpiar" resetea inputs a valores por defecto y vacía tabla/gráfico
- "Mostrar Gráfico" solo habilitado después de correr al menos una simulación

## ENTREGABLE FINAL

Proyecto Maven completo, compilable con `mvn clean package` y ejecutable con `mvn clean javafx:run`, sin errores ni warnings de compilación.
