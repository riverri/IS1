# Gestión de riesgos

Plan de Reducción, Supervisión y Gestión del Riesgo (**RSGR**) del proyecto. Versión del 1 de noviembre de 2026; sustituye a la del 16 de octubre y se revisa en cada retrospectiva.

Versión en PDF: [`docs/scrum/gestion-riesgos.pdf`](https://github.com/riverri/IS1/blob/main/docs/scrum/gestion-riesgos.pdf)

## 1. Introducción

Un **riesgo** es todo aquello que pueda afectar negativamente al proyecto. No todos preocupan igual, así que este plan identifica los riesgos, los valora, se queda con los más graves y define qué hacer con cada uno.

Se sigue una **estrategia proactiva**: los riesgos se identifican y valoran antes de que ocurran, se priorizan y se prepara un plan para evitarlos, con planes de contingencia por si se hacen reales. Lo contrario sería la estrategia reactiva, que solo «apaga fuegos» cuando ya hay un problema. El método es el de **Boehm**, con la tabla de probabilidad y consecuencias **SQAS-SEI**.

| Fase | Paso | Dónde |
|---|---|---|
| Valoración del riesgo | Identificación | Sección 2.1 |
| | Análisis | Sección 2.2 |
| | Priorización | Secciones 2.3 y 2.4 |
| Control del riesgo | Planificación | Secciones 3 y 4 |
| | Resolución | Sección 3 (gestión de cada riesgo) |
| | Monitorización | Sección 3 (supervisión) y sección 4 |

**Contexto del proyecto:**
- Equipo de cinco estudiantes. El profesor actúa como Product Owner y el Scrum Master es Carlos Jurado.
- Scrum con sprints de dos semanas hasta el 18/12/2026. Está terminando el Sprint 3 (21/10 – 3/11); quedan los sprints 4 a 6 y un Sprint 7 corto.
- La aplicación tiene 45 historias hechas y 283 pruebas automáticas. Casi todo el código se ha generado con IA: el Sprint 1 se programó en dos días, y en el Sprint 3 aparecieron ramas de la ampliación con más de 10.000 líneas añadidas en dos días.
- La nota es individual: proyecto (40 %), defensa sobre lo aprendido en el proyecto (30 %) y examen (30 %).

**Cambios respecto a la versión del 16 de octubre:**
- El documento sigue un índice nuevo: introducción, priorización, plan RSGR de los intolerables, planificación temporal y resumen.
- Se eliminan los antiguos R07 (incumplir las normas de integridad académica) y R18 (juego compulsivo), que no tienen sentido a ojos del proyecto.
- Se añaden cuatro riesgos: R19 (uso abusivo de la IA), R20 (subestimación de la capacidad de desarrollo de la IA), R21 (pérdida de noción de lo que la IA desarrolla) y R22 (pérdida de la rutina de trabajo por exámenes).
- Los demás se renumeran: R08 → R07, R09 → R08, R10 → R09, R11 → R10, R12 → R11, R13 → R12, R14 → R13, R15 → R14, R16 → R15, R17 → R16, R19 → R17, R20 → R18; R01 a R06 no cambian.
- Solo se desarrolla el plan RSGR de los riesgos intolerables (sección 3).

## 2. Priorización de riesgos del proyecto

Se identifican los riesgos, se valora la probabilidad y la gravedad de cada uno y se combinan las tres técnicas de priorización del tema:
- el **nivel de riesgo SQAS-SEI**, que sale de cruzar probabilidad y gravedad;
- la **exposición**, que es probabilidad × gravedad, con las dos escalas de 1 a 5;
- la **tabla de riesgo ordenada**, con una línea de corte.

### 2.1 Riesgos identificados

Se ha usado como lista de comprobación el **Top 10 de Boehm** y se han revisado personal, planificación, requisitos, tecnología, despliegue, evaluación y uso de la IA. Tipos: **P** = de proyecto (amenaza el plan), **T** = técnico (amenaza la calidad), **N** = de negocio (amenaza la viabilidad: aprobar y aprender).

| ID | Riesgo | Tipo | Descripción | Top 10 de Boehm |
|---|---|:-:|---|---|
| R01 | Participación desigual | P | El trabajo se concentra en pocos miembros y el resto aporta poco. El proyecto depende de una o dos personas. | Deficiencias del personal |
| R02 | Ausencias de miembros | P | Una persona concreta no puede trabajar durante varios días en un momento clave por exámenes, enfermedad u otras obligaciones. | Deficiencias del personal |
| R03 | Planificación poco realista | P | No hay una velocidad real medida del equipo: la del Sprint 1 no sirve porque se hizo con IA en dos días. Si la capacidad real es menor que la planificada, no se termina lo comprometido. | Planificaciones y presupuestos poco realistas |
| R04 | Alcance excesivo y chapado | P | Querer hacer todas las épicas, o añadir detalles que nadie ha pedido, deja sin terminar lo importante. | Chapado |
| R05 | Requisitos poco claros o cambiantes | P | El Product Owner no ha concretado la ampliación; pueden aparecer cambios tarde o criterios mal entendidos. | Continua corriente de cambios en los requisitos |
| R06 | Documentación que no refleja la realidad | P | Las actas no recogen lo que de verdad pasó: faltan dailies, reviews o retrospectivas, o no cuadran con el calendario. | — |
| R07 | Miembros que no conocen el proyecto | N | Quien no ha trabajado en una parte no sabe explicarla, y la defensa es individual. | Deficiencias del personal (formación cruzada) |
| R08 | Conflictos al integrar cambios | T | Ramas que tocan los mismos archivos, o dos migraciones con el mismo número, rompen main o bloquean pull requests. | — |
| R09 | Dependencia de la API externa | T | El plan gratuito de football-data.org limita las peticiones, no da datos en directo y puede cambiar o caer. | Deficiencias en componentes externos |
| R10 | Directo y cash-out más difíciles de lo previsto | T | Cuotas en tiempo real y valor de cash-out son técnicamente nuevos para el equipo. | Exprimir las capacidades; tiempo real |
| R11 | Regresiones al ampliar | T | Las funciones nuevas rompen otras que ya funcionaban (resolución de apuestas, saldos). | — |
| R12 | Caída del despliegue en la demo | T | Render o Neon (planes gratuitos) fallan, tardan en arrancar o cambian sus condiciones el día de una review. | Deficiencias en componentes externos |
| R13 | Fallos de seguridad en las funciones nuevas | T | Comentarios, roles de moderador o perfiles públicos abren huecos (XSS, acceso a datos de otros). | — |
| R14 | Rendimiento insuficiente | T | Las consultas del ranking o de la exposición de la casa se vuelven lentas con más datos. | Deficiencias en rendimiento |
| R15 | Pérdida de datos del servidor | T | Se pierden los datos de la base de datos de Neon por un error humano o del servicio. | — |
| R16 | Construir algo que el Product Owner no valora | N | La ampliación no es lo que el profesor esperaba con «mucho más grande». | Desarrollo de funciones erróneas |
| R17 | Interfaz poco usable | T | Las pantallas nuevas se ven mal en el móvil o son confusas. | Desarrollo erróneo del interfaz |
| R18 | Filtración de credenciales | T | La clave de la API o las contraseñas del servidor acaban en el repositorio o en capturas compartidas. | — |
| R19 | Uso abusivo de la IA | N | Se delega en la IA el trabajo que hay que aprender o decidir (código, documentación, actas, retrospectivas) sin criterio propio ni revisión. El proyecto avanza, pero el equipo no aprende lo que se evalúa en la defensa y en el examen. | — |
| R20 | Subestimación de la capacidad de desarrollo de la IA | P | Se planifica como si todo se programara a mano: la IA genera en horas lo que se estimó para semanas, el plan deja de servir y se produce más de lo que el equipo puede revisar. | Planificaciones y presupuestos poco realistas |
| R21 | Pérdida de noción de lo que la IA desarrolla | N | El código crece más deprisa de lo que el equipo lo lee: nadie sabe con seguridad qué hace cada parte, si es correcta ni por qué se hizo así. Los fallos pasan desapercibidos y el proyecto no se puede mantener ni defender. | Deficiencias del personal |
| R22 | Pérdida de la rutina de trabajo por causas externas (exámenes) | P | En las semanas de exámenes de otras asignaturas el equipo entero deja de hacer dailies, commits y revisiones; al volver cuesta retomar el ritmo y el contexto. | Deficiencias del personal |

### 2.2 Análisis: probabilidad y consecuencias

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
| R03 | Planificación poco realista | Probable | Crítica | No hay velocidad real medida del equipo |
| R04 | Alcance excesivo y chapado | Probable | Seria | 173 puntos deseados frente a unos 110 de capacidad |
| R05 | Requisitos poco claros o cambiantes | Probable | Seria | El Product Owner solo ha pedido «más grande» |
| R06 | Documentación que no refleja la realidad | Probable | Seria | Ya pasó con las actas de las iteraciones del Sprint 1, y el acta del Sprint 3 tiene apartados sin rellenar |
| R07 | Miembros que no conocen el proyecto | Probable | Crítica | La defensa es el 30 % de la nota de cada uno |
| R08 | Conflictos al integrar cambios | Probable | Menor | Varias ramas en paralelo; se arreglan en horas |
| R09 | Dependencia de la API externa | Ocasional | Seria | Plan gratuito con límites |
| R10 | Directo y cash-out difíciles | Probable | Seria | Tecnología nueva para el equipo |
| R11 | Regresiones al ampliar | Ocasional | Seria | Hay 283 pruebas automáticas que lo detectan |
| R12 | Caída del despliegue en la demo | Ocasional | Seria | Servicios gratuitos sin garantías |
| R13 | Fallos de seguridad nuevos | Ocasional | Seria | Contenido escrito por usuarios y roles nuevos |
| R14 | Rendimiento insuficiente | Remota | Menor | Pocos usuarios y consultas ya agregadas |
| R15 | Pérdida de datos del servidor | Remota | Seria | Neon permite restaurar; los datos se pueden recrear |
| R16 | Algo que el Product Owner no valora | Ocasional | Crítica | La ampliación aún no se ha validado con él |
| R17 | Interfaz poco usable | Ocasional | Menor | Ya hay diseño adaptado al móvil |
| R18 | Filtración de credenciales | Ocasional | Seria | Ya se ha compartido una captura con una clave visible |
| R19 | Uso abusivo de la IA | Ocasional | Crítica | Casi todo el desarrollo se ha hecho con IA; en el Sprint 3 el equipo acordó vigilar su uso excesivo |
| R20 | Subestimación de la capacidad de la IA | Ocasional | Crítica | La retrospectiva del Sprint 2 lo señala: la IA superó de lejos el ritmo que el equipo podía manejar |
| R21 | Pérdida de noción de lo que la IA desarrolla | Frecuente | Catastrófica | Ya pasa: el Sprint 1 se programó en dos días y el objetivo del Sprint 3 fue entender el código que ya existía |
| R22 | Pérdida de la rutina por exámenes | Ocasional | Menor | Los exámenes se concentran en pocas semanas y el ritmo se recupera en unos días |

### 2.3 Matriz de nivel de riesgo

| Gravedad \ Probabilidad | Frecuente | Probable | Ocasional | Remota | Improbable |
|---|---|---|---|---|---|
| **Catastrófica** | 🟥 IN · **R21** | 🟥 IN | 🟥 IN | 🟧 H | 🟨 M |
| **Crítica** | 🟥 IN · **R01** | 🟥 IN · **R03** **R07** | 🟧 H · **R16** **R19** **R20** | 🟨 M | 🟩 L |
| **Seria** | 🟧 H | 🟧 H · **R02** **R04** **R05** **R06** **R10** | 🟨 M · R09 R11 R12 R13 R18 | 🟩 L · R15 | ⬜ T |
| **Menor** | 🟨 M | 🟨 M · R08 | 🟩 L · R17 R22 | ⬜ T · R14 | ⬜ T |
| **Despreciable** | 🟨 M | 🟩 L | ⬜ T | ⬜ T | ⬜ T |

**IN** Intolerable: no puede obviarse su gestión · **H** Alto: controlar, supervisar y tener plan de contingencia · **M** Medio: debería controlarse · **L** Bajo: efectos asumibles · **T** Tolerable: si sucede, no importa.

### 2.4 Tabla de riesgo ordenada

| Orden | ID | Riesgo | Nivel | Exposición | Estrategia |
|---:|---|---|:-:|:-:|---|
| 1 | R21 | Pérdida de noción de lo que la IA desarrolla | 🟥 IN | 25 | Controlar |
| 2 | R01 | Participación desigual | 🟥 IN | 20 | Controlar |
| 3 | R03 | Planificación poco realista | 🟥 IN | 16 | Controlar |
| 4 | R07 | Miembros que no conocen el proyecto | 🟥 IN | 16 | Controlar |
| | | **— Línea de corte: por encima, intolerables con plan RSGR detallado (sección 3) —** | | | |
| 5 | R02 | Ausencias de miembros | 🟧 H | 12 | Controlar |
| 6 | R04 | Alcance excesivo y chapado | 🟧 H | 12 | Evitar |
| 7 | R05 | Requisitos poco claros o cambiantes | 🟧 H | 12 | Controlar |
| 8 | R06 | Documentación que no refleja la realidad | 🟧 H | 12 | Controlar |
| 9 | R10 | Directo y cash-out difíciles | 🟧 H | 12 | Controlar |
| 10 | R16 | Algo que el Product Owner no valora | 🟧 H | 12 | Controlar |
| 11 | R19 | Uso abusivo de la IA | 🟧 H | 12 | Controlar |
| 12 | R20 | Subestimación de la capacidad de la IA | 🟧 H | 12 | Controlar |
| 13 | R09 | Dependencia de la API externa | 🟨 M | 9 | Controlar |
| 14 | R11 | Regresiones al ampliar | 🟨 M | 9 | Controlar |
| 15 | R12 | Caída del despliegue en la demo | 🟨 M | 9 | Controlar |
| 16 | R13 | Fallos de seguridad nuevos | 🟨 M | 9 | Controlar |
| 17 | R18 | Filtración de credenciales | 🟨 M | 9 | Evitar |
| 18 | R08 | Conflictos al integrar cambios | 🟨 M | 8 | Controlar |
| 19 | R15 | Pérdida de datos del servidor | 🟩 L | 6 | Asumir |
| 20 | R17 | Interfaz poco usable | 🟩 L | 6 | Asumir |
| 21 | R22 | Pérdida de la rutina por exámenes | 🟩 L | 6 | Asumir |
| 22 | R14 | Rendimiento insuficiente | ⬜ T | 4 | Asumir |

**Decisión:** los riesgos de nivel **intolerable** son los que no pueden dejar de gestionarse, así que son los que reciben un plan RSGR detallado en la sección 3. Son 4 de 22: R21 (pérdida de noción de lo que la IA desarrolla), R01 (participación desigual), R03 (planificación poco realista), R07 (miembros que no conocen el proyecto). El resto se revisa en cada retrospectiva (sección 4).

**Estrategias:** *evitar* (elegir una alternativa con menos riesgo), *controlar* (reducir la probabilidad o el impacto) y *asumir* (aceptar que ocurra y tener solo un plan de contingencia). No hay riesgos que *transferir*, porque no hay terceros a quienes trasladarlos.

## 3. Reducción, supervisión y gestión del riesgo

El plan de **Reducción, Supervisión y Gestión del Riesgo (RSGR)** define tres cosas para cada riesgo que se trata:
- **Reducción:** los pasos para que no ocurra o, si ocurre, haga menos daño.
- **Supervisión:** cómo se sabe si se está haciendo real y si los pasos de reducción se cumplen. Incluye un **disparador**: la señal concreta que activa la gestión.
- **Gestión:** el plan de contingencia que se aplica si el riesgo se hace real.

Preparar los tres pasos para los 22 riesgos identificados sería inviable y diluiría el esfuerzo. Se aplica la **regla de Pareto (80/20)**: el 20 % de los riesgos identificados supone el 80 % del riesgo total. Por eso solo se detallan los riesgos que la priorización ha marcado como **intolerables**: 4 de 22, un 18 %, cerca de ese 20 %. Si en una retrospectiva otro riesgo pasa a intolerable, se le añade aquí su apartado.

### 3.1 R21 · Pérdida de noción de lo que la IA desarrolla 🟥 IN

**Probabilidad:** frecuente · **Gravedad:** catastrófica · **Exposición:** 25 · **Estrategia:** controlar · **Responsable:** todo el equipo; el Scrum Master hace el seguimiento

#### 3.1.1 Reducción
- Nada entra en `main` sin un pull request revisado y aprobado por un miembro que no lo haya generado. Quien lo aprueba tiene que poder explicar el cambio.
- Pull requests pequeños, de una historia cada uno. Si un cambio no se puede revisar en una sesión, se divide antes de revisarlo.
- Quien abre el pull request explica con sus palabras qué cambia y por qué, en la plantilla del PR.
- La IA no decide qué se construye: solo se programan historias del Product Backlog con criterios de aceptación.
- El resumen del código y las revisiones de código (`docs/ResumenCodigo.md`, `docs/RevisionCódigo1.md`) se actualizan con cada funcionalidad que entra.

#### 3.1.2 Supervisión
- **Indicadores:** pull requests fusionados sin revisión, tamaño de cada pull request y funcionalidades de `main` que nadie del equipo sabe explicar (se pregunta en la daily y en la review).
- **Disparador:** se fusiona un pull request sin la revisión de otro miembro, o en la review nadie sabe explicar una funcionalidad que ya está en `main`.

#### 3.1.3 Gestión
- Se paran las funcionalidades nuevas hasta revisar lo pendiente, como se hizo en el Sprint 3.
- Lo que nadie entienda no se fusiona. Si ya está en `main`, se revisa en pareja o se revierte.
- Sesión de lectura en parejas del código afectado; lo aprendido se apunta en el resumen del código.

### 3.2 R01 · Participación desigual 🟥 IN

**Probabilidad:** frecuente · **Gravedad:** crítica · **Exposición:** 20 · **Estrategia:** controlar · **Responsable:** Scrum Master

#### 3.2.1 Reducción
- Un responsable por épica o funcionalidad.
- Cada miembro abre al menos un pull request por sprint, y otra persona se lo revisa.
- Como mucho una tarea en curso por persona.
- Las historias se reparten en la Sprint Planning, con nombre y apellidos.
- Programación en pareja en las historias grandes.

#### 3.2.2 Supervisión
- **Indicadores:** pull requests y horas completadas por persona en cada sprint (pestaña *Insights* de GitHub y tablero).
- **Disparador:** un miembro termina el sprint sin ningún pull request fusionado, o una sola persona hace más del 50 % del trabajo.

#### 3.2.3 Gestión
- En la daily, sus tareas se reasignan a otra persona, que las hace en pareja con quien las tenía.
- En la retrospectiva se habla abiertamente y se acuerda una acción concreta.
- Si se repite dos sprints seguidos, se comunica al Product Owner, porque la nota del proyecto es individual.

### 3.3 R03 · Planificación poco realista 🟥 IN

**Probabilidad:** probable · **Gravedad:** crítica · **Exposición:** 16 · **Estrategia:** controlar · **Responsable:** Scrum Master

#### 3.3.1 Reducción
- Estimar entre todos con Planning Poker.
- Calcular la capacidad con las horas reales que puede dedicar cada uno (en el Sprint 3, 20 horas).
- Planificar con las horas estimadas de cada historia, no con la velocidad del Sprint 1, que se hizo con IA ([métricas](https://github.com/riverri/IS1/blob/main/docs/scrum/metricas.md)).
- Desarrollo incremental: las historias Must van primero.
- Tareas de un día como máximo.
- Comparar al final de cada sprint las horas reales con las estimadas y ajustar el siguiente.

#### 3.3.2 Supervisión
- **Indicadores:** burndown actualizado en cada daily; horas reales frente a las estimadas.
- **Disparador:** a mitad de sprint está hecho menos del 40 % de lo comprometido, o las horas reales superan en más de un 50 % a las estimadas.

#### 3.3.3 Gestión
- Recortar el alcance según MoSCoW: primero las Could y luego las Should.
- Las historias sin terminar vuelven al Product Backlog.
- Renegociar con el Product Owner qué entra en la entrega final.

### 3.4 R07 · Miembros que no conocen el proyecto 🟥 IN

**Probabilidad:** probable · **Gravedad:** crítica · **Exposición:** 16 · **Estrategia:** controlar · **Responsable:** todo el equipo

#### 3.4.1 Reducción
- Formación cruzada: cada pull request lo revisa alguien que no lo ha hecho (ver R21).
- En la Sprint Review, cada responsable explica su parte a los demás.
- Rotar las historias pequeñas entre miembros.
- Usar como material de estudio la guía del proyecto, la wiki y el resumen del código.

#### 3.4.2 Supervisión
- **Indicadores:** simulacro de defensa en la retrospectiva de los sprints 4 y 6: cada miembro explica en dos minutos una parte que no ha programado.
- **Disparador:** alguien no sabe explicar la arquitectura, el proceso o una parte que no es la suya.

#### 3.4.3 Gestión
- Sesiones de repaso por parejas en el Sprint 7, centradas en lo que pregunta la defensa (proceso, requisitos, planificación y modelado), con preguntas de práctica.

## 4. Planificación temporal

Las fechas de los sprints 4 a 7 son orientativas (sprints de dos semanas desde el cierre del Sprint 3) y se confirman en cada Sprint Planning. Las acciones continuas (revisión de cada pull request, un pull request por persona y sprint, actas) se hacen en todos los sprints.

| Sprint | Fechas | Acciones | Riesgos |
|---|---|---|---|
| **Cierre del Sprint 3** | hasta el 3/11 | Aprobar esta versión del plan. Acordar las reglas de revisión: ningún pull request sin revisar y uno por historia. Apuntar en la siguiente planning los exámenes de todos. | R21, R19, R22 |
| **Sprint 4** | 3/11 – 17/11 | Revisar el código pendiente de integrar antes de añadir funcionalidades nuevas. Planificar con horas reales y horas estimadas. Primer simulacro de defensa. Revisión completa de este plan. | R21, R20, R03, R07 |
| **Sprint 5** | 17/11 – 1/12 | Comprobar los pull requests por persona del Sprint 4 y corregir el reparto. Ajustar la capacidad a los exámenes. Recalcular el plan con las horas reales. | R01, R03, R22, R02 |
| **Sprint 6** | 1/12 – 15/12 | Congelar el alcance: solo se termina y se arregla. Segundo simulacro de defensa. Ensayo de la demo final con el servidor. | R04, R07, R12 |
| **Sprint 7** | 15/12 – 18/12 | Repaso por parejas. Cierre de este plan: qué riesgos se dieron y qué se aprendió. | R07, R21 |

**Monitorización continua:**
- **En cada daily:** el Scrum Master repasa los 4 intolerables: ¿alguno se está haciendo real?
- **En cada retrospectiva:** se vuelven a valorar todos los riesgos, se comprueba que los pasos de reducción se están haciendo y se añaden los riesgos nuevos.
- **Registro:** cada revisión deja una línea en el acta del sprint con los riesgos que suben, bajan o se han hecho reales y la acción tomada.

## 5. Resumen

- **22 riesgos identificados:** 4 intolerables, 8 altos, 6 medios, 3 bajos y 1 tolerable.
- **Los 4 intolerables** reciben el plan RSGR completo, siguiendo la regla de Pareto: R21, pérdida de noción de lo que la IA desarrolla (exposición 25); R01, participación desigual (exposición 20); R03, planificación poco realista (exposición 16); R07, miembros que no conocen el proyecto (exposición 16).
- **El riesgo más grave es nuevo: R21, la pérdida de noción de lo que la IA desarrolla.** Es frecuente y catastrófico porque ya está pasando, y si el equipo no entiende el código no puede mantenerlo, encontrar sus fallos ni defenderlo.
- **Tres de los cuatro riesgos nuevos tienen que ver con la IA** (R19, R20 y R21). El reto del proyecto ya no es producir código, sino entenderlo, revisarlo y planificar a un ritmo que el equipo pueda seguir. La pérdida de la rutina por exámenes (R22) queda en nivel bajo y se asume.
- **Los riesgos técnicos siguen en nivel medio o bajo**, porque el proyecto tiene pruebas automáticas e integración continua.
- **La estrategia es proactiva:** las primeras acciones se toman al cerrar el Sprint 3 y los riesgos se revisan en cada daily y en cada retrospectiva.
