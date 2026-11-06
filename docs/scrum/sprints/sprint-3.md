# Sprint 3 · _(21/10-03/11)_

> Acta del sprint, por completar por el equipo. Esta página se publica sola en la wiki desde `docs/scrum/sprints/sprint-3.md`: para cambiarla, edita ese archivo.

## Sprint Planning (21/10)
- **Asistentes: Carlos Jurado, Jing Li, David Ortega, Carlos Martín-Salas, Jaime Martín**
- **Sprint Goal: cada miembro sabe explicar al menos un paquete del código.** Se comprueba en la review: cada uno explica el suyo, `RevisionCódigo1.md` cubre los paquetes que anuncia y los issues #75, #91 y #92 están cerrados.
- **Capacidad del equipo (horas): 20**
- **Acciones de la retrospectiva anterior que se incluyen: ser más conscientes del posible uso excesivo de la Inteligencia Artificial**

El sprint no tiene historias de usuario nuevas: son tareas para entender el código, arreglar errores y completar la documentación, así que no llevan puntos.

| Issue | Tarea | Puntos | Horas estimadas | Responsable |
|---|---|---:|---:|---|
| — | Comentar el código para saber con certeza lo que ha hecho la IA | — | 3 | David Ortega |
| — | Esquema de la carpeta `src`: dónde está el código y la interfaz (`docs/ResumenCodigo.md`) | — | 2 | Carlos Martín-Salas |
| — | Resumen de cada paquete y clase del código (`docs/RevisionCódigo1.md`) | — | 4 | Carlos Martín-Salas |
| [#75](https://github.com/riverri/IS1/issues/75) | Arreglo de errores: prohibir nombres de usuario ofensivos | — | 2 | Jaime Martín · Jing Li |
| [#73](https://github.com/riverri/IS1/issues/73) | Gestión de riesgos: nueva versión del plan RSGR | — | 4 | Carlos Jurado · Jing Li |
| [#91](https://github.com/riverri/IS1/issues/91) | Quitar la documentación con erratas (la de los 12 sprints) | — | 1 | Carlos Jurado |
| [#92](https://github.com/riverri/IS1/issues/92) | Reconfigurar el Sprint Backlog del Sprint 1 | — | 2 | Carlos Jurado |
| — | Acta del Sprint 3 y retrospectiva del Sprint 2 | — | 2 | Jaime Martín |
| | **Total** | | **20** | |

**Fuera de la planificación:** David Ortega abrió los pull requests #86 a #90 (libro de movimientos del saldo, anuncios, casino, mercados de predicción y criptomonedas). Sus 19 historias están en [historias-ampliacion-propuestas.md](https://github.com/riverri/IS1/blob/main/docs/requisitos/historias-ampliacion-propuestas.md), pendientes de revisar por el equipo y de aprobar por el Product Owner.

## Daily scrums
_Por completar por el equipo: una línea por persona en cada daily (qué hice, qué haré, qué me bloquea)._

## Sprint Review (03/11)
- **Tareas completadas** (según el repositorio): nombres ofensivos prohibidos ([#75](https://github.com/riverri/IS1/issues/75), PR #84, 22/10) · documentación con erratas retirada ([#91](https://github.com/riverri/IS1/issues/91), 26/10) · comentarios en el código (PR #85, 26/10) · esquema de la carpeta `src` (`docs/ResumenCodigo.md`, 27/10) · nueva versión del plan RSGR ([#73](https://github.com/riverri/IS1/issues/73), PR #95, 1/11) · Sprint Backlog del Sprint 1 reconfigurado ([#92](https://github.com/riverri/IS1/issues/92), 1/11)
- **Tareas no completadas (vuelven al Product Backlog):** resumen de cada paquete (`docs/RevisionCódigo1.md` solo cubre el paquete `equipos`) · esta acta (faltan las dailies, la review y qué fue bien)
- **Horas reales frente a estimadas:**
- **Comentarios del Product Owner:**

## Sprint Retrospective (02/11)

### Seguimiento de las acciones del Sprint 2
| Acción del Sprint 2 | ¿Se cumplió? |
|---|---|
| Cada historia del Sprint Backlog es un issue con milestone «Sprint 3», responsable y estimación (puntos y horas) | **No.** No se crearon milestones y 4 de las 8 tareas no tienen issue. Desde el Sprint 4 el seguimiento se hace en el grafo del equipo |
| Al cerrar cada historia, apuntar en su issue las horas reales y compararlas en la Sprint Review | **No.** La review no tiene horas reales |
| Dejar constancia de cada daily scrum en el acta | **No.** El acta no tiene dailies |

Las tareas del Sprint 3 (comentar el código, esquema de `src`, resumen de las clases, fallos e issues) están en la Sprint Review.

Tras exponer los avances del proyecto con el profesor nos hemos dado cuenta que el ritmo de trabajo y principalmente la manera en la que hemos desarrollado el proyecto no corresponde con la metodología scrum ni en general con ninguna metodología agile. Por todo eso nos hemos comprometido a cambiar completamente la metodología de trabajo para cumplir con los requisitos establecidos por la metodología scrum. Además, buscamos aumentar la comunicación entre los miembros debido a que, a nuestro parecer, gran parte del peso de los problemas de metodología se deben a la falta de comunicación activa por parte de los miembros del grupo en general. Esto es especialmente notable cuando vemos que a la hora de desarrollar el grueso de la aplicación se hizo por un solo miembro del equipo sin consultar al equipo en el proceso, remarcando que esto no es una crítica a un miembro en específico del grupo sino al colectivo en general. 

Por último, hemos visto que le ritmo de desarrollo de los dos primeros sprints (o simplemente del primero) es insostenible en el tiempo, debido que para seguir desarrollando a ese nivel la única opción es mediante el uso prácticamente para la totalidad del proyecto de la IA generativa, por lo que se ha decidido reducir el ritmo de HU completadas por Sprint. Por último queríamos concluir con mencionar que los sprints que restan el equipo se lo va a tomar como un proyecto nuevo aprendiendo de los errores del pasado para no volver a repetirlos. Se volverá a hacer mención a esta reflexión durante el sprint 4.

### Qué fue bien y qué mejorar

### Acciones de mejora para el Sprint 4
| Acción | Responsable | Cómo se comprueba |
|---|---|---|
| Ningún pull request entra en `main` sin la aprobación de otro miembro (proteger la rama `main` en GitHub) | Scrum Master (Carlos Jurado) | Todos los pull requests del Sprint 4 tienen una revisión «Approved» |
| Repetir las tres acciones no cumplidas del Sprint 2: issues con milestone, responsable y horas; horas reales en la review; dailies en el acta | Scrum Master (Carlos Jurado) | El acta del Sprint 4 tiene sus dailies y las horas reales frente a las estimadas |
| Apuntar en el acta los riesgos que suben, bajan o se han hecho reales ([plan RSGR](https://github.com/riverri/IS1/wiki/Gesti%C3%B3n-de-riesgos)) | Scrum Master (Carlos Jurado) | El acta del Sprint 4 tiene un apartado «Seguimiento de riesgos» |
| Primer simulacro de defensa: cada uno explica una parte que no ha programado | Todo el equipo | Se hace en la review del Sprint 4 |
