# Sprint 9 · 02/10 – 16/10

## Sprint Planning (02/10)
- **Asistentes:** Jing Li
- **Sprint Goal:** la ficha de cada equipo muestra su evolución en la temporada; cada usuario puede ponerse límites o tomarse una pausa, y eliminar su cuenta cuando quiera.
- **Velocidad media de los sprints anteriores:** 20 puntos · **Puntos comprometidos:** 12 (16 con lo añadido durante el sprint)
- **Mejora de la retrospectiva anterior incluida:** _(pendiente de la retrospectiva del Sprint 8)_

| Historia | Descripción | Fila backlog | Puntos | Responsable | Estado |
|---|---|---|---|---|---|
| HU-32 | Gráfico de evolución de un equipo en la temporada | 20 | 3 | Jing Li | Hecho |
| HU-10 | Juego responsable: límites diario y semanal y pausa temporal | 27 | 5 | Jing Li | Hecho |
| HU-18 | Eliminar la cuenta | 22 | 3 | Jing Li | Hecho |
| — | Migraciones V5 y V6 de la base de datos | — | 1 | Jing Li | Hecho |
| HU-10 | Interruptor en Gestión para activar o desactivar el juego responsable | 27 | 1 | Jing Li | Hecho (añadida durante el sprint) |
| HU-09 | Rediseño visual: portada con partido destacado, colores por deporte, cabecera de partido, podio del ranking, acceso y pie de página | — | 3 | Jing Li | Hecho (añadida durante el sprint) |

### Gráfico de evolución (HU-32)
- En la ficha de cada equipo con dos o más partidos jugados.
- **Muestra** los puntos acumulados partido a partido (3 por victoria y 1 por empate), con cada punto coloreado según el resultado.
- Al pasar el ratón por un punto se ven la fecha, el rival y los puntos.
- El SVG se genera en el servidor, sin librerías de JavaScript.

### Juego responsable (HU-10)
- En *Mi cuenta → Juego responsable*.
- **Límites:** importe máximo apostado en 24 horas y en 7 días. Vacío significa sin límite, y el diario no puede ser mayor que el semanal.
  - Cuentan todas las apuestas menos las canceladas.
  - Se aplican a las simples, las combinadas, las de largo plazo y las subidas de importe.
  - Si una apuesta los supera, se rechaza y se explica cuánto lleva apostado.
- **Pausa:** de 1, 7 o 30 días.
  - Durante la pausa no se puede apostar ni subir el importe de una apuesta, pero sí bajarlo o cancelarla.
  - No se puede acortar: si ya hay una pausa más larga, se mantiene.
  - Una franja en la cabecera recuerda hasta cuándo dura.
- **Interruptor en Gestión** (pedido por el PO durante el sprint): el creador puede desactivar el juego responsable en toda la web.
  - Mientras está desactivado no se aplican los límites ni las pausas, y la sección desaparece de *Mi cuenta* y de la cabecera.
  - Lo que cada usuario tenga guardado se conserva y vuelve a aplicarse al activarlo.
- **Decisión:** los límites cambian en el momento, también al subirlos. En las casas de apuestas reales subir un límite tarda unas horas en aplicarse; aquí no hace falta porque no hay dinero real. Queda anotado como posible mejora.

### Eliminar la cuenta (HU-18)
- En *Mi cuenta → Eliminar cuenta*, con la contraseña y una confirmación.
- **Qué pasa:**
  - se cancelan las apuestas que aún se pueden cancelar;
  - se borran los avisos;
  - se **anonimiza** el usuario: email y nombre sustituidos, saldo a cero, contraseña inutilizable y cuenta desactivada.
- La fila se conserva para que cuadren las apuestas ya hechas, así que el historial y las estadísticas globales no cambian.
- El jugador desaparece del ranking y su email queda libre para registrarse de nuevo.
- La cuenta del creador de apuestas no se puede eliminar.

### Rediseño visual (pedido por el PO durante el sprint)
- **Cabecera:**
  - fondo translúcido;
  - la sección en la que estás queda marcada en el menú;
  - en el móvil, el menú es una sola fila que se desliza en horizontal.
- **Portada:** el título lleva un degradado y a la derecha aparece el **partido destacado** (el próximo de fútbol), con sus cuotas para añadirlo al boleto.
- **Color por deporte:** cada deporte tiene su color (fútbol verde, baloncesto naranja, tenis lima, F1 rojo, MotoGP violeta). Se ve en la franja superior de las tarjetas de partidos y de largo plazo, en sus etiquetas y en los accesos de la portada.
- **Página de un partido:** cabecera oscura tipo "entrada", con los escudos, la competición, la jornada y la hora.
- **Ranking:** **podio** con medallas para los tres primeros y avatar con iniciales en el resto.
- **Entrar y registrarse:** a dos columnas, con un panel que resume qué ofrece la web.
- **Pie de página** en todas las páginas, con enlaces y el aviso de que es un juego sin dinero real.
- **Detalles:** botones con relieve, títulos de sección con una marca de color, tarjetas más redondeadas y sombras más suaves.

### Cambios en la base de datos (migraciones V5 y V6)
- **V5:** columnas `limite_diario`, `limite_semanal`, `pausa_hasta` y `eliminado` en la tabla `usuario`.
- **V6:** columna `juego_responsable` en la tabla `limites` (activado por defecto).
- Las bases de datos del Sprint 8 se actualizan solas al arrancar (comprobado).

## Dailies
- **02/10**: gráfico de evolución, juego responsable, eliminar la cuenta, migración V5, pruebas y actualización de los diagramas y las métricas.
- **02/10**: interruptor del juego responsable en Gestión y rediseño visual.

## Sprint Review (16/10)
- Historias completadas:
- Historias no completadas (vuelven al backlog):
- Feedback del PO/profesor:

## Retrospectiva (16/10)
- **Bien:**
- **A mejorar:**
- **Acciones para el próximo sprint:**
