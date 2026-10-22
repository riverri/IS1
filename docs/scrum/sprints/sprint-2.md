# Sprint 2 · 3/10 – 17/10

> Acta del sprint. La planificación, la review y la retrospectiva son las que escribió el equipo. Se han añadido, con datos del repositorio, el Sprint Backlog (lo que se hizo y dónde está), el seguimiento de las acciones del Sprint 1 y las acciones de mejora.
>
> Esta página se publica sola en la wiki desde `docs/scrum/sprints/sprint-2.md`: para cambiarla, edita ese archivo.

## Sprint Planning
- Realizar testing y pruebas sobre el MVP realizado en el sprint anterior (búsqueda de bugs).
- Solucionar aquellos problemas o errores detectados.
- Completar las historias de usuario con las estimaciones de coste y prioridades entre ellas (lo que nos comentó el profe en la presentación).
- Búsqueda/creación de una API (probablemente con IA) que actualice los resultados deportivos sobre los que se basan nuestras apuestas (e intentar que eso se produzca en tiempo real).
- Añadir detalles estéticos, organizar la IDE de la aplicación y diseñar y colocar los botones donde se accederá a las funciones aún no implementadas.

## Sprint Backlog (según el repositorio)
| Objetivo de la planificación | Qué se hizo | Dónde |
|---|---|---|
| Buscar bugs | Revisión completa del código: 34 fallos documentados (14 del informe y 20 de la revisión) | Issues [#34–#67](https://github.com/riverri/IS1/issues?q=is%3Aissue+label%3Abug) |
| Solucionar los errores | Los 34 fallos corregidos, con una prueba cada uno; issues cerrados | [PR #70](https://github.com/riverri/IS1/pull/70) |
| Estimaciones y prioridades de las historias | Criterios de aceptación de todas las historias en formato Dado / Cuando / Entonces (PR #70). Prioridad del 1 al 5 de cada historia (Carlos Jurado, 16/10). La estimación de horas se hizo ya en el Sprint 3 | [PR #70](https://github.com/riverri/IS1/pull/70), commits del 16/10 en `historias-de-usuario.md` |
| API de resultados deportivos | Ya estaba hecha desde el Sprint 1 (HU-21, iteración 5): sincroniza cada 30 minutos con football-data.org. No da resultados en tiempo real | [iteración 5](https://github.com/riverri/IS1/blob/main/docs/scrum/sprints/iteraciones/iteracion-05.md) |
| Detalles estéticos y botones de lo no implementado | No hay cambios registrados en el repositorio durante el sprint | — |

También se hizo, fuera de la planificación:
- **HU-17, recuperar la contraseña** (5 puntos), con correo y enlace de un solo uso: [PR #72](https://github.com/riverri/IS1/pull/72).
- **Wiki del proyecto**, publicada sola desde `docs/wiki`: [PR #71](https://github.com/riverri/IS1/pull/71).
- **Roles:** el profesor es el Product Owner ([PR #33](https://github.com/riverri/IS1/pull/33)) y Carlos Jurado es el Scrum Master desde el 16/10 ([PR #76](https://github.com/riverri/IS1/pull/76)).
- **Plan de gestión de riesgos** ([PR #76](https://github.com/riverri/IS1/pull/76), issue #73) y descripción del proyecto (Carlos Jurado, 14/10).
- **Ideas para la ampliación**, en la [discusión #74](https://github.com/riverri/IS1/discussions/74).

## Sprint Review
Durante este sprint se han superado las expectativas fijadas para los objetivos, hasta el punto de completar la mayor parte del producto final. Únicamente se han descartado algunos detalles menores, debido a los posibles problemas y riesgos que implicaban y a que resultaban prescindibles para los requisitos del cliente. Se ha realizado la correspondiente presentación en el aula para informar al profesor de los avances y logros alcanzados. Asimismo, se han dedicado varias sesiones a la depuración de errores y al tratamiento de casos extremos, con el fin de evitar posibles fallos que comprometan el correcto funcionamiento de la aplicación. Por último, el equipo ha decidido realizar una sesión de brainstorming para generar nuevas ideas y funcionalidades que incorporar al producto actual en el próximo sprint.

## Sprint Retrospective
Se han cumplido en plazo todos los objetivos del sprint y se han superado las expectativas al dedicar tiempo adicional a tareas como la depuración de errores. El equipo ha terminado de comprender el funcionamiento del sistema de control de versiones GitHub y lo ha utilizado con éxito. Además, el desarrollo se ha ajustado a la metodología Scrum; por ejemplo, se han realizado daily scrums para poner en común los avances y las propuestas de cada miembro. Sin embargo, se ha cometido un error de estimación al subestimar el esfuerzo que requería cada uno de los objetivos de la planificación.

### Seguimiento de las acciones del Sprint 1
| Acción | ¿Se cumplió? |
|---|---|
| Estimar en puntos cada objetivo en la Sprint Planning | **No.** Los objetivos del Sprint 2 no tienen estimación, y la retrospectiva vuelve a señalar un error de estimación |
| Fijar fecha y persona para la exposición en clase | **Sí.** La presentación se hizo en el aula durante el sprint |

### Acciones de mejora para el Sprint 3
Salen de lo que señala la retrospectiva: se repite el error de estimación, y las daily scrums se hicieron pero no quedan registradas.

| Acción | Responsable | Cómo se comprueba |
|---|---|---|
| Cada historia del Sprint Backlog es un issue con milestone «Sprint 3», responsable y estimación (puntos y horas) | Scrum Master (Carlos Jurado) | Al empezar el sprint, todas las historias elegidas tienen su issue completo |
| Al cerrar cada historia, apuntar en su issue las horas reales y compararlas con la estimación en la Sprint Review | Responsable de cada historia | La review del Sprint 3 tiene horas reales frente a estimadas |
| Dejar constancia de cada daily scrum en el acta: una línea por persona (qué hice, qué haré, qué me bloquea) | Scrum Master (Carlos Jurado) | El acta del Sprint 3 tiene sus dailies |
