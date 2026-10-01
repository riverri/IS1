# Sprint 5 · 01/10 – 15/10

## Sprint Planning (01/10)
- **Asistentes:** Jing Li
- **Sprint Goal:** los partidos y resultados reales de LaLiga y la Champions llegan solos desde una API y las apuestas se resuelven sin intervención del creador.
- **Puntos comprometidos:** 13
- **Mejora de la retrospectiva anterior incluida:** _(pendiente de la retrospectiva del Sprint 4)_

| Historia | Descripción | Fila backlog | Puntos | Responsable | Estado |
|---|---|---|---|---|---|
| HU-21 | Sincronizar eventos con una fuente externa sin intervención manual | 13 | 8 | Jing Li | Hecho |
| HU-25 | Resolución automática de apuestas con el resultado que llega de la API | 13, 16 | 3 | Jing Li | Hecho |
| — | Escudos oficiales de los equipos desde la API | 13 | 1 | Jing Li | Hecho |
| HU-05 | Aplazados (suspender) y cancelados (anular) desde la API | 13 | 1 | Jing Li | Hecho |

### Cómo funciona
- **API:** se usa la **API v4 de football-data.org**, gratuita para LaLiga, la Champions y las grandes ligas europeas.
- **Qué se consulta:** para cada competición configurada, los partidos desde hace 7 días hasta dentro de 21.
- **Qué se hace con cada partido:**
  - Si no existe, se crea. Si ya existía (creado a mano), se enlaza: mismos equipos y fecha parecida.
  - Si cambia la fecha, se actualiza.
  - Si ha terminado, se introduce el resultado y las apuestas se pagan.
  - Si se aplaza, se suspende. Si se cancela, se anula y se devuelve el importe.
- **Equipos:**
  - Se reconocen por su identificador en la API o por un nombre equivalente ("Club Atlético de Madrid" = "Atlético de Madrid").
  - Los nuevos se crean con su escudo oficial y una calificación de 6,0 que el creador debe revisar.
- **Cuándo se sincroniza:** a los 30 segundos de arrancar y cada 30 minutos, o al pulsar *Sincronizar ahora* en Gestión.
- **La clave de la API** se lee de la variable de entorno `FOOTBALL_DATA_TOKEN` y nunca se guarda en el repositorio.

### Limitaciones conocidas
- **Formato de la API sin probar contra la real.** El código se ha escrito según la documentación de la API v4 y se ha probado con respuestas simuladas. La prueba con la API real se hace en local, con una clave personal.
- **Solo fútbol.** Los demás deportes siguen con eventos de ejemplo, porque no hay una API gratuita equivalente.
- **Varios ordenadores con la misma clave.** Si varias personas sincronizan con la misma clave a la vez, se puede superar el límite de 10 peticiones por minuto.

## Dailies
- **01/10**: cliente de la API, sincronización, tarea programada, botón en Gestión y documentación.

## Sprint Review (15/10)
- Historias completadas:
- Historias no completadas (vuelven al backlog):
- Feedback del PO/profesor:

## Retrospectiva (15/10)
- **Bien:**
- **A mejorar:**
- **Acciones para el próximo sprint:**
