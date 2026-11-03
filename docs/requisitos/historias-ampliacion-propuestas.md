# Historias de usuario propuestas para la ampliación

> **Propuesta pendiente de aprobar.** Estas 19 historias las escribió David Ortega junto con el código de sus pull requests #86 a #90 (23 y 24 de octubre de 2026). Todavía no las han revisado el equipo ni el Product Owner, así que no forman parte del Product Backlog aprobado ni de [historias-de-usuario.md](historias-de-usuario.md).
>
> Se copian tal cual de la rama `feature/cartera-cripto`, que contiene las de todos los PR. Cuando se decida cuáles entran, se pasan a `historias-de-usuario.md` con su fila del Product Backlog.

**Antes de aprobarlas hay que acordar la numeración.** HU-55 a HU-73 coinciden con los números de la propuesta de la [discusión #74](https://github.com/riverri/IS1/discussions/74), donde HU-55 era la quiniela.

| Pull request | Funcionalidad | Historias |
|---|---|---|
| [#86](https://github.com/riverri/IS1/pull/86) | Libro de movimientos del saldo | HU-55 |
| [#87](https://github.com/riverri/IS1/pull/87) | Anuncios internos de otras secciones | HU-60 a HU-62 |
| [#88](https://github.com/riverri/IS1/pull/88) | Casino: ruleta europea y blackjack | HU-56 a HU-59 |
| [#89](https://github.com/riverri/IS1/pull/89) | Mercados de predicción de sí o no | HU-63 a HU-68 |
| [#90](https://github.com/riverri/IS1/pull/90) | Cartera de criptomonedas | HU-69 a HU-73 |

Los PR están apilados: cada uno incluye los anteriores, y se fusionan en el orden #86 → #87 → #88 → #89 → #90.

Las filas 36 a 40 del Product Backlog que citan las fichas también son propuesta: las añaden esos mismos PR y todavía no están en [product-backlog.md](../scrum/product-backlog.md).

## Resumen

Mismas columnas que en [historias-de-usuario.md](historias-de-usuario.md#resumen); la estimación sigue el mismo [modelo](historias-de-usuario.md#estimación).

| Historia | Título | Estado | Criterios | Prioridad | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|---:|
| HU-55 | Movimientos del saldo | En revisión (PR #86) | 3 | 3 (Media) | 5 | 410 | 29 | 4,9 | 4,8 $ |
| HU-56 | Ruleta europea | En revisión (PR #88) | 3 | 4 (Baja) | 5 | 410 | 29 | 4,9 | 4,8 $ |
| HU-57 | Blackjack | En revisión (PR #88) | 5 | 4 (Baja) | 8 | 656 | 47 | 7,6 | 7,8 $ |
| HU-58 | Historial de partidas del casino | En revisión (PR #88) | 1 | 4 (Baja) | 2 | 164 | 12 | 1,9 | 1,9 $ |
| HU-59 | Configurar el casino | En revisión (PR #88) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-60 | Anuncios de otras secciones | En revisión (PR #87) | 3 | 4 (Baja) | 3 | 246 | 18 | 3,0 | 2,9 $ |
| HU-61 | Gestionar los anuncios | En revisión (PR #87) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-62 | Medir los clics de cada anuncio | En revisión (PR #87) | 1 | 4 (Baja) | 2 | 164 | 12 | 1,9 | 1,9 $ |
| HU-63 | Crear, cerrar y anular mercados de predicción | En revisión (PR #89) | 3 | 4 (Baja) | 3 | 246 | 18 | 3,0 | 2,9 $ |
| HU-64 | Comprar participaciones | En revisión (PR #89) | 3 | 4 (Baja) | 8 | 656 | 47 | 7,5 | 7,8 $ |
| HU-65 | Vender participaciones | En revisión (PR #89) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-66 | Resolver el mercado y pagar | En revisión (PR #89) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-67 | Estadísticas y evolución del precio | En revisión (PR #89) | 1 | 4 (Baja) | 3 | 246 | 18 | 2,7 | 2,9 $ |
| HU-68 | Posiciones de mis compañeros de liga | En revisión (PR #89) | 2 | 4 (Baja) | 2 | 164 | 12 | 2,1 | 1,9 $ |
| HU-69 | Ver criptomonedas a su precio real | En revisión (PR #90) | 4 | 4 (Baja) | 5 | 410 | 29 | 4,8 | 4,8 $ |
| HU-70 | Comprar criptomonedas con moneditas | En revisión (PR #90) | 4 | 4 (Baja) | 5 | 410 | 29 | 4,9 | 4,8 $ |
| HU-71 | Vender criptomonedas | En revisión (PR #90) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-72 | Mi cartera de criptomonedas | En revisión (PR #90) | 2 | 4 (Baja) | 2 | 164 | 12 | 2,0 | 1,9 $ |
| HU-73 | Configurar las criptomonedas | En revisión (PR #90) | 3 | 4 (Baja) | 3 | 246 | 18 | 2,7 | 2,9 $ |

**19 historias** y 48 criterios de aceptación.

## Libro de movimientos del saldo

### HU-55 · Movimientos del saldo

**Como** usuario, **quiero** ver cada movimiento de mi saldo con su concepto, **para** saber de dónde sale y adónde va cada monedita.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 36 | Should | 3 | En revisión (PR) | 5 | 410 | 29 | 4,9 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-55.1 | Una operación cambia mi saldo (apuesta, premio, devolución, corrección, recarga o baja) | Se confirma | Queda un movimiento con su tipo, el importe con signo, el saldo resultante, el concepto y la fecha. |
| CA-55.2 | Tengo movimientos | Abro *Mi cuenta → Movimientos* | Los veo del más reciente al más antiguo, y el saldo resultante del último coincide con mi saldo. |
| CA-55.3 | Una operación falla (saldo insuficiente, límites…) | Se deshace | No queda ningún movimiento ni cambia mi saldo. |

Es la base común de la ampliación: el casino y los mercados de predicción apuntarán aquí sus cobros y pagos con sus propios tipos de movimiento.

## Casino

Juegos de azar con moneditas virtuales. El azar lo decide siempre el servidor; cada partida deja sus movimientos de saldo (HU-55) y cuenta en el juego responsable (HU-10).

### HU-56 · Ruleta europea

**Como** usuario, **quiero** apostar moneditas a un número, un color, par o impar, o una docena en una ruleta europea, **para** jugar con mi saldo virtual.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 37 | Could | 3 | En revisión (PR) | 5 | 410 | 29 | 4,9 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-56.1 | Tengo saldo y un importe dentro de los límites del casino | Apuesto y giro | El servidor elige el número (0–36), se cobra el importe y, si acierto, se paga: pleno 35 a 1, color o par/impar 1 a 1 y docena 2 a 1. |
| CA-56.2 | Sale el 0 | Tenía una apuesta a color, par/impar o docena | La pierdo. |
| CA-56.3 | No tengo saldo, el importe está fuera de los límites, estoy en pausa o supero mis límites (HU-10) | Intento girar | No se juega, no se cobra nada y se me avisa. |

### HU-57 · Blackjack

**Como** usuario, **quiero** jugar al blackjack contra la banca pudiendo pedir, plantarme y doblar, **para** jugar con mi saldo virtual.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 37 | Could | 3 | En revisión (PR) | 8 | 656 | 47 | 7,6 | 7,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-57.1 | Empiezo una mano con un importe válido | Se reparte | Se cobra el importe y veo mis dos cartas y solo una de la banca; la otra no llega al navegador. |
| CA-57.2 | Mi mano es blackjack y la de la banca no | Termina la mano | Cobro el importe más 3 a 2 (o lo que diga la configuración). |
| CA-57.3 | Tengo dos cartas y saldo para otra apuesta igual | Doblo | Se cobra otro importe, recibo una sola carta y la mano termina. |
| CA-57.4 | Juego mi mano | Paso de 21 o me planto | Si me paso, pierdo. Si me planto, la banca juega con sus reglas y cobro el doble si gano o recupero el importe si empato. |
| CA-57.5 | Tengo una mano a medias | Recargo la página o pulso dos veces | Sigo en la misma mano: no se reparte otra ni se cobra dos veces. |

Sin dividir parejas ni seguro (fuera del alcance).

### HU-58 · Historial de partidas del casino

**Como** usuario, **quiero** ver mis partidas del casino, **para** saber cuánto he jugado y ganado.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 37 | Could | 3 | En revisión (PR) | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-58.1 | He jugado partidas | Abro el historial | Veo juego, fecha, apuesta, resultado, importe y balance, de la más reciente a la más antigua. |

### HU-59 · Configurar el casino

**Como** creador de apuestas, **quiero** fijar los límites por apuesta y las reglas del casino, **para** controlar la ventaja de la casa.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 37 | Could | 3 | En revisión (PR) | 3 | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-59.1 | Estoy en *Gestión → Casino* | Guardo el mínimo, el máximo y las reglas del blackjack (barajas, si la banca pide con 17 blando y si el blackjack paga 3:2 o 6:5) | Se aplican a las partidas nuevas y veo la ventaja teórica de cada juego. |
| CA-59.2 | Cierro el casino | Un usuario intenta jugar | No puede. |

La ventaja de la casa se controla con reglas reales: la ruleta europea siempre tiene un 2,70 % (un 0 entre 37 números); la del blackjack es aproximada, con estrategia básica.

---

## Anuncios internos

### HU-60 · Anuncios de otras secciones

**Como** visitante o usuario, **quiero** ver anuncios de otras secciones de la web y pulsarlos, **para** descubrir lo que ofrece.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 38 | Could | 3 | En revisión (PR) | 3 | 246 | 18 | 3,0 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-60.1 | Hay anuncios activos para la sección de la página | La abro | Veo uno de los de prioridad más alta; si hay varios con esa prioridad, se turnan al azar. |
| CA-60.2 | Estoy en una sección | Se elige el anuncio | Nunca es uno que lleve a esa misma sección. |
| CA-60.3 | Veo un anuncio | Lo pulso | Voy directo a su sección. Los que llevan a secciones que necesitan sesión (ligas, Mi cuenta) no se enseñan a los visitantes. |

### HU-61 · Gestionar los anuncios

**Como** creador de apuestas, **quiero** crear, editar, activar y desactivar anuncios con su texto, su destino, las secciones donde salen, su prioridad y sus fechas, **para** decidir qué se promociona sin tocar el código.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 38 | Could | 3 | En revisión (PR) | 3 | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-61.1 | Creo un anuncio con un destino de la web y al menos una sección | Lo guardo | Sale en esas secciones mientras esté activo y dentro de sus fechas. |
| CA-61.2 | El destino es una dirección de otra web o no es una sección conocida | Lo guardo | Se rechaza con un mensaje. |

### HU-62 · Medir los clics de cada anuncio

**Como** creador de apuestas, **quiero** ver cuántas veces se ha mostrado y pulsado cada anuncio, **para** saber cuáles funcionan.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 38 | Could | 3 | En revisión (PR) | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-62.1 | Un usuario o un visitante pulsa un anuncio | Lo abro en Gestión | Queda apuntado el clic con su fecha y la página de origen, y veo las impresiones, los clics y el porcentaje de clics de cada anuncio. |

---

## Mercados de predicción

Preguntas de sí o no, tipo Polymarket. El precio de cada respuesta va de 0 a 1 y se lee como probabilidad; lo calcula un creador de mercado automático (LMSR) con cada compra y venta. No son los mercados a largo plazo de HU-44 (cuotas fijas del creador).

### HU-63 · Crear, cerrar y anular mercados de predicción

**Como** creador de apuestas, **quiero** crear un mercado de sí o no con su pregunta, su fecha de cierre y su liquidez, y poder cerrarlo o anularlo, **para** ofrecer predicciones.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 39 | Could | 3 | En revisión (PR) | 3 | 246 | 18 | 3,0 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-63.1 | Escribo la pregunta, el cierre, la liquidez y la comisión | Creo el mercado | Queda abierto con el «Sí» y el «No» a 0,50. |
| CA-63.2 | Un mercado abierto | Lo cierro o llega su fecha | Ya no admite compras ni ventas. |
| CA-63.3 | Un mercado con posiciones | Lo anulo | A cada usuario se le devuelve lo que pagó menos lo que recuperó vendiendo. |

### HU-64 · Comprar participaciones

**Como** usuario, **quiero** comprar participaciones del «Sí» o del «No» con mi saldo, **para** ganar si acierto la predicción.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 39 | Could | 3 | En revisión (PR) | 8 | 656 | 47 | 7,5 | 7,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-64.1 | Elijo un lado y un importe | Pido la previsión y confirmo | Antes de confirmar veo cuántas participaciones recibo y el precio medio; al confirmar se cobra el importe (comisión incluida) y el precio de ese lado sube. |
| CA-64.2 | No tengo saldo, el importe está fuera de los límites, el mercado está cerrado o resuelto, o el juego responsable no lo permite | Intento comprar | No se compra y se me avisa. |
| CA-64.3 | El precio cambia entre que lo veo y confirmo | Confirmo | No se compra y se me pide aceptar el precio nuevo, como en HU-29. |

### HU-65 · Vender participaciones

**Como** usuario, **quiero** vender mis participaciones antes del cierre, **para** recoger ganancias o cortar pérdidas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 39 | Could | 3 | En revisión (PR) | 3 | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-65.1 | Tengo participaciones de un mercado abierto | Vendo | Recibo su valor menos la comisión y el precio de ese lado baja. |
| CA-65.2 | Quiero vender más participaciones de las que tengo | Lo intento | No se vende nada y se me avisa. |

### HU-66 · Resolver el mercado y pagar

**Como** creador de apuestas, **quiero** marcar si la respuesta fue «Sí» o «No», **para** pagar a los ganadores.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 39 | Could | 3 | En revisión (PR) | 3 | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-66.1 | Un mercado con posiciones | Marco la respuesta | Cada participación ganadora paga 1 monedita y las perdedoras nada, cada pago con su movimiento de saldo y su aviso (HU-37). |
| CA-66.2 | Un mercado ya resuelto | Corrijo la respuesta | Los saldos se ajustan por la diferencia con lo ya pagado (ADR 0002). |

### HU-67 · Estadísticas y evolución del precio

**Como** visitante, **quiero** ver el precio actual, el volumen, el número de participantes y la evolución del precio de cada mercado, **para** saber qué piensa la comunidad.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 39 | Could | 3 | En revisión (PR) | 3 | 246 | 18 | 2,7 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-67.1 | Un mercado con operaciones | Lo abro, aunque no haya iniciado sesión | Veo el precio, el volumen, los participantes y un gráfico con el precio del «Sí» después de cada operación. |

### HU-68 · Posiciones de mis compañeros de liga

**Como** usuario, **quiero** ver en cada mercado qué posiciones tienen los miembros de mis ligas privadas, **para** comparar mis predicciones con las de mis amigos.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 39 | Could | 3 | En revisión (PR) | 2 | 164 | 12 | 2,1 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-68.1 | Comparto una liga privada (HU-51) con otros jugadores | Abro un mercado | Veo el lado, las participaciones y el valor actual de los que tienen posición. |
| CA-68.2 | Alguien no comparte ninguna liga conmigo | Abre el mercado | No ve mis posiciones (tampoco se enseñan en el perfil público, HU-48). |

---

## Cartera de criptomonedas

Criptomonedas reales a su precio real en euros, compradas y vendidas con moneditas (1 monedita = 1 €). Solo el precio es real: nadie tiene criptomonedas de verdad, no hay carteras externas ni blockchain, y todo sigue siendo saldo virtual. Las protagonistas son las memecoins (DOGE, SHIB, PEPE, WIF, BONK, FLOKI, FARTCOIN), las de más riesgo y las que más se mueven; BTC, ETH y SOL quedan como referencia. Los precios vienen de la API pública de CoinGecko.

### HU-69 · Ver criptomonedas a su precio real

**Como** visitante, **quiero** ver las criptomonedas con su precio real en euros, cuánto se han movido en 24 horas y su gráfico de la semana, **para** decidir en cuál me la juego.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 40 | Could | 3 | En revisión (PR) | 5 | 410 | 29 | 4,8 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-69.1 | Hay criptomonedas con precio | Abro *Cripto*, aunque no haya iniciado sesión | Veo primero las memecoins y después las de referencia, cada una con su precio (con las cifras necesarias aunque valga una fracción de céntimo) y su variación en 24 horas, y destacada la que más se ha movido. |
| CA-69.2 | Una cripto con precios guardados | Abro su ficha | Veo un gráfico de su precio en los últimos 7 días. |
| CA-69.3 | Cualquier página de criptomonedas | La abro | Veo un aviso de riesgo (muy alto en las memecoins) y que solo se juega con moneditas. |
| CA-69.4 | La aplicación está en marcha | Pasan 2 minutos | Los precios se descargan solos de CoinGecko; si la API falla, la web sigue funcionando y se reintenta después. |

### HU-70 · Comprar criptomonedas con moneditas

**Como** usuario, **quiero** comprar una criptomoneda con mi saldo al precio actual, **para** ganar si sube.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 40 | Could | 3 | En revisión (PR) | 5 | 410 | 29 | 4,9 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-70.1 | Elijo una cripto y un importe | Pido la previsión y confirmo | Antes de confirmar veo cuánta cripto recibo y la comisión; al confirmar se cobra el importe (comisión incluida) con un movimiento de saldo (HU-55). |
| CA-70.2 | No tengo saldo, el importe está fuera de los límites (HU-07), soy el creador o el juego responsable no lo permite (HU-10) | Intento comprar | No se compra, no se cobra y se me avisa. |
| CA-70.3 | Llega un precio nuevo entre que veo la previsión y confirmo | Confirmo | No se compra y se me enseña el precio nuevo para que decida, como en HU-29. |
| CA-70.4 | El último precio tiene más de 15 minutos o no hay | Intento comprar | No se compra: hace falta un precio al día. |

### HU-71 · Vender criptomonedas

**Como** usuario, **quiero** vender una parte o todo lo que tengo de una criptomoneda, **para** recoger ganancias o cortar pérdidas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 40 | Could | 3 | En revisión (PR) | 3 | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-71.1 | Tengo una cripto con precio al día | Vendo una parte o todo | Recibo cantidad × precio menos la comisión, y el coste de lo que me queda baja en la misma proporción. |
| CA-71.2 | Quiero vender más de lo que tengo, o algo que no tengo | Lo intento | No se vende nada y se me avisa. |

### HU-72 · Mi cartera de criptomonedas

**Como** usuario, **quiero** ver lo que tengo de cada criptomoneda, cuánto vale y cuánto gano o pierdo, **para** seguir mis inversiones.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 40 | Could | 3 | En revisión (PR) | 2 | 164 | 12 | 2,0 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-72.1 | Tengo criptomonedas | Abro *Mi cartera* | Veo de cada una la cantidad, el precio, el valor, lo que me costó y la ganancia o pérdida en moneditas y en %, y los totales. |
| CA-72.2 | He comprado y vendido | Abro *Mi cartera* | Veo mis últimas operaciones con su precio, importe y comisión. |

### HU-73 · Configurar las criptomonedas

**Como** creador de apuestas, **quiero** abrir o cerrar la sección, fijar la comisión y elegir qué criptos se ofrecen, **para** controlar lo que gana o arriesga la casa.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 40 | Could | 3 | En revisión (PR) | 3 | 246 | 18 | 2,7 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-73.1 | Estoy en *Gestión → Criptomonedas* | Guardo si está abierta, la comisión (0–10 %) y los minutos que sirve un precio (1–120) | Se aplica a las compras y ventas siguientes; con la sección cerrada se ven los precios pero nadie opera. |
| CA-73.2 | Retiro una cripto | Un usuario la busca | Ya no aparece ni se puede comprar, pero quien la tenga puede venderla. |
| CA-73.3 | Quiero precios ya | Pulso *Actualizar precios ahora* | Se descargan en el momento; si CoinGecko no responde, se me avisa. |
