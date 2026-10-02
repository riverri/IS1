# Sprint 8 · 02/10 – 16/10

## Sprint Planning (02/10)
- **Asistentes:** Jing Li
- **Sprint Goal:** el creador puede corregir los eventos que ha creado mal, cada usuario gestiona sus datos y puede ver el perfil de los demás jugadores; además, el proyecto tiene sus diagramas UML y sus métricas de Scrum.
- **Velocidad media de los sprints anteriores:** 21 puntos · **Puntos comprometidos:** 12
- **Mejora de la retrospectiva anterior incluida:** _(pendiente de la retrospectiva del Sprint 7)_

| Historia | Descripción | Fila backlog | Puntos | Responsable | Estado |
|---|---|---|---|---|---|
| HU-46 | Editar y borrar eventos creados por error | 29 | 3 | Jing Li | Hecho |
| HU-47 | Cambiar nombre y contraseña desde *Mi cuenta* | 30 | 2 | Jing Li | Hecho |
| HU-48 | Perfil público de cada jugador desde el ranking | 31 | 2 | Jing Li | Hecho |
| — | Diagramas UML: casos de uso, arquitectura, clases, estados, secuencia y modelo de datos | — | 3 | Jing Li | Hecho |
| — | Métricas: velocidad y progreso acumulado por sprint | — | 2 | Jing Li | Hecho |

### Editar y borrar eventos (HU-46)
- **Dónde:** en *Gestión → evento*, con los botones *Editar* y *Borrar*, mientras el evento no haya empezado y no esté finalizado ni anulado.
- **Qué se puede editar:** el local, el visitante, la fecha y la fase. La competición no cambia.
- **Si ya hay apuestas:**
  - solo se pueden cambiar la fecha y la fase, porque cambiar los equipos cambiaría el sentido de esas apuestas;
  - no se puede borrar el evento: hay que anularlo, y se devuelve el importe.
- **Partidos que llegan de la API:** se pueden editar, pero la siguiente sincronización vuelve a poner la fecha oficial. La página lo avisa.

### Cambiar nombre y contraseña (HU-47)
- **Dónde:** en *Mi cuenta*, con dos formularios.
- **El nombre** sigue las mismas reglas que en el registro y es el que se ve en el ranking.
- **Para cambiar la contraseña** hay que escribir la actual. La nueva sigue las reglas del registro (8 a 72 caracteres) y se repite para confirmarla.

### Perfil público (HU-48)
- **Dónde:** pulsando un nombre en el ranking se abre `/jugadores/{id}`, que es público, como el ranking.
- **Muestra:**
  - el puesto por saldo;
  - el saldo;
  - las apuestas en juego y las resueltas;
  - el porcentaje de aciertos, el balance y la rentabilidad.
- **No muestra** sus apuestas ni su email.
- **El creador de apuestas** no juega, así que no tiene perfil.

### Documentación
- [Diagramas UML](../../diseno/uml.md): casos de uso, arquitectura por capas, clases del dominio, estados (evento, apuesta y mercado), secuencias (apostar una combinada y resolver un partido) y modelo de datos. Están hechos en Mermaid, que GitHub dibuja directamente.
- [Métricas](../metricas.md): velocidad por sprint y progreso acumulado, con el script que genera las gráficas.

## Dailies
- **02/10**: HU-46, HU-47 y HU-48, pruebas, diagramas UML y métricas.

## Sprint Review (16/10)
- Historias completadas:
- Historias no completadas (vuelven al backlog):
- Feedback del PO/profesor:

## Retrospectiva (16/10)
- **Bien:**
- **A mejorar:**
- **Acciones para el próximo sprint:**
