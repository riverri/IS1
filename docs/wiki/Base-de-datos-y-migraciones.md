# Base de datos y migraciones

## Dónde está
- **En tu ordenador (H2):** en el archivo `datos/apuestas.mv.db`, dentro de la carpeta del proyecto. Para empezar de cero, para la aplicación y borra la carpeta `datos/`.
- **En el servidor:** PostgreSQL en [Neon](https://console.neon.tech) (SQL Editor o pestaña *Tables*).

**Consola de H2** (con la aplicación arrancada en local y la sesión del creador iniciada): http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:file:./datos/apuestas`
- Usuario: `sa`, sin contraseña

## Por qué Flyway
Al principio Hibernate creaba las tablas desde las entidades. Cuando los datos empezaron a guardarse en un archivo, al añadir un campo las bases de datos existentes no tenían la columna y páginas como *Ranking* daban error 500. En el Sprint 5 se pasó a **Flyway**:

- Cada cambio es un script SQL numerado (`V1__esquema_inicial.sql`, `V2__...`).
- Al arrancar, Flyway mira en su tabla `flyway_schema_history` cuáles ya aplicó y ejecuta solo los nuevos, en orden.
- Las bases de datos antiguas se ponen al día solas **sin perder datos**.

## Las migraciones

| Migración | Qué cambia |
|---|---|
| `V1__esquema_inicial` | Tablas `usuario`, `competicion`, `equipo`, `equipo_competiciones`, `evento`, `apuesta`, `seleccion` |
| `V2__identificadores_api` | `id_externo` en equipos y eventos |
| `V3__forma_limites_largo_plazo` | Forma; tablas `limites` (1 / 500 / 10), `mercado` y `candidato` |
| `V4__notificaciones` | Tabla `notificacion` |
| `V5__juego_responsable_y_bajas` | Límites, pausa y "eliminado" en el usuario |
| `V6__interruptor_juego_responsable` | Interruptor en `limites` |
| `V7__jugadores` | Tabla `jugador` |
| `V8__ligas_y_mas_apuestas` | Tablas `liga` y `liga_miembro`; goles en el evento; tipo especial en la selección |
| `V9__concurrencia_y_suspensiones` | `version` en el usuario y `suspendido_por_api` en el evento |
| `V10__recuperar_contrasena` | Tabla `token_recuperacion` (enlaces para recuperar la contraseña) |

## Reglas
- Los scripts están en `db/migration/h2/` y `db/migration/postgresql/`. En PostgreSQL, `V8__esquema_postgresql.sql` crea todo lo de V1–V8 de golpe; desde V9 van los mismos scripts en las dos carpetas.
- Si cambias una entidad, crea un script nuevo (`V11__descripcion.sql`) **en las dos carpetas**.
- **Nunca modifiques un script que ya está en `main`**: otros ordenadores ya lo aplicaron.
- Con `ddl-auto=validate`, si falta una migración la aplicación no arranca y dice qué columna falta. Las pruebas lo detectan antes.
