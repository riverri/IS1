# Sprint 4 · 04/11 – 17/11

> Acta del sprint, en curso. Lo que sale del repositorio está rellenado; lo que pasa en las reuniones lo completa el equipo. Esta página se publica sola en la wiki desde `docs/scrum/sprints/sprint-4.md`: para cambiarla, edita ese archivo.

## Sprint Planning (05/11)
- **Sprint Goal:** Nuestro principal objetico es que `main` no tenga los fallos encontrados en la revisión de código y que la ampliación quede escrita y lista para que el Product Owner decida qué entra. Se amplía el Product Backlog con las nuevas historias de usuario (de HU-74 hasta HU-84) provisionalmente en un archivo. Además se desarrollan algunas de las ampliaciones del proyecto ya redactadas 

- **Acciones de la retrospectiva anterior que se incluyen:** las cuatro del [Sprint 3](https://github.com/riverri/IS1/blob/main/docs/scrum/sprints/sprint-3.md): revisión de cada pull request, issues con horas y dailies, seguimiento de riesgos en el acta y primer simulacro de defensa.

## Sprint Backlog
Se recopila en la siguiente tabla nuestras previsiones para este Sprint:

| Issue | Historia o tarea | Puntos | Horas estimadas | Responsable |
|---|---|---|---|---|
| — | Revisión del código de `main`: un arreglo con su prueba por cada fallo encontrado | — | | David Ortega |
| — | Integrar en orden las ramas de la ampliación ([#86](https://github.com/riverri/IS1/pull/86) a [#90](https://github.com/riverri/IS1/pull/90)) | — | | David Ortega |
| — | HU-74 · Baccarat | 5 | | David Ortega |
| — | HU-75 · Cobrar antes de tiempo (cash-out) | 8 | | David Ortega |
| — | HU-76 · Riesgo de la casa por evento | 5 | | David Ortega |
| — | HU-77 · Correo al ganar una apuesta | 3 | | David Ortega |
| — | HU-78 a HU-80 · Quiniela entre jugadores | 11 | | David Ortega |
| — | HU-81 a HU-83 · Liga fantasy | 12 | | David Ortega |
| — | HU-84 · Ranking por tipo de juego | 3 | | David Ortega |
| — | Grafo del equipo para seguir el Sprint Backlog ([decisión 0003](https://github.com/riverri/IS1/blob/main/docs/decisiones/0003-seguimiento-en-el-grafo.md)) | — | | David Ortega |
| — | Revisión de la documentación frente a la teoría de la asignatura | — | | David Ortega |
| | **Total** | **47** | | |

Las historias HU-74 a HU-84 están en [historias-ampliacion-propuestas.md](https://github.com/riverri/IS1/blob/main/docs/requisitos/historias-ampliacion-propuestas.md#sprint-4-hu-74-a-hu-84). Son una propuesta: no se empieza su desarrollo hasta que el Product Owner las apruebe y otro miembro del equipo lo revise. Queda pendiente de hacer la fusión del archivo original con el de las nuevas propuestas en el siguiente Spint (Sprint 5) ya que se ha decido exponer las nuevas propuestas al Product Owner en la presentación final del Sprint.


## Daily scrums
Por el chat del grupo, los días que se trabaja en el proyecto ([proceso](https://github.com/riverri/IS1/blob/main/docs/scrum/proceso.md)). Una línea por persona: qué hice, qué haré y qué me bloquea.
Además es la manera que tienen los miembros del equipo de comunicar los avances o cambios que se realizan durante el desarrollo del proyecto. 



## Sprint Review (17/11)
> Se completa en la review. A 06/11 consta en el repositorio:

- **Historias completadas:** ninguna aceptada todavía. HU-56 a HU-59 y HU-63 a HU-73 están integradas en `main` desde el 06/11, junto con las correcciones de la revisión de código, en un solo pull request.
- **Preparado para revisar:** las 15 historias integradas, el código de HU-55, HU-60 a HU-62 y HU-74 a HU-84 (aún no subido) y esta documentación.
- **Horas reales frente a estimadas:** _cada responsable_
- **Comentarios del Product Owner:** _en la review_

## Sprint Retrospective (_fecha_)

### Seguimiento de las acciones del Sprint 3
| Acción del Sprint 3 | ¿Se cumplió? |
|---|---|
| Ningún pull request entra en `main` sin la aprobación de otro miembro | **No, por ahora.** El 04/11 entraron en `main` tres cambios del acta del Sprint 3 sin pull request; el 06/11, esta revisión de la documentación, también sin pull request, y el pull request de la integración de #86 a #90, sin la aprobación de otro miembro. Se vuelve a comprobar al cerrar el sprint |
| Issues con milestone, responsable y horas; horas reales; dailies en el acta | Los milestones se sustituyen por el grafo del equipo ([decisión 0003](https://github.com/riverri/IS1/blob/main/docs/decisiones/0003-seguimiento-en-el-grafo.md)). Horas reales y dailies: _al cerrar el sprint_ |
| Apuntar en el acta los riesgos que suben o bajan | **Sí.** Ver el apartado siguiente |
| Primer simulacro de defensa | _En la review del 17/11_ |

### Seguimiento de riesgos
Según el [plan RSGR](https://github.com/riverri/IS1/wiki/Gesti%C3%B3n-de-riesgos).

| Riesgo | Cambio | Por qué | Acción |
|---|---|---|---|
| R21 · Perder la noción de lo que desarrolla la IA | Sube | En este sprint se han programado con IA 11 historias nuevas, unas 4.500 líneas | No se suben sin la revisión de otro miembro que sepa explicarlas |
| R01 · Participación desigual | Sube | Hasta ahora, todo el trabajo del sprint es de una persona | Repartir entre los cinco la revisión de los pull requests de código |
| R16 · Algo que el Product Owner no valora | Sube | HU-56 a HU-59 y HU-63 a HU-73 están en `main` sin que el Product Owner las haya aprobado | Enseñárselas en la review del 17/11; si alguna no la acepta, se quita o se cambia |
| R10 · Directo y cash-out difíciles | Baja | El cash-out (HU-75) está programado sin apuestas en directo | — |
| R06 · Documentación que no refleja la realidad | Baja | Se han corregido las actas y las cifras que no cuadraban | Mantener el acta al día en cada daily |

### Qué fue bien y qué mejorar
_Por completar por el equipo en la retrospectiva._

### Acciones de mejora para el Sprint 5
| Acción | Responsable | Cómo se comprueba |
|---|---|---|
| | | |
