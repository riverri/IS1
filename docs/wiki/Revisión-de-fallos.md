# Revisión de fallos

Se partió de un informe del proyecto con 14 fallos (**F-01 a F-14**) y una revisión completa del código encontró 20 más (**N-01 a N-20**). Los 34 están corregidos en `main`, con 34 pruebas nuevas, y sus issues (#34–#67) están cerrados.
En noviembre, la [revisión final](#revisión-final-noviembre-de-2026) encontró 16 más: 15 corregidos o documentados y uno descartado. La [cuarta depuración](#cuarta-depuración-21112026) encontró 15 más, todos corregidos, y dejó un punto para decidir en equipo. La [quinta](#quinta-depuración-21112026) decidió ese punto y corrigió 14 fallos más.

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

## Cuarta depuración (21/11/2026)

Revisión de lo que entró en `main` después de la tercera depuración: el casino con el baccarat, la liga fantasy, los anuncios y *Mis movimientos* y las optimizaciones de cuotas, clasificaciones y estadísticas. Además, la prueba aleatoria del dinero (`InvariantesDineroTest`) hace ahora también cobros anticipados y bloqueos de liga, y comprueba que su dinero cuadra. Cada arreglo tiene su prueba, y se ha comprobado que las nuevas fallan con el código anterior.

| Fallo | Gravedad | Qué pasaba | Cómo se arregló | Prueba |
|---|---|---|---|---|
| D4-01 · Liga con bloqueos | Alta | No se podía borrar una liga con bloqueos, ni eliminar la cuenta de quien la creó (error al guardar). Lo encontró la prueba aleatoria del dinero | Antes de borrar la liga, sus bloqueos se borran y se devuelven los que aún no se han jugado (evento `LigaBorrada`) | `CashOutYBloqueoLigaWebTest.borrarUnaLigaConBloqueosLosDevuelveYLaBorra`, `.eliminarLaCuentaDeQuienCreoUnaLigaConBloqueosFunciona` |
| D4-02 · Doble clic en el casino | Media | Un doble clic en la ruleta, el baccarat o al repartir jugaba y cobraba dos veces; en «Pedir carta» daba dos cartas (CA-57.5) | Cada formulario lleva un código de un solo uso; «Pedir carta» lleva las cartas que se veían | `CasinoWebTest.unDobleClicEnLaRuletaSoloJuegaYCobraUnaVez`, `.unDobleClicAlRepartirOEnElBaccaratTampocoCobraDosVeces`, `.unDobleClicEnPedirCartaSoloDaUna` |
| D4-03 · Doble clic al fichar | Media | Se podía fichar dos veces al mismo jugador o pasarse del presupuesto y de la formación: añadir un fichaje no subía la versión del equipo | El equipo se bloquea en la base de datos al fichar o vender | `FantasyConcurrenteTest` |
| D4-04 · Traspasos en la fantasy | Media | Si la API cambiaba a un jugador de club o de posición, sus puntos pasados se recalculaban (de 7 a 0) y la formación se rompía (8 jugadores, uno sin poder venderse) | Cada fichaje guarda el club, la posición y el nombre de cuando se fichó (migración V30) | `FantasyWebTest.unTraspasoNoCambiaLoQueYaSumoNiDesdeQueClubPuntua` |
| D4-05 · Jugador borrado | Media | Al borrar un jugador (o desaparecer de la API) se borraban sus fichajes y con ellos sus puntos (CA-82.1) | Los fichajes se quedan sin jugador (`on delete set null`) y se pueden vender | `FantasyWebTest.borrarUnJugadorNoBorraSusPuntosYSePuedeVenderSuFichaje` |
| D4-06 · Puesto del perfil | Media | Con el mismo saldo, el perfil no daba el puesto del ranking: dos jugadores con el mismo nombre salían los dos 1.º, y en PostgreSQL «bea» iba antes que «Carla» | Los empatados se ordenan en Java, como en el ranking (nombre e id) | `PerfilYMovimientosWebTest.elPuestoDelPerfilEsElDelRankingAunqueSeLlamenIgual` |
| D4-07 · Impresiones de anuncios | Media | Contaban también las descargas CSV, las redirecciones y las páginas que daban error (HU-62) | Se cuentan al pintar la página (`ImpresionesAnuncio`) | `ImpresionesYDestinosWebTest.soloCuentaLaImpresionSiSePintaLaPagina` |
| D4-08 · Anuncios para visitantes | Media | A un visitante se le enseñaban anuncios que llevaban a páginas con sesión (al pulsarlos, al login), y no los de páginas públicas de secciones con sesión (CA-60.3); se aceptaban destinos como `/cuenta/../tema` | Se pregunta a Spring Security si un visitante puede abrir el destino; los destinos con `..` no valen | `ImpresionesYDestinosWebTest` |
| D4-09 · Cuentas eliminadas en la fantasy | Baja | Seguían en la clasificación | Se quitan, como en el resto de clasificaciones | `FantasyWebTest.lasCuentasEliminadasNoSalenEnLaClasificacion` |
| D4-10 · «Otra mano» doblada | Baja | Tras doblar, «Otra mano» proponía el importe doblado | Propone la apuesta inicial | `CasinoWebTest.otraManoProponeLaApuestaInicialAunqueSeDoblara` |
| D4-11 · Regla del 17 blando | Baja | Cambiarla en Gestión afectaba a las manos ya repartidas (CA-59.1) | Se guarda con la mano al repartir | `CasinoWebTest.cambiarLaReglaDel17NoAfectaALaManoYaRepartida` |
| D4-12 · Manos abandonadas | Baja | Una mano de blackjack a medias no terminaba nunca y no entraba en las cuentas de la casa | Pasado un día se planta sola | `CasinoWebTest.lasManosAbandonadasSePlantanSolasPasadoUnDia` |
| D4-13 · Mis movimientos | Baja | Si la recarga se aplicaba al abrir la página, la cabecera enseñaba el saldo anterior (CA-55.2); al atrasar la hora en octubre el orden fallaba | Cabecera con el saldo recargado; orden por id | `PerfilYMovimientosWebTest.laCabeceraDeMisMovimientosEnsenaElSaldoYaRecargado` |
| D4-14 · Clics de anuncios caducados | Baja | Un enlace viejo a un anuncio caducado seguía sumando clics; editar un anuncio podía pisar impresiones contadas a la vez | Solo cuentan los vigentes (y el enlace lleva `nofollow`); las impresiones no se escriben al editar | `ImpresionesYDestinosWebTest.unAnuncioCaducadoNoSumaClics` |
| D4-15 · Doble clic al crear el equipo fantasy | Baja | El segundo envío daba un error 500 | Mensaje «Ya tienes un equipo» | — |

**Para decidir en equipo:** en la fantasy se puede vender a un jugador cuyo partido ya se ha jugado y fichar a otro que juega más tarde, así que un mismo hueco suma los puntos de varios partidos de la jornada. Las historias (HU-81 a HU-83) no dicen si hay cierre de jornada o un límite de cambios, así que no se ha tocado. *Resuelto en la quinta depuración (D5-03).*

## Quinta depuración (21/11/2026)

Revisión de las apuestas (cobro anticipado, supercuotas, retos y quinielas), las cuentas y la seguridad, la importación desde la API y los arreglos de la cuarta depuración. También se ha decidido la regla de la jornada de la liga fantasy, que la cuarta depuración dejó pendiente ([ADR 0007](https://github.com/riverri/IS1/blob/main/docs/decisiones/0007-jornada-fantasy.md)). Cada arreglo tiene su prueba, y las nuevas fallan con el código anterior.

| Fallo | Gravedad | Qué pasaba | Cómo se arregló | Prueba |
|---|---|---|---|---|
| D5-01 · Supercuota y cobro anticipado | Alta | Una simple normal se pasaba a la supercuota «cambiando» el importe al mismo valor (gratis). Después se podía cobrar antes, casi por el doble de lo apostado y sin riesgo | Una apuesta con la cuota de una supercuota no se puede cobrar antes, sea cual sea su fecha | `QuintaDepuracionApuestasTest.unaApuestaQuePasaALaSupercuotaAlCambiarElImporteNoSePuedeCobrarAntes` |
| D5-02 · Subidas de importe y juego responsable | Alta | Lo que se subía a una apuesta antigua no contaba después en los límites diario y semanal (HU-10): la apuesta cuenta en la fecha en que se hizo | Las subidas son un movimiento propio (`SUBIDA_APUESTA`) que cuenta cuando se hace, y cada apuesta cuenta lo que se apostó al hacerla (migración V31) | `QuintaDepuracionApuestasTest.subirElImporteDeUnaApuestaAntiguaCuentaEnElLimiteDeHoy`, `.unaApuestaDeHoySubidaYBajadaCuentaLoQueQueda` |
| D5-03 · Jornada de la liga fantasy | Media | Se podía vender a un jugador que ya había jugado y fichar a otro que jugaba más tarde: un mismo hueco sumaba varios partidos de la jornada | Jornada de lunes a domingo. Con la jornada empezada, los cambios cuentan desde la siguiente (CA-82.3, ADR 0007) | `FantasyWebTest.unHuecoNoSumaDosPartidosDeLaMismaJornada`, `.conLaJornadaEmpezadaLoQueSeFichaSumaDesdeElLunes`, `.laPaginaAvisaDeQueLaJornadaHaEmpezado` |
| D5-04 · Partido en juego con la fecha cambiada | Media | Si el creador había cambiado la fecha de un partido de la API y la liga lo adelantaba, se seguía apostando mientras se jugaba | Si la API dice que ya se juega o ha terminado antes de nuestra fecha, se adelanta a la suya | `SincronizacionServiceTest.unPartidoEnJuegoNoAdmiteApuestasAunqueTengaFechaManual` |
| D5-05 · Límite de envíos | Media | `POST /regis%74ro` creaba cuentas sin contar en el límite por IP | Se compara la ruta descodificada, como la ve Spring MVC | `LimitePeticionesTest.laRutaCodificadaCuentaComoLaMisma` |
| D5-06 · Quiniela con cuentas eliminadas | Media | El boleto de una cuenta eliminada se quedaba su parte del bote (que no se pagaba a nadie), y los que habían acertado cobraban menos | Los boletos de cuentas eliminadas no ganan | `QuinielaWebTest.elBoletoDeUnaCuentaEliminadaNoSeQuedaParteDelBote` |
| D5-07 · Plantillas de la API | Media | Si fallaba la descarga de la liga del nuevo club de un jugador, se le borraba, y al día siguiente se creaba otro: se perdía su nota y el enlace con sus fichajes de la fantasy | Si alguna liga falla, ese día no se borra a ningún jugador | `SincronizacionPlantillasTest.siFallaLaLigaDelEquipoNuevoElTraspasadoNoSeBorra` |
| D5-08 · Bloqueo de liga al subir | Baja | Con un bloqueo de liga se podía subir la apuesta que ya había a la selección bloqueada | Subir el importe comprueba los bloqueos; bajarlo, no (CA-105.3) | `QuintaDepuracionApuestasTest.conUnBloqueoDeLigaNoSePuedeSubirLaApuestaQueYaHabia` |
| D5-09 · Quiniela con un anulado | Baja | Si nadie acertaba ningún partido jugado, el anulado (que cuenta como acierto) hacía «ganar» a todos, y todos perdían la comisión | Se devuelve todo, como si nadie hubiera acertado (CA-80.3) | `QuinielaWebTest.conUnAnuladoYNingunAciertoEnLosDemasSeDevuelveTodo` |
| D5-10 · Bloqueos de partidos aplazados | Baja | Al borrar una liga, los bloqueos de un partido aplazado se borraban sin devolverlos | Se devuelven también | `CashOutYBloqueoLigaWebTest.borrarUnaLigaDevuelveTambienLosBloqueosDeUnPartidoAplazado` |
| D5-11 · Anuncios con filtros | Baja | Un anuncio con destino como `/eventos?deporte=FUTBOL` no se enseñaba nunca a los visitantes | Se comprueba solo la ruta, sin `?` ni `#` | `ImpresionesYDestinosWebTest.unDestinoConFiltrosTambienSeEnsenaAUnVisitante` |
| D5-12 · Puesto del resumen semanal | Baja | Igual que D4-06, pero en el correo: con empates, en PostgreSQL el puesto podía no ser el del ranking | Mismo orden que el ranking, en Java | — (en H2 no se reproduce) |
| D5-13 · Avisos a cuentas eliminadas | Baja | Si se resolvía una apuesta de una cuenta ya eliminada, se mandaba el correo de premio a su dirección inventada y se creaban avisos para nadie | No se avisa a las cuentas eliminadas, y al eliminarla se desactivan sus correos | `CorreoPremioWebTest.unaCuentaEliminadaNoRecibeNiCorreoNiAvisoAlResolverseSuApuesta` |
| D5-14 · Manos de cuentas eliminadas | Baja | La mano de blackjack abandonada de una cuenta eliminada se plantaba y su premio contaba como pagado en las cuentas de la casa | Se cierra con premio 0 | `CasinoWebTest.laManoAbandonadaDeUnaCuentaEliminadaNoCuentaComoPagada` |
| D5-15 · Tema en Mis movimientos | Baja | Cambiar el tema en Mis movimientos llevaba a Mi cuenta (CA-94.1) | Vuelve a Mis movimientos | `TemaWebTest.desdeMisMovimientosVuelveAMisMovimientos` |

**Sin arreglar, para el equipo:**
- **Cobro anticipado de una apuesta recién hecha:** si el usuario ya tenía otra apuesta al mismo resultado, la nueva vale un poco menos de lo apostado nada más hacerla (CA-75.1). La forma sencilla de arreglarlo (cobrar siempre lo apostado cuando hay otra apuesta propia) dejaría salir sin perder de una apuesta que va mal. Se deja así: va en contra del usuario, no de la casa.
- **IP detrás del proxy:** en los perfiles `nube` y `compartir`, el límite de envíos usa la IP que llega en `X-Forwarded-For`. Si Render añade la IP real al final de la que manda el navegador (en vez de sustituirla), un atacante podría cambiar la suya en cada envío. Hay que comprobarlo en Render antes de cambiar nada: la misma cabecera decide si la conexión es HTTPS.
- **Códigos del casino:** se guardan los 20 últimos de cada sesión. Si se cargan más de 20 páginas del casino en una pestaña sin jugar en otra, la otra recibe el aviso de «has pulsado dos veces». No se cobra nada de más.

## En producción

Fallos del despliegue en Render, no del código de la aplicación. Cómo reconocerlos: [Ejecutar y desplegar → Si un deploy falla en Render](Ejecutar-y-desplegar#si-un-deploy-falla-en-render).

| Fallo | Gravedad | Qué pasaba | Cómo se arregló | Pull request |
|---|---|---|---|---|
| D-01 · Render no veía el puerto | Alta | Del 04/11 al 15/11 fallaron todos los deploys (unos 20, también los que solo cambiaban documentación), y la web se quedó en la versión del 04/11 (`a27c56c`). La aplicación arrancaba en el puerto 10000, pero Java escuchaba en IPv6 (`::`) y Render, que busca IPv4 (`0.0.0.0`), repetía `No open HTTP ports detected on 0.0.0.0` hasta cancelar el deploy a los 15 minutos. El primer deploy que falló (`bd98bd5`) tenía el mismo código que el último que funcionó, así que lo más probable es que cambiara la forma en que Render detecta el puerto. Efecto secundario: los deploys fallidos sí aplicaron las migraciones V11 a V15.1 en Neon | `server.address=0.0.0.0` (perfil `nube`), `-Djava.net.preferIPv4Stack=true` (`Dockerfile`) y *health check* en `/ping`, que no usa la base de datos. Publicado el 15/11 | [#103](https://github.com/riverri/IS1/pull/103) |

## Al integrar

> *Texto redactado por la IA (Claude) a partir de lo que pasó; el equipo puede personalizarlo.*

Fallos al fusionar pull requests en `main`, no del código de cada uno por separado. Las acciones para que no se repitan están en la retrospectiva del [Sprint 5](https://github.com/riverri/IS1/blob/main/docs/scrum/sprints/sprint-5.md#sprint-retrospective-fecha).

| Fallo | Gravedad | Qué pasaba | Cómo se arregló | Pull request |
|---|---|---|---|---|
| I-01 · PR fusionados en desorden | Alta (no llegó a producción) | El 21/11 se fusionaron en media hora #138, #133, #125 y #132, en un orden distinto del previsto. La liga fantasy (#133, migración V27) entró antes que el baccarat (#132, migración V26), y Flyway no aplica una migración con número menor que la última ya aplicada: la base de datos de Render no habría arrancado. Además, #132 estaba apilado sobre la rama del casino (#125) y se fusionó 21 segundos después que él, antes de que GitHub cambiara su base a `main`, así que el baccarat acabó en `codigo/vuelve-casino` y no en `main`. También se vio a tiempo que #125 y #131 ponían una tarjeta en el mismo hueco de `gestion/panel.html`, lo que habría dado conflicto | Migración del baccarat renumerada a V28 y PR nuevo contra `main` (#140); tarjeta de anuncios movida en #131. Se probó en local `main` con los PR pendientes juntos (584 pruebas), se fusionaron de uno en uno esperando al CI, y se comprobó que `main` quedaba idéntico a lo probado | [#140](https://github.com/riverri/IS1/pull/140), [#131](https://github.com/riverri/IS1/pull/131) |
