# Sprint 10 · 02/10 – 16/10

## Sprint Planning (02/10)
- **Asistentes:** Jing Li
- **Sprint Goal:** en la ficha de cada equipo se ven sus jugadores con su nota y la alineación probable; las plantillas llegan de la API y el creador puede ajustarlas.
- **Velocidad media de los sprints anteriores:** 20 puntos · **Puntos comprometidos:** 11
- **Mejora de la retrospectiva anterior incluida:** _(pendiente de la retrospectiva del Sprint 9)_

| Historia | Descripción | Fila backlog | Puntos | Responsable | Estado |
|---|---|---|---|---|---|
| HU-49 | Plantilla y alineación probable en la ficha del equipo | 32 | 5 | Jing Li | Hecho |
| HU-50 | Descarga de plantillas desde la API y gestión de jugadores y notas | 32 | 5 | Jing Li | Hecho |
| — | Migración V7 de la base de datos | — | 1 | Jing Li | Hecho |
| — | Corrección: la ficha se cortaba para los visitantes sin sesión | — | — | Jing Li | Hecho (encontrado durante el sprint) |

### Plantilla y alineación probable (HU-49)
- **Alineación probable**, en la ficha de los equipos de fútbol con jugadores suficientes:
  - un campo con un **4-3-3**: en cada posición salen los de mejor nota y, a igual nota, el dorsal más bajo (los titulares suelen llevar del 1 al 11);
  - cada jugador muestra su dorsal, su apellido y su nota (el nombre completo aparece al pasar el ratón);
  - al lado, la nota media del once.
- **Plantilla:** agrupada en porteros, defensas, centrocampistas y delanteros, con el dorsal, la nacionalidad, la edad y la nota (en verde desde 8, en ámbar desde 6,5).
- Si el equipo aún no tiene jugadores, la ficha explica cómo se añaden.

### Plantillas desde la API y gestión (HU-50)
- **Descarga:** se usa `GET /competitions/{código}/teams` de football-data.org, con una petición por competición (LaLiga y Champions).
  - Solo se rellenan los equipos que ya tenemos (se reconocen por su identificador en la API o por el nombre); no se crean equipos.
  - Se descarga al poco de arrancar, una vez al día y con el botón **Descargar plantillas** de Gestión.
- **Al actualizar:**
  - los jugadores nuevos entran con nota 6,0;
  - los que ya estaban actualizan su dorsal, su posición y su equipo (los fichajes cambian de equipo solos), **sin tocar su nota**;
  - los que ya no están en la plantilla se quitan;
  - los jugadores añadidos a mano no se tocan.
- **La posición** se traduce desde la de la API ("Goalkeeper", "Centre-Back", "Defensive Midfield", "Left Winger"…).
- **En Gestión → Editar equipo:**
  - lista de jugadores con su nota editable y un botón para quitarlos;
  - formulario para añadir jugadores (nombre, posición, dorsal, nota y nacionalidad).
- **Sin la clave de la API** los equipos empiezan sin jugadores. No se cargan plantillas inventadas para no mostrar datos que no sean reales.

### Corrección encontrada durante el sprint
- **El fallo:** las tarjetas de partido consultan el boleto, que vive en la sesión. Para un visitante sin sesión, la sesión se intentaba crear a mitad de página. En páginas largas (la ficha con la plantilla), parte de la respuesta ya se había enviado, la creación fallaba y la página se cortaba.
- **La solución:** los visitantes no tienen boleto, así que ya no se les crea sesión. Hay un test que lo comprueba.

### Cambios en la base de datos (migración V7)
- Tabla `jugador`: equipo, nombre, posición, dorsal, nacionalidad, fecha de nacimiento, nota e identificador de la API.
- Las bases de datos del Sprint 9 se actualizan solas al arrancar (comprobado).

## Dailies
- **02/10**: modelo de jugadores, alineación, descarga de la API, pantallas, gestión, migración V7, corrección de la sesión y pruebas.

## Sprint Review (16/10)
- **Historias completadas:** HU-49 y HU-50, más la migración V7 (11 puntos), y la corrección de la ficha para los visitantes.
- **Historias no completadas:** ninguna.
- **Feedback del PO:** entran en el backlog las ligas privadas entre amigos (fila 33, HU-51), más tipos de apuesta en fútbol (fila 34, HU-52) y un panel con las cuentas de la casa (fila 35, HU-54). La recuperación de contraseña (HU-17) se deja para más adelante.

## Retrospectiva (16/10)
- **Bien:**
- **A mejorar:**
- **Acciones para el próximo sprint:**
