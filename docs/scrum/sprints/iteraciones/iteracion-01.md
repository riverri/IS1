# Iteración 1 · 01/10/2026 · Sprint 1

> **Iteración de desarrollo del [Sprint 1](../sprint-1.md)** (19/09 – 3/10). Esta acta se escribió como «Sprint 1» con el formato de un sprint, pero no lo es: las doce iteraciones se hicieron el 1 y el 2 de octubre, dentro del Sprint 1. Las retrospectivas y las reviews con el Product Owner son las de cada sprint, no las de cada iteración.

## Planificación (01/10)
- **Hecha por:** Jing Li
- **Objetivo:** un visitante puede ver el catálogo de eventos; un usuario puede registrarse, iniciar sesión y recibir sus moneditas; el creador de apuestas puede dar de alta competiciones, equipos y eventos.
- **Puntos de la iteración anterior:** — (primera iteración) · **Puntos comprometidos:** 23

| Historia | Descripción | Fila backlog | Puntos | Responsable | Estado |
|---|---|---|---|---|---|
| HU-01 | Alta de competiciones, equipos (calidad 0-10) y eventos | 1, 2, 17 | 8 | Jing Li | Hecho |
| HU-11 | Registro de usuario | 5 | 3 | Jing Li | Hecho |
| HU-12 | Inicio de sesión seguro | 5, 8 | 3 | Jing Li | Hecho |
| HU-13 | Consultar saldo | 4, 12 | 2 | Jing Li | Hecho |
| HU-14 | Saldo de bienvenida y recarga gratuita periódica | 4 | 2 | Jing Li | Hecho |
| HU-08 | Catálogo visible sin registrarse; al apostar se pide login | 9 | 2 | Jing Li | Hecho |
| HU-19 | Eventos agrupados por deporte y ordenados por fecha | 9 | 3 | Jing Li | Hecho |

Estimaciones iniciales en puntos de historia (Fibonacci). Se revisarán en Planning Poker cuando se incorpore el resto del equipo.

### Decisiones tomadas
- Saldo de bienvenida: **1000** moneditas. Recarga gratuita: **200** cada **7 días**, que se aplica al iniciar sesión o al consultar la cuenta. Se configura en `application.properties`.
- Un evento enfrenta a dos equipos de la misma competición (local y visitante), pensado para apuestas G/E/P. Los deportes individuales (F1, MotoGP) necesitarán otro tipo de evento más adelante.
- Las cuotas se muestran vacías hasta la iteración 2 (algoritmo de cuotas, fila 3).
- Los partidos son reales y los introduce a mano el creador de apuestas, como indica el MVP. La API de datos (fila 13) queda para más adelante. La base de datos se guarda en archivo para no perderlos al reiniciar.

## Registro del trabajo
- **01/10**: arranque de la iteración, modelo de datos y seguridad. Se quitan los partidos de ejemplo inventados; la base de datos pasa a guardarse en archivo. Se cargan los 20 equipos de LaLiga, los partidos reales de las jornadas 8 y 9 y la jornada 2 de la Champions.

## Resultado (01/10)
- **Historias completadas:** HU-01, HU-08, HU-11, HU-12, HU-13, HU-14, HU-19 (23 puntos).
- **Trabajo añadido durante la iteración:** partidos reales de LaLiga y Champions, base de datos persistente, rediseño visual, escudos y edición de equipos desde Gestión.
- **Historias no completadas:** ninguna.
- **Puntos completados:** 23 puntos.
