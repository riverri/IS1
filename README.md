# Better · Web de apuestas deportivas (IS1)

Proyecto de **Ingeniería del Software I** (doble grado Informática–Matemáticas, UCM, curso 2026/27).
Profesor: Gonzalo Rubén Méndez Pozo.

## Equipo

| Miembro | Rol Scrum |
|---|---|
| Gonzalo Rubén Méndez Pozo (profesor) | Product Owner |
| Carlos Martín-Salas | Equipo de Desarrollo |
| Jaime Martín | Equipo de Desarrollo |
| David Ortega | Equipo de Desarrollo |
| Jing Li | Equipo de Desarrollo |
| Carlos Jurado | Scrum Master y Equipo de Desarrollo |

> El **Scrum Master** es Carlos Jurado ([issue #28](https://github.com/riverri/IS1/issues/28)). Ver [docs/scrum/proceso.md](docs/scrum/proceso.md).

**Tablero del sprint:** desde el Sprint 4, el Sprint Backlog se sigue en el [grafo del equipo](https://grafo-better.vercel.app) (o [en solo lectura, sin cuenta](https://grafo-better.vercel.app/?presentacion=8FeSBocO1rjfr6U1N9CJ1BBq)), que enlaza cada tarea con su issue o pull request ([decisión 0003](docs/decisiones/0003-seguimiento-en-el-grafo.md)). Los [issues](https://github.com/riverri/IS1/issues) siguen en el tablero de GitHub Projects (pestaña *Projects*).

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
│   │   ├── sprints/                   # Actas de cada sprint (se publican en la wiki)
│   │   │   └── iteraciones/           # Las 12 iteraciones de desarrollo del Sprint 1
│   │   ├── metricas.md                # Velocidad y progreso
│   │   └── graficas/                  # Gráficas de las métricas y el script que las genera
│   ├── diseno/uml.md                  # Diagramas UML (casos de uso, clases, estados, secuencia, datos)
│   ├── wiki/                          # Páginas de la wiki (se publican solas al fusionar en main)
│   ├── presentaciones/                # Presentaciones del proyecto (PowerPoint y PDF)
│   ├── Better_Guia_del_proyecto.pdf   # Guía completa del proyecto, para leerla de corrido
│   ├── decisiones/                    # Decisiones (stack, saldo negativo, grafo del equipo)
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

Antes de arrancar pide una contraseña para `creador@apuestas.es` (o la toma de la variable `CREADOR_PASSWORD`), porque la del README es pública y con el enlace cualquiera podría entrar en *Gestión*.

**macOS / Linux:** `brew install cloudflared` (o el paquete de su web) y después `./compartir.sh`.

- El enlace solo funciona mientras ese ordenador esté encendido y con las dos ventanas abiertas, y **cambia cada vez** que se lanza.
- Usa el perfil `compartir`, que desactiva la consola de H2: no tiene contraseña y con el túnel cualquiera podría entrar en la base de datos. **No compartas nunca la aplicación arrancada desde IntelliJ sin ese perfil.**
- Con el túnel las contraseñas de prueba de este README son públicas: el creador usa la de `CREADOR_PASSWORD` (el script la pide) y `usuario@apuestas.es` solo entra si defines `USUARIO_PASSWORD`. Cada uno puede registrarse con su propia cuenta.
- Si la red de la facultad bloquea el túnel, prueba con los datos del móvil.

## Publicarla en Internet (servidor permanente y gratuito)

Para entrar desde cualquier sitio (también desde el móvil) sin tener el ordenador encendido. Se usan tres servicios gratuitos, ninguno pide tarjeta:

| Servicio | Para qué |
|---|---|
| [Render](https://render.com) | Ejecuta la aplicación. El repositorio ya trae el `Dockerfile` y `render.yaml`. |
| [Neon](https://neon.tech) | Base de datos PostgreSQL permanente (0,5 GB, de sobra para el proyecto). |
| [UptimeRobot](https://uptimerobot.com) (opcional) | Avisa si la web se cae. Ya no hace falta: lo hace el trabajo *Vigilancia* de GitHub (ver abajo), y para que Render no la apague la aplicación se visita sola (keep-alive). |

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
| `DEMO_DATOS` | Opcional: con `true` carga una vez datos de demostración (ver abajo), para enseñar la web con contenido. |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | Opcional: servidor de correo para enviar el enlace de recuperar la contraseña (ver abajo). |
| `URL_PUBLICA` | Opcional: dirección de la web para el enlace de recuperar la contraseña. En Render no hace falta (se usa la que da Render). |

3. Despliega. La primera vez tarda unos minutos. Las tablas las crea Flyway solo, y queda en un enlace fijo del tipo `https://apuestas-is1.onrender.com`.

Cada vez que se fusiona algo en `main`, Render la vuelve a publicar sola. Los datos siguen en Neon.

La imagen guarda una caché de las clases de Java (CDS) preparada al construirla. Con 1 CPU y 512 MB, la aplicación arranca en unos 15 segundos en vez de 22, y usa unos 30 MB menos de memoria. También separa las dependencias de nuestro código en capas distintas, así un cambio pequeño solo reconstruye la capa pequeña.

**Vigilar el servidor**
- **Aviso si se cae:** el trabajo *Vigilancia* de GitHub Actions pide `/actuator/health` cada hora. Si no responde en tres intentos, abre un issue «La web no responde» (etiqueta `caida`), y lo cierra solo cuando vuelve. Para activarlo, pon una vez la dirección de la web en GitHub: *Settings → Secrets and variables → Actions → Variables → New repository variable*, con el nombre `URL_PUBLICA`. Avisa a todo el que sigue el repositorio, así que UptimeRobot ya no hace falta.
- `GET /actuator/health` responde `{"status":"UP"}` si la aplicación y su base de datos responden, y `DOWN` si no. No da más detalles y no hace falta sesión. El resto de Actuator no está publicado.
- Cada respuesta lleva la cabecera `X-Request-Id`, y cada línea del registro, el mismo identificador entre corchetes. Por ejemplo: `INFO [3f9a1c2b7d10] … No hay servidor de correo configurado`.
  - Si alguien avisa de un error, con el identificador (se ve en las herramientas de desarrollo del navegador, pestaña *Red*) se buscan en *Logs* de Render las líneas de esa petición.
  - Si la petición ya trae un identificador válido (letras, números y guiones), se usa ese.

**3. Que no se apague (keep-alive)**

Render apaga las aplicaciones gratuitas tras 15 minutos sin visitas, y la siguiente visita tarda alrededor de un minuto. Para evitarlo, la propia aplicación hace un GET a `/ping` por su dirección pública cada 10 minutos (HU-85). No hay que configurar nada: la dirección la da Render en `RENDER_EXTERNAL_URL`. El aviso cuando la web se cae lo da el trabajo *Vigilancia* de GitHub (ver *Vigilar el servidor*). El plan gratuito de Render da 750 horas al mes, suficientes para tener un servicio encendido todo el mes.

**4. Datos de demostración (opcional)**

Con `DEMO_DATOS=true`, al arrancar se cargan una sola vez seis jugadores de ejemplo (Lucía, Marcos, Sofía, Pablo, Elena y Hugo) con apuestas ganadas, perdidas y activas, partidos próximos (si hay menos de seis), una combinada, una supercuota y la liga privada «Liga de la clase». Si existe `usuario@apuestas.es`, también tiene apuestas y está en la liga, para enseñar *Mis apuestas* al entrar con él. Todo se hace con los mismos servicios que usa la web, así que el saldo, el ranking y las cuotas cuadran. Los jugadores de ejemplo no pueden iniciar sesión (su contraseña es aleatoria). Si ya están cargados, no se repiten (y quitar la variable no los borra). También funciona en local: `DEMO_DATOS=true ./mvnw spring-boot:run`.

**Notas**
- Sin las variables `SPRING_DATASOURCE_*` funciona igual, pero con H2: los datos se borran en cada reinicio.
- No uses la base de datos PostgreSQL gratuita de Render: se borra a los 30 días.
- Usa el perfil `nube`: la consola de H2 está desactivada y, si falta `CREADOR_PASSWORD`, la aplicación no arranca.

## Funcionalidades disponibles

| Página | Qué hace | Historias |
|---|---|---|
| `/eventos` | Catálogo público con cuotas 1/X/2, pestañas por deporte (fútbol, baloncesto, tenis, F1, MotoGP) y buscador; arriba, los próximos partidos de tus equipos favoritos | HU-08, HU-19, HU-20, HU-22, HU-86 |
| `/eventos/{id}/apostar` | Apuesta simple directa: elegir resultado e importe, con la ganancia potencial. En fútbol también doble oportunidad, más/menos de 2,5 goles y ambos marcan, que se pueden añadir al boleto; con supercuota si el creador la ha puesto, gráfico de la evolución de las cuotas y alertas de cuota | HU-03, HU-23, HU-52, HU-87, HU-90, HU-91 |
| `/eventos/{id}/apostar` · cara a cara | Racha de cada equipo y enfrentamientos anteriores entre los dos | HU-33 |
| `/equipos` | Equipos y deportistas por deporte, con su calificación y su forma | — |
| `/equipos/{id}` | Ficha del equipo: alineación probable, plantilla con la nota de cada jugador, últimos resultados, racha, balance, gráfico de evolución, clasificación y próximos partidos; añadirlo o quitarlo de favoritos | HU-31, HU-32, HU-49, HU-86 |
| `/boleto` | Boleto: las cuotas pulsadas en el catálogo se juntan en una combinada con su multiplicador total | HU-28, HU-29 |
| `/mercados` | Apuestas a largo plazo: Balón de Oro, campeón de liga… Se elige un candidato y se cobra al conocerse el ganador | HU-44 |
| `/apuestas` | Mis apuestas activas (con opción de cambiar el importe o cancelar), historial y estadísticas: % de aciertos y rentabilidad; estadísticas por competición y descargar las apuestas en CSV | HU-24, HU-26, HU-27, HU-34, HU-35, HU-96, HU-114 |
| `/notificaciones` | Avisos cuando se gana, se pierde o se anula una apuesta (campana en la cabecera) | HU-37 |
| `/ranking` | Ranking de jugadores por saldo, ganancias o % de aciertos, con tu posición destacada; también por tipo de juego (deportes, predicciones, cripto, memecoins y quiniela) | HU-36, HU-84 |
| `/jugadores/{id}` | Perfil público de un jugador: puesto, saldo, aciertos y balance, sin sus apuestas; y sus logros | HU-48, HU-88 |
| `/ligas` | Ligas privadas: crear una, unirse con su código y ver su ranking solo entre sus miembros; retos 1 contra 1 entre sus miembros | HU-51, HU-89 |
| `/registro`, `/login` | Registro e inicio de sesión (contraseñas cifradas con BCrypt) | HU-11, HU-12 |
| `/recuperar` | Recuperar la contraseña con un enlace por correo que caduca a los 30 minutos y solo sirve una vez | HU-17 |
| `/cuenta` | Saldo de moneditas, próxima recarga gratuita, cambio de nombre y contraseña, juego responsable (límites y pausa) y eliminar la cuenta; correo al ganar y resumen semanal (activar o desactivar) y descargar los movimientos en CSV | HU-10, HU-13, HU-14, HU-18, HU-47, HU-77, HU-92, HU-114 |
| `/cuenta/movimientos` | *(Pendiente de subir)* Movimientos del saldo: cada entrada y salida de moneditas con su motivo y el saldo que dejó | HU-55 |
| `/gestion` | Panel del creador: alta de competiciones, equipos y eventos; calificación, forma, escudo y jugadores de cada equipo (con su nota); límites de apuesta; activar o desactivar el juego responsable | HU-01, HU-02, HU-07, HU-10 |
| `/gestion/casa` | Cuentas de la casa: apostado, pagado, beneficio y margen real frente al teórico, por tipo de apuesta y por deporte; beneficio de cada uno de los últimos 30 días | HU-53, HU-95 |
| `/gestion/anuncios` | *(Pendiente de subir)* Anuncios internos: alta, edición, activar o desactivar, con sus impresiones, clics y porcentaje de clics | HU-61, HU-62 |
| `/anuncios/{id}/ir` | *(Pendiente de subir)* Clic en un anuncio: lo apunta y lleva a su sección (lo pintan las páginas debajo de la cabecera) | HU-60, HU-62 |
| `/casino` | *(Pendiente de subir)* Casino con moneditas: ruleta europea (`/casino/ruleta`), blackjack (`/casino/blackjack`) y mis partidas (`/casino/historial`) | HU-56, HU-57, HU-58 |
| `/gestion/casino` | *(Pendiente de subir)* Configuración del casino: abrirlo o cerrarlo, límites por apuesta y reglas del blackjack, con la ventaja de la casa | HU-59 |
| `/predicciones` | Mercados de predicción de sí o no: precio de 0 a 1, comprar y vender participaciones, estadísticas, gráfico y posiciones de los compañeros de liga (`/predicciones/{id}`) | HU-64 a HU-68 |
| `/gestion/predicciones` | Crear, cerrar, anular y resolver los mercados de predicción | HU-63, HU-66 |
| `/cripto` | Criptomonedas a su precio real en euros (CoinGecko), memecoins primero; comprar y vender con moneditas y gráfico de la semana (`/cripto/{símbolo}`), y mi cartera (`/cripto/cartera`) | HU-69 a HU-72 |
| `/gestion/cripto` | Abrir o cerrar las criptomonedas, comisión, validez de los precios, qué criptos se ofrecen y descargar los precios en el momento | HU-73 |
| `/quinielas` | Quinielas de 3 a 15 partidos: echar un boleto con 1, X o 2 en cada uno y, al terminar, el reparto del bote | HU-79, HU-80 |
| `/gestion/quinielas` | Montar quinielas, repartir el bote entre los que más aciertan y anularlas | HU-78, HU-80 |
| `/tema` | Cambia entre los colores claros y el modo oscuro (la luna o el sol de la cabecera) | HU-94 |
| `/gestion/eventos/{id}` | Dinero apostado a cada resultado; introducir o corregir el resultado o, en fútbol, el marcador (resuelve y paga las apuestas); suspender, reactivar o anular; editar o borrar si aún no ha empezado; riesgo de la casa con cada resultado y supercuota | HU-04, HU-05, HU-06, HU-25, HU-46, HU-52, HU-76, HU-87 |
| `/gestion/mercados/{id}` | Mercados a largo plazo: candidatos y cuotas, dinero apostado a cada uno, cerrar, marcar o corregir el ganador y anular | HU-45 |

- **Pruebas:** `./mvnw test`. Antes de un PR conviene `./mvnw verify`, lo mismo que hace la CI:
  - las pruebas;
  - la cobertura mínima (JaCoCo: 90 % de líneas y 75 % de ramas; informe en `target/site/jacoco/index.html`);
  - el análisis estático (SpotBugs, que falla con los errores de prioridad alta).
  - Además, en la CI:
    - **Pruebas de mutación (PIT):** `./mvnw -Pmutacion test-compile org.pitest:pitest-maven:mutationCoverage -Djacoco.skip=true`, que exige que las pruebas detecten al menos el 80 % de los cambios (informe en `target/pit-reports/index.html`).
    - **En un navegador real**, con la aplicación arrancada, desde la carpeta `a11y` (`npm install` la primera vez):
      - `node comprobar.js` (accesibilidad);
      - `node e2e.js` (de principio a fin: registrarse, apostar, cancelar, combinada, el creador pone el resultado y se cobra, salir).
  - Dependabot abre cada mes un PR si sale una versión nueva de una dependencia, y enseguida si la que usamos tiene un fallo de seguridad conocido.
- **Partidos:** con la API configurada (ver abajo) se descargan solos. Sin ella, vienen cargados los partidos reales de LaLiga (jornadas 8 y 9) y de la Champions (jornada 2) de la temporada 2026/27, y eventos de ejemplo de baloncesto, tenis, Fórmula 1 y MotoGP, en `DatosIniciales.java`. El resto se da de alta como creador de apuestas en *Gestión → Nuevo evento*.
- **Base de datos:** se guarda en la carpeta `datos/` (no se sube a GitHub), así que los datos se conservan entre arranques. Las tablas las crea y actualiza **Flyway** (ver más abajo), así que al actualizar el proyecto no hace falta borrarla. Para empezar de cero, para la aplicación y borra esa carpeta.
- **Consola de la base de datos:** http://localhost:8080/h2-console, solo con la sesión del creador de apuestas iniciada (JDBC URL `jdbc:h2:file:./datos/apuestas`, usuario `sa`, sin contraseña)
- **Reglas que conviene conocer:**
  - Tras 5 contraseñas incorrectas seguidas, la cuenta queda bloqueada 15 minutos.
  - Desde una misma conexión (IP) se pueden enviar como mucho 10 registros y 10 peticiones de recuperar la contraseña cada 15 minutos; después sale «Demasiados intentos» (se cambia con `apuestas.limite-peticiones.maximo` y `apuestas.limite-peticiones.ventana`).
  - Las páginas llevan cabeceras de seguridad (política de contenido CSP, Referrer-Policy y Permissions-Policy): el navegador no carga scripts de otras webs ni envía formularios fuera.
  - En las exportaciones CSV, un texto que empieza por `=`, `+`, `-` o `@` lleva delante un apóstrofo, para que Excel no lo ejecute como fórmula.
  - Al cambiar la contraseña o dar de baja la cuenta se cierran las sesiones abiertas en otros dispositivos.
  - Una combinada no puede pasar de una cuota total de 1.000.
  - El creador de apuestas no puede apostar.
  - En el ajuste de cuotas por volumen, cada jugador cuenta como mucho 100 moneditas por resultado (las combinadas se reparten entre sus selecciones), para que nadie mueva las cuotas a su favor.
  - El % de aciertos del ranking solo cuenta a partir de 5 apuestas resueltas.
  - Las horas son siempre las de Madrid, esté donde esté el servidor.
- **IDE recomendado:** IntelliJ IDEA Community. Abrir la carpeta, que detecta el `pom.xml`. También valen Eclipse y VS Code con el "Extension Pack for Java".

## Base de datos y migraciones (Flyway)

Las tablas se crean con los scripts SQL de `src/main/resources/db/migration`, que **Flyway** aplica en orden al arrancar. Hay una carpeta por base de datos:
- `h2/`, la de tu ordenador: de `V1__esquema_inicial.sql` a `V17__seguimiento_y_ampliacion.sql`.
- `postgresql/`, la del servidor: `V8__esquema_postgresql.sql` crea de golpe el esquema equivalente y desde ahí van los mismos scripts que en `h2/` (`V9__…`, `V10__…`, `V11__…`, `V12__…`, `V13__…`, `V14__…`, `V15__…`). Flyway apunta en la propia base de datos qué scripts ha aplicado ya, así que cada uno se ejecuta una sola vez y los datos existentes se conservan.

**Si cambias una entidad** (añadir un campo, una tabla, un valor de un `enum`…):
1. Crea un script nuevo con el número siguiente, por ejemplo `V11__descripcion_del_cambio.sql`, con el `alter table` o `create table` necesario, **en las dos carpetas** (`h2/` y `postgresql/`). La sintaxis puede cambiar un poco: H2 usa `enum (...)` y PostgreSQL `varchar` con `check`.
2. **No modifiques nunca un script que ya esté en `main`**: otros ordenadores ya lo han aplicado.
3. Arranca y ejecuta `./mvnw test`. Hibernate comprueba (`ddl-auto=validate`) que las entidades coinciden con las tablas; si falta una migración, la aplicación no arranca y dice qué columna o tabla falta.

Si tu carpeta `datos/` es de antes de la iteración 4 del Sprint 1 (apuestas combinadas), no se puede actualizar: para la aplicación y bórrala.

## Datos reales con la API (football-data.org)

La aplicación puede descargar sola los partidos y resultados reales de **LaLiga** y la **Champions League**. Al terminar un partido, su resultado entra automáticamente y las apuestas se pagan. Si hubo prórroga o penaltis, cuenta el marcador de los 90 minutos; si la API no lo da, el partido se queda sin resolver y aparece como error de la sincronización para que el creador lo introduzca a mano. Un partido aplazado se suspende, y vuelve a abrirse solo si lo suspendió la API (no si lo suspendió el creador).

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

## Correo para recuperar la contraseña

En *Iniciar sesión → ¿Has olvidado tu contraseña?* se pide un enlace para crear una contraseña nueva. Caduca a los 30 minutos y solo sirve una vez.

- **Sin servidor de correo** (lo normal en tu ordenador), el enlace se escribe en la consola de la aplicación: busca la línea `Enlace para recuperar la contraseña de …` y ábrelo en el navegador.
- **Con servidor de correo**, se envía de verdad. Por ejemplo, en tu ordenador, con una cuenta de Gmail con verificación en dos pasos:
  1. Crea una *contraseña de aplicación* en https://myaccount.google.com/apppasswords.
  2. Define `MAIL_HOST=smtp.gmail.com`, `MAIL_PORT=587`, `MAIL_USERNAME=tu_cuenta@gmail.com` y `MAIL_PASSWORD=la_contraseña_de_aplicación` (en IntelliJ).
- **En Render (plan gratuito) Gmail no sirve.** Desde el 26 de septiembre de 2025, Render bloquea en sus servicios gratuitos las conexiones a los puertos de correo 25, 465 y 587 ([aviso de Render](https://render.com/changelog/free-web-services-will-no-longer-allow-outbound-traffic-to-smtp-ports)). La aplicación no se rompe: el envío falla tras unos segundos y el enlace queda en *Logs*. Para enviarlo de verdad hace falta un servicio que acepte otro puerto, como [Brevo](https://www.brevo.com), gratis hasta 300 correos al día, que admite el **2525**:
  1. Crea una cuenta en Brevo y, en *Senders, Domains & Dedicated IPs → Senders*, añade y verifica el correo que saldrá como remitente.
  2. En *SMTP & API → SMTP*, genera una *clave SMTP*. El usuario es el *Login* que sale ahí, que no es el correo con el que entras.
  3. En *Environment* de Render define:
     - `MAIL_HOST=smtp-relay.brevo.com`;
     - `MAIL_PORT=2525`;
     - `MAIL_USERNAME=` el *Login* de Brevo;
     - `MAIL_PASSWORD=` la clave SMTP;
     - `APUESTAS_CORREO_REMITENTE=` el remitente verificado.
  4. Pide un enlace en */recuperar* con tu cuenta y mira que llegue. Si en *Logs* sale `No se ha podido enviar el correo de recuperación`, el motivo viene detrás.

  Las claves van solo en Render, nunca en el repositorio. Esta configuración no se ha probado todavía desde Render: si Brevo tampoco conecta, la alternativa es pasar el servicio a un plan de pago de Render, que sí deja usar el 587.
- La respuesta al pedir el enlace es la misma exista o no la cuenta, para no desvelar qué emails están registrados.
- El enlace apunta siempre a la dirección configurada (`apuestas.url-publica`), nunca a la que llega en la petición: así nadie puede falsear las cabeceras para que el correo lleve a otra web. En local es `http://localhost:8080`; en Render, la de Render; con el túnel, define `URL_PUBLICA` con el enlace `https://…trycloudflare.com`.

## Cómo empezar

Para una visión general, lo más rápido es la [guía completa del proyecto](docs/Better_Guia_del_proyecto.pdf) (PDF) o la [wiki](https://github.com/riverri/IS1/wiki). Después:

1. Leer [CONTRIBUTING.md](CONTRIBUTING.md) (flujo Git, ramas y pull requests).
2. Leer [docs/scrum/proceso.md](docs/scrum/proceso.md) (cómo aplicamos Scrum).
3. Revisar el [Product Backlog](docs/scrum/product-backlog.md), las [historias de usuario](docs/requisitos/historias-de-usuario.md) y la [matriz de trazabilidad](docs/requisitos/trazabilidad.md) (qué prueba comprueba cada criterio).
4. Arrancar la aplicación (ver arriba) y revisar los sprints en [docs/scrum/sprints](docs/scrum/sprints).
5. Para entender el diseño: [diagramas UML](docs/diseno/uml.md). Para ver cómo avanza el proyecto: [métricas de velocidad y progreso](docs/scrum/metricas.md).
