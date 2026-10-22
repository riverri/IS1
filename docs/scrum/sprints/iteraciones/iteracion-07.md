# Iteración 7 · 01/10/2026 · Sprint 1

> **Iteración de desarrollo del [Sprint 1](../sprint-1.md)** (19/09 – 3/10). Esta acta se escribió como «Sprint 7» con el formato de un sprint, pero no lo es: las doce iteraciones se hicieron el 1 y el 2 de octubre, dentro del Sprint 1. Las retrospectivas y las reviews con el Product Owner son las de cada sprint, no las de cada iteración.

## Planificación (01/10)
- **Hecha por:** Jing Li
- **Objetivo:** antes de apostar, el usuario puede consultar la ficha de cada equipo y el cara a cara de los rivales; recibe un aviso cuando se resuelven sus apuestas y puede cambiar el importe mientras el partido no haya empezado.
- **Puntos comprometidos:** 16

| Historia | Descripción | Fila backlog | Puntos | Responsable | Estado |
|---|---|---|---|---|---|
| HU-31 | Ficha de equipo: últimos resultados, balance, racha, clasificación y próximos partidos | 20 | 5 | Jing Li | Hecho |
| HU-33 | Cara a cara de los dos rivales en la página del partido | 20 | 3 | Jing Li | Hecho |
| HU-37 | Avisos dentro de la web cuando se gana, se pierde o se anula una apuesta | 23 | 5 | Jing Li | Hecho |
| HU-27 | Cambiar el importe de una apuesta antes de que empiece el evento | 14 | 2 | Jing Li | Hecho |
| — | Migración V4 de la base de datos | — | 1 | Jing Li | Hecho |

**Historias nuevas en el backlog:** HU-46, HU-47 y HU-48 (filas 29 a 31).

### Ficha de equipo (HU-31)
- Se abre pulsando un equipo en *Equipos*, en la página de un partido o en la propia ficha (los rivales).
- **Muestra:**
  - calidad y forma;
  - partidos jugados, victorias, empates y derrotas;
  - racha de los últimos 5 resultados;
  - últimos 10 partidos;
  - próximos partidos, con sus cuotas.
- **Clasificación:** se calcula con los resultados registrados en la web (3 puntos por victoria y 1 por empate; a igualdad de puntos, más victorias). No es la clasificación oficial: solo cuentan los partidos que tiene la web. Con la API activada salen todos los de LaLiga y la Champions.
- **El gráfico de evolución (HU-32)** queda en el backlog.

### Cara a cara (HU-33)
- Está en la página de apostar de cada partido.
- **Muestra:**
  - la racha de cada equipo;
  - el balance de los enfrentamientos anteriores entre los dos (victorias de cada uno y empates);
  - la lista de esos partidos.

### Avisos (HU-37)
- **Cuándo se avisa:** cuando una apuesta pasa a ganada, perdida o anulada, tanto al introducir el resultado a mano como al llegar de la API, al anular un evento o al resolver un mercado a largo plazo.
- **Cuándo no:** una combinada que sigue esperando otros partidos no avisa hasta que se decide. Si se corrige un resultado, el aviso empieza por "Resultado corregido".
- **En la web:**
  - una campana en la cabecera muestra los avisos sin leer;
  - en *Avisos* aparecen los últimos 50, con los nuevos destacados;
  - al abrir *Avisos* quedan todos leídos.
- **No se envían correos:** no hay servidor de correo.

### Cambiar el importe (HU-27)
- **Dónde:** en *Mis apuestas*, en las apuestas que todavía se pueden cancelar, con la opción *Cambiar importe*.
- **Qué pasa al cambiarlo:**
  - se cobra o se devuelve la diferencia;
  - se aplican los límites de apuesta;
  - **se aplica la cuota actual**. Si no, se podría apostar poco con una cuota alta y subir el importe cuando la cuota ya hubiera bajado.

### Cambios en la base de datos (migración V4)
- Tabla `notificacion`. Las bases de datos de la iteración 6 se actualizan solas al arrancar.

## Registro del trabajo
- **01/10**: historias nuevas en el backlog; ficha de equipo, cara a cara, avisos, cambiar importe, migración V4 y pruebas.

## Resultado (01/10)
- **Historias completadas:** HU-31, HU-33, HU-37 y HU-27 (16 puntos).
- **Historias no completadas:** ninguna.
- **Peticiones añadidas durante la iteración:**
  - Siguiente iteración: HU-46, HU-47 y HU-48.
  - Además, documentación de la asignatura: diagramas UML y gráficas de velocidad y progreso.
