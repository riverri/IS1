package es.ucm.fdi.is1.apuestas.anuncios;

import java.util.Arrays;
import java.util.Optional;

/**
 * Secciones de la web donde se puede mostrar un anuncio y a las que puede llevar (HU-60, HU-61).
 * Cada valor lleva su nombre, la ruta con la que empiezan sus páginas y si hace falta sesión para entrar
 * (las de sesión son las que {@code SeguridadConfig} no deja abiertas a los visitantes).
 *
 * <p>Qué anuncio sale en cada una lo decide el creador en Gestión; aquí solo está la lista de secciones,
 * que cambia únicamente cuando la web tiene una sección nueva.
 */
public enum Ubicacion {

    PORTADA("Portada", "/", false),
    EVENTOS("Catálogo de eventos", "/eventos", false),
    MERCADOS("Apuestas a largo plazo", "/mercados", false),
    EQUIPOS("Equipos", "/equipos", false),
    RANKING("Ranking", "/ranking", false),
    MIS_APUESTAS("Mis apuestas", "/apuestas", true),
    LIGAS("Ligas", "/ligas", true),
    CUENTA("Mi cuenta", "/cuenta", true);

    private final String nombre;
    private final String ruta;
    private final boolean requiereSesion;

    Ubicacion(String nombre, String ruta, boolean requiereSesion) {
        this.nombre = nombre;
        this.ruta = ruta;
        this.requiereSesion = requiereSesion;
    }

    /**
     * Sección a la que pertenece una ruta: "/eventos?deporte=FUTBOL" y "/eventos/7/apostar" son de EVENTOS; "/" es la
     * portada. Vacío si la ruta no es de ninguna (Gestión, el login, una dirección externa…).
     */
    public static Optional<Ubicacion> de(String ruta) {
        if (ruta == null || !ruta.startsWith("/") || ruta.startsWith("//")) {
            return Optional.empty();
        }
        // Sin los parámetros (?…) ni el ancla (#…)
        String camino = ruta.split("[?#]", 2)[0];
        if (camino.equals("/")) {
            return Optional.of(PORTADA);
        }
        return Arrays.stream(values())
                .filter(u -> u != PORTADA)
                .filter(u -> camino.equals(u.ruta) || camino.startsWith(u.ruta + "/"))
                .findFirst();
    }

    public String getNombre() {
        return nombre;
    }

    public String getRuta() {
        return ruta;
    }

    public boolean isRequiereSesion() {
        return requiereSesion;
    }
}
