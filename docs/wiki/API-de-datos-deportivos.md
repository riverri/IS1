# API de datos deportivos

[football-data.org](https://www.football-data.org) es un servicio gratuito con datos de fútbol. Con una clave en la variable `FOOTBALL_DATA_TOKEN`, la aplicación se sincroniza a los 30 segundos de arrancar y luego cada 30 minutos.

## Cómo conseguir y poner la clave
1. Regístrate en https://www.football-data.org/client/register. Llega por email.
2. En IntelliJ: *Run → Edit Configurations… → ApuestasApplication → Environment variables*: `FOOTBALL_DATA_TOKEN=tu_clave`.
3. **Nunca la escribas en un archivo del repositorio.**

## Partidos
1. `FootballDataCliente` pide los partidos de cada competición (`PD` LaLiga, `CL` Champions) desde 7 días atrás hasta 21 adelante.
2. `SincronizacionService` busca cada partido por su identificador de la API. Si no existe, crea el evento y, si hace falta, los equipos (con calidad 6,0, que el creador ajusta).
3. Según el estado:

| Estado en la API | Qué hacemos |
|---|---|
| `SCHEDULED`, `TIMED` | Actualizar la hora. Si lo suspendió la propia API, reactivarlo (lo que suspendió el creador sigue suspendido) |
| `FINISHED` | Resultado con el **marcador de los 90 minutos**: se resuelven y pagan las apuestas. Si hubo prórroga y la API no da ese marcador, queda como error para introducirlo a mano |
| `POSTPONED`, `SUSPENDED` | Suspender |
| `CANCELLED` | Anular |
| `IN_PLAY`, `PAUSED` | Nada hasta que termine |

4. Cada partido va en su propia transacción: si uno falla, los demás se guardan y el error aparece en Gestión. Los eventos anulados no se tocan.
5. Si la API no responde en 5 s (conexión) o 20 s (respuesta), se corta y se reintenta en la siguiente sincronización.

## Plantillas
A los 2 minutos de arrancar y una vez al día, `SincronizacionPlantillas` descarga los jugadores de los equipos que ya tenemos, de 7 ligas (LaLiga, Premier, Bundesliga, Serie A, Ligue 1, Liga Portugal y Eredivisie). En el plan gratuito la Champions no trae jugadores. Llegan sin dorsal y con nota 6,0; los de otras ligas se añaden a mano en *Gestión → Editar equipo*.

El plan gratuito permite 10 peticiones por minuto; cada sincronización hace una por competición.
