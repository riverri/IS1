# Sprint 5 · 18/11 – 01/12

> Acta del sprint, en curso. Ya está apuntado lo que se ha hecho; el resto lo rellena el equipo. Esta página se publica sola en la wiki desde `docs/scrum/sprints/sprint-5.md`: para cambiarla, edita ese archivo.

## Sprint Planning (_fecha_)
- **Asistentes:**
- **Sprint Goal:**
- **Capacidad del equipo (horas):**
- **Acciones de la retrospectiva anterior que se incluyen:**

## Sprint Backlog
| Historia o tarea | Horas reales | Horas estimadas |
|---|---|---|
| Seguimiento: HU-86 (equipos favoritos), HU-90 (alerta de cuota) y HU-91 (evolución de la cuota), fila 48 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| HU-87: supercuota, fila 49 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| HU-88: logros en el perfil, fila 50 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| HU-89: reto 1 contra 1 en las ligas, fila 51 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| HU-92: resumen semanal por correo, fila 52 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| HU-93: exportar mis apuestas y movimientos en CSV, fila 53 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| HU-94: modo oscuro, fila 54 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| HU-95: beneficio diario de la casa, fila 55 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| HU-96: estadísticas por competición, fila 56 ([#113](https://github.com/riverri/IS1/pull/113)) | | |
| Mejora del aspecto de la web: cabecera en una fila, saldo siempre visible en el móvil, página de apostar en dos columnas y el mismo ancho en toda la web ([#115](https://github.com/riverri/IS1/pull/115), [#116](https://github.com/riverri/IS1/pull/116)) | | |
| Accesibilidad: HU-97 a HU-101 (teclado, lector de pantalla, contraste y ampliar, no solo color y menos movimiento), fila 57 ([#120](https://github.com/riverri/IS1/pull/120)) | | |
| Informe de cobertura con JaCoCo y 13 pruebas más de HU-86 a HU-96. Arreglos: aceptar un reto de un partido ya empezado no devolvía el importe, y una alerta o supercuota de un partido que no existe daba un error 500 ([#121](https://github.com/riverri/IS1/pull/121)) | | |
| Datos de demostración para Render (`DEMO_DATOS=true`): jugadores con apuestas, partidos próximos, una supercuota y una liga; la comprobación de accesibilidad de la CI los usa | | |
| Depuración: errores 500 con ids inexistentes y nombres raros, retos que no se recalculaban al corregir ni se devolvían al dejar la liga, supercuota desde el boleto, bloqueo de apuestas y quinielas a la vez, resultados a mano que pisaba la API y límite de intentos con la contraseña actual ([#123](https://github.com/riverri/IS1/pull/123)) | | |
| Segunda depuración: sincronización con la API (eventos anulados, traspasos, fechas puestas a mano), partidos aplazados, quiniela con todo anulado, apuestas a la vez que se anula el partido, supercuota por debajo de la normal, alertas, avisos de corrección, `/ping` sin base de datos y perfil `compartir` ([#124](https://github.com/riverri/IS1/pull/124)) | | |
| | | |
| **Total** | | |

**Hecho hasta ahora:** las dieciséis historias (HU-86 a HU-101) están integradas en `main`, con sus 41 criterios de aceptación cubiertos por pruebas automáticas ([trazabilidad](https://github.com/riverri/IS1/blob/main/docs/requisitos/trazabilidad.md)). La fusión de las historias propuestas con `historias-de-usuario.md`, que el acta del Sprint 4 dejaba para este sprint, también está hecha ([#107](https://github.com/riverri/IS1/pull/107)).

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
| | | | |

### Qué fue bien y qué mejorar

### Acciones de mejora para el Sprint 6
| Acción | Responsable | Cómo se comprueba |
|---|---|---|
| | | |
