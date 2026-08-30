# Taller de Simulación de Sistemas — Modelo teórico y validación

## Marco teórico y referencias bibliográficas

El modelo de simulación se apoya en los dos textos de referencia habituales de un curso de
Simulación de Sistemas:

- **Ross, S. M. (2022).** *Simulation* (6th ed.). Academic Press. ISBN 978-0-323-85738-3.
  (También válido usar la 5ª ed., 2013, ISBN 978-0-12-415825-2 — el contenido relevante es el mismo).
- **Ríos Insua, D., Ríos Insua, S., y Martín Jiménez, J. R. (1997).** *Simulación: Métodos y
  Aplicaciones*. Madrid: RA-MA. ISBN 84-7897-258-7. (2ª ed.: Jiménez Martín, A., Ríos Insua, D.,
  Ríos Insua, S., Martín Jiménez, J. R., y García Rama, M. L. *Simulación. Métodos y
  Aplicaciones*, 2ª ed., RA-MA, ISBN 978-84-7897-895-3.)

Cada bloque del modelo implementado corresponde a una técnica descrita en esos libros:

| Técnica usada en el taller | Ross (2022) | Ríos Insua et al. |
|---|---|---|
| Generación de números pseudoaleatorios U(0,1) como insumo de todo el modelo | Cap. "Random Numbers" | Cap. "Números aleatorios" |
| Método de la transformada inversa para generar variables continuas (aquí: la triangular, para Af, Ac, flujo e inflación) | Cap. "Generating Continuous Random Variables" | Cap. "Variables aleatorias" |
| Muestreo de una distribución empírica por tramos (producción diaria y capacidad por camión, Problema 2) como caso particular de transformada inversa sobre una función de distribución escalonada | Cap. "Generating Continuous Random Variables" | Cap. "Variables aleatorias" |
| Simulación como "reloj" de eventos/periodos que avanza escenario a escenario (5 años) o día a día (250 días) | Cap. "The Discrete Event Simulation Approach" | Cap. "Simulación de sucesos discretos" |
| Números aleatorios comunes: en el Problema 2, la producción y la capacidad de cada camión se generan **una sola vez por día** y se reutilizan para evaluar todos los valores de N, para que las distintas flotas se comparen bajo el mismo escenario simulado (reduce la varianza del contraste entre alternativas) | Cap. "Variance Reduction Techniques" | Cap. "Reducción de la varianza" |
| Análisis de la salida: número de iteraciones/réplicas, media, desviación estándar, mínimo/máximo de la TIR simulada, y estimación de P(TIR>TREMA) como proporción muestral | Cap. "Statistical Analysis of Simulated Data" | Cap. "Análisis de resultados" |
| Validación del modelo: se contrastan los resultados de la "simulación manual" (Excel, muestra pequeña) contra una implementación independiente (Java, muestra grande) para verificar que ambas convergen a la misma decisión | Discusión de validación de modelos de simulación | Cap. "Análisis de resultados" (contraste de salidas del modelo) |



## Problema 1: Decisión de inversión

**Variables aleatorias** (todas triangulares, muestreadas por transformada inversa):

Para `U~Unif(0,1)`, con `a=min(pesimista,optimista)`, `b=max(pesimista,optimista)`, `m=probable`:

```
si U <= (m-a)/(b-a):  X = a + sqrt(U(b-a)(m-a))
si no:                 X = b - sqrt((1-U)(b-a)(b-m))
```

(Para las tasas de inflación, "pesimista" es el valor más alto, así que `a` y `b` se toman
con MIN/MAX en vez de asumir el orden literal de las columnas.)

**Flujo de caja de cada escenario simulado (una iteración = un proyecto a 5 años):**

- Activo fijo (Af) y activo circulante (Ac): triangulares independientes.
- Flujo antes de impuestos y depreciación, año i: triangular (30000,40000,45000), muestreado
  independientemente cada año.
- Inflación año i: triangular propia de cada año.
- Flujo inflacionado_i = Flujo_i × Π(1+Inflación_j/100), j=1..i (se acumula la inflación).
- Depreciación anual = |Af| / 5 (línea recta sobre el valor simulado del activo, vida fiscal 5 años).
- UAI_i = FlujoInflacionado_i − Depreciación
- Impuesto_i = UAI_i × 50% (se permite escudo fiscal si UAI_i < 0, ya que es una compañía en marcha)
- Utilidad neta_i = UAI_i − Impuesto_i
- Valor de rescate = 20%×|Af| + 100%×|Ac| (se suma al flujo del año 5)
- FNE_i = Utilidad neta_i + Depreciación (+ Valor de rescate si i=5)
- FNE_0 = Af + Ac (inversión inicial, negativa)
- TIR = tasa que hace VPN(FNE_0..FNE_5) = 0

**Regla de decisión:** aceptar si `P(TIR > TREMA=15%) >= 90%`.

## Problema 2: Número óptimo de camiones

- Producción diaria y toneladas por camión: distribuciones empíricas por tramos, muestreadas
  como uniforme continua dentro del tramo seleccionado (según la probabilidad acumulada).
- **Nota:** el enunciado no asigna probabilidad al tramo 70–75 ton (la tabla suma 1.00 sin él);
  se respetó tal cual, tratándolo como intervalo sin masa de probabilidad.
- Para cada camión adicional se genera su propia capacidad diaria (independiente); la capacidad
  total con N camiones es la suma de las primeras N capacidades del día (números aleatorios
  comunes: mismo día, mismas variables base, para poder comparar N de forma justa).
- Excedente_día = max(Producción − CapacidadTotal(N), 0), enviado a $100/tonelada.
- Costo total anual(N) = N×$100,000 + $100 × Σ Excedente_día (250 días).
- N óptimo = el que minimiza el costo total anual.

## Validación (Excel manual vs. Java)

| | Excel (n=100 / 250 días) | Java (n=100 / 250 días) | Java (muestra grande) |
|---|---|---|---|
| P(TIR > TREMA) | 100% | 100% | 100% (n=20,000) |
| TIR promedio | ~27–28% | ~27% | ~27.5% (n=20,000) |
| Decisión | **Aceptar** | **Aceptar** | **Aceptar** |
| N óptimo de camiones | 12 | 12 | 12 (2,000 réplicas) |
| Costo total mínimo | ~$1.37–1.39M | ~$1.37M | ~$1.38M (promedio) |

Los tres enfoques coinciden en la conclusión: los valores puntuales varían ligeramente porque
cada corrida usa números aleatorios distintos (es una simulación Montecarlo), pero la decisión
y el número óptimo son estables. Esto valida el modelo implementado en Excel contra el programa
Java (mismo modelo teórico, misma lógica de muestreo).

## Cómo compilar y ejecutar el Java

```bash
javac SimulacionTaller.java
java SimulacionTaller
```

No requiere librerías externas (solo `java.util.Random`).

## Bibliografía

- Ross, S. M. (2022). *Simulation* (6th ed.). Academic Press, San Diego, CA. ISBN 978-0-323-85738-3.
- Ríos Insua, D., Ríos Insua, S., y Martín Jiménez, J. R. (1997). *Simulación: Métodos y
  Aplicaciones*. RA-MA, Madrid. ISBN 84-7897-258-7.

