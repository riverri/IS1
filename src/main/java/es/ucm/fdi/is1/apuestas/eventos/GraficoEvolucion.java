package es.ucm.fdi.is1.apuestas.eventos;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.ucm.fdi.is1.apuestas.equipos.Equipo;

/**
 * Evolución de un equipo a lo largo de la temporada (HU-32): puntos acumulados partido a partido
 * (3 por victoria y 1 por empate). Las coordenadas se calculan aquí para dibujar un SVG sin JavaScript.
 */
public record GraficoEvolucion(int ancho, int alto, String linea, List<Marca> puntos, List<Marca> ejeY,
                               List<Marca> ejeX, double base, double izquierda, double derecha) {

    static final int ANCHO = 640;
    static final int ALTO = 220;
    static final double MARGEN_IZQ = 36;
    static final double MARGEN_DER = 16;
    static final double MARGEN_ARRIBA = 16;
    static final double MARGEN_ABAJO = 28;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-ES"));

    /** Un punto, una marca del eje o una etiqueta: posición, texto y clase CSS. */
    public record Marca(double x, double y, String texto, String clase) {
    }

    /**
     * @param jugados partidos finalizados del equipo, del más antiguo al más reciente
     * @return null si hay menos de dos partidos (una línea necesita al menos dos puntos)
     */
    static GraficoEvolucion de(List<Evento> jugados, Equipo equipo) {
        if (jugados.size() < 2) {
            return null;
        }
        List<Integer> acumulados = new ArrayList<>();
        int total = 0;
        for (Evento evento : jugados) {
            total += switch (ResultadoEquipo.de(evento, equipo)) {
                case VICTORIA -> 3;
                case EMPATE -> 1;
                case DERROTA -> 0;
            };
            acumulados.add(total);
        }
        int maximo = Math.max(3, (int) Math.ceil(total / 3.0) * 3);
        double altoUtil = ALTO - MARGEN_ARRIBA - MARGEN_ABAJO;
        double anchoUtil = ANCHO - MARGEN_IZQ - MARGEN_DER;
        double paso = anchoUtil / (jugados.size() - 1);

        List<Marca> puntos = new ArrayList<>();
        List<Marca> ejeX = new ArrayList<>();
        StringBuilder linea = new StringBuilder();
        int cadaCuantos = Math.max(1, (int) Math.ceil(jugados.size() / 8.0));
        for (int i = 0; i < jugados.size(); i++) {
            Evento evento = jugados.get(i);
            double x = MARGEN_IZQ + i * paso;
            double y = MARGEN_ARRIBA + altoUtil * (1 - acumulados.get(i) / (double) maximo);
            linea.append(i == 0 ? "M" : " L").append(redondear(x)).append(',').append(redondear(y));
            ResultadoEquipo resultado = ResultadoEquipo.de(evento, equipo);
            Equipo rival = evento.getLocal().getId().equals(equipo.getId()) ? evento.getVisitante() : evento.getLocal();
            puntos.add(new Marca(x, y, FECHA.format(evento.getFechaHora()) + " · " + resultado.getDescripcion()
                    + " contra " + rival.getNombre() + " · " + acumulados.get(i) + " puntos",
                    resultado.name().toLowerCase(Locale.ROOT)));
            if (i % cadaCuantos == 0 || i == jugados.size() - 1) {
                ejeX.add(new Marca(x, ALTO - 8, FECHA.format(evento.getFechaHora()), ""));
            }
        }
        List<Marca> ejeY = new ArrayList<>();
        for (int valor = 0; valor <= maximo; valor += Math.max(1, maximo / 3)) {
            ejeY.add(new Marca(MARGEN_IZQ - 8, MARGEN_ARRIBA + altoUtil * (1 - valor / (double) maximo),
                    String.valueOf(valor), ""));
        }
        return new GraficoEvolucion(ANCHO, ALTO, linea.toString(), puntos, ejeY, ejeX,
                ALTO - MARGEN_ABAJO, MARGEN_IZQ, ANCHO - MARGEN_DER);
    }

    private static String redondear(double valor) {
        return String.format(Locale.ROOT, "%.1f", valor);
    }

    /** Puntos acumulados al final, para el texto alternativo. */
    public String getResumen() {
        return puntos.isEmpty() ? "" : puntos.getLast().texto();
    }
}
