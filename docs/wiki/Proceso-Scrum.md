# Proceso Scrum

**Scrum** organiza el trabajo en ciclos cortos de duración fija (**sprints**). Al final de cada sprint hay algo que funciona y se puede enseñar. El proceso completo está en [`docs/scrum/proceso.md`](https://github.com/riverri/IS1/blob/main/docs/scrum/proceso.md), basado en el Tema 3 de la asignatura.

## Calendario de sprints

Sprints de dos semanas. El acta de cada uno (planificación, review, retrospectiva y acciones de mejora) está en la sección **Sprints** de esta wiki.

| Sprint | Fechas | Acta |
|---|---|---|
| 1 | 19/09 – 3/10 | [Primer Sprint](Primer-Sprint) · el código se hizo en 12 [iteraciones](https://github.com/riverri/IS1/tree/main/docs/scrum/sprints/iteraciones) el 1 y el 2 de octubre |
| 2 | 3/10 – 17/10 | [Segundo Sprint](Segundo-Sprint) |
| 3 | _por completar_ | [Tercer Sprint](Tercer-Sprint) |

Las actas se editan en el repositorio, en `docs/scrum/sprints/sprint-N.md`, y se publican solas en la wiki. Para el siguiente sprint, copia la [plantilla](https://github.com/riverri/IS1/blob/main/docs/scrum/sprints/plantilla-sprint.md).

## Roles

| Rol | Quién | Qué hace |
|---|---|---|
| **Product Owner** | El profesor (también hace de cliente) | Decide qué se construye y en qué orden |
| **Scrum Master** | Carlos Jurado ([#28](https://github.com/riverri/IS1/issues/28)) | Vela por que se siga Scrum, organiza los eventos y quita obstáculos |
| **Equipo de desarrollo** | Los cinco miembros | Decide cuánto trabajo cabe en el sprint y cómo hacerlo |

## Eventos

| Evento | Cuándo y para qué |
|---|---|
| Sprint Planning | Al empezar: se elige el objetivo (**Sprint Goal**) y las historias (**Sprint Backlog**) |
| Daily Scrum | Cada día, 15 minutos: qué hice, qué haré, qué me bloquea |
| Sprint Review | Al final: se enseña lo hecho al Product Owner |
| Retrospectiva | Tras la review: qué fue bien, qué mejorar y al menos una mejora para el siguiente sprint |

## Artefactos en GitHub
- **Product Backlog:** [`docs/scrum/product-backlog.md`](https://github.com/riverri/IS1/blob/main/docs/scrum/product-backlog.md), 35 filas ordenadas y priorizadas.
- **Sprint Backlog:** issues con la etiqueta del sprint en el tablero de GitHub Projects. Columnas: Product Backlog → Sprint Backlog → En curso → En revisión → Terminado. Como mucho **1 tarea en curso por persona**.
- **Incremento:** la rama `main` al terminar el sprint.

## Definition of Ready
Una historia puede entrar en un sprint si:
- sigue el formato *Como… quiero… para…*;
- tiene criterios de aceptación *Dado… cuando… entonces…*;
- está estimada y mide 13 puntos o menos;
- sus dependencias están hechas.

## Definition of Done
Una historia está terminada si:
- cumple sus criterios de aceptación;
- está en `main` por un pull request revisado por al menos un compañero;
- tiene pruebas automáticas que pasan;
- sus criterios están en la matriz de trazabilidad;
- se ve bien en móvil y ordenador;
- la documentación está al día;
- el Product Owner la ha aceptado en la review.

## Estimación y priorización
- **Puntos de historia** en escala Fibonacci (1, 2, 3, 5, 8, 13, 20): tamaño relativo, no horas. Se estiman con **Planning Poker**.
- **Velocidad:** puntos terminados por sprint. La del Sprint 1 no sirve para planificar, porque casi todo el código se hizo con IA en dos días; hasta tener la de un sprint normal se planifica con las horas estimadas de cada historia ([métricas](https://github.com/riverri/IS1/blob/main/docs/scrum/metricas.md)).
- **MoSCoW:** cada fila del backlog es Must, Should, Could o Won't. El **MVP** (producto mínimo viable) son los Must.
