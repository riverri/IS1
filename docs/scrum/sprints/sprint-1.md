# Sprint 1 · 19/09 – 3/10

> Acta del sprint. La planificación, la review, la retrospectiva y el Sprint Backlog son los que escribió el equipo. Se han añadido, con datos del repositorio, el trabajo que se integró en el código y las acciones de mejora de la retrospectiva.
>
> Esta página se publica sola en la wiki desde `docs/scrum/sprints/sprint-1.md`: para cambiarla, edita ese archivo.

## Sprint Planning (objetivos del sprint)
- Redactar las historias de usuario que conforman nuestra primera versión del product backlog.
- Revisar con el equipo las especificaciones necesarias para conformar el MVP (Producto Mínimo Viable).
- Asegurarse de que todos los integrantes del grupo tengan acceso al repositorio.
- Crear una pequeña presentación para exponer nuestro progreso a la clase.
- Añadir licencias, documentación y la wiki donde tengamos registro de cada uno de los Sprints.
- Desarrollar una primera versión del MVP que contenga las especificaciones detalladas en el Sprint Backlog.

## Sprint Backlog
Se ha dividido en dos secciones: una para lo que se tenía previsto desarrollar en este Sprint y otra para lo que se desarrolló de manera adicional a lo planeado.
- HU-01: Alta de Competiciones y equipos
- HU-08: Catálogo de apuestas
- HU-11: Registro de cuenta
- HU-12: Inicio de sesión
- HU-13: Consultar saldo
- HU-18: Eliminar cuenta
- HU-19: Catálogo por deporte y fecha
- HU-46: Editar o modificar un evento

Las historias que se desarrollaron de forma adicional fueron:
- HU-02: Forma reciente de los equipos
- HU-04: Introducir resultado
- HU-05: Anular evento
- HU-23: Apuesta simple
- HU-24: Apuestas activas
- HU-26: Cancelar Apuesta
- HU-27: Cambiar Importe
- HU-47: Cambiar nombre y contraseña

### Trabajo integrado en el código durante el sprint (según el repositorio)
El código se desarrolló en **doce iteraciones** el 1 y el 2 de octubre (pull requests #1 a #30). Cada iteración tiene su acta, con lo que se planificó, lo que se hizo y las decisiones tomadas. En el código quedaron integradas **44 historias**: las 16 de la lista anterior y 28 más.

| Iteración | Fecha | Lo que se integró | Puntos |
|---:|---|---|---:|
| [1](https://github.com/riverri/IS1/blob/main/docs/scrum/sprints/iteraciones/iteracion-01.md) | 01/10 | HU-01, HU-08, HU-11, HU-12, HU-13, HU-14, HU-19 | 23 |
| [2](https://github.com/riverri/IS1/blob/main/docs/scrum/sprints/iteraciones/iteracion-02.md) | 01/10 | HU-03 (versión 1), HU-20, HU-22, HU-23, HU-24, HU-26, HU-36 (por saldo); más deportes y escudos | 33 |
| [3](https://github.com/riverri/IS1/blob/main/docs/scrum/sprints/iteraciones/iteracion-03.md) | 01/10 | HU-04, HU-05, HU-06, HU-25, HU-34, HU-35, HU-36 | 24 |
| [4](https://github.com/riverri/IS1/blob/main/docs/scrum/sprints/iteraciones/iteracion-04.md) | 01/10 | HU-28, HU-29, HU-30, HU-07 (parcial). Con ellas, el **MVP queda completo** | 18 |
| [5](https://github.com/riverri/IS1/blob/main/docs/scrum/sprints/iteraciones/iteracion-05.md) | 01/10 | HU-21, HU-25 y HU-05 desde la API; migraciones con Flyway | 16 |
| [6](https://github.com/riverri/IS1/blob/main/docs/scrum/sprints/iteraciones/iteracion-06.md) | 01/10 | HU-02, HU-03 (versión 2), HU-07, HU-44, HU-45 | 18 |
| [7](https://github.com/riverri/IS1/blob/main/docs/scrum/sprints/iteraciones/iteracion-07.md) | 01/10 | HU-27, HU-31, HU-33, HU-37 | 16 |
| [8](https://github.com/riverri/IS1/blob/main/docs/scrum/sprints/iteraciones/iteracion-08.md) | 02/10 | HU-46, HU-47, HU-48; diagramas UML y métricas | 12 |
| [9](https://github.com/riverri/IS1/blob/main/docs/scrum/sprints/iteraciones/iteracion-09.md) | 02/10 | HU-10, HU-18, HU-32; rediseño visual | 16 |
| [10](https://github.com/riverri/IS1/blob/main/docs/scrum/sprints/iteraciones/iteracion-10.md) | 02/10 | HU-49, HU-50 | 11 |
| [11](https://github.com/riverri/IS1/blob/main/docs/scrum/sprints/iteraciones/iteracion-11.md) | 02/10 | HU-51, HU-52, HU-53 | 17 |
| [12](https://github.com/riverri/IS1/blob/main/docs/scrum/sprints/iteraciones/iteracion-12.md) | 02/10 | Matriz de trazabilidad, servidor permanente con PostgreSQL y tablero (issues #24–#26) | 10 |
| | | **Total** | **214** |

Todas las iteraciones las hizo Jing Li, con ayuda de IA. La velocidad del sprint no sirve para planificar los siguientes; ver [métricas](https://github.com/riverri/IS1/blob/main/docs/scrum/metricas.md).

## Sprint Review
Se han logrado todos los objetivos del sprint salvo la exposición, debido a la falta de tiempo en clase para realizarla, pero queda prevista para el segundo sprint. Además, se ha considerado que los objetivos de este sprint eran demasiado reducidos, por lo que se decidió también crear una primera versión MVP para poder ir complementándola en futuros sprints, por iniciativa de uno de los miembros del equipo. Además, en esta fase se decidieron los objetivos del siguiente Sprint (Sprint Planning), que estarán documentados en la hoja correspondiente.

## Sprint Retrospective
En general el equipo considera que el trabajo de este sprint inicial ha sido muy satisfactorio y se cree que deberíamos seguir por este camino. El principal problema ha sido la falta de previsión con la planificación del sprint, pues al poco de comenzarlo nos dimos cuenta de que lo planificado era demasiado conservador, por lo que decidimos complementarlo con la creación de una versión primitiva de nuestro MVP. Sin embargo, creímos que era un poco pobre y uno de los integrantes propuso completarla. En resumen, creemos que el equipo ha realizado un buen trabajo, pero deberíamos mejorar nuestras estimaciones de objetivos para los sprints.

### Acciones de mejora para el Sprint 2
Salen de los dos problemas que señala la retrospectiva: la planificación se quedó corta y no dio tiempo a la exposición.

| Acción | Responsable | Cómo se comprueba |
|---|---|---|
| En la Sprint Planning, estimar en puntos cada objetivo o historia (Planning Poker) y comprometer solo lo que quepa | Todo el equipo | La planificación del Sprint 2 tiene una estimación por objetivo |
| Al planificar, fijar la fecha y la persona de la exposición en clase | Todo el equipo | La exposición se hace en el Sprint 2 |

## Documentación auxiliar
- Presentación: https://docs.google.com/presentation/d/13Zj3vaPLyBvaLjcsb8fe4QZFU6ODuAaYIT8MFqxArok/edit?usp=sharing
