package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
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
import es.ucm.fdi.is1.apuestas.web.Monedas;

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
    /** Nombre en la tabla tarea_realizada. */
    static final String TAREA = "resumen-semanal";

    private final UsuarioRepository usuarios;
    private final MovimientoSaldoRepository movimientos;
    private final EnvioResumen envio;
    private final TareaRealizadaRepository tareas;
    private final Clock reloj;
    private final String urlPublica;

    public ResumenSemanalService(UsuarioRepository usuarios, MovimientoSaldoRepository movimientos, EnvioResumen envio,
                                 TareaRealizadaRepository tareas, Clock reloj,
                                 @Value("${apuestas.url-publica}") String urlPublica) {
        this.usuarios = usuarios;
        this.movimientos = movimientos;
        this.envio = envio;
        this.tareas = tareas;
        this.reloj = reloj;
        this.urlPublica = urlPublica;
    }

    /** Lo de la semana de un jugador. */
    public record Semana(BigDecimal jugado, BigDecimal cobrado, BigDecimal saldo, int puesto, int jugadores) {

        public boolean isConActividad() {
            return jugado.signum() > 0 || cobrado.signum() > 0;
        }
    }

    /**
     * Toca mandarlo cada lunes a las 9:00 (hora de Madrid). Se mira cada hora si ya se ha mandado el de esta semana:
     * si la aplicación estaba parada o reiniciándose justo a las 9:00 (Render la vuelve a publicar con cada cambio),
     * se manda en cuanto vuelve, en vez de perder esa semana. La última vez queda en la tabla tarea_realizada.
     *
     * <p>La primera vez que arranca con esta tabla no manda nada: solo apunta el último lunes, porque el de esta
     * semana puede haberlo mandado ya la versión anterior.
     *
     * @return a cuántos se ha mandado (0 si aún no tocaba)
     */
    @Scheduled(cron = "${apuestas.resumen.cron:0 7 * * * *}", zone = "Europe/Madrid")
    @Transactional
    public int enviarSiToca() {
        LocalDateTime tocaba = ultimoLunes(LocalDateTime.now(reloj));
        TareaRealizada tarea = tareas.findById(TAREA).orElse(null);
        if (tarea == null) {
            tareas.save(new TareaRealizada(TAREA, tocaba));
            return 0;
        }
        if (!tarea.getUltima().isBefore(tocaba)) {
            return 0;
        }
        // Se apunta antes de mandar: si a mitad falla un correo, no se vuelve a mandar a todos dentro de una hora
        tarea.marcar(tocaba);
        return enviar();
    }

    /** El último lunes a las 9:00 que ya ha pasado (el de esta semana o, si aún no son las 9 del lunes, el anterior). */
    static LocalDateTime ultimoLunes(LocalDateTime ahora) {
        LocalDateTime lunes = ahora.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atTime(9, 0);
        return lunes.isAfter(ahora) ? lunes.minusWeeks(1) : lunes;
    }

    /** Manda el resumen de los últimos 7 días a quien le toque. @return a cuántos se ha mandado */
    @Transactional(readOnly = true)
    public int enviar() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime desde = ahora.minusDays(7);
        // Mismo orden que el ranking (RankingService): saldo, nombre e id, ordenado en Java. El de la base de datos
        // no vale: en PostgreSQL «bea» va antes que «Carla», y con el mismo nombre no hay orden fijo
        List<Usuario> ranking = usuarios.findByRolOrderBySaldoDescNombreAsc(Rol.USUARIO).stream()
                .filter(u -> !u.isEliminado())
                .sorted(Comparator.comparing(Usuario::getSaldo).reversed().thenComparing(Usuario::getNombre)
                        .thenComparing(Usuario::getId))
                .toList();
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
                + "- Has jugado " + Monedas.texto(s.jugado()) + " monedas.\n"
                + "- Has cobrado " + Monedas.texto(s.cobrado()) + " monedas (" + (balance.signum() >= 0 ? "+" : "")
                + Monedas.texto(balance) + " de balance).\n"
                + "- Tu saldo es de " + Monedas.texto(s.saldo()) + " monedas.\n"
                + "- Vas " + s.puesto() + "º de " + s.jugadores() + " en el ranking.\n\n"
                + "Tus apuestas: " + urlPublica.replaceAll("/+$", "") + "/apuestas";
    }

}
