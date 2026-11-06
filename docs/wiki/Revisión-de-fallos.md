# Revisión de fallos

Se partió de un informe del proyecto con 14 fallos (**F-01 a F-14**) y una revisión completa del código encontró 20 más (**N-01 a N-20**). Los 34 están corregidos en `main`, con 34 pruebas nuevas, y sus issues (#34–#67) están cerrados.
En noviembre, la [revisión final](#revisión-final-noviembre-de-2026) encontró 16 más: 15 corregidos o documentados y uno descartado.

Gravedad: **alta** = puede tumbar la aplicación, perder dinero o dejar entrar a quien no debe · **media** = comportamiento incorrecto · **baja** = molestia o caso raro.

## Del informe

| Fallo | Gravedad | Qué pasaba | Cómo se arregló | Issue |
|---|---|---|---|---|
| F-01 · Zona horaria | Alta | El servidor en UTC dejaba apostar hasta 2 horas después de empezar un partido | Reloj en Europe/Madrid y `TZ` en el Dockerfile | [#34](https://github.com/riverri/IS1/issues/34) |
| F-02 · Concurrencia del saldo | Alta | Dos apuestas a la vez podían pisarse | Bloqueo optimista (`@Version`) | [#35](https://github.com/riverri/IS1/issues/35) |
| F-03 · Manipular cuotas | Media | Apostar mucho y cancelar movía las cuotas gratis | Máximo 100 por jugador en el volumen | [#36](https://github.com/riverri/IS1/issues/36) |
| F-04 · Contraseña del creador | Alta | Con el túnel seguía valiendo `creador123` | Contraseña obligatoria en los perfiles públicos | [#37](https://github.com/riverri/IS1/issues/37) |
| F-05 · Sincronización | Alta | Un partido con error deshacía todos | Una transacción por partido | [#38](https://github.com/riverri/IS1/issues/38) |
| F-06 · Cuota de combinadas | Media | Sin tope: error 500 con cuotas enormes | Máximo 1.000 | [#39](https://github.com/riverri/IS1/issues/39) |
| F-07 · Prórroga y penaltis | Media | Se usaba el ganador final | Marcador de los 90 minutos | [#40](https://github.com/riverri/IS1/issues/40) |
| F-08 · Intentos de login | Media | Sin límite | 5 fallos → 15 minutos de bloqueo | [#41](https://github.com/riverri/IS1/issues/41) |
| F-09 · Ranking en memoria | Media | Cargaba todas las apuestas | Consulta agregada | [#42](https://github.com/riverri/IS1/issues/42) |
| F-10 · % de aciertos | Baja | 1 de 1 quedaba primero | Mínimo de 5 apuestas resueltas | [#43](https://github.com/riverri/IS1/issues/43) |
| F-11 · Boleto con GET | Baja | Un enlace GET cambiaba datos | Formulario POST con CSRF | [#44](https://github.com/riverri/IS1/issues/44) |
| F-12 · Saldo negativo | Baja | Sin documentar | Decisión 0002: se mantiene y no se puede apostar | [#45](https://github.com/riverri/IS1/issues/45) |
| F-13 · Volumen y cuentas eliminadas | Media | Combinadas contadas de más; ajustes a cuentas borradas | Importe repartido; cuentas eliminadas intactas | [#46](https://github.com/riverri/IS1/issues/46) |
| F-14 · Consola de H2 | Alta | Abierta a cualquiera | Solo el creador | [#47](https://github.com/riverri/IS1/issues/47) |

## De la revisión del código

| Fallo | Gravedad | Qué pasaba | Cómo se arregló | Issue |
|---|---|---|---|---|
| N-01 · Nombres repetidos | Alta | Impedían arrancar la aplicación | Se rechazan; búsqueda tolerante | [#48](https://github.com/riverri/IS1/issues/48) |
| N-02 · Evento anulado | Alta | Bloqueaba la sincronización | Se salta | [#49](https://github.com/riverri/IS1/issues/49) |
| N-03 · El creador apostaba | Alta | Podía apostar y poner el resultado | Prohibido | [#50](https://github.com/riverri/IS1/issues/50) |
| N-04 · Partidos duplicados | Media | Reaparecían al reiniciar | Datos de ejemplo solo en base vacía | [#51](https://github.com/riverri/IS1/issues/51) |
| N-05 · Cuota cambiada | Media | La simple no avisaba | Aviso con la cuota nueva | [#52](https://github.com/riverri/IS1/issues/52) |
| N-06 · Suspensiones | Media | La API las deshacía | Solo reabre lo que suspendió ella | [#53](https://github.com/riverri/IS1/issues/53) |
| N-07 · XSS en jugadores | Media | Un apóstrofo rompía la confirmación | Nombre en atributo `data-` | [#54](https://github.com/riverri/IS1/issues/54) |
| N-08 · Bloquear bajas | Media | Registrándose con el email anónimo | Dominio reservado | [#55](https://github.com/riverri/IS1/issues/55) |
| N-09 · Contraseñas con tildes | Media | Error 500 por los 72 bytes | Se avisa | [#56](https://github.com/riverri/IS1/issues/56) |
| N-10 · Otras sesiones | Media | Seguían abiertas | Se cierran | [#57](https://github.com/riverri/IS1/issues/57) |
| N-11 · Corregir resultado | Media | Mandaba el marcador viejo | Marcador y resultado deben coincidir | [#58](https://github.com/riverri/IS1/issues/58) |
| N-12 · Aceptar cuotas | Baja | Había que pulsarlo dos veces | Una vez | [#59](https://github.com/riverri/IS1/issues/59) |
| N-13 · 404 en el boleto | Baja | Si un partido empezaba | Aviso | [#60](https://github.com/riverri/IS1/issues/60) |
| N-14 · Ligas huérfanas | Baja | No se podían borrar | Se borran con la baja | [#61](https://github.com/riverri/IS1/issues/61) |
| N-15 · Textos largos | Baja | Error 500 | Validación | [#62](https://github.com/riverri/IS1/issues/62) |
| N-16 · Nota NaN | Baja | Se guardaba 0 | Se rechaza | [#63](https://github.com/riverri/IS1/issues/63) |
| N-17 · Equipo inexistente | Baja | Error 500 | Página 404 | [#64](https://github.com/riverri/IS1/issues/64) |
| N-18 · Plantillas | Baja | Iban al equipo equivocado | Solo equipos sin enlazar | [#65](https://github.com/riverri/IS1/issues/65) |
| N-19 · API colgada | Baja | Sin tiempo máximo | 5 s / 20 s | [#66](https://github.com/riverri/IS1/issues/66) |
| N-20 · Avisos leídos | Baja | Se marcaban los no vistos | Solo los mostrados | [#67](https://github.com/riverri/IS1/issues/67) |

## Revisión final (noviembre de 2026)

Revisión del código de `main` del 5/11 (**R-01 a R-12**) y de las ramas #86 a #90 de la ampliación (**A-01 a A-04**, encontrados en la revisión del Sprint 3). No tienen issue: cada arreglo es un commit con su prueba, y entraron en `main` el 06/11 en el pull request de la integración. Se ha comprobado que las pruebas de R-01 y de A-01 a A-04 fallan con el código anterior.

| Fallo | Gravedad | Qué pasaba | Cómo se arregló | Prueba |
|---|---|---|---|---|
| R-01 · Enlace de recuperación falseable | Alta | El enlace del correo salía de la cabecera `Host`: falseándola, otro podía quedarse con el código de la víctima | Dirección fija (`apuestas.url-publica`) | `RecuperacionWebTest` |
| R-02 · Resultado y apuesta a la vez | Media | El creador veía «otra operación con tu saldo» | Aviso propio para volver a intentarlo | `ConcurrenciaAdviceTest` |
| R-03 · Cuota al cambiar el importe | Media | Se guardaba con la cuota nueva sin avisar (HU-29) | Se compara con la cuota vista y se enseña la nueva | `ModificarImporteWebTest` |
| R-04 · Apostar y cancelar para mover cuotas | Media | Con el tope de 100 por jugador, una cuenta mueve las cuotas como mucho un 2,7 %; solo se nota con muchas cuentas | Se acepta como limitación conocida ([Algoritmo de cuotas](Algoritmo-de-cuotas)) | — |
| R-05 · Correos de recuperación sin límite | Media | Se podía llenar de correos el buzón de otra persona | Como mucho 3 cada 30 minutos por cuenta | `RecuperacionWebTest` |
| R-06 · Motivo de no poder cancelar | Baja | Decía siempre «el evento ha empezado» | Dice el motivo real | `LargoPlazoWebTest` |
| R-07 · Boleto perdido | Baja | Si la apuesta fallaba al confirmarse, el boleto ya se había vaciado | Se vacía cuando la apuesta está guardada | `CombinadaWebTest` |
| R-08 · Bloqueo por intentos | Baja | Apuntaba también correos sin cuenta y no olvidaba los caducados: la memoria crecía sin fin | Solo cuentas que existen; los caducados se olvidan | `IntentosLoginWebTest` |
| R-09 · Evento finalizado | Baja | No se podía anular | Se quita el resultado y se recalculan las apuestas | `ResolucionWebTest` |
| R-10 · Límites sin tope | Baja | Importe × cuota máxima no cabía en el saldo y fallaba la resolución | Tope de 100.000 monedas | `LimitesWebTest` |
| R-12 · Archivo `ta` | Baja | Salida de `git help` subida por error | Borrado | — |
| A-01 · Cripto retirada | Media | Solo se descargaban precios de las criptos que se ofrecen: al caducar el de una retirada, nadie podía venderla (CA-73.2) | También se descargan los de las retiradas que alguien tiene | `CriptoWebTest.unaCriptoRetiradaQueAlguienTieneSigueTeniendoPrecio` |
| A-02 · Precios viejos | Media | Se descargan cada 2 minutos pero servían 15 (hasta 120): si la descarga fallaba, se compraba con un precio viejo sabiendo ya el real | 5 minutos por defecto (migración V15.1) y 30 como mucho | `CriptoWebTest.unPrecioSirveCincoMinutosYComoMuchoTreinta` |
| A-03 · Doblar con el casino cerrado | Media | Doblar en el blackjack cobraba dinero nuevo aunque el creador hubiera cerrado el casino | No se dobla; pedir y plantarse sí, para acabar la mano | `CasinoWebTest.conElCasinoCerradoNoSeDoblaPeroSePuedeAcabarLaMano` |
| A-04 · Mercados anulados en el panel | Media | Lo comprado en un mercado anulado contaba como beneficio de la casa, aunque se había devuelto | Columna «Devoluciones», que se resta | `PrediccionWebTest.loDevueltoAlAnularNoCuentaComoBeneficioDeLaCasa` |

R-11 (cambiar el workflow de la wiki) se descartó por decisión del equipo.

Se dejaron como están a propósito: avisar en el registro de que un email ya existe (lo pide HU-11) y recuperar la contraseña, que no era un fallo sino una funcionalidad nueva (HU-17, hecha después).
