package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

/**
 * Cobrar una apuesta antes de tiempo (cash-out, HU-75). Lo que vale ahora es
 * importe × cuotas ya acertadas × (cuota apostada / cuota actual) de cada selección pendiente, sin pasar de lo que
 * cobraría acertando y redondeado hacia abajo (CA-75.1). La cuota actual no cuenta el dinero del propio jugador:
 * recién hecha, la apuesta vale lo apostado.
 */
@Service
public class CashOutService {

    private final ApuestaRepository apuestas;
    private final UsuarioRepository usuarios;
    private final CalculadoraCuotas calculadora;
    private final Clock reloj;

    public CashOutService(ApuestaRepository apuestas, UsuarioRepository usuarios, CalculadoraCuotas calculadora,
                          Clock reloj) {
        this.apuestas = apuestas;
        this.usuarios = usuarios;
        this.calculadora = calculadora;
        this.reloj = reloj;
    }

    /** Lo que se ofrece por cada apuesta activa del usuario que se puede cobrar ya (id → valor). */
    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> ofertas(String email) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        Map<Long, BigDecimal> ofertas = new LinkedHashMap<>();
        apuestas.findByUsuarioAndEstadoOrderByFechaDesc(usuario, EstadoApuesta.ACTIVA)
                .forEach(a -> valor(a, ahora).ifPresent(v -> ofertas.put(a.getId(), v)));
        return ofertas;
    }

    /**
     * Cobra la apuesta (CA-75.3).
     *
     * @param valorVisto lo que se le enseñó; si ha cambiado no se cobra (CA-75.4)
     * @throws ApuestaNoEncontradaException si no es suya
     * @throws IllegalStateException        si ya no se puede cobrar (CA-75.2)
     * @throws CashOutCambiadoException     si el valor no es el que vio
     */
    @Transactional
    public BigDecimal cobrar(String email, Long apuestaId, BigDecimal valorVisto) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Apuesta apuesta = apuestas.findById(apuestaId).filter(a -> a.getUsuario().getEmail().equals(email))
                .orElseThrow(() -> new ApuestaNoEncontradaException(apuestaId));
        BigDecimal valor = valor(apuesta, ahora)
                .orElseThrow(() -> new IllegalStateException("Esta apuesta ya no se puede cobrar: algún partido ha empezado o está suspendido"));
        if (valorVisto == null || valorVisto.compareTo(valor) != 0) {
            throw new CashOutCambiadoException(valor);
        }
        apuesta.cobrar(valor, ahora);
        return valor;
    }

    /** Vacío si no se puede cobrar: no está activa, o alguna selección pendiente ya no admite apuestas (CA-75.2). */
    Optional<BigDecimal> valor(Apuesta apuesta, LocalDateTime ahora) {
        if (apuesta.getEstado() != EstadoApuesta.ACTIVA) {
            return Optional.empty();
        }
        BigDecimal valor = apuesta.getImporte();
        Long usuario = apuesta.getUsuario().getId();
        for (Seleccion s : apuesta.getSelecciones()) {
            switch (s.getEstado()) {
                case ACERTADA -> valor = valor.multiply(s.getCuota());
                case ANULADA -> {
                    // cuenta con cuota 1,00
                }
                case FALLADA -> {
                    return Optional.empty();
                }
                case PENDIENTE -> {
                    // Las de largo plazo no se cobran antes: su cuota la fija el creador
                    if (s.isLargoPlazo() || s.getEvento().getEstado() != EstadoEvento.PROGRAMADO
                            || !s.getEvento().admiteApuestas(ahora)) {
                        return Optional.empty();
                    }
                    BigDecimal actual = s.getEspecial() != null
                            ? calculadora.especiales(s.getEvento()).get(s.getEspecial())
                            : calculadora.calcularSin(s.getEvento(), usuario).de(s.getPronostico());
                    if (actual == null) {
                        return Optional.empty();
                    }
                    valor = valor.multiply(s.getCuota()).divide(actual, MathContext.DECIMAL64);
                }
            }
        }
        return Optional.of(valor.min(apuesta.getGananciaPotencial()).setScale(2, RoundingMode.DOWN));
    }
}
