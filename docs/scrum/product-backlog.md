# Product Backlog (borrador inicial)

> Borrador para discutir con el equipo. Cuando se acuerde, cada historia se pasa a un **Issue** de GitHub con la plantilla "Historia de usuario", y el orden de prioridad se mantiene en el tablero de GitHub Projects.
> Estimaciones en puntos de historia, a revisar en la Sprint Planning.

| ID | Historia | Prioridad | Est. |
|---|---|---|---|
| HU-01 | Como **visitante** quiero **registrarme** para poder apostar. | P1 | 3 |
| HU-02 | Como **usuario** quiero **iniciar y cerrar sesión** para acceder a mi cuenta. | P1 | 3 |
| HU-03 | Como **usuario** quiero **recibir un saldo inicial ficticio** para empezar a apostar sin dinero real. | P1 | 2 |
| HU-04 | Como **visitante** quiero **ver la lista de eventos disponibles** con sus cuotas para elegir sobre cuál apostar. | P1 | 5 |
| HU-05 | Como **usuario** quiero **hacer una apuesta simple** sobre un resultado para ganar saldo si acierto. | P1 | 5 |
| HU-06 | Como **usuario** quiero **crear una apuesta combinada** con varias selecciones y ver el multiplicador total calculado. | P1 | 8 |
| HU-07 | Como **administrador** quiero **crear y editar eventos y cuotas** para mantener la oferta actualizada. | P1 | 5 |
| HU-08 | Como **administrador** quiero **registrar el resultado de un evento** para que las apuestas se resuelvan y se pague a los ganadores. | P1 | 5 |
| HU-09 | Como **usuario** quiero **ver mi historial de apuestas** y su estado (pendiente/ganada/perdida). | P2 | 3 |
| HU-10 | Como **usuario** quiero **consultar estadísticas de un equipo** (últimos resultados, rachas) antes de apostar. | P2 | 8 |
| HU-11 | Como **usuario** quiero **ver la clasificación de una competición**. | P2 | 5 |
| HU-12 | Como **visitante** quiero **filtrar eventos por deporte y competición**. | P2 | 3 |
| HU-13 | Como **usuario** quiero **ver un ranking de usuarios por beneficio** para competir con amigos. | P3 | 3 |
| HU-14 | Como **usuario** quiero **ver gráficas de la evolución de un equipo**. | P3 | 5 |

## Notas técnicas
- **Multiplicador de una combinada** (cuotas decimales): `cuota_total = c1 × c2 × … × cn`; `ganancia = importe × cuota_total`.
  Reglas habituales: no se permiten dos selecciones del mismo evento, y la combinada solo se gana si se aciertan todas las selecciones.
