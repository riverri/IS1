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
- Apuestas simples y **combinadas** (multiplicador = producto de las cuotas), resueltas automáticamente al introducir el resultado.
- **Banco de estadísticas** para consultar la evolución de los equipos antes de apostar.

Más detalle en [docs/requisitos/vision.md](docs/requisitos/vision.md).

## Estructura del repositorio

```
.
├── docs/
│   ├── requisitos/      # Visión del producto e historias de usuario
│   ├── scrum/           # Proceso, Product Backlog y actas de sprint
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

## Funcionalidades disponibles

| Página | Qué hace | Historias |
|---|---|---|
| `/eventos` | Catálogo público de eventos, agrupados por deporte y ordenados por fecha | HU-08, HU-19 |
| `/registro`, `/login` | Registro e inicio de sesión (contraseñas cifradas con BCrypt) | HU-11, HU-12 |
| `/cuenta` | Saldo de moneditas y fecha de la próxima recarga gratuita | HU-13, HU-14 |
| `/gestion` | Panel del creador: alta de competiciones, equipos y eventos | HU-01 |
| `/equipos` | Listado público de equipos y deportistas | — |

- **Pruebas:** `./mvnw test`
- **Partidos:** vienen cargados los partidos reales de LaLiga (jornadas 8 y 9) y de la Champions (jornada 2) de la temporada 2026/27, en `DatosIniciales.java`. El resto se da de alta como creador de apuestas en *Gestión → Nuevo evento*.
- **Base de datos:** se guarda en la carpeta `datos/` (no se sube a GitHub), así que los datos se conservan entre arranques. Para empezar de cero, para la aplicación y borra esa carpeta.
- **Consola de la base de datos:** http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./datos/apuestas`, usuario `sa`, sin contraseña)
- **IDE recomendado:** IntelliJ IDEA Community. Abrir la carpeta, que detecta el `pom.xml`. También valen Eclipse y VS Code con el "Extension Pack for Java".

## Cómo empezar

1. Leer [CONTRIBUTING.md](CONTRIBUTING.md) (flujo Git, ramas y pull requests).
2. Leer [docs/scrum/proceso.md](docs/scrum/proceso.md) (cómo aplicamos Scrum).
3. Revisar el [Product Backlog](docs/scrum/product-backlog.md) y las [historias de usuario](docs/requisitos/historias-de-usuario.md).
4. Arrancar la aplicación (ver arriba) y revisar el [Sprint 1](docs/scrum/sprints/sprint-01.md).
