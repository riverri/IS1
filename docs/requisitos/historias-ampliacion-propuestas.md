# Historias de usuario propuestas para la ampliación

> **Propuesta pendiente de aprobar.** Estas 11 historias (HU-74 a HU-84) se acordaron en la planificación del [Sprint 4](../scrum/sprints/sprint-4.md) (05/11). Todavía no las han revisado el equipo ni el Product Owner, así que no forman parte del Product Backlog aprobado ni de [historias-de-usuario.md](historias-de-usuario.md).

**HU-55 a HU-73 ya no están aquí.** Eran las 19 historias de los pull requests #86 a #90 (23 y 24 de octubre de 2026). El 06/11 se integró su código en `main` sin esperar a la aprobación del Product Owner, y sus historias pasaron a [historias-de-usuario.md](historias-de-usuario.md) con el estado «Integrada» y a las filas 36 a 40 del [Product Backlog](../scrum/product-backlog.md). Se mantiene su numeración; la quiniela de la [discusión #74](https://github.com/riverri/IS1/discussions/74), que allí era HU-55, es ahora HU-78 a HU-80.

Las filas 41 a 46 del Product Backlog que citan las fichas (cash-out, riesgo de la casa, correo, quiniela, fantasy y ranking por tipo) también son propuesta y todavía no están en [product-backlog.md](../scrum/product-backlog.md). El baccarat va en la fila 37, la del casino.

## Resumen

Mismas columnas que en [historias-de-usuario.md](historias-de-usuario.md#resumen); la estimación sigue el mismo [modelo](historias-de-usuario.md#estimación).

| Historia | Título | Estado | Criterios | Prioridad | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|---:|
| HU-74 | Baccarat | Propuesta (Sprint 4) | 4 | 4 (Baja) | 5 | 410 | 29 | 5,0 | 4,8 $ |
| HU-75 | Cobrar antes de tiempo (cash-out) | Propuesta (Sprint 4) | 4 | 3 (Media) | 8 | 656 | 47 | 7,6 | 7,8 $ |
| HU-76 | Riesgo de la casa por evento | Propuesta (Sprint 4) | 3 | 3 (Media) | 5 | 410 | 29 | 4,8 | 4,8 $ |
| HU-77 | Correo al ganar una apuesta | Propuesta (Sprint 4) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,9 | 2,9 $ |
| HU-78 | Montar una quiniela | Propuesta (Sprint 4) | 3 | 4 (Baja) | 3 | 246 | 18 | 3,1 | 2,9 $ |
| HU-79 | Echar un boleto | Propuesta (Sprint 4) | 3 | 4 (Baja) | 3 | 246 | 18 | 3,1 | 2,9 $ |
| HU-80 | Repartir el bote de la quiniela | Propuesta (Sprint 4) | 3 | 4 (Baja) | 5 | 410 | 29 | 4,8 | 4,8 $ |
| HU-81 | Crear un equipo fantasy y fichar | Propuesta (Sprint 4) | 3 | 4 (Baja) | 5 | 410 | 29 | 4,8 | 4,8 $ |
| HU-82 | Puntos de los jugadores fantasy | Propuesta (Sprint 4) | 2 | 4 (Baja) | 5 | 410 | 29 | 4,7 | 4,8 $ |
| HU-83 | Clasificación fantasy | Propuesta (Sprint 4) | 1 | 4 (Baja) | 2 | 164 | 12 | 1,9 | 1,9 $ |
| HU-84 | Ranking por tipo de juego | Propuesta (Sprint 4) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,9 | 2,9 $ |

**11 historias** y 30 criterios de aceptación.

## Sprint 4 (HU-74 a HU-84)

Acordadas en la planificación del [Sprint 4](../scrum/sprints/sprint-4.md) (05/11). Están pendientes de revisar por el equipo y de aprobar por el Product Owner. Cada historia sube su código en un pull request propio cuando se apruebe.

### HU-74 · Baccarat

**Como** usuario, **quiero** jugar al baccarat Punto Banco en el casino, **para** tener un juego de cartas en el que no hay que decidir nada después de apostar.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 37 | 4 (Baja) | 4 | Propuesta | 5 | 410 | 29 | 5,0 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-74.1 | Tengo saldo y elijo Jugador, Banca o Empate con un importe | Reparto la mano | Se cobra el importe, las cartas se reparten solas y se paga: Jugador 1 a 1, Banca 1 a 1 menos un 5 % de comisión y Empate 8 a 1. La mano queda en el historial del casino. |
| CA-74.2 | No elijo a qué apuesto | Intento repartir | Se me pide elegir y no se cobra nada. |
| CA-74.3 | La mano acaba en empate | Había apostado a Jugador o a Banca | Se me devuelve lo apostado. |
| CA-74.4 | Las cartas de una mano | Se juega | Se siguen las reglas del Punto Banco: figuras y dieces valen 0 y solo cuentan las unidades; con 8 o 9 nadie pide; el jugador pide con 5 o menos y se planta con 6 o 7; la banca sigue la tabla de la tercera carta. |

Se juega con 8 barajas y va junto al blackjack (HU-57), en la misma página del casino.

### HU-75 · Cobrar antes de tiempo (cash-out)

**Como** usuario, **quiero** cobrar una apuesta antes de que terminen sus partidos, **para** asegurarme una ganancia o limitar lo que pierdo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 41 | 3 (Media) | 4 | Propuesta | 8 | 656 | 47 | 7,6 | 7,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-75.1 | Una apuesta activa cuyos partidos pendientes no han empezado | Abro *Mis apuestas* | Veo «Cobrar ya» con lo que vale ahora: importe × cuotas ya acertadas × (cuota apostada / cuota actual) de las pendientes, sin pasar de lo que cobraría acertando. Recién hecha vale lo apostado. |
| CA-75.2 | Algún partido pendiente ha empezado o está suspendido | Abro *Mis apuestas* | No se ofrece cobrar. |
| CA-75.3 | He cobrado una apuesta | Después se juegan sus partidos | La apuesta queda «Cobrada» y el resultado ya no la cambia. |
| CA-75.4 | Las cuotas cambian mientras decido | Pulso «Cobrar ya» | No se cobra y se me enseña la oferta nueva. |

La cuota actual no cuenta el dinero que ha apostado el propio jugador, para que apostar y cobrar al momento no dé ganancia. Una apuesta cobrada cuenta en el balance y en el ranking, pero no como acierto.

### HU-76 · Riesgo de la casa por evento

**Como** creador de apuestas, **quiero** ver cuánto pagaría la casa con cada resultado de un evento, **para** detectar a tiempo los partidos en los que puede perder mucho dinero.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 42 | 3 (Media) | 4 | Propuesta | 5 | 410 | 29 | 4,8 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-76.1 | Un evento con apuestas | Abro su ficha en *Gestión* | Veo lo que habría que pagar con cada resultado y lo que le quedaría a la casa. Las apuestas canceladas no cuentan. |
| CA-76.2 | En el peor caso la casa pierde más que el límite de alerta | Abro la ficha del evento | Sale un aviso de riesgo. |
| CA-76.3 | Hay eventos en alerta | Abro el panel de *Gestión* | Esos eventos salen marcados. El límite (2.000 monedas al empezar) se cambia en *Límites de apuesta*. |

El peor caso cuenta las combinadas enteras y supone que las de goles y ambos marcan se cobran con cualquier resultado.

### HU-77 · Correo al ganar una apuesta

**Como** usuario, **quiero** recibir un correo cuando gano una apuesta, **para** enterarme aunque no entre en la web.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 43 | 4 (Baja) | 4 | Propuesta | 3 | 246 | 18 | 2,9 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-77.1 | Tengo los correos activados | Una apuesta mía se resuelve como ganada | Recibo un correo con el mismo texto que el aviso de la web y el enlace a *Mis apuestas*. Si pierdo, no llega ninguno. |
| CA-77.2 | He desactivado los correos en *Mi cuenta* | Gano una apuesta | No recibo correo; el aviso de la web sigue saliendo. |

El correo se envía cuando el resultado ya está guardado, para no avisar de premios que se deshacen. Al corregir un resultado no se envía. Sin servidor de correo, se escribe en la consola, como el de recuperar la contraseña (HU-17).

### HU-78 · Montar una quiniela

**Como** creador de apuestas, **quiero** montar una quiniela con varios partidos y un precio por boleto, **para** que los jugadores compitan entre ellos sin jugar contra la casa.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 44 | 4 (Baja) | 4 | Propuesta | 3 | 246 | 18 | 3,1 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-78.1 | Hay partidos programados de deportes con empate | Elijo de 3 a 15 y el precio del boleto | Se crea la quiniela con los partidos en orden de fecha. |
| CA-78.2 | Elijo menos de 3 partidos | Intento crearla | No se crea y se dice por qué. |
| CA-78.3 | Soy un usuario sin rol de creador | Intento entrar en la gestión de quinielas | No puedo. |

No tiene cuotas: el bote sale de lo que pagan los jugadores y la casa solo se queda un 10 % de comisión.

### HU-79 · Echar un boleto

**Como** usuario, **quiero** rellenar un boleto con 1, X o 2 en cada partido de la quiniela, **para** optar al bote.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 44 | 4 (Baja) | 4 | Propuesta | 3 | 246 | 18 | 3,1 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-79.1 | Una quiniela abierta y saldo suficiente | Echo mi boleto con un pronóstico en cada partido | Se cobra el precio y veo mi boleto en la página. Solo puedo echar uno. |
| CA-79.2 | Ha empezado un partido, o alguno está suspendido o anulado | Intento echar un boleto | La quiniela está cerrada y no se admite. |
| CA-79.3 | Soy el creador de apuestas | Abro la quiniela | No puedo jugar. |

### HU-80 · Repartir el bote de la quiniela

**Como** creador de apuestas, **quiero** repartir el bote cuando se han jugado todos los partidos, **para** pagar a quienes más aciertan.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 44 | 4 (Baja) | 4 | Propuesta | 5 | 410 | 29 | 4,8 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-80.1 | Se han jugado todos los partidos | Reparto el bote | El bote (lo pagado menos el 10 % de la casa) se reparte a partes iguales entre los boletos con más aciertos, y cada jugador recibe un aviso. |
| CA-80.2 | Se ha anulado un partido | Se cuentan los aciertos | Ese partido cuenta como acierto para todos. |
| CA-80.3 | Nadie ha acertado ninguno, o anulo la quiniela antes de repartirla | Se reparte o se anula | Se devuelve el precio de todos los boletos. |

### HU-81 · Crear un equipo fantasy y fichar

**Como** usuario, **quiero** montar un equipo de siete futbolistas reales con un presupuesto, **para** competir con lo que sé de fútbol sin gastar moneditas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 45 | 4 (Baja) | 4 | Propuesta | 5 | 410 | 29 | 4,8 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-81.1 | No tengo equipo | Entro en *Fantasy* | Se me pide crearlo, y después ficho con un presupuesto de 90 millones ficticios. |
| CA-81.2 | Tengo equipo | Intento fichar a un jugador | Solo se puede si hay hueco en su posición (1 portero, 2 defensas, 2 centrocampistas y 2 delanteros), como mucho 2 del mismo club y sin pasar del presupuesto. El mercado filtra por posición y dice por qué no se puede fichar a alguien. |
| CA-81.3 | Soy el creador de apuestas | Entro en *Fantasy* | No puedo crear un equipo. |

El precio de cada jugador sale de su nota y de la calidad de su equipo (de unos 7 a unos 20 millones). Al vender a un jugador se recupera lo que costó.

### HU-82 · Puntos de los jugadores fantasy

**Como** usuario, **quiero** que mis jugadores sumen puntos con los partidos de sus clubes, **para** seguir cómo va mi equipo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 45 | 4 (Baja) | 4 | Propuesta | 5 | 410 | 29 | 4,7 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-82.1 | Ficho a un jugador | Se juegan partidos de su club | Solo le cuentan los que empiezan después de ficharlo. Si lo vendo, lo que ya sumó se queda. |
| CA-82.2 | Un partido de su club ha terminado | Se calculan sus puntos | Victoria 3 y empate 1. Portería a cero: portero +4, defensa +3 y centrocampista +1. Cada gol de su club: delantero +2 y centrocampista +1. Si su club encaja 3 o más, portero y defensa −1. Sin marcador, solo cuentan la victoria y el empate. |

No hay estadísticas de cada jugador (goles, tarjetas…): todo sale del resultado de su club y de su posición.

### HU-83 · Clasificación fantasy

**Como** usuario, **quiero** ver la clasificación de todos los equipos fantasy, **para** saber cómo voy frente a los demás.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 45 | 4 (Baja) | 4 | Propuesta | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-83.1 | Hay varios equipos | Abro la clasificación | Salen de más a menos puntos; con los mismos puntos va delante el que se creó antes. Si el creador corrige un resultado, la clasificación se corrige sola. |

### HU-84 · Ranking por tipo de juego

**Como** usuario, **quiero** filtrar el ranking por tipo de juego (deportes, predicciones, casino, cripto, memecoins y quiniela), **para** compararme con quienes juegan a lo mismo que yo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 46 | 4 (Baja) | 4 | Propuesta | 3 | 246 | 18 | 2,9 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-84.1 | Elijo un tipo de juego en el ranking | Se carga | Solo salen los que han jugado a ese tipo, con sus estadísticas de ese tipo. |
| CA-84.2 | He elegido un tipo de juego | Ordeno el ranking | No se puede ordenar por saldo, que es común a todos los juegos, y en cripto no hay porcentaje de aciertos. |

Depende del casino, los mercados de predicción y la cartera (HU-56 a HU-73) y de la quiniela (HU-78 a HU-80).
