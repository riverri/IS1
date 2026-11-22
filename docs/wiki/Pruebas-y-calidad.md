# Pruebas y calidad

## Las pruebas automáticas
Se ejecutan todas con `./mvnw test` (Windows: `mvnw.cmd test`).

- **Unitarias:** una clase sola, sin base de datos ni web. Ejemplo: `CalculadoraCuotasTest` comprueba que la buena forma baja la cuota y que la suma de 1/cuota es mayor que 1.
- **Web** (`...WebTest`): arrancan la aplicación con una base de datos de prueba y simulan peticiones con **MockMvc**. Ejemplo: Ana inicia sesión, apuesta 10 y su saldo baja 10.

La mayoría llevan `@Transactional`: lo que hacen se deshace al terminar. La hora se fija con un reloj de prueba o la hora de Madrid (clase `Hora`).

| Paquete | Clases de prueba (número de pruebas) |
|---|---|
| `anuncios` | AnuncioTest (7), AnunciosWebTest (10), ImpresionesYDestinosWebTest (6) |
| `api` | FootballDataClienteTest (2), SincronizacionPlantillasTest (7), SincronizacionServiceTest (19), SincronizacionSinClaveTest (1) |
| `apuesta` | ApuestaWebTest (10), CancelacionWebTest (3), CashOutYBloqueoLigaWebTest (19), CombinadaWebTest (11), ConcurrenciaSaldoTest (4), CorreoPremioWebTest (6), CorreoResumenTest (3), CorreoTrasGuardarTest (2), EliminarCuentaWebTest (6), EstadisticasCompeticionWebTest (1), EstadisticasTest (2), ExportacionWebTest (3), JuegoResponsableWebTest (10), LargoPlazoWebTest (15), LimitesWebTest (7), LogrosWebTest (2), ModificarImporteWebTest (8), MovimientosSaldoWebTest (6), NotificacionesWebTest (6), OtrasApuestasWebTest (9), PerfilJugadorWebTest (4), QuintaDepuracionApuestasTest (4), RankingPorJuegoWebTest (4), ResolucionWebTest (11), ResumenSemanalTest (3), RevisionFallosWebTest (23), RiesgoWebTest (5), SupercuotaWebTest (15) |
| `apuestas` | ApuestasApplicationTests (2), InvariantesDineroTest (2) |
| `casino` | CasinoWebTest (26), ManoBaccaratTest (6), ManoBlackjackTest (10), RuletaTest (5) |
| `config` | ActuatorWebTest (4), CabecerasSeguridadWebTest (4), ContrasenasPruebaTest (1), DatosDemoTest (4), DatosInicialesTest (8), IdPeticionTest (3), LimitePeticionesTest (6), PasswordCreadorFijaTest (1), PasswordUsuarioFijaTest (1) |
| `cripto` | CoinGeckoClienteTest (2), CriptoWebTest (20), PosicionCriptoTest (4) |
| `cuotas` | CalculadoraCuotasTest (19), EspecialTest (5), FormaYVolumenWebTest (4) |
| `equipos` | AlineacionTest (5), EquipoTest (3), EquiposWebTest (4), PlantillaWebTest (8) |
| `eventos` | BusquedaWebTest (5), CatalogoWebTest (5), EventoTest (15), FichaEquipoWebTest (10) |
| `fantasy` | FantasyConcurrenteTest (1), FantasyWebTest (13) |
| `gestion` | BeneficioDiarioWebTest (1), CasaWebTest (2), EditarEventoWebTest (11), GestionWebTest (15) |
| `ligas` | BloqueoConcurrenteTest (1), LigasSinTransaccionTest (1), LigasWebTest (10), RetoSinTransaccionTest (1), RetoWebTest (15) |
| `prediccion` | LmsrTest (6), PrediccionWebTest (16) |
| `quiniela` | QuinielaWebTest (25) |
| `seguimiento` | EvolucionCuotaWebTest (2), SeguimientoWebTest (15) |
| `usuarios` | CuentaWebTest (9), IntentosLoginWebTest (3), MovimientoSaldoTest (5), NombresProhibidosTest (5), PerfilYMovimientosWebTest (2), RankingWebTest (3), RecargaAlEntrarHandlerTest (1), RecuperacionWebTest (9), RegistroConcurrenteTest (1), SesionesWebTest (1), UsuarioTest (14), UsuariosWebTest (9) |
| `web` | AccesibilidadCssTest (5), AccesibilidadWebTest (8), CompresionTest (2), ConcurrenciaAdviceTest (2), ConsultasWebTest (6), CsvTest (4), FormatoTest (13), KeepAliveTest (3), MonedasTest (3), PingSinBaseDeDatosTest (1), PlantillasSinScriptsTest (1), RendimientoWebTest (2), TemaWebTest (7) |

**Total, a 22/11: 700 pruebas en 105 clases**, todas en verde con H2 y con PostgreSQL, contando `ApuestasApplicationTests` (2), que comprueba que la aplicación arranca, e `InvariantesDineroTest` (2), que hace cientos de operaciones al azar (apostar, corregir, anular, retos, quinielas, bajas…) y comprueba después de cada una que el dinero cuadra. Esta es la cifra de referencia: el resto de la documentación enlaza aquí.

## Matriz de trazabilidad
[`docs/requisitos/trazabilidad.md`](https://github.com/riverri/IS1/blob/main/docs/requisitos/trazabilidad.md) enlaza cada criterio de aceptación (**CA-xx.y**) con la prueba que lo comprueba.

| Historias | Criterios | Automática | Manual | Sin prueba |
|---|---:|---:|---:|---:|
| Hechas (45) | 97 | 93 (96 %) | 4 | 0 |
| Integradas (56) | 161 | 153 (95 %) | 0 | 6 (y 2 sin hacer, de HU-107) |

Manuales (dependen del navegador): la web en el móvil, la ganancia potencial al escribir el importe y la confirmación al eliminar la cuenta.

## Cobertura

`./mvnw verify` mide qué parte del código recorren las pruebas con [JaCoCo](https://www.jacoco.org/) y deja el informe en `target/site/jacoco/index.html`, con el detalle de cada paquete, clase y línea. En la CI, el resumen sale en la página de cada ejecución del trabajo **build** y el informe completo se descarga como artefacto `cobertura-jacoco`.

A 22/11: **93,9 % de las líneas, 82,0 % de las ramas** (cada `if` cuenta dos: cuando se cumple y cuando no) y 94,5 % de las instrucciones.

`./mvnw verify` falla si baja del **90 % de las líneas** o del **75 % de las ramas**, o si SpotBugs encuentra un error de prioridad alta ([decisión 0004](https://github.com/riverri/IS1/blob/main/docs/decisiones/0004-umbrales-de-calidad-en-la-ci.md)).

## Pruebas de mutación

Que una línea esté cubierta no quiere decir que se compruebe lo que hace. [PIT](https://pitest.org/) cambia el código a propósito (un `>` por un `>=`, un `+` por un `-`, quita una llamada…) y vuelve a pasar las pruebas: si alguna falla, el cambio está «detectado». Se aplica a la lógica con pruebas unitarias rápidas: cuotas, eventos, equipos, usuarios, estadísticas, formato, CSV, el LMSR de las predicciones, la ruleta, el blackjack y el baccarat, anuncios, cripto, el límite de envíos y el identificador de petición.

```
./mvnw -Pmutacion test-compile org.pitest:pitest-maven:mutationCoverage -Djacoco.skip=true
```

El informe queda en `target/pit-reports/index.html`. A 21/11 se detecta el **82,6 %** de 603 cambios (al empezar, el 41 % de 317), y la CI exige al menos el 80 %.

## Número de consultas

[`ConsultasWebTest`](https://github.com/riverri/IS1/blob/main/src/test/java/es/ucm/fdi/is1/apuestas/web/ConsultasWebTest.java) pide el catálogo, *Mis apuestas*, el ranking, el panel del creador y *Mis movimientos* con pocas filas y con 10 más, y cuenta las consultas a la base de datos. Tienen que ser las mismas, como mucho una más por lote de 50. Si una página hace una consulta por cada fila (el problema «N+1»), con 500 filas haría 500 consultas, y esta prueba lo detecta antes. Al escribirla, *Mis apuestas* pasaba de 26 a 76 consultas con 10 apuestas más; ahora hace 14 siempre.

## Integración continua
`.github/workflows/ci.yml` se ejecuta en cada pull request y en cada cambio de `main`:
- **build**: compila y pasa todas las pruebas con H2, mide la cobertura y pasa SpotBugs y la comprobación de estilo (Checkstyle).
- **postgresql**: arranca PostgreSQL 17 y pasa las mismas pruebas, para comprobar las migraciones de PostgreSQL.
- **mutacion**: pasa las pruebas de mutación (ver arriba) y deja el informe como artefacto `mutacion-pit`.
- **accesibilidad**: arranca la aplicación con los datos de demostración y la recorre con un navegador ([`a11y/comprobar.js`](https://github.com/riverri/IS1/blob/main/a11y/comprobar.js)). Pasa las reglas de WCAG 2.1 A y AA de axe a las páginas principales, con colores claros y oscuros; hace una apuesta solo con el teclado y comprueba que el foco siempre se ve; y mira que no haya desplazamiento lateral a 320 px ni con la página ampliada al 200 % (HU-97 a HU-101).

  Después, [`a11y/e2e.js`](https://github.com/riverri/IS1/blob/main/a11y/e2e.js) hace el recorrido de principio a fin:
  - registrarse, apostar, cancelar, una combinada, el creador pone el resultado y se cobra;
  - el creador da de alta un partido con supercuota, se apuesta con ella y al anularlo se devuelve;
  - una liga entre dos jugadores con un bloqueo;
  - una quiniela anulada;
  - recuperar la contraseña con el enlace del registro;
  - cerrar sesión.

Si alguno falla, el PR sale en rojo y no se fusiona.

Aparte, el trabajo **Vigilancia** pide cada hora `/actuator/health` a la web publicada y abre un issue si no responde.
