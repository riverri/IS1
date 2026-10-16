# Gestión de riesgos

Plan de Reducción, Supervisión y Gestión del Riesgo (**RSGR**) del proyecto, siguiendo el método de **Boehm** con la tabla de probabilidad y consecuencias **SQAS-SEI**. Cubre el proyecto actual y la ampliación (sprints 2 a 7, hasta el 18/12/2026) y se revisa en cada retrospectiva.

Versión en PDF: [`docs/scrum/gestion-riesgos.pdf`](https://github.com/riverri/IS1/blob/main/docs/scrum/gestion-riesgos.pdf)

## 1. Introducción

Un **riesgo** es todo aquello que pueda afectar negativamente al proyecto. No todos preocupan igual, así que este plan identifica los riesgos, los valora, se queda con los más graves y define qué hacer con cada uno.

Se sigue una **estrategia proactiva**: los riesgos se identifican y valoran antes del trabajo técnico de la ampliación, se priorizan y se prepara un plan para evitarlos, con planes de contingencia por si se hacen reales. Lo contrario sería la estrategia reactiva, que solo "apaga fuegos" cuando ya hay un problema.

| Fase | Paso | Dónde |
|---|---|---|
| Valoración del riesgo | Identificación | Sección 2 |
| | Análisis | Sección 3 |
| | Priorización | Sección 4 |
| Control del riesgo | Planificación | Secciones 5 y 6 |
| | Resolución | Sección 7 |
| | Monitorización | Sección 8 |

**Contexto:**
- Equipo de cinco estudiantes. El profesor actúa como Product Owner y el Scrum Master es Carlos Jurado.
- Scrum con sprints de dos semanas. Quedan los sprints 3 a 6 y un Sprint 7 corto.
- La aplicación actual tiene 45 historias hechas y 276 pruebas automáticas. El Product Owner ha pedido una ampliación grande.
- La nota es individual: proyecto (40 %), defensa sobre lo aprendido en el proyecto (30 %) y examen (30 %).

## 2. Identificación de riesgos

Se ha usado como lista de comprobación el **Top 10 de Boehm** y se han revisado personal, planificación, requisitos, tecnología, despliegue y evaluación. Tipos: **P** = de proyecto (amenaza el plan), **T** = técnico (amenaza la calidad), **N** = de negocio (amenaza la viabilidad: aprobar y aprender).

| ID | Riesgo | Tipo | Descripción | Top 10 de Boehm |
|---|---|:-:|---|---|
| R01 | Participación desigual | P | El trabajo se concentra en pocos miembros y el resto aporta poco. El proyecto depende de una o dos personas. | Deficiencias del personal |
| R02 | Ausencias de miembros | P | Exámenes de otras asignaturas, enfermedad u otras obligaciones reducen la disponibilidad en momentos clave. | Deficiencias del personal |
| R03 | Planificación poco realista | P | La velocidad de 25 puntos por sprint es una hipótesis; si la real es menor, no se termina lo comprometido. | Planificaciones y presupuestos poco realistas |
| R04 | Alcance excesivo y chapado | P | Querer hacer todas las épicas, o añadir detalles que nadie ha pedido, deja sin terminar lo importante. | Chapado |
| R05 | Requisitos poco claros o cambiantes | P | El Product Owner no ha concretado la ampliación; pueden aparecer cambios tarde o criterios mal entendidos. | Continua corriente de cambios en los requisitos |
| R06 | Documentación que no refleja la realidad | P | Las actas de los sprints no cuadran con el calendario real de la wiki, y faltan reviews y retrospectivas. | — |
| R07 | Incumplir las normas de integridad académica | N | Usar herramientas o materiales no autorizados expresamente por el profesorado en entregas evaluables (ficha docente). | — |
| R08 | Miembros que no conocen el proyecto | N | Quien no ha trabajado en una parte no sabe explicarla, y la defensa es individual. | Deficiencias del personal (formación cruzada) |
| R09 | Conflictos al integrar cambios | T | Ramas que tocan los mismos archivos, o dos migraciones con el mismo número, rompen `main` o bloquean pull requests. | — |
| R10 | Dependencia de la API externa | T | El plan gratuito de football-data.org limita las peticiones, no da datos en directo y puede cambiar o caer. | Deficiencias en componentes externos |
| R11 | Directo y cash-out más difíciles de lo previsto | T | Cuotas en tiempo real y valor de cash-out son técnicamente nuevos para el equipo. | Exprimir las capacidades; tiempo real |
| R12 | Regresiones al ampliar | T | Las funciones nuevas rompen otras que ya funcionaban (resolución de apuestas, saldos). | — |
| R13 | Caída del despliegue en la demo | T | Render o Neon (planes gratuitos) fallan, tardan en arrancar o cambian sus condiciones el día de una review. | Deficiencias en componentes externos |
| R14 | Fallos de seguridad en las funciones nuevas | T | Comentarios, roles de moderador o perfiles públicos abren huecos (XSS, acceso a datos de otros). | — |
| R15 | Rendimiento insuficiente | T | Las consultas del ranking fantasy o de la exposición de la casa se vuelven lentas con más datos. | Deficiencias en rendimiento |
| R16 | Pérdida de datos del servidor | T | Se pierden los datos de la base de datos de Neon (error humano o del servicio). | — |
| R17 | Construir algo que el Product Owner no valora | N | La ampliación no es lo que el profesor esperaba con "mucho más grande". | Desarrollo de funciones erróneas |
| R18 | Impacto social: juego compulsivo | N | Directo, cash-out y rachas pueden incitar a apostar más, aunque sea con moneditas. | — |
| R19 | Interfaz poco usable | T | Las pantallas nuevas (quiniela, fantasy, directo) se ven mal en el móvil o son confusas. | Desarrollo erróneo del interfaz |
| R20 | Filtración de credenciales | T | La clave de la API o las contraseñas del servidor acaban en el repositorio o en capturas compartidas. | — |

Según el conocimiento: R01 a R06 y R08 son **conocidos** (el equipo ya los ha notado); el resto son **desconocidos** que han salido al seguir el proceso de identificación. Los **impredecibles** se tratarán según aparezcan.

## 3. Análisis de riesgos

La tabla SQAS-SEI original habla de meses de retraso y porcentaje de coste. Para un proyecto de un cuatrimestre se ha adaptado a sprints y a historias:

| Probabilidad | En este proyecto | | Gravedad | En este proyecto |
|---|---|---|---|---|
| **Frecuente** (5) | Pasará varias veces en el cuatrimestre | | **Catastrófica** (5) | Suspender o no poder entregar el proyecto |
| **Probable** (4) | Se espera que pase al menos una vez | | **Crítica** (4) | Más de un sprint de retraso, o quedan fuera historias Must |
| **Ocasional** (3) | Podría pasar en algún momento | | **Seria** (3) | Menos de un sprint de retraso, o quedan fuera historias Should |
| **Remota** (2) | Poco probable, aunque concebible | | **Menor** (2) | Unos días de retraso, o quedan fuera historias Could |
| **Improbable** (1) | Probabilidad casi nula | | **Despreciable** (1) | Sin impacto apreciable |

| ID | Riesgo | Probabilidad | Gravedad | Motivo |
|---|---|---|---|---|
| R01 | Participación desigual | Frecuente | Crítica | Ya ha pasado en los primeros sprints |
| R02 | Ausencias de miembros | Probable | Seria | Diciembre tiene exámenes de otras asignaturas |
| R03 | Planificación poco realista | Probable | Crítica | No hay velocidad real medida en la ampliación |
| R04 | Alcance excesivo y chapado | Probable | Seria | 173 puntos deseados frente a unos 110 de capacidad |
| R05 | Requisitos poco claros o cambiantes | Probable | Seria | El Product Owner solo ha pedido "más grande" |
| R06 | Documentación que no refleja la realidad | Probable | Seria | Ya hay contradicciones entre actas y wiki |
| R07 | Incumplir las normas de integridad | Ocasional | Catastrófica | La ficha prevé la pérdida de la convocatoria |
| R08 | Miembros que no conocen el proyecto | Probable | Crítica | La defensa es el 30 % de la nota de cada uno |
| R09 | Conflictos al integrar cambios | Probable | Menor | Cinco épicas en paralelo; se arreglan en horas |
| R10 | Dependencia de la API externa | Ocasional | Seria | Plan gratuito con límites |
| R11 | Directo y cash-out difíciles | Probable | Seria | Tecnología nueva para el equipo |
| R12 | Regresiones al ampliar | Ocasional | Seria | Hay 276 pruebas que lo detectan |
| R13 | Caída del despliegue en la demo | Ocasional | Seria | Servicios gratuitos sin garantías |
| R14 | Fallos de seguridad nuevos | Ocasional | Seria | Contenido escrito por usuarios y roles nuevos |
| R15 | Rendimiento insuficiente | Remota | Menor | Pocos usuarios y consultas ya agregadas |
| R16 | Pérdida de datos del servidor | Remota | Seria | Neon permite restaurar; los datos se pueden recrear |
| R17 | Algo que el Product Owner no valora | Ocasional | Crítica | La ampliación aún no se ha validado con él |
| R18 | Juego compulsivo | Ocasional | Seria | Las funciones nuevas son más adictivas |
| R19 | Interfaz poco usable | Ocasional | Menor | Ya hay diseño adaptado al móvil |
| R20 | Filtración de credenciales | Ocasional | Seria | Ya se ha compartido una captura con una clave visible |

## 4. Priorización de riesgos

Se combinan las tres técnicas del tema:
- el **nivel de riesgo SQAS-SEI**, que sale de cruzar probabilidad y gravedad;
- la **exposición**, que es probabilidad × gravedad (de 1 a 5 cada una);
- la **tabla de riesgo ordenada** con una línea de corte.

### Matriz de nivel de riesgo

| Gravedad \ Probabilidad | Frecuente | Probable | Ocasional | Remota | Improbable |
|---|---|---|---|---|---|
| **Catastrófica** | 🟥 IN | 🟥 IN | 🟥 IN · **R07** | 🟧 H | 🟨 M |
| **Crítica** | 🟥 IN · **R01** | 🟥 IN · **R03 R08** | 🟧 H · **R17** | 🟨 M | 🟩 L |
| **Seria** | 🟧 H | 🟧 H · **R02 R04 R05 R06 R11** | 🟨 M · R10 R12 R13 R14 R18 R20 | 🟩 L · R16 | ⬜ T |
| **Menor** | 🟨 M | 🟨 M · R09 | 🟩 L · R19 | ⬜ T · R15 | ⬜ T |
| **Despreciable** | 🟨 M | 🟩 L | ⬜ T | ⬜ T | ⬜ T |

**IN** Intolerable: no puede obviarse su gestión · **H** Alto: controlar, supervisar y tener plan de contingencia · **M** Medio: debería controlarse · **L** Bajo: efectos asumibles · **T** Tolerable: si sucede, no importa.

### Tabla de riesgo ordenada

| Orden | ID | Riesgo | Nivel | Exposición | Estrategia |
|---:|---|---|:-:|:-:|---|
| 1 | R01 | Participación desigual | 🟥 IN | 20 | Controlar |
| 2 | R03 | Planificación poco realista | 🟥 IN | 16 | Controlar |
| 3 | R08 | Miembros que no conocen el proyecto | 🟥 IN | 16 | Controlar |
| 4 | R07 | Incumplir las normas de integridad | 🟥 IN | 15 | Evitar |
| 5 | R17 | Algo que el Product Owner no valora | 🟧 H | 12 | Controlar |
| 6 | R11 | Directo y cash-out difíciles | 🟧 H | 12 | Controlar |
| 7 | R04 | Alcance excesivo y chapado | 🟧 H | 12 | Evitar |
| 8 | R05 | Requisitos poco claros o cambiantes | 🟧 H | 12 | Controlar |
| 9 | R02 | Ausencias de miembros | 🟧 H | 12 | Controlar |
| 10 | R06 | Documentación que no refleja la realidad | 🟧 H | 12 | Controlar |
| | | **— Línea de corte: por encima, plan detallado; por debajo, medidas básicas —** | | | |
| 11 | R10 | Dependencia de la API externa | 🟨 M | 9 | Controlar |
| 12 | R12 | Regresiones al ampliar | 🟨 M | 9 | Controlar |
| 13 | R13 | Caída del despliegue en la demo | 🟨 M | 9 | Controlar |
| 14 | R14 | Fallos de seguridad nuevos | 🟨 M | 9 | Controlar |
| 15 | R18 | Juego compulsivo | 🟨 M | 9 | Controlar |
| 16 | R20 | Filtración de credenciales | 🟨 M | 9 | Evitar |
| 17 | R09 | Conflictos al integrar cambios | 🟨 M | 8 | Controlar |
| 18 | R16 | Pérdida de datos del servidor | 🟩 L | 6 | Asumir |
| 19 | R19 | Interfaz poco usable | 🟩 L | 6 | Asumir |
| 20 | R15 | Rendimiento insuficiente | ⬜ T | 4 | Asumir |

**Principio de Pareto.** Con 20 riesgos, dedicar entre 3 y 7 pasos a cada uno sería inviable. Siguiendo la regla 80-20, el plan detallado se reserva a los **4 intolerables** (el 20 % de la lista, donde se concentra la mayor parte del riesgo real) y a los **6 de nivel alto**. Los de nivel medio reciben medidas básicas; los bajos y tolerables se asumen.

| Estrategia | Significado | Riesgos |
|---|---|---|
| **Evitar** | Elegir una alternativa con menos riesgo | R07, R04, R20 |
| **Controlar** | Reducir la probabilidad o el impacto | R01, R02, R03, R05, R06, R08, R09–R14, R17, R18 |
| **Asumir** | Aceptar que ocurra y tener solo plan de contingencia | R15, R16, R19 |
| **Transferir** | Compartir el riesgo con otro | Ninguno: no hay terceros a quienes trasladarlo |

## 5. Reducción, supervisión y gestión de cada riesgo

Para cada riesgo por encima de la línea de corte:
- **Reducción:** pasos para que no ocurra o haga menos daño.
- **Supervisión:** cómo se sabe si se está haciendo real, con un **disparador** concreto que activa la contingencia.
- **Gestión:** el plan de contingencia si ocurre.

### R01 · Participación desigual 🟥 IN
- **Estrategia:** controlar · **Responsable:** Scrum Master
- **Reducción:**
  - un responsable por épica;
  - al menos un pull request por persona y sprint, revisado por otra persona;
  - como mucho una tarea en curso por persona;
  - historias repartidas con nombre en la Sprint Planning;
  - programación en pareja en las historias grandes.
- **Supervisión:** pull requests y puntos completados por persona en cada sprint (pestaña *Insights* de GitHub y tablero). **Disparador:** un miembro termina el sprint sin ningún PR fusionado, o una sola persona hace más del 50 % de los puntos.
- **Gestión:**
  - en la daily, sus tareas se reasignan a otra persona en pareja con él;
  - en la retrospectiva se habla abiertamente y se acuerda una acción;
  - si se repite dos sprints seguidos, se comunica al Product Owner, porque la nota del proyecto es individual.

### R03 · Planificación poco realista 🟥 IN
- **Estrategia:** controlar · **Responsable:** Scrum Master
- **Reducción:**
  - Planning Poker entre todos;
  - capacidad calculada con las horas reales de cada uno;
  - desarrollo incremental, con las historias Must primero;
  - tareas de un día como máximo;
  - velocidad recalculada al final de cada sprint.
- **Supervisión:** burndown en cada daily y velocidad real frente a la prevista. **Disparador:** a mitad de sprint está hecho menos del 40 % de los puntos, o la velocidad de un sprint queda por debajo de 20.
- **Gestión:**
  - recortar alcance según MoSCoW, primero las Could y luego las Should;
  - las historias sin terminar vuelven al backlog;
  - renegociar la entrega final con el Product Owner.

### R08 · Miembros que no conocen el proyecto 🟥 IN
- **Estrategia:** controlar · **Responsable:** todo el equipo
- **Reducción:**
  - formación cruzada: cada pull request lo revisa alguien de otra épica;
  - en la review, cada responsable explica su épica a los demás;
  - rotar las historias pequeñas entre épicas;
  - usar la guía y la wiki como material de estudio.
- **Supervisión:** simulacro de defensa en la retrospectiva de los sprints 4 y 6: cada miembro explica en dos minutos una parte que no ha programado. **Disparador:** alguien no sabe explicar la arquitectura, el proceso o una épica que no es la suya.
- **Gestión:** sesiones de repaso por parejas en el Sprint 7, centradas en proceso, requisitos, planificación y modelado, con preguntas de práctica.

### R07 · Incumplir las normas de integridad académica 🟥 IN
- **Estrategia:** evitar · **Responsable:** todo el equipo
- **Reducción:**
  - confirmar con el profesor, **por escrito**, qué herramientas y materiales están autorizados;
  - guardar esa confirmación en la wiki;
  - que cada miembro sea autor real de su trabajo y lo entienda (ver R08).
- **Supervisión:** revisar los avisos y normas de la asignatura antes de cada entrega. **Disparador:** no hay confirmación escrita antes de la primera entrega de la ampliación, o cambian las normas.
- **Gestión:** dejar de usar en las entregas lo que no esté autorizado, rehacer esas partes y explicarlo con transparencia al profesor antes de que lo detecte él.

### R17 · Construir algo que el Product Owner no valora 🟧 H
- **Estrategia:** controlar · **Responsable:** responsables de épica
- **Reducción:**
  - presentar las épicas al Product Owner en el Sprint 2, antes de programar;
  - Sprint Review con él en cada sprint, con prototipos y demos;
  - empezar cada épica por la pieza más visible.
- **Supervisión:** comentarios del Product Owner en el acta de cada review y porcentaje de historias aceptadas. **Disparador:** rechaza una épica o dice que no es lo que esperaba.
- **Gestión:** repriorizar el backlog en la siguiente planning; si hace falta, sustituir la épica.

### R11 · Directo y cash-out más difíciles de lo previsto 🟧 H
- **Estrategia:** controlar · **Responsable:** responsable de la épica E2
- **Reducción:**
  - simulador de partidos desde el primer sprint;
  - desarrollo en cadena: en juego → cuotas → apuestas → cash-out;
  - investigación previa (*spike*) de la fórmula del cash-out en el Sprint 5.
- **Supervisión:** estado de HU-63 y HU-64 al final del Sprint 5. **Disparador:** las cuotas en directo no están terminadas al cerrar el Sprint 5.
- **Gestión:** el cash-out pasa a Should; el directo se entrega con el simulador y con cuotas simplificadas.

### R04 · Alcance excesivo y chapado 🟧 H
- **Estrategia:** evitar · **Responsable:** Scrum Master con el Product Owner
- **Reducción:**
  - MoSCoW y "fregado de requisitos";
  - nada se programa sin historia ni criterios de aceptación;
  - lo comprometido nunca supera la capacidad.
- **Supervisión:** puntos comprometidos frente a capacidad, y trabajo en los PR sin historia asociada. **Disparador:** se comprometen más puntos de los que caben, o aparece trabajo fuera del backlog.
- **Gestión:** congelar el alcance desde el Sprint 6: solo se termina y se arregla, no se empieza nada nuevo.

### R05 · Requisitos poco claros o cambiantes 🟧 H
- **Estrategia:** controlar · **Responsable:** responsables de épica
- **Reducción:**
  - criterios Dado / Cuando / Entonces revisados con el Product Owner;
  - refinamiento a mitad de sprint;
  - trazabilidad entre criterios y pruebas, para medir el impacto de los cambios;
  - umbral de cambio alto: un cambio entra por el backlog en la siguiente planning.
- **Supervisión:** cambios pedidos por sprint y dudas abiertas en las historias. **Disparador:** más de dos historias cambian después de la planning, o una llega a la planning sin criterios.
- **Gestión:** la historia sale del sprint y vuelve a refinamiento; si el cambio es urgente, se cambia por otra del mismo tamaño.

### R02 · Ausencias de miembros 🟧 H
- **Estrategia:** controlar · **Responsable:** Scrum Master
- **Reducción:**
  - calendario de exámenes de todos en la planning;
  - Sprint 7 corto y sin funcionalidades nuevas;
  - formación cruzada (R08);
  - trabajo siempre subido a una rama, nunca solo en un ordenador.
- **Supervisión:** disponibilidad anunciada en la planning y en cada daily. **Disparador:** alguien no puede trabajar más de tres días seguidos en un sprint.
- **Gestión:** reasignar sus tareas en la daily; si no hay capacidad, la historia vuelve al backlog y se avisa en la review.

### R06 · Documentación que no refleja la realidad 🟧 H
- **Estrategia:** controlar · **Responsable:** Scrum Master
- **Reducción:**
  - acta de cada evento el mismo día, en la wiki, con fechas y asistentes reales;
  - corregir las actas que no cuadran con el calendario;
  - rellenar las reviews y retrospectivas pendientes.
- **Supervisión:** lista de comprobación en cada review (acta de planning, dailies, review y retrospectiva). **Disparador:** falta el acta de algún evento al cerrar el sprint.
- **Gestión:** reconstruir el acta con el historial de GitHub y las notas del equipo antes de la siguiente entrega.

## 6. Riesgos de nivel medio, bajo y tolerable

| ID | Riesgo | Reducción | Contingencia |
|---|---|---|---|
| R10 | API externa 🟨 | Simulador; datos iniciales propios; tiempos de espera configurados; respetar el límite de peticiones | Trabajar solo con datos propios y el simulador |
| R12 | Regresiones 🟨 | Las 276 pruebas en el CI con H2 y PostgreSQL; pruebas nuevas en la Definition of Done | Revertir el pull request que lo rompe |
| R13 | Caída en la demo 🟨 | Despertar el servidor antes de la clase; ensayar la demo | Demo en local o con el túnel; vídeo de respaldo |
| R14 | Seguridad 🟨 | Escapar el texto de los usuarios, CSRF, comprobar permisos, pruebas de acceso | Desactivar la función hasta corregirla |
| R18 | Juego compulsivo 🟨 | Límites, pausas y avisos de juego responsable en todas las funciones nuevas; máximo diario de experiencia | Desactivar la función desde Gestión |
| R20 | Credenciales 🟨 | Claves solo en variables de entorno; revisar las capturas antes de compartirlas | Revocar la clave expuesta y pedir una nueva |
| R09 | Integración 🟨 | Pull requests pequeños; actualizar la rama con `main` a menudo; reservar los números de migración | Resolver el conflicto en pareja el mismo día |
| R16 | Pérdida de datos 🟩 | Se asume | Restaurar desde Neon o recrear con los datos iniciales; exportar antes de cada demo |
| R19 | Interfaz 🟩 | Se asume | Ajustar el diseño en el sprint siguiente |
| R15 | Rendimiento ⬜ | Se asume | Optimizar la consulta lenta si aparece |

## 7. Planificación temporal

| Sprint | Acciones | Riesgos |
|---|---|---|
| **2** · 5/10 – 16/10 | Aprobar este plan. Confirmar por escrito el uso de herramientas autorizado. Elegir los responsables de épica (el Scrum Master ya está elegido: Carlos Jurado). Presentar las épicas al Product Owner. Corregir actas y rellenar retrospectivas. Planning Poker de las Must. | R07, R01, R17, R06, R03, R05 |
| **3** · 19/10 – 30/10 | Capacidad con las horas reales. Reservar números de migración. Simulador de partidos (HU-67). Medir la velocidad real. | R03, R09, R11, R10 |
| **4** · 2/11 – 13/11 | Recalcular el plan con la velocidad real y recortar si hace falta. Primer simulacro de defensa. Revisión completa de este plan. | R03, R04, R08 |
| **5** · 16/11 – 27/11 | Investigación previa del cash-out. Calendarios de exámenes de diciembre. Revisión de seguridad de comentarios y moderación. | R11, R02, R14 |
| **6** · 30/11 – 11/12 | Congelar el alcance. Segundo simulacro de defensa. Ensayo de la demo final con el servidor. | R04, R08, R13 |
| **7** · 14/12 – 18/12 | Repaso por parejas. Exportar la base de datos y grabar el vídeo de respaldo. Cierre de este plan: qué riesgos se dieron y qué se aprendió. | R08, R13, R16 |

Las acciones continuas (revisión cruzada, un PR por persona y sprint, actas) se hacen en todos los sprints.

## 8. Monitorización

- **En cada daily:** el Scrum Master repasa los intolerables (R01, R03, R07 y R08): ¿alguno se está haciendo real?
- **En cada retrospectiva:**
  - se revisan todos los riesgos altos e intolerables;
  - se vuelven a valorar su probabilidad y su gravedad;
  - se comprueba que los pasos de reducción se están haciendo;
  - se añaden los riesgos nuevos.
- **Métricas:** velocidad y burndown (R03), pull requests por persona (R01), historias aceptadas por el Product Owner (R17), cambios de requisitos por sprint (R05), pruebas en verde en el CI (R12).
- **Registro:** cada revisión deja una línea en el acta del sprint con los riesgos que suben, bajan o se han hecho reales y la acción tomada. Esta página se actualiza al final de los sprints 4 y 7.

## 9. Resumen

**20 riesgos identificados: 4 intolerables y 6 de nivel alto.** Los más graves **no son técnicos**, sino de personas y de planificación:
- que el trabajo recaiga en pocos miembros (R01);
- que la velocidad prevista no sea real (R03);
- que no todos conozcan el proyecto para la defensa (R08);
- incumplir las normas de integridad académica (R07).

Los riesgos técnicos quedan en nivel medio porque el proyecto ya tiene pruebas automáticas, integración continua y un simulador previsto. La estrategia es **proactiva**: las acciones principales se toman en el Sprint 2, antes de empezar la ampliación, y los riesgos se revisan en cada daily y en cada retrospectiva.
