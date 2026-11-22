package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.RegistroCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoNoDisponibleException;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.mercados.Candidato;
import es.ucm.fdi.is1.apuestas.mercados.CandidatoRepository;
import es.ucm.fdi.is1.apuestas.mercados.MercadoNoDisponibleException;
import es.ucm.fdi.is1.apuestas.mercados.MercadoRepository;
import es.ucm.fdi.is1.apuestas.ligas.BloqueoLigaService;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.web.Monedas;

/**
 * Servicio de apuestas del jugador: apostar (simple, combinada o a largo plazo), cancelar, cambiar el importe
 * y consultar las activas, el historial y las estadísticas (HU-10, HU-23, HU-24, HU-26, HU-27, HU-28, HU-29,
 * HU-34, HU-35, HU-44, HU-52).
 *
 * <p>Aquí se comprueban las reglas antes de tocar el saldo: eventos disponibles, límites del creador (HU-07),
 * juego responsable (HU-10) y cuotas vistas. Cada método público es una transacción: si algo falla no se cobra
 * nada. Si dos operaciones cambian el saldo del mismo usuario a la vez, el {@code @Version} de
 * {@link Usuario} hace fallar la segunda al confirmar la transacción.
 */
@Service
public class ApuestaService {

    private final ApuestaRepository apuestas;
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    private final CandidatoRepository candidatos;
    private final MercadoRepository mercados;
    private final CalculadoraCuotas calculadora;
    private final LimitesService limites;
    private final JuegoResponsableService juegoResponsable;
    private final RegistroCuotas registroCuotas;
    private final SupercuotaRepository supercuotas;
    private final Clock reloj;
    private final BloqueoLigaService bloqueosLiga;
    private final SeleccionRepository selecciones;
    private final VolumenApuestas volumen;

    public ApuestaService(ApuestaRepository apuestas, EventoRepository eventos, UsuarioRepository usuarios,
                          CandidatoRepository candidatos, MercadoRepository mercados, CalculadoraCuotas calculadora,
                          LimitesService limites,
                          JuegoResponsableService juegoResponsable, RegistroCuotas registroCuotas,
                          SupercuotaRepository supercuotas, Clock reloj, BloqueoLigaService bloqueosLiga,
                          SeleccionRepository selecciones, VolumenApuestas volumen) {
        this.apuestas = apuestas;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.candidatos = candidatos;
        this.mercados = mercados;
        this.calculadora = calculadora;
        this.limites = limites;
        this.juegoResponsable = juegoResponsable;
        this.registroCuotas = registroCuotas;
        this.supercuotas = supercuotas;
        this.reloj = reloj;
        this.bloqueosLiga = bloqueosLiga;
        this.selecciones = selecciones;
        this.volumen = volumen;
    }

    /** Apuesta simple (HU-23): una combinada de una sola selección, con la cuota vigente. */
    @Transactional
    public Apuesta apostar(String email, Long eventoId, Resultado resultado, BigDecimal importe) {
        return apostar(email, List.of(new SeleccionPedida(eventoId, resultado, null)), importe);
    }

    /**
     * Registra una apuesta simple o combinada (HU-23, HU-28, HU-29): comprueba que los eventos admiten
     * apuestas, que no se repiten, los límites de apuesta (HU-07) y el saldo, y descuenta el importe.
     * Las cuotas se calculan en el servidor. Si el usuario vio una cuota distinta de la actual, no se apuesta
     * y se le pide que acepte las nuevas (HU-29).
     *
     * @throws EventoNoDisponibleException si algún evento no existe o ya no admite apuestas
     * @throws ResultadoNoValidoException  si algún pronóstico no se puede apostar en su evento
     * @throws CuotasCambiadasException    si alguna cuota vista no coincide con la actual
     * @throws ImporteFueraDeLimitesException si el importe no respeta los límites del creador
     * @throws JuegoResponsableException   si el usuario está en pausa o supera sus límites personales
     * @throws es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException si no tiene saldo
     * @throws IllegalArgumentException    si el boleto está vacío, tiene demasiadas selecciones, repite evento,
     *                                     la cuota total pasa del máximo o el usuario es el creador
     */
    @Transactional
    public Apuesta apostar(String email, List<SeleccionPedida> pedidas, BigDecimal importe) {
        // Pasos: (1) límites del creador, (2) cada selección: evento disponible y cuota actual,
        // (3) cuotas cambiadas, cuota total y juego responsable, (4) cobrar y guardar.
        // Cualquier excepción por el camino deshace la transacción: no se cobra ni se guarda nada
        if (pedidas.isEmpty()) {
            throw new IllegalArgumentException("El boleto está vacío");
        }
        Limites limitesActuales = limites.actuales();
        limitesActuales.comprobarSelecciones(pedidas.size());
        limitesActuales.comprobarImporte(importe);
        // Una sola hora para toda la operación: todas las selecciones se comprueban con el mismo "ahora"
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        comprobarQueJuega(usuario);
        Apuesta apuesta = new Apuesta(usuario, importe, ahora);
        List<Long> cambiadas = new ArrayList<>();
        for (SeleccionPedida pedida : pedidas) {
            // Si el evento no existe o no admite apuestas (filter deja el Optional vacío), se lanza la excepción
            // Con la fila del evento bloqueada: si a la vez se anula o se resuelve, uno espera al otro
            Evento evento = eventos.paraApostar(pedida.eventoId())
                    .filter(e -> e.admiteApuestas(ahora))
                    .orElseThrow(() -> new EventoNoDisponibleException(pedida.eventoId()));
            bloqueosLiga.comprobar(usuario, evento, pedida.resultado(), pedida.especial());
            // La cuota se calcula aquí, nunca se fía de la que envía la página
            BigDecimal cuota = cuota(evento, pedida.resultado(), pedida.especial());
            // HU-87: en una simple a un resultado con supercuota vale la supercuota, hasta su importe máximo
            if (pedidas.size() == 1 && pedida.especial() == null) {
                Supercuota supercuota = supercuotaVigente(evento, pedida.resultado(), ahora);
                if (supercuota != null) {
                    if (importe.compareTo(supercuota.getImporteMaximo()) > 0) {
                        throw new ImporteFueraDeLimitesException("Con la supercuota se pueden apostar como mucho "
                                + Monedas.texto(supercuota.getImporteMaximo()) + " monedas");
                    }
                    cuota = supercuota.getCuota();
                }
            }
            // Se apuntan todas las cambiadas antes de avisar, para enseñarlas todas de una vez
            if (pedida.cuotaVista() != null && pedida.cuotaVista().compareTo(cuota) != 0) {
                cambiadas.add(evento.getId());
            }
            if (pedida.especial() != null) {
                apuesta.anadir(evento, pedida.especial(), cuota);
            } else {
                apuesta.anadir(evento, pedida.resultado(), cuota);
            }
        }
        if (!cambiadas.isEmpty()) {
            throw new CuotasCambiadasException(cambiadas);
        }
        comprobarCuotaTotal(apuesta.getCuota());
        comprobarJuegoResponsable(usuario, importe, ahora);
        // Se cobra tras todas las comprobaciones; sin saldo lanza SaldoInsuficienteException y no se guarda nada (CA-23.2)
        usuario.cargar(importe, TipoMovimiento.APUESTA, "Nueva " + apuesta.descripcion(), ahora);
        // Las selecciones se guardan con la apuesta (cascade); el usuario y su movimiento, al confirmar la transacción
        Apuesta guardada = apuestas.save(apuesta);
        registrarCuotas(guardada);
        return guardada;
    }

    /**
     * Apuesta a largo plazo (HU-44): al candidato de un mercado abierto, con la cuota que ha fijado el creador.
     * Si el usuario vio otra cuota (el creador la ha cambiado mientras tanto), no se apuesta.
     *
     * @throws MercadoNoDisponibleException si el candidato no es de ese mercado o el mercado no admite apuestas
     * @throws CuotasCambiadasException     si la cuota vista no es la actual (con la lista de eventos vacía)
     */
    @Transactional
    public Apuesta apostarMercado(String email, Long mercadoId, Long candidatoId, BigDecimal cuotaVista,
                                  BigDecimal importe) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        // Primero el mercado, con su fila bloqueada: si a la vez se resuelve o se anula, uno espera al otro
        if (mercados.paraApostar(mercadoId).isEmpty()) {
            throw new MercadoNoDisponibleException(mercadoId);
        }
        // El candidato tiene que ser del mercado de la URL y ese mercado tiene que seguir abierto
        Candidato candidato = candidatos.findById(candidatoId)
                .filter(c -> c.getMercado().getId().equals(mercadoId))
                .filter(c -> c.getMercado().admiteApuestas(ahora))
                .orElseThrow(() -> new MercadoNoDisponibleException(mercadoId));
        limites.actuales().comprobarImporte(importe);
        if (cuotaVista != null && cuotaVista.compareTo(candidato.getCuota()) != 0) {
            throw new CuotasCambiadasException(List.of());
        }
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        comprobarQueJuega(usuario);
        comprobarJuegoResponsable(usuario, importe, ahora);
        Apuesta apuesta = new Apuesta(usuario, importe, ahora);
        apuesta.anadir(candidato, candidato.getCuota());
        usuario.cargar(importe, TipoMovimiento.APUESTA, "Nueva " + apuesta.descripcion(), ahora);
        return apuestas.save(apuesta);
    }

    /**
     * Cancela una apuesta activa del usuario antes de que empiece el evento (o, a largo plazo, mientras el
     * mercado siga abierto) y le devuelve el importe (HU-26).
     * Si la apuesta no es suya se trata como inexistente.
     *
     * @throws ApuestaNoEncontradaException si no existe o es de otro usuario
     * @throws IllegalStateException        si ya no se puede cancelar
     */
    @Transactional
    public Apuesta cancelar(String email, Long apuestaId) {
        Apuesta apuesta = apuestas.conSelecciones(apuestaId)
                .filter(a -> a.getUsuario().getEmail().equals(email))
                .orElseThrow(() -> new ApuestaNoEncontradaException(apuestaId));
        // No hace falta save: la apuesta está gestionada por JPA y los cambios se guardan al terminar la transacción
        apuesta.cancelar(LocalDateTime.now(reloj));
        registrarCuotas(apuesta);
        return apuesta;
    }

    /** Igual que {@link #modificarImporte(String, Long, BigDecimal, BigDecimal)} sin comprobar la cuota vista. */
    @Transactional
    public Apuesta modificarImporte(String email, Long apuestaId, BigDecimal nuevo) {
        return modificarImporte(email, apuestaId, nuevo, null);
    }

    /**
     * Cambia el importe de una apuesta activa del usuario antes de que empiece el evento (HU-27).
     * Se aplican los límites de apuesta y las cuotas actuales, y se cobra o devuelve la diferencia.
     * Si el usuario vio otra cuota total (las cuotas se han movido desde que apostó), no se cambia nada
     * y se le enseña la nueva para que la acepte, como en HU-29.
     *
     * @param cuotaVista cuota total que vio el usuario; null para no comprobarla
     * @throws ApuestaNoEncontradaException si no existe o es de otro usuario
     * @throws IllegalStateException        si ya no se puede modificar; el mensaje dice por qué
     * @throws CuotaApuestaCambiadaException si la cuota total actual no es la que vio el usuario
     * @throws IllegalArgumentException     si el nuevo importe o la nueva cuota total no respetan los límites
     * @throws es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException si sube el importe y no hay saldo
     */
    @Transactional
    public Apuesta modificarImporte(String email, Long apuestaId, BigDecimal nuevo, BigDecimal cuotaVista) {
        Apuesta apuesta = apuestas.conSelecciones(apuestaId)
                .filter(a -> a.getUsuario().getEmail().equals(email))
                .orElseThrow(() -> new ApuestaNoEncontradaException(apuestaId));
        LocalDateTime ahora = LocalDateTime.now(reloj);
        // Primero si aún se puede tocar: si el partido ha empezado no tiene sentido hablar de cuotas
        if (!apuesta.cancelable(ahora)) {
            throw new IllegalStateException(apuesta.motivoNoCancelable(ahora));
        }
        limites.actuales().comprobarImporte(nuevo);
        // Para el juego responsable solo cuenta lo que se añade; bajar el importe siempre se permite
        BigDecimal aumento = nuevo.subtract(apuesta.getImporte());
        if (aumento.signum() > 0) {
            comprobarJuegoResponsable(apuesta.getUsuario(), aumento, ahora);
            // HU-105: con un bloqueo de liga no se puede apostar más a eso; la apuesta que ya había se queda como estaba
            for (Seleccion seleccion : apuesta.getSelecciones()) {
                if (seleccion.getEvento() != null) {
                    bloqueosLiga.comprobar(apuesta.getUsuario(), seleccion.getEvento(), seleccion.getPronostico(),
                            seleccion.getEspecial());
                }
            }
        }
        // Cuotas vigentes, en el mismo orden que las selecciones (Apuesta.cambiarImporte cuenta con ello)
        List<BigDecimal> cuotas = apuesta.getSelecciones().stream().map(this::cuotaActual).toList();
        // HU-87: una simple a un resultado con supercuota vigente sigue con la supercuota, y con su importe máximo
        Seleccion unica = apuesta.getSelecciones().size() == 1 ? apuesta.getSelecciones().get(0) : null;
        Supercuota supercuota = unica == null || unica.isLargoPlazo() || unica.getEspecial() != null ? null
                : supercuotaVigente(unica.getEvento(), unica.getPronostico(), ahora);
        if (supercuota != null) {
            if (nuevo.compareTo(supercuota.getImporteMaximo()) > 0) {
                throw new ImporteFueraDeLimitesException("Con la supercuota se pueden apostar como mucho "
                        + Monedas.texto(supercuota.getImporteMaximo()) + " monedas");
            }
            cuotas = List.of(supercuota.getCuota());
        }
        BigDecimal cuotaTotal = Apuesta.producto(cuotas);
        // HU-29 también aquí: no se aplica en silencio una cuota distinta de la que ha visto
        if (cuotaVista != null && cuotaVista.compareTo(cuotaTotal) != 0) {
            throw new CuotaApuestaCambiadaException(cuotaTotal);
        }
        comprobarCuotaTotal(cuotaTotal);
        apuesta.cambiarImporte(nuevo, cuotas, ahora);
        registrarCuotas(apuesta);
        return apuesta;
    }

    /**
     * Supercuota vigente de ese resultado (HU-87), o null si no hay. Solo vale en apuestas simples; el boleto
     * también la usa para enseñar la misma cuota que se va a aplicar al confirmar.
     */
    Supercuota supercuotaVigente(Evento evento, Resultado resultado, LocalDateTime ahora) {
        if (resultado == null) {
            return null;
        }
        // Si la cuota normal ha subido hasta igualarla o pasarla, la "mejora" ya no mejora: vale la normal
        return supercuotas.findByEventoAndHastaAfter(evento, ahora).stream()
                .filter(s -> s.isVigente(ahora) && s.getResultado() == resultado)
                .filter(s -> s.getCuota().compareTo(calculadora.cuota(evento, resultado, null)) > 0)
                .findFirst().orElse(null);
    }

    /** Las cuotas 1X2 se mueven con el dinero apostado: se apuntan para su evolución (HU-91). */
    private void registrarCuotas(Apuesta apuesta) {
        apuesta.getSelecciones().stream().filter(s -> s.getEvento() != null && s.getPronostico() != null)
                .map(Seleccion::getEvento).distinct().forEach(registroCuotas::registrar);
    }

    /** El creador de apuestas introduce los resultados: no puede apostar a ellos. */
    private static void comprobarQueJuega(Usuario usuario) {
        if (usuario.isCreador()) {
            throw new IllegalArgumentException("El creador de apuestas no puede apostar");
        }
    }

    /** Lanza IllegalArgumentException si la cuota total pasa de {@link Apuesta#CUOTA_TOTAL_MAXIMA}. */
    static void comprobarCuotaTotal(BigDecimal cuota) {
        if (cuota.compareTo(Apuesta.CUOTA_TOTAL_MAXIMA) > 0) {
            throw new IllegalArgumentException("La cuota total no puede pasar de 1.000: quita alguna selección");
        }
    }

    /** Juego responsable (HU-10): las reglas están en {@link JuegoResponsableService}. */
    private void comprobarJuegoResponsable(Usuario usuario, BigDecimal importe, LocalDateTime ahora) {
        juegoResponsable.comprobar(usuario, importe, ahora);
    }

    /** Cuota vigente de una selección ya apostada: la del candidato o la que calcula ahora la calculadora. */
    private BigDecimal cuotaActual(Seleccion seleccion) {
        if (seleccion.isLargoPlazo()) {
            return seleccion.getCandidato().getCuota();
        }
        return calculadora.cuota(seleccion.getEvento(), seleccion.getPronostico(), seleccion.getEspecial());
    }

    /**
     * Cuota actual de un resultado o de un tipo especial (HU-52); exactamente uno de los dos.
     *
     * @throws ResultadoNoValidoException si vienen los dos, ninguno, o no hay cuota para eso en el evento
     */
    BigDecimal cuota(Evento evento, Resultado resultado, Especial especial) {
        // Los dos null o los dos rellenos: no es un pronóstico válido
        BigDecimal cuota = (resultado == null) == (especial == null) ? null
                : calculadora.cuota(evento, resultado, especial);
        if (cuota == null) {
            throw ResultadoNoValidoException.de(resultado, especial);
        }
        return cuota;
    }

    /** Historial: apuestas ya resueltas, anuladas o canceladas, de la más reciente a la más antigua (HU-34). */
    @Transactional(readOnly = true)
    public List<Apuesta> historial(String email) {
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        return apuestas.findByUsuarioAndEstadoInOrderByFechaDesc(usuario, EnumSet.of(EstadoApuesta.GANADA,
                EstadoApuesta.PERDIDA, EstadoApuesta.ANULADA, EstadoApuesta.CANCELADA, EstadoApuesta.COBRADA));
    }

    /** Una pantalla acotada del historial; la descarga CSV conserva el método {@link #historial(String)} completo. */
    @Transactional(readOnly = true)
    public PaginaApuestas paginaHistorial(String email, int numero) {
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        int pagina = Math.min(Math.max(0, numero), 10000);
        Slice<Apuesta> resultado = apuestas.findByUsuarioAndEstadoInOrderByFechaDescIdDesc(usuario,
                EnumSet.of(EstadoApuesta.GANADA, EstadoApuesta.PERDIDA, EstadoApuesta.ANULADA,
                        EstadoApuesta.CANCELADA, EstadoApuesta.COBRADA), PageRequest.of(pagina, 20));
        precalentarSelecciones(resultado.getContent());
        return new PaginaApuestas(resultado.getContent(), pagina, pagina > 0, resultado.hasNext());
    }

    /** Una pantalla acotada de activas; las ofertas de cash out solo se calculan para las que se muestran. */
    @Transactional(readOnly = true)
    public PaginaApuestas paginaActivas(String email, int numero) {
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        int pagina = Math.min(Math.max(0, numero), 10000);
        Slice<Apuesta> resultado = apuestas.findByUsuarioAndEstadoOrderByFechaDescIdDesc(usuario,
                EstadoApuesta.ACTIVA, PageRequest.of(pagina, 20));
        precalentarSelecciones(resultado.getContent());
        return new PaginaApuestas(resultado.getContent(), pagina, pagina > 0, resultado.hasNext());
    }

    public record PaginaApuestas(List<Apuesta> apuestas, int numero, boolean anterior, boolean siguiente) {
    }

    /** Inicializa las colecciones de una página en lotes dentro de la transacción que las devuelve a la vista. */
    private static void precalentarSelecciones(List<Apuesta> pagina) {
        pagina.forEach(apuesta -> apuesta.getSelecciones().size());
    }

    /** Total comprometido sin cargar la lista de apuestas activas. */
    @Transactional(readOnly = true)
    public BigDecimal importeActivo(String email) {
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        return apuestas.importePorEstado(usuario, EstadoApuesta.ACTIVA);
    }

    @Transactional(readOnly = true)
    public long cuentaActivas(String email) {
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        return apuestas.countByUsuarioAndEstado(usuario, EstadoApuesta.ACTIVA);
    }

    /** Porcentaje de aciertos y rentabilidad del usuario (HU-35). */
    @Transactional(readOnly = true)
    public Estadisticas estadisticas(String email) {
        Long usuarioId = usuarios.findByEmail(email).orElseThrow().getId();
        Object[] resumen = apuestas.resumenResueltasDe(usuarioId).getFirst();
        return Estadisticas.de(((Number) resumen[0]).longValue(), ((Number) resumen[1]).longValue(),
                (BigDecimal) resumen[2], (BigDecimal) resumen[3]);
    }

    /** Calcula las estadísticas sobre el historial ya cargado por la página "Mis apuestas". */
    Estadisticas estadisticas(List<Apuesta> historial) {
        return Estadisticas.de(historial);
    }

    /** Estadísticas de un grupo de apuestas: una competición, las combinadas o las de largo plazo (HU-96). */
    public record EstadisticasGrupo(String nombre, Estadisticas estadisticas) {
    }

    /**
     * Mis estadísticas por competición (HU-96): las simples van con la competición de su partido, y las combinadas
     * (pueden mezclar competiciones) y las de largo plazo, cada una en su grupo. Solo los grupos con apuestas
     * resueltas, de más a menos.
     */
    @Transactional(readOnly = true)
    public List<EstadisticasGrupo> estadisticasPorCompeticion(String email) {
        Long usuarioId = usuarios.findByEmail(email).orElseThrow().getId();
        return apuestas.resumenPorGrupo(usuarioId).stream()
                .map(grupo -> new EstadisticasGrupo(grupo.getNombre(),
                        Estadisticas.de(grupo.getResueltas(), grupo.getGanadas(), grupo.getApostado(),
                                grupo.getPagado())))
                .toList();
    }

    /** Agrupa el historial ya cargado para evitar repetir la consulta al pintarlo junto a las estadísticas. */
    List<EstadisticasGrupo> estadisticasPorCompeticion(List<Apuesta> historial) {
        Map<String, List<Apuesta>> grupos = new LinkedHashMap<>();
        for (Apuesta apuesta : historial) {
            if (!apuesta.isResuelta() && apuesta.getEstado() != EstadoApuesta.COBRADA) {
                continue;
            }
            String grupo = apuesta.isLargoPlazo() ? "Largo plazo"
                    : apuesta.isCombinada() ? "Combinadas"
                    : apuesta.getSelecciones().get(0).getEvento().getCompeticion().getNombre();
            grupos.computeIfAbsent(grupo, g -> new ArrayList<>()).add(apuesta);
        }
        return grupos.entrySet().stream()
                .map(e -> new EstadisticasGrupo(e.getKey(), Estadisticas.de(e.getValue())))
                .sorted(Comparator.comparing((EstadisticasGrupo g) -> g.estadisticas().resueltas()).reversed()
                        .thenComparing(EstadisticasGrupo::nombre))
                .toList();
    }

    /** Apuestas activas del usuario, de la más reciente a la más antigua (HU-24). */
    @Transactional(readOnly = true)
    public List<Apuesta> activas(String email) {
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        return apuestas.findByUsuarioAndEstadoOrderByFechaDesc(usuario, EstadoApuesta.ACTIVA);
    }

    /** Ofertas de cobro anticipado (HU-75, HU-102) de las apuestas activas, simples o combinadas, que lo admiten. */
    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> ofertasCashOut(List<Apuesta> activas, LocalDateTime ahora) {
        Map<Long, BigDecimal> ofertas = new HashMap<>();
        if (activas.isEmpty()) {
            return ofertas;
        }
        // En lote: una consulta para el dinero apostado a todos los partidos y otra para sus supercuotas, en vez de
        // una por selección (ver ConsultasWebTest). Las apuestas de una lista son todas del mismo usuario
        List<Evento> partidos = activas.stream().flatMap(a -> a.getSelecciones().stream())
                .map(Seleccion::getEvento).filter(Objects::nonNull).distinct().toList();
        Long usuario = activas.getFirst().getUsuario().getId();
        Map<Long, Set<Long>> apuestasPorEvento = new HashMap<>();
        if (!partidos.isEmpty()) {
            for (Object[] fila : selecciones.apuestasDelUsuarioEnEventos(partidos.stream().map(Evento::getId).toList(),
                    usuario)) {
                apuestasPorEvento.computeIfAbsent((Long) fila[1], e -> new HashSet<>()).add((Long) fila[0]);
            }
        }
        Lote lote = new Lote(volumen.importes(partidos, usuario),
                partidos.isEmpty() ? List.of() : supercuotas.findByEventoIn(partidos), apuestasPorEvento);
        for (Apuesta apuesta : activas) {
            BigDecimal importe = ofertaCashOut(apuesta, ahora, false, lote);
            if (importe != null) {
                ofertas.put(apuesta.getId(), importe);
            }
        }
        return ofertas;
    }

    /** Confirma una oferta vigente. Los eventos se bloquean para que no empiecen mientras se abona. */
    @Transactional
    public Apuesta cobrarAnticipado(String email, Long apuestaId, BigDecimal ofertaVista) {
        Apuesta apuesta = apuestas.conSelecciones(apuestaId)
                .filter(a -> a.getUsuario().getEmail().equalsIgnoreCase(email))
                .orElseThrow(() -> new ApuestaNoEncontradaException(apuestaId));
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (apuesta.isLargoPlazo() || apuesta.getEstado() != EstadoApuesta.ACTIVA) {
            throw new IllegalStateException("Esta apuesta ya no admite un cobro anticipado");
        }
        BigDecimal nueva = ofertaCashOut(apuesta, ahora, true, null);
        if (nueva == null) {
            throw new IllegalStateException("Algún partido ya ha empezado, se ha suspendido o se ha anulado");
        }
        if (ofertaVista == null || ofertaVista.compareTo(nueva) != 0) {
            throw new OfertaCashOutCambiadaException(nueva);
        }
        apuesta.cobrarAnticipado(nueva, ahora);
        return apuesta;
    }

    /**
     * Si es una simple con supercuota (HU-87): hay o hubo una supercuota de ese partido y resultado con la misma
     * cuota que la aceptada. La apuesta no guarda si la llevaba, por eso se busca así. No se mira cuándo: al cambiar
     * el importe, una apuesta hecha antes pasa a la supercuota vigente (y su fecha no cambia).
     */
    private boolean conSupercuota(Apuesta apuesta, Lote lote) {
        if (apuesta.isCombinada()) {
            return false;
        }
        Seleccion unica = apuesta.getSelecciones().getFirst();
        if (unica.getEvento() == null || unica.getPronostico() == null || unica.getEspecial() != null) {
            return false;
        }
        List<Supercuota> candidatas = lote != null
                ? lote.supercuotas().stream().filter(s -> s.getEvento().getId().equals(unica.getEvento().getId())
                        && s.getResultado() == unica.getPronostico()).toList()
                : supercuotas.findByEventoAndResultado(unica.getEvento(), unica.getPronostico());
        return candidatas.stream().anyMatch(s -> s.getCuota().compareTo(unica.getCuota()) == 0);
    }

    /**
     * Datos consultados de una vez para las ofertas de una lista de apuestas: el dinero apostado a cada partido sin
     * contar el del propio usuario, las supercuotas de esos partidos y qué apuestas suyas hay en cada partido.
     */
    private record Lote(Map<Long, Map<Resultado, BigDecimal>> volumen, List<Supercuota> supercuotas,
                        Map<Long, Set<Long>> apuestasPorEvento) {

        /** Si en alguno de esos partidos hay otra apuesta del usuario que no sea esta. */
        boolean hayOtras(Set<Long> eventos, Long apuesta) {
            return eventos.stream().map(e -> apuestasPorEvento.getOrDefault(e, Set.of()))
                    .anyMatch(ids -> ids.stream().anyMatch(id -> !id.equals(apuesta)));
        }
    }

    /**
     * Recalcula la oferta; en confirmación también vuelve a comprobar y bloquea cada evento. Con {@code lote}
     * (listado de Mis apuestas) usa lo ya consultado; sin él (al cobrar una), consulta lo de esa apuesta.
     */
    private BigDecimal ofertaCashOut(Apuesta apuesta, LocalDateTime ahora, boolean bloquearEventos, Lote lote) {
        // CA-75.1: simples y combinadas. Una simple con supercuota no: su cuota aceptada es más alta que la normal
        // desde que se apuesta, y el cobro anticipado pagaría más de lo apostado nada más apostar
        if (apuesta.isLargoPlazo() || !apuesta.cancelable(ahora) || conSupercuota(apuesta, lote)) {
            return null;
        }
        Map<Long, Evento> eventosBloqueados = new HashMap<>();
        if (bloquearEventos) {
            for (Seleccion seleccion : apuesta.getSelecciones().stream()
                    .sorted(Comparator.comparing(s -> s.getEvento().getId())).toList()) {
                Evento bloqueado = eventos.paraCambiar(seleccion.getEvento().getId())
                        .filter(e -> e.admiteApuestas(ahora)).orElse(null);
                if (bloqueado == null) {
                    return null;
                }
                eventosBloqueados.put(bloqueado.getId(), bloqueado);
            }
        }
        BigDecimal factor = BigDecimal.ONE;
        for (Seleccion seleccion : apuesta.getSelecciones()) {
            Evento evento = bloquearEventos ? eventosBloqueados.get(seleccion.getEvento().getId())
                    : seleccion.getEvento();
            BigDecimal actual;
            if (lote == null) {
                actual = calculadora.cuota(evento, seleccion.getPronostico(), seleccion.getEspecial(),
                        apuesta.getUsuario().getId());
            } else if (seleccion.getEspecial() != null) {
                // Las especiales no se ajustan por volumen (como en CalculadoraCuotas.cuota)
                actual = calculadora.especiales(evento).get(seleccion.getEspecial());
            } else {
                actual = seleccion.getPronostico() == null ? null : calculadora.calcular(evento,
                        lote.volumen().getOrDefault(evento.getId(), Map.of())).de(seleccion.getPronostico());
            }
            if (actual == null || actual.signum() <= 0) {
                return null;
            }
            factor = factor.multiply(seleccion.getCuota().divide(actual, MathContext.DECIMAL64));
        }
        // Con otra apuesta propia en estos partidos (activa o cancelada) pudo inflar la cuota aceptada: sin ganancia
        Set<Long> idsEventos = apuesta.getSelecciones().stream().map(s -> s.getEvento().getId())
                .collect(Collectors.toSet());
        if (factor.compareTo(BigDecimal.ONE) > 0 && (lote != null ? lote.hayOtras(idsEventos, apuesta.getId())
                : selecciones.hayOtrasApuestasDelUsuario(idsEventos, apuesta.getUsuario().getId(), apuesta.getId()))) {
            factor = BigDecimal.ONE;
        }
        return apuesta.getImporte().multiply(factor).setScale(2, RoundingMode.DOWN)
                .min(apuesta.getGananciaPotencial());
    }
}
