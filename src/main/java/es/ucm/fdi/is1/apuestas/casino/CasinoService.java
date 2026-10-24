package es.ucm.fdi.is1.apuestas.casino;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.random.RandomGenerator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.apuesta.JuegoResponsableService;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

/**
 * Casino (HU-56 a HU-59): ruleta europea y blackjack con moneditas.
 *
 * <p>Aquí está lo que comparten todos los juegos, en este orden: el casino está abierto, el creador no juega, el
 * importe está dentro de los límites del casino, el juego responsable lo permite (HU-10) y hay saldo. Entonces se
 * cobra, se juega con el azar del servidor y se paga, cada cosa con su movimiento de saldo (HU-55). Cada método es
 * una transacción: si algo falla, no se cobra nada.
 */
@Service
public class CasinoService {

    private final PartidaRepository partidas;
    private final ConfiguracionCasinoRepository configuraciones;
    private final UsuarioRepository usuarios;
    private final JuegoResponsableService juegoResponsable;
    private final RandomGenerator azar;
    private final Clock reloj;

    public CasinoService(PartidaRepository partidas, ConfiguracionCasinoRepository configuraciones,
                         UsuarioRepository usuarios, JuegoResponsableService juegoResponsable, RandomGenerator azar,
                         Clock reloj) {
        this.partidas = partidas;
        this.configuraciones = configuraciones;
        this.usuarios = usuarios;
        this.juegoResponsable = juegoResponsable;
        this.azar = azar;
        this.reloj = reloj;
    }

    /** @throws IllegalStateException si falta la fila (la base de datos no tiene la migración V13) */
    @Transactional(readOnly = true)
    public ConfiguracionCasino configuracion() {
        return configuraciones.findById(ConfiguracionCasino.ID)
                .orElseThrow(() -> new IllegalStateException("Falta la configuración del casino (migración V13)"));
    }

    /** El creador cambia la configuración (HU-59); ver {@link ConfiguracionCasino#cambiar}. */
    @Transactional
    public ConfiguracionCasino cambiarConfiguracion(boolean activo, BigDecimal minimo, BigDecimal maximo, int barajas,
                                                    boolean pideCon17Blando, PagoBlackjack pago) {
        ConfiguracionCasino configuracion = configuracion();
        configuracion.cambiar(activo, minimo, maximo, barajas, pideCon17Blando, pago);
        return configuracion;
    }

    /**
     * Una tirada de ruleta (HU-56): cobra, gira en el servidor y paga si se acierta (CA-56.1, CA-56.2).
     *
     * @param numero número apostado; obligatorio en un pleno (0–36)
     * @throws IllegalArgumentException si el pleno no lleva un número válido, el importe está fuera de los límites,
     *                                  el juego responsable no lo permite o juega el creador
     * @throws IllegalStateException    si el casino está cerrado
     * @throws es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException si no hay saldo
     */
    @Transactional
    public Partida girarRuleta(String email, ApuestaRuleta tipo, Integer numero, BigDecimal importe) {
        if (tipo == null) {
            throw new IllegalArgumentException("Elige a qué apuestas");
        }
        if (tipo == ApuestaRuleta.PLENO && (numero == null || numero < 0 || numero > 36)) {
            throw new IllegalArgumentException("En un pleno elige un número del 0 al 36");
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        String apuesta = tipo == ApuestaRuleta.PLENO ? "Pleno al " + numero : tipo.getNombre();
        Usuario usuario = cobrar(email, importe, "Ruleta: " + apuesta, ahora);
        Partida partida = new Partida(usuario, TipoJuego.RULETA, importe, apuesta, ahora);

        // El número lo decide siempre el servidor (0 a 36: nextInt(37) nunca devuelve 37)
        int salido = azar.nextInt(37);
        BigDecimal premio = tipo.gana(salido, numero) ? importe.multiply(tipo.getDevuelve()) : BigDecimal.ZERO;
        pagar(usuario, premio, "Ruleta: salió el " + salido, ahora);
        partida.terminar(premio, "Salió el " + salido + " (" + ApuestaRuleta.color(salido) + ")");
        return partidas.save(partida);
    }

    /**
     * Empieza una mano de blackjack (HU-57). Si el usuario ya tiene una a medias, se devuelve esa sin cobrar otra vez
     * (CA-57.5). Si alguien tiene blackjack al repartir, la mano termina y se paga en el acto.
     */
    @Transactional
    public Partida empezarBlackjack(String email, BigDecimal importe) {
        Optional<Partida> enCurso = blackjackEnCurso(email);
        if (enCurso.isPresent()) {
            return enCurso.get();
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Usuario usuario = cobrar(email, importe, "Blackjack: nueva mano", ahora);
        Partida partida = new Partida(usuario, TipoJuego.BLACKJACK, importe, "Blackjack", ahora);
        ManoBlackjack mano = ManoBlackjack.repartir(configuracion().getBarajas(), azar);
        partida.guardarMano(mano);
        if (mano.isTerminada()) {
            liquidar(partida, mano, ahora);
        }
        return partidas.save(partida);
    }

    /** Pide carta en la mano en curso. */
    @Transactional
    public Partida pedir(String email, Long partidaId) {
        Partida partida = manoDe(email, partidaId);
        ManoBlackjack mano = partida.mano();
        mano.pedir(configuracion().isBancaPideCon17Blando());
        return seguir(partida, mano);
    }

    /** Se planta: juega la banca y se paga (CA-57.4). */
    @Transactional
    public Partida plantarse(String email, Long partidaId) {
        Partida partida = manoDe(email, partidaId);
        ManoBlackjack mano = partida.mano();
        mano.plantarse(configuracion().isBancaPideCon17Blando());
        return seguir(partida, mano);
    }

    /**
     * Dobla: se cobra otra vez el mismo importe, recibe una carta y termina la mano (CA-57.3). Lo doblado cuenta para el
     * juego responsable, pero no tiene que respetar el máximo por apuesta: es la misma apuesta.
     */
    @Transactional
    public Partida doblar(String email, Long partidaId) {
        Partida partida = manoDe(email, partidaId);
        ManoBlackjack mano = partida.mano();
        if (!mano.isPuedeDoblar()) {
            throw new IllegalStateException("Solo se puede doblar con las dos primeras cartas");
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Usuario usuario = partida.getUsuario();
        BigDecimal extra = partida.getApostado();
        juegoResponsable.comprobar(usuario, extra, ahora);
        usuario.cargar(extra, TipoMovimiento.CASINO_APUESTA, "Blackjack: doblar", ahora);
        partida.doblarApuesta();
        mano.doblar(configuracion().isBancaPideCon17Blando());
        return seguir(partida, mano);
    }

    /** La mano de blackjack que el usuario tiene a medias. */
    @Transactional(readOnly = true)
    public Optional<Partida> blackjackEnCurso(String email) {
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        return partidas.findFirstByUsuarioAndJuegoAndEstado(usuario, TipoJuego.BLACKJACK, EstadoPartida.EN_CURSO);
    }

    /** Una partida de blackjack del usuario (en curso o terminada); vacío si no existe o es de otro. */
    @Transactional(readOnly = true)
    public Optional<Partida> manoDelUsuario(String email, Long partidaId) {
        return partidas.findById(partidaId)
                .filter(p -> p.getUsuario().getEmail().equals(email) && p.getJuego() == TipoJuego.BLACKJACK);
    }

    /** Lo que se ve de una mano: nunca la carta oculta de la banca ni la baraja (CA-57.1). */
    @Transactional(readOnly = true)
    public MesaBlackjack mesa(Partida partida) {
        return MesaBlackjack.de(partida);
    }

    /** Historial de partidas del usuario, de la más reciente a la más antigua (CA-58.1). */
    @Transactional(readOnly = true)
    public List<Partida> historial(String email) {
        return partidas.findTop50ByUsuarioOrderByFechaDescIdDesc(usuarios.findByEmail(email).orElseThrow());
    }

    /** Cuentas del casino para el panel de la casa (HU-54): partidas, apostado y pagado por juego. */
    @Transactional(readOnly = true)
    public List<ResumenJuego> resumenCasa() {
        List<ResumenJuego> filas = new ArrayList<>();
        for (Object[] fila : partidas.resumenPorJuego()) {
            filas.add(new ResumenJuego((TipoJuego) fila[0], ((Number) fila[1]).longValue(), (BigDecimal) fila[2],
                    (BigDecimal) fila[3]));
        }
        return filas;
    }

    /** Una fila del panel de la casa: lo que se queda la casa es apostado − pagado. */
    public record ResumenJuego(TipoJuego juego, long partidas, BigDecimal apostado, BigDecimal pagado) {

        public BigDecimal getBeneficio() {
            return apostado.subtract(pagado);
        }

        /** Margen real en %, con un decimal; null si no hay nada apostado. */
        public BigDecimal getMargen() {
            return apostado.signum() == 0 ? null
                    : getBeneficio().multiply(BigDecimal.valueOf(100)).divide(apostado, 1, RoundingMode.HALF_UP);
        }
    }

    /** Comprobaciones comunes a todos los juegos y cobro de la apuesta (CA-56.3). */
    private Usuario cobrar(String email, BigDecimal importe, String concepto, LocalDateTime ahora) {
        ConfiguracionCasino configuracion = configuracion();
        if (!configuracion.isActivo()) {
            throw new IllegalStateException("El casino está cerrado ahora mismo");
        }
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        if (usuario.isCreador()) {
            throw new IllegalArgumentException("El creador de apuestas no puede jugar");
        }
        if (importe == null || importe.signum() <= 0 || importe.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Introduce un importe válido, con 2 decimales como mucho");
        }
        configuracion.comprobarImporte(importe);
        juegoResponsable.comprobar(usuario, importe, ahora);
        usuario.cargar(importe, TipoMovimiento.CASINO_APUESTA, concepto, ahora);
        return usuario;
    }

    private void pagar(Usuario usuario, BigDecimal premio, String concepto, LocalDateTime ahora) {
        if (premio.signum() > 0) {
            usuario.abonar(premio, TipoMovimiento.CASINO_PREMIO, concepto, ahora);
        }
    }

    /** La mano en curso del usuario; si no es suya o ya terminó, no se puede seguir jugando. */
    private Partida manoDe(String email, Long partidaId) {
        return partidas.findById(partidaId)
                .filter(p -> p.getUsuario().getEmail().equals(email))
                .filter(p -> p.getJuego() == TipoJuego.BLACKJACK && p.isEnCurso())
                .orElseThrow(() -> new IllegalStateException("Esta mano ya ha terminado"));
    }

    /** Guarda la mano y, si ha terminado, la paga. */
    private Partida seguir(Partida partida, ManoBlackjack mano) {
        partida.guardarMano(mano);
        if (mano.isTerminada()) {
            liquidar(partida, mano, LocalDateTime.now(reloj));
        }
        return partida;
    }

    /**
     * Paga una mano terminada (CA-57.2, CA-57.4): blackjack, la apuesta más lo que diga la configuración (3:2 o 6:5);
     * ganar, el doble; empate, la apuesta; perder, nada. Se redondea hacia abajo, nunca a favor del jugador.
     */
    private void liquidar(Partida partida, ManoBlackjack mano, LocalDateTime ahora) {
        ResultadoMano resultado = mano.resultado();
        BigDecimal apostado = partida.getApostado();
        BigDecimal premio = switch (resultado) {
            case BLACKJACK -> apostado.add(apostado.multiply(configuracion().getPagoBlackjack().getGanancia()));
            case GANA -> apostado.multiply(BigDecimal.valueOf(2));
            case EMPATE -> apostado;
            case PIERDE -> BigDecimal.ZERO;
        };
        premio = premio.setScale(2, RoundingMode.DOWN);
        String marcador = "Tú " + ManoBlackjack.valor(mano.getJugador()) + ", banca "
                + ManoBlackjack.valor(mano.getBanca());
        pagar(partida.getUsuario(), premio, "Blackjack: " + resultado.getDescripcion().toLowerCase(Locale.ROOT), ahora);
        partida.terminar(premio, resultado.getDescripcion() + " (" + marcador + ")");
    }
}
