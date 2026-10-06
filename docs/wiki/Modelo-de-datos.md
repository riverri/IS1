# Modelo de datos

Cada entidad es una tabla. Las relaciones "muchos a muchos" usan una tabla intermedia.

| Entidad | Campos principales | Relaciones |
|---|---|---|
| **Usuario** | email, nombre, passwordHash, rol, saldo, ultimaRecarga, limiteDiario, limiteSemanal, pausaHasta, eliminado, version | Muchas apuestas, notificaciones y ligas |
| **Competicion** | nombre, deporte | Muchos equipos (`equipo_competiciones`) |
| **Equipo** | nombre, deporte, calidad, forma, escudoUrl, idExterno | Varias competiciones; muchos jugadores |
| **Jugador** | nombre, posicion, dorsal, nacionalidad, fechaNacimiento, nota, idExterno | Un equipo |
| **Evento** | fechaHora, estado, resultado, golesLocal, golesVisitante, fase, idExterno, suspendidoPorApi | Una competición, un local y un visitante |
| **Apuesta** | importe, cuota, estado, pagado, fecha | Un usuario; una o varias selecciones |
| **Seleccion** | pronostico (1X2), especial, cuota, estado | Una apuesta y **o** un evento **o** un candidato |
| **Mercado** | nombre, deporte, cierre, estado | Varios candidatos; un ganador |
| **Candidato** | nombre, cuota | Un mercado; opcionalmente un equipo |
| **Liga** | nombre, codigo, creada | Un creador y muchos miembros (`liga_miembro`) |
| **Notificacion** | texto, tipo, fecha, leida | Un usuario |
| **TokenRecuperacion** | hash (SHA-256 del código), caduca, usado | Un usuario |
| **Limites** | importeMinimo, importeMaximo, maxSelecciones, juegoResponsable | Una sola fila |

## Estados

### Evento
- **PROGRAMADO** → admite apuestas hasta su hora de inicio.
- **SUSPENDIDO** → no admite apuestas; se puede reactivar.
- **FINALIZADO** → tiene resultado; se puede corregir.
- **ANULADO** → no se juega; sus selecciones cuentan con cuota 1,00.

### Apuesta
- **ACTIVA** → esperando resultados.
- **GANADA** / **PERDIDA** → resuelta y pagada.
- **ANULADA** → todas sus selecciones anuladas; se devuelve el importe.
- **CANCELADA** → la canceló el jugador antes de empezar.

### Selección
`PENDIENTE`, `ACERTADA`, `FALLADA`, `ANULADA`.

### Mercado
`ABIERTO`, `CERRADO`, `RESUELTO`, `ANULADO`.

Los diagramas UML (casos de uso, clases, estados, secuencias y modelo de datos) están en [`docs/diseno/uml.md`](https://github.com/riverri/IS1/blob/main/docs/diseno/uml.md).
