# Conceptos básicos

Antes de mirar el código conviene entender cuatro ideas: cliente y servidor, peticiones HTTP, qué hace cada tecnología y cómo se reparte el código en capas.

## Cómo funciona una aplicación web

### Cliente y servidor
- El **cliente** es el navegador (Chrome, Firefox, el del móvil). Enseña las páginas y envía lo que hace el usuario.
- El **servidor** es un programa siempre encendido que espera peticiones. En nuestro caso, la aplicación Java.
- El servidor guarda los datos en una **base de datos**.

```
Navegador ──petición HTTP──▶ Servidor (Java) ──▶ Base de datos
    ◀──────── página HTML nueva ────────┘
```

Ejemplo: el jugador pulsa "Apostar" → el navegador envía `POST /eventos/12/apostar` con el importe → el servidor comprueba, calcula y guarda → responde con una página nueva.

### HTTP, GET y POST
Cada petición tiene un **método** y una **dirección** (URL):
- **GET** pide ver algo y no debe cambiar nada (`GET /ranking`). Es lo que pasa al pulsar un enlace.
- **POST** envía datos que cambian algo (apostar, registrarse, introducir un resultado). Es lo que hace un formulario con `method="post"`.

Que GET no cambie nada es importante: el navegador puede repetir o precargar peticiones GET por su cuenta (ver el fallo F-11 en [Revisión de fallos](Revisión-de-fallos)).

### HTML, CSS y JavaScript
- **HTML**: el contenido (títulos, tablas, formularios).
- **CSS**: el aspecto (colores, tamaños, adaptación al móvil).
- **JavaScript**: comportamiento en el navegador.

Aquí casi todo lo hace el servidor, que genera el HTML ya completo. El JavaScript solo se usa para detalles, como calcular la ganancia potencial mientras se escribe el importe.

### Sesión y cookies
HTTP no recuerda nada entre peticiones. Al iniciar sesión, el servidor crea una **sesión** (memoria para ese usuario) y el navegador guarda su identificador en una **cookie**, que envía en cada petición. En la sesión también vive el **boleto** que el jugador va montando.

## Las tecnologías

La decisión está en [`docs/decisiones/0001-stack-tecnologico.md`](https://github.com/riverri/IS1/blob/main/docs/decisiones/0001-stack-tecnologico.md): se eligió **Java** porque lo conoce todo el equipo, y el resto por ser lo estándar alrededor de Java.

| Tecnología | Para qué sirve en el proyecto |
|---|---|
| **Java 21** | El lenguaje de todo el servidor. |
| **Spring Boot 4** | Un *framework*: resuelve lo común (recibir peticiones, base de datos, seguridad) y solo escribimos lo propio de las apuestas. Incluye el servidor web, así que basta con ejecutar un programa Java. |
| **Thymeleaf** | Plantillas HTML con huecos que se rellenan con datos de Java (`th:text="${usuario.saldo}"`). Están en `src/main/resources/templates`. |
| **Spring Data JPA / Hibernate** | Traduce entre objetos Java y tablas (**ORM**). Una clase `@Entity` es una tabla; un **repositorio** da métodos para buscar y guardar sin escribir SQL. |
| **H2** | Base de datos que corre dentro de la aplicación y guarda los datos en `datos/apuestas.mv.db`. La de cada ordenador. |
| **PostgreSQL** | La base de datos del servidor de Internet (en Neon). |
| **Flyway** | Crea y actualiza las tablas con scripts SQL numerados ([Base de datos y migraciones](Base-de-datos-y-migraciones)). |
| **Spring Security** | Inicio de sesión, contraseñas cifradas (BCrypt), permisos por rol y protección CSRF. |
| **Maven** | Descarga las librerías (`pom.xml`), compila, pasa las pruebas y empaqueta. Se usa con `./mvnw` (Windows: `mvnw.cmd`) sin instalar nada. |
| **JUnit 5 y MockMvc** | Pruebas automáticas. MockMvc simula peticiones sin abrir un navegador. |
| **Git y GitHub** | Control de versiones, issues, pull requests y tablero. |
| **GitHub Actions** | Integración continua: compila y pasa las pruebas en cada pull request. |
| **Docker** | Empaqueta la aplicación para el servidor (`Dockerfile`). |

## La arquitectura por capas

Cada capa solo habla con la de abajo. Es el patrón **MVC** (Modelo-Vista-Controlador) con una capa de servicios en medio:

| Capa | Qué hace | Ejemplo |
|---|---|---|
| **Vista** | Plantillas Thymeleaf. Solo muestran datos. | `apostar.html` |
| **Controlador** | Recibe la petición, lee el formulario, llama al servicio y elige la plantilla. | `ApuestaController` |
| **Servicio** | **Reglas del negocio** (saldo, límites, cuotas, resolución) y **transacciones** (`@Transactional`: o se guarda todo o nada). | `ApuestaService` |
| **Modelo** | Entidades (`@Entity`) y repositorios. Las entidades también tienen lógica. | `Apuesta.reevaluar()` |
| **Base de datos** | H2 o PostgreSQL, con las tablas creadas por Flyway. | |

El código se agrupa **por funcionalidad**: todo lo de las apuestas está en el paquete `apuesta`, todo lo de las ligas en `ligas`, etc.

### Inyección de dependencias
Ninguna clase hace `new ApuestaService(...)`. Spring crea un único objeto de cada clase marcada con `@Service`, `@Controller`, `@Component`… y se lo pasa por el constructor a quien lo necesite. Esto también permite cambiar piezas en las pruebas (por ejemplo, fijar el reloj).
