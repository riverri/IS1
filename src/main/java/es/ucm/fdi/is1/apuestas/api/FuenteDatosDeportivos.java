package es.ucm.fdi.is1.apuestas.api;

import java.time.LocalDate;
import java.util.List;

/** De dónde salen los partidos y resultados reales. Interfaz para poder sustituirla en las pruebas. */
public interface FuenteDatosDeportivos {

    List<PartidoApi> partidos(String competicion, LocalDate desde, LocalDate hasta);

    /** Equipos de una competición con sus plantillas. */
    List<PlantillaApi> plantillas(String competicion);
}
