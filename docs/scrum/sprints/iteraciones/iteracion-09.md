# Iteración 9 · 02/10/2026 · Sprint 1

> **Iteración de desarrollo del [Sprint 1](../sprint-1.md)** (19/09 – 3/10). Esta acta se escribió como «Sprint 9» con el formato de un sprint, pero no lo es: las doce iteraciones se hicieron el 1 y el 2 de octubre, dentro del Sprint 1. Las retrospectivas y las reviews con el Product Owner son las de cada sprint, no las de cada iteración.

## Planificación (02/10)
- **Hecha por:** Jing Li
- **Objetivo:** la ficha de cada equipo muestra su evolución en la temporada; cada usuario puede ponerse límites o tomarse una pausa, y eliminar su cuenta cuando quiera.
- **Media de las iteraciones anteriores:** 20 puntos · **Puntos comprometidos:** 12 (16 con lo añadido durante la iteración)

| Historia | Descripción | Fila backlog | Puntos | Responsable | Estado |
|---|---|---|---|---|---|
| HU-32 | Gráfico de evolución de un equipo en la temporada | 20 | 3 | Jing Li | Hecho |
| HU-10 | Juego responsable: límites diario y semanal y pausa temporal | 27 | 5 | Jing Li | Hecho |
| HU-18 | Eliminar la cuenta | 22 | 3 | Jing Li | Hecho |
| — | Migraciones V5 y V6 de la base de datos | — | 1 | Jing Li | Hecho |
| HU-10 | Interruptor en Gestión para activar o desactivar el juego responsable | 27 | 1 | Jing Li | Hecho (añadida durante la iteración) |
| HU-09 | Rediseño visual: portada con partido destacado, colores por deporte, cabecera de partido, podio del ranking, acceso y pie de página | — | 3 | Jing Li | Hecho (añadida durante la iteración) |

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
- **Interruptor en Gestión** (pedido por el PO durante la iteración): el creador puede desactivar el juego responsable en toda la web.
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

### Rediseño visual (pedido por el PO durante la iteración)
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
- Las bases de datos de la iteración 8 se actualizan solas al arrancar (comprobado).

## Registro del trabajo
- **02/10**: gráfico de evolución, juego responsable, eliminar la cuenta, migración V5, pruebas y actualización de los diagramas y las métricas.
- **02/10**: interruptor del juego responsable en Gestión y rediseño visual.

## Resultado (02/10)
- **Historias completadas:** HU-32, HU-10 (con el interruptor en Gestión) y HU-18, más el rediseño visual (16 puntos).
- **Historias no completadas:** ninguna.
- **Peticiones añadidas durante la iteración:** poder entrar en cada equipo y ver sus jugadores, su alineación y su puntuación. Entra en el backlog como fila 32 (HU-49 y HU-50).
