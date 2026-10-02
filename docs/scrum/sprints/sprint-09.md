# Sprint 9 · 02/10 – 16/10

## Sprint Planning (02/10)
- **Asistentes:** Jing Li
- **Sprint Goal:** la ficha de cada equipo muestra su evolución en la temporada; cada usuario puede ponerse límites o tomarse una pausa, y eliminar su cuenta cuando quiera.
- **Velocidad media de los sprints anteriores:** 20 puntos · **Puntos comprometidos:** 12
- **Mejora de la retrospectiva anterior incluida:** _(pendiente de la retrospectiva del Sprint 8)_

| Historia | Descripción | Fila backlog | Puntos | Responsable | Estado |
|---|---|---|---|---|---|
| HU-32 | Gráfico de evolución de un equipo en la temporada | 20 | 3 | Jing Li | Hecho |
| HU-10 | Juego responsable: límites diario y semanal y pausa temporal | 27 | 5 | Jing Li | Hecho |
| HU-18 | Eliminar la cuenta | 22 | 3 | Jing Li | Hecho |
| — | Migración V5 de la base de datos | — | 1 | Jing Li | Hecho |

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

### Cambios en la base de datos (migración V5)
- Columnas `limite_diario`, `limite_semanal`, `pausa_hasta` y `eliminado` en la tabla `usuario`.
- Las bases de datos del Sprint 8 se actualizan solas al arrancar (comprobado).

## Dailies
- **02/10**: gráfico de evolución, juego responsable, eliminar la cuenta, migración V5, pruebas y actualización de los diagramas y las métricas.

## Sprint Review (16/10)
- Historias completadas:
- Historias no completadas (vuelven al backlog):
- Feedback del PO/profesor:

## Retrospectiva (16/10)
- **Bien:**
- **A mejorar:**
- **Acciones para el próximo sprint:**
