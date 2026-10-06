# Estructura del repositorio

```
IS1/
├── README.md                  Portada: qué es, cómo ejecutarlo, funcionalidades
├── CONTRIBUTING.md            Ramas, commits y pull requests
├── pom.xml                    Configuración de Maven: versión de Java y librerías
├── mvnw, mvnw.cmd, .mvn/      Maven incluido: ./mvnw funciona sin instalar nada
├── Dockerfile, .dockerignore  Cómo empaquetar la aplicación para el servidor
├── render.yaml                Configuración del servicio en Render
├── compartir.bat / .sh        Aplicación + túnel para jugar desde otros dispositivos
├── .github/
│   ├── workflows/ci.yml       Integración continua
│   ├── ISSUE_TEMPLATE/        Plantillas: historia de usuario, tarea y fallo
│   └── pull_request_template.md
├── docs/
│   ├── requisitos/            vision.md · historias-de-usuario.md · trazabilidad.md
│   ├── scrum/                 proceso.md · product-backlog.md · metricas.md · sprints/
│   ├── diseno/uml.md          Diagramas UML
│   └── decisiones/            0001 (tecnologías) y 0002 (saldo negativo)
└── src/
    ├── main/java/es/ucm/fdi/is1/apuestas/   El código, en 12 paquetes
    ├── main/resources/
    │   ├── application*.properties          Configuración
    │   ├── db/migration/h2/                 Scripts de la base de datos para H2
    │   ├── db/migration/postgresql/         Los mismos para PostgreSQL
    │   ├── templates/                       Las 30 plantillas HTML
    │   └── static/css/estilos.css           Todo el diseño visual
    └── test/java/...                        Las pruebas, con los mismos paquetes
```

Hay dos carpetas que se crean solas y **no se suben a GitHub**: `target/` (programa compilado) y `datos/` (la base de datos H2 de tu ordenador).

## Las páginas y sus direcciones

| Dirección | Qué es | Quién |
|---|---|---|
| `/` | Portada con el partido destacado | Todos |
| `/eventos` | Catálogo por deporte, con buscador y cuotas | Todos |
| `/eventos/{id}/apostar` | Página del partido: cuotas, otros mercados, cara a cara y formulario | Todos (apostar: jugador) |
| `/equipos`, `/equipos/{id}` | Listado de equipos y ficha de cada uno | Todos |
| `/mercados` | Apuestas a largo plazo | Todos (apostar: jugador) |
| `/ranking`, `/jugadores/{id}` | Ranking y perfil público | Todos |
| `/registro`, `/login` | Crear cuenta e iniciar sesión | Todos |
| `/recuperar` | Recuperar la contraseña | Todos |
| `/boleto` | El boleto de la combinada | Jugador |
| `/apuestas` | Mis apuestas: activas, historial y estadísticas | Jugador |
| `/notificaciones` | Mis avisos | Con sesión |
| `/ligas`, `/ligas/{id}` | Mis ligas y su ranking | Jugador |
| `/cuenta` | Saldo, recarga, nombre, contraseña, juego responsable y baja | Con sesión |
| `/gestion/**` | Panel del creador | Creador |
| `/h2-console` | Consola de la base de datos (solo en local) | Creador |

Las plantillas comparten trozos (**fragmentos**) en `templates/fragments/comun.html`: cabecera, pie, tarjeta de partido con sus cuotas y escudo.

## La configuración

Está en `src/main/resources/application.properties`:

| Propiedad | Para qué |
|---|---|
| `spring.datasource.url` | Qué base de datos usar. Por defecto H2 en `./datos/apuestas`; en el servidor, la variable `SPRING_DATASOURCE_URL`. |
| `spring.flyway.locations` | `db/migration/{vendor}`: Flyway elige `h2/` o `postgresql/`. |
| `spring.jpa.hibernate.ddl-auto=validate` | Hibernate no crea tablas: comprueba que coinciden con las entidades. |
| `apuestas.creador.password`, `apuestas.usuario.password` | Contraseñas de prueba (`creador123`, `usuario123`), cambiables con `CREADOR_PASSWORD` y `USUARIO_PASSWORD`. |
| `apuestas.saldo.*` | Bienvenida de 1.000 y recarga de 200 cada 7 días. |
| `apuestas.api.*` | Dirección y clave de la API, competiciones, días hacia atrás (7) y adelante (21), intervalo (30 min). |
| `apuestas.zona-horaria` | `Europe/Madrid`. |

### Perfiles
- `compartir`: para el túnel. Desactiva la consola de H2 y exige la contraseña del creador.
- `nube`: para Render. Desactiva la consola de H2 y exige `CREADOR_PASSWORD`.

> **La clave de la API nunca va en un archivo.** `apuestas.api.token=${FOOTBALL_DATA_TOKEN:}` la lee de una variable de entorno. Se pone en IntelliJ (*Run → Edit Configurations → Environment variables*) o en Render.
