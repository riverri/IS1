package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
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
