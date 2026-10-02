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
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoNoDisponibleException;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.mercados.Candidato;
import es.ucm.fdi.is1.apuestas.mercados.CandidatoRepository;
import es.ucm.fdi.is1.apuestas.mercados.MercadoNoDisponibleException;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

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
     */
    @Transactional
    public Apuesta apostar(String email, List<SeleccionPedida> pedidas, BigDecimal importe) {
        if (pedidas.isEmpty()) {
            throw new IllegalArgumentException("El boleto está vacío");
        }
        Limites limitesActuales = limites.actuales();
        limitesActuales.comprobarSelecciones(pedidas.size());
        limitesActuales.comprobarImporte(importe);
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        Apuesta apuesta = new Apuesta(usuario, importe, ahora);
        List<Long> cambiadas = new ArrayList<>();
        for (SeleccionPedida pedida : pedidas) {
            Evento evento = eventos.findById(pedida.eventoId())
                    .filter(e -> e.admiteApuestas(ahora))
                    .orElseThrow(() -> new EventoNoDisponibleException(pedida.eventoId()));
            BigDecimal cuota = calculadora.calcular(evento).de(pedida.resultado());
            if (cuota == null) {
                throw new ResultadoNoValidoException(pedida.resultado());
            }
            if (pedida.cuotaVista() != null && pedida.cuotaVista().compareTo(cuota) != 0) {
                cambiadas.add(evento.getId());
            }
            apuesta.anadir(evento, pedida.resultado(), cuota);
        }
        if (!cambiadas.isEmpty()) {
            throw new CuotasCambiadasException(cambiadas);
        }
        comprobarJuegoResponsable(usuario, importe, ahora);
        usuario.cargar(importe);
        return apuestas.save(apuesta);
    }

    /**
     * Apuesta a largo plazo (HU-44): al candidato de un mercado abierto, con la cuota que ha fijado el creador.
     * Si el usuario vio otra cuota (el creador la ha cambiado mientras tanto), no se apuesta.
     */
    @Transactional
    public Apuesta apostarMercado(String email, Long mercadoId, Long candidatoId, BigDecimal cuotaVista,
                                  BigDecimal importe) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Candidato candidato = candidatos.findById(candidatoId)
                .filter(c -> c.getMercado().getId().equals(mercadoId))
                .filter(c -> c.getMercado().admiteApuestas(ahora))
                .orElseThrow(() -> new MercadoNoDisponibleException(mercadoId));
        limites.actuales().comprobarImporte(importe);
        if (cuotaVista != null && cuotaVista.compareTo(candidato.getCuota()) != 0) {
            throw new CuotasCambiadasException(List.of());
        }
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
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
     */
    @Transactional
    public Apuesta cancelar(String email, Long apuestaId) {
        Apuesta apuesta = apuestas.findById(apuestaId)
                .filter(a -> a.getUsuario().getEmail().equals(email))
                .orElseThrow(() -> new ApuestaNoEncontradaException(apuestaId));
        apuesta.cancelar(LocalDateTime.now(reloj));
        return apuesta;
    }

    /**
     * Cambia el importe de una apuesta activa del usuario antes de que empiece el evento (HU-27).
     * Se aplican los límites de apuesta y las cuotas actuales, y se cobra o devuelve la diferencia.
     */
    @Transactional
    public Apuesta modificarImporte(String email, Long apuestaId, BigDecimal nuevo) {
        Apuesta apuesta = apuestas.findById(apuestaId)
                .filter(a -> a.getUsuario().getEmail().equals(email))
                .orElseThrow(() -> new ApuestaNoEncontradaException(apuestaId));
        limites.actuales().comprobarImporte(nuevo);
        LocalDateTime ahora = LocalDateTime.now(reloj);
        BigDecimal aumento = nuevo.subtract(apuesta.getImporte());
        if (aumento.signum() > 0) {
            comprobarJuegoResponsable(apuesta.getUsuario(), aumento, ahora);
        }
        List<BigDecimal> cuotas = apuesta.getSelecciones().stream().map(this::cuotaActual).toList();
        apuesta.cambiarImporte(nuevo, cuotas, ahora);
        return apuesta;
    }

    /**
     * Juego responsable (HU-10): con una pausa activa no se puede apostar, y lo apostado en las últimas
     * 24 horas o 7 días más el nuevo importe no puede superar los límites que se ha puesto el usuario.
     */
    private void comprobarJuegoResponsable(Usuario usuario, BigDecimal importe, LocalDateTime ahora) {
        if (usuario.enPausa(ahora)) {
            throw new JuegoResponsableException("Has pausado tus apuestas hasta el "
                    + FECHA.format(usuario.getPausaHasta()) + ".");
        }
        comprobarLimite(usuario, usuario.getLimiteDiario(), ahora.minusDays(1), importe, "diario", "24 horas");
        comprobarLimite(usuario, usuario.getLimiteSemanal(), ahora.minusDays(7), importe, "semanal", "7 días");
    }

    private void comprobarLimite(Usuario usuario, BigDecimal limite, LocalDateTime desde, BigDecimal importe,
                                 String nombre, String periodo) {
        if (limite == null) {
            return;
        }
        BigDecimal apostado = apuestas.apostadoDesde(usuario, desde);
        if (apostado.add(importe).compareTo(limite) > 0) {
            throw new JuegoResponsableException("Superarías tu límite " + nombre + " de " + monedas(limite)
                    + " monedas: llevas " + monedas(apostado) + " apostadas en los últimos " + periodo + ".");
        }
    }

    private static String monedas(BigDecimal cantidad) {
        return cantidad.setScale(2, RoundingMode.DOWN).toPlainString().replace('.', ',');
    }

    private BigDecimal cuotaActual(Seleccion seleccion) {
        if (seleccion.isLargoPlazo()) {
            return seleccion.getCandidato().getCuota();
        }
        return calculadora.calcular(seleccion.getEvento()).de(seleccion.getPronostico());
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
