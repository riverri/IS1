package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import es.ucm.fdi.is1.apuestas.cripto.CategoriaCripto;
import es.ucm.fdi.is1.apuestas.cripto.OperacionCripto;
import es.ucm.fdi.is1.apuestas.cripto.OperacionCriptoRepository;
import es.ucm.fdi.is1.apuestas.cripto.PosicionCripto;
import es.ucm.fdi.is1.apuestas.cripto.PosicionCriptoRepository;
import es.ucm.fdi.is1.apuestas.prediccion.PosicionPrediccion;
import es.ucm.fdi.is1.apuestas.prediccion.PosicionPrediccionRepository;
import es.ucm.fdi.is1.apuestas.quiniela.BoletoQuiniela;
import es.ucm.fdi.is1.apuestas.quiniela.BoletoQuinielaRepository;

/**
 * Estadísticas de cada jugador en un tipo de juego (HU-84), para {@link RankingService}. Solo salen los que han
 * jugado a ese tipo:
 * <ul>
 * <li>deportes: sus apuestas resueltas, como en el ranking general;</li>
 * <li>predicciones: sus posiciones en mercados resueltos; acierta si tenía el lado de la respuesta;</li>
 * <li>cripto y memecoins: sus compras y ventas; el beneficio es lo vendido más lo que vale hoy lo que tiene, menos
 * lo comprado;</li>
 * <li>quiniela: sus boletos en quinielas repartidas, sin los devueltos; acierta si cobró parte del bote.</li>
 * </ul>
 */
@Component
class EstadisticasPorJuego {

    private final ApuestaRepository apuestas;
    private final PosicionPrediccionRepository prediccion;
    private final OperacionCriptoRepository operacionesCripto;
    private final PosicionCriptoRepository posicionesCripto;
    private final BoletoQuinielaRepository boletos;

    EstadisticasPorJuego(ApuestaRepository apuestas, PosicionPrediccionRepository prediccion,
                         OperacionCriptoRepository operacionesCripto, PosicionCriptoRepository posicionesCripto,
                         BoletoQuinielaRepository boletos) {
        this.apuestas = apuestas;
        this.prediccion = prediccion;
        this.operacionesCripto = operacionesCripto;
        this.posicionesCripto = posicionesCripto;
        this.boletos = boletos;
    }

    /** Id del usuario → sus estadísticas en ese tipo de juego. Null = el ranking general (las apuestas). */
    Map<Long, Estadisticas> de(TipoJuego juego) {
        return switch (juego == null ? TipoJuego.DEPORTES : juego) {
            case DEPORTES -> deportes();
            case PREDICCIONES -> predicciones();
            case CRIPTO -> cripto(CategoriaCripto.REFERENCIA);
            case MEMECOINS -> cripto(CategoriaCripto.MEMECOIN);
            case QUINIELA -> quiniela();
        };
    }

    private Map<Long, Estadisticas> deportes() {
        Map<Long, Estadisticas> porUsuario = new HashMap<>();
        // Una sola consulta agregada para todos: fila = [id, resueltas, ganadas, apostado, pagado]
        for (Object[] fila : apuestas.resumenResueltasPorUsuario()) {
            // El tipo exacto de count y sum (Long, Integer…) depende de la consulta: se leen como Number
            porUsuario.put((Long) fila[0], Estadisticas.de(((Number) fila[1]).longValue(),
                    ((Number) fila[2]).longValue(), (BigDecimal) fila[3], (BigDecimal) fila[4]));
        }
        return porUsuario;
    }

    private Map<Long, Estadisticas> predicciones() {
        Map<Long, Estadisticas> porUsuario = new HashMap<>();
        for (PosicionPrediccion p : prediccion.deMercadosResueltos()) {
            int ganada = p.getLado() == p.getMercado().getResultado() ? 1 : 0;
            sumar(porUsuario, p.getUsuario().getId(),
                    new Estadisticas(1, ganada, p.getInvertido(), p.getPagado().subtract(p.getInvertido())));
        }
        return porUsuario;
    }

    private Map<Long, Estadisticas> cripto(CategoriaCripto categoria) {
        Map<Long, Estadisticas> porUsuario = new HashMap<>();
        // fila = [id del usuario, tipo, número de operaciones, suma de importes]
        for (Object[] fila : operacionesCripto.resumenPorUsuario(categoria)) {
            BigDecimal importe = (BigDecimal) fila[3];
            boolean compra = fila[1] == OperacionCripto.Tipo.COMPRA;
            // Comprar resta y vender suma; lo comprado es lo "apostado"
            sumar(porUsuario, (Long) fila[0], new Estadisticas((int) ((Number) fila[2]).longValue(), 0,
                    compra ? importe : BigDecimal.ZERO, compra ? importe.negate() : importe));
        }
        for (PosicionCripto p : posicionesCripto.findAll()) {
            if (p.getCripto().getCategoria() == categoria && porUsuario.containsKey(p.getUsuario().getId())) {
                sumar(porUsuario, p.getUsuario().getId(), new Estadisticas(0, 0, BigDecimal.ZERO, p.getValor()));
            }
        }
        return porUsuario;
    }

    private Map<Long, Estadisticas> quiniela() {
        Map<Long, Estadisticas> porUsuario = new HashMap<>();
        for (BoletoQuiniela b : boletos.deQuinielasRepartidas()) {
            if (!b.isDevuelto()) {
                sumar(porUsuario, b.getUsuario().getId(),
                        new Estadisticas(1, b.isGanador() ? 1 : 0, b.getPagado(), b.getBeneficio()));
            }
        }
        return porUsuario;
    }

    private static void sumar(Map<Long, Estadisticas> porUsuario, Long usuario, Estadisticas mas) {
        // merge: si el usuario no está, guarda "mas"; si está, guarda la suma de las dos
        porUsuario.merge(usuario, mas, (a, b) -> new Estadisticas(a.resueltas() + b.resueltas(),
                a.ganadas() + b.ganadas(), a.apostado().add(b.apostado()), a.beneficio().add(b.beneficio())));
    }
}
