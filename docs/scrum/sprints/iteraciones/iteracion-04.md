# Iteración 4 · 01/10/2026 · Sprint 1

> **Iteración de desarrollo del [Sprint 1](../sprint-1.md)** (19/09 – 3/10). Esta acta se escribió como «Sprint 4» con el formato de un sprint, pero no lo es: las doce iteraciones se hicieron el 1 y el 2 de octubre, dentro del Sprint 1. Las retrospectivas y las reviews con el Product Owner son las de cada sprint, no las de cada iteración.

## Planificación (01/10)
- **Hecha por:** Jing Li
- **Objetivo:** un usuario puede juntar selecciones de varios partidos en un boleto, ver el multiplicador total y apostar una combinada que se resuelve sola.
- **Puntos comprometidos:** 18

| Historia | Descripción | Fila backlog | Puntos | Responsable | Estado |
|---|---|---|---|---|---|
| HU-28 | Boleto con selecciones de varios eventos; no se repite evento; con una sola es simple | 15, 18 | 5 | Jing Li | Hecho |
| HU-29 | Multiplicador total y ganancia potencial; avisar si cambia una cuota antes de confirmar | 15, 18 | 5 | Jing Li | Hecho |
| HU-30 | Resolución de combinadas: perdida si falla una, ganada si aciertan todas, anulada = cuota 1,00 | 16 | 5 | Jing Li | Hecho |
| HU-07 | Límite de selecciones por combinada (parte de la historia) | 17 | 1 | Jing Li | Hecho (parcial) |
| — | Adaptar apuestas, resolución, historial y gestión al nuevo modelo | — | 2 | Jing Li | Hecho |

### Decisiones tomadas
- **Una apuesta tiene una o varias selecciones.** Una apuesta simple es una combinada de una sola selección, así no hay dos formas de guardar las apuestas.
- **El boleto se guarda en la sesión** hasta que se confirma. Al pulsar una cuota del catálogo se añade al boleto, y una barra flotante muestra el número de selecciones y el multiplicador.
- **Desde la ficha del partido** (pulsando en los equipos) se puede seguir haciendo una apuesta simple directa.
- **Pago:** importe × producto de las cuotas, redondeado hacia abajo a 2 decimales. Si se anula un evento, su selección cuenta con 1,00; si se anulan todas, se devuelve el importe.
- **Corrección de resultados:** cada apuesta guarda lo que ya se ha pagado, y al corregir se paga o se retira solo la diferencia.
- **Máximo de selecciones:** 10 por combinada, configurable en `application.properties` (`apuestas.limites.max-selecciones`).
- **Cambio en la base de datos:** las apuestas pasan a guardarse con sus selecciones. Para usar esta versión hay que **borrar la carpeta `datos/`**, con la aplicación parada.

## Registro del trabajo
- **01/10**: modelo de selecciones, boleto, resolución de combinadas y pantallas.

## Resultado (01/10)
- **Historias completadas:** HU-28, HU-29, HU-30 y HU-07 (parcial). Con ellas el **MVP queda completo**.
- **Historias no completadas:** ninguna.
- **Peticiones añadidas durante la iteración:**
  - Probar la web en clase con varias personas a la vez: se usará un enlace temporal (Cloudflare Tunnel) desde un portátil.
  - Siguiente prioridad: la API de datos reales.
