# Sprint 11 · 16/10 – 30/10

## Sprint Planning (16/10)
- **Asistentes:** Jing Li
- **Sprint Goal:** jugar entre amigos en ligas privadas, apostar a más cosas de cada partido de fútbol y que el creador vea si la casa gana o pierde dinero.
- **Velocidad media de los sprints anteriores:** 18,7 puntos · **Puntos comprometidos:** 17
- **Mejora de la retrospectiva anterior incluida:** _(pendiente de la retrospectiva del Sprint 10)_

| Historia | Descripción | Fila backlog | Puntos | Responsable | Estado |
|---|---|---|---|---|---|
| HU-51 | Ligas privadas con código de invitación y ranking propio | 33 | 5 | Jing Li | Hecho |
| HU-52 | Doble oportunidad, más/menos de 2,5 goles y ambos marcan | 34 | 8 | Jing Li | Hecho |
| HU-54 | Cuentas de la casa para el creador | 35 | 3 | Jing Li | Hecho |
| — | Migración V8 de la base de datos | — | 1 | Jing Li | Hecho |

### Ligas privadas (HU-51)
- Nueva sección **Ligas** en el menú (solo para jugadores; el creador de apuestas no juega).
- **Crear una liga:** se le pone un nombre y se genera un código de 6 caracteres. Se usan letras y números que no se confunden al dictarlos (sin 0/O ni 1/I/L). Quien la crea es su primer miembro.
- **Unirse:** se escribe el código. Da igual escribirlo en mayúsculas o minúsculas y con espacios.
- **Ranking de la liga:** solo con sus miembros y con los mismos criterios que el general (saldo, ganancias y % de aciertos). Desde cada nombre se abre el perfil público del jugador.
- Cada jugador puede estar como mucho en 10 ligas.
- Solo los miembros ven la liga; para los demás no existe.
- Un miembro puede salir de la liga, y quien la creó puede borrarla.
- Los jugadores que se dan de baja dejan de aparecer en el ranking.

### Más tipos de apuesta en fútbol (HU-52)
- En la página de cada partido de fútbol, además del 1X2:
  - **Doble oportunidad:** 1X, X2 y 12.
  - **Goles:** más o menos de 2,5.
  - **Ambos marcan:** sí o no.
- Se pueden apostar solas o añadir al boleto con el botón **Añadir al boleto para combinar**. Se mantiene la regla de una selección por partido.
- **Cuotas:** se calculan con el mismo nivel de los equipos y el mismo margen de la casa (1,07).
  - Doble oportunidad: se suman las probabilidades de los dos resultados.
  - Goles: cada equipo marca según una distribución de Poisson, con una media de 1,3 goles que sube o baja según la diferencia de nivel.
  - Ambos marcan: probabilidad de que marque el local por la de que marque el visitante.
  - Estas cuotas no tienen el ajuste por volumen, que sigue siendo solo del 1X2.
- **Resolución:**
  - En Gestión, en los partidos de fútbol se introduce el **marcador final**; el ganador sale de él.
  - Si solo se marca el ganador, la doble oportunidad se resuelve igual, pero las apuestas de goles y de ambos marcan se **anulan** (cuentan con cuota 1,00).
  - Corregir el marcador vuelve a resolver las apuestas y ajusta los saldos, igual que con el resultado.
  - Con la API, el marcador entra solo si el partido acabó en los 90 minutos. Si hubo prórroga o penaltis, solo se usa el ganador, porque la API suma esos goles. _(Cambiado después: ahora se usa el marcador de los 90 minutos; ver la revisión de fallos.)_
- **Corrección hecha durante el sprint:** las apuestas de estos tipos no tienen resultado 1X2 y rompían el cálculo del dinero apostado a cada resultado. Ahora no entran en ese cálculo, y hay una prueba que lo comprueba.

### Cuentas de la casa (HU-54)
- Nueva página **Gestión → Cuentas de la casa**, con:
  - el total apostado, el pagado, el beneficio y lo que hay en juego;
  - el **margen real** (beneficio / apostado) frente al **teórico** (6,5 %, que es lo que deja el margen de 1,07 en una simple);
  - un desglose por tipo (simples 1X2, simples de los nuevos tipos, combinadas y largo plazo) y otro de las simples por deporte.
- Lo apostado y lo pagado se cuentan solo de las apuestas cerradas (ganadas, perdidas o anuladas). Las canceladas no cuentan, porque se devolvió el importe antes de jugarse.

### Cambios en la base de datos (migración V8)
- Tablas `liga` y `liga_miembro`.
- Columnas `goles_local` y `goles_visitante` en `evento`.
- Columna `especial` en `seleccion`, con el tipo de apuesta cuando no es 1X2.
- Las bases de datos del Sprint 10 se actualizan solas al arrancar (comprobado con una apuesta ya hecha).

## Dailies
- **16/10**: ligas, nuevos tipos de apuesta con sus cuotas y su resolución, marcador desde Gestión y desde la API, cuentas de la casa, migración V8 y pruebas.

## Sprint Review (30/10)
- Historias completadas:
- Historias no completadas (vuelven al backlog):
- Feedback del PO/profesor:

## Retrospectiva (30/10)
- **Bien:**
- **A mejorar:**
- **Acciones para el próximo sprint:**
