# Paquetes y clases

El punto de entrada es `ApuestasApplication.java`, con el `main` que arranca Spring Boot. El código está en `src/main/java/es/ucm/fdi/is1/apuestas/`, repartido en 12 paquetes.

| Paquete | Qué contiene |
|---|---|
| `usuarios` | Cuentas, roles, registro, inicio de sesión, saldo y recargas, límites personales, sesiones |
| `equipos` | Competiciones, equipos, jugadores, plantillas y alineación probable |
| `eventos` | Partidos, catálogo, búsqueda, ficha de equipo, cara a cara y gráfico de evolución |
| `cuotas` | El algoritmo de cuotas y los tipos de resultado |
| `apuesta` | Apuestas y selecciones, boleto, resolución, límites de la casa, ranking, estadísticas, cuentas de la casa y baja. Es el paquete central |
| `mercados` | Mercados a largo plazo y sus candidatos |
| `ligas` | Ligas privadas con código de invitación |
| `notificaciones` | Avisos cuando se resuelve una apuesta |
| `gestion` | Panel del creador: formularios y controladores de alta y edición |
| `api` | Conexión con football-data.org y sincronización |
| `config` | Seguridad, reloj y datos iniciales |
| `web` | Piezas comunes: inicio, formato, colores de escudos, avisos de concurrencia |

**Dependencias:** `apuesta` usa `eventos`, `cuotas`, `mercados`, `usuarios` y `notificaciones`; `ligas` usa `apuesta`. Se evitan los ciclos: cuando un usuario se da de baja, `usuarios` no llama a `ligas`, sino que publica un evento (`CuentaEliminada`) que `ligas` escucha.

## Tipos de clase

| Sufijo o anotación | Qué es |
|---|---|
| `@Entity` | Una tabla de la base de datos ([Modelo de datos](Modelo-de-datos)) |
| `...Repository` | Interfaz para leer y guardar una entidad. Spring escribe el código a partir del nombre de los métodos (`findByEmail`) o de una `@Query` |
| `...Service` | Reglas del negocio y transacciones |
| `...Controller` | Atiende las peticiones de unas direcciones |
| `...Form` | Los campos de un formulario con sus validaciones (`@NotBlank`, `@Size`…) |
| `...Exception` | Un error del negocio con nombre (saldo insuficiente, evento no disponible…). Las de "no encontrado" dan 404 |
| `record` | Clase solo de datos para pasar información a las plantillas |
| `enum` | Lista cerrada de valores: estados, deportes, roles |

## usuarios
| Clase | Qué hace |
|---|---|
| `Usuario` (entidad) | Email, nombre, contraseña cifrada, rol, saldo, última recarga, límites, pausa, si está eliminado y versión (bloqueo optimista). Métodos `cargar`, `abonar`, `ajustar` y la recarga |
| `Rol` | `USUARIO` (jugador) o `CREADOR` |
| `UsuarioService` | Registro, recarga gratuita, nombre y contraseña, límites, pausas y anonimizar al dar de baja |
| `UsuariosController` | `/registro`, `/login` y `/cuenta` |
| `UsuarioDetailsService` | Le dice a Spring Security cómo buscar un usuario por email |
| `RecargaAlEntrarHandler` | Aplica la recarga al iniciar sesión |
| `IntentosLogin` | Bloqueo de 15 minutos tras 5 contraseñas incorrectas |
| `Sesiones` | Cierra las sesiones de otros navegadores al cambiar la contraseña o darse de baja |
| `Contrasenas` | Comprueba el límite de 72 bytes de BCrypt |
| `CuentaEliminada` | Evento que se publica al dar de baja una cuenta |
| `RecuperacionService`, `RecuperacionController`, `TokenRecuperacion` | Recuperar la contraseña con un enlace que caduca y sirve una vez |
| `EnvioCorreo`, `CorreoRecuperacion` | Envía el enlace por correo, o lo escribe en la consola si no hay servidor de correo |

## equipos
| Clase | Qué hace |
|---|---|
| `Competicion`, `Equipo`, `Jugador` | Entidades. Un equipo tiene deporte, **calidad** (0–10), **forma**, escudo y competiciones. Un jugador tiene posición, dorsal, nacionalidad y **nota** (6,0 al empezar) |
| `Deporte`, `Forma`, `Posicion` | Enumeraciones. La forma va de −2 ("muy mala racha") a +2 |
| `EquiposService` | Listado por deporte y competición, con buscador |
| `PlantillaService` | Añadir, puntuar y quitar jugadores |
| `Alineacion` | Alineación probable en 4-3-3 con los de mejor nota |

## eventos
| Clase | Qué hace |
|---|---|
| `Evento` (entidad) | Partido: competición, local, visitante, fecha, estado, resultado, goles, fase. Decide si admite apuestas |
| `EstadoEvento` | `PROGRAMADO`, `SUSPENDIDO`, `FINALIZADO`, `ANULADO` |
| `CatalogoService`, `EventosController` | Catálogo, búsqueda y página del partido |
| `FichaEquipoService` | Últimos 10 resultados, racha de 5, balance, clasificación y próximos 5 partidos |
| `CaraACara`, `GraficoEvolucion`, `FichaEquipo` | Datos de esas páginas; el gráfico se dibuja en SVG |

## cuotas
| Clase | Qué hace |
|---|---|
| `CalculadoraCuotas` | El [Algoritmo de cuotas](Algoritmo-de-cuotas) |
| `Cuotas`, `Resultado`, `Especial` | Las tres cuotas; `LOCAL`/`EMPATE`/`VISITANTE`; los otros mercados de fútbol |

## apuesta
| Clase | Qué hace |
|---|---|
| `Apuesta` (entidad) | Usuario, importe, cuota total, estado, lo ya pagado y fecha, con sus selecciones. Sabe resolverse, cancelarse y cambiar de importe |
| `Seleccion` (entidad) | Un pronóstico con la cuota del momento: evento + resultado, evento + mercado especial, o candidato |
| `ApuestaService`, `ApuestaController` | Apostar, cancelar, cambiar importe, historial, estadísticas |
| `Boleto`, `BoletoService`, `BoletoController` | El boleto (vive en la sesión) y la confirmación de la combinada |
| `ResolucionService`, `ResolucionMercadosService` | Resultados, suspender, reactivar, anular y dinero apostado |
| `Limites`, `LimitesService` | Límites de la casa: mínimo 1, máximo 500, 10 selecciones; interruptor del juego responsable |
| `VolumenApuestas` | Dinero apostado por resultado para el ajuste de cuotas (máximo 100 por jugador) |
| `RankingService`, `Estadisticas` | Ranking y estadísticas personales |
| `CuentasCasaService` | Cuentas de la casa |
| `AvisosApuestas` | Crea un aviso cuando una apuesta cambia de estado |
| `BajaService`, `BajaController` | Eliminar la cuenta |

## mercados, ligas y notificaciones
| Clase | Qué hace |
|---|---|
| `Mercado`, `Candidato`, `MercadoService` | Mercados a largo plazo: `ABIERTO`, `CERRADO`, `RESUELTO`, `ANULADO` |
| `Liga`, `LigaService`, `LigasController` | Ligas con código de 6 caracteres, ranking propio, salir y borrar |
| `Notificacion`, `NotificacionService` | Avisos y campana de la cabecera |

## gestion, api, config y web
| Clase | Qué hace |
|---|---|
| `GestionController`, `GestionService` | Panel `/gestion`: alta y edición de competiciones, equipos y eventos |
| `GestionMercadosController`, `CasaController` | Mercados y cuentas de la casa |
| `FootballDataCliente`, `FuenteDatosDeportivos` | Llamadas a la API (la interfaz permite cambiarla en las pruebas) |
| `SincronizacionService`, `SincronizacionPlantillas`, `SincronizacionProgramada` | Sincronización de partidos (cada 30 min) y plantillas (diaria) |
| `TiemposDeEsperaConfig` | 5 s para conectar y 20 s para responder |
| `SeguridadConfig` | Qué páginas son públicas y cuáles exigen sesión o rol |
| `RelojConfig` | Reloj en hora de Madrid |
| `DatosIniciales` | Usuarios de prueba, competiciones, equipos, partidos y mercados de ejemplo (solo en una base de datos vacía) |
| `UsuarioActualAdvice` | Usuario conectado, saldo, boleto y avisos en todas las páginas |
| `ConcurrenciaAdvice` | Aviso cuando dos operaciones chocan sobre el mismo saldo |
