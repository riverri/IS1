# IS1 · Web de apuestas deportivas

Proyecto de **Ingeniería del Software I** (doble grado Informática–Matemáticas, UCM, curso 2026/27).
Profesor: Gonzalo Rubén Méndez Pozo.

## Equipo

| Miembro | Rol Scrum (provisional) |
|---|---|
| Carlos Martín-Salas | _por decidir_ |
| Jaime Martín | _por decidir_ |
| David Ortega | _por decidir_ |
| Jing Li | _por decidir_ |
| Carlos Jurado | _por decidir_ |

> Los roles se definen en la primera reunión. Ver [docs/scrum/proceso.md](docs/scrum/proceso.md).

## ¿Qué es?

Una web donde los usuarios apuestan con **moneditas virtuales (sin dinero real)** sobre eventos deportivos:

- **Cuotas calculadas por nuestro algoritmo** a partir de la calidad y la forma de cada equipo y del volumen apostado.
- Apuestas simples, **combinadas** (multiplicador = producto de las cuotas) y **a largo plazo** (Balón de Oro, campeón de liga…), resueltas automáticamente al introducir el resultado o el ganador.
- **Banco de estadísticas** para consultar la evolución de los equipos antes de apostar.

Más detalle en [docs/requisitos/vision.md](docs/requisitos/vision.md).

## Estructura del repositorio

```
.
├── docs/
│   ├── requisitos/      # Visión del producto e historias de usuario
│   ├── scrum/           # Proceso, Product Backlog, actas de sprint y métricas
│   ├── diseno/          # Diagramas UML (casos de uso, clases, estados, secuencia, datos)
│   ├── decisiones/      # Decisiones técnicas (stack, arquitectura…)
│   └── recursos.md      # Enlaces de teoría y de Git
├── src/main/java/       # Código Java (Spring Boot)
├── src/main/resources/  # Plantillas HTML, CSS, configuración y datos de ejemplo
├── src/test/java/       # Pruebas (JUnit)
├── pom.xml              # Dependencias (Maven)
├── .github/             # Plantillas de issues y pull requests
└── CONTRIBUTING.md      # Cómo trabajamos con Git y GitHub
```

## Tecnología

Java 21 · Spring Boot · Thymeleaf · Spring Data JPA + H2 · Maven · JUnit. El porqué está en [docs/decisiones/0001-stack-tecnologico.md](docs/decisiones/0001-stack-tecnologico.md).

## Cómo ejecutarlo

Requisito: tener instalado un **JDK 21**, por ejemplo [Eclipse Temurin 21](https://adoptium.net/). Maven no hace falta: va incluido como `mvnw`.

```bash
git clone https://github.com/riverri/IS1.git
cd IS1
./mvnw spring-boot:run        # en Windows: mvnw.cmd spring-boot:run
```

Abrir http://localhost:8080.

**Usuarios de prueba** (se crean en el primer arranque, junto con competiciones y equipos):

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

## Funcionalidades disponibles

| Página | Qué hace | Historias |
|---|---|---|
| `/eventos` | Catálogo público con cuotas 1/X/2, pestañas por deporte (fútbol, baloncesto, tenis, F1, MotoGP) y buscador | HU-08, HU-19, HU-20, HU-22 |
| `/eventos/{id}/apostar` | Apuesta simple directa: elegir resultado e importe, con la ganancia potencial | HU-03, HU-23 |
| `/boleto` | Boleto: las cuotas pulsadas en el catálogo se juntan en una combinada con su multiplicador total | HU-28, HU-29 |
| `/mercados` | Apuestas a largo plazo: Balón de Oro, campeón de liga… Se elige un candidato y se cobra al conocerse el ganador | HU-44 |
| `/eventos/{id}/apostar` · cara a cara | Racha de cada equipo y enfrentamientos anteriores entre los dos | HU-33 |
| `/apuestas` | Mis apuestas activas (con opción de cambiar el importe o cancelar), historial y estadísticas: % de aciertos y rentabilidad | HU-24, HU-26, HU-27, HU-34, HU-35 |
| `/notificaciones` | Avisos cuando se gana, se pierde o se anula una apuesta (campana en la cabecera) | HU-37 |
| `/ranking` | Ranking de jugadores por saldo, ganancias o % de aciertos, con tu posición destacada | HU-36 |
| `/jugadores/{id}` | Perfil público de un jugador: puesto, saldo, aciertos y balance, sin sus apuestas | HU-48 |
| `/registro`, `/login` | Registro e inicio de sesión (contraseñas cifradas con BCrypt) | HU-11, HU-12 |
| `/cuenta` | Saldo de moneditas, próxima recarga gratuita, cambio de nombre y contraseña, juego responsable (límites y pausa) y eliminar la cuenta | HU-10, HU-13, HU-14, HU-18, HU-47 |
| `/gestion` | Panel del creador: alta de competiciones, equipos y eventos; calificación, forma reciente y escudo de cada equipo; límites de apuesta | HU-01, HU-02, HU-07 |
| `/gestion/eventos/{id}` | Dinero apostado a cada resultado; introducir o corregir el resultado (resuelve y paga las apuestas); suspender, reactivar o anular; editar o borrar si aún no ha empezado | HU-04, HU-05, HU-06, HU-25, HU-46 |
| `/gestion/mercados/{id}` | Mercados a largo plazo: candidatos y cuotas, dinero apostado a cada uno, cerrar, marcar o corregir el ganador y anular | HU-45 |
| `/equipos` | Equipos y deportistas por deporte, con su calificación y su forma | — |
| `/equipos/{id}` | Ficha del equipo: últimos resultados, racha, balance, gráfico de evolución, clasificación y próximos partidos | HU-31, HU-32 |

- **Pruebas:** `./mvnw test`
- **Partidos:** con la API configurada (ver abajo) se descargan solos. Sin ella, vienen cargados los partidos reales de LaLiga (jornadas 8 y 9) y de la Champions (jornada 2) de la temporada 2026/27, y eventos de ejemplo de baloncesto, tenis, Fórmula 1 y MotoGP, en `DatosIniciales.java`. El resto se da de alta como creador de apuestas en *Gestión → Nuevo evento*.
- **Base de datos:** se guarda en la carpeta `datos/` (no se sube a GitHub), así que los datos se conservan entre arranques. Las tablas las crea y actualiza **Flyway** (ver más abajo), así que al actualizar el proyecto no hace falta borrarla. Para empezar de cero, para la aplicación y borra esa carpeta.
- **Consola de la base de datos:** http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./datos/apuestas`, usuario `sa`, sin contraseña)
- **IDE recomendado:** IntelliJ IDEA Community. Abrir la carpeta, que detecta el `pom.xml`. También valen Eclipse y VS Code con el "Extension Pack for Java".

## Base de datos y migraciones (Flyway)

Las tablas se crean con los scripts SQL de `src/main/resources/db/migration`, que **Flyway** aplica en orden al arrancar: `V1__esquema_inicial.sql`, `V2__identificadores_api.sql`… Flyway apunta en la propia base de datos qué scripts ha aplicado ya, así que cada uno se ejecuta una sola vez y los datos existentes se conservan.

**Si cambias una entidad** (añadir un campo, una tabla, un valor de un `enum`…):
1. Crea un script nuevo con el número siguiente, por ejemplo `V3__descripcion_del_cambio.sql`, con el `alter table` o `create table` necesario.
2. **No modifiques nunca un script que ya esté en `main`**: otros ordenadores ya lo han aplicado.
3. Arranca y ejecuta `./mvnw test`. Hibernate comprueba (`ddl-auto=validate`) que las entidades coinciden con las tablas; si falta una migración, la aplicación no arranca y dice qué columna o tabla falta.

Si tu carpeta `datos/` es de antes del Sprint 4 (apuestas combinadas), no se puede actualizar: para la aplicación y bórrala.

## Datos reales con la API (football-data.org)

La aplicación puede descargar sola los partidos y resultados reales de **LaLiga** y la **Champions League**. Al terminar un partido, su resultado entra automáticamente y las apuestas se pagan.

1. Regístrate gratis en https://www.football-data.org/client/register. Te llega por email una clave (*API token*).
2. En IntelliJ: *Run → Edit Configurations… → ApuestasApplication → Environment variables* y añade `FOOTBALL_DATA_TOKEN=tu_clave`.
   Desde la terminal de Windows: `set FOOTBALL_DATA_TOKEN=tu_clave` y después `mvnw.cmd spring-boot:run`.
3. Arranca la aplicación. Se sincroniza a los 30 segundos y luego cada 30 minutos; también hay un botón **Sincronizar ahora** en *Gestión*.

- **La clave es personal: no la escribas en ningún archivo del repositorio.**
- El plan gratuito permite 10 peticiones por minuto. Cada sincronización hace una por competición.
- Las competiciones se configuran en `application.properties` (`apuestas.api.competiciones`): `PD` es LaLiga, `CL` la Champions, `PL` la Premier, `SA` la Serie A, `BL1` la Bundesliga y `FL1` la Ligue 1.
- Los equipos nuevos que llegan por la API reciben una calificación de 6,0. El creador de apuestas la ajusta en *Gestión → Editar*.

## Cómo empezar

1. Leer [CONTRIBUTING.md](CONTRIBUTING.md) (flujo Git, ramas y pull requests).
2. Leer [docs/scrum/proceso.md](docs/scrum/proceso.md) (cómo aplicamos Scrum).
3. Revisar el [Product Backlog](docs/scrum/product-backlog.md) y las [historias de usuario](docs/requisitos/historias-de-usuario.md).
4. Arrancar la aplicación (ver arriba) y revisar los sprints en [docs/scrum/sprints](docs/scrum/sprints).
5. Para entender el diseño: [diagramas UML](docs/diseno/uml.md). Para ver cómo avanza el proyecto: [métricas de velocidad y progreso](docs/scrum/metricas.md).
