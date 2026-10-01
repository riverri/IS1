# Sprint 1 · 01/10 – 14/10

## Sprint Planning (01/10)
- **Asistentes:** Jing Li
- **Sprint Goal:** un visitante puede ver el catálogo de eventos; un usuario puede registrarse, iniciar sesión y recibir sus moneditas; el creador de apuestas puede dar de alta competiciones, equipos y eventos.
- **Velocidad del sprint anterior:** — (primer sprint) · **Puntos comprometidos:** 23
- **Mejora de la retrospectiva anterior incluida:** — (primer sprint)

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
- Las cuotas se muestran vacías hasta el Sprint 2 (algoritmo de cuotas, fila 3).

## Dailies
- **01/10**: arranque del sprint, modelo de datos y seguridad.

## Sprint Review (14/10)
- Historias completadas:
- Historias no completadas (vuelven al backlog):
- Feedback del PO/profesor:

## Retrospectiva (14/10)
- **Bien:**
- **A mejorar:**
- **Acciones para el próximo sprint:**
