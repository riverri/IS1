# 0003 · Seguimiento del Sprint Backlog en el grafo del equipo

**Estado:** PROPUESTA (05/11/2026). El equipo la confirma en la Sprint Review del Sprint 4.

## Contexto
- [proceso.md](../scrum/proceso.md) preveía los milestones de GitHub para el Sprint Backlog, y la retrospectiva del Sprint 2 lo puso como acción. En el Sprint 3 no se llegaron a crear.
- El tablero de GitHub Projects enseña tarjetas sueltas: no se ve qué tarea depende de cuál, qué pruebas tiene cada historia ni cuánto trabajo lleva cada persona.

## Decisión
El Sprint Backlog se sigue en un grafo de tareas compartido: **https://grafo-better.vercel.app**.

| Qué | Cómo |
|---|---|
| Tareas | Cada historia y tarea es un nodo; las dependencias son flechas. Cada nodo tiene responsable, estado, prioridad, sprint, criterios de aceptación y pruebas |
| Acceso | Cada miembro entra con el correo con el que hace sus commits, con un enlace que llega por correo (sin contraseña) |
| Tiempo real | Los cambios de uno aparecen al momento en las pantallas de los demás |
| GitHub | Cada nodo puede enlazar su issue o pull request; el botón «Comprobar estados» trae su estado de GitHub |
| Product Owner | Un enlace de solo lectura, sin cuenta, que se saca en los ajustes del grafo |
| Otros módulos | Riesgos, burndown, carga de trabajo por persona, entregas y comentarios |
| Infraestructura | Supabase (base de datos y acceso) y Vercel (web), en sus planes gratuitos |

## Alternativas descartadas
- **Milestones de GitHub:** cuentan cuántos issues están cerrados, pero no enseñan dependencias, pruebas ni carga de trabajo.
- **Solo GitHub Projects:** mismo problema, y cada tarjeta necesita un issue aunque sea una tarea de minutos.

## Consecuencias
- Los issues y los pull requests siguen en GitHub: el grafo los enlaza, no los sustituye.
- El estado de GitHub no se trae solo: hay que pulsar «Comprobar estados».
- El plan gratuito de Supabase envía pocos correos de acceso por hora. Como la sesión se queda abierta, solo afecta a la primera entrada de cada uno.
