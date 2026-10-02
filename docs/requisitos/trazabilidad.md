# Matriz de trazabilidad

Relaciona cada criterio de aceptación de las [historias de usuario](historias-de-usuario.md) con la prueba automática que lo comprueba. Cada prueba se identifica por su clase y su método, dentro de `src/test/java`. Todas se ejecutan con `./mvnw test` y en el CI de cada pull request.

**Estado del criterio:**
- **Automática:** hay al menos una prueba que lo comprueba.
- **Manual:** se comprueba a mano porque depende del navegador (JavaScript o diseño en el móvil).
- **Sin prueba:** está hecho, pero falta una prueba automática.

## Resumen

| Historias hechas | Criterios | Automática | Manual | Sin prueba |
|---:|---:|---:|---:|---:|
| 44 | 92 | 88 (96 %) | 3 | 1 |

**Fuera de la matriz:**
- HU-15 y HU-16 (cuenta bancaria y retiradas) y HU-38 a HU-43 (parte social) no se hacen (Won't, filas 25 y 26 del backlog).
- HU-17 (recuperar la contraseña) está pendiente.

**Huecos conocidos, candidatos a una prueba nueva:**
- HU-33: el mensaje cuando no hay enfrentamientos previos.
- HU-10: consultar la cuenta durante una pausa.
- HU-19: el orden por fecha del catálogo.

Al terminar una historia, se añaden aquí sus criterios con sus pruebas. Forma parte de la Definition of Done (ver [proceso](../scrum/proceso.md)).

## HU-01 · Alta de competiciones y equipos con su calificación

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Un equipo dado de alta queda disponible para crear eventos | Automática | [`GestionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/gestion/GestionWebTest.java)`.altaDeEquipoLoDejaDisponible`<br>[`GestionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/gestion/GestionWebTest.java)`.altaDeEvento` |
| Una calificación fuera de 0–10 se rechaza | Automática | [`GestionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/gestion/GestionWebTest.java)`.rechazaCalificacionFueraDeRango`<br>[`GestionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/gestion/GestionWebTest.java)`.edicionRechazaCalificacionFueraDeRango`<br>[`EquipoTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/equipos/EquipoTest.java)`.rechazaCalificacionMayorQueDiez`<br>[`EquipoTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/equipos/EquipoTest.java)`.rechazaCalificacionNegativa` |

## HU-02 · Forma reciente de un equipo

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Al cambiar la forma se recalculan las cuotas de sus eventos | Automática | [`FormaYVolumenWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/cuotas/FormaYVolumenWebTest.java)`.elCreadorCambiaLaFormaYBajaLaCuota`<br>[`CalculadoraCuotasTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/cuotas/CalculadoraCuotasTest.java)`.laBuenaFormaBajaLaCuota` |

## HU-03 · Cuotas automáticas con margen para la casa

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Cada evento tiene una cuota por resultado a partir de calidad y forma | Automática | [`CalculadoraCuotasTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/cuotas/CalculadoraCuotasTest.java)`.elFavoritoTieneCuotaMasBaja`<br>[`CalculadoraCuotasTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/cuotas/CalculadoraCuotasTest.java)`.conEquiposIgualesJugarEnCasaEsVentaja`<br>[`ApuestaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ApuestaWebTest.java)`.elCatalogoMuestraLasCuotasCalculadas` |
| La suma de 1/cuota es siempre mayor que 1 | Automática | [`CalculadoraCuotasTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/cuotas/CalculadoraCuotasTest.java)`.laCasaSiempreTieneMargen`<br>[`CalculadoraCuotasTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/cuotas/CalculadoraCuotasTest.java)`.conVolumenLaCasaSigueTeniendoMargen` |
| Ninguna cuota baja del mínimo (1,01) | Automática | [`CalculadoraCuotasTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/cuotas/CalculadoraCuotasTest.java)`.ningunaCuotaBajaDelMinimo`<br>[`CalculadoraCuotasTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/cuotas/CalculadoraCuotasTest.java)`.elVolumenTieneUnPesoMaximo` |

## HU-04 · Introducir el resultado

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Al introducir el resultado el evento se finaliza y se resuelven sus apuestas | Automática | [`ResolucionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ResolucionWebTest.java)`.introducirResultadoResuelveYPagaLasApuestas`<br>[`ResolucionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ResolucionWebTest.java)`.elFinalizadoYaNoAdmiteApuestas` |
| Corregir el resultado vuelve a resolver y ajusta los saldos | Automática | [`ResolucionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ResolucionWebTest.java)`.corregirElResultadoReajustaLosSaldos`<br>[`CombinadaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/CombinadaWebTest.java)`.corregirUnResultadoRecalculaLaCombinada` |

## HU-05 · Suspender o anular un evento

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Suspendido no admite apuestas hasta reactivarlo | Automática | [`ResolucionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ResolucionWebTest.java)`.suspenderQuitaElEventoDelCatalogoYReactivarLoDevuelve` |
| Anulado devuelve el importe a cada usuario | Automática | [`ResolucionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ResolucionWebTest.java)`.anularDevuelveElImporteATodos` |

## HU-06 · Dinero apostado a cada resultado

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| El creador ve importe y número de apuestas de cada resultado (y de los otros tipos de apuesta) | Automática | [`ResolucionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ResolucionWebTest.java)`.elCreadorVeCuantoSeHaApostadoACadaResultado`<br>[`OtrasApuestasWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/OtrasApuestasWebTest.java)`.enGestionSeVeElDineroApostadoATodosLosTipos` |

## HU-07 · Límites de apuesta del creador

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Fuera de los límites no se puede apostar y se indica el límite | Automática | [`LimitesWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/LimitesWebTest.java)`.rechazaApuestasFueraDeLosLimites`<br>[`LimitesWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/LimitesWebTest.java)`.elBoletoRespetaElMaximoDeSelecciones`<br>[`LimitesWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/LimitesWebTest.java)`.elMaximoNoPuedeSerMenorQueElMinimo` |

## HU-08 · Catálogo sin registrarse

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Un visitante ve los eventos y sus cuotas | Automática | [`CatalogoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/eventos/CatalogoWebTest.java)`.unVisitanteVeElCatalogoAgrupadoPorDeporte` |
| Un visitante que intenta apostar va al inicio de sesión | Automática | [`CatalogoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/eventos/CatalogoWebTest.java)`.unVisitanteQueIntentaApostarVaAlLogin`<br>[`ApuestaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ApuestaWebTest.java)`.unVisitanteNoPuedeApostar`<br>[`CombinadaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/CombinadaWebTest.java)`.unVisitanteQueAnadeAlBoletoVaAlLogin` |

## HU-09 · Web adaptada a móvil

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Todas las páginas se ven y se usan bien en el móvil | Manual | Capturas a 390 px de ancho en cada sprint con cambios de pantallas (Playwright). |

## HU-10 · Juego responsable

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Con el límite diario alcanzado no se puede apostar más | Automática | [`JuegoResponsableWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/JuegoResponsableWebTest.java)`.elLimiteDiarioCuentaLoYaApostado`<br>[`JuegoResponsableWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/JuegoResponsableWebTest.java)`.lasCanceladasNoCuentan` |
| Con una pausa no se puede apostar, pero sí consultar la cuenta | Automática | [`JuegoResponsableWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/JuegoResponsableWebTest.java)`.conPausaNoSePuedeApostarNiSubirElImporte`<br>[`JuegoResponsableWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/JuegoResponsableWebTest.java)`.unaPausaNoSePuedeAcortar`<br>La consulta de la cuenta durante la pausa no tiene prueba propia. |

## HU-11 · Registro

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Con datos válidos se crea la cuenta | Automática | [`UsuariosWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/UsuariosWebTest.java)`.registroCreaLaCuentaConElSaldoDeBienvenida` |
| Con un email ya registrado se avisa | Automática | [`UsuariosWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/UsuariosWebTest.java)`.registroConEmailExistenteAvisa`<br>[`UsuariosWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/UsuariosWebTest.java)`.registroConContrasenasDistintasAvisa` |

## HU-12 · Inicio de sesión

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Con los datos correctos se entra | Automática | [`UsuariosWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/UsuariosWebTest.java)`.loginCorrectoDaAcceso` |
| Con datos incorrectos se deniega con un mensaje genérico | Automática | [`UsuariosWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/UsuariosWebTest.java)`.loginIncorrectoDeniegaConMensajeGenerico` |

## HU-13 · Consultar el saldo

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| En Mi cuenta se ve el saldo | Automática | [`UsuariosWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/UsuariosWebTest.java)`.laCuentaMuestraElSaldo`<br>[`UsuariosWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/UsuariosWebTest.java)`.laCuentaExigeSesion` |
| El saldo está actualizado tras apostar o recargar | Automática | [`ApuestaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ApuestaWebTest.java)`.apostarDescuentaElSaldoYQuedaActiva`<br>[`UsuarioTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/UsuarioTest.java)`.recargaCuandoHaPasadoElPeriodo` |

## HU-14 · Saldo de bienvenida y recargas

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Al registrarse se recibe el saldo de bienvenida | Automática | [`UsuariosWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/UsuariosWebTest.java)`.registroCreaLaCuentaConElSaldoDeBienvenida`<br>[`UsuarioTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/UsuarioTest.java)`.empiezaConElSaldoDeBienvenida` |
| Pasado el periodo se recibe una recarga, una sola vez | Automática | [`UsuarioTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/UsuarioTest.java)`.noRecargaAntesDeQuePaseElPeriodo`<br>[`UsuarioTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/UsuarioTest.java)`.recargaCuandoHaPasadoElPeriodo`<br>[`UsuarioTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/UsuarioTest.java)`.soloRecargaUnaVezPorPeriodo` |

## HU-18 · Eliminar la cuenta

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Antes de eliminarla se avisa de lo que se pierde y se pide confirmación | Manual | El aviso y la confirmación están en *Mi cuenta*; se ha comprobado a mano. [`EliminarCuentaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/EliminarCuentaWebTest.java)`.conLaContrasenaIncorrectaNoSeElimina` comprueba que sin la contraseña no se elimina. |
| Tras eliminarla no se puede entrar y se borran los datos personales | Automática | [`EliminarCuentaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/EliminarCuentaWebTest.java)`.conLaContrasenaCorrectaSeAnonimizaYSeCancelanLasPendientes`<br>[`EliminarCuentaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/EliminarCuentaWebTest.java)`.desapareceDelRankingYYaNoPuedeEntrar`<br>[`EliminarCuentaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/EliminarCuentaWebTest.java)`.elEmailQuedaLibreParaOtraCuenta` |

## HU-19 · Catálogo por deporte y fecha

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Los eventos aparecen agrupados por deporte y ordenados por fecha | Automática | [`CatalogoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/eventos/CatalogoWebTest.java)`.unVisitanteVeElCatalogoAgrupadoPorDeporte`<br>El orden por fecha lo da la consulta del repositorio; no tiene prueba propia. |
| Un evento empezado o terminado ya no se puede apostar | Automática | [`CatalogoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/eventos/CatalogoWebTest.java)`.losEventosYaJugadosNoAparecen`<br>[`EventoTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/eventos/EventoTest.java)`.noAdmiteApuestasSiYaHaEmpezado`<br>[`ApuestaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ApuestaWebTest.java)`.noSePuedeApostarAUnEventoQueYaHaEmpezado` |

## HU-20 · Cuotas de cada resultado

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Cada evento muestra una cuota por resultado | Automática | [`ApuestaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ApuestaWebTest.java)`.elCatalogoMuestraLasCuotasCalculadas`<br>[`ApuestaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ApuestaWebTest.java)`.noSePuedeApostarAlEmpateEnBaloncesto` |
| Las cuotas se actualizan con el dinero apostado | Automática | [`FormaYVolumenWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/cuotas/FormaYVolumenWebTest.java)`.elDineroApostadoBajaLaCuotaDeEseResultado`<br>[`FormaYVolumenWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/cuotas/FormaYVolumenWebTest.java)`.lasApuestasYaHechasConservanSuCuota` |

## HU-21 · Cargar eventos a mano o desde la API

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Un evento dado de alta o editado aparece en el catálogo | Automática | [`GestionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/gestion/GestionWebTest.java)`.altaDeEvento`<br>[`EditarEventoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/gestion/EditarEventoWebTest.java)`.editaEquiposFechaYFase` |
| Con la API se sincronizan eventos y resultados solos | Automática | [`SincronizacionServiceTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/api/SincronizacionServiceTest.java)`.creaLosPartidosYEquiposQueFaltan`<br>[`SincronizacionServiceTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/api/SincronizacionServiceTest.java)`.cambiaLaFechaSiLaApiLaMueve`<br>[`SincronizacionServiceTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/api/SincronizacionServiceTest.java)`.sincronizarDosVecesNoDuplica`<br>[`SincronizacionServiceTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/api/SincronizacionServiceTest.java)`.aplazadoSeSuspendeYCanceladoSeAnula` |

## HU-22 · Búsqueda y filtros

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Buscando un equipo salen solo sus eventos | Automática | [`BusquedaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/eventos/BusquedaWebTest.java)`.buscaSinDistinguirTildesNiMayusculas` |
| Los filtros dejan solo los eventos que los cumplen | Automática | [`BusquedaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/eventos/BusquedaWebTest.java)`.filtraPorDeporte`<br>[`BusquedaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/eventos/BusquedaWebTest.java)`.buscaPorCompeticion`<br>El filtro por fecha no está hecho: el catálogo ya va ordenado por fecha. |
| Si nada cumple la búsqueda se avisa | Automática | [`BusquedaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/eventos/BusquedaWebTest.java)`.avisaSiNoHayResultados` |

## HU-23 · Apuesta simple

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Con saldo suficiente se descuenta y queda activa | Automática | [`ApuestaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ApuestaWebTest.java)`.apostarDescuentaElSaldoYQuedaActiva` |
| Sin saldo suficiente no se puede y se avisa | Automática | [`ApuestaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ApuestaWebTest.java)`.sinSaldoSuficienteNoSePuedeApostar`<br>[`ApuestaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ApuestaWebTest.java)`.rechazaImportesNoValidos` |

## HU-24 · Apuestas activas

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Se ven las activas con su evento, cuota e importe | Automática | [`ApuestaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ApuestaWebTest.java)`.misApuestasMuestraLasActivasYLoComprometido` |

## HU-25 · Resolución automática

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Al finalizar el evento sus apuestas pasan a ganada o perdida | Automática | [`ResolucionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ResolucionWebTest.java)`.introducirResultadoResuelveYPagaLasApuestas`<br>[`SincronizacionServiceTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/api/SincronizacionServiceTest.java)`.unPartidoTerminadoResuelveLasApuestas` |
| Una apuesta ganada suma la ganancia al saldo | Automática | [`ResolucionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ResolucionWebTest.java)`.introducirResultadoResuelveYPagaLasApuestas` |
| El resultado aparece en el historial sin hacer nada | Automática | [`ResolucionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ResolucionWebTest.java)`.historialYEstadisticasTrasResolver` |

## HU-26 · Cancelar una apuesta

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Antes de empezar se devuelve el importe | Automática | [`CancelacionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/CancelacionWebTest.java)`.cancelarDevuelveElImporte`<br>[`LargoPlazoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/LargoPlazoWebTest.java)`.sePuedeCancelarMientrasElMercadoEsteAbierto` |
| Empezado el evento no se puede cancelar | Automática | [`CancelacionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/CancelacionWebTest.java)`.noSePuedeCancelarSiElEventoYaHaEmpezado`<br>[`CancelacionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/CancelacionWebTest.java)`.noSePuedeCancelarLaApuestaDeOtro` |

## HU-27 · Cambiar el importe

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Al subirlo se cobra la diferencia con la cuota vigente | Automática | [`ModificarImporteWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ModificarImporteWebTest.java)`.subirElImporteCobraLaDiferenciaYAplicaLaCuotaActual`<br>[`ModificarImporteWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ModificarImporteWebTest.java)`.respetaLosLimitesYElSaldo` |
| Al bajarlo se devuelve la diferencia | Automática | [`ModificarImporteWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ModificarImporteWebTest.java)`.bajarElImporteDevuelveLaDiferencia`<br>[`ModificarImporteWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ModificarImporteWebTest.java)`.noSePuedeCambiarSiElEventoHaEmpezado` |

## HU-28 · Boleto de combinadas

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Se añaden selecciones de eventos distintos | Automática | [`CombinadaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/CombinadaWebTest.java)`.confirmarCreaLaCombinadaYDescuentaElSaldo` |
| Dos selecciones del mismo evento no se permiten | Automática | [`CombinadaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/CombinadaWebTest.java)`.noSePuedenCombinarDosSeleccionesDelMismoEvento` |
| Si solo queda una selección es una apuesta simple | Automática | [`CombinadaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/CombinadaWebTest.java)`.siSoloQuedaUnaSeleccionEsUnaApuestaSimple` |

## HU-29 · Multiplicador total y ganancia potencial

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| El multiplicador es el producto de las cuotas | Automática | [`CombinadaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/CombinadaWebTest.java)`.elBoletoMuestraElMultiplicadorTotal` |
| Con un importe se ve la ganancia potencial antes de confirmar | Manual | Se calcula en el navegador (JavaScript) al escribir el importe; se ha comprobado a mano en las capturas. |
| Si cambia una cuota se pide aceptar la nueva | Automática | [`CombinadaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/CombinadaWebTest.java)`.siCambiaUnaCuotaSePideAceptarla`<br>[`LargoPlazoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/LargoPlazoWebTest.java)`.siElCreadorCambiaLaCuotaNoSeApuestaConLaVieja` |

## HU-30 · Resolución de combinadas

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Si falla una selección se pierde sin esperar al resto | Automática | [`CombinadaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/CombinadaWebTest.java)`.laCombinadaSePierdeEnCuantoFallaUnaSeleccion` |
| Si aciertan todas se gana y se cobra | Automática | [`CombinadaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/CombinadaWebTest.java)`.laCombinadaSeGanaSiAciertanTodas` |
| Un evento anulado cuenta con cuota 1,00 | Automática | [`CombinadaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/CombinadaWebTest.java)`.unEventoAnuladoCuentaConCuotaUno` |

## HU-31 · Ficha del equipo

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Se ven los últimos resultados y la clasificación | Automática | [`FichaEquipoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/eventos/FichaEquipoWebTest.java)`.laFichaTieneBalanceRachaYClasificacion`<br>[`FichaEquipoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/eventos/FichaEquipoWebTest.java)`.resultadoDesdeCadaEquipo`<br>[`FichaEquipoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/eventos/FichaEquipoWebTest.java)`.laFichaEsPublica` |
| Desde un evento se llega a la ficha de cada equipo | Automática | [`FichaEquipoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/eventos/FichaEquipoWebTest.java)`.laListaDeEquiposEnlazaConLasFichas` |

## HU-32 · Gráfico de evolución

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| En la ficha se ve un gráfico con los puntos jornada a jornada | Automática | [`FichaEquipoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/eventos/FichaEquipoWebTest.java)`.elGraficoDeEvolucionAcumulaLosPuntos`<br>[`FichaEquipoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/eventos/FichaEquipoWebTest.java)`.conMenosDeDosPartidosNoHayGrafico` |

## HU-33 · Cara a cara

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Se ve el historial de enfrentamientos y las rachas de los dos | Automática | [`FichaEquipoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/eventos/FichaEquipoWebTest.java)`.caraACaraEntreLosRivalesDeUnPartido` |
| Sin enfrentamientos previos se indica | Sin prueba | La página lo muestra ("No hay enfrentamientos anteriores registrados"), pero no hay prueba automática. |

## HU-34 · Historial

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Se ven las apuestas resueltas con evento, cuota, importe y resultado | Automática | [`ResolucionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ResolucionWebTest.java)`.historialYEstadisticasTrasResolver`<br>[`OtrasApuestasWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/OtrasApuestasWebTest.java)`.enLasApuestasSeVeElTipo` |

## HU-35 · Estadísticas personales

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Se ven el porcentaje de aciertos y la rentabilidad | Automática | [`EstadisticasTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/EstadisticasTest.java)`.calculaAciertosYRentabilidad`<br>[`EstadisticasTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/EstadisticasTest.java)`.sinApuestasResueltasTodoACero`<br>[`ResolucionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ResolucionWebTest.java)`.historialYEstadisticasTrasResolver` |

## HU-36 · Ranking

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Se ve una clasificación ordenada de los jugadores | Automática | [`RankingWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/RankingWebTest.java)`.ordenaPorSaldoYNoIncluyeAlCreador` |
| Se puede ordenar por ganancias, aciertos o saldo | Automática | [`ResolucionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ResolucionWebTest.java)`.rankingPorGananciasYPorAciertos` |
| Tras resolver una apuesta el ranking está al día | Automática | [`ResolucionWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/ResolucionWebTest.java)`.rankingPorGananciasYPorAciertos` |
| La posición propia sale destacada aunque no esté arriba | Automática | [`RankingWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/RankingWebTest.java)`.destacaAlUsuarioQueLoConsulta` |

## HU-37 · Avisos

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Al resolverse una apuesta llega un aviso | Automática | [`NotificacionesWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/NotificacionesWebTest.java)`.alGanarRecibeUnAvisoConLoQueCobra`<br>[`NotificacionesWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/NotificacionesWebTest.java)`.anularYCorregirTambienAvisan`<br>[`NotificacionesWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/NotificacionesWebTest.java)`.unaCombinadaPendienteDeOtroPartidoNoAvisa` |
| El aviso dice el evento, el resultado y si se ganó | Automática | [`NotificacionesWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/NotificacionesWebTest.java)`.alGanarRecibeUnAvisoConLoQueCobra` |
| Los avisos leídos se pueden consultar después | Automática | [`NotificacionesWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/NotificacionesWebTest.java)`.laCampanaMuestraLosAvisosSinLeerYAlAbrirlosSeLeen` |

## HU-44 · Apuestas a largo plazo

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| En un mercado abierto se ven los candidatos con su cuota | Automática | [`LargoPlazoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/LargoPlazoWebTest.java)`.laPaginaEsPublicaYMuestraLosMercadosAbiertos` |
| Con saldo suficiente se descuenta y queda activa | Automática | [`LargoPlazoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/LargoPlazoWebTest.java)`.apostarDescuentaElSaldoYApareceEnMisApuestas` |
| Un mercado cerrado no admite apuestas | Automática | [`LargoPlazoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/LargoPlazoWebTest.java)`.unMercadoCerradoNoAdmiteApuestas` |

## HU-45 · Gestión de mercados

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Un mercado con al menos dos candidatos aparece al guardarlo | Automática | [`LargoPlazoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/LargoPlazoWebTest.java)`.elCreadorDaDeAltaUnMercadoConSusCandidatos`<br>[`LargoPlazoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/LargoPlazoWebTest.java)`.altaDeMercadoRechazaLineasMalEscritas` |
| Cerrado deja de admitir apuestas | Automática | [`LargoPlazoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/LargoPlazoWebTest.java)`.unMercadoCerradoNoAdmiteApuestas` |
| Al marcar el ganador se cobran sus apuestas y el resto se pierden | Automática | [`LargoPlazoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/LargoPlazoWebTest.java)`.alMarcarElGanadorSePaganLasApuestasYSePuedeCorregir`<br>[`LargoPlazoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/LargoPlazoWebTest.java)`.anularElMercadoDevuelveElImporte` |

## HU-46 · Editar o borrar un evento

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Sin empezar se pueden cambiar fecha, equipos y fase | Automática | [`EditarEventoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/gestion/EditarEventoWebTest.java)`.editaEquiposFechaYFase`<br>[`EditarEventoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/gestion/EditarEventoWebTest.java)`.conApuestasNoSeCambianLosEquiposPeroSiLaFecha`<br>[`EditarEventoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/gestion/EditarEventoWebTest.java)`.unEventoEmpezadoNoSeEdita`<br>Las cuotas se calculan siempre al mostrarlas, así que salen ya con los datos nuevos. |
| Sin apuestas se puede borrar; con apuestas solo anular | Automática | [`EditarEventoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/gestion/EditarEventoWebTest.java)`.borraUnEventoSinApuestas`<br>[`EditarEventoWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/gestion/EditarEventoWebTest.java)`.conApuestasNoSeBorra` |

## HU-47 · Cambiar nombre y contraseña

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Con la contraseña actual correcta se cambia y se entra con la nueva | Automática | [`CuentaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/CuentaWebTest.java)`.cambiaLaContrasenaConLaActualCorrecta`<br>[`CuentaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/CuentaWebTest.java)`.cambiaElNombre` |
| Con la contraseña actual incorrecta no se cambia | Automática | [`CuentaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/usuarios/CuentaWebTest.java)`.rechazaContrasenaActualIncorrectaCortaONoCoincidente` |

## HU-48 · Perfil público de un jugador

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Desde el ranking se ve su perfil con sus cifras, sin sus apuestas | Automática | [`PerfilJugadorWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/PerfilJugadorWebTest.java)`.elRankingEnlazaConLosPerfiles`<br>[`PerfilJugadorWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/PerfilJugadorWebTest.java)`.elPerfilEsPublicoYMuestraSusCifrasPeroNoSusApuestas`<br>[`PerfilJugadorWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/PerfilJugadorWebTest.java)`.cuentaLasApuestasEnJuego` |

## HU-49 · Plantilla y alineación probable

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Con jugadores suficientes se ve la alineación 4-3-3 con los de mejor nota | Automática | [`PlantillaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/equipos/PlantillaWebTest.java)`.laFichaMuestraLaPlantillaYLaAlineacionProbable`<br>[`AlineacionTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/equipos/AlineacionTest.java)`.aIgualNotaSaleElDorsalMasBajo` |
| Se ve la plantilla por posición con dorsal, nacionalidad, edad y nota | Automática | [`PlantillaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/equipos/PlantillaWebTest.java)`.laFichaMuestraLaPlantillaYLaAlineacionProbable`<br>[`PlantillaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/equipos/PlantillaWebTest.java)`.conUnaPlantillaIncompletaHayPlantillaPeroNoAlineacion`<br>[`PlantillaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/equipos/PlantillaWebTest.java)`.sinJugadoresLaFichaLoExplica` |

## HU-50 · Plantillas desde la API y a mano

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Al descargar se crean o actualizan los jugadores sin tocar las notas | Automática | [`SincronizacionPlantillasTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/api/SincronizacionPlantillasTest.java)`.creaLosJugadoresDeLosEquiposQueYaTenemos`<br>[`SincronizacionPlantillasTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/api/SincronizacionPlantillasTest.java)`.actualizaSinTocarLaNotaYQuitaALosQueSeVan`<br>[`FootballDataClienteTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/api/FootballDataClienteTest.java)`.pideLasPlantillasDeUnaCompeticion` |
| Los que se van desaparecen; los añadidos a mano se quedan | Automática | [`SincronizacionPlantillasTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/api/SincronizacionPlantillasTest.java)`.actualizaSinTocarLaNotaYQuitaALosQueSeVan`<br>[`PlantillaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/equipos/PlantillaWebTest.java)`.elCreadorAnadePonNotaYQuitaJugadores` |

## HU-51 · Ligas privadas

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Al crearla se genera un código y quien la crea es miembro | Automática | [`LigasWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/ligas/LigasWebTest.java)`.alCrearlaSeGeneraUnCodigoYQuienLaCreaEsMiembro`<br>[`LigasSinTransaccionTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/ligas/LigasSinTransaccionTest.java)`.laListaDeLigasMuestraSusMiembros` |
| Con el código se entra y se ve su ranking | Automática | [`LigasWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/ligas/LigasWebTest.java)`.conElCodigoSeEntraYElRankingEsSoloDeSusMiembros`<br>[`LigasWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/ligas/LigasWebTest.java)`.unCodigoQueNoExisteAvisa`<br>[`LigasWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/ligas/LigasWebTest.java)`.quienNoEsMiembroNoLaVe` |
| Un miembro puede salir y quien la creó borrarla | Automática | [`LigasWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/ligas/LigasWebTest.java)`.unMiembroPuedeSalirYQuienLaCreoBorrarla` |

## HU-52 · Doble oportunidad, goles y ambos marcan

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| En los partidos de fútbol se ven sus cuotas | Automática | [`OtrasApuestasWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/OtrasApuestasWebTest.java)`.laPaginaDelPartidoMuestraLosOtrosTiposSoloEnFutbol`<br>[`OtrasApuestasWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/OtrasApuestasWebTest.java)`.enBaloncestoNoSePuedenHacer`<br>[`EspecialTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/cuotas/EspecialTest.java)`.laCasaTieneMargenEnCadaParDeOpciones` |
| Se pueden apostar solas o en una combinada | Automática | [`OtrasApuestasWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/OtrasApuestasWebTest.java)`.conElMarcadorSeResuelvenLasDeGoles`<br>[`OtrasApuestasWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/OtrasApuestasWebTest.java)`.seCombinanConOtrosPartidos` |
| Con el marcador se resuelven solas; sin él, las de goles se anulan | Automática | [`OtrasApuestasWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/OtrasApuestasWebTest.java)`.conElMarcadorSeResuelvenLasDeGoles`<br>[`OtrasApuestasWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/OtrasApuestasWebTest.java)`.sinMarcadorLasDeGolesSeAnulanYLasDeDobleOportunidadSeResuelven`<br>[`OtrasApuestasWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/apuesta/OtrasApuestasWebTest.java)`.corregirElMarcadorVuelveAResolver`<br>[`EspecialTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/cuotas/EspecialTest.java)`.golesYAmbosMarcanNecesitanElMarcador`<br>[`SincronizacionServiceTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/api/SincronizacionServiceTest.java)`.conElMarcadorSeResuelvenTambienLasApuestasDeGoles`<br>[`SincronizacionServiceTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/api/SincronizacionServiceTest.java)`.conProrrogaSoloSeUsaElGanador` |

## HU-54 · Cuentas de la casa

| Criterio de aceptación | Estado | Pruebas |
|---|---|---|
| Se ven el apostado, el pagado, el beneficio y el margen real frente al teórico | Automática | [`CasaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/gestion/CasaWebTest.java)`.cuentaLoApostadoLoPagadoYElBeneficio`<br>[`CasaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/gestion/CasaWebTest.java)`.elCreadorVeElPanelYUnUsuarioNo` |
| Se ve el desglose por tipo de apuesta y por deporte | Automática | [`CasaWebTest`](../../src/test/java/es/ucm/fdi/is1/apuestas/gestion/CasaWebTest.java)`.cuentaLoApostadoLoPagadoYElBeneficio` |
