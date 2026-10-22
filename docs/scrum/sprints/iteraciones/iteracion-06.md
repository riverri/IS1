# Sprint 6 · 01/10 (cerrado antes de plazo)

## Sprint Planning (01/10)
- **Asistentes:** Jing Li
- **Sprint Goal:** las cuotas tienen en cuenta la forma de los equipos y el dinero apostado, el creador fija los límites de apuesta, y se puede apostar a largo plazo (Balón de Oro, campeón de liga…).
- **Puntos comprometidos:** 18
- **Mejora de la retrospectiva anterior incluida:** _(pendiente de la retrospectiva del Sprint 5)_

| Historia | Descripción | Fila backlog | Puntos | Responsable | Estado |
|---|---|---|---|---|---|
| HU-02 | Factor manual de forma reciente de cada equipo | 3 | 2 | Jing Li | Hecho |
| HU-03 | Cuotas versión 2: forma reciente y ajuste por el dinero apostado | 3 | 3 | Jing Li | Hecho |
| HU-07 | Límites de apuesta editables por el creador: importe mínimo y máximo y selecciones por combinada | 17 | 2 | Jing Li | Hecho |
| HU-44 | Apostar a largo plazo al ganador de un premio o una competición | 28 | 5 | Jing Li | Hecho |
| HU-45 | Crear mercados a largo plazo con sus candidatos y cuotas, cerrarlos y marcar el ganador | 28 | 5 | Jing Li | Hecho |
| — | Migración V3 de la base de datos | — | 1 | Jing Li | Hecho |

### Algoritmo de cuotas (versión 2)
- **Nivel de cada equipo** = calificación + forma × 0,4. La forma va de −2 (muy mala racha) a +2 (muy buena racha) y la fija el creador en *Gestión → Editar equipo*.
- Con los niveles se calculan las probabilidades como en la versión 1: ventaja de jugar en casa, empate solo en fútbol y curva logística para el resto.
- **Ajuste por volumen:** la probabilidad de cada resultado se mezcla con la parte del dinero apostado que va a ese resultado.
  - Peso del volumen = 0,30 × total / (total + 1000). Con 1000 monedas apostadas pesa 0,15; nunca pasa de 0,30.
  - Así baja la cuota del resultado al que apuesta casi todo el mundo y suben las demás, y la casa no queda tan expuesta.
  - Solo cuentan las apuestas activas.
- Al final se aplica el mismo margen de la casa (7 %) y los mismos límites de cuota (1,01–50).
- **Las apuestas ya hechas conservan su cuota.** Si la cuota cambia mientras alguien está apostando, se le avisa para que la acepte, como en las combinadas.

### Límites de apuesta
- Se guardan en la base de datos y los cambia el creador en *Gestión → Límites de apuesta*.
- Por defecto: entre 1 y 500 monedas por apuesta y combinadas de hasta 10 selecciones. Sustituye a la propiedad `apuestas.limites.max-selecciones` del Sprint 4.
- Se aplican a las apuestas simples, a las combinadas y a las de largo plazo. Las páginas de apostar muestran los límites vigentes.

### Apuestas a largo plazo
- **Mercado:** un premio o competición (por ejemplo, *Balón de Oro 2027*) con su deporte, una fecha de cierre y varios candidatos, cada uno con una cuota que fija el creador.
- **Estados:** abierto (admite apuestas hasta la fecha de cierre), cerrado (el creador lo cierra antes), resuelto (tiene ganador) y anulado (se devuelve el importe).
- **Usuario:** en *Largo plazo* elige un candidato y un importe. Las apuestas aparecen en *Mis apuestas* y cuentan para el historial, las estadísticas y el ranking. Se pueden cancelar mientras el mercado esté abierto.
- **Creador:**
  - Crea el mercado con un candidato por línea (`Nombre; cuota`).
  - Puede cambiar cuotas, añadir candidatos, cerrar, marcar el ganador (o corregirlo) y anular.
  - Ve cuánto se ha apostado a cada candidato.
- **No se combinan** con partidos: una apuesta a largo plazo es siempre simple.
- Si un candidato se llama igual que un equipo o deportista del catálogo, se muestra su escudo.
- **Datos iniciales:** campeón de la Champions y de LaLiga 2026/27, Balón de Oro 2027 y Mundial de F1 2026 (este último de ejemplo, como el resto de deportes). Las cuotas son una propuesta inicial que el creador debe revisar.

### Cambios en la base de datos (migración V3)
- Columna `forma` en los equipos (empiezan en *Normal*).
- Tabla `limites`, con los valores por defecto.
- Tablas `mercado` y `candidato`.
- Una selección pasa a ser el resultado de un evento **o** un candidato de un mercado.
- Las bases de datos del Sprint 5 se actualizan solas al arrancar, sin perder apuestas ni saldos (comprobado).

## Dailies
- **01/10**: forma y ajuste por volumen en las cuotas, límites de apuesta, mercados a largo plazo, pantallas, migración V3 y pruebas.

## Sprint Review (01/10)
- **Historias completadas:** HU-02, HU-03 (versión 2), HU-07, HU-44 y HU-45.
- **Historias no completadas:** ninguna.
- **Feedback del PO:**
  - Añadir al backlog tres historias nuevas: HU-46 (editar y borrar eventos), HU-47 (cambiar nombre y contraseña) y HU-48 (perfil público desde el ranking).
  - Siguiente sprint: ficha de equipo, cara a cara, notificaciones y modificar el importe de una apuesta.

## Retrospectiva (01/10)
- **Bien:**
- **A mejorar:**
- **Acciones para el próximo sprint:**
