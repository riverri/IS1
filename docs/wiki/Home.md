# Web de apuestas deportivas · IS1

Proyecto de **Ingeniería del Software I** (doble grado Informática–Matemáticas, UCM, curso 2026/27).

Es una **web de apuestas deportivas con moneditas virtuales**: no se usa dinero real en ningún momento. Cada jugador recibe moneditas al registrarse y recargas gratuitas cada semana, y apuesta sobre partidos reales con cuotas calculadas por un algoritmo propio.

## Equipo

| Miembro | Rol Scrum |
|---|---|
| Gonzalo Rubén Méndez Pozo (profesor) | Product Owner |
| Carlos Martín-Salas | Equipo de Desarrollo |
| Jaime Martín | Equipo de Desarrollo |
| David Ortega | Equipo de Desarrollo |
| Jing Li | Equipo de Desarrollo |
| Carlos Jurado | Equipo de Desarrollo |

El Scrum Master está por decidir ([issue #28](https://github.com/riverri/IS1/issues/28)).

## Quién usa la aplicación

| Actor | Qué puede hacer |
|---|---|
| **Visitante** (sin cuenta) | Ver el catálogo de partidos con sus cuotas, las fichas de los equipos, los mercados a largo plazo, el ranking y los perfiles públicos. Para apostar tiene que registrarse. |
| **Jugador** (rol `USUARIO`) | Apostar (simples, combinadas y a largo plazo), seguir sus apuestas, cancelarlas o cambiar el importe antes de que empiece el partido, ver su historial y estadísticas, recibir avisos, competir en el ranking y en ligas privadas, ponerse límites y eliminar su cuenta. |
| **Creador de apuestas** (rol `CREADOR`) | El administrador: da de alta competiciones, equipos, jugadores y partidos; introduce resultados (que resuelven y pagan las apuestas); suspende, anula o corrige eventos; gestiona los mercados a largo plazo; fija límites y ve las cuentas de la casa. **No puede apostar.** |

## Qué tiene

- **Cuotas propias**, calculadas con la calidad de los equipos, su forma y el dinero apostado ([Algoritmo de cuotas](Algoritmo-de-cuotas)).
- **Tres tipos de apuesta:** simple, combinada y a largo plazo (Balón de Oro, campeón de liga…).
- **Más mercados en fútbol:** doble oportunidad, más/menos de 2,5 goles y ambos marcan.
- **Resolución automática:** al introducir el resultado, las apuestas se resuelven y se pagan solas.
- **Datos reales** de LaLiga y la Champions con la API de football-data.org ([API de datos deportivos](API-de-datos-deportivos)).
- **Banco de estadísticas:** ficha de cada equipo, gráfico de evolución, plantilla, alineación probable y cara a cara.
- **Parte social:** ranking, perfiles públicos y ligas privadas con código.
- **Juego responsable:** límites diarios y semanales, pausas y baja de la cuenta.
- **Deportes:** fútbol, baloncesto, tenis, Fórmula 1 y MotoGP.

## En cifras

| | |
|---|---|
| Historias de usuario | 53 escritas: 45 hechas y 8 descartadas |
| Criterios de aceptación | 107 (94 de historias hechas: 91 con prueba automática y 3 que se comprueban a mano) |
| Código | 136 clases Java en 12 paquetes, 30 plantillas HTML |
| Pruebas | 276 pruebas automáticas en 44 clases, con H2 y con PostgreSQL |
| Base de datos | 10 migraciones Flyway (V1–V10) |

## Por dónde empezar

1. [Conceptos básicos](Conceptos-básicos): cómo funciona una web y qué hace cada tecnología.
2. [Estructura del repositorio](Estructura-del-repositorio) y [Paquetes y clases](Paquetes-y-clases): dónde está cada cosa.
3. [Funcionalidades](Funcionalidades): cómo funciona cada parte por dentro.
4. [Ejecutar y desplegar](Ejecutar-y-desplegar): arrancarla en tu ordenador.
5. [Cómo contribuir](Cómo-contribuir): ramas, commits y pull requests.

Las palabras técnicas están explicadas en el [Glosario](Glosario).
