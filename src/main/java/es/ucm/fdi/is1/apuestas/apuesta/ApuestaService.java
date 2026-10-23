package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoNoDisponibleException;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.mercados.Candidato;
import es.ucm.fdi.is1.apuestas.mercados.CandidatoRepository;
import es.ucm.fdi.is1.apuestas.mercados.MercadoNoDisponibleException;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

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

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");

    private final ApuestaRepository apuestas;
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    private final CandidatoRepository candidatos;
    private final CalculadoraCuotas calculadora;
    private final LimitesService limites;
    private final Clock reloj;

    public ApuestaService(ApuestaRepository apuestas, EventoRepository eventos, UsuarioRepository usuarios,
                          CandidatoRepository candidatos, CalculadoraCuotas calculadora, LimitesService limites,
                          Clock reloj) {
        this.apuestas = apuestas;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.candidatos = candidatos;
        this.calculadora = calculadora;
        this.limites = limites;
        this.reloj = reloj;
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
            Evento evento = eventos.findById(pedida.eventoId())
                    .filter(e -> e.admiteApuestas(ahora))
                    .orElseThrow(() -> new EventoNoDisponibleException(pedida.eventoId()));
            // La cuota se calcula aquí, nunca se fía de la que envía la página
            BigDecimal cuota = cuota(evento, pedida.resultado(), pedida.especial());
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
        usuario.cargar(importe);
        // Las selecciones se guardan con la apuesta (cascade); el usuario, al confirmar la transacción
        return apuestas.save(apuesta);
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
        usuario.cargar(importe);
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
        Apuesta apuesta = apuestas.findById(apuestaId)
                .filter(a -> a.getUsuario().getEmail().equals(email))
                .orElseThrow(() -> new ApuestaNoEncontradaException(apuestaId));
        // No hace falta save: la apuesta está gestionada por JPA y los cambios se guardan al terminar la transacción
        apuesta.cancelar(LocalDateTime.now(reloj));
        return apuesta;
    }

    /**
     * Cambia el importe de una apuesta activa del usuario antes de que empiece el evento (HU-27).
     * Se aplican los límites de apuesta y las cuotas actuales, y se cobra o devuelve la diferencia.
     *
     * @throws ApuestaNoEncontradaException si no existe o es de otro usuario
     * @throws IllegalStateException        si alguno de sus eventos ya ha empezado
     * @throws IllegalArgumentException     si el nuevo importe o la nueva cuota total no respetan los límites
     * @throws es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException si sube el importe y no hay saldo
     */
    @Transactional
    public Apuesta modificarImporte(String email, Long apuestaId, BigDecimal nuevo) {
        Apuesta apuesta = apuestas.findById(apuestaId)
                .filter(a -> a.getUsuario().getEmail().equals(email))
                .orElseThrow(() -> new ApuestaNoEncontradaException(apuestaId));
        limites.actuales().comprobarImporte(nuevo);
        LocalDateTime ahora = LocalDateTime.now(reloj);
        // Para el juego responsable solo cuenta lo que se añade; bajar el importe siempre se permite
        BigDecimal aumento = nuevo.subtract(apuesta.getImporte());
        if (aumento.signum() > 0) {
            comprobarJuegoResponsable(apuesta.getUsuario(), aumento, ahora);
        }
        // Cuotas vigentes, en el mismo orden que las selecciones (Apuesta.cambiarImporte cuenta con ello)
        List<BigDecimal> cuotas = apuesta.getSelecciones().stream().map(this::cuotaActual).toList();
        comprobarCuotaTotal(Apuesta.producto(cuotas));
        apuesta.cambiarImporte(nuevo, cuotas, ahora);
        return apuesta;
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

    /**
     * Juego responsable (HU-10): con una pausa activa no se puede apostar, y lo apostado en las últimas
     * 24 horas o 7 días más el nuevo importe no puede superar los límites que se ha puesto el usuario.
     * No se comprueba nada si el creador lo ha desactivado en Gestión.
     */
    private void comprobarJuegoResponsable(Usuario usuario, BigDecimal importe, LocalDateTime ahora) {
        if (!limites.actuales().isJuegoResponsable()) {
            return;
        }
        if (usuario.enPausa(ahora)) {
            throw new JuegoResponsableException("Has pausado tus apuestas hasta el "
                    + FECHA.format(usuario.getPausaHasta()) + ".");
        }
        // Ventanas móviles (las últimas 24 h y los últimos 7 días), no el día o la semana del calendario
        comprobarLimite(usuario, usuario.getLimiteDiario(), ahora.minusDays(1), importe, "diario", "24 horas");
        comprobarLimite(usuario, usuario.getLimiteSemanal(), ahora.minusDays(7), importe, "semanal", "7 días");
    }

    /** Lanza JuegoResponsableException si lo apostado desde {@code desde} más {@code importe} supera el límite. */
    private void comprobarLimite(Usuario usuario, BigDecimal limite, LocalDateTime desde, BigDecimal importe,
                                 String nombre, String periodo) {
        // Sin límite fijado (null): no hay nada que comprobar
        if (limite == null) {
            return;
        }
        // Cuentan también las apuestas ya resueltas; las canceladas no (ApuestaRepository.apostadoDesde)
        BigDecimal apostado = apuestas.apostadoDesde(usuario, desde);
        if (apostado.add(importe).compareTo(limite) > 0) {
            throw new JuegoResponsableException("Superarías tu límite " + nombre + " de " + monedas(limite)
                    + " monedas: llevas " + monedas(apostado) + " apostadas en los últimos " + periodo + ".");
        }
    }

    private static String monedas(BigDecimal cantidad) {
        return cantidad.setScale(2, RoundingMode.DOWN).toPlainString().replace('.', ',');
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
                EstadoApuesta.PERDIDA, EstadoApuesta.ANULADA, EstadoApuesta.CANCELADA));
    }

    /** Porcentaje de aciertos y rentabilidad del usuario (HU-35). */
    @Transactional(readOnly = true)
    public Estadisticas estadisticas(String email) {
        return Estadisticas.de(historial(email));
    }

    /** Apuestas activas del usuario, de la más reciente a la más antigua (HU-24). */
    @Transactional(readOnly = true)
    public List<Apuesta> activas(String email) {
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        return apuestas.findByUsuarioAndEstadoOrderByFechaDesc(usuario, EstadoApuesta.ACTIVA);
    }
}
