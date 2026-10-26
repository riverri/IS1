package es.ucm.fdi.is1.apuestas.eventos;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.ucm.fdi.is1.apuestas.equipos.Equipo;

/**
 * Evolución de un equipo a lo largo de la temporada (HU-32): puntos acumulados partido a partido
 * (3 por victoria y 1 por empate). Las coordenadas se calculan aquí para dibujar un SVG sin JavaScript.
 *
 * <p>Forma parte de {@link FichaEquipo}; la plantilla {@code equipo.html} solo pinta los datos.
 * (SVG es un formato de dibujo dentro del HTML: se describen líneas y círculos con coordenadas x, y en píxeles,
 * con el origen (0, 0) en la esquina de arriba a la izquierda.)
 *
 * @param ancho     ancho del SVG en píxeles
 * @param alto      alto del SVG en píxeles
 * @param linea     atributo {@code d} del {@code <path>} que une los puntos ("M x,y L x,y …")
 * @param puntos    un círculo por partido, con su descripción como texto emergente
 * @param ejeY      etiquetas de puntos del eje vertical
 * @param ejeX      etiquetas de fecha del eje horizontal
 * @param base      coordenada y de la línea de 0 puntos (parte de abajo del área del gráfico)
 * @param izquierda coordenada x donde empiezan las líneas horizontales de la rejilla
 * @param derecha   coordenada x donde terminan las líneas horizontales de la rejilla
 */
public record GraficoEvolucion(int ancho, int alto, String linea, List<Marca> puntos, List<Marca> ejeY,
                               List<Marca> ejeX, double base, double izquierda, double derecha) {

    static final int ANCHO = 640;
    static final int ALTO = 220;
    // Márgenes que dejan sitio a las etiquetas de los ejes
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
        // Puntos acumulados después de cada partido
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
        // Tope del eje Y: el total redondeado hacia arriba a un múltiplo de 3 (como mínimo 3),
        // para que las marcas del eje (cada tercio) sean números enteros
        int maximo = Math.max(3, (int) Math.ceil(total / 3.0) * 3);
        double altoUtil = ALTO - MARGEN_ARRIBA - MARGEN_ABAJO;
        double anchoUtil = ANCHO - MARGEN_IZQ - MARGEN_DER;
        // Separación horizontal entre partidos: el primero queda en el borde izquierdo y el último en el derecho
        double paso = anchoUtil / (jugados.size() - 1);

        List<Marca> puntos = new ArrayList<>();
        List<Marca> ejeX = new ArrayList<>();
        StringBuilder linea = new StringBuilder();
        // Como mucho unas 8 fechas en el eje X, para que no se solapen
        int cadaCuantos = Math.max(1, (int) Math.ceil(jugados.size() / 8.0));
        for (int i = 0; i < jugados.size(); i++) {
            Evento evento = jugados.get(i);
            double x = MARGEN_IZQ + i * paso;
            // En SVG la y crece hacia abajo: 0 puntos va abajo y el máximo arriba
            // (double) maximo: sin él, dividir dos enteros daría un entero (2 / 3 = 0) y se perdería la fracción
            double y = MARGEN_ARRIBA + altoUtil * (1 - acumulados.get(i) / (double) maximo);
            // "M x,y" mueve el lápiz al primer punto; " L x,y" traza una línea hasta el siguiente
            linea.append(i == 0 ? "M" : " L").append(redondear(x)).append(',').append(redondear(y));
            ResultadoEquipo resultado = ResultadoEquipo.de(evento, equipo);
            Equipo rival = evento.getLocal().getId().equals(equipo.getId()) ? evento.getVisitante() : evento.getLocal();
            // Texto emergente del punto, y la clase CSS (victoria, empate o derrota) para el color
            puntos.add(new Marca(x, y, FECHA.format(evento.getFechaHora()) + " · " + resultado.getDescripcion()
                    + " contra " + rival.getNombre() + " · " + acumulados.get(i) + " puntos",
                    resultado.name().toLowerCase(Locale.ROOT)));
            // La fecha del último partido se muestra siempre. "%" es el resto de la división:
            // i % 3 == 0 es cierto para i = 0, 3, 6... (una de cada tres fechas)
            if (i % cadaCuantos == 0 || i == jugados.size() - 1) {
                ejeX.add(new Marca(x, ALTO - 8, FECHA.format(evento.getFechaHora()), ""));
            }
        }
        // Marcas del eje Y: 0, un tercio, dos tercios y el máximo
        List<Marca> ejeY = new ArrayList<>();
        for (int valor = 0; valor <= maximo; valor += Math.max(1, maximo / 3)) {
            ejeY.add(new Marca(MARGEN_IZQ - 8, MARGEN_ARRIBA + altoUtil * (1 - valor / (double) maximo),
                    String.valueOf(valor), ""));
        }
        return new GraficoEvolucion(ANCHO, ALTO, linea.toString(), puntos, ejeY, ejeX,
                ALTO - MARGEN_ABAJO, MARGEN_IZQ, ANCHO - MARGEN_DER);
    }

    /** Un decimal y punto como separador, sea cual sea el idioma del servidor (el SVG lo exige). */
    private static String redondear(double valor) {
        return String.format(Locale.ROOT, "%.1f", valor);
    }

    /** Puntos acumulados al final, para el texto alternativo. */
    public String getResumen() {
        return puntos.isEmpty() ? "" : puntos.getLast().texto();
    }
}
