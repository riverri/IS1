# Ejecutar y desplegar

## En tu ordenador
1. Instala **Java 21** (por ejemplo Temurin) y **Git**. Recomendado: **IntelliJ IDEA Community**.
2. `git clone https://github.com/riverri/IS1.git`
3. Arranca:
   - **IntelliJ:** abre la carpeta, abre `ApuestasApplication.java` y pulsa el triángulo verde junto a la clase.
   - **Terminal:** `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).
4. Espera a `Started ApuestasApplication` y abre http://localhost:8080.
5. Usuarios de prueba:
   - Creador: `creador@apuestas.es` / `creador123`
   - Jugador: `usuario@apuestas.es` / `usuario123`
6. Pruebas: `./mvnw test`.

**Si en IntelliJ falta la clase principal** (campo *Main class* en rojo): pon `es.ucm.fdi.is1.apuestas.ApuestasApplication` y el módulo `apuestas`, o arranca desde el triángulo verde de la clase.

Para partidos reales, añade `FOOTBALL_DATA_TOKEN` (ver [API de datos deportivos](API-de-datos-deportivos)).

## Desde otros dispositivos (túnel)
`compartir.bat` (Windows) o `./compartir.sh` sacan un enlace público tipo `https://palabras-al-azar.trycloudflare.com` que funciona desde cualquier red.

1. Para la aplicación en IntelliJ (el script necesita el puerto 8080 libre).
2. En una terminal en la carpeta del proyecto, si quieres partidos reales: `set FOOTBALL_DATA_TOKEN=tu_clave`.
3. Ejecuta `compartir.bat`. Te pedirá una contraseña para el creador (la del README es pública).
4. Comparte el enlace.

Solo funciona con tu ordenador encendido y las dos ventanas abiertas, y el enlace cambia cada vez. Si la red de la facultad bloquea el túnel, usa los datos del móvil.

## Servidor permanente y gratuito

| Servicio | Para qué |
|---|---|
| [Render](https://render.com) | Ejecuta la aplicación con el `Dockerfile` y `render.yaml` |
| [Neon](https://neon.tech) | PostgreSQL permanente (0,5 GB) |
| [UptimeRobot](https://uptimerobot.com) | Visita la web cada 5 minutos para que Render no la apague |

1. **Neon:** crea un proyecto (región Frankfurt), desactiva *Connection pooling* y copia los datos de conexión.
2. **Render:** *Web Service* con el repositorio `riverri/IS1`, rama `main`, Docker, instancia Free. Variables:

| Variable | Valor |
|---|---|
| `CREADOR_PASSWORD` | **Obligatoria.** Contraseña del creador |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://HOST/neondb?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de Neon |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de Neon |
| `FOOTBALL_DATA_TOKEN` | Opcional |

3. **UptimeRobot:** monitor HTTP(s) con el enlace de Render cada 5 minutos.

Cada fusión en `main` se publica sola. No uses la base de datos gratuita de Render: se borra a los 30 días.
