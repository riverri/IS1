package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldo;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

/**
 * Resumen semanal por correo (HU-92): cada lunes, a quien ha jugado en los últimos 7 días y no lo ha desactivado, lo
 * que ha jugado, lo que ha cobrado, su saldo y su puesto en el ranking. Lo manda {@link EnvioResumen}.
 */
@Service
public class ResumenSemanalService {

    /** Lo que no es jugar: no cuenta ni para jugado ni para cobrado. */
    private static final Set<TipoMovimiento> AJENOS = EnumSet.of(TipoMovimiento.BIENVENIDA, TipoMovimiento.RECARGA,
            TipoMovimiento.SALDO_INICIAL, TipoMovimiento.BAJA);
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM");

    private final UsuarioRepository usuarios;
    private final MovimientoSaldoRepository movimientos;
    private final EnvioResumen envio;
    private final Clock reloj;
    private final String urlPublica;

    public ResumenSemanalService(UsuarioRepository usuarios, MovimientoSaldoRepository movimientos, EnvioResumen envio,
                                 Clock reloj, @Value("${apuestas.url-publica}") String urlPublica) {
        this.usuarios = usuarios;
        this.movimientos = movimientos;
        this.envio = envio;
        this.reloj = reloj;
        this.urlPublica = urlPublica;
    }

    /** Lo de la semana de un jugador. */
    public record Semana(BigDecimal jugado, BigDecimal cobrado, BigDecimal saldo, int puesto, int jugadores) {

        public boolean isConActividad() {
            return jugado.signum() > 0 || cobrado.signum() > 0;
        }
    }

    /** Cada lunes a las 9:00 (hora de Madrid). @return a cuántos se ha mandado */
    @Scheduled(cron = "${apuestas.resumen.cron:0 0 9 * * MON}", zone = "Europe/Madrid")
    @Transactional(readOnly = true)
    public int enviar() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime desde = ahora.minusDays(7);
        List<Usuario> ranking = usuarios.findByRolOrderBySaldoDescNombreAsc(Rol.USUARIO).stream()
                .filter(u -> !u.isEliminado()).toList();
        int enviados = 0;
        for (int i = 0; i < ranking.size(); i++) {
            Usuario usuario = ranking.get(i);
            if (!usuario.isResumenSemanal()) {
                continue;
            }
            Semana semana = semana(usuario, desde, i + 1, ranking.size());
            // Sin jugar esa semana no se manda: sería un correo vacío
            if (semana.isConActividad()) {
                envio.enviarResumen(usuario.getEmail(), usuario.getNombre(), texto(semana, desde, ahora));
                enviados++;
            }
        }
        return enviados;
    }

    Semana semana(Usuario usuario, LocalDateTime desde, int puesto, int jugadores) {
        BigDecimal jugado = BigDecimal.ZERO;
        BigDecimal cobrado = BigDecimal.ZERO;
        for (MovimientoSaldo m : movimientos.findByUsuarioAndFechaGreaterThanEqual(usuario, desde)) {
            if (AJENOS.contains(m.getTipo())) {
                continue;
            }
            if (m.getImporte().signum() < 0) {
                jugado = jugado.subtract(m.getImporte());
            } else {
                cobrado = cobrado.add(m.getImporte());
            }
        }
        return new Semana(jugado, cobrado, usuario.getSaldo(), puesto, jugadores);
    }

    private String texto(Semana s, LocalDateTime desde, LocalDateTime hasta) {
        BigDecimal balance = s.cobrado().subtract(s.jugado());
        return "Tu semana del " + desde.format(DIA) + " al " + hasta.format(DIA) + ":\n\n"
                + "- Has jugado " + monedas(s.jugado()) + " monedas.\n"
                + "- Has cobrado " + monedas(s.cobrado()) + " monedas (" + (balance.signum() >= 0 ? "+" : "")
                + monedas(balance) + " de balance).\n"
                + "- Tu saldo es de " + monedas(s.saldo()) + " monedas.\n"
                + "- Vas " + s.puesto() + "º de " + s.jugadores() + " en el ranking.\n\n"
                + "Tus apuestas: " + urlPublica.replaceAll("/+$", "") + "/apuestas";
    }

    private static String monedas(BigDecimal cantidad) {
        return new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-ES"))).format(cantidad);
    }
}
