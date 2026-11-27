# Seguimiento de la RTF del 27/11/2026

Acta: [2026-11-27-acta-rtf.pdf](2026-11-27-acta-rtf.pdf). El otro grupo revisó la documentación de requisitos, la gestión de riesgos y el proceso de trabajo sobre el commit `c21b72d` de `main`. **Dictamen: aceptado con cambios** (21/27, 77,36 %).

Esta tabla recoge qué se ha hecho con cada compromiso del acta y qué falta.

| Área | Compromiso | Qué se ha hecho (27/11) | Qué falta |
|---|---|---|---|
| Identificadores y contadores | Resolver la colisión HU-93 / HU-114 | HU-114 es «Exportar mi historial» (antes HU-93) en el código, las pruebas y la documentación. HU-93 se descartó el 27/11 porque no era una historia de usuario; su número se queda como hueco | — |
| | Cuadrar los totales de historias y criterios entre `README.md`, la wiki e `historias-de-usuario.md` | Los mismos totales en las historias, la wiki (Inicio e Historias de usuario) y el Product Backlog. El `README.md` no da totales. Desde el 28/11 se calculan con `scripts/totales.py` ([totales.md](../requisitos/totales.md)) y la CI comprueba que están al día | — |
| DoD y trazabilidad | Incorporar HU-54 y HU-111 a HU-114 al Product Backlog y a `trazabilidad.md` | HU-54 tiene fila propia (59) y sección en la trazabilidad. HU-102 a HU-113 figuran como ampliación de las filas 33 y 42. HU-114 ya tenía fila (53) y trazabilidad. HU-111 a HU-113 ya estaban en la trazabilidad. Las pruebas de CA-111.4, CA-112.2 y CA-113.5 se añadieron el 28/11 | — |
| | Aclarar el estado real de las historias retiradas de `main` | HU-55 a HU-62 se quitaron de `main` el 06/11 y volvieron el 21/11 (#125 y #131): están integradas, y ya no figuran como «pendientes de subir» | — |
| | Estimar las propuestas pendientes (HU-93 y HU-102 a HU-113) | HU-93 ya no hace falta estimarla (descartada) | **Estimar HU-102 a HU-113:** sus autores, en la próxima planning |
| Depuración de estimaciones | Corregir los valores fuera de la escala Fibonacci | HU-03 pasa de 11 a 13, HU-05 de 4 a 5 y HU-10 de 6 a 5, en las fichas, el resumen y el Product Backlog | — |
| | Retirar las líneas, horas y costes de las 8 Won't | HU-15, HU-16 y HU-38 a HU-43 conservan los puntos con los que se estimaron, pero ya no tienen líneas, horas ni coste, ni cuentan en esos totales | — |
| Gestión de riesgos | Eliminar o sincronizar `TablaRiesgos.md` | Borrada. El único registro es la página [Gestión de riesgos](https://github.com/riverri/IS1/wiki/Gestión-de-riesgos) | — |
| | Centralizar el seguimiento de las retrospectivas | Sección 6 de Gestión de riesgos: todo lo apuntado en los sprints 3, 4 y 5, y lo de esta semana (memoria de Render, HU-85 y HU-93, esta RTF) | Añadir lo que salga en cada retrospectiva |
| | Planes de contingencia de los 8 riesgos altos | Sección 3.5 de Gestión de riesgos: disparador, plan de contingencia y responsable de R02, R04, R05, R06, R10, R16, R19 y R20 | — |
| | Cerrar los huecos de `sprint-4.md` y `sprint-5.md` | Sprint 4: horas reales de las dos últimas tareas (sacadas de Git) y «qué fue bien y qué mejorar». Sprint 5: acciones de la retrospectiva anterior y horas estimadas de cada historia | Sprint 4: fecha de la retrospectiva. Sprint 5 (termina el 01/12): capacidad del equipo, horas reales, dailies, review y retrospectiva |
| Flujo de trabajo en Git | Proteger la rama `main` para que solo se cambie mediante pull request revisado | — | **Decisión del equipo:** activar la protección de `main` en GitHub (ajustes del repositorio). Hace falta permiso de administrador y afecta a todos |
