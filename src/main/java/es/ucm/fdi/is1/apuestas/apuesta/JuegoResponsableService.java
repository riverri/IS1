package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/**
 * Juego responsable (HU-10): con una pausa activa no se puede apostar, y lo apostado en las últimas 24 horas o
 * 7 días más el nuevo importe no puede superar los límites que se ha puesto el usuario. No se comprueba nada si
 * el creador lo ha desactivado en Gestión.
 *
 * <p>Estaba dentro de {@link ApuestaService}; está aparte para que lo usen también los demás juegos de la web
 * que cobran del saldo (la ampliación del casino y de los mercados de predicción), con las mismas reglas.
 * No abre transacción propia: se llama desde la del servicio que va a cobrar.
 */
@Service
public class JuegoResponsableService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");

    private final ApuestaRepository apuestas;
    private final MovimientoSaldoRepository movimientos;
    private final LimitesService limites;

    public JuegoResponsableService(ApuestaRepository apuestas, MovimientoSaldoRepository movimientos,
                                   LimitesService limites) {
        this.apuestas = apuestas;
        this.movimientos = movimientos;
        this.limites = limites;
    }

    /**
     * Comprueba si el usuario puede jugar ahora {@code importe} moneditas.
     *
     * @throws JuegoResponsableException si está en pausa o superaría su límite diario o semanal
     */
    public void comprobar(Usuario usuario, BigDecimal importe, LocalDateTime ahora) {
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
        BigDecimal apostado = apostadoDesde(usuario, desde);
        if (apostado.add(importe).compareTo(limite) > 0) {
            throw new JuegoResponsableException("Superarías tu límite " + nombre + " de " + monedas(limite)
                    + " monedas: llevas " + monedas(apostado) + " apostadas en los últimos " + periodo + ".");
        }
    }

    /**
     * Lo jugado desde una fecha: las apuestas deportivas (también las ya resueltas; las canceladas no, ver
     * {@link ApuestaRepository#apostadoDesde}) más lo jugado en los otros juegos, que sale del libro de movimientos
     * ({@link TipoMovimiento#deJuego()}: el casino y los que vengan después).
     */
    BigDecimal apostadoDesde(Usuario usuario, LocalDateTime desde) {
        return apuestas.apostadoDesde(usuario, desde)
                .add(movimientos.jugadoDesde(usuario, desde, TipoMovimiento.deJuego()));
    }

    private static String monedas(BigDecimal cantidad) {
        return cantidad.setScale(2, RoundingMode.DOWN).toPlainString().replace('.', ',');
    }
}
