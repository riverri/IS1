# IS1 · Web de apuestas deportivas

Proyecto de **Ingeniería del Software I** (doble grado Informática–Matemáticas, UCM, curso 2026/27).
Profesor: Gonzalo Rubén Méndez Pozo.

## Equipo

| Miembro | Rol Scrum (provisional) |
|---|---|
| Carlos Martín-Salas | Product Owner · Equipo de Desarrollo |
| Jaime Martín | Product Owner · Equipo de Desarrollo |
| David Ortega | Product Owner · Equipo de Desarrollo |
| Jing Li | Product Owner · Equipo de Desarrollo |
| Carlos Jurado | Product Owner · Equipo de Desarrollo |

> El profesor actúa como Product Owner y cliente. De momento los cinco compartimos también ese rol, y el **Scrum Master** está por decidir ([issue #28](https://github.com/riverri/IS1/issues/28)). Ver [docs/scrum/proceso.md](docs/scrum/proceso.md).

**Tablero del sprint:** las tareas y las historias pendientes están en los [issues](https://github.com/riverri/IS1/issues) y en el tablero de GitHub Projects del repositorio (pestaña *Projects*).

## ¿Qué es?

Una web donde los usuarios apuestan con **moneditas virtuales (sin dinero real)** sobre eventos deportivos:

- **Cuotas calculadas por nuestro algoritmo** a partir de la calidad y la forma de cada equipo y del volumen apostado.
- Apuestas simples, **combinadas** (multiplicador = producto de las cuotas) y **a largo plazo** (Balón de Oro, campeón de liga…), resueltas automáticamente al introducir el resultado o el ganador.
- En fútbol, además del 1X2: **doble oportunidad, más/menos de 2,5 goles y ambos marcan**.
- **Banco de estadísticas** para consultar la evolución de los equipos, su plantilla y su alineación probable antes de apostar.
- **Ranking** general y **ligas privadas** para competir con los amigos.
- **Panel del creador de apuestas** para gestionar eventos y mercados, introducir resultados y ver las cuentas de la casa.
- **Datos reales** de LaLiga y la Champions con la API de football-data.org.

Más detalle en [docs/requisitos/vision.md](docs/requisitos/vision.md).

## Estructura del repositorio

```
.
├── docs/
│   ├── requisitos/
│   │   ├── vision.md                  # Visión del producto
│   │   ├── historias-de-usuario.md    # Historias de usuario con sus criterios de aceptación
│   │   └── trazabilidad.md            # Qué prueba comprueba cada criterio de aceptación
│   ├── scrum/
│   │   ├── proceso.md                 # Cómo aplicamos Scrum
│   │   ├── product-backlog.md         # Product Backlog (orden, estimación, MoSCoW y estado)
│   │   ├── sprints/                   # Actas de cada sprint (planning, dailies, review y retro)
│   │   ├── metricas.md                # Velocidad y progreso
│   │   └── graficas/                  # Gráficas de las métricas y el script que las genera
│   ├── diseno/uml.md                  # Diagramas UML (casos de uso, clases, estados, secuencia, datos)
│   ├── decisiones/                    # Decisiones técnicas (stack, arquitectura…)
│   └── recursos.md                    # Enlaces de teoría y de Git
├── src/main/java/                     # Código Java (Spring Boot), un paquete por parte del dominio
├── src/main/resources/                # Plantillas HTML, CSS, configuración y migraciones de la base de datos
├── src/test/java/                     # Pruebas (JUnit)
├── pom.xml                            # Dependencias (Maven)
├── compartir.bat, compartir.sh        # Jugar desde otros ordenadores con un enlace público
├── Dockerfile, render.yaml            # Publicar la aplicación en Internet (Render + Neon)
├── .github/                           # Plantillas de issues y pull requests, y CI
└── CONTRIBUTING.md                    # Cómo trabajamos con Git y GitHub
```

## Tecnología

Java 21 · Spring Boot · Spring Security · Thymeleaf · Spring Data JPA + H2 · Flyway · Maven · JUnit. El porqué está en [docs/decisiones/0001-stack-tecnologico.md](docs/decisiones/0001-stack-tecnologico.md).

## Cómo ejecutarlo

Requisito: tener instalado un **JDK 21**, por ejemplo [Eclipse Temurin 21](https://adoptium.net/). Maven no hace falta: va incluido como `mvnw`.

```bash
git clone https://github.com/riverri/IS1.git
cd IS1
./mvnw spring-boot:run        # en Windows: mvnw.cmd spring-boot:run
```

Abrir http://localhost:8080.

**Usuarios de prueba** (se crean en el primer arranque, junto con competiciones y equipos; las contraseñas se pueden cambiar con las variables `CREADOR_PASSWORD` y `USUARIO_PASSWORD`):

| Rol | Email | Contraseña |
|---|---|---|
| Creador de apuestas (acceso a *Gestión*) | `creador@apuestas.es` | `creador123` |
| Usuario | `usuario@apuestas.es` | `usuario123` |

## Jugar desde otros ordenadores (túnel)

La aplicación se ejecuta en un solo ordenador y los demás entran desde el navegador (también desde el móvil) con un enlace público, sin instalar nada. Todos juegan sobre la misma base de datos: mismo ranking, mismos eventos.

**Windows:** doble clic en `compartir.bat` (o `compartir.bat` desde la terminal, en la carpeta del proyecto).
1. La primera vez descarga `cloudflared.exe` (no se sube a GitHub).
2. Abre la aplicación en otra ventana y espera a que arranque (la primera vez tarda un poco).
3. Saca un enlace del tipo `https://palabras-al-azar.trycloudflare.com`. Ese es el que se comparte.

**macOS / Linux:** `brew install cloudflared` (o el paquete de su web) y después `./compartir.sh`.

- El enlace solo funciona mientras ese ordenador esté encendido y con las dos ventanas abiertas, y **cambia cada vez** que se lanza.
- Usa el perfil `compartir`, que desactiva la consola de H2: no tiene contraseña y con el túnel cualquiera podría entrar en la base de datos. **No compartas nunca la aplicación arrancada desde IntelliJ sin ese perfil.**
- Si la red de la facultad bloquea el túnel, prueba con los datos del móvil.

## Publicarla en Internet (servidor permanente y gratuito)

Para entrar desde cualquier sitio (también desde el móvil) sin tener el ordenador encendido. Se usan tres servicios gratuitos, ninguno pide tarjeta:

| Servicio | Para qué |
|---|---|
| [Render](https://render.com) | Ejecuta la aplicación. El repositorio ya trae el `Dockerfile` y `render.yaml`. |
| [Neon](https://neon.tech) | Base de datos PostgreSQL permanente (0,5 GB, de sobra para el proyecto). |
| [UptimeRobot](https://uptimerobot.com) (opcional) | Visita la web cada 5 minutos para que Render no la apague. |

**1. Base de datos (Neon)**
1. Entra en https://neon.tech con tu cuenta de GitHub y crea un proyecto (región *Europe (Frankfurt)*).
2. En *Connect* desactiva *Connection pooling* y copia los datos de conexión. Neon los da como `postgresql://USUARIO:CONTRASEÑA@HOST/neondb?sslmode=require`.

**2. Aplicación (Render)**
1. Entra en https://render.com con tu cuenta de GitHub. Crea un *Web Service* con el repositorio `riverri/IS1`, rama `main`, lenguaje *Docker* e instancia *Free* (o *New → Blueprint*, que lo rellena desde `render.yaml`).
2. En *Environment* añade:

| Variable | Valor |
|---|---|
| `CREADOR_PASSWORD` | **Obligatoria.** La contraseña de `creador@apuestas.es` (la del README es pública). |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://HOST/neondb?sslmode=require` (el `HOST` de Neon) |
| `SPRING_DATASOURCE_USERNAME` | el `USUARIO` de Neon |
| `SPRING_DATASOURCE_PASSWORD` | la `CONTRASEÑA` de Neon |
| `USUARIO_PASSWORD` | Opcional: crea también `usuario@apuestas.es` con esta contraseña. |
| `FOOTBALL_DATA_TOKEN` | Opcional: la clave de la API para los partidos reales. |

3. Despliega. La primera vez tarda unos minutos. Las tablas las crea Flyway solo, y queda en un enlace fijo del tipo `https://apuestas-is1.onrender.com`.

Cada vez que se fusiona algo en `main`, Render la vuelve a publicar sola. Los datos siguen en Neon.

**3. Que no se apague (UptimeRobot, opcional)**

Render apaga las aplicaciones gratuitas tras 15 minutos sin visitas, y la siguiente visita tarda alrededor de un minuto. Para evitarlo, crea en UptimeRobot un monitor *HTTP(s)* con el enlace de Render cada 5 minutos. El plan gratuito de Render da 750 horas al mes, suficientes para tener un servicio encendido todo el mes.

**Notas**
- Sin las variables `SPRING_DATASOURCE_*` funciona igual, pero con H2: los datos se borran en cada reinicio.
- No uses la base de datos PostgreSQL gratuita de Render: se borra a los 30 días.
- Usa el perfil `nube`: la consola de H2 está desactivada y, si falta `CREADOR_PASSWORD`, la aplicación no arranca.

## Funcionalidades disponibles

| Página | Qué hace | Historias |
|---|---|---|
| `/eventos` | Catálogo público con cuotas 1/X/2, pestañas por deporte (fútbol, baloncesto, tenis, F1, MotoGP) y buscador | HU-08, HU-19, HU-20, HU-22 |
| `/eventos/{id}/apostar` | Apuesta simple directa: elegir resultado e importe, con la ganancia potencial. En fútbol también doble oportunidad, más/menos de 2,5 goles y ambos marcan, que se pueden añadir al boleto | HU-03, HU-23, HU-52 |
| `/eventos/{id}/apostar` · cara a cara | Racha de cada equipo y enfrentamientos anteriores entre los dos | HU-33 |
| `/equipos` | Equipos y deportistas por deporte, con su calificación y su forma | — |
| `/equipos/{id}` | Ficha del equipo: alineación probable, plantilla con la nota de cada jugador, últimos resultados, racha, balance, gráfico de evolución, clasificación y próximos partidos | HU-31, HU-32, HU-49 |
| `/boleto` | Boleto: las cuotas pulsadas en el catálogo se juntan en una combinada con su multiplicador total | HU-28, HU-29 |
| `/mercados` | Apuestas a largo plazo: Balón de Oro, campeón de liga… Se elige un candidato y se cobra al conocerse el ganador | HU-44 |
| `/apuestas` | Mis apuestas activas (con opción de cambiar el importe o cancelar), historial y estadísticas: % de aciertos y rentabilidad | HU-24, HU-26, HU-27, HU-34, HU-35 |
| `/notificaciones` | Avisos cuando se gana, se pierde o se anula una apuesta (campana en la cabecera) | HU-37 |
| `/ranking` | Ranking de jugadores por saldo, ganancias o % de aciertos, con tu posición destacada | HU-36 |
| `/jugadores/{id}` | Perfil público de un jugador: puesto, saldo, aciertos y balance, sin sus apuestas | HU-48 |
| `/ligas` | Ligas privadas: crear una, unirse con su código y ver su ranking solo entre sus miembros | HU-51 |
| `/registro`, `/login` | Registro e inicio de sesión (contraseñas cifradas con BCrypt) | HU-11, HU-12 |
| `/cuenta` | Saldo de moneditas, próxima recarga gratuita, cambio de nombre y contraseña, juego responsable (límites y pausa) y eliminar la cuenta | HU-10, HU-13, HU-14, HU-18, HU-47 |
| `/gestion` | Panel del creador: alta de competiciones, equipos y eventos; calificación, forma, escudo y jugadores de cada equipo (con su nota); límites de apuesta; activar o desactivar el juego responsable | HU-01, HU-02, HU-07, HU-10 |
| `/gestion/casa` | Cuentas de la casa: apostado, pagado, beneficio y margen real frente al teórico, por tipo de apuesta y por deporte | HU-54 |
| `/gestion/eventos/{id}` | Dinero apostado a cada resultado; introducir o corregir el resultado o, en fútbol, el marcador (resuelve y paga las apuestas); suspender, reactivar o anular; editar o borrar si aún no ha empezado | HU-04, HU-05, HU-06, HU-25, HU-46, HU-52 |
| `/gestion/mercados/{id}` | Mercados a largo plazo: candidatos y cuotas, dinero apostado a cada uno, cerrar, marcar o corregir el ganador y anular | HU-45 |

- **Pruebas:** `./mvnw test`
- **Partidos:** con la API configurada (ver abajo) se descargan solos. Sin ella, vienen cargados los partidos reales de LaLiga (jornadas 8 y 9) y de la Champions (jornada 2) de la temporada 2026/27, y eventos de ejemplo de baloncesto, tenis, Fórmula 1 y MotoGP, en `DatosIniciales.java`. El resto se da de alta como creador de apuestas en *Gestión → Nuevo evento*.
- **Base de datos:** se guarda en la carpeta `datos/` (no se sube a GitHub), así que los datos se conservan entre arranques. Las tablas las crea y actualiza **Flyway** (ver más abajo), así que al actualizar el proyecto no hace falta borrarla. Para empezar de cero, para la aplicación y borra esa carpeta.
- **Consola de la base de datos:** http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./datos/apuestas`, usuario `sa`, sin contraseña)
- **IDE recomendado:** IntelliJ IDEA Community. Abrir la carpeta, que detecta el `pom.xml`. También valen Eclipse y VS Code con el "Extension Pack for Java".

## Base de datos y migraciones (Flyway)

Las tablas se crean con los scripts SQL de `src/main/resources/db/migration`, que **Flyway** aplica en orden al arrancar. Hay una carpeta por base de datos:
- `h2/`, la de tu ordenador: de `V1__esquema_inicial.sql` a `V8__ligas_y_mas_apuestas.sql`.
- `postgresql/`, la del servidor: `V8__esquema_postgresql.sql` crea de golpe el esquema equivalente. Flyway apunta en la propia base de datos qué scripts ha aplicado ya, así que cada uno se ejecuta una sola vez y los datos existentes se conservan.

**Si cambias una entidad** (añadir un campo, una tabla, un valor de un `enum`…):
1. Crea un script nuevo con el número siguiente, por ejemplo `V9__descripcion_del_cambio.sql`, con el `alter table` o `create table` necesario, **en las dos carpetas** (`h2/` y `postgresql/`). La sintaxis puede cambiar un poco: H2 usa `enum (...)` y PostgreSQL `varchar` con `check`.
2. **No modifiques nunca un script que ya esté en `main`**: otros ordenadores ya lo han aplicado.
3. Arranca y ejecuta `./mvnw test`. Hibernate comprueba (`ddl-auto=validate`) que las entidades coinciden con las tablas; si falta una migración, la aplicación no arranca y dice qué columna o tabla falta.

Si tu carpeta `datos/` es de antes del Sprint 4 (apuestas combinadas), no se puede actualizar: para la aplicación y bórrala.

## Datos reales con la API (football-data.org)

La aplicación puede descargar sola los partidos y resultados reales de **LaLiga** y la **Champions League**. Al terminar un partido, su resultado entra automáticamente y las apuestas se pagan.

1. Regístrate gratis en https://www.football-data.org/client/register. Te llega por email una clave (*API token*).
2. En IntelliJ: *Run → Edit Configurations… → ApuestasApplication → Environment variables* y añade `FOOTBALL_DATA_TOKEN=tu_clave`.
   Desde la terminal de Windows: `set FOOTBALL_DATA_TOKEN=tu_clave` y después `mvnw.cmd spring-boot:run`.
3. Arranca la aplicación. Se sincroniza a los 30 segundos y luego cada 30 minutos; también hay un botón **Sincronizar ahora** en *Gestión*.
4. Las **plantillas** (jugadores de cada equipo) se descargan a los 2 minutos de arrancar y una vez al día, o con el botón **Descargar plantillas** de *Gestión*. Las notas de los jugadores las pone el creador de apuestas.
   - En el plan gratuito la Champions no trae jugadores, así que las plantillas salen de la liga de cada equipo (`apuestas.api.competiciones-plantillas`: LaLiga, Premier, Bundesliga, Serie A, Ligue 1, Liga Portugal y Eredivisie).
   - Los equipos de otras ligas (Turquía, Bélgica, Grecia…) no tienen plantilla en el plan gratuito. Sus jugadores se añaden a mano en *Gestión → Editar equipo*.
   - El plan gratuito no da dorsales: todos los jugadores entran sin dorsal y con nota 6,0.

- **La clave es personal: no la escribas en ningún archivo del repositorio.**
- El plan gratuito permite 10 peticiones por minuto. Cada sincronización hace una por competición.
- Las competiciones se configuran en `application.properties` (`apuestas.api.competiciones`): `PD` es LaLiga, `CL` la Champions, `PL` la Premier, `SA` la Serie A, `BL1` la Bundesliga y `FL1` la Ligue 1.
- Los equipos nuevos que llegan por la API reciben una calificación de 6,0. El creador de apuestas la ajusta en *Gestión → Editar*.

## Cómo empezar

1. Leer [CONTRIBUTING.md](CONTRIBUTING.md) (flujo Git, ramas y pull requests).
2. Leer [docs/scrum/proceso.md](docs/scrum/proceso.md) (cómo aplicamos Scrum).
3. Revisar el [Product Backlog](docs/scrum/product-backlog.md), las [historias de usuario](docs/requisitos/historias-de-usuario.md) y la [matriz de trazabilidad](docs/requisitos/trazabilidad.md) (qué prueba comprueba cada criterio).
4. Arrancar la aplicación (ver arriba) y revisar los sprints en [docs/scrum/sprints](docs/scrum/sprints).
5. Para entender el diseño: [diagramas UML](docs/diseno/uml.md). Para ver cómo avanza el proyecto: [métricas de velocidad y progreso](docs/scrum/metricas.md).
