# Algoritmo de cuotas

Una **cuota** es lo que se multiplica por el importe si se acierta: apostar 10 a 1,80 devuelve 18 (8 de beneficio). Cuanto más probable es un resultado, más baja es su cuota. El código está en `cuotas/CalculadoraCuotas.java`.

## Los cinco pasos

| Paso | Qué hace | Fórmula |
|---|---|---|
| 1. Nivel | Calidad (0–10, la pone el creador) y forma (−2 a +2) | `nivel = calidad + 0,4 × forma` |
| 2. Diferencia | Jugar en casa da ventaja | `dif = nivelLocal + 0,5 − nivelVisitante` |
| 3. Empate | Solo fútbol; más probable si están igualados | `pE = max(0,10; 0,30 − 0,02 × |dif|)` |
| 4. Reparto | El resto con una curva logística | `r = 1 / (1 + e^(−0,45 × dif))`, `pL = (1 − pE) × r`, `pV = (1 − pE) × (1 − r)` |
| 5. Cuota | Margen de la casa (1,07), redondeo hacia abajo, entre 1,01 y 50 | `cuota = 1 / (p × 1,07)` |

## Ejemplo resuelto
Local con calidad 8 y forma normal; visitante con calidad 7 y "mala racha" (−1).

- Niveles: local 8; visitante 7 − 0,4 = 6,6. Diferencia: 8 + 0,5 − 6,6 = **1,9**.
- Empate: 0,30 − 0,02 × 1,9 = **0,262**. Reparto: r = 0,702.
- Local: 0,738 × 0,702 = **0,518**. Visitante: 0,738 × 0,298 = **0,220**.
- **Cuotas: 1,80 · 3,56 · 4,24.**

1/1,80 + 1/3,56 + 1/4,24 = **1,072**. Que pase de 1 es lo que asegura que **la casa gana a largo plazo**: es el margen. El 6,5 % de las cuentas de la casa sale de 1 − 1/1,07.

## Ajuste por volumen
Si mucha gente apuesta a un resultado, su cuota baja y las demás suben:

```
peso = 0,30 × total / (total + 1.000)        (nunca llega a 0,30)
p'   = (1 − peso) × p + peso × (dinero en ese resultado / total)
```

Con 1.000 moneditas apostadas al local en el ejemplo, el peso es 0,15 y las cuotas pasan a **1,58 · 4,19 · 4,99**.

Para que nadie pueda mover las cuotas a su favor, cada jugador cuenta como máximo **100 moneditas por resultado**, y el importe de una combinada se reparte entre sus selecciones.

Las cuotas no se guardan en el evento: se calculan cada vez que se muestran. La que cuenta para una apuesta es la que se guarda en su selección al apostar.

## Otros mercados de fútbol

| Mercado | Probabilidad | Ejemplo anterior |
|---|---|---|
| Doble oportunidad (1X, X2, 12) | Suma de las de los dos resultados | 1,19 · 1,93 · 1,26 |
| Más/menos de 2,5 goles | Cada equipo marca según una **distribución de Poisson** con media 1,3 × e^(±0,1 × dif). "Menos de 2,5" = probabilidad de 0, 1 o 2 goles en total | +2,5: 1,89 · −2,5: 1,84 |
| Ambos marcan | P(local marca) × P(visitante marca) | Sí: 1,79 · No: 1,95 |

Estos mercados usan el mismo margen y no tienen ajuste por volumen.
