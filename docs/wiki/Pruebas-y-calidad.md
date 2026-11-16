# Pruebas y calidad

## Las pruebas automáticas
Se ejecutan todas con `./mvnw test` (Windows: `mvnw.cmd test`).

- **Unitarias:** una clase sola, sin base de datos ni web. Ejemplo: `CalculadoraCuotasTest` comprueba que la buena forma baja la cuota y que la suma de 1/cuota es mayor que 1.
- **Web** (`...WebTest`): arrancan la aplicación con una base de datos de prueba y simulan peticiones con **MockMvc**. Ejemplo: Ana inicia sesión, apuesta 10 y su saldo baja 10.

La mayoría llevan `@Transactional`: lo que hacen se deshace al terminar. La hora se fija con un reloj de prueba o la hora de Madrid (clase `Hora`).

| Paquete | Clases de prueba (número de pruebas) |
|---|---|
| `api` | FootballDataClienteTest (2), SincronizacionPlantillasTest (5), SincronizacionServiceTest (13) |
| `apuesta` | ApuestaWebTest (10), CancelacionWebTest (3), CashOutWebTest (6), CombinadaWebTest (11), ConcurrenciaSaldoTest (1), CorreoPremioWebTest (5), CorreoTrasGuardarTest (2), EliminarCuentaWebTest (5), EstadisticasCompeticionWebTest (1), EstadisticasTest (2), ExportacionWebTest (3), JuegoResponsableWebTest (10), LargoPlazoWebTest (13), LimitesWebTest (7), LogrosWebTest (2), ModificarImporteWebTest (8), MovimientosSaldoWebTest (4), NotificacionesWebTest (5), OtrasApuestasWebTest (9), PerfilJugadorWebTest (4), RankingPorJuegoWebTest (4), ResolucionWebTest (11), ResumenSemanalTest (1), RevisionFallosWebTest (23), RiesgoWebTest (5), SupercuotaWebTest (4) |
| `config` | ContrasenasPruebaTest (1), DatosInicialesTest (7), PasswordCreadorFijaTest (1) |
| `cripto` | CoinGeckoClienteTest (2), CriptoWebTest (20), PosicionCriptoTest (4) |
| `cuotas` | CalculadoraCuotasTest (12), EspecialTest (5), FormaYVolumenWebTest (4) |
| `equipos` | AlineacionTest (5), EquipoTest (3), EquiposWebTest (4), PlantillaWebTest (8) |
| `eventos` | BusquedaWebTest (5), CatalogoWebTest (5), EventoTest (2), FichaEquipoWebTest (10) |
| `gestion` | BeneficioDiarioWebTest (1), CasaWebTest (2), EditarEventoWebTest (8), GestionWebTest (15) |
| `ligas` | LigasSinTransaccionTest (1), LigasWebTest (7), RetoWebTest (6) |
| `prediccion` | LmsrTest (6), PrediccionWebTest (16) |
| `quiniela` | QuinielaWebTest (14) |
| `seguimiento` | EvolucionCuotaWebTest (2), SeguimientoWebTest (7) |
| `usuarios` | CuentaWebTest (7), IntentosLoginWebTest (3), MovimientoSaldoTest (5), NombresProhibidosTest (5), RankingWebTest (2), RecuperacionWebTest (9), SesionesWebTest (1), UsuarioTest (4), UsuariosWebTest (8) |
| `web` | CompresionTest (2), ConcurrenciaAdviceTest (2), FormatoTest (5), KeepAliveTest (3), RendimientoWebTest (2), TemaWebTest (5) |

**Total: 427 pruebas en 75 clases**, contando `ApuestasApplicationTests` (2), que comprueba que la aplicación arranca.

## Matriz de trazabilidad
[`docs/requisitos/trazabilidad.md`](https://github.com/riverri/IS1/blob/main/docs/requisitos/trazabilidad.md) enlaza cada criterio de aceptación (**CA-xx.y**) con la prueba que lo comprueba.

| Historias hechas | Criterios | Automática | Manual | Sin prueba |
|---:|---:|---:|---:|---:|
| 45 | 97 | 93 (96 %) | 4 | 0 |

Manuales (dependen del navegador): la web en el móvil, la ganancia potencial al escribir el importe y la confirmación al eliminar la cuenta.

## Integración continua
`.github/workflows/ci.yml` se ejecuta en cada pull request y en cada cambio de `main`:
- **build**: compila y pasa todas las pruebas con H2.
- **postgresql**: arranca PostgreSQL 17 y pasa las mismas pruebas, para comprobar las migraciones de PostgreSQL.

Si alguno falla, el PR sale en rojo y no se fusiona.
