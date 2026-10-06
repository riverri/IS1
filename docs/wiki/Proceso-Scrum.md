# Proceso Scrum

**Scrum** organiza el trabajo en ciclos cortos de duración fija (**sprints**). Al final de cada sprint hay algo que funciona y se puede enseñar. El proceso completo está en [`docs/scrum/proceso.md`](https://github.com/riverri/IS1/blob/main/docs/scrum/proceso.md), basado en el Tema 3 de la asignatura.

La documentación de cada sprint está en la sección **Sprints** de esta wiki.

## Roles

| Rol | Quién | Qué hace |
|---|---|---|
| **Product Owner** | El profesor (también hace de cliente) | Decide qué se construye y en qué orden |
| **Scrum Master** | Por decidir ([#28](https://github.com/riverri/IS1/issues/28)) | Vela por que se siga Scrum, organiza los eventos y quita obstáculos |
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
- **Velocidad:** puntos terminados por sprint; sirve para planificar.
- **MoSCoW:** cada fila del backlog es Must, Should, Could o Won't. El **MVP** (producto mínimo viable) son los Must.
