# Cómo aplicamos Scrum

## Roles
- **Product Owner (PO)**: mantiene y prioriza el Product Backlog y acepta o rechaza historias en la Sprint Review.
- **Scrum Master (SM)**: facilita las reuniones, elimina impedimentos y vela por que se siga el proceso.
- **Equipo de desarrollo**: todos (PO y SM incluidos, por el tamaño del equipo).

> El profesor puede hacer de "cliente". Confirmar con él si alguno de los roles lo asigna la asignatura.

## Sprints
- Duración propuesta: **2 semanas** (ajustar al calendario de entregas de la asignatura).
- Cada sprint tiene un **objetivo** y un conjunto de historias del backlog.
- Las actas de cada sprint van en `docs/scrum/sprints/sprint-NN.md` (plantilla: [sprints/plantilla-sprint.md](sprints/plantilla-sprint.md)).

## Ceremonias
| Ceremonia | Cuándo | Duración | Resultado |
|---|---|---|---|
| Sprint Planning | Inicio del sprint | ≤ 1 h | Objetivo del sprint + historias elegidas, divididas en tareas |
| Daily (asíncrona) | 2–3 veces por semana | 15 min o mensaje | ¿Qué hice? ¿Qué haré? ¿Algo me bloquea? |
| Sprint Review | Fin del sprint | ≤ 45 min | Demo de lo terminado; el PO acepta o rechaza |
| Retrospectiva | Tras la review | ≤ 30 min | Qué fue bien, qué mejorar y acciones concretas |

## Herramientas en GitHub
- **Issues** = historias de usuario, tareas y bugs (con las plantillas de `.github/ISSUE_TEMPLATE`).
- **GitHub Projects** (tablero) con columnas: `Product Backlog` → `Sprint Backlog` → `En curso` → `En revisión` → `Hecho`.
- **Milestones** = sprints (Sprint 1, Sprint 2…), con fecha de fin.
- **Labels**: `historia`, `tarea`, `bug`, `docs`, y prioridad `P1`/`P2`/`P3`.
- **Estimación**: puntos de historia (1, 2, 3, 5, 8, 13), anotados en el issue.

## Definition of Ready (una historia puede entrar en un sprint si…)
- Tiene el formato "Como… quiero… para…".
- Tiene criterios de aceptación verificables.
- Está estimada y es lo bastante pequeña para un sprint.

## Definition of Done (una historia está terminada si…)
- El código está en `main` mediante un PR revisado por al menos 1 compañero.
- Cumple todos sus criterios de aceptación.
- Tiene pruebas (cuando aplique) y estas pasan.
- La documentación afectada está actualizada.
- Se ha enseñado en la Sprint Review.
