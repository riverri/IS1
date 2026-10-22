# Iteración 3 · 01/10/2026 · Sprint 1

> **Iteración de desarrollo del [Sprint 1](../sprint-1.md)** (19/09 – 3/10). Esta acta se escribió como «Sprint 3» con el formato de un sprint, pero no lo es: las doce iteraciones se hicieron el 1 y el 2 de octubre, dentro del Sprint 1. Las retrospectivas y las reviews con el Product Owner son las de cada sprint, no las de cada iteración.

## Planificación (01/10)
- **Hecha por:** Jing Li
- **Objetivo:** el creador de apuestas introduce el resultado de un evento y las apuestas se resuelven y se pagan solas; el usuario ve su historial y sus estadísticas.
- **Puntos comprometidos:** 24

| Historia | Descripción | Fila backlog | Puntos | Responsable | Estado |
|---|---|---|---|---|---|
| HU-04 | Introducir y corregir el resultado de un evento | 16, 17 | 5 | Jing Li | Hecho |
| HU-25 | Resolución automática de las apuestas y pago de las ganancias | 16 | 5 | Jing Li | Hecho |
| HU-05 | Suspender, reactivar y anular eventos (con devolución) | 17 | 3 | Jing Li | Hecho |
| HU-06 | Ver cuánto se ha apostado a cada resultado | 17 | 2 | Jing Li | Hecho |
| HU-34 | Historial de apuestas | 19 | 2 | Jing Li | Hecho |
| HU-35 | Estadísticas personales: % de aciertos y rentabilidad | 19 | 3 | Jing Li | Hecho |
| HU-36 | Ranking por saldo, ganancias o % de aciertos | 24 | 2 | Jing Li | Hecho (completada) |
| — | Añadir al backlog las apuestas a largo plazo (HU-44, HU-45) | 28 | 2 | Jing Li | Hecho |

### Decisiones tomadas
- **Corrección de resultados:** al cambiar el resultado se deshace el pago anterior y se paga a los nuevos ganadores. Si un usuario ya se había gastado una ganancia que se le retira, su saldo puede quedar en negativo.
- **Introducir el resultado antes de que empiece el evento:** se permite, avisando con una confirmación, para poder hacer demostraciones del ciclo completo sin esperar a que se jueguen los partidos.
- **Al suspender un evento** desaparece del catálogo y no admite apuestas ni cancelaciones hasta que se reactive.
- **Las apuestas canceladas o anuladas** no cuentan para las estadísticas ni para el ranking.

## Registro del trabajo
- **01/10**: resolución de apuestas, suspensión y anulación, historial, estadísticas y ranking completo.

## Resultado (01/10)
- **Historias completadas:** HU-04, HU-05, HU-06, HU-25, HU-34, HU-35, HU-36 (24 puntos).
- **Historias no completadas:** ninguna.
- **Peticiones añadidas durante la iteración:** siguiente prioridad, las apuestas combinadas (MVP) antes que las de largo plazo.
