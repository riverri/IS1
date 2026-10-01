# 0001 · Stack tecnológico

**Estado:** ADOPTADA para el esqueleto inicial. El grupo la confirma en la primera reunión.

## Contexto
- El equipo sabe **Java** y **C++**. Una persona sabe **SQL** y algo de **Python**. Nadie tiene experiencia en desarrollo web.
- Hay que repartir el trabajo entre 5 personas con un flujo Git de ramas y pull requests.

## Decisión
| Capa | Tecnología | Por qué |
|---|---|---|
| Lenguaje | **Java 21** (LTS) | Es el que todo el equipo conoce |
| Framework | **Spring Boot 4** | Estándar de la industria para web en Java, con muchísima documentación |
| Vistas (HTML) | **Thymeleaf** | HTML generado en el servidor, sin aprender React ni JavaScript avanzado |
| Persistencia | **Spring Data JPA** + **H2** en memoria (desarrollo) | Las tablas se crean desde las clases Java. Más adelante se puede pasar a MySQL o PostgreSQL |
| Build | **Maven** con wrapper (`mvnw`) | No hace falta instalar Maven |
| Pruebas | **JUnit 5** + MockMvc | Incluidas en Spring Boot |
| CI | **GitHub Actions** | Compila y pasa las pruebas en cada pull request |

## Alternativas descartadas
- **React + Node.js**: obliga a aprender JavaScript, React y Node a la vez.
- **Python + Django**: más rápido de montar, pero solo una persona sabe algo de Python.
- **C++**: no tiene un ecosistema web práctico para este proyecto.

## Consecuencias
- Hay que aprender lo básico de Spring: controladores, entidades JPA y plantillas Thymeleaf. La guía de Spring https://spring.io/guides/gs/serving-web-content es un buen punto de partida.
- La base de datos H2 se borra en cada arranque. Cuando haga falta conservar datos, se cambia a H2 en fichero o a MySQL/PostgreSQL. Solo hay que tocar `application.properties`.
