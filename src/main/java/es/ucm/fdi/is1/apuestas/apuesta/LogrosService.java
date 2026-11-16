package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.prediccion.PosicionPrediccionRepository;
import es.ucm.fdi.is1.apuestas.quiniela.BoletoQuinielaRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

/**
 * Logros de un jugador (HU-88): insignias por hitos que se ven en su perfil público. Se calculan con lo que ya hay
 * guardado (apuestas, boletos y posiciones), así que no hace falta apuntarlos: aparecen en cuanto se cumplen.
 */
@Service
public class LogrosService {

    /** Ganadas seguidas para la racha. */
    static final int RACHA = 5;
    /** Cuota mínima para «Cuota alta». */
    static final BigDecimal CUOTA_ALTA = new BigDecimal("5");
    /** Apuestas resueltas mínimas para «En positivo». */
    static final int RESUELTAS_EN_POSITIVO = 10;

    private final UsuarioRepository usuarios;
    private final ApuestaRepository apuestas;
    private final BoletoQuinielaRepository boletos;
    private final PosicionPrediccionRepository predicciones;

    public LogrosService(UsuarioRepository usuarios, ApuestaRepository apuestas, BoletoQuinielaRepository boletos,
                         PosicionPrediccionRepository predicciones) {
        this.usuarios = usuarios;
        this.apuestas = apuestas;
        this.boletos = boletos;
        this.predicciones = predicciones;
    }

    /** Una insignia, conseguida o no. */
    public record Logro(String icono, String nombre, String descripcion, boolean conseguido) {
    }

    /** Todos los logros del jugador, en el mismo orden siempre (CA-88.1). */
    @Transactional(readOnly = true)
    public List<Logro> de(Long usuarioId) {
        Usuario usuario = usuarios.findById(usuarioId).orElseThrow();
        List<Apuesta> suyas = apuestas.findByUsuarioAndEstadoInOrderByFechaDesc(usuario,
                EnumSet.complementOf(EnumSet.of(EstadoApuesta.CANCELADA)));
        List<Apuesta> resueltas = suyas.stream().filter(Apuesta::isResuelta)
                .sorted(Comparator.comparing(Apuesta::getFecha).thenComparing(Apuesta::getId)).toList();
        List<Apuesta> ganadas = resueltas.stream().filter(a -> a.getEstado() == EstadoApuesta.GANADA).toList();
        Estadisticas estadisticas = Estadisticas.de(resueltas);
        return List.of(
                new Logro("🎯", "Primera apuesta", "Hacer una apuesta.", !suyas.isEmpty()),
                new Logro("🏆", "Primer acierto", "Ganar una apuesta.", !ganadas.isEmpty()),
                new Logro("🔗", "Combinada ganadora", "Ganar una combinada.", ganadas.stream().anyMatch(Apuesta::isCombinada)),
                new Logro("🔥", "Racha de " + RACHA, "Ganar " + RACHA + " apuestas seguidas.", rachaMaxima(resueltas) >= RACHA),
                new Logro("💎", "Cuota alta", "Ganar una apuesta con cuota de 5 o más.",
                        ganadas.stream().anyMatch(a -> a.getCuota().compareTo(CUOTA_ALTA) >= 0)),
                new Logro("📈", "En positivo", "Ir ganando dinero con al menos " + RESUELTAS_EN_POSITIVO + " apuestas resueltas.",
                        resueltas.size() >= RESUELTAS_EN_POSITIVO && estadisticas.beneficio().signum() > 0),
                new Logro("🎟️", "Quiniela ganadora", "Llevarse parte del bote de una quiniela.",
                        boletos.findByUsuario(usuario).stream().anyMatch(b -> b.isGanador())),
                new Logro("🔮", "Visionario", "Acertar un mercado de predicción.",
                        predicciones.deMercadosResueltos().stream().anyMatch(p -> p.getUsuario().getId().equals(usuarioId)
                                && p.getLado() == p.getMercado().getResultado() && p.getPagado().signum() > 0)));
    }

    /** La racha más larga de ganadas seguidas, en el orden en que se hicieron. */
    private static int rachaMaxima(List<Apuesta> resueltas) {
        int actual = 0;
        int maxima = 0;
        for (Apuesta a : resueltas) {
            actual = a.getEstado() == EstadoApuesta.GANADA ? actual + 1 : 0;
            maxima = Math.max(maxima, actual);
        }
        return maxima;
    }
}
