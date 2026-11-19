# Pruebas y calidad

## Las pruebas automáticas
Se ejecutan todas con `./mvnw test` (Windows: `mvnw.cmd test`).

- **Unitarias:** una clase sola, sin base de datos ni web. Ejemplo: `CalculadoraCuotasTest` comprueba que la buena forma baja la cuota y que la suma de 1/cuota es mayor que 1.
- **Web** (`...WebTest`): arrancan la aplicación con una base de datos de prueba y simulan peticiones con **MockMvc**. Ejemplo: Ana inicia sesión, apuesta 10 y su saldo baja 10.

La mayoría llevan `@Transactional`: lo que hacen se deshace al terminar. La hora se fija con un reloj de prueba o la hora de Madrid (clase `Hora`).

| Paquete | Clases de prueba (número de pruebas) |
|---|---|
| `api` | FootballDataClienteTest (2), SincronizacionPlantillasTest (5), SincronizacionServiceTest (14), SincronizacionSinClaveTest (1) |
| `apuesta` | ApuestaWebTest (10), CancelacionWebTest (3), CombinadaWebTest (11), ConcurrenciaSaldoTest (2), CorreoPremioWebTest (5), CorreoResumenTest (3), CorreoTrasGuardarTest (2), EliminarCuentaWebTest (5), EstadisticasCompeticionWebTest (1), EstadisticasTest (2), ExportacionWebTest (3), JuegoResponsableWebTest (10), LargoPlazoWebTest (15), LimitesWebTest (7), LogrosWebTest (2), ModificarImporteWebTest (8), MovimientosSaldoWebTest (4), NotificacionesWebTest (5), OtrasApuestasWebTest (9), PerfilJugadorWebTest (4), RankingPorJuegoWebTest (4), ResolucionWebTest (11), ResumenSemanalTest (1), RevisionFallosWebTest (23), RiesgoWebTest (5), SupercuotaWebTest (11) |
| `config` | ContrasenasPruebaTest (1), DatosDemoTest (4), DatosInicialesTest (7), PasswordCreadorFijaTest (1) |
| `cripto` | CoinGeckoClienteTest (2), CriptoWebTest (20), PosicionCriptoTest (4) |
| `cuotas` | CalculadoraCuotasTest (12), EspecialTest (5), FormaYVolumenWebTest (4) |
| `equipos` | AlineacionTest (5), EquipoTest (3), EquiposWebTest (4), PlantillaWebTest (8) |
| `eventos` | BusquedaWebTest (5), CatalogoWebTest (5), EventoTest (2), FichaEquipoWebTest (10) |
| `gestion` | BeneficioDiarioWebTest (1), CasaWebTest (2), EditarEventoWebTest (10), GestionWebTest (15) |
| `ligas` | LigasSinTransaccionTest (1), LigasWebTest (7), RetoSinTransaccionTest (1), RetoWebTest (13) |
| `prediccion` | LmsrTest (6), PrediccionWebTest (16) |
| `quiniela` | QuinielaWebTest (15) |
| `seguimiento` | EvolucionCuotaWebTest (2), SeguimientoWebTest (11) |
| `usuarios` | CuentaWebTest (8), IntentosLoginWebTest (3), MovimientoSaldoTest (5), NombresProhibidosTest (5), RankingWebTest (3), RecuperacionWebTest (9), RegistroConcurrenteTest (1), SesionesWebTest (1), UsuarioTest (4), UsuariosWebTest (8) |
| `web` | AccesibilidadCssTest (5), AccesibilidadWebTest (7), CompresionTest (2), ConcurrenciaAdviceTest (2), FormatoTest (6), KeepAliveTest (3), RendimientoWebTest (2), TemaWebTest (5) |

**Total: 471 pruebas en 80 clases**, contando `ApuestasApplicationTests` (2), que comprueba que la aplicación arranca.

## Matriz de trazabilidad
[`docs/requisitos/trazabilidad.md`](https://github.com/riverri/IS1/blob/main/docs/requisitos/trazabilidad.md) enlaza cada criterio de aceptación (**CA-xx.y**) con la prueba que lo comprueba.

| Historias hechas | Criterios | Automática | Manual | Sin prueba |
|---:|---:|---:|---:|---:|
| 45 | 97 | 93 (96 %) | 4 | 0 |

Manuales (dependen del navegador): la web en el móvil, la ganancia potencial al escribir el importe y la confirmación al eliminar la cuenta.

## Cobertura

`./mvnw verify` mide qué parte del código recorren las pruebas con [JaCoCo](https://www.jacoco.org/) y deja el informe en `target/site/jacoco/index.html`, con el detalle de cada paquete, clase y línea. En la CI, el resumen sale en la página de cada ejecución del trabajo **build** y el informe completo se descarga como artefacto `cobertura-jacoco`.

A 19/11: **93,8 % de las líneas, 77,5 % de las ramas** (cada `if` cuenta dos: cuando se cumple y cuando no) y 94,3 % de las instrucciones.

## Integración continua
`.github/workflows/ci.yml` se ejecuta en cada pull request y en cada cambio de `main`:
- **build**: compila y pasa todas las pruebas con H2, y mide la cobertura.
- **postgresql**: arranca PostgreSQL 17 y pasa las mismas pruebas, para comprobar las migraciones de PostgreSQL.
- **accesibilidad**: arranca la aplicación con los datos de demostración y la recorre con un navegador ([`a11y/comprobar.js`](https://github.com/riverri/IS1/blob/main/a11y/comprobar.js)). Pasa las reglas de WCAG 2.1 A y AA de axe a las páginas principales, con colores claros y oscuros; hace una apuesta solo con el teclado y comprueba que el foco siempre se ve; y mira que no haya desplazamiento lateral a 320 px ni con la página ampliada al 200 % (HU-97 a HU-101).

Si alguno falla, el PR sale en rojo y no se fusiona.
