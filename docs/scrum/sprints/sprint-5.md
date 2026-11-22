# Sprint 5 · 18/11 – 01/12

> Acta del sprint, en curso. Ya está apuntado lo que se ha hecho; el resto lo rellena el equipo. Esta página se publica sola en la wiki desde `docs/scrum/sprints/sprint-5.md`: para cambiarla, edita ese archivo.

## Sprint Planning (19/11):

- **Asistentes:** Carlos Jurado, Carlos Martín-Salas, Jaime Martín , David Ortega, Jing Li
- **Sprint Goal:** Terminar las ampliaciones y diseñar más para dar oportunidades a todos los miembros del equipo.
- **Capacidad del equipo (horas):**
- **Acciones de la retrospectiva anterior que se incluyen:**


Los objetivos principales de este Sprint es terminar con las HU de las ampliaciones anteriores (HU55 - HU62 y HU74 - HU84). Además vamos a desarrollar e implementar nuevas funcionalidades que se detallarán en el Sprint Backlog para dar la oportunidad a todos los miembros de participar equitativamente en el desarrollo del proyecto. Además se seguirá trabajando en las mejoras propuestas en anteriores Sprints, además de minimizar los riesgos detectados en la gestión de riesgos y el plan RSGR.  Por último, se realizaran "auditorías" al grupo que desarrolla la app de RENFE y al grupo del plan de estudios, al mismo tiempo que nos realizaran "auditorías" otros dos grupos. Toda la documentación relativa a estos dos procesos será añadida a una nueva carpeta de la carpeta principal docs.

**NOTA**: Aunque en un primer momento se planificaron 6 Sprints por temas de tiempo y exámenes es posible que este sea el último Sprint que se desarrolle, se confirmará este detalle en el Sprint Review.

## Sprint Backlog
| Historia o tarea | Horas reales | Horas estimadas |
|---|---|---|
| Seguimiento: HU-86 (equipos favoritos), HU-90 (alerta de cuota) y HU-91 (evolución de la cuota), fila 48 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| HU-87: supercuota, fila 49 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| HU-88: logros en el perfil, fila 50 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| HU-89: reto 1 contra 1 en las ligas, fila 51 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| HU-92: resumen semanal por correo, fila 52 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| HU-114 (antes HU-93): exportar mis apuestas y movimientos en CSV, fila 53 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| HU-94: modo oscuro, fila 54 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| HU-95: beneficio diario de la casa, fila 55 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| HU-96: estadísticas por competición, fila 56 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| Mejora del aspecto de la web: cabecera en una fila, saldo siempre visible en el móvil, página de apostar en dos columnas y el mismo ancho en toda la web ([#115](https://github.com/riverri/IS1/pull/115), [#116](https://github.com/riverri/IS1/pull/116)) | | |
| Accesibilidad: HU-97 a HU-101 (teclado, lector de pantalla, contraste y ampliar, no solo color y menos movimiento), fila 57 ([#120](https://github.com/riverri/IS1/pull/120)) | | |
| Informe de cobertura con JaCoCo y 13 pruebas más de HU-86 a HU-96 y HU-114. Arreglos: aceptar un reto de un partido ya empezado no devolvía el importe, y una alerta o supercuota de un partido que no existe daba un error 500 ([#121](https://github.com/riverri/IS1/pull/121)) | | |
| Datos de demostración para Render (`DEMO_DATOS=true`): jugadores con apuestas, partidos próximos, una supercuota y una liga; la comprobación de accesibilidad de la CI los usa | | |
| Depuración: errores 500 con ids inexistentes y nombres raros, retos que no se recalculaban al corregir ni se devolvían al dejar la liga, supercuota desde el boleto, bloqueo de apuestas y quinielas a la vez, resultados a mano que pisaba la API y límite de intentos con la contraseña actual ([#123](https://github.com/riverri/IS1/pull/123)) | | |
| Segunda depuración: sincronización con la API (eventos anulados, traspasos, fechas puestas a mano), partidos aplazados, quiniela con todo anulado, apuestas a la vez que se anula el partido, supercuota por debajo de la normal, alertas, avisos de corrección, `/ping` sin base de datos y perfil `compartir` ([#124](https://github.com/riverri/IS1/pull/124)) | | |
| Tercera depuración: panel de quinielas del creador, avisos que no se veían, creador en páginas de jugador, alertas de aplazados, partidos de ejemplo en el pasado, tiempos de espera del correo, y una prueba aleatoria de que el dinero siempre cuadra ([#126](https://github.com/riverri/IS1/pull/126)) | | |
| Cuarta depuración: dobles clics en el casino y la fantasy, ligas con bloqueos que no se podían borrar, puntos de la fantasy con traspasos y jugadores borrados, puesto del perfil con empates, impresiones de anuncios y anuncios para visitantes ([revisión de fallos](https://github.com/riverri/IS1/wiki/Revisión-de-fallos#cuarta-depuración-21112026)) | | |
| Quinta depuración: cierre de jornada en la liga fantasy (ADR 0007), cobro anticipado con supercuota, subidas de importe en el juego responsable, partidos en juego con la fecha cambiada, límite de envíos, quinielas con anulados y cuentas eliminadas, plantillas de la API ([revisión de fallos](https://github.com/riverri/IS1/wiki/Revisión-de-fallos#quinta-depuración-21112026)) | | |
| Sexta depuración y optimización: robo de cuenta con el cambio de correo, despliegue sin `REMEMBER_ME_KEY`, navegadores que no se podían revocar, error 500 en las predicciones, búsqueda con tildes, cuentas eliminadas en predicciones y cripto; índices en PostgreSQL (portada de 1 s a 42 ms) ([revisión de fallos](https://github.com/riverri/IS1/wiki/Revisión-de-fallos#sexta-depuración-y-optimización-23112026)) | | |
| **Total** | | |

**Hecho hasta ahora:** las dieciséis historias (HU-86 a HU-92, HU-94 a HU-101 y HU-114) están integradas en `main`, con sus 41 criterios de aceptación cubiertos por pruebas automáticas ([trazabilidad](https://github.com/riverri/IS1/blob/main/docs/requisitos/trazabilidad.md)). La fusión de las historias propuestas con `historias-de-usuario.md`, que el acta del Sprint 4 dejaba para este sprint, también está hecha ([#107](https://github.com/riverri/IS1/pull/107)).

## Daily scrums
- **_fecha_:**

## Sprint Review (_fecha_)
- **Historias completadas:**
- **Historias no completadas (vuelven al Product Backlog):**
- **Horas reales frente a estimadas:**
- **Comentarios del Product Owner:**

## Sprint Retrospective (_fecha_)

### Seguimiento de las acciones del Sprint 4
| Acción | ¿Se cumplió? |
|---|---|
| | |

### Seguimiento de riesgos
| Riesgo | Cambio | Por qué | Acción |
|---|---|---|---|
| 8 · Conflictos al integrar cambios | Se materializó | El 21/11 se fusionaron cinco PR en un orden distinto del previsto: el baccarat no llegó a `main` y su migración quedó detrás de otra ya aplicada (ver abajo, I-01 en [Revisión de fallos](../../wiki/Revisión-de-fallos.md#al-integrar)) | Un PR cada vez, sin PR apilados, y el número de migración se revisa justo antes de fusionar |

### Qué fue bien y qué mejorar

> *Texto redactado por la IA (Claude) a partir de lo que pasó; el equipo puede personalizarlo.*

**Mejorar: PR fusionados en desorden (21/11).** Quedaban cinco PR de código listos: el arreglo del cash-out (#138), la liga fantasy (#133, migración V27), la vuelta del casino (#125), el baccarat (#132, migración V26) y los anuncios y movimientos (#131). El orden previsto era casino → baccarat → fantasy, pero se fusionaron en otro orden debido a un despiste por pate de uno de los miembros :

1. **Migraciones fuera de orden.** La fantasy (V27) entró antes que el baccarat (V26). Flyway no aplica una migración con número menor que la última que ya tiene la base de datos, así que al fusionar el baccarat la base de datos de Render no habría arrancado.
2. **PR apilado fusionado en la rama equivocada.** El baccarat se hizo encima de la rama del casino porque lo necesita, así que su PR apuntaba a `codigo/vuelve-casino` y no a `main`. Se fusionó 21 segundos después que el casino, antes de que GitHub cambiara su base a `main`. El baccarat acabó en la rama del casino, que ya estaba fusionada, y `main` se quedó sin él, aunque GitHub mostraba el PR como fusionado.

Además, los PR del casino y de los anuncios añadían una tarjeta en el mismo hueco del panel de gestión: el segundo en fusionarse habría dado conflicto.

**Cómo se solucionó:** la migración del baccarat se renumeró a V28. Se abrió un PR nuevo, ya contra `main` (#140). La tarjeta de anuncios se movió de sitio en #131. Antes de fusionar, se probó en local `main` con los PR que quedaban juntos: 584 pruebas en verde. Después se fusionaron de uno en uno, esperando al CI de cada uno, y se comprobó que `main` quedaba idéntico a lo probado. No se llegó a romper Render: el fallo se vio antes de que el baccarat entrara en `main`.

**Qué fue bien:** el CI y las pruebas permitieron comprobar la combinación antes de fusionarla. El fallo quedó en ramas y no llegó a producción.

### Acciones de mejora para el Sprint 6
| Acción | Responsable | Cómo se comprueba |
|---|---|---|
| Fusionar los PR de uno en uno: el siguiente solo cuando el CI de `main` del anterior esté en verde | Quien fusiona | Historial de `main` y de Actions |
| No fusionar PR apilados: cuando entra su base, se cambia su destino a `main` (o se abre otro) y se espera a que el CI vuelva a pasar | Autor del PR | La base del PR es `main` al fusionarlo |
| Comprobar el número de la migración justo antes de fusionar: tiene que ser mayor que la última de `main` | Autor del PR | `ls src/main/resources/db/migration/h2` frente a la rama |
