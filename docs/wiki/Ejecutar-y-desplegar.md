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
| [UptimeRobot](https://uptimerobot.com) | Opcional: avisa si la web se cae. Ya no hace falta para que Render no la apague (keep-alive, abajo) |

1. **Neon:** crea un proyecto (región Frankfurt), desactiva *Connection pooling* y copia los datos de conexión.
2. **Render:** *Web Service* con el repositorio `riverri/IS1`, rama `main`, Docker, instancia Free. Variables:

| Variable | Valor |
|---|---|
| `CREADOR_PASSWORD` | **Obligatoria.** Contraseña del creador |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://HOST/neondb?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de Neon |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de Neon |
| `FOOTBALL_DATA_TOKEN` | Opcional |
| `KEEP_ALIVE_URL` | Opcional: dirección a la que se hace el keep-alive. Si no se define, se usa la que da Render (`RENDER_EXTERNAL_URL`) |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | Opcional: correo para el enlace de recuperar la contraseña |
| `URL_PUBLICA` | Opcional: dirección de la web en el enlace de recuperar la contraseña (en Render se usa la suya) |

3. **Keep-alive (HU-85):** Render apaga la web gratuita tras 15 minutos sin peticiones, y la siguiente visita espera de 30 a 60 segundos a que arranque. Para evitarlo, la propia aplicación hace un GET a `/ping` por su dirección pública cada 10 minutos (`web.KeepAlive`, solo con el perfil `nube`). No hay que configurar nada: Render da la dirección en `RENDER_EXTERNAL_URL`. En los registros se ve `Keep-alive: … no responde` si falla.
4. **UptimeRobot (opcional):** monitor HTTP(s) a `https://TU-WEB.onrender.com/ping` cada 5 minutos. Despierta la web si se llegara a apagar (por ejemplo, si se acaban las 750 horas gratuitas del mes) y avisa por correo si se cae.

## Correo para recuperar la contraseña
Sin servidor de correo, el enlace de *¿Has olvidado tu contraseña?* se escribe en la consola de la aplicación (línea `Enlace para recuperar la contraseña de …`). Para enviarlo de verdad, por ejemplo con Gmail:
1. Activa la verificación en dos pasos y crea una *contraseña de aplicación* en https://myaccount.google.com/apppasswords.
2. Define `MAIL_HOST=smtp.gmail.com`, `MAIL_PORT=587`, `MAIL_USERNAME=tu_cuenta@gmail.com` y `MAIL_PASSWORD=la_contraseña_de_aplicación`.

Cada fusión en `main` se publica sola. No uses la base de datos gratuita de Render: se borra a los 30 días.
