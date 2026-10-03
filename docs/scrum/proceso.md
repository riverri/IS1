# Cómo aplicamos Scrum

> Basado en el Tema 3 (Scrum, G. Méndez) y en las técnicas de estimación ágil (E. P. Concepción).

## Roles
| Rol | Quién | Responsabilidad |
|---|---|---|
| **Product Owner** | El profesor (Gonzalo Rubén Méndez Pozo) | Único responsable del Product Backlog: contenido, orden y claridad. Maximiza el valor del producto. |
| **Scrum Master** | _por decidir_ ([issue #28](https://github.com/riverri/IS1/issues/28)) | Vela por que Scrum se aplique bien. Facilita los eventos, controla los tiempos y elimina impedimentos. |
| **Equipo de Desarrollo** | Los 5 miembros (el SM también desarrolla) | Autoorganizado y multifuncional. Decide cuánto trabajo entra en el sprint y cómo hacerlo. |

El profesor, como Product Owner, decide el orden del Product Backlog y acepta en las Sprint Reviews lo que está terminado.

## Sprint
- Duración fija de **2 semanas**, que no cambia entre sprints. La teoría pone como máximo 1 mes; ajustar al calendario de entregas.
- Cada sprint empieza justo al terminar el anterior.
- Tiene un **Sprint Goal** (objetivo) y produce un **Incremento** "Terminado" y utilizable.

## Eventos
| Evento | Cuándo | Duración (sprint de 2 semanas) | Resultado |
|---|---|---|---|
| **Sprint Planning** | Día 1 | ≤ 2 h | Sprint Goal y **Sprint Backlog**: historias elegidas más el plan, con tareas de ≤ 1 día |
| **Daily Scrum** | Cada día (o 3 veces por semana, en persona o por chat) | 15 min | ¿Qué hice para el objetivo? ¿Qué haré? ¿Hay impedimentos? |
| **Sprint Review** | Último día | ≤ 1 h | Demo del incremento. El PO dice qué está "Terminado" y se actualiza el Product Backlog |
| **Sprint Retrospective** | Tras la review | ≤ 45 min | Qué fue bien, qué mejorar y **al menos una mejora** que entra en el siguiente Sprint Backlog |

Las actas de cada sprint van en `docs/scrum/sprints/sprint-NN.md` (ver la [plantilla](sprints/plantilla-sprint.md)).

## Artefactos y su equivalencia en GitHub
| Artefacto Scrum | En GitHub |
|---|---|
| Product Backlog | [product-backlog.md](product-backlog.md) y un **Issue por historia** (plantilla "Historia de usuario"), en la columna `Product Backlog` del tablero |
| Sprint Backlog | Issues asignados al **Milestone** del sprint ("Sprint 1"…) y a la columna `Sprint Backlog` |
| Incremento | Rama `main` al final del sprint, marcada con un **tag/release** (`sprint-1`, `sprint-2`…) |

## Tablero Kanban (GitHub Projects)
Desde el Sprint 12, cada historia o tarea es un [issue](https://github.com/riverri/IS1/issues) con su etiqueta (`historia`, `tarea`, `documentación`, `sprint-12`…) y está en el tablero de la pestaña *Projects* del repositorio. Un PR que termina un issue lo cierra con `Closes #N` en su descripción.

Columnas: `Product Backlog` → `Sprint Backlog` → `En curso` → `En revisión (PR)` → `Terminado`
- **Límite de trabajo en curso (WIP):** como máximo **1 tarea en curso por persona**. No se empieza otra hasta mover la anterior a revisión.
- El Daily se hace mirando el tablero: ¿qué está bloqueado?, ¿qué avanza más lento de lo esperado?

## Estimación
- **Puntos de historia**, que son relativos y no equivalen a horas. Escala Fibonacci: 1, 2, 3, 5, 8, 13, 20.
- Técnica: **Planning Poker**. El PO explica la historia, cada miembro elige carta en secreto y se revelan a la vez. Si hay mucha diferencia, los extremos explican su voto y se repite.
- Una historia de más de 13 puntos es demasiado grande y se divide.
- **Velocidad** = puntos terminados por sprint. Tras el Sprint 1 sirve para planificar los siguientes.
- Seguimiento con un **burndown**: puntos pendientes por día del sprint.

## Priorización
Método **MoSCoW** (Must / Should / Could / Won't), indicado en el [Product Backlog](product-backlog.md). El MVP son los **Must**.

## Definition of Ready (una historia puede entrar en un sprint si…)
- Sigue el formato *Como… quiero… para…*.
- Tiene criterios de aceptación *Dado… cuando… entonces…*.
- Está estimada en puntos y mide ≤ 13.
- Sus dependencias están terminadas o dentro del mismo sprint.

## Definition of Done (una historia está "Terminada" si…)
- Cumple todos sus criterios de aceptación.
- El código está en `main` mediante un PR revisado y aprobado por al menos 1 compañero.
- Tiene pruebas automáticas de la lógica (cuotas, saldo, resolución…) y pasan.
- Sus criterios de aceptación están en la [matriz de trazabilidad](../requisitos/trazabilidad.md), cada uno con su prueba.
- La web se ve y se usa bien en móvil y en ordenador (HU-09).
- La documentación afectada está actualizada.
- Se ha enseñado en la Sprint Review y el PO la ha aceptado.
