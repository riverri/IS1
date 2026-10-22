# Sprint 2 · 01/10 (cerrado antes de plazo)

## Sprint Planning (01/10)
- **Asistentes:** Jing Li
- **Sprint Goal:** un usuario ve las cuotas de cada partido, calculadas por nuestro algoritmo, y puede hacer apuestas simples con sus moneditas.
- **Velocidad del sprint anterior:** 23 puntos · **Puntos comprometidos:** 21
- **Mejora de la retrospectiva anterior incluida:** _(pendiente de la retrospectiva del Sprint 1)_

| Historia | Descripción | Fila backlog | Puntos | Responsable | Estado |
|---|---|---|---|---|---|
| HU-03 | Cuotas iniciales 1/X/2 calculadas con margen para la casa | 3 | 8 | Jing Li | Hecho (versión 1) |
| HU-20 | Ver las cuotas de cada resultado en el catálogo | 3, 9 | 3 | Jing Li | Hecho |
| HU-23 | Apuesta simple con parte del saldo | 6, 7 | 5 | Jing Li | Hecho |
| HU-24 | Ver mis apuestas activas | 10, 11 | 3 | Jing Li | Hecho |
| — | Editar calificación y escudo de los equipos desde Gestión | 17 | 2 | Jing Li | Hecho |
| HU-22 | Búsqueda y filtros del catálogo (pestañas por deporte, buscador) | 21 | 3 | Jing Li | Hecho (añadida durante el sprint) |
| HU-26 | Cancelar una apuesta antes de que empiece el evento | 14 | 2 | Jing Li | Hecho (añadida durante el sprint) |
| HU-36 | Ranking de usuarios | 24 | 3 | Jing Li | Parcial: por saldo (añadida durante el sprint) |
| — | Más deportes: baloncesto (ACB, Euroliga, NBA), tenis, Fórmula 1 y MotoGP | 2 | 3 | Jing Li | Hecho |
| — | Escudos dibujados con los colores de cada club cuando no hay imagen | — | 1 | Jing Li | Hecho |

### Algoritmo de cuotas (versión 1)
1. **Diferencia de nivel** = (calidad local + 0,5 por jugar en casa) − calidad visitante.
2. **Probabilidad de empate** (solo fútbol): 30 % si están igualados, bajando 2 puntos por cada punto de diferencia, con un mínimo del 10 %.
3. **El resto se reparte** entre local y visitante con una curva logística según la diferencia.
4. **Margen de la casa del 7 %:** cuota = 1 / (probabilidad × 1,07), redondeada hacia abajo a 2 decimales, entre 1,01 y 50.
   Así la suma de 1/cuota es siempre mayor que 1 (comprobado con una prueba automática que recorre todas las combinaciones de calidades).

Las cuotas se calculan en el momento, así que si el creador cambia la calificación de un equipo, cambian las cuotas de sus partidos pendientes. Cada apuesta guarda la cuota del momento en que se hizo.

### Pendiente para la versión 2 (otro sprint)
- **Factor de forma reciente (HU-02).**
- **Bajada de la cuota según el volumen apostado** a cada resultado (fila 3 del backlog).

### Notas sobre el trabajo añadido
- Los eventos de baloncesto, tenis, Fórmula 1 y MotoGP son **de ejemplo** (no son el calendario real). El creador de apuestas debe sustituirlos por los reales desde Gestión.
- En Fórmula 1 y MotoGP se apuesta a **duelos**: cuál de los dos pilotos acaba por delante.
- El ranking ordena por saldo. Cuando se resuelvan las apuestas (Sprint 3) se podrá ordenar por ganancias y por porcentaje de aciertos, como pide HU-36.

## Dailies
- **01/10**: algoritmo de cuotas, apuesta simple y "Mis apuestas".
- **01/10**: catálogo por deportes con buscador, nuevos deportes, cancelación de apuestas, ranking y escudos dibujados.

## Sprint Review (01/10)
- **Historias completadas:** HU-03 (versión 1), HU-20, HU-23, HU-24, HU-22, HU-26 y HU-36 (por saldo), más los deportes nuevos y los escudos.
- **Historias no completadas:** ninguna de las comprometidas. HU-36 se completa en el Sprint 3, con orden por ganancias y aciertos.
- **Feedback del PO:** añadir apuestas a largo plazo (Balón de Oro, campeón de liga…). Entra en el backlog como fila 28 (HU-44, HU-45), después de la resolución de apuestas.

## Retrospectiva (01/10)
- **Bien:**
- **A mejorar:**
- **Acciones para el próximo sprint:**
