# Sprint 12 · 30/10 – 13/11

## Sprint Planning (30/10)
- **Asistentes:** Jing Li
- **Sprint Goal:** la aplicación funciona en un servidor permanente con sus datos, cada criterio de aceptación está enlazado con su prueba y el trabajo del sprint se sigue en un tablero.
- **Velocidad media de los sprints anteriores:** 18,5 puntos · **Puntos comprometidos:** 10 (más tareas del equipo sin estimar)
- **Mejora de la retrospectiva anterior incluida:** _(pendiente de la retrospectiva del Sprint 11)_

Desde este sprint el Sprint Backlog son los [issues](https://github.com/riverri/IS1/issues) con la etiqueta `sprint-12`, en el tablero de GitHub Projects.

| Issue | Descripción | Puntos | Responsable | Estado |
|---|---|---|---|---|
| [#24](https://github.com/riverri/IS1/issues/24) | Matriz de trazabilidad: criterios de aceptación → pruebas | 3 | Jing Li | Hecho |
| [#25](https://github.com/riverri/IS1/issues/25) | Gestión: dinero apostado a todos los tipos de apuesta de un partido | 2 | Jing Li | Hecho |
| [#26](https://github.com/riverri/IS1/issues/26) | Servidor permanente: base de datos PostgreSQL | 5 | Jing Li | Hecho |
| [#28](https://github.com/riverri/IS1/issues/28) | Decidir el Scrum Master | — | Equipo | Pendiente |
| [#29](https://github.com/riverri/IS1/issues/29) | Rellenar las retrospectivas de los sprints 1–11 | — | Equipo | Pendiente |

En el Product Backlog queda [#27](https://github.com/riverri/IS1/issues/27) (HU-17, recuperar la contraseña).

### Roles
- El profesor actúa como Product Owner y cliente.
- De momento los cinco miembros figuran también como Product Owner (provisional).
- El Scrum Master está por decidir (#28).

### Matriz de trazabilidad (#24)
- Nuevo documento [docs/requisitos/trazabilidad.md](../../requisitos/trazabilidad.md): 44 historias hechas y 92 criterios de aceptación.
  - 88 criterios tienen prueba automática.
  - 3 se comprueban a mano porque dependen del navegador: la web en el móvil, la ganancia potencial calculada al escribir el importe y la confirmación al eliminar la cuenta.
  - 1 no tiene prueba: el mensaje de "no hay enfrentamientos previos".
- Pasa a formar parte de la Definition of Done.

### Dinero apostado a todos los tipos (#25)
- En *Gestión → evento*, la tabla "Dinero apostado" incluye ahora la doble oportunidad, los goles y ambos marcan, con su cuota actual, el número de apuestas y el importe.

### Servidor permanente con PostgreSQL (#26)
- La aplicación funciona con H2 (en local) o con PostgreSQL (en el servidor). Para usar PostgreSQL basta con definir las variables `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`.
- Las migraciones están en dos carpetas: `db/migration/h2` (las V1–V8 de siempre) y `db/migration/postgresql` (el mismo esquema). Flyway elige la carpeta según la base de datos.
- Montaje propuesto, gratuito y sin tarjeta:
  - Render ejecuta la aplicación.
  - Neon guarda la base de datos.
  - UptimeRobot, opcional, visita la web para que no se apague.
  - Los pasos están en el README.
- El CI ejecuta ahora todas las pruebas dos veces: con H2 y con PostgreSQL.
- **Corrección encontrada durante el sprint:**
  - **El fallo:** la lista de *Mis ligas* daba error 500 en la aplicación real. Los miembros de cada liga no se cargaban, y la página los pedía cuando la consulta a la base de datos ya había terminado. Las pruebas no lo detectaban porque se ejecutan dentro de una transacción.
  - **La solución:** los miembros se cargan junto con la liga. Hay una prueba nueva que se ejecuta fuera de una transacción; falla sin la corrección y pasa con ella.
- **Comprobado:**
  - Con un PostgreSQL real: registro, apuestas, resolución con marcador, ligas y cuentas de la casa; todas las páginas sin errores y los datos se conservan al reiniciar.
  - Las bases de datos H2 del Sprint 11 siguen arrancando y conservan sus datos.

### Plantillas con la API real
Es la primera prueba con una clave real de football-data.org; hasta ahora solo se había probado con una API simulada.
- **Partidos:** se descargan bien.
- **Resultado de la prueba:**
  - **Equipos repetidos.** Cuatro equipos tienen otro nombre en la API ("Real Racing Club de Santander", "Como 1907", "PAE AEK", "SK Slavia Praha") y la sincronización los creaba otra vez. Ahora se guarda su identificador de la API desde el primer arranque.
  - **Champions sin jugadores.** En el plan gratuito la Champions no trae jugadores, así que solo los equipos de LaLiga tenían plantilla. Ahora las plantillas se descargan de la liga de cada equipo (Premier, Bundesliga, Serie A, Ligue 1, Liga Portugal y Eredivisie).
  - **Plantillas vacías.** Una plantilla vacía ya no borra los jugadores que ya había.
  - **Ficha del equipo.** Los nombres largos tapaban la nota de cada jugador en la plantilla.
- **Resultado final:** 40 de los 51 equipos de fútbol tienen plantilla (1.063 jugadores) y ningún equipo repetido. Los otros 11 juegan en ligas que el plan gratuito no incluye; sus jugadores se añaden a mano.
- El plan gratuito no da dorsales.

## Dailies
- **30/10**: issues y tablero, matriz de trazabilidad, tabla de dinero apostado, soporte de PostgreSQL y pruebas.

## Sprint Review (13/11)
- Historias completadas:
- Historias no completadas (vuelven al backlog):
- Feedback del PO/profesor:

## Retrospectiva (13/11)
- **Bien:**
- **A mejorar:**
- **Acciones para el próximo sprint:**
