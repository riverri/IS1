# Pruebas y calidad

## Las pruebas automáticas
Se ejecutan todas con `./mvnw test` (Windows: `mvnw.cmd test`).

- **Unitarias:** una clase sola, sin base de datos ni web. Ejemplo: `CalculadoraCuotasTest` comprueba que la buena forma baja la cuota y que la suma de 1/cuota es mayor que 1.
- **Web** (`...WebTest`): arrancan la aplicación con una base de datos de prueba y simulan peticiones con **MockMvc**. Ejemplo: Ana inicia sesión, apuesta 10 y su saldo baja 10.

La mayoría llevan `@Transactional`: lo que hacen se deshace al terminar. La hora se fija con un reloj de prueba o la hora de Madrid (clase `Hora`).

| Paquete | Clases de prueba (número de pruebas) |
|---|---|
| `api` | FootballDataClienteTest (2), SincronizacionServiceTest (13), SincronizacionPlantillasTest (5) |
| `apuesta` | ApuestaWebTest (10), CancelacionWebTest (3), CombinadaWebTest (11), ConcurrenciaSaldoTest (1), EliminarCuentaWebTest (5), EstadisticasTest (2), JuegoResponsableWebTest (9), LargoPlazoWebTest (12), LimitesWebTest (6), ModificarImporteWebTest (6), NotificacionesWebTest (5), OtrasApuestasWebTest (9), PerfilJugadorWebTest (4), ResolucionWebTest (10), RevisionFallosWebTest (23) |
| `config` | ContrasenasPruebaTest (1), DatosInicialesTest (7), PasswordCreadorFijaTest (1) |
| `cuotas` | CalculadoraCuotasTest (12), EspecialTest (5), FormaYVolumenWebTest (4) |
| `equipos` | AlineacionTest (5), EquipoTest (3), EquiposWebTest (4), PlantillaWebTest (8) |
| `eventos` | BusquedaWebTest (5), CatalogoWebTest (4), EventoTest (2), FichaEquipoWebTest (9) |
| `gestion` | CasaWebTest (2), EditarEventoWebTest (8), GestionWebTest (15) |
| `ligas` | LigasWebTest (7), LigasSinTransaccionTest (1) |
| `usuarios` | CuentaWebTest (6), RankingWebTest (2), SesionesWebTest (1), UsuarioTest (4), UsuariosWebTest (7) |
| `web` | FormatoTest (5) |

**Total: 266 pruebas en 43 clases.**

## Matriz de trazabilidad
[`docs/requisitos/trazabilidad.md`](https://github.com/riverri/IS1/blob/main/docs/requisitos/trazabilidad.md) enlaza cada criterio de aceptación (**CA-xx.y**) con la prueba que lo comprueba.

| Historias hechas | Criterios | Automática | Manual | Sin prueba |
|---:|---:|---:|---:|---:|
| 44 | 92 | 88 (96 %) | 3 | 1 |

Manuales (dependen del navegador): la web en el móvil, la ganancia potencial al escribir el importe y la confirmación al eliminar la cuenta. Sin prueba: el mensaje de "no hay enfrentamientos previos".

## Integración continua
`.github/workflows/ci.yml` se ejecuta en cada pull request y en cada cambio de `main`:
- **build**: compila y pasa todas las pruebas con H2.
- **postgresql**: arranca PostgreSQL 17 y pasa las mismas pruebas, para comprobar las migraciones de PostgreSQL.

Si alguno falla, el PR sale en rojo y no se fusiona.
